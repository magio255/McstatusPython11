package me.jules.magiocore;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class VanishCommand implements CommandExecutor, Listener {
    private final MagioCore plugin;
    private final Set<UUID> vanishedPlayers = new HashSet<>();

    public VanishCommand(MagioCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;
        if (!player.hasPermission("magiocore.vanish")) {
            player.sendMessage(FontUtils.parse("§c" + "ɴᴇᴍáš ᴘřísᴛᴜᴘ ᴋ ᴛᴏᴍᴜᴛᴏ ᴘříᴋᴀᴢᴜ."));
            return true;
        }

        UUID uuid = player.getUniqueId();
        if (vanishedPlayers.contains(uuid)) {
            unvanish(player);
            player.sendMessage(FontUtils.parse("&#00fbff" + "ᴠᴀɴɪsʜ ʙʏʟ &ᴄᴠʏᴘɴᴜᴛ."));
        } else {
            vanish(player);
            player.sendMessage(FontUtils.parse("&#00fbff" + "ᴠᴀɴɪsʜ ʙʏʟ &ᴀᴢᴀᴘɴᴜᴛ."));
        }

        return true;
    }

    private void vanish(Player player) {
        vanishedPlayers.add(player.getUniqueId());
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, -1, 0, false, false));
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.equals(player)) continue;
            if (!online.hasPermission("magiocore.vanish.see")) {
                online.hidePlayer(plugin, player);
            }
        }
    }

    private void unvanish(Player player) {
        vanishedPlayers.remove(player.getUniqueId());
        player.removePotionEffect(PotionEffectType.INVISIBILITY);
        for (Player online : Bukkit.getOnlinePlayers()) {
            online.showPlayer(plugin, player);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // Hide vanished players from the joiner
        for (UUID uuid : vanishedPlayers) {
            Player vanished = Bukkit.getPlayer(uuid);
            if (vanished != null && !player.hasPermission("magiocore.vanish.see")) {
                player.hidePlayer(plugin, vanished);
            }
        }

        // If the joiner is supposed to be vanished (e.g. from previous session, but we don't persist yet)
        // For now, we don't persist vanish across restarts.
    }

    public boolean isVanished(UUID uuid) {
        return vanishedPlayers.contains(uuid);
    }
}
