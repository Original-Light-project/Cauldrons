package ol.cauldrons.player;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import ol.cauldrons.recipe.Recipe;

public class CauldronsPlayer {
    public Player player;
    public Map<Recipe, Boolean> recipesUnlocked = new HashMap<>();
}
