package me.jules.apexbuild;

import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class BuildGUI implements Listener {

    private final ApexBuild plugin;
    private final NamespacedKey worldKey;
    private static final Component GUI_TITLE = Component.text("Build Menu", NamedTextColor.DARK_GRAY);
    private static final Component WORLDS_TITLE = Component.text("Existující světy", NamedTextColor.DARK_GRAY);

    public BuildGUI(ApexBuild plugin) {
        this.plugin = plugin;
        this.worldKey = new NamespacedKey(plugin, "world_name");
    }

    public static class BuildHolder implements InventoryHolder {
        @Override public @NotNull Inventory getInventory() { return null; }
    }

    public static class WorldsHolder implements InventoryHolder {
        @Override public @NotNull Inventory getInventory() { return null; }
    }

    public void openGUI(Player player) {
        Inventory inv = Bukkit.createInventory(new BuildHolder(), 27, GUI_TITLE);

        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, Component.text(" "));
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, filler);
        }

        inv.setItem(11, createItem(Material.GRASS_BLOCK,
                Component.text("Vytvořit nový svět", NamedTextColor.GREEN, TextDecoration.BOLD),
                Component.text("Kliknutím vytvoříš svůj vlastní", NamedTextColor.GRAY),
                Component.text("stavební svět.", NamedTextColor.GRAY)));

        inv.setItem(13, createItem(Material.COMPASS,
                Component.text("Moje světy", NamedTextColor.YELLOW, TextDecoration.BOLD),
                Component.text("Kliknutím zobrazíš seznam", NamedTextColor.GRAY),
                Component.text("všech dostupných světů.", NamedTextColor.GRAY)));

        inv.setItem(15, createItem(Material.BARRIER,
                Component.text("Smazat svůj svět", NamedTextColor.RED, TextDecoration.BOLD),
                Component.text("Kliknutím smažeš svůj", NamedTextColor.GRAY),
                Component.text("existující svět.", NamedTextColor.GRAY)));

        player.openInventory(inv);
    }

    public void openWorldsGUI(Player player) {
        Inventory inv = Bukkit.createInventory(new WorldsHolder(), 54, WORLDS_TITLE);

        for (MultiverseWorld mvWorld : plugin.getMultiverseCore().getMVWorldManager().getMVWorlds()) {
            String worldName = mvWorld.getName();
            Material material = Material.GRASS_BLOCK;

            if (mvWorld.getEnvironment() == World.Environment.NETHER) material = Material.NETHERRACK;
            if (mvWorld.getEnvironment() == World.Environment.THE_END) material = Material.END_STONE;

            ItemStack worldItem = createItem(material, Component.text(worldName, NamedTextColor.WHITE));
            ItemMeta meta = worldItem.getItemMeta();
            if (meta != null) {
                meta.getPersistentDataContainer().set(worldKey, PersistentDataType.STRING, worldName);
                List<Component> lore = new ArrayList<>();
                lore.add(Component.text("Klikni pro teleportaci.", NamedTextColor.GRAY));
                meta.lore(lore);
                worldItem.setItemMeta(meta);
            }
            inv.addItem(worldItem);
        }

        player.openInventory(inv);
    }

    private ItemStack createItem(Material material, Component name, Component... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(name.decoration(TextDecoration.ITALIC, false));
            List<Component> loreList = new ArrayList<>();
            for (Component line : lore) {
                loreList.add(line.decoration(TextDecoration.ITALIC, false));
            }
            meta.lore(loreList);
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getClickedInventory() != event.getView().getTopInventory()) return;

        InventoryHolder holder = event.getInventory().getHolder();

        if (holder instanceof BuildHolder) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;

            if (clicked.getType() == Material.GRASS_BLOCK) {
                if (!player.hasPermission("build.create")) {
                    player.sendMessage(Component.text("Nemáš oprávnění vytvářet světy.", NamedTextColor.RED));
                    return;
                }
                WorldManager.createWorld(plugin, player);
                player.closeInventory();
            } else if (clicked.getType() == Material.COMPASS) {
                openWorldsGUI(player);
            } else if (clicked.getType() == Material.BARRIER) {
                WorldManager.deleteWorld(plugin, player);
                player.closeInventory();
            }
        } else if (holder instanceof WorldsHolder) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;

            ItemMeta meta = clicked.getItemMeta();
            if (meta != null && meta.getPersistentDataContainer().has(worldKey, PersistentDataType.STRING)) {
                String worldName = meta.getPersistentDataContainer().get(worldKey, PersistentDataType.STRING);
                if (player.hasPermission("build.tp")) {
                    WorldManager.teleportToWorld(plugin, player, worldName);
                } else {
                    player.sendMessage(Component.text("Nemáš oprávnění se teleportovat do světů.", NamedTextColor.RED));
                }
                player.closeInventory();
            }
        }
    }
}
