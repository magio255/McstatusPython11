package me.jules.magiocore;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SettingsManager {
    private final MagioCore plugin;
    private final File file;
    private FileConfiguration config;
    private final Map<UUID, PlayerSettings> settingsMap = new ConcurrentHashMap<>();

    public SettingsManager(MagioCore plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "settings.yml");
        load();
    }

    public static class PlayerSettings {
        public boolean chat = true;
        public boolean dms = true;
        public boolean actionbar = true;
        public boolean scoreboard = false; // Disabled by default as per memory
    }

    public PlayerSettings getSettings(UUID uuid) {
        return settingsMap.computeIfAbsent(uuid, k -> {
            PlayerSettings s = new PlayerSettings();
            if (config.contains(uuid.toString())) {
                s.chat = config.getBoolean(uuid.toString() + ".chat", true);
                s.dms = config.getBoolean(uuid.toString() + ".dms", true);
                s.actionbar = config.getBoolean(uuid.toString() + ".actionbar", true);
                s.scoreboard = config.getBoolean(uuid.toString() + ".scoreboard", false);
            }
            return s;
        });
    }

    public void saveSettings(UUID uuid) {
        PlayerSettings s = settingsMap.get(uuid);
        if (s == null) return;
        config.set(uuid.toString() + ".chat", s.chat);
        config.set(uuid.toString() + ".dms", s.dms);
        config.set(uuid.toString() + ".actionbar", s.actionbar);
        config.set(uuid.toString() + ".scoreboard", s.scoreboard);
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void load() {
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
    }
}
