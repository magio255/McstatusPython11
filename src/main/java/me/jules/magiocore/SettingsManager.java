package me.jules.magiocore;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SettingsManager {
    private final MagioCore plugin;
    private final File file;
    private FileConfiguration config;
    private final Map<UUID, PlayerSettings> playerSettings = new HashMap<>();

    public SettingsManager(MagioCore plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "settings.yml");
        load();
    }

    public void load() {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
        for (String key : config.getKeys(false)) {
            UUID uuid = UUID.fromString(key);
            boolean chat = config.getBoolean(key + ".chat", true);
            boolean msg = config.getBoolean(key + ".msg", true);
            boolean bossbar = config.getBoolean(key + ".bossbar", true);
            boolean scoreboard = config.getBoolean(key + ".scoreboard", true);
            playerSettings.put(uuid, new PlayerSettings(chat, msg, bossbar, scoreboard));
        }
    }

    public void save() {
        for (Map.Entry<UUID, PlayerSettings> entry : playerSettings.entrySet()) {
            String key = entry.getKey().toString();
            PlayerSettings s = entry.getValue();
            config.set(key + ".chat", s.chat());
            config.set(key + ".msg", s.msg());
            config.set(key + ".bossbar", s.bossbar());
            config.set(key + ".scoreboard", s.scoreboard());
        }
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public PlayerSettings getSettings(UUID uuid) {
        // Default scoreboard to false so we don't overwrite existing ones from other plugins
        return playerSettings.computeIfAbsent(uuid, k -> new PlayerSettings(true, true, true, false));
    }

    public void updateSettings(UUID uuid, PlayerSettings settings) {
        playerSettings.put(uuid, settings);
        save();
    }

    public record PlayerSettings(boolean chat, boolean msg, boolean bossbar, boolean scoreboard) {
        public PlayerSettings withChat(boolean chat) { return new PlayerSettings(chat, msg, bossbar, scoreboard); }
        public PlayerSettings withMsg(boolean msg) { return new PlayerSettings(chat, msg, bossbar, scoreboard); }
        public PlayerSettings withBossbar(boolean bossbar) { return new PlayerSettings(chat, msg, bossbar, scoreboard); }
        public PlayerSettings withScoreboard(boolean scoreboard) { return new PlayerSettings(chat, msg, bossbar, scoreboard); }
    }
}
