package me.jules.magiograves;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class DeathListener implements Listener {

    private final Magiograves plugin;
    private final DeathLootManager deathLootManager;

    public DeathListener(Magiograves plugin, DeathLootManager deathLootManager) {
        this.plugin = plugin;
        this.deathLootManager = deathLootManager;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (event.getDrops().isEmpty() && player.getInventory().isEmpty()) {
            return;
        }

        // Store drops and clear them from world
        ItemStack[] items = event.getDrops().toArray(new ItemStack[0]);
        event.getDrops().clear();

        Location deathLoc = player.getLocation().getBlock().getLocation();
        Block block = deathLoc.getBlock();
        block.setType(Material.PLAYER_HEAD);

        // Set skin to dead player
        if (block.getState() instanceof Skull skull) {
            skull.setOwningPlayer(player);
            skull.update();
        }

        deathLootManager.addLoot(deathLoc, player.getUniqueId(), player.getName(), items);
        DeathLootManager.DeathLoot loot = deathLootManager.getLoot(deathLoc);

        // Spawn TextDisplay
        Location displayLoc = deathLoc.clone().add(0.5, 1.2, 0.5);
        TextDisplay textDisplay = deathLoc.getWorld().spawn(displayLoc, TextDisplay.class, display -> {
            display.setBillboard(TextDisplay.Billboard.CENTER);
            display.setText(player.getName() + "\n§e5:00");
        });

        loot.setTextDisplay(textDisplay);
    }
}
