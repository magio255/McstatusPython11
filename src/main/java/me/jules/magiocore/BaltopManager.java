package me.jules.magiocore;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class BaltopManager {
    private final MagioCore plugin;
    private List<BaltopEntry> cachedTop = new ArrayList<>();

    public static record BaltopEntry(String name, UUID uuid, double balance) {}

    public BaltopManager(MagioCore plugin) {
        this.plugin = plugin;
        startUpdateTask();
    }

    public void startUpdateTask() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::updateTop, 0L, 12000L); // Every 10 mins
    }

    public void updateTop() {
        List<BaltopEntry> entries = new ArrayList<>();
        for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
            double bal = plugin.getEconomy().getBalance(player);
            entries.add(new BaltopEntry(player.getName() != null ? player.getName() : "Unknown", player.getUniqueId(), bal));
        }

        // Sort descending
        entries.sort((e1, e2) -> Double.compare(e2.balance(), e1.balance()));
        this.cachedTop = Collections.unmodifiableList(entries);
    }

    public List<BaltopEntry> getCachedTop() {
        return cachedTop;
    }
}
