package me.jules.apexbuild;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public class JoinListener implements Listener {
    private final DataManager dataManager;

    public JoinListener(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        String name = event.getPlayer().getName();
        java.util.UUID uuid = event.getPlayer().getUniqueId();

        InetSocketAddress address = event.getPlayer().getAddress();
        String ip = "Unknown";
        if (address != null) {
            InetAddress inetAddress = address.getAddress();
            if (inetAddress != null) {
                ip = inetAddress.getHostAddress();
            }
        }

        long timestamp = System.currentTimeMillis();

        dataManager.addEntry(new JoinEntry(name, uuid, ip, timestamp));
    }
}
