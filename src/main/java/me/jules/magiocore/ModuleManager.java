package me.jules.magiocore;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ModuleManager {
    private final MagioCore plugin;
    private final File modulesDir;
    private final File modulesToggleFile;
    private FileConfiguration modulesToggleConfig;
    private final Map<String, Boolean> moduleStates = new HashMap<>();
    private final Map<String, FileConfiguration> moduleConfigs = new HashMap<>();

    public ModuleManager(MagioCore plugin) {
        this.plugin = plugin;
        this.modulesDir = new File(plugin.getDataFolder(), "modules");
        if (!modulesDir.exists()) modulesDir.mkdirs();

        this.modulesToggleFile = new File(plugin.getDataFolder(), "modules.yml");
        loadModulesToggleConfig();
        loadAllModuleConfigs();
    }

    public void loadModulesToggleConfig() {
        if (!modulesToggleFile.exists()) {
            plugin.saveResource("modules.yml", false);
        }
        modulesToggleConfig = YamlConfiguration.loadConfiguration(modulesToggleFile);

        String[] modules = {
            "home", "tpa", "spawn", "gamemode", "rtp", "flyspeed", "playtime", "coinflip",
            "invsee", "baltop", "dailyrewards", "playtimerewards", "itemedit", "utilities",
            "virtualspawner", "vanish", "warp", "afkzone", "msg", "join", "chat", "settings",
            "autorestart", "staffchat", "report", "socials", "keyall", "freeze", "antigrief", "deathsystem"
        };
        for (String module : modules) {
            if (!modulesToggleConfig.contains(module)) {
                modulesToggleConfig.set(module, true);
            }
            moduleStates.put(module.toLowerCase(), modulesToggleConfig.getBoolean(module));
        }
        saveModulesToggleConfig();
    }

    private void loadAllModuleConfigs() {
        String[] modules = {
            "home", "tpa", "spawn", "autorestart", "socials", "keyall", "freeze",
            "antigrief", "deathsystem", "virtualspawner", "coinflip", "baltop",
            "gamemode", "rtp", "flyspeed", "playtime", "invsee", "dailyrewards",
            "playtimerewards", "itemedit", "utilities", "vanish", "warp", "afkzone", "msg",
            "join", "chat", "settings", "report", "scoreboard"
        };
        for (String module : modules) {
            File file = new File(modulesDir, module + ".yml");
            if (!file.exists()) {
                plugin.saveResource("modules/" + module + ".yml", false);
            }
            moduleConfigs.put(module, YamlConfiguration.loadConfiguration(file));
        }
    }

    public void saveModulesToggleConfig() {
        try {
            modulesToggleConfig.save(modulesToggleFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isEnabled(String moduleName) {
        return moduleStates.getOrDefault(moduleName.toLowerCase(), false);
    }

    public FileConfiguration getModuleConfig(String moduleName) {
        return moduleConfigs.get(moduleName.toLowerCase());
    }

    public void saveModuleConfig(String moduleName) {
        FileConfiguration config = getModuleConfig(moduleName);
        if (config == null) return;
        try {
            config.save(new File(modulesDir, moduleName.toLowerCase() + ".yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
