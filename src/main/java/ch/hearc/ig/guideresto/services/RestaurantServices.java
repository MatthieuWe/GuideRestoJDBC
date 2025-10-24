package ch.hearc.ig.guideresto.services;


import ch.hearc.ig.guideresto.business.*;
import ch.hearc.ig.guideresto.persistence.*;

import java.sql.Connection;
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

    public City createCity(City city){
        cityMapper.create(city);
        return city;
    }

    public Restaurant createRestaurant(Restaurant restaurant){
        restaurantMapper.create(restaurant);
        return restaurant;
    }

    public BasicEvaluation createBasicEvaluation(BasicEvaluation eval) {
        basicEvaluationMapper.create(eval);
        return eval;
    }

    public CompleteEvaluation createCompleteEvaluation(CompleteEvaluation eval) {
        completeEvaluationMapper.create(eval);
        return eval;
    }

    public Grade createGrade(Grade grade) {
        gradeMapper.create(grade);
        return grade;
    }

    public void updateRestaurant(Restaurant restaurant) {
        restaurantMapper.update(restaurant);
    }

    public boolean deleteRestaurant(Restaurant restaurant){
        return restaurantMapper.delete(restaurant);
    }

}
