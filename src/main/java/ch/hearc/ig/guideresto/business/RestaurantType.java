package ch.hearc.ig.guideresto.business;

import ch.hearc.ig.guideresto.persistence.RestaurantMapper;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author cedric.baudet
 */
public class RestaurantType implements IBusinessObject {

    private Integer id;
    private String label;
    private String description;
    private Set<Restaurant> restaurants;

    public RestaurantType() {
        this(null, null);
    }

    public RestaurantType(String label, String description) {
        this(null, label, description);
    }

    public RestaurantType(Integer id, String label, String description) {
        this.restaurants = null;
        this.id = id;
        this.label = label;
        this.description = description;
    }

    @Override
    public String toString() {
        return label;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<Restaurant> getRestaurants() {
        if (restaurants == null) {
            RestaurantMapper restaurantMapper = new RestaurantMapper();
            restaurants = restaurantMapper.findForType(this);
        }
        return restaurants;
    }

    public void setRestaurants(Set<Restaurant> restaurants) {
        this.restaurants = restaurants;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        RestaurantType that = (RestaurantType) o;
        // ne prends pas en compte l'id pour permettre la comparaison Objet - DB
        return Objects.equals(label, that.label) && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(label, description);
    }
}