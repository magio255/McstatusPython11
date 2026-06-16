package me.jules.magiocore;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class TpaManager {
    private final MagioCore plugin;
    private final File file;
    private final FileConfiguration config;
    private final Map<UUID, TpaRequest> pendingRequests = new HashMap<>(); // Key: target (receiver)
    private final Map<UUID, UUID> sentRequests = new HashMap<>(); // Key: requester, Value: target
    private final Set<UUID> tpaOff = new HashSet<>();

    public TpaManager(MagioCore plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "tpa.yml");
        if (!file.exists()) {
            plugin.getDataFolder().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.config = YamlConfiguration.loadConfiguration(file);
        loadTpaOff();
    }

    private void loadTpaOff() {
        if (config.getStringList("tpa-off") == null) return;
        for (String uuidStr : config.getStringList("tpa-off")) {
            tpaOff.add(UUID.fromString(uuidStr));
        }
    }

    private void saveTpaOff() {
        Set<String> uuidStrings = new HashSet<>();
        for (UUID uuid : tpaOff) {
            uuidStrings.add(uuid.toString());
        }
        config.set("tpa-off", uuidStrings.stream().toList());
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class TpaRequest {
        public final UUID requester;
        public final String type; // "to" or "here"
        public final long timestamp;

        public TpaRequest(UUID requester, String type) {
            this.requester = requester;
            this.type = type;
            this.timestamp = System.currentTimeMillis();
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - timestamp > 60000; // 60 seconds
        }
    }

    public void sendRequest(UUID requester, UUID target, String type) {
        pendingRequests.put(target, new TpaRequest(requester, type));
        sentRequests.put(requester, target);
    }

    public TpaRequest getRequest(UUID target) {
        TpaRequest request = pendingRequests.get(target);
        if (request != null && request.isExpired()) {
            pendingRequests.remove(target);
            sentRequests.remove(request.requester);
            return null;
        }
        return request;
    }

    public void removeRequest(UUID target) {
        TpaRequest request = pendingRequests.remove(target);
        if (request != null) {
            sentRequests.remove(request.requester);
        }
    }

    public UUID getSentRequestTarget(UUID requester) {
        return sentRequests.get(requester);
    }

    public void toggleTpa(UUID uuid) {
        if (tpaOff.contains(uuid)) {
            tpaOff.remove(uuid);
        } else {
            tpaOff.add(uuid);
        }
        saveTpaOff();
    }

    public boolean isTpaOff(UUID uuid) {
        return tpaOff.contains(uuid);
    }
}
