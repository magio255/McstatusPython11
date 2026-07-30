package me.jules.magiograves;

import org.bukkit.plugin.java.JavaPlugin;

public class Magiograves extends JavaPlugin {

    private DeathLootManager deathLootManager;

    @Override
    public void onEnable() {
        this.deathLootManager = new DeathLootManager(this);

        getServer().getPluginManager().registerEvents(new DeathListener(this, deathLootManager), this);
        getServer().getPluginManager().registerEvents(new InteractionListener(this, deathLootManager), this);

        getLogger().info("Magiograves has been enabled!");
    }

    @Override
    public void onDisable() {
        if (deathLootManager != null) {
            deathLootManager.cleanupAll();
        }
        getLogger().info("Magiograves has been disabled!");
    }

    public DeathLootManager getDeathLootManager() {
        return deathLootManager;
    }
}
