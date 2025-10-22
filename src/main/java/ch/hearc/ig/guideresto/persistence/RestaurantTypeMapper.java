package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.RestaurantType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.sql.*;

public class RestaurantTypeMapper extends AbstractMapper<RestaurantType> {
    private final Connection connection;
    private Map<Long, RestaurantType> cache = new HashMap<>();

    public RestaurantTypeMapper(Connection connection) {
        this.connection = connection;
    }

    private RestaurantType addToCache(ResultSet rs) throws SQLException {
        int id = rs.getInt("numero");

        if (!cache.containsKey((long) id)) {
            RestaurantType type = new RestaurantType(
                    id,
                    rs.getString("libelle"),
                    rs.getString("description")
            );
            cache.put((long) id, type);
        }

        return cache.get((long) id);
    }

    public RestaurantType findById(int id) {
        if (cache.containsKey((long)id)) {
            return cache.get((long) id);
        }
        RestaurantType type = null;
        try {
            PreparedStatement s = connection.prepareStatement("SELECT * FROM types_gastronomiques WHERE numero = ?");
            s.setInt(1, id);
            ResultSet rs = s.executeQuery();
            // recherche sur la clé primaire donc max 1 resultat
            // sinon on remplirait une List avec une boucle while
            if(rs.next()) {
                type = addToCache(rs);
            } else {
                logger.error("No such restaurant type");
            }
            rs.close();
        } catch (SQLException e) {
            logger.error("SQLException: {}", e.getMessage());
        }
        return type;
    }
    public RestaurantType findByLabel(String label) {
        RestaurantType type = null;
        try {
            PreparedStatement s = connection.prepareStatement("SELECT * FROM types_gastronomiques WHERE libelle = ?");
            s.setString(1, label);
            ResultSet rs = s.executeQuery();
            // le libelle est une colonne a contrainte unique
            if(rs.next()) {
                type = new RestaurantType(
                        rs.getInt("numero"),
                        rs.getString("libelle"),
                        rs.getString("description")
                );
            } else {
                logger.error("No such restaurant type");
            }
            rs.close();
        } catch (SQLException e) {
            logger.error("SQLException: {}", e.getMessage());
        }
        return type;
    }

    public Set<RestaurantType> findAll() {
        Set<RestaurantType> types = new HashSet<>();
        try {
            PreparedStatement s = connection.prepareStatement("SELECT * FROM types_gastronomiques");
            ResultSet rs = s.executeQuery();
            while(rs.next()) {
                types.add(new RestaurantType(
                        rs.getInt("numero"),
                        rs.getString("libelle"),
                        rs.getString("description")
                ));
            }
            rs.close();
        } catch (SQLException e) {
            logger.error("SQLException: {}", e.getMessage());
        }
        return types;
    }
    public RestaurantType create(RestaurantType type) {
        try {
            String generatedColumns[] = { "numero" };
            PreparedStatement s = connection.prepareStatement(
                    "INSERT INTO types_gastronomiques (libelle, description)" +
                    "VALUES (?, ?)",
                    generatedColumns);
            s.setString(1, type.getLabel());
            s.setString(2, type.getDescription());
            s.executeUpdate();
            ResultSet rs = s.getGeneratedKeys();
            if (rs.next()) {
                type.setId(rs.getInt(1));
                cache.put((long) type.getId(), type); //identity map here 😬😬😬😬
            } else {
                logger.warn("Failed to insert type into the table: ", type.getLabel() + ". Continuing..." );
            }
            rs.close();
            connection.commit();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1) {
                // le type existe deja: violation de contrainte unique sur le libelle
                // -> on gère. retourne l'id comme si tout s'était bien passé.
                type = this.findByLabel(type.getLabel());
                logger.warn("Type already exists: " + type.getLabel() + ". Continuing...");
            } else {
            logger.error("SQLException: {}", e.getMessage());
            }
        }
        return type;
    }

    public boolean update(RestaurantType type) {
        int affectedRows = 0;
        try {
            PreparedStatement s = connection.prepareStatement(
                    "UPDATE types_gastronomiques SET libelle = ?, description = ? WHERE numero = ?");
            s.setString(1, type.getLabel());
            s.setString(2, type.getDescription());
            s.setInt(3, type.getId());
            affectedRows = s.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            logger.error("SQLException: {}", e.getMessage());
        }
        return affectedRows > 0;
    }
    public boolean delete(RestaurantType type) {
        return this.deleteById(type.getId());
    }
    public boolean deleteById(int id) {
        int affectedRows = 0;
        try {
            PreparedStatement s = connection.prepareStatement(
                    "DELETE types_gastronomiques WHERE numero = ?");
            s.setInt(1, id);
            affectedRows = s.executeUpdate();
            if (affectedRows > 0) {//cache !!!
                cache.remove((long) id);
            }
            connection.commit();
        } catch (SQLException e) {
            logger.error("SQLException: {}", e.getMessage());
        }
        return affectedRows > 0;
    }

    protected String getSequenceQuery(){
        return "SELECT seq_types_gastronomiques.NextVal FROM dual";
    }
    protected String getExistsQuery() {
        return "SELECT numero FROM types_gastronomiques WHERE numero = ?";
    }
    protected String getCountQuery() {
        return "SELECT Count(*) FROM types_gastronomiques";
    }

    public void clearCache(){
        cache.clear();
    }

}
