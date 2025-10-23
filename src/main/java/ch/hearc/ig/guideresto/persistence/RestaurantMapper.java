package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.City;
import ch.hearc.ig.guideresto.business.Localisation;
import ch.hearc.ig.guideresto.business.Restaurant;
import ch.hearc.ig.guideresto.business.RestaurantType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.sql.*;

public class RestaurantMapper extends AbstractMapper<Restaurant> {

    private final Connection connection;

    public RestaurantMapper(Connection connection) {
        this.connection = connection;
    }

    public Restaurant findById(int id) {
        if (super.cache.containsKey(id)) { //identity map ici 😬😬😬😬😬😬
            return (Restaurant) super.cache.get(id); // on caste parce qu'on peut. On a mis que des restos dedans.
        } else {
            Restaurant resto = null;
            try {
                PreparedStatement s = connection.prepareStatement("SELECT r.numero num_resto, r.nom, r.description desc_resto, r.site_web," +
                        " r.adresse, v.numero num_ville, v.nom_ville, v.code_postal," +
                        " t.numero num_type, t.libelle, t.description desc_type" +
                        " FROM restaurants r" +
                        " INNER JOIN villes v ON r.fk_ville = v.numero" +
                        " INNER JOIN types_gastronomiques t ON r.fk_type = t.numero" +
                        " WHERE num_resto = ?");
                s.setInt(1, id);
                ResultSet rs = s.executeQuery();

                if(rs.next()) {
                    resto = this.loadRestaurant(rs);
                    super.addToCache(resto);
                } else {
                    logger.error("No restaurant found with id " + id);
                }
                rs.close();
            } catch (SQLException e) {
                logger.error("SQLException: {}", e.getMessage());
            }
            return resto;
        }
    }

    public Set<Restaurant> findForCity(City city) {
        Set<Restaurant> restos = new HashSet<>();
        try {
            PreparedStatement s = connection.prepareStatement("SELECT r.numero num_resto, r.nom, r.description desc_resto, r.site_web," +
                    " r.adresse, t.numero num_type, t.libelle, t.description desc_type" +
                    " FROM restaurants r" +
                    " INNER JOIN types_gastronomiques t ON r.fk_type = t.numero" +
                    " WHERE r.fk_ville = ?");
            s.setInt(1, city.getId());
            ResultSet rs = s.executeQuery();
            while(rs.next()) {
                restos.add(this.loadRestaurant(rs, city));
                /* IMPORTANT ici on ne met pas les restos en cache. Cette méthode est utilisée pour lazy loader les restos
                / d'un objet City. Si on les met en cache, on va créer des références circulaires, et ça, c'est pas bien.
                 */
            }
            rs.close();
        } catch (SQLException e) {
            logger.error("SQLException: {}", e.getMessage());
        }
        return restos;
    }
    public Set<Restaurant> findForType(RestaurantType type) {
        Set<Restaurant> restos = new HashSet<>();
        try {
            PreparedStatement s = connection.prepareStatement("SELECT r.numero num_resto, r.nom, r.description desc_resto, r.site_web," +
                    " r.adresse, v.numero num_ville, v.nom_ville, v.code_postal" +
                    " FROM restaurants r" +
                    " INNER JOIN villes v ON r.fk_ville = v.numero" +
                    " WHERE r.fk_type = ?");
            s.setInt(1, type.getId());
            ResultSet rs = s.executeQuery();
            while(rs.next()) {
                restos.add(this.loadRestaurant(rs, type));
                // Idem, on ne met pas en cache
            }
            rs.close();
        } catch (SQLException e) {
            logger.error("SQLException: {}", e.getMessage());
            e.printStackTrace();
        }
        return restos;
    }
    public Set<Restaurant> findAll() {
        Set<Restaurant> restos = new HashSet<>();
        super.resetCache(); // bonne occasion pour rafraîchir le cache comme on va de toute manière tout charger
        try {
            PreparedStatement s = connection.prepareStatement("SELECT r.numero num_resto, r.nom, r.description desc_resto, r.site_web," +
                    " r.adresse, v.numero num_ville, v.nom_ville, v.code_postal," +
                    " t.numero num_type, t.libelle, t.description desc_type" +
                    " FROM restaurants r" +
                    " INNER JOIN villes v ON r.fk_ville = v.numero" +
                    " INNER JOIN types_gastronomiques t ON r.fk_type = t.numero");
            ResultSet rs = s.executeQuery();
            while(rs.next()) {
                Restaurant restaurant = this.loadRestaurant(rs);
                restos.add(restaurant);
                super.addToCache(restaurant);
            }
            rs.close();
        } catch (SQLException e) {
            logger.error("SQLException: {}", e.getMessage());
        }
        return restos;
    }

    /*
    Pour chaque resto qu'on charge en mémoire, on crée un nouvel objet en mémoire pour chaque ville et type
    Chaque resto aura une ville (Neuchâtel) qui est égale aux autres au sens de equals() mais pas au sens de ==
        -> ce sont d'autres objets, elle est chargée plein de fois, on ne peut pas partir d'un de ces objets
    pour retrouver tous les restos sans les charger à double depuis la DB...
    il nous faut un moyen de tracker les objets en mémoire pour assurer leur unicité -> une identity map.
    UPDATE 23.10.2025: On va accepter qu'il y ait certains doublons en mémoire pour éviter les références circulaires
    et permettre le eager loading (charger un resto avec une seule requete et des jointures sur ville et type)
    -> dans le cache des restaurants, depuis le RestaurantMapper, on garantit l'unicité des restos mais il y aura aussi
    des villes et des types liés à ces restaurants qui existent en double dans le cache de leur propre mapper ou dans d'autres restos
    -> dans le cache des villes ou des types, ce sera pareil donc on ne garantit l'unicité d'un type d'objet que
    dans son propre cache/mapper, pas dans toute l'appli.
    */
    // Eager Loading relation n..1
    private Restaurant loadRestaurant(ResultSet rs) throws SQLException {

        City city = new City(rs.getInt("num_ville"),
                rs.getString("code_postal"),
                rs.getString("nom_ville"));
        return this.loadRestaurant(rs, city);
    }

    private Restaurant loadRestaurant(ResultSet rs, City city) throws SQLException {
        Localisation address = new Localisation(rs.getString("adresse"), city);
        RestaurantType type = new RestaurantType(rs.getInt("num_type"),
                rs.getString("libelle"),
                rs.getString("desc_type"));
        return this.loadRestaurant(rs, address, type);
    }
    private Restaurant loadRestaurant(ResultSet rs, RestaurantType type) throws SQLException {
        City city = new City(rs.getInt("num_ville"),
                rs.getString("code_postal"),
                rs.getString("nom_ville"));
        Localisation address = new Localisation(rs.getString("adresse"), city);
        return this.loadRestaurant(rs, address, type);
    }
    private Restaurant loadRestaurant(ResultSet rs, Localisation address, RestaurantType type) throws SQLException {
        Restaurant resto = new Restaurant(
                rs.getInt("num_resto"),
                rs.getString("nom"),
                rs.getString("desc_resto"),
                rs.getString("site_web"),
                address,
                type);
        return resto;
    }
    public Restaurant create(Restaurant resto) {
        try {
            String generatedColumns[] = { "numero" };
            PreparedStatement s = connection.prepareStatement(
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
                super.addToCache(resto);
            } else {
                logger.warn("Failed to insert resto into the table: ", resto.getName() + ". Continuing..." );
            }
            rs.close();
            connection.commit();
        } catch (SQLException | RuntimeException e) {
            logger.error("SQLException or runtimeexcption: {}", e.getMessage());
        }
        return resto;
    }
    public boolean update(Restaurant resto) {
        int affectedRows = 0;
        try {
            PreparedStatement s = connection.prepareStatement(
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
            connection.commit();
        } catch (SQLException e) {
            logger.error("SQLException: {}", e.getMessage());
        }
        if (affectedRows > 0) {
            super.removeFromCache(resto.getId());
            super.addToCache(resto);
            return true;
        } else {
            return false;
        }
    }
    public boolean delete(Restaurant resto) {
        return this.deleteById(resto.getId());
    }
    public boolean deleteById(int id) {
        int affectedRows = 0;
        try {
            PreparedStatement s = connection.prepareStatement(
                    "DELETE likes WHERE fk_rest = ?");
            s.setInt(1, id);
            s.executeUpdate();

            s = connection.prepareStatement(
                    "DELETE notes WHERE fk_comm IN " +
                            "(SELECT numero FROM commentaires WHERE fk_rest = ?)");
            s.setInt(1, id);
            s.executeUpdate();

            s = connection.prepareStatement(
                    "DELETE commentaires WHERE fk_rest = ?");
            s.setInt(1, id);
            s.executeUpdate();

            s = connection.prepareStatement(
                    "DELETE restaurants WHERE numero = ?");
            s.setInt(1, id);
            affectedRows = s.executeUpdate();

            super.removeFromCache(id); // enleve le resto du cache (que le resto)
            /* on n'a pas accès aux caches des autres objets depuis ici, ils risquent d'y rester comme des fantômes un moment...
            / -> acceptable, les caches de ces objets-là sont quasi inutiles. Ils ne sont jamais accédés autrement que
            / depuis leur restaurant et meme si on voulait tous les lister, les méthodes findAll() font un reset du cache.
             */

            connection.commit();
        } catch (SQLException e) {
            logger.error("SQLException: {}", e.getMessage());
            e.printStackTrace();
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
