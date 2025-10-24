
package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.*;
import oracle.jdbc.proxy.annotation.Pre;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class GradeMapper extends AbstractMapper<Grade> {
    private final Connection connection;

    public GradeMapper(Connection connection) {
        this.connection = connection;
    }

    private Grade loadGrade(ResultSet rs) throws SQLException {
        CompleteEvaluation eval = new CompleteEvaluation(
                rs.getInt("num_comm"),
                rs.getDate("date_eval"),
                super.loadRestaurant(rs),
                rs.getString("commentaire"),
                rs.getString("nom_utilisateur")
        );
        return this.loadGrade(rs, eval);
    }
    private Grade loadGrade(ResultSet rs, CompleteEvaluation eval) throws SQLException {
        EvaluationCriteria crit = new EvaluationCriteria(
                rs.getInt("num_ce"),
                rs.getString("nom_ce"),
                rs.getString("desc_ce")
        );
        return new Grade(
                rs.getInt("num_note"),
                rs.getInt("note"),
                eval,
                crit
        );
    }

    @Override
    public Grade findById(int id) {
        Grade grade = null;
        try {
            // AAAAHHHAHHAA elle est énorme! Et en plus ça sert a rien on appelera jamais cette méthode...
            PreparedStatement s = connection.prepareStatement("SELECT n.numero num_note, n.note, n.fk_comm, n.fk_crit,"+
                    " co.numero num_comm, co.date_eval, co.commentaire, co.nom_utilisateur," +
                    " ce.numero num_ce, ce.nom nom_ce, ce.description desc_ce," +
                    " r.numero num_resto, r.nom, r.description desc_resto, r.site_web," +
                    " r.adresse, v.numero num_ville, v.nom_ville, v.code_postal," +
                    " t.numero num_type, t.libelle, t.description desc_type" +
                    " FROM notes n" +
                    " INNER JOIN commentaires co ON n.fk_comm = co.numero" +
                    " INNER JOIN criteres_evaluation ce ON n.fk_crit = ce.numero"+
                    " INNER JOIN restaurants r ON co.fk_rest = r.numero" +
                    " INNER JOIN villes v ON r.fk_ville = v.numero" +
                    " INNER JOIN types_gastronomiques t ON r.fk_type = t.numero" +
                    " WHERE n.numero = ?");
            s.setInt(1, id);
            ResultSet rs = s.executeQuery();

            if(rs.next()) {
                grade = this.loadGrade(rs);
            } else {
                logger.error("No grade found");
            }
            rs.close();
        } catch (SQLException e) {
            logger.error("SQLException: " + e.getMessage());
        }
        return grade;
    }

    public Set<Grade> findAll() {
        Set<Grade> grades = new HashSet<>();
        try {
            PreparedStatement s = connection.prepareStatement("SELECT n.numero num_note, n.note, n.fk_comm, n.fk_crit,"+
                    " co.numero num_comm, co.date_eval, co.commentaire, co.nom_utilisateur," +
                    " ce.numero num_ce, ce.nom nom_ce, ce.description desc_ce," +
                    " r.numero num_resto, r.nom, r.description desc_resto, r.site_web," +
                    " r.adresse, v.numero num_ville, v.nom_ville, v.code_postal," +
                    " t.numero num_type, t.libelle, t.description desc_type" +
                    " FROM notes n" +
                    " INNER JOIN commentaires co ON n.fk_comm = co.numero" +
                    " INNER JOIN criteres_evaluation ce ON n.fk_crit = ce.numero"+
                    " INNER JOIN restaurants r ON co.fk_rest = r.numero" +
                    " INNER JOIN villes v ON r.fk_ville = v.numero" +
                    " INNER JOIN types_gastronomiques t ON r.fk_type = t.numero");
            ResultSet rs = s.executeQuery();
            while (rs.next()) {
                grades.add(this.loadGrade(rs));
            }
            rs.close();
        } catch (SQLException e) {
            logger.error("SQLException: " + e.getMessage());
        }
        return grades;
    }

    public Grade create(Grade grade) {
        try {
            String generatedColumns[] = {"numero"};
            PreparedStatement s = connection.prepareStatement(
                    "INSERT INTO notes(note,fk_comm, fk_crit)" +
                            "VALUES (?, ?, ?)",
                    generatedColumns);
            s.setInt(1, grade.getGrade());
            s.setInt(2, grade.getEvaluation().getId());
            s.setInt(3, grade.getCriteria().getId());
            s.executeUpdate();
            ResultSet rs = s.getGeneratedKeys();
            if (rs.next()) {
                grade.setId(rs.getInt(1));
            } else{
                logger.warn("Failed to insert grade into the table", grade.getGrade() + ". Continuing...");
            }
            rs.close();
            connection.commit();
        } catch (SQLException e) {
            logger.error("SQLException: " + e.getMessage());
        }
        return grade;
    }

    public boolean update(Grade grade) {
        int affectedRows = 0;
        try {
            PreparedStatement s = connection.prepareStatement(
                    "UPDATE notes"+
                        " SET note = ?, fk_comm = ?, fk_crit = ?"+
                        " WHERE numero = ?");
            s.setInt(1, grade.getGrade());
            s.setInt(2, grade.getEvaluation().getId());
            s.setInt(3, grade.getCriteria().getId());
            affectedRows = s.executeUpdate();
            connection.commit();
        }catch (SQLException e) {
            logger.error("SQLException: " + e.getMessage());
        }
        return affectedRows > 0;
    }

    public boolean delete(Grade grade) { return this.deleteById(grade.getId()); }

    public boolean deleteById(int id) {
        int affectedRows = 0;
        try{
            PreparedStatement s = connection.prepareStatement(
                    "DELETE notes WHERE numero = ? ");
            s.setInt(1, id);
            affectedRows = s.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            logger.error("SQLException: " + e.getMessage());
        }
        return affectedRows > 0;
    }

    protected String getSequenceQuery() {return "SELECT seq_notes.NextVal FROM dual";}
    protected String getExistsQuery() {return "SELECT numero FROM notes WHERE numero = ? ";}
    protected String getCountQuery() {return "SELECT count(*) FROM notes";}

    public Set<Grade> findForCompleteEvaluation(CompleteEvaluation evaluation) {
        Set<Grade> grades = new HashSet<>();
        try {
            PreparedStatement s = connection.prepareStatement("SELECT n.numero as numeroNote, n.note, n.fk_comm, n.fk_crit,"+
                    " ce.numero as NumeroCE, ce.nom as nomCe, ce.description as descriptionCe," +
                    " co.numero as numCom, co.nom as nomCom, co.description as descriptionCom" +
                    " FROM notes n" +
                    " INNER JOIN commentaires co ON n.fk_comm = co.numero" +
                    " INNER JOIN criteres_evaluation ce ON n.fk_crit = ce.numero"+
                    " WHERE numCom = ?");
            s.setInt(1, evaluation.getId());
            ResultSet rs = s.executeQuery();
            while (rs.next()) {
                grades.add(this.loadGrade(rs, evaluation));
            }
            rs.close();
        } catch (SQLException e) {
            logger.error("SQLException: " + e.getMessage());
        }
        return grades;
    }
}