package ol.cauldrons.recipe;

import java.util.Map;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Recipe {
    private final String id;   // Recipe name to internal ID
    private final String name; // Recipe name to display in GUI
    private final Map<Material, Integer> ingredients;
    private final ItemStack result;

    public Recipe(String id, String name, Map<Material, Integer> ingredients, ItemStack result) {
        this.id = id;
        this.name = name;
        this.ingredients = ingredients;
        this.result = result;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Map<Material, Integer> getIngredients() {
        return ingredients;
    }

    public ItemStack getResult() {
        return result.clone();
    }
}
