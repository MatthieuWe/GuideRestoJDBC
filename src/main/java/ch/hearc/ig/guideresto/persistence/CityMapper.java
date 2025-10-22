package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.City;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.sql.*;

public class CityMapper extends AbstractMapper<City> {
    private final Connection connection;
    private Map<Long, City> cache = new HashMap<>();


    public CityMapper(Connection connection) {
        this.connection = connection;
    }
    public void clearCache(){
        cache.clear();
    }
    private City addToCache(ResultSet rs) throws SQLException {
        int id = rs.getInt("numero");
        if (!cache.containsKey((long) id)) {
            City city = new City(id, rs.getString("code_postal"), rs.getString("nom_ville"));
            cache.put((long) id, city);
        }
        return cache.get((long) id);
    }

    public City findById(int id) {
        if (cache.containsKey((long) id)) {
            return cache.get((long) id);
        }
        City city = null;
        try {
            PreparedStatement s = connection.prepareStatement("SELECT * FROM villes WHERE numero = ?");
            s.setInt(1, id);
            ResultSet rs = s.executeQuery();
            if(rs.next()) {
                city = new City(
                        rs.getInt("numero"),
                        rs.getString("code_postal"),
                        rs.getString("nom_ville")
                );
            } else {
                logger.error("No such city");
            }
            rs.close();
        } catch (SQLException e) {
            logger.error("SQLException: {}", e.getMessage());
        }
        return city;
    }

    public Set<City> findAll() {
        Set<City> cities = new HashSet<>();
        try {
            PreparedStatement s = connection.prepareStatement("SELECT * FROM villes");
            ResultSet rs = s.executeQuery();
            while(rs.next()) {
                cities.add(addToCache(rs));
            }
            rs.close();
        } catch (SQLException e) {
            logger.error("SQLException: {}", e.getMessage());
        }
        return cities;
    }
    public City create(City city) {
        try {
            String generatedColumns[] = { "numero" };
            PreparedStatement s = connection.prepareStatement(
                    "INSERT INTO villes (code_postal, nom_ville)" +
                            "VALUES (?, ?)",
                    generatedColumns);
            s.setString(1, city.getZipCode());
            s.setString(2, city.getCityName());
            s.executeUpdate();
            ResultSet rs = s.getGeneratedKeys();
            if (rs.next()) {
                city.setId(rs.getInt(1));
                cache.put((long) city.getId(), city);
            } else {
                logger.warn("Failed to insert city into the table: ", city.getCityName() + ". Continuing..." );
            }
            rs.close();
            connection.commit();
        } catch (SQLException e) {
            logger.error("SQLException: {}", e.getMessage());
        }
        return city;
    }
    public boolean update(City city) {
        int affectedRows = 0;
        try {
            PreparedStatement s = connection.prepareStatement(
                    "UPDATE villes SET code_postal = ?, nom_ville = ? WHERE numero = ?");
            s.setString(1, city.getZipCode());
            s.setString(2, city.getCityName());
            s.setInt(3, city.getId());
            affectedRows = s.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            logger.error("SQLException: {}", e.getMessage());
        }
        return affectedRows > 0;
    }
    public boolean delete(City city) {
        return this.deleteById(city.getId());
    }
    public boolean deleteById(int id) {
        int affectedRows = 0;
        try {
            PreparedStatement s = connection.prepareStatement(
                    "DELETE villes WHERE numero = ?");
            s.setInt(1, id);
            affectedRows = s.executeUpdate();
            if (affectedRows > 0) {
                cache.remove((long) id);
            }
            connection.commit();
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
