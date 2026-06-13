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
        File storageDir = new File(plugin.getDataFolder(), "Storage");
        if (!storageDir.exists()) storageDir.mkdirs();
        this.file = new File(storageDir, "settings.yml");
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
        playerSettings.clear();
        for (String key : config.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                boolean chat = config.getBoolean(key + ".chat", true);
                boolean msg = config.getBoolean(key + ".msg", true);
                boolean bossbar = config.getBoolean(key + ".bossbar", true);
                boolean kitOnDeath = config.getBoolean(key + ".kitOnDeath", true);
                boolean tpaInvites = config.getBoolean(key + ".tpaInvites", true);
                boolean tpaAuto = config.getBoolean(key + ".tpaAuto", false);
                boolean mobSpawn = config.getBoolean(key + ".mobSpawn", false);
                boolean nightVision = config.getBoolean(key + ".nightVision", false);
                String coinflipStyle = config.getString(key + ".coinflipStyle", "CLASSIC");
                playerSettings.put(uuid, new PlayerSettings(chat, msg, bossbar, kitOnDeath, tpaInvites, tpaAuto, mobSpawn, nightVision, coinflipStyle));
            } catch (IllegalArgumentException ignored) {}
        }
    }

    public void save() {
        if (config == null) return;
        for (Map.Entry<UUID, PlayerSettings> entry : playerSettings.entrySet()) {
            String key = entry.getKey().toString();
            PlayerSettings s = entry.getValue();
            config.set(key + ".chat", s.chat());
            config.set(key + ".msg", s.msg());
            config.set(key + ".bossbar", s.bossbar());
            config.set(key + ".kitOnDeath", s.kitOnDeath());
            config.set(key + ".tpaInvites", s.tpaInvites());
            config.set(key + ".tpaAuto", s.tpaAuto());
            config.set(key + ".mobSpawn", s.mobSpawn());
            config.set(key + ".nightVision", s.nightVision());
            config.set(key + ".coinflipStyle", s.coinflipStyle());
        }
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public PlayerSettings getSettings(UUID uuid) {
        return playerSettings.computeIfAbsent(uuid, k -> new PlayerSettings(true, true, true, true, true, false, false, false, "CLASSIC"));
    }

    public void updateSettings(UUID uuid, PlayerSettings settings) {
        playerSettings.put(uuid, settings);
        save();
    }

    public record PlayerSettings(boolean chat, boolean msg, boolean bossbar, boolean kitOnDeath, boolean tpaInvites, boolean tpaAuto, boolean mobSpawn, boolean nightVision, String coinflipStyle) {
        public PlayerSettings withChat(boolean chat) { return new PlayerSettings(chat, msg, bossbar, kitOnDeath, tpaInvites, tpaAuto, mobSpawn, nightVision, coinflipStyle); }
        public PlayerSettings withMsg(boolean msg) { return new PlayerSettings(chat, msg, bossbar, kitOnDeath, tpaInvites, tpaAuto, mobSpawn, nightVision, coinflipStyle); }
        public PlayerSettings withBossbar(boolean bossbar) { return new PlayerSettings(chat, msg, bossbar, kitOnDeath, tpaInvites, tpaAuto, mobSpawn, nightVision, coinflipStyle); }
        public PlayerSettings withKitOnDeath(boolean kitOnDeath) { return new PlayerSettings(chat, msg, bossbar, kitOnDeath, tpaInvites, tpaAuto, mobSpawn, nightVision, coinflipStyle); }
        public PlayerSettings withTpaInvites(boolean tpaInvites) { return new PlayerSettings(chat, msg, bossbar, kitOnDeath, tpaInvites, tpaAuto, mobSpawn, nightVision, coinflipStyle); }
        public PlayerSettings withTpaAuto(boolean tpaAuto) { return new PlayerSettings(chat, msg, bossbar, kitOnDeath, tpaInvites, tpaAuto, mobSpawn, nightVision, coinflipStyle); }
        public PlayerSettings withMobSpawn(boolean mobSpawn) { return new PlayerSettings(chat, msg, bossbar, kitOnDeath, tpaInvites, tpaAuto, mobSpawn, nightVision, coinflipStyle); }
        public PlayerSettings withNightVision(boolean nightVision) { return new PlayerSettings(chat, msg, bossbar, kitOnDeath, tpaInvites, tpaAuto, mobSpawn, nightVision, coinflipStyle); }
        public PlayerSettings withCoinflipStyle(String coinflipStyle) { return new PlayerSettings(chat, msg, bossbar, kitOnDeath, tpaInvites, tpaAuto, mobSpawn, nightVision, coinflipStyle); }
    }
}
