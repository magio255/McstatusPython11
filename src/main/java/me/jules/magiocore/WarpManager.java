package me.jules.magiocore;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class WarpManager {
    private final MagioCore plugin;
    private final File file;
    private FileConfiguration config;
    private final Map<String, Warp> warps = new HashMap<>();

    public WarpManager(MagioCore plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "warps.yml");
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
        warps.clear();

        for (String name : config.getKeys(false)) {
            Location loc = config.getLocation(name);
            if (loc != null) {
                warps.put(name.toLowerCase(), new Warp(name, loc));
            }
        }
    }

    public void save() {
        for (String key : config.getKeys(false)) {
            config.set(key, null);
        }
        for (Warp warp : warps.values()) {
            config.set(warp.getName(), warp.getLocation());
        }
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setWarp(String name, Location loc) {
        warps.put(name.toLowerCase(), new Warp(name, loc));
        save();
    }

    public void deleteWarp(String name) {
        warps.remove(name.toLowerCase());
        save();
    }

    public Warp getWarp(String name) {
        return warps.get(name.toLowerCase());
    }

    public Collection<Warp> getWarps() {
        return warps.values();
    }

    public boolean exists(String name) {
        return warps.containsKey(name.toLowerCase());
    }
}
