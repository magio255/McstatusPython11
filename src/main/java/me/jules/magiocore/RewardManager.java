package me.jules.magiocore;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RewardManager {
    private final MagioCore plugin;
    private File file;
    private FileConfiguration config;
    private final Map<UUID, Long> dailyClaims = new HashMap<>();
    private final Map<UUID, Integer> dailyStreaks = new HashMap<>();
    private final Map<UUID, Map<Integer, Boolean>> playtimeClaims = new HashMap<>();

    public RewardManager(MagioCore plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        file = new File(plugin.getDataFolder(), "rewards.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(file);

        if (config.contains("daily")) {
            for (String key : config.getConfigurationSection("daily").getKeys(false)) {
                dailyClaims.put(UUID.fromString(key), config.getLong("daily." + key));
            }
        }

        if (config.contains("streaks")) {
            for (String key : config.getConfigurationSection("streaks").getKeys(false)) {
                dailyStreaks.put(UUID.fromString(key), config.getInt("streaks." + key));
            }
        }

        if (config.contains("playtime")) {
            for (String uuidStr : config.getConfigurationSection("playtime").getKeys(false)) {
                UUID uuid = UUID.fromString(uuidStr);
                Map<Integer, Boolean> levels = new HashMap<>();
                for (String levelStr : config.getConfigurationSection("playtime." + uuidStr).getKeys(false)) {
                    levels.put(Integer.parseInt(levelStr), config.getBoolean("playtime." + uuidStr + "." + levelStr));
                }
                playtimeClaims.put(uuid, levels);
            }
        }
    }

    public void save() {
        for (Map.Entry<UUID, Long> entry : dailyClaims.entrySet()) {
            config.set("daily." + entry.getKey().toString(), entry.getValue());
        }
        for (Map.Entry<UUID, Integer> entry : dailyStreaks.entrySet()) {
            config.set("streaks." + entry.getKey().toString(), entry.getValue());
        }
        for (Map.Entry<UUID, Map<Integer, Boolean>> entry : playtimeClaims.entrySet()) {
            String uuid = entry.getKey().toString();
            for (Map.Entry<Integer, Boolean> level : entry.getValue().entrySet()) {
                config.set("playtime." + uuid + "." + level.getKey(), level.getValue());
            }
        }
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public long getLastDailyClaim(UUID uuid) {
        return dailyClaims.getOrDefault(uuid, 0L);
    }

    public void setLastDailyClaim(UUID uuid, long time) {
        dailyClaims.put(uuid, time);
        save();
    }

    public int getDailyStreak(UUID uuid) {
        return dailyStreaks.getOrDefault(uuid, 0);
    }

    public void setDailyStreak(UUID uuid, int streak) {
        dailyStreaks.put(uuid, streak);
        save();
    }

    public boolean hasClaimedPlaytime(UUID uuid, int level) {
        return playtimeClaims.getOrDefault(uuid, new HashMap<>()).getOrDefault(level, false);
    }

    public void setClaimedPlaytime(UUID uuid, int level) {
        playtimeClaims.computeIfAbsent(uuid, k -> new HashMap<>()).put(level, true);
        save();
    }
}
