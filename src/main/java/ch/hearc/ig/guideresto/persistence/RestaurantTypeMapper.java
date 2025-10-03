package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.RestaurantType;

import java.util.HashSet;
import java.util.Set;
import java.sql.*;

public class RestaurantTypeMapper extends AbstractMapper<RestaurantType> {
    private Connection c = ConnectionUtils.getConnection();

    public RestaurantType findById(int id) {
        RestaurantType type = null;
        Connection c = ConnectionUtils.getConnection();
        try {
            PreparedStatement s = c.prepareStatement("SELECT * FROM types_gastronomiques WHERE numero = ?");
            s.setInt(1, id);
            ResultSet rs = s.executeQuery();
            // recherche sur la clé primaire donc max 1 resultat
            // sinon on remplirait une List avec une boucle while
            if(rs.next()) {
                type = new RestaurantType(
                    rs.getInt("numero"),
                    rs.getString("libelle"),
                    rs.getString("description")
                );
            } else {
                logger.error("No such restaurant type");
            }
        } catch (SQLException e) {
            logger.error("SQLException: {}", e.getMessage());
        }
        return type;
    }
    public Set<RestaurantType> findAll() {
        Set<RestaurantType> types = new HashSet<>();
        try {
            PreparedStatement s = c.prepareStatement("SELECT * FROM types_gastronomiques");
            ResultSet rs = s.executeQuery();
            while(rs.next()) {
                types.add(new RestaurantType(
                        rs.getInt("numero"),
                        rs.getString("libelle"),
                        rs.getString("description")
                ));
            }
        } catch (SQLException e) {
            logger.error("SQLException: {}", e.getMessage());
        }
        return types;
    }
    public RestaurantType create(RestaurantType type) {
        try {
            PreparedStatement s = c.prepareStatement("INSERT INTO types_gastronomiques (libelle, description)" +
                    "VALUES (?, ?)");
            s.setString(1, type.getLabel());
            s.setString(2, type.getDescription());
            int affectedRows = s.executeUpdate();
            if (affectedRows > 0) {
                ResultSet rs = s.getGeneratedKeys();
                type.setId(rs.getInt(1));
            } else {
                logger.error("Failed to insert type into the table: ", type.getLabel() );
            }
        } catch (SQLException e) {
            logger.error("SQLException: {}", e.getMessage());
        }
        return type;
    }
    public boolean update(RestaurantType type) {
        int affectedRows = 0;
        try {
            PreparedStatement s = c.prepareStatement("UPDATE types_gastronomiques" +
                    "SET libelle = ?, description = ?" +
                    "WHERE numero = ?");
            s.setString(1, type.getLabel());
            s.setString(2, type.getDescription());
            s.setInt(3, type.getId());
            affectedRows = s.executeUpdate();
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
            PreparedStatement s = c.prepareStatement("DELETE types_gastronomiques" +
                    "WHERE numero = ?");
            s.setInt(1, id);
            affectedRows = s.executeUpdate();
        } catch (SQLException e) {
            logger.error("SQLException: {}", e.getMessage());
        }
        return affectedRows > 0;
    }

    protected String getSequenceQuery(){
        // seq.nextval est spécifique a oracle... bad practice ?
        return "SELECT seq_types_gastronomiques.CurrVal FROM dual";
    }
    protected String getExistsQuery() {
        return "SELECT numero FROM types_gastronomiques WHERE numero = ?";
    }
    protected String getCountQuery() {
        return "SELECT Count(*) FROM types_gastronomiques";
    }

}
