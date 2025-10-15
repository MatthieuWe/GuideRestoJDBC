package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.EvaluationCriteria;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class EvaluationCriteriaMapper extends AbstractMapper<EvaluationCriteria> {
    private Connection c = ConnectionUtils.getConnection();

    public EvaluationCriteria findById(int id) {
        EvaluationCriteria criteria = null;
        try {
            PreparedStatement s = c.prepareStatement("SELECT * FROM CRITERES_EVALUATION WHERE id = ?");
            s.setInt(1, id);
            ResultSet rs = s.executeQuery();

            if(rs.next()) {
                criteria = new EvaluationCriteria(
                        rs.getInt("numero"),
                        rs.getString("nom"),
                        rs.getString("description")
                );
            } else {
                logger.error("No evaluation criteria found");
            }
            rs.close();
        } catch (SQLException e) {
            logger.error("SQLException: " + e.getMessage());
        }
        return criteria;
    }

    public Set<EvaluationCriteria> findAll() {
        Set<EvaluationCriteria> result = new HashSet<>();
        String sql = "SELECT * FROM CRITERES_EVALUATION";
        try (PreparedStatement s = c.prepareStatement(sql);
             ResultSet rs = s.executeQuery()) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("SQLException: {}", e.getMessage());
        }
        return result;
    }

    @Override
    public EvaluationCriteria create(EvaluationCriteria object) {
        String sql = "INSERT INTO CRITERES_EVALUATION(numero, nom, description) VALUES(?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, object.getNumero());
            ps.setString(2, object.getNom());
            ps.setString(3, object.getDescription());
            int affected = ps.executeUpdate();
            if (affected == 0) return null;
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    object.setId(keys.getInt(1));
                }
            }
            return object;
        } catch (SQLException e) {
            logger.error("SQLException: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public boolean update(EvaluationCriteria object) {
        String sql = "UPDATE CRITERES_EVALUATION SET numero = ?, nom = ?, description = ? WHERE id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, object.getNumero());
            ps.setString(2, object.getNom());
            ps.setString(3, object.getDescription());
            ps.setInt(4, object.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("SQLException: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(EvaluationCriteria object) {
        return deleteById(object.getId());
    }

    @Override
    public boolean deleteById(int id) {
        String sql = "DELETE FROM CRITERES_EVALUATION WHERE id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("SQLException: {}", e.getMessage());
            return false;
        }
    }

    @Override
    protected String getSequenceQuery() {
        return "SELECT nextval('criteres_evaluation_id_seq')";
    }

    @Override
    protected String getExistsQuery() {
        return "SELECT 1 FROM CRITERES_EVALUATION WHERE id = ?";
    }

    @Override
    protected String getCountQuery() {
        return "SELECT COUNT(*) FROM CRITERES_EVALUATION";
    }

    // Helper to map a ResultSet row to an EvaluationCriteria instance
    private EvaluationCriteria mapRow(ResultSet rs) throws SQLException {
        EvaluationCriteria ec = new EvaluationCriteria(
                rs.getInt("numero"),
                rs.getString("nom"),
                rs.getString("description")
        );
        // set id if column present and setter available
        try {
            int id = rs.getInt("id");
            ec.setId(id);
        } catch (SQLException ignore) {
            // ignore if there's no id column or setter not applicable
        }
        return ec;
    }
}
