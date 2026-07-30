package me.jules.magiocore;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.entity.Monster;

public class MobSpawnListener implements Listener {
    private final MagioCore plugin;

    public MobSpawnListener(MagioCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMobSpawn(EntitySpawnEvent event) {
        if (!(event.getEntity() instanceof Monster)) return;

        for (Player player : event.getLocation().getWorld().getPlayers()) {
            if (player.getLocation().distanceSquared(event.getLocation()) < 2500) { // ~50 blocks
                if (plugin.getSettingsManager().getSettings(player.getUniqueId()).mobSpawn()) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }
}
