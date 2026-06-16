package me.jules.magiocore;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HomeManager {
    private final MagioCore plugin;
    private final File file;
    private final FileConfiguration config;
    private final Map<UUID, Map<Integer, Home>> playerHomes = new HashMap<>();

    public HomeManager(MagioCore plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "homes.yml");
        if (!file.exists()) {
            plugin.getDataFolder().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.config = YamlConfiguration.loadConfiguration(file);
        loadHomes();
    }

    private void loadHomes() {
        if (config.getConfigurationSection("homes") == null) return;
        for (String uuidStr : config.getConfigurationSection("homes").getKeys(false)) {
            UUID uuid = UUID.fromString(uuidStr);
            Map<Integer, Home> homes = new HashMap<>();
            for (String numStr : config.getConfigurationSection("homes." + uuidStr).getKeys(false)) {
                int number = Integer.parseInt(numStr);
                Location loc = config.getLocation("homes." + uuidStr + "." + numStr);
                homes.put(number, new Home(uuid, number, loc));
            }
            playerHomes.put(uuid, homes);
        }
    }

    public void setHome(UUID uuid, int number, Location location) {
        Map<Integer, Home> homes = playerHomes.computeIfAbsent(uuid, k -> new HashMap<>());
        homes.put(number, new Home(uuid, number, location));
        config.set("homes." + uuid.toString() + "." + number, location);
        save();
    }

    public Home getHome(UUID uuid, int number) {
        Map<Integer, Home> homes = playerHomes.get(uuid);
        return (homes != null) ? homes.get(number) : null;
    }

    public Map<Integer, Home> getHomes(UUID uuid) {
        return playerHomes.getOrDefault(uuid, new HashMap<>());
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
