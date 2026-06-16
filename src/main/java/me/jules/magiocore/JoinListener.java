package me.jules.magiocore;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
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
        boolean firstJoin = !player.hasPlayedBefore();

        boolean showHead = plugin.getConfig().getBoolean("join-message.show-head", true);

        String configPath = firstJoin ? "join-message.first-join" : "join-message.private-welcome";
        String format = plugin.getConfig().getString(configPath + ".format");
        List<String> sideMessages = plugin.getConfig().getStringList(configPath + ".side-messages");

        // Disable default join message
        event.joinMessage(null);

        if (showHead) {
            SkinUtils.getHeadRows(player).thenAccept(rows -> {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    for (int i = 0; i < 8; i++) {
                        Component row = rows.get(i);
                        String sideText = "";
                        if (i < sideMessages.size()) {
                            sideText = " " + sideMessages.get(i).replace("%player%", player.getName());
                        } else if (i == 4 && sideMessages.isEmpty()) {
                            sideText = " " + format.replace("%player%", player.getName());
                        }

                        Component finalRow = row.append(FontUtils.parse(sideText));

                        if (firstJoin) {
                            Bukkit.broadcast(finalRow);
                        } else {
                            player.sendMessage(finalRow);
                        }
                    }
                });
            });
        } else {
            Component msg = FontUtils.parse(format.replace("%player%", player.getName()));
            if (firstJoin) {
                Bukkit.broadcast(msg);
            } else {
                player.sendMessage(msg);
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        event.quitMessage(null);
    }
}
