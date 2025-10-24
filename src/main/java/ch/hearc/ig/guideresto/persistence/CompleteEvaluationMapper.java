package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.CompleteEvaluation;
import ch.hearc.ig.guideresto.business.Evaluation;
import ch.hearc.ig.guideresto.business.Restaurant;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class CompleteEvaluationMapper extends AbstractMapper<CompleteEvaluation> {
    private final Connection connection;

    public CompleteEvaluationMapper(Connection connection) {
        this.connection = connection;
    }

    public CompleteEvaluation findById(int id) {
        CompleteEvaluation completeEvaluation = null;
        try {
            PreparedStatement s = connection.prepareStatement(
                    "SELECT c.numero num_comm, c.date_eval, c.commentaire, c.nom_utilisateur" +
                            " r.numero num_resto, r.nom, r.description desc_resto, r.site_web," +
                            " r.adresse, v.numero num_ville, v.nom_ville, v.code_postal," +
                            " t.numero num_type, t.libelle, t.description desc_type" +
                            " FROM commentaires c" +
                            " INNER JOIN restaurants r ON c.fk_rest = r.numero" +
                            " INNER JOIN villes v ON r.fk_ville = v.numero" +
                            " INNER JOIN types_gastronomiques t ON r.fk_type = t.numero" +
                            " WHERE c.numero = ?");
            s.setInt(1, id);
            ResultSet rs = s.executeQuery();
            if(rs.next()) {
                Restaurant restaurant = super.loadRestaurant(rs);
                completeEvaluation = new CompleteEvaluation(
                        rs.getInt("num_comm"),
                        rs.getDate("date_eval"),
                        restaurant,
                        rs.getString("commentaire"),
                        rs.getString("nom_utilisateur")
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
    public Set<Evaluation> findForRestaurant(Restaurant resto) {
        Set<Evaluation> completeEvaluations = new HashSet<>();
        try {
            PreparedStatement s = connection.prepareStatement("SELECT * FROM commentaires WHERE fk_rest = ?");
            s.setInt(1, resto.getId());
            ResultSet rs = s.executeQuery();
            while(rs.next()) {
                completeEvaluations.add(new CompleteEvaluation(
                        rs.getInt("numero"),
                        rs.getDate("date_eval"),
                        resto,
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
    public Set<CompleteEvaluation> findAll() {
        Set<CompleteEvaluation> completeEvaluations = new HashSet<>();
        try {
            PreparedStatement s = connection.prepareStatement(
                    "SELECT c.numero num_comm, c.date_eval, c.commentaire, c.nom_utilisateur" +
                            " r.numero num_resto, r.nom, r.description desc_resto, r.site_web," +
                            " r.adresse, v.numero num_ville, v.nom_ville, v.code_postal," +
                            " t.numero num_type, t.libelle, t.description desc_type" +
                            " FROM commentaires c" +
                            " INNER JOIN restaurants r ON c.fk_rest = r.numero" +
                            " INNER JOIN villes v ON r.fk_ville = v.numero" +
                            " INNER JOIN types_gastronomiques t ON r.fk_type = t.numero" +
                            " WHERE c.numero = ?");
            ResultSet rs = s.executeQuery();
            while(rs.next()) {
                Restaurant restaurant = super.loadRestaurant(rs);
                completeEvaluations.add(new CompleteEvaluation(
                        rs.getInt("num_comm"),
                        rs.getDate("date_eval"),
                        restaurant,
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
            PreparedStatement s = connection.prepareStatement(
                    "INSERT INTO commentaires (date_eval, commentaire, nom_utilisateur, fk_rest)" +
                            "VALUES (?, ?, ?, ?)",
                    generatedColumns);
            s.setDate(1, new Date(completeEvaluation.getVisitDate().getTime()));
            s.setString(2, completeEvaluation.getComment());
            s.setString(3, completeEvaluation.getUsername());
            s.setInt(4, completeEvaluation.getRestaurant().getId());
            s.executeUpdate();
            ResultSet rs = s.getGeneratedKeys();
            if (rs.next()) {
                completeEvaluation.setId(rs.getInt(1));
            } else {
                logger.warn("Failed to insert comment into the table: ", completeEvaluation.getVisitDate() + ". Continuing..." );
            }
            rs.close();
            connection.commit();
        } catch (SQLException e) {
            logger.error("SQLException: {}", e.getMessage());
        }
        return completeEvaluation;
    }
    public boolean update(CompleteEvaluation completeEvaluation) {
        int affectedRows = 0;
        try {
            PreparedStatement s = connection.prepareStatement(
                    "UPDATE commentaires"+
                            "SET date_eval = ?, commentaire = ?, nom_utilisateur = ?, fk_rest = ? "
                    +"WHERE numero = ?");
            s.setDate(1, new Date(completeEvaluation.getVisitDate().getTime()));
            s.setString(2, completeEvaluation.getComment());
            s.setString(3, completeEvaluation.getUsername());
            s.setInt(4, completeEvaluation.getRestaurant().getId());
            s.setInt(5, completeEvaluation.getId());

            affectedRows = s.executeUpdate();
            connection.commit();
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
            PreparedStatement s = connection.prepareStatement(
                    "DELETE commentaires WHERE numero = ?");
            s.setInt(1, id);
            affectedRows = s.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            logger.error("SQLException: {}", e.getMessage());
        }
        return affectedRows > 0;
    }

    protected String getSequenceQuery(){
        return "SELECT seq_commentaires.NextVal FROM dual";
    }
    protected String getExistsQuery() {
        return "SELECT numero FROM commentaires WHERE numero = ?";
    }
    protected String getCountQuery() {
        return "SELECT Count(*) FROM commentaires";
    }

}
