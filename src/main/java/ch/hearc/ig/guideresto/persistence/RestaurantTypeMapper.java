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
    public boolean update(RestaurantType object) {
        // TODO
        return false;
    }
    public boolean delete(RestaurantType object) {
        // TODO
        return false;
    }
    public boolean deleteById(int id) {
        // TODO
        return false;
    }

    protected String getSequenceQuery(){
        return "SELECT seq_types_gastronomiques.NextVal() FROM dual";
    }
    protected String getExistsQuery() {
        return "SELECT numero FROM types_gastronomiques WHERE numero = ?";
    }
    protected String getCountQuery() {
        return "SELECT Count(*) FROM types_gastronomiques";
    }

}
