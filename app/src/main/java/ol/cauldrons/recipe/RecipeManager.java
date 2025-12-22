package ol.cauldrons.recipe;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class RecipeManager {
    private final JavaPlugin plugin;
    private final Map<String, Recipe> recipes = new HashMap<>();

    public RecipeManager(JavaPlugin plugin)
    {
        this.plugin = plugin;
    }

    public void loadRecipes() {
        recipes.clear();

        File file = new File(plugin.getDataFolder(), "recipes.yml");
        if (!file.exists()) {
            plugin.getDataFolder().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create recipes.yml!");
                e.printStackTrace();
            }
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = config.getConfigurationSection("recipes");

        for (String id : section.getKeys(false)) {
            ConfigurationSection cs = section.getConfigurationSection(id);
            if (cs == null) continue;

            String name = cs.getString("name", id);
            Map<Material, Integer> ingredients = new HashMap<>();
            ConfigurationSection cs2 = cs.getConfigurationSection("ingredients");
            for (String ingredient : cs2.getKeys(false)) {
                Material material = Material.valueOf(ingredient);
                int amount = cs2.getInt(ingredient);
                ingredients.put(material, amount);
            }
            
            Material resultMaterial = Material.valueOf(cs.getString("result.material"));
            int resultAmount = cs.getInt("result.amount", 1);

            ItemStack result = new ItemStack(resultMaterial, resultAmount);
            Recipe recipe = new Recipe(id, name, ingredients, result);

            recipes.put(id, recipe);
        }
    }

    public Collection<Recipe> getAllRecipes() {
        return recipes.values();
    }

    public Recipe getRecipe(String id) {
        return id == null ? null : recipes.get(id.toLowerCase());
    }
}
