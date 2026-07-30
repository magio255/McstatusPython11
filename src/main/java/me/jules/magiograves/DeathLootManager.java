package me.jules.magiograves;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class DeathLootManager {

    private final Magiograves plugin;
    private final Map<Location, DeathLoot> activeLoots = new HashMap<>();

    public DeathLootManager(Magiograves plugin) {
        this.plugin = plugin;
        startCleanupTask();
    }

    public void addLoot(Location location, UUID ownerId, String ownerName, ItemStack[] items) {
        Inventory inventory = Bukkit.createInventory(null, 54, "Loot: " + ownerName);
        for (ItemStack item : items) {
            if (item != null) {
                inventory.addItem(item);
            }
        }

        DeathLoot loot = new DeathLoot(ownerId, ownerName, inventory, 300); // 300 seconds = 5 minutes
        activeLoots.put(location.getBlock().getLocation(), loot);
    }

    public DeathLoot getLoot(Location location) {
        return activeLoots.get(location.getBlock().getLocation());
    }

    public Location getLocationByInventory(Inventory inventory) {
        for (Map.Entry<Location, DeathLoot> entry : activeLoots.entrySet()) {
            if (entry.getValue().getInventory().equals(inventory)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public void removeLoot(Location location) {
        DeathLoot loot = activeLoots.remove(location.getBlock().getLocation());
        if (loot != null) {
            cleanupLoot(location, loot);
        }
    }

    private void cleanupLoot(Location location, DeathLoot loot) {
        Block block = location.getBlock();
        if (block.getType() == Material.PLAYER_HEAD || block.getType() == Material.PLAYER_WALL_HEAD) {
            block.setType(Material.AIR);
        }
        if (loot.getTextDisplay() != null) {
            loot.getTextDisplay().remove();
        }
    }

    public void cleanupAll() {
        for (Map.Entry<Location, DeathLoot> entry : activeLoots.entrySet()) {
            cleanupLoot(entry.getKey(), entry.getValue());
        }
        activeLoots.clear();
    }

    private void startCleanupTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                Iterator<Map.Entry<Location, DeathLoot>> iterator = activeLoots.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<Location, DeathLoot> entry = iterator.next();
                    Location loc = entry.getKey();
                    DeathLoot loot = entry.getValue();

                    loot.decrementTime();
                    if (loot.getTimeLeft() <= 0) {
                        cleanupLoot(loc, loot);
                        iterator.remove();
                    } else {
                        updateHologram(loot);
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private void updateHologram(DeathLoot loot) {
        if (loot.getTextDisplay() != null) {
            int minutes = loot.getTimeLeft() / 60;
            int seconds = loot.getTimeLeft() % 60;
            String timeStr = String.format("%d:%02d", minutes, seconds);
            loot.getTextDisplay().setText(loot.getOwnerName() + "\n§e" + timeStr);
        }
    }

    public static class DeathLoot {
        private final UUID ownerId;
        private final String ownerName;
        private final Inventory inventory;
        private int timeLeft;
        private TextDisplay textDisplay;

        public DeathLoot(UUID ownerId, String ownerName, Inventory inventory, int timeLeft) {
            this.ownerId = ownerId;
            this.ownerName = ownerName;
            this.inventory = inventory;
            this.timeLeft = timeLeft;
        }

        public UUID getOwnerId() { return ownerId; }
        public String getOwnerName() { return ownerName; }
        public Inventory getInventory() { return inventory; }
        public int getTimeLeft() { return timeLeft; }
        public void decrementTime() { this.timeLeft--; }
        public TextDisplay getTextDisplay() { return textDisplay; }
        public void setTextDisplay(TextDisplay textDisplay) { this.textDisplay = textDisplay; }
    }
}
