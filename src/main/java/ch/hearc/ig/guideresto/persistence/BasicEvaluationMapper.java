package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.BasicEvaluation;
import ch.hearc.ig.guideresto.business.Evaluation;
import ch.hearc.ig.guideresto.business.Restaurant;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class BasicEvaluationMapper extends AbstractMapper<BasicEvaluation> {
    private Connection c = ConnectionUtils.getConnection();

    @Override
    public BasicEvaluation findById(int id) {
        BasicEvaluation basicEvaluation = null;
        try {
            PreparedStatement s = c.prepareStatement("SELECT * FROM likes WHERE numero = ?");
            s.setInt(1, id);
            ResultSet rs = s.executeQuery();

            Boolean appreciation;
            if ("T" == rs.getString("appreciation")) {
                appreciation=true;
            } else {
                appreciation=false;
            }

            if (rs.next()) {
                Restaurant restaurant = new RestaurantMapper().findById(rs.getInt("fk_rest"));
                basicEvaluation = new BasicEvaluation(
                        rs.getInt("numero"),
                        rs.getDate("date_eval"),
                        restaurant,
                        appreciation,
                        rs.getString("adresse_ip")
                );
            } else {
                logger.error("No basic evaluation found");
            }
            rs.close();
        } catch (SQLException e) {
            logger.error("SQLException:{}", e.getMessage());
        }
        return basicEvaluation;
    }
    public Set<Evaluation> findForRestaurant(Restaurant resto) {
        Set<Evaluation> basicEvaluations = new HashSet<>();
        try {
            PreparedStatement s = c.prepareStatement("SELECT * FROM likes WHERE fk_rest = ?");
            s.setInt(1, resto.getId());
            ResultSet rs = s.executeQuery();

            while (rs.next()) {
                Boolean appreciation;
                if ("T" == rs.getString("appreciation")) {
                    appreciation=true;
                } else {
                    appreciation=false;
                }
                basicEvaluations.add(new BasicEvaluation(
                        rs.getInt("numero"),
                        rs.getDate("date_eval"),
                        resto,
                        appreciation,
                        rs.getString("adresse_ip")
                ));
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
        try {
            PreparedStatement s = c.prepareStatement("SELECT * FROM likes");
            ResultSet rs = s.executeQuery();
            while (rs.next()) {
                //beep boop identity map ???
                Restaurant restaurant = new RestaurantMapper().findById(rs.getInt("fk_rest"));

                Boolean appreciation;
                if ("T" == rs.getString("appreciation")) {
                	appreciation=true;
                } else {
                	appreciation=false;
                }

                basicEvaluations.add(new BasicEvaluation(
                        rs.getInt("numero"),
                        rs.getDate("date_eval"),
                        restaurant,
                        appreciation, //🥰🥰🥰🥰🥰🥰 ça marchera !
                        rs.getString("adresse_ip")
                ));
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
            PreparedStatement s = c.prepareStatement(
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
                int id = rs.getInt(1);
                basicEvaluation.setId(rs.getInt(1));
            } else {
                logger.warn("Failed to insert basic evaluation into the table : ");
            }
            rs.close();
            c.commit(); //ne pas oublier les commits sinon... ça s'efface!!
        } catch (SQLException e) {
            logger.error("SQLException: {}", e.getMessage());
        }
        return basicEvaluation;
    }

    @Override
    public boolean update(BasicEvaluation basicEvaluation) {
        int affectedRows = 0;
        try {
            PreparedStatement s = c.prepareStatement(
                    "UPDATE likes" +
                            " SET date_eval = ?, fk_rest = ?, appreciation = ?, adresse_ip = ?" +
                            " WHERE numero = ?");
            s.setDate(1, new java.sql.Date(basicEvaluation.getVisitDate().getTime()));
            s.setInt(2, basicEvaluation.getRestaurant().getId());

            s.setString(3, basicEvaluation.getLikeRestaurant() ? "T" : "F"); //ew ew ew ew j'aime pas quand c'est écrit comme ça 🤮🤮🤮🤮
            s.setString(4, basicEvaluation.getIpAddress());
            s.setInt(5, basicEvaluation.getId());
            affectedRows = s.executeUpdate();
            c.commit();
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
            PreparedStatement s = c.prepareStatement(
                    "DELETE FROM likes WHERE numero = ?");
            s.setInt(1, id);
            affectedRows = s.executeUpdate();
            c.commit();
        } catch (SQLException e) {
            logger.error("SQLException: {}", e.getMessage());
        }
        return affectedRows > 0;
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
