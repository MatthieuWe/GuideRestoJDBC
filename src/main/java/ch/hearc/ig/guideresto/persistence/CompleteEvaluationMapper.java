package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.CompleteEvaluation;
import ch.hearc.ig.guideresto.business.Restaurant;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class CompleteEvaluationMapper extends AbstractMapper<CompleteEvaluation> {
    private Connection c = ConnectionUtils.getConnection();

    public CompleteEvaluation findById(int id) {
        CompleteEvaluation completeEvaluation = null;
        try {
            PreparedStatement s = c.prepareStatement("SELECT * FROM commentaires WHERE numero = ?");
            s.setInt(1, id);
            ResultSet rs = s.executeQuery();
            if(rs.next()) {
                completeEvaluation = new CompleteEvaluation(
                        //est-ce que j'ai besoin du numero ?? ou bien pas ??
                        rs.getInt("numero"),
                        rs.getDate("date_eval"),
                        (Restaurant) rs.getObject("restaurant"), //à tester mdrrr
                        rs.getString("commentaire"),
                        rs.getString("utilisateur")
                );
            } else {
                logger.error("No such city");
            }
            rs.close();
        } catch (SQLException e) {
            logger.error("SQLException: {}", e.getMessage());
        }
        return completeEvaluation;
    }

    public Set<CompleteEvaluation> findAll() {
        Set<CompleteEvaluation> completeEvaluations = new HashSet<>();
        try {
            PreparedStatement s = c.prepareStatement("SELECT * FROM commentaires");
            ResultSet rs = s.executeQuery();
            while(rs.next()) {
                completeEvaluations.add(new CompleteEvaluation(
                        rs.getDate("date_eval"),
                        (Restaurant) rs.getObject("restaurant"), //à tester mdrrr
                        rs.getString("commentaire"),
                        rs.getString("nom_utilisateur")
                ));
            }
            rs.close();
        } catch (SQLException e) {
            logger.error("SQLException: {}", e.getMessage());
        }
        return completeEvaluations;
    }
    public CompleteEvaluation create(CompleteEvaluation completeEvaluation) {
        try {
            String generatedColumns[] = { "numero" };
            PreparedStatement s = c.prepareStatement(
                    "INSERT INTO commentaires (numero, date_eval, commentaire, nom_utilisateur)" +
                            "VALUES (?, ?, ?, ?)",
                    generatedColumns);
            s.setInt(1, completeEvaluation.getId());
            s.setDate(2, new java.sql.Date(completeEvaluation.getVisitDate().getTime()));
            s.setString(3, completeEvaluation.getComment());
            s.setString(4, completeEvaluation.getUsername());
            s.executeUpdate();
            ResultSet rs = s.getGeneratedKeys();
            if (rs.next()) {
                completeEvaluation.setId(rs.getInt(1));
            } else {
                logger.warn("Failed to insert comment into the table: ", completeEvaluation.getVisitDate() + ". Continuing..." );
            }
            rs.close();
            c.commit();
        } catch (SQLException e) {
            logger.error("SQLException: {}", e.getMessage());
        }
        return completeEvaluation;
    }
    public boolean update(CompleteEvaluation completeEvaluation) {
        int affectedRows = 0;
        try {
            PreparedStatement s = c.prepareStatement(
                    "UPDATE commentaires"+
                            "SET date_eval = ?, commentaire = ?, nom_utilisateur = ?, fk_rest = ? "
                    +"WHERE numero = ?");
            s.setDate(2, new java.sql.Date(completeEvaluation.getVisitDate().getTime()));
            s.setString(3, completeEvaluation.getComment());
            s.setString(4, completeEvaluation.getUsername());
            s.setInt(5, completeEvaluation.getRestaurant().getId());
            s.setInt(1, completeEvaluation.getId());

            affectedRows = s.executeUpdate();
            c.commit();
        } catch (SQLException e) {
            logger.error("SQLException: {}", e.getMessage());
        }
        return affectedRows > 0;
    }
    public boolean delete(CompleteEvaluation completeEvaluation) {
        return this.deleteById(completeEvaluation.getId());
    }
    public boolean deleteById(int id) {
        int affectedRows = 0;
        try {
            PreparedStatement s = c.prepareStatement(
                    "DELETE commentaires WHERE numero = ?");
            s.setInt(1, id);
            affectedRows = s.executeUpdate();
            c.commit();
        } catch (SQLException e) {
            logger.error("SQLException: {}", e.getMessage());
        }
        return affectedRows > 0;
    }

    protected String getSequenceQuery(){
        return "SELECT seq_villes.NextVal FROM dual";
    }
    protected String getExistsQuery() {
        return "SELECT numero FROM villes WHERE numero = ?";
    }
    protected String getCountQuery() {
        return "SELECT Count(*) FROM villes";
    }

}
