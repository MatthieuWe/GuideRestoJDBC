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
                /* On passe par la base AVANT le cache pour plusieurs raisons:
                / 1) Il y a une clause where et donc on veut être sûr d'avoir une liste fraîche.
                / 2) Ce genre de filtre serait relativement lourd algorithmiquement. On devrait accéder à chaque resto,
                /  sa ville, et la comparer avec celle qui est fournie avant de décider quoi faire -> SQL est bien plus adapté
                / 3) Cette méthode est utilisée pour lazy loader les restos d'une ville. Le principe même du lazy loader
                /  est de retarder l'accès à la base; ça n'a pas de sens de lazy loader depuis un cache,
                /  autant tout eager loader tout de suite.
                 */
                int id = rs.getInt("num_resto");
                if (super.cache.containsKey(id)) {
                    restos.add((Restaurant) super.cache.get(id));
                } else {
                    Restaurant resto = this.loadRestaurant(rs, city);
                    restos.add(resto);
                    super.addToCache(resto);
                }
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
                int id = rs.getInt("num_resto");
                if (super.cache.containsKey(id)) {
                    restos.add((Restaurant) super.cache.get(id));
                } else {
                    Restaurant resto = this.loadRestaurant(rs, type);
                    restos.add(resto);
                    super.addToCache(resto);
                }
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
    public Set<Restaurant> findByName(String partialName) {
        Set<Restaurant> restos = new HashSet<>();
        try {
            PreparedStatement s = connection.prepareStatement(
                    "SELECT r.numero num_resto, r.nom, r.description desc_resto, r.site_web," +
                            " r.adresse, v.numero num_ville, v.nom_ville, v.code_postal," +
                            " t.numero num_type, t.libelle, t.description desc_type" +
                            " FROM restaurants r" +
                            " INNER JOIN villes v ON r.fk_ville = v.numero" +
                            " INNER JOIN types_gastronomiques t ON r.fk_type = t.numero" +
                            " WHERE Upper(nom) LIKE Upper(?)");
            s.setString(1, "%" + partialName + "%");
            ResultSet rs = s.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("numero");
                if (super.cache.containsKey(id)) {
                    restos.add((Restaurant) super.cache.get(id));
                } else {
                    Restaurant restaurant = this.loadRestaurant(rs);
                    restos.add(restaurant);
                    super.addToCache(restaurant);
                }
            }
            rs.close();
        } catch (SQLException e) {
            logger.error("SQLException: {}", e.getMessage());
        }
        return restos;
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
