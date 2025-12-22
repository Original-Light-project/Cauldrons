package ol.cauldrons.gui;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import ol.cauldrons.Cauldrons;
import ol.cauldrons.recipe.Recipe;

public class GuiManager {
    private static final int[] INGREDIENT_SLOTS = {28, 29, 30, 37, 38, 39};
    private static final int COOK_SLOT = 32;
    private static final int RESULT_SLOT = 34;

    private static final Set<Integer> ALLOWED_SLOTS = Set.of(28, 29, 30, 37, 38, 39);

    private static final Map<Player, Recipe> selectedRecipes = new HashMap<>();

    public static void openCookGui(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, "Cauldrons");

        ItemStack filler = createPane(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 54; i++) {
            inv.setItem(i, filler);
        }

        for (int slot : INGREDIENT_SLOTS) {
            inv.setItem(slot, null);
        }

        inv.setItem(10, createPane(Material.BOOK, "§aRecipe Book"));
        inv.setItem(RESULT_SLOT, null);

        Recipe recipe = selectedRecipes.get(player);
        if (recipe == null) {
            inv.setItem(12, createPane(Material.BARRIER, "§cNo recipe selected"));
        } else {
            ItemStack display = recipe.getResult().clone();
            ItemMeta meta = display.getItemMeta();
            meta.setDisplayName("§a" + recipe.getName());

            List<String> lore = new ArrayList<>();
            recipe.getIngredients().forEach((mat, amt) ->
                    lore.add("§7" + mat.name() + " x" + amt)
            );

            meta.setLore(lore);
            display.setItemMeta(meta);
            inv.setItem(12, display);
        }

        updateCookIndicator(player, inv);
        player.openInventory(inv);
    }

    public static void closeCookGui(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        String title = event.getView().getTitle();

        if (!title.equals("Cauldrons")) return;

        Inventory inv = event.getInventory();

        for (int slot : GuiManager.INGREDIENT_SLOTS) {
            ItemStack item = inv.getItem(slot);
            if (item != null && !item.getType().isAir()) {
                HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(item);
                leftover.values().forEach(i -> player.getWorld().dropItemNaturally(player.getLocation(), i));
            }
        }

        ItemStack result = inv.getItem(GuiManager.RESULT_SLOT);
        if (result != null && !result.getType().isAir()) {
            HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(result);
            leftover.values().forEach(i -> player.getWorld().dropItemNaturally(player.getLocation(), i));
        }

        inv.clear();
    }

    public static void handleCookGuiEvent(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (event.getClickedInventory() == null) return;

        Player player = (Player) event.getWhoClicked();

        if (event.getClickedInventory().getType() == InventoryType.PLAYER) return;

        String title = event.getView().getTitle();

        if (title.equals("Cauldrons")) {
            int slot = event.getSlot();

            if (event.isShiftClick() && event.getClickedInventory().getType() == InventoryType.PLAYER) {
                event.setCancelled(true);
                return;
            }

            if (slot == RESULT_SLOT) {
                if (event.getCursor() != null && !event.getCursor().getType().isAir()) {
                    event.setCancelled(true);
                }
                return;
            }

            if (!ALLOWED_SLOTS.contains(slot)) {
                event.setCancelled(true);

                if (slot == 10) {
                    openRecipeGui(player);
                    return;
                }

                if (slot == COOK_SLOT) {
                    tryCook(player, event.getInventory());
                }
            }

            Bukkit.getScheduler().runTask(
                    Cauldrons.instance,
                    () -> updateCookIndicator(player, event.getInventory())
            );
        }

        if (title.equals("Recipes")) {
            event.setCancelled(true);

            ItemStack item = event.getCurrentItem();
            if (item == null || item.getType().isAir()) return;
            if (!item.hasItemMeta() || item.getItemMeta().getLore() == null) return;

            String idLine = item.getItemMeta().getLore().stream()
                    .filter(s -> s.startsWith("§8ID: "))
                    .findFirst()
                    .orElse(null);

            if (idLine == null) return;

            String id = idLine.replace("§8ID: ", "");
            Recipe recipe = Cauldrons.instance.rm.getRecipe(id);
            if (recipe == null) return;

            selectedRecipes.put(player, recipe);
            openCookGui(player);
        }
    }

    private static void updateCookIndicator(Player player, Inventory inv) {
        Recipe recipe = selectedRecipes.get(player);

        if (recipe == null) {
            inv.setItem(COOK_SLOT,
                    createPane(Material.RED_STAINED_GLASS_PANE, "§cSelect a recipe"));
            return;
        }

        Map<Material, Integer> missing = getMissingIngredients(recipe, inv);

        ItemStack output = inv.getItem(RESULT_SLOT);
        boolean outputBlocked = output != null && !output.getType().isAir();

        if (missing.isEmpty() && !outputBlocked) {
            inv.setItem(COOK_SLOT,
                    createPane(Material.GREEN_STAINED_GLASS_PANE, "§aClick to cook"));
        } else {
            ItemStack pane = new ItemStack(Material.RED_STAINED_GLASS_PANE);
            ItemMeta meta = pane.getItemMeta();
            meta.setDisplayName("§cCannot cook");

            List<String> lore = new ArrayList<>();
            if (outputBlocked) {
                lore.add("§7Output slot is not empty");
            }

            missing.forEach((mat, amt) ->
                    lore.add("§7" + mat.name() + " x" + amt)
            );

            meta.setLore(lore);
            pane.setItemMeta(meta);
            inv.setItem(COOK_SLOT, pane);
        }
    }

    private static Map<Material, Integer> getMissingIngredients(Recipe recipe, Inventory inv) {
        Map<Material, Integer> provided = new HashMap<>();

        for (int slot : INGREDIENT_SLOTS) {
            ItemStack item = inv.getItem(slot);
            if (item == null || item.getType().isAir()) continue;

            provided.merge(item.getType(), item.getAmount(), Integer::sum);
        }

        Map<Material, Integer> missing = new HashMap<>();

        recipe.getIngredients().forEach((mat, req) -> {
            int have = provided.getOrDefault(mat, 0);
            if (have < req) {
                missing.put(mat, req - have);
            }
        });

        return missing;
    }

    private static void tryCook(Player player, Inventory inv) {
        Recipe recipe = selectedRecipes.get(player);
        if (recipe == null) return;

        if (!getMissingIngredients(recipe, inv).isEmpty()) return;

        ItemStack output = inv.getItem(RESULT_SLOT);
        if (output != null && !output.getType().isAir()) return;

        recipe.getIngredients().forEach((mat, amount) -> {
            int remaining = amount;

            for (int slot : INGREDIENT_SLOTS) {
                if (remaining <= 0) break;

                ItemStack item = inv.getItem(slot);
                if (item == null || item.getType() != mat) continue;

                int take = Math.min(item.getAmount(), remaining);
                item.setAmount(item.getAmount() - take);
                remaining -= take;

                if (item.getAmount() <= 0) {
                    inv.setItem(slot, null);
                }
            }
        });

        inv.setItem(RESULT_SLOT, recipe.getResult().clone());
        player.sendMessage("§aCooked §f" + recipe.getName());
    }

    public static void openRecipeGui(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, "Recipes");

        int slot = 0;
        for (Recipe r : Cauldrons.instance.rm.getAllRecipes()) {
            ItemStack icon = r.getResult().clone();
            ItemMeta meta = icon.getItemMeta();

            meta.setDisplayName("§a" + r.getName());
            meta.setLore(List.of(
                    "§7Click to select",
                    "§8ID: " + r.getId()
            ));

            icon.setItemMeta(meta);
            inv.setItem(slot++, icon);
        }

        player.openInventory(inv);
    }

    private static ItemStack createPane(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }
}
