package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.BasicEvaluation;
import ch.hearc.ig.guideresto.business.City;
import ch.hearc.ig.guideresto.business.Evaluation;
import ch.hearc.ig.guideresto.business.Restaurant;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class BasicEvaluationMapper extends AbstractMapper<BasicEvaluation> {
    private final Connection connection ;

    public BasicEvaluationMapper(Connection connection) {
        this.connection = connection;
    }

    @Override
    public BasicEvaluation findById(int id) {
        BasicEvaluation basicEvaluation = null;
        if (super.cache.containsKey(id)) {
            basicEvaluation = (BasicEvaluation) super.cache.get(id);
        } else {
            try {
                PreparedStatement s = connection.prepareStatement(
                    "SELECT l.numero num_like, l.appreciation, l.date_eval, l.adresse_ip" +
                            " r.numero num_resto, r.nom, r.description desc_resto, r.site_web," +
                            " r.adresse, v.numero num_ville, v.nom_ville, v.code_postal," +
                            " t.numero num_type, t.libelle, t.description desc_type" +
                            " FROM likes l" +
                            " INNER JOIN restaurants r ON l.fk_rest = r.numero" +
                            " INNER JOIN villes v ON r.fk_ville = v.numero" +
                            " INNER JOIN types_gastronomiques t ON r.fk_type = t.numero" +
                            " WHERE l.numero = ?");
                s.setInt(1, id);
                ResultSet rs = s.executeQuery();

                if (rs.next()) {
                    Boolean appreciation;
                    if ("T" == rs.getString("appreciation")) {
                        appreciation=true;
                    } else {
                        appreciation=false;
                    }
                    Restaurant restaurant = super.loadRestaurant(rs);
                    basicEvaluation = new BasicEvaluation(
                        rs.getInt("numero"),
                        rs.getDate("date_eval"),
                        restaurant,
                        appreciation,
                        rs.getString("adresse_ip")
                    );
                    super.addToCache(basicEvaluation);
                } else {
                    logger.error("No basic evaluation found");
                }
                rs.close();
            } catch (SQLException e) {
                logger.error("SQLException:{}", e.getMessage());
            }
        }
        return basicEvaluation;
    }
    public Set<Evaluation> findForRestaurant(Restaurant resto) {
        Set<Evaluation> basicEvaluations = new HashSet<>();
        try {
            PreparedStatement s = connection.prepareStatement("SELECT * FROM likes WHERE fk_rest = ?");
            s.setInt(1, resto.getId());
            ResultSet rs = s.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("numero");
                if (super.cache.containsKey(id)) {
                    basicEvaluations.add((BasicEvaluation) super.cache.get(id));
                } else {
                    Boolean appreciation;
                    if ("T" == rs.getString("appreciation")) {
                        appreciation=true;
                    } else {
                        appreciation=false;
                    }
                    BasicEvaluation eval = new BasicEvaluation(
                            rs.getInt("numero"),
                            rs.getDate("date_eval"),
                            resto,
                            appreciation,
                            rs.getString("adresse_ip")
                    );
                    basicEvaluations.add(eval);
                    super.addToCache(eval);
                }
            }
            rs.close();
        } catch (SQLException e) {
            logger.error("SQLException:{}", e.getMessage());
        }
        return basicEvaluations;
    }

    @Override
    public Set<BasicEvaluation> findAll() {
        Set<BasicEvaluation> basicEvaluations = new HashSet<>();
        super.resetCache();
        try {
            PreparedStatement s = connection.prepareStatement(
                    "SELECT l.numero num_like, l.appreciation, l.date_eval, l.adresse_ip" +
                    " r.numero num_resto, r.nom, r.description desc_resto, r.site_web," +
                    " r.adresse, v.numero num_ville, v.nom_ville, v.code_postal," +
                    " t.numero num_type, t.libelle, t.description desc_type" +
                    " FROM likes l" +
                    " INNER JOIN restaurants r ON l.fk_rest = r.numero" +
                    " INNER JOIN villes v ON r.fk_ville = v.numero" +
                    " INNER JOIN types_gastronomiques t ON r.fk_type = t.numero"
            );
            ResultSet rs = s.executeQuery();
            while (rs.next()) {
                Boolean appreciation;
                if ("T" == rs.getString("appreciation")) {
                	appreciation=true;
                } else {
                	appreciation=false;
                }
                Restaurant restaurant = super.loadRestaurant(rs);
                BasicEvaluation basicEvaluation = new BasicEvaluation(
                        rs.getInt("num_like"),
                        rs.getDate("date_eval"),
                        restaurant,
                        appreciation, //🥰🥰🥰🥰🥰🥰 ça marchera !
                        rs.getString("adresse_ip")
                );
                basicEvaluations.add(basicEvaluation);
                super.addToCache(basicEvaluation);
            }
            rs.close();
        } catch (SQLException e) {
            logger.error("SQLException: {}", e.getMessage());
        }
        return basicEvaluations;
    }

    @Override
    public BasicEvaluation create(BasicEvaluation basicEvaluation) {
        try {
            String generatedColumns[] = {"numero"};
            PreparedStatement s = connection.prepareStatement(
                    "INSERT INTO likes (date_eval, fk_rest, appreciation, adresse_ip)" +
                            "VALUES (?, ?, ?, ?)",
                    generatedColumns);
            s.setDate(1, new java.sql.Date(basicEvaluation.getVisitDate().getTime()));
            s.setInt(2, basicEvaluation.getRestaurant().getId());
            s.setString(3, basicEvaluation.getLikeRestaurant() ? "T" : "F"); //ewewewewe je veux pas
            s.setString(4, basicEvaluation.getIpAddress());
            s.executeUpdate();
            ResultSet rs = s.getGeneratedKeys();
            if (rs.next()) {
                basicEvaluation.setId(rs.getInt(1));
                super.addToCache(basicEvaluation);
            } else {
                logger.warn("Failed to insert basic evaluation into the table : ");
            }
            rs.close();
            connection.commit(); //ne pas oublier les commits sinon... ça s'efface!!
        } catch (SQLException e) {
            logger.error("SQLException: {}", e.getMessage());
        }
        return basicEvaluation;
    }

    @Override
    public boolean update(BasicEvaluation basicEvaluation) {
        int affectedRows = 0;
        try {
            PreparedStatement s = connection.prepareStatement(
                    "UPDATE likes" +
                            " SET date_eval = ?, fk_rest = ?, appreciation = ?, adresse_ip = ?" +
                            " WHERE numero = ?");
            s.setDate(1, new java.sql.Date(basicEvaluation.getVisitDate().getTime()));
            s.setInt(2, basicEvaluation.getRestaurant().getId());

            s.setString(3, basicEvaluation.getLikeRestaurant() ? "T" : "F"); //ew ew ew ew j'aime pas quand connection'est écrit comme ça 🤮🤮🤮🤮
            s.setString(4, basicEvaluation.getIpAddress());
            s.setInt(5, basicEvaluation.getId());
            affectedRows = s.executeUpdate();
            connection.commit();
            super.addToCache(basicEvaluation);
        } catch (SQLException e) {
            logger.error("SQLException: {}", e.getMessage());
        }
        return affectedRows > 0;
    }

    @Override
    public boolean delete(BasicEvaluation basicEvaluation) {
        return this.deleteById(basicEvaluation.getId());
    }

    @Override
    public boolean deleteById(int id) {
        int affectedRows = 0;
        try {
            PreparedStatement s = connection.prepareStatement(
                    "DELETE FROM likes WHERE numero = ?");
            s.setInt(1, id);
            affectedRows = s.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            logger.error("SQLException: {}", e.getMessage());
        }
        if(affectedRows > 0) {
            super.removeFromCache(id);
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected String getSequenceQuery() {
        return "SELECT seq_likes.NextVal FROM dual";
    }

    @Override
    protected String getExistsQuery() {
        return "SELECT numero FROM likes WHERE numero = ?";
    }

    @Override
    protected String getCountQuery() {
        return "SELECT Count(*) FROM likes";
    }
}
