package me.jules.magiocore;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CoinflipManager {
    private final MagioCore plugin;
    private final List<CoinflipBet> activeBets = new ArrayList<>();
    private final ConcurrentHashMap<UUID, CoinflipStats> statsMap = new ConcurrentHashMap<>();
    private final File file;
    private FileConfiguration config;

    public CoinflipManager(MagioCore plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "coinflip.yml");
        loadStats();
    }

    public static class CoinflipBet {
        public final UUID creator;
        public final String creatorName;
        public final double amount;

        public CoinflipBet(UUID creator, String creatorName, double amount) {
            this.creator = creator;
            this.creatorName = creatorName;
            this.amount = amount;
        }
    }

    public static class CoinflipStats {
        public int wins = 0;
        public int losses = 0;
        public double wonAmount = 0;
        public double lostAmount = 0;
    }

    public void addBet(Player player, double amount) {
        activeBets.add(new CoinflipBet(player.getUniqueId(), player.getName(), amount));
    }

    public boolean hasActiveBet(UUID uuid) {
        return activeBets.stream().anyMatch(bet -> bet.creator.equals(uuid));
    }

    public List<CoinflipBet> getActiveBets() {
        return activeBets;
    }

    public void removeBet(CoinflipBet bet) {
        activeBets.remove(bet);
    }

    public CoinflipStats getStats(UUID uuid) {
        return statsMap.computeIfAbsent(uuid, k -> new CoinflipStats());
    }

    public void recordWin(UUID uuid, double amount) {
        CoinflipStats stats = getStats(uuid);
        stats.wins++;
        stats.wonAmount += amount;
        saveSingleStats(uuid, stats);
    }

    public void recordLoss(UUID uuid, double amount) {
        CoinflipStats stats = getStats(uuid);
        stats.losses++;
        stats.lostAmount += amount;
        saveSingleStats(uuid, stats);
    }

    private void loadStats() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
        if (config.contains("stats")) {
            for (String key : config.getConfigurationSection("stats").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    CoinflipStats stats = new CoinflipStats();
                    stats.wins = config.getInt("stats." + key + ".wins");
                    stats.losses = config.getInt("stats." + key + ".losses");
                    stats.wonAmount = config.getDouble("stats." + key + ".wonAmount");
                    stats.lostAmount = config.getDouble("stats." + key + ".lostAmount");
                    statsMap.put(uuid, stats);
                } catch (IllegalArgumentException ignored) {}
            }
        }
    }

    private void saveSingleStats(UUID uuid, CoinflipStats stats) {
        config.set("stats." + uuid.toString() + ".wins", stats.wins);
        config.set("stats." + uuid.toString() + ".losses", stats.losses);
        config.set("stats." + uuid.toString() + ".wonAmount", stats.wonAmount);
        config.set("stats." + uuid.toString() + ".lostAmount", stats.lostAmount);
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
