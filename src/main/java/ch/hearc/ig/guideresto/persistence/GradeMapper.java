
package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.*;
import oracle.jdbc.proxy.annotation.Pre;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

/*
  Notes:
  - This implementation uses plain JDBC. Provide a valid JDBC URL with
    System.setProperty("jdbc.url", "<your-jdbc-url>") before using, or
    replace getConnection() with your project's connection provider.
  - Assumes a table named 'grade' with columns: id (PK, auto-generated), name (VARCHAR), value (INT).
  - Adjust column names and Grade constructor/mutators to match your business class.
*/

public class GradeMapper extends AbstractMapper<Grade> {
    private final Connection connection;

    public GradeMapper(Connection connection) {
        this.connection = connection;
    }

    private Grade loadGrade(ResultSet rs, CompleteEvaluation eval, EvaluationCriteria crit) throws SQLException {
        return new Grade(
                rs.getInt("numero"),
                rs.getInt("note"),
                eval,
                crit
        );
    }
    private Grade loadGrade(ResultSet rs) throws SQLException {
        CompleteEvaluation eval = new CompleteEvaluation(rs.getInt("NumeroCE"),
                null, //voir comment mettre une date plus tard
                null, //restaurant to be implemented later
                rs.getString("nomCe"),
                rs.getString("descriptionCe"));


        EvaluationCriteria crit = new EvaluationCriteria(
                rs.getInt("numCom"),
                rs.getString("nomCom"),
                rs.getString("descriptionCom")
        );

        return new Grade(
                rs.getInt("numero"),
                rs.getInt("note"),
                eval,
                crit
        );
    }

    @Override
    public Grade findById(int id) {
        Grade grade = null;
        try {
            PreparedStatement s = connection.prepareStatement("SELECT n.NUMERO as numeroNote, n.NOTE, n.FK_COMM, n.FK_CRIT,"+
                    "ce.numero as NumeroCE, ce.nom as nomCe, ce.description as descriptionCe," +
                    "co.numero as numCom, co.nom as nomCom, co.description as descriptionCom " +
                    "FROM notes n" +
                    "INNER JOIN commentaires co ON n.FK_COMM = co.NUMERO" +
                    "INNER JOIN criteres_evaluation ce ON n.FK_CRIT = ce.NUMERO"+
                    "WHERE numeroNote = ?");
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
            PreparedStatement s = connection.prepareStatement("SELECT n.NUMERO as numeroNote, n.NOTE, n.FK_COMM, n.FK_CRIT,"+
                            "ce.numero as NumeroCE, ce.nom as nomCe, ce.description as descriptionCe," +
                            "co.numero as numCom, co.nom as nomCom, co.description as descriptionCom " +
                            "FROM notes n" +
                            "INNER JOIN commentaires co ON n.FK_COMM = co.NUMERO" +
                            "INNER JOIN criteres_evaluation ce ON n.FK_CRIT = ce.NUMERO");
            ResultSet rs = s.executeQuery();
            while (rs.next()) {
                //blablablabla identity map ????????????????????
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
                    "SET note = ?, fk_comm = ?, fk_crit = ?"+
                    "WHERE numero = ?");
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

    public Set<Grade> findForCompleteEvaluation(int id) {
        Set<Grade> grades = new HashSet<>();
        try {
            PreparedStatement s = connection.prepareStatement("SELECT n.NUMERO as numeroNote, n.NOTE, n.FK_COMM, n.FK_CRIT,"+
                    "ce.numero as NumeroCE, ce.nom as nomCe, ce.description as descriptionCe," +
                    "co.numero as numCom, co.nom as nomCom, co.description as descriptionCom " +
                    "FROM notes n" +
                    "INNER JOIN commentaires co ON n.FK_COMM = co.NUMERO" +
                    "INNER JOIN criteres_evaluation ce ON n.FK_CRIT = ce.NUMERO"+
                    "WHERE numCom = ?");
            s.setInt(1, id);
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

}