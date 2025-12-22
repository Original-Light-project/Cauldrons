package ol.cauldrons.recipe;

import org.bukkit.Material;

public class Ingredient {
    private final Material material;
    private final int amount;

    public Ingredient(Material material, int amount) {
        this.material = material;
        this.amount = amount;
    }

    public Material getMaterial() {
        return material;
    }

    public int getAmount() {
        return amount;
    }
}
