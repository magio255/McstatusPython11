package me.jules.apexbuild;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class ApexBuild extends JavaPlugin {
    private DataManager dataManager;

    @Override
    public void onEnable() {
        dataManager = new DataManager(this);

        getServer().getPluginManager().registerEvents(new JoinListener(dataManager), this);
        getServer().getPluginManager().registerEvents(new SearchListener(dataManager), this);

        Objects.requireNonNull(getCommand("historyjoins")).setExecutor(new HistoryCommand(dataManager));

        getLogger().info("ApexBuild enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("ApexBuild disabled!");
    }
}
