package me.jules.apexbuild;

import java.util.UUID;

public class JoinEntry {
    private final String playerName;
    private final UUID playerUUID;
    private final String ipAddress;
    private final long timestamp;

    public JoinEntry(String playerName, UUID playerUUID, String ipAddress, long timestamp) {
        this.playerName = playerName;
        this.playerUUID = playerUUID;
        this.ipAddress = ipAddress;
        this.timestamp = timestamp;
    }

    public String getPlayerName() {
        return playerName;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
