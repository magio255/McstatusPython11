package me.jules.magiocore;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class VanishCommand implements CommandExecutor, Listener {
    private final MagioCore plugin;
    private final Set<UUID> vanished = new HashSet<>();

    public VanishCommand(MagioCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;
        if (!player.hasPermission("magiocore.vanish")) {
            player.sendMessage(FontUtils.parse("§c" + "ɴᴇᴍáš ᴏᴘʀáᴠɴěɴí."));
            return true;
        }

        FileConfiguration config = plugin.getModuleManager().getModuleConfig("vanish");
        if (vanished.contains(player.getUniqueId())) {
            vanished.remove(player.getUniqueId());
            for (Player online : Bukkit.getOnlinePlayers()) {
                online.showPlayer(plugin, player);
            }
            player.sendMessage(FontUtils.parse(config.getString("messages.disabled", "§cᴠᴀɴɪsʜ &#888888» §7Nyní jsi viditelný.")));
        } else {
            vanished.add(player.getUniqueId());
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (!online.hasPermission("magiocore.vanish.see")) {
                    online.hidePlayer(plugin, player);
                }
            }
            player.sendMessage(FontUtils.parse(config.getString("messages.enabled", "&#00ff44ᴠᴀɴɪsʜ &#888888» §7Nyní jsi neviditelný.")));

            // Action bar task
            Bukkit.getScheduler().runTaskTimer(plugin, task -> {
                if (!vanished.contains(player.getUniqueId()) || !player.isOnline()) {
                    task.cancel();
                    return;
                }
                player.sendActionBar(FontUtils.parse(config.getString("messages.actionbar", "&#00fbffᴠᴀɴɪsʜ ᴀᴋᴛɪᴠɴí")));
            }, 0, 40L);
        }

        return true;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        for (UUID uuid : vanished) {
            Player v = Bukkit.getPlayer(uuid);
            if (v != null && !player.hasPermission("magiocore.vanish.see")) {
                player.hidePlayer(plugin, v);
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        vanished.remove(event.getPlayer().getUniqueId());
    }
}
