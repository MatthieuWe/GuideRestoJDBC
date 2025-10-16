package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.City;
import ch.hearc.ig.guideresto.business.Localisation;
import ch.hearc.ig.guideresto.business.Restaurant;
import ch.hearc.ig.guideresto.business.RestaurantType;

import java.util.HashSet;
import java.util.Set;
import java.sql.*;

public class RestaurantMapper extends AbstractMapper<Restaurant> {

    private Connection c = ConnectionUtils.getConnection();

    public Restaurant findById(int id) {
        Restaurant resto = null;
        try {
            PreparedStatement s = c.prepareStatement("SELECT r.numero num_resto, r.nom, r.description desc_resto, r.site_web," +
                    " r.adresse, v.numero num_ville, v.nom_ville, v.code_postal," +
                    " t.numero num_type, t.libelle, t.description desc_type" +
                    " FROM restaurants r" +
                    " INNER JOIN villes v ON r.fk_ville = v.numero" +
                    " INNER JOIN types_gastronomiques t ON r.fk_type = t.numero" +
                    " WHERE numero = ?");
            s.setInt(1, id);
            ResultSet rs = s.executeQuery();

            if(rs.next()) {
                resto = this.loadRestaurant(rs);
            } else {
                logger.error("No restaurant found with id " + id);
            }
            rs.close();
        } catch (SQLException e) {
            logger.error("SQLException: {}", e.getMessage());
        }
        return resto;
    }

    public Set<Restaurant> findForCity(City city) {
        Set<Restaurant> restos = new HashSet<>();
        try {
            PreparedStatement s = c.prepareStatement("SELECT r.numero num_resto, r.nom, r.description desc_resto, r.site_web," +
                    " r.adresse, t.numero num_type, t.libelle, t.description desc_type" +
                    " FROM restaurants r" +
                    " INNER JOIN types_gastronomiques t ON r.fk_type = t.numero" +
                    " WHERE r.fk_ville = ?");
            s.setInt(1, city.getId());
            ResultSet rs = s.executeQuery();
            while(rs.next()) {
                restos.add(this.loadRestaurant(rs, city));
            }
            rs.close();
        } catch (SQLException e) {
            logger.error("SQLException: {}", e.getMessage());
        }
        return restos;
    }
    public Set<Restaurant> findAll() {
        Set<Restaurant> restos = new HashSet<>();
        try {
            PreparedStatement s = c.prepareStatement("SELECT r.numero num_resto, r.nom, r.description desc_resto, r.site_web," +
                    " r.adresse, v.numero num_ville, v.nom_ville, v.code_postal," +
                    " t.numero num_type, t.libelle, t.description desc_type" +
                    " FROM restaurants r" +
                    " INNER JOIN villes v ON r.fk_ville = v.numero" +
                    " INNER JOIN types_gastronomiques t ON r.fk_type = t.numero");
            ResultSet rs = s.executeQuery();
            while(rs.next()) {
                restos.add(this.loadRestaurant(rs));
            }
            rs.close();
        } catch (SQLException e) {
            logger.error("SQLException: {}", e.getMessage());
        }
        return restos;
    }
    private Restaurant loadRestaurant(ResultSet rs, City city) throws SQLException {
        /*
        Pour chaque resto qu'on charge en mémoire, on crée un nouvel objet en mémoire pour chaque ville et type
        Chaque resto aura une ville (Neuchâtel) qui est égale aux autres au sens de equals() mais pas au sens de ==
            -> ce sont d'autres objets, elle est chargée plein de fois, on ne peut pas partir d'un de ces objets
            pour retrouver tous les restos sans les charger à double depuis la DB...
        TODO il nous faut un moyen de tracker les objets en mémoire pour assurer leur unicité -> une identity map.
         */
        // Eager Loading relation n..1
        Localisation address = new Localisation(rs.getString("adresse"), city);
        RestaurantType type = new RestaurantType(rs.getInt("num_type"),
                rs.getString("libelle"),
                rs.getString("desc_type"));
        Restaurant resto = new Restaurant(
                rs.getInt("num_resto"),
                rs.getString("nom"),
                rs.getString("desc_resto"),
                rs.getString("site_web"),
                address,
                type);
        return resto;
    }
    private Restaurant loadRestaurant(ResultSet rs) throws SQLException {
        City city = new City(rs.getInt("num_ville"),
                rs.getString("code_postal"),
                rs.getString("nom_ville"));
        return this.loadRestaurant(rs, city);
    }
    public Restaurant create(Restaurant resto) {
        try {
            String generatedColumns[] = { "numero" };
            PreparedStatement s = c.prepareStatement(
                    "INSERT INTO restaurants (nom, description, site_web, adresse, fk_type, fk_ville)" +
                            "VALUES (?, ?, ?, ?, ?, ?)",
                    generatedColumns);
            s.setString(1, resto.getName());
            s.setString(2, resto.getDescription());
            s.setString(3, resto.getWebsite());
            s.setString(4, resto.getAddress().getStreet());
            s.setInt(5, resto.getType().getId());
            s.setInt(6, resto.getAddress().getCity().getId());
            s.executeUpdate();
            ResultSet rs = s.getGeneratedKeys();
            if (rs.next()) {
                resto.setId(rs.getInt(1));
            } else {
                logger.warn("Failed to insert resto into the table: ", resto.getName() + ". Continuing..." );
            }
            rs.close();
            c.commit();
        } catch (SQLException e) {
            logger.error("SQLException: {}", e.getMessage());
        }
        return resto;
    }
    public boolean update(Restaurant resto) {
        int affectedRows = 0;
        try {
            PreparedStatement s = c.prepareStatement(
                    "UPDATE restaurants"+
                            " SET nom = ?, description = ?, site_web = ?, adresse = ?, fk_type = ?, fk_ville = ?"+
                            " WHERE numero = ?");
            s.setString(1, resto.getName());
            s.setString(2, resto.getDescription());
            s.setString(3, resto.getWebsite());
            s.setString(4, resto.getAddress().getStreet());
            s.setInt(5, resto.getType().getId());
            s.setInt(6, resto.getAddress().getCity().getId());
            s.setInt(7, resto.getId());
            affectedRows = s.executeUpdate();
            c.commit();
        } catch (SQLException e) {
            logger.error("SQLException: {}", e.getMessage());
        }
        return affectedRows > 0;
    }
    public boolean delete(Restaurant resto) {
        return this.deleteById(resto.getId());
    }
    public boolean deleteById(int id) {
        int affectedRows = 0;
        try {
            PreparedStatement s = c.prepareStatement(
                    "DELETE restaurants WHERE numero = ?");
            s.setInt(1, id);
            affectedRows = s.executeUpdate();
            c.commit();
        } catch (SQLException e) {
            logger.error("SQLException: {}", e.getMessage());
        }
        return affectedRows > 0;
    }

    protected String getSequenceQuery(){
        return "SELECT seq_restaurants.NextVal FROM dual";
    }
    protected String getExistsQuery() {
        return "SELECT numero FROM restaurants WHERE numero = ?";
    }
    protected String getCountQuery() {
        return "SELECT Count(*) FROM restaurants";
    }

}
