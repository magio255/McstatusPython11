package me.jules.apexbuild;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class DataManager {
    private final JavaPlugin plugin;
    private File file;
    private FileConfiguration config;
    private final List<JoinEntry> history = new ArrayList<>();

    public DataManager(JavaPlugin plugin) {
        this.plugin = plugin;
        setup();
        load();
    }

    private void setup() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }
        file = new File(plugin.getDataFolder(), "joins.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create joins.yml", e);
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public void load() {
        history.clear();
        if (config.contains("joins")) {
            List<Map<?, ?>> list = config.getMapList("joins");
            for (Map<?, ?> map : list) {
                try {
                    String name = (String) map.get("name");
                    UUID uuid = UUID.fromString((String) map.get("uuid"));
                    String ip = (String) map.get("ip");
                    long time = ((Number) map.get("time")).longValue();
                    history.add(new JoinEntry(name, uuid, ip, time));
                } catch (Exception e) {
                    plugin.getLogger().warning("Skipping invalid join record: " + map);
                }
            }
        }
        // Keep history in reverse chronological order (newest first)
        history.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
    }

    public void addEntry(JoinEntry entry) {
        history.add(0, entry);
        saveAsync();
    }

    private void saveAsync() {
        // Create a copy of the history to avoid ConcurrentModificationException if needed,
        // although addEntry is called from the main thread.
        // To be safe, we'll serialize data to maps before passing to async task.
        List<Map<String, Object>> serialized = new ArrayList<>();
        for (JoinEntry entry : history) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("name", entry.getPlayerName());
            map.put("uuid", entry.getPlayerUUID().toString());
            map.put("ip", entry.getIpAddress());
            map.put("time", entry.getTimestamp());
            serialized.add(map);
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            synchronized (this) {
                config.set("joins", serialized);
                try {
                    config.save(file);
                } catch (IOException e) {
                    plugin.getLogger().log(Level.SEVERE, "Could not save joins.yml", e);
                }
            }
        });
    }

    public List<JoinEntry> getHistory() {
        return Collections.unmodifiableList(history);
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }
}
