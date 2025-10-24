package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class AbstractMapper<T extends IBusinessObject> {

    protected static final Logger logger = LogManager.getLogger();
    protected Map<Integer, IBusinessObject> cache = new HashMap<>();

    public abstract T findById(int id);
    public abstract Set<T> findAll();
    public abstract T create(T object);
    public abstract boolean update(T object);
    public abstract boolean delete(T object);
    public abstract boolean deleteById(int id);

    protected abstract String getSequenceQuery();
    protected abstract String getExistsQuery();
    protected abstract String getCountQuery();



    /**
     * Vérifie si un objet avec l'ID donné existe.
     * @param id the ID to check
     * @return true si l'objet existe, false sinon
     */
    public boolean exists(int id) {
        Connection connection = ConnectionUtils.getConnection();

        try (PreparedStatement stmt = connection.prepareStatement(getExistsQuery())) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            logger.error("SQLException: {}", ex.getMessage());
        }
        return false;
    }

    /**
     * Compte le nombre d'objets en base de données.
     * @return
     */
    public int count() {
        Connection connection = ConnectionUtils.getConnection();

        try (PreparedStatement stmt = connection.prepareStatement(getCountQuery());
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException ex) {
            logger.error("SQLException: {}", ex.getMessage());
            return 0;
        }
    }

    /**
     * Obtient la valeur de la séquence actuelle en base de données
     * @return Le nombre de villes
     * @En cas d'erreur SQL
     */
    protected Integer getSequenceValue() {
        Connection connection = ConnectionUtils.getConnection();

        try (PreparedStatement stmt = connection.prepareStatement(getSequenceQuery());
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException ex) {
            logger.error("SQLException: {}", ex.getMessage());
            return 0;
        }
    }

    /**
     * Vérifie si le cache est actuellement vide
     * @return true si le cache ne contient aucun objet, false sinon
     */
    protected boolean isCacheEmpty() {
        return this.cache.isEmpty();
    }

    /**
     * Vide le cache
     */
    protected void resetCache() {
        this.cache.clear();
    }

    /**
     * Ajoute un objet au cache
     * @param objet l'objet à ajouter
     */
    protected void addToCache(T objet) {
        int id = objet.getId();
        if (!this.cache.containsKey(id)) {
            this.cache.put(id, objet);
        }
        /* Selon le cours:
        / Notez que la présence en cache est vérifiée 2 fois (findById et addToCache)
        / – On s’assure ainsi de ne pas créer de doublon en mémoire; mieux vaut prévenir que guérir
        / -> non je suis pas d'accord. Une map ne peut pas avoir de doublon de clé K, un put remplace la V existante
        / -> ceci dit j'ai beaucoup de respect pour M. Matile alors on fait quand même comme il dit et on parlera plus tard
         */
    }

    /**
     * Retire un objet du cache
     * @param id l'ID de l'objet à retirer du cache
     */
    protected void removeFromCache(int id) {
            this.cache.remove(id);
    }


    // Méthodes pour charger un restaurant sont utilisées par plusieurs mappers: eager load depuis les évaluations
    /*
    / Pour chaque resto qu'on charge en mémoire, on crée un nouvel objet en mémoire pour chaque ville et type
    / Chaque resto aura une ville (Neuchâtel) qui est égale aux autres au sens de equals() mais pas au sens de ==
    /    -> ce sont d'autres objets, elle est chargée plein de fois, on ne peut pas partir d'un de ces objets
    / pour retrouver tous les restos sans les charger à double depuis la DB...
    TODO il nous faut un moyen de tracker les objets en mémoire pour assurer leur unicité -> une identity map.
    */
    protected Restaurant loadRestaurant(ResultSet rs) throws SQLException {
        City city = new City(rs.getInt("num_ville"),
                rs.getString("code_postal"),
                rs.getString("nom_ville"));
        return this.loadRestaurant(rs, city);
    }

    protected Restaurant loadRestaurant(ResultSet rs, City city) throws SQLException {
        RestaurantType type = new RestaurantType(rs.getInt("num_type"),
                rs.getString("libelle"),
                rs.getString("desc_type"));
        return this.loadRestaurant(rs, city, type);
    }
    protected Restaurant loadRestaurant(ResultSet rs, RestaurantType type) throws SQLException {
        City city = new City(rs.getInt("num_ville"),
                rs.getString("code_postal"),
                rs.getString("nom_ville"));
        return this.loadRestaurant(rs, city, type);
    }
    protected Restaurant loadRestaurant(ResultSet rs, City city, RestaurantType type) throws SQLException {
        Localisation address = new Localisation(rs.getString("adresse"), city);
        Restaurant resto = new Restaurant(
                rs.getInt("num_resto"),
                rs.getString("nom"),
                rs.getString("desc_resto"),
                rs.getString("site_web"),
                address,
                type);
        return resto;
    }

}
