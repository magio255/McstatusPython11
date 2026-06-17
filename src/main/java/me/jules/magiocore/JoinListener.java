package me.jules.magiocore;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;

public class JoinListener implements Listener {
    private final MagioCore plugin;

    public JoinListener(MagioCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        event.joinMessage(null); // Suppress default join message

        FileConfiguration config = plugin.getModuleManager().getModuleConfig("join");

        if (!player.hasPlayedBefore()) {
            String format = config.getString("join-message.first-join.format", "&#00fbff%player% §7se poprvé připojil!");
            Component msg = FontUtils.parse(format.replace("%player%", player.getName()));
            Bukkit.broadcast(msg);

            if (config.getBoolean("join-message.show-head", true)) {
                sendHeadMessage(player, config.getStringList("join-message.first-join.side-messages"));
            }

            // Starter Kit logic
            if (config.getBoolean("starter-kit.enabled", true)) {
                for (String entry : config.getStringList("starter-kit.items")) {
                    try {
                        String[] parts = entry.split(":");
                        org.bukkit.Material mat = org.bukkit.Material.valueOf(parts[0]);
                        int amount = Integer.parseInt(parts[1]);

                        org.bukkit.inventory.ItemStack item = new org.bukkit.inventory.ItemStack(mat, amount);

                        // Check if it's armor and equip it automatically if slots are empty
                        if (mat.name().endsWith("_HELMET") && player.getInventory().getHelmet() == null) player.getInventory().setHelmet(item);
                        else if (mat.name().endsWith("_CHESTPLATE") && player.getInventory().getChestplate() == null) player.getInventory().setChestplate(item);
                        else if (mat.name().endsWith("_LEGGINGS") && player.getInventory().getLeggings() == null) player.getInventory().setLeggings(item);
                        else if (mat.name().endsWith("_BOOTS") && player.getInventory().getBoots() == null) player.getInventory().setBoots(item);
                        else player.getInventory().addItem(item);

                    } catch (Exception ignored) {}
                }
            }

            // Teleport to spawn on first join
            FileConfiguration spawnConfig = plugin.getModuleManager().getModuleConfig("spawn");
            org.bukkit.Location spawn = spawnConfig.getLocation("location");
            if (spawn != null) {
                player.teleport(spawn);
            }
        } else {
            String format = config.getString("join-message.private-welcome.format", "§7Vítej zpět, &#00fbff%player%§7!");
            Component msg = FontUtils.parse(format.replace("%player%", player.getName()));
            player.sendMessage(msg);

            if (config.getBoolean("join-message.show-head", true)) {
                sendHeadMessage(player, config.getStringList("join-message.private-welcome.side-messages"));
            }
        }
    }

    private void sendHeadMessage(Player player, List<String> sideMessages) {
        SkinUtils.getHeadRows(player).thenAccept(rows -> {
            for (int i = 0; i < 8; i++) {
                Component headRow = rows.get(i);
                String sideText = (i < sideMessages.size()) ? sideMessages.get(i).replace("%player%", player.getName()) : "";
                Component line = headRow.append(Component.text("  ")).append(FontUtils.parse(sideText));
                player.sendMessage(line);
            }
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        event.quitMessage(null); // Suppress default quit message
    }
}
