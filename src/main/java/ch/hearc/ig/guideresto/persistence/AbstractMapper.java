package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.City;
import ch.hearc.ig.guideresto.business.IBusinessObject;
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
}
