package me.jules.magiocore;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class CombatListener implements Listener {
    private final MagioCore plugin;

    public CombatListener(MagioCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onKill(PlayerDeathEvent event) {
        // We handle teleporting to spawn on respawn instead of death to avoid issues
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        FileConfiguration spawnConfig = plugin.getModuleManager().getModuleConfig("spawn");
        Location spawn = spawnConfig.getLocation("location");
        if (spawn != null) {
            event.setRespawnLocation(spawn);
        }
    }
}
