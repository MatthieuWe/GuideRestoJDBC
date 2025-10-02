package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.RestaurantType;

import java.util.HashSet;
import java.util.Set;
import java.sql.*;

public class RestaurantTypeMapper extends AbstractMapper<RestaurantType> {
    private Connection c = ConnectionUtils.getConnection();

    public RestaurantType findById(int id) {
        // TODO checker si le no existe en DB avec this.exists() et lancer une exception
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
            s.executeUpdate();
            // TODO get the id and add it to the type
        } catch (SQLException e) {
            logger.error("SQLException: {}", e.getMessage());
        }
        return type;
    }
    public boolean update(RestaurantType type) {
        try {
            PreparedStatement s = c.prepareStatement("UPDATE types_gastronomiques" +
                    "SET libelle = ?, description = ?" +
                    "WHERE numero = ?");
            s.setString(1, type.getLabel());
            s.setString(2, type.getDescription());
            s.setInt(3, type.getId());
            s.executeUpdate();
            return this.exists(type.getId()); // C'est pas très opti ça...
        } catch (SQLException e) {
            logger.error("SQLException: {}", e.getMessage());
            return false;
        }
    }
    public boolean delete(RestaurantType type) {
        return this.deleteById(type.getId());
    }
    public boolean deleteById(int id) {
        try {
            PreparedStatement s = c.prepareStatement("DELETE types_gastronomiques" +
                    "WHERE numero = ?");
            s.setInt(1, id);
            s.executeUpdate();
            return true;
        } catch (SQLException e) {
            logger.error("SQLException: {}", e.getMessage());
            return false;
        }
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
