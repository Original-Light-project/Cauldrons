package ol.cauldrons;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.plugin.java.JavaPlugin;

import ol.cauldrons.command.Commands;
import ol.cauldrons.gui.GuiManager;
import ol.cauldrons.player.CauldronsPlayer;
import ol.cauldrons.recipe.RecipeManager;

public class Cauldrons extends JavaPlugin implements Listener {
    public static Cauldrons instance;

    public final List<CauldronsPlayer> players = new ArrayList<>();
    public RecipeManager rm;

    @Override
    public void onEnable() {
        instance = this;
        
        this.getLogger().info("Cauldrons Enabled!");
        
        Bukkit.getPluginCommand("cook").setExecutor(new Commands());
        Bukkit.getPluginManager().registerEvents(this, this);

        rm = new RecipeManager(this);
        rm.loadRecipes();
        this.getLogger().info("Loaded " + rm.getAllRecipes().size() + " recipe(s).");
    }

    @Override
    public void onDisable() {
        this.getLogger().info("Cauldrons Disabled!");
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;

        GuiManager.handleCookGuiEvent(event);
    }
}
