
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
    private Connection c = ConnectionUtils.getConnection();

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
                rs.getString("nomCe"),
                rs.getString("descriptionCe"));


        EvaluationCriteria crit = new EvaluationCriteriaMapper().findById(rs.getInt("fk_crit"));

        return new Grade(
                rs.getInt("numero"),
                rs.getInt("note")
        );
    }

    @Override
    public Grade findById(int id) {
        Grade grade = null;
        try {
            PreparedStatement s = c.prepareStatement("SELECT n.NUMERO as numeroNote, n.NOTE, n.FK_COMM, n.FK_CRIT,"+
                    "ce.numero as NumeroCE, ce.nom as nomCe, ce.description as descriptionCe," +
                    "co.numero as numCom, co.nom as nomCom, co.description as descriptionCom " +
                    "FROM notes n" +
                    "INNER JOIN commentaires co ON n.FK_COMM = co.NUMERO" +
                    "INNER JOIN criteres_evaluation ce ON n.FK_CRIT = ce.NUMERO"
                    "WHERE id = ?");
            s.setInt(1, id);
            ResultSet rs = s.executeQuery();

            if(rs.next()) {
                grade = this.loadGrade(rs);

                grade = new Grade(
                        rs.getInt("numero"),
                        rs.getInt("note"),
                        eval,
                        crit
                ); //criteria to be implemented later
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
            PreparedStatement s = c.prepareStatement("SELECT * FROM notes");
            ResultSet rs = s.executeQuery();
            while (rs.next()) {
                //blablablabla identity map
                CompleteEvaluation eval = new CompleteEvaluationMapper().findById(rs.getInt("fk_comm"));
                EvaluationCriteria crit = new EvaluationCriteriaMapper().findById(rs.getInt("fk_crit"));

                grades.add(new Grade(
                        rs.getInt("numero"),
                        rs.getInt("note"),
                        eval,
                        crit
                ));
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
            PreparedStatement s = c.prepareStatement(
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
            c.commit();
        } catch (SQLException e) {
            logger.error("SQLException: " + e.getMessage());
        }
        return grade;
    }

    public boolean update(Grade grade) {
        int affectedRows = 0;
        try {
            PreparedStatement s = c.prepareStatement(
                    "UPDATE notes"+
                    "SET note = ?, fk_comm = ?, fk_crit = ?"+
                    "WHERE numero = ?");
            s.setInt(1, grade.getGrade());
            s.setInt(2, grade.getEvaluation().getId());
            s.setInt(3, grade.getCriteria().getId());
            affectedRows = s.executeUpdate();
            c.commit();
        }catch (SQLException e) {
            logger.error("SQLException: " + e.getMessage());
        }
        return affectedRows > 0;
    }

    public boolean delete(Grade grade) { return this.deleteById(grade.getId()); }

    public boolean deleteById(int id) {
        int affectedRows = 0;
        try{
            PreparedStatement s = c.prepareStatement(
                    "DELETE notes WHERE numero = ? ");
            s.setInt(1, id);
            affectedRows = s.executeUpdate();
            c.commit();
        } catch (SQLException e) {
            logger.error("SQLException: " + e.getMessage());
        }
        return affectedRows > 0;
    }

    protected String getSequenceQuery() {return "SELECT seq_notes.NextVal FROM dual";}
    protected String getExistsQuery() {return "SELECT numero FROM notes WHERE numero = ? ";}
    protected String getCountQuery() {return "SELECT count(*) FROM notes";}

}