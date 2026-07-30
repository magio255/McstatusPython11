package me.jules.magiograves;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InteractionListener implements Listener {

    private final Magiograves plugin;
    private final DeathLootManager deathLootManager;

    public InteractionListener(Magiograves plugin, DeathLootManager deathLootManager) {
        this.plugin = plugin;
        this.deathLootManager = deathLootManager;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null || (block.getType() != Material.PLAYER_HEAD && block.getType() != Material.PLAYER_WALL_HEAD)) return;

        DeathLootManager.DeathLoot loot = deathLootManager.getLoot(block.getLocation());
        if (loot != null) {
            event.setCancelled(true);
            event.getPlayer().openInventory(loot.getInventory());
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.PLAYER_HEAD && block.getType() != Material.PLAYER_WALL_HEAD) return;

        DeathLootManager.DeathLoot loot = deathLootManager.getLoot(block.getLocation());
        if (loot != null) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cYou cannot break this grave! Right-click to loot it.");
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        if (event.getView().getTitle().startsWith("Loot: ")) {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (isEmpty(inventory)) {
                    // Try to find the loot by checking all active loots (matching inventory)
                    Location locToRemove = deathLootManager.getLocationByInventory(inventory);
                    if (locToRemove != null) {
                        deathLootManager.removeLoot(locToRemove);
                    }
                }
            });
        }
    }

    private boolean isEmpty(Inventory inv) {
        for (ItemStack item : inv.getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                return false;
            }
        }
        return true;
    }
}
