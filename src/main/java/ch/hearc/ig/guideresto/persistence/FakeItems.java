package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * @author cedric.baudet
 */
public class FakeItems {

    private static Set<RestaurantType> types;
    private static Set<Restaurant> restaurants;
    private static Set<EvaluationCriteria> criterias;
    private static Set<City> cities;

    private static boolean initDone = false;
    private static final Logger logger = LogManager.getLogger();


    private static void init() {
        initDone = true;

        RestaurantTypeMapper typeMapper = new RestaurantTypeMapper();
        CityMapper cityMapper = new CityMapper();
        EvaluationCriteriaMapper criteriaMapper = new EvaluationCriteriaMapper();
        RestaurantMapper restaurantMapper = new RestaurantMapper();

        restaurants = new LinkedHashSet<>(restaurantMapper.findAll());
        types = new LinkedHashSet<>(typeMapper.findAll());
        criterias = new LinkedHashSet<>(criteriaMapper.findAll());
        cities = new LinkedHashSet<>(cityMapper.findAll());
    }

    public static Set<Restaurant> getAllRestaurants() {
        if (!initDone) {
            init();
        }

        return restaurants;
    }

    public static Set<EvaluationCriteria> getEvaluationCriterias() {
        if (!initDone) {
            init();
        }

        return criterias;
    }

    public static Set<RestaurantType> getRestaurantTypes() {
        if (!initDone) {
            init();
        }

        return types;
    }

    public static Set<City> getCities() {
        if (!initDone) {
            init();
        }

        return cities;
    }

}
