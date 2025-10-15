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
        RestaurantMapper restaurantMapper = new RestaurantMapper();

        // I know it's shit. but we need to be able to get the ID in the variables to init.
        // We'll be able to get rid of everything when the mappers and identity map are fully implemented
        Map<Integer, Restaurant> restaurantsScopedMap = new LinkedHashMap<>();
        Map<Integer, RestaurantType> typesScopedMap = new LinkedHashMap<>();
        Map<Integer, EvaluationCriteria> criteriasScopedMap = new LinkedHashMap<>();
        Map<Integer, City> citiesScopedMap = new LinkedHashMap<>();

        restaurants = new LinkedHashSet<>(restaurantMapper.findAll());
        types = new LinkedHashSet<>(typeMapper.findAll());
        criterias = new LinkedHashSet<>();
        cities = new LinkedHashSet<>(cityMapper.findAll());

        RestaurantType typeSuisse = new RestaurantType("Cuisine suisse", "Cuisine classique et plats typiquement suisses");
        RestaurantType typeGastro = new RestaurantType("Restaurant gastronomique", "Restaurant gastronomique de haut standing");
        if (!types.contains(typeSuisse)) {
            typeMapper.create(typeSuisse);
            types.add(typeSuisse);
        }
        if (!types.contains(typeGastro)) {
            typeMapper.create(typeGastro);
            types.add(typeGastro);
        }
        // méthode dite "du bourrin" selon Eddy.
        // on essaie de le mettre dans la base et si cela lève une erreur on gère. voir méthode create()
        types.add(typeMapper.create(new RestaurantType("Pizzeria", "Pizzas et autres spécialités italiennes")));

        for(RestaurantType t : types) {
            typesScopedMap.put(t.hashCode(), t);
        }
        typeSuisse.setId(typesScopedMap.get(typeSuisse.hashCode()).getId());
        typeGastro.setId(typesScopedMap.get(typeGastro.hashCode()).getId());

        EvaluationCriteria critService = new EvaluationCriteria(1, "Service", "Qualité du service");
        EvaluationCriteria critCuisine = new EvaluationCriteria(2, "Cuisine", "Qualité de la nourriture");
        EvaluationCriteria critCadre = new EvaluationCriteria(3, "Cadre", "L'ambiance et la décoration sont-elles bonnes ?");

        criterias.add(critService);
        criterias.add(critCuisine);
        criterias.add(critCadre);

        City city = new City("2000", "Neuchatel");
        if (!cities.contains(city)) {
            cityMapper.create(city);
            cities.add(city);
        }
        for (City c : cities) {
            citiesScopedMap.put(c.hashCode(), c);
        }
        city.setId(citiesScopedMap.get(city.hashCode()).getId());

        Restaurant restaurant = new Restaurant("Fleur-de-Lys", "Pizzeria au centre de Neuchâtel", "http://www.pizzeria-neuchatel.ch/", "Rue du Bassin 10", city, typeSuisse);
        if (!restaurants.contains(restaurant)) {
            restaurantMapper.create(restaurant);
            restaurants.add(restaurant);
        }
        for (Restaurant r : restaurants) {
            restaurantsScopedMap.put(r.hashCode(), r);
        }
        restaurant.setId(restaurantsScopedMap.get(restaurant.hashCode()).getId());

        city.getRestaurants().add(restaurant);
        typeSuisse.getRestaurants().add(restaurant);
        restaurant.getEvaluations().add(new BasicEvaluation(1, new Date(), restaurant, true, "1.2.3.4"));
        restaurant.getEvaluations().add(new BasicEvaluation(2, new Date(), restaurant, true, "1.2.3.5"));
        restaurant.getEvaluations().add(new BasicEvaluation(3, new Date(), restaurant, false, "1.2.3.6"));

        CompleteEvaluation ce = new CompleteEvaluation(1, new Date(), restaurant, "Génial !", "Toto");
        ce.getGrades().add(new Grade(1, 4, ce, critService));
        ce.getGrades().add(new Grade(2, 5, ce, critCuisine));
        ce.getGrades().add(new Grade(3, 4, ce, critCadre));
        restaurant.getEvaluations().add(ce);

        ce = new CompleteEvaluation(2, new Date(), restaurant, "Très bon", "Titi");
        ce.getGrades().add(new Grade(4, 4, ce, critService));
        ce.getGrades().add(new Grade(5, 4, ce, critCuisine));
        ce.getGrades().add(new Grade(6, 4, ce, critCadre));
        restaurant.getEvaluations().add(ce);


        restaurant = new Restaurant("La Maison du Prussien", "Restaurant gastronomique renommé de Neuchâtel", "www.hotel-prussien.ch/‎", "Rue des Tunnels 11", city, typeGastro);
        if (!restaurants.contains(restaurant)) {
            restaurantMapper.create(restaurant);
            restaurants.add(restaurant);
        }
        for (Restaurant r : restaurants) {
            restaurantsScopedMap.put(r.hashCode(), r);
        }
        restaurant.setId(restaurantsScopedMap.get(restaurant.hashCode()).getId());

        typeGastro.getRestaurants().add(restaurant);
        restaurant.getEvaluations().add(new BasicEvaluation(4, new Date(), restaurant, true, "1.2.3.7"));
        restaurant.getEvaluations().add(new BasicEvaluation(5, new Date(), restaurant, true, "1.2.3.8"));
        restaurant.getEvaluations().add(new BasicEvaluation(6, new Date(), restaurant, true, "1.2.3.9"));
        ce = new CompleteEvaluation(3, new Date(), restaurant, "Un régal !", "Dupont");
        ce.getGrades().add(new Grade(7, 5, ce, critService));
        ce.getGrades().add(new Grade(8, 5, ce, critCuisine));
        ce.getGrades().add(new Grade(9, 5, ce, critCadre));
        restaurant.getEvaluations().add(ce);

        ce = new CompleteEvaluation(2, new Date(), restaurant, "Rien à dire, le top !", "Dupasquier");
        ce.getGrades().add(new Grade(10, 5, ce, critService));
        ce.getGrades().add(new Grade(11, 5, ce, critCuisine));
        ce.getGrades().add(new Grade(12, 5, ce, critCadre));
        restaurant.getEvaluations().add(ce);
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
