package ch.hearc.ig.guideresto.services;


import ch.hearc.ig.guideresto.business.*;
import ch.hearc.ig.guideresto.persistence.*;
import ch.hearc.ig.guideresto.presentation.Application;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class RestaurantServices {
    private final RestaurantMapper restaurantMapper;
    private final Connection connection;
    private final CityMapper cityMapper;
    private final RestaurantTypeMapper restaurantTypeMapper;
    private final GradeMapper gradeMapper;
    private final EvaluationCriteriaMapper evaluationCriteriaMapper;
    private final BasicEvaluationMapper basicEvaluationMapper;
    private final CompleteEvaluationMapper completeEvaluationMapper;
    private static final Logger logger = LogManager.getLogger(RestaurantServices.class);

    public RestaurantServices() {
        connection = ConnectionUtils.getConnection();
        this.restaurantMapper = new RestaurantMapper(connection);
        this.cityMapper = new CityMapper(connection);
        this.restaurantTypeMapper = new RestaurantTypeMapper(connection);
        this.gradeMapper = new GradeMapper(connection);
        this.evaluationCriteriaMapper = new EvaluationCriteriaMapper(connection);
        this.basicEvaluationMapper = new BasicEvaluationMapper(connection);
        this.completeEvaluationMapper = new CompleteEvaluationMapper(connection);
    }

    public Set<Restaurant> findAllRestaurant() {
        return restaurantMapper.findAll();
    }
    public Set<RestaurantType> findAllRestaurantType() {
        return restaurantTypeMapper.findAll();
    }

    public Set<City> findAllCities(){
        return cityMapper.findAll();
    }
    public Set<EvaluationCriteria> findAllEvaluationCriteria() {
        return evaluationCriteriaMapper.findAll();
    }

    public Set<Restaurant> searchByName(String search){
        return restaurantMapper.findByName(search);
    }
    public Set<Restaurant> searchByCity(String search){
        Set<City> cities = cityMapper.findByName(search);
        Set<Restaurant> restaurants = new HashSet<>();
        for (City city : cities){
            restaurants.addAll(restaurantMapper.findForCity(city));
        }
        return restaurants;
    }
    public Set<Restaurant> searchByType(RestaurantType type){
        return restaurantMapper.findForType(type);
    }
    public City createCity(String zipCode, String cityName) {
        City city = new City(zipCode, cityName);
        return cityMapper.create(city);
    }

    public Restaurant createRestaurant(String name, String description, String website, String street, City city, RestaurantType restaurantType) {
        Restaurant restaurant = new Restaurant(name, description, website, street, city, restaurantType);
        city.getRestaurants().add(restaurant);
        restaurantType.getRestaurants().add(restaurant);
        return restaurantMapper.create(restaurant);
    }

    public BasicEvaluation createBasicEvaluation(Restaurant restaurant, Boolean like) {
        String ipAddress;
        try {
            ipAddress = Inet4Address.getLocalHost().toString(); // Permet de retrouver l'adresse IP locale de l'utilisateur.
        } catch (UnknownHostException ex) {
            logger.error("Error - Couldn't retreive host IP address");
            ipAddress = "Indisponible";
        }
        BasicEvaluation eval = new BasicEvaluation(new Date(), restaurant, like, ipAddress);
        restaurant.getEvaluations().add(eval);
        return basicEvaluationMapper.create(eval);
    }

    public CompleteEvaluation createCompleteEvaluation(Restaurant restaurant, String comment, String username) {
        CompleteEvaluation eval = new CompleteEvaluation(new Date(), restaurant, comment, username);
        eval = completeEvaluationMapper.create(eval);
        restaurant.getEvaluations().add(eval);
        return eval;
    }

    public Grade createGrade(Integer note, CompleteEvaluation eval, EvaluationCriteria currentCriteria) {
        Grade grade = new Grade(note, eval, currentCriteria);
        eval.getGrades().add(grade);
        return gradeMapper.create(grade);
    }

    public void updateRestaurant(Restaurant restaurant, RestaurantType newType, City newCity) {
        if (newType != null && newType != restaurant.getType()) {
            restaurant.getType().getRestaurants().remove(restaurant); // Il faut d'abord supprimer notre restaurant puisque le type va peut-être changer
            restaurant.setType(newType);
            newType.getRestaurants().add(restaurant);
        }
        if (newCity != null && newCity != restaurant.getAddress().getCity()) {
            restaurant.getAddress().getCity().getRestaurants().remove(restaurant); // On supprime l'adresse de la ville
            restaurant.getAddress().setCity(newCity);
            newCity.getRestaurants().add(restaurant);
        }
            restaurantMapper.update(restaurant);
    }

    public boolean deleteRestaurant(Restaurant restaurant){
        restaurant.getAddress().getCity().getRestaurants().remove(restaurant);
        restaurant.getType().getRestaurants().remove(restaurant);
        return restaurantMapper.delete(restaurant);
    }

    public void shutdown() {
        ConnectionUtils.closeConnection();
    }

}
