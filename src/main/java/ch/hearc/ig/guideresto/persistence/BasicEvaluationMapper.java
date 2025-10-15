package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.BasicEvaluation;
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

            if (rs.next()) {
                Restaurant restaurant = new RestaurantMapper().findById(rs.getInt("fk_rest"));
                BasicEvaluation basicEvaluation1 = new BasicEvaluation(
                        rs.getInt("numero"),
                        rs.getDate("date_eval"),
                        restaurant,
                        rs.getBoolean("appreciation"), //je suis pas convaincue du boolean ??? je crois que ça va pas sortir juste..
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

    @Override
    public Set<BasicEvaluation> findAll() {
        Set<BasicEvaluation> basicEvaluations = new HashSet<>();
        try {
            PreparedStatement s = c.prepareStatement("SELECT * FROM likes");
            ResultSet rs = s.executeQuery();
            while (rs.next()) {
                //beep boop identity map ???
                Restaurant restaurant = new RestaurantMapper().findById(rs.getInt("fk_rest"));
                basicEvaluations.add(new BasicEvaluation(
                        rs.getInt("numero"),
                        rs.getDate("date_eval"),
                        restaurant,
                        rs.getBoolean("appreciation"), //je suis pas convaincue du boolean ??? je crois que ça va pas sortir juste..
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
            s.setBoolean(3, basicEvaluation.getLikeRestaurant());
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
            s.setBoolean(3, basicEvaluation.getLikeRestaurant());
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
        //problème... id est putain de privé c'est dans la classe abstraite...
        //return this.delete(basicEvaluation.getId());
        return null;
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
