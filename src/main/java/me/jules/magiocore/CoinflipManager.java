package me.jules.magiocore;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CoinflipManager {
    private final MagioCore plugin;
    private final File file;
    private final FileConfiguration config;
    private final List<CoinflipBet> activeBets = new ArrayList<>();
    private final Map<UUID, CoinflipStats> stats = new HashMap<>();

    public CoinflipManager(MagioCore plugin) {
        this.plugin = plugin;
        File storageDir = new File(plugin.getDataFolder(), "Storage");
        if (!storageDir.exists()) storageDir.mkdirs();
        this.file = new File(storageDir, "coinflip.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.config = YamlConfiguration.loadConfiguration(file);
        loadStats();
    }

    private void loadStats() {
        if (config.getConfigurationSection("stats") == null) return;
        for (String key : config.getConfigurationSection("stats").getKeys(false)) {
            UUID uuid = UUID.fromString(key);
            int wins = config.getInt("stats." + key + ".wins");
            int losses = config.getInt("stats." + key + ".losses");
            double won = config.getDouble("stats." + key + ".won");
            double lost = config.getDouble("stats." + key + ".lost");
            stats.put(uuid, new CoinflipStats(wins, losses, won, lost));
        }
    }

    private void saveStats() {
        for (Map.Entry<UUID, CoinflipStats> entry : stats.entrySet()) {
            String key = entry.getKey().toString();
            CoinflipStats s = entry.getValue();
            config.set("stats." + key + ".wins", s.wins());
            config.set("stats." + key + ".losses", s.losses());
            config.set("stats." + key + ".won", s.wonAmount());
            config.set("stats." + key + ".lost", s.lostAmount());
        }
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public static record CoinflipStats(int wins, int losses, double wonAmount, double lostAmount) {
        public CoinflipStats addWin(double amount) { return new CoinflipStats(wins + 1, losses, wonAmount + amount, lostAmount); }
        public CoinflipStats addLoss(double amount) { return new CoinflipStats(wins, losses + 1, wonAmount, lostAmount + amount); }
    }

    public void addBet(Player player, double amount) {
        activeBets.add(new CoinflipBet(player.getUniqueId(), player.getName(), amount));
    }

    public CoinflipStats getStats(UUID uuid) {
        return stats.getOrDefault(uuid, new CoinflipStats(0, 0, 0, 0));
    }

    public void updateStats(UUID uuid, boolean win, double amount) {
        CoinflipStats s = getStats(uuid);
        stats.put(uuid, win ? s.addWin(amount) : s.addLoss(amount));
        saveStats();
    }

    public boolean hasBet(UUID uuid) {
        return activeBets.stream().anyMatch(bet -> bet.creator.equals(uuid));
    }

    public List<CoinflipBet> getActiveBets() {
        return activeBets;
    }

    public void removeBet(CoinflipBet bet) {
        activeBets.remove(bet);
    }
}
