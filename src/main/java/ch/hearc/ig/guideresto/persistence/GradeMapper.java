
package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.*;

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

    @Override
    public Grade findById(int id) {
        Grade grade = null;
        try {
            PreparedStatement s = c.prepareStatement("SELECT * value FROM grade WHERE id = ?");
            s.setInt(1, id);
            ResultSet rs = s.executeQuery();

            if(rs.next()) {
                CompleteEvaluation eval = new CompleteEvaluationMapper().findById(rs.getInt("fk_comm"));
                //EvaluationCriteria crit = new EvaluationCriteriaMapper().findById(rs.getInt("fk_crit"));
                EvaluationCriteria crit = null;

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