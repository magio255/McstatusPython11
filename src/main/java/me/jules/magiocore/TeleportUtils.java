package me.jules.magiocore;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class TeleportUtils {
    private static final Map<UUID, BukkitRunnable> pendingTeleports = new HashMap<>();

    public static void startTeleportCountdown(Player player, Location target, String prefix, MagioCore plugin, Consumer<Boolean> callback) {
        startTeleportCountdown(player, target, null, prefix, 3, plugin, callback);
    }

    public static void startTeleportCountdown(Player player, Player targetPlayer, String prefix, MagioCore plugin, Consumer<Boolean> callback) {
        startTeleportCountdown(player, null, targetPlayer, prefix, 3, plugin, callback);
    }

    private static void startTeleportCountdown(Player player, Location targetLoc, Player targetPlayer, String prefix, int seconds, MagioCore plugin, Consumer<Boolean> callback) {
        cancelPendingTeleport(player);

        Location startLocation = player.getLocation().clone();

        BukkitRunnable task = new BukkitRunnable() {
            int remaining = seconds;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    pendingTeleports.remove(player.getUniqueId());
                    callback.accept(false);
                    return;
                }

                if (player.getWorld() != startLocation.getWorld()) {
                    player.sendMessage(FontUtils.parse("§c" + "ᴛᴇʟᴇᴘᴏʀᴛᴀᴄᴇ ᴢʀᴜšᴇɴᴀ! ᴢᴍěɴɪʟ ᴊsɪ sᴠěᴛ"));
                    cancel();
                    pendingTeleports.remove(player.getUniqueId());
                    callback.accept(false);
                    return;
                }

                if (player.getLocation().distanceSquared(startLocation) > 0.25) { // 0.5 distance limit
                    player.sendMessage(FontUtils.parse("§c" + "ᴛᴇʟᴇᴘᴏʀᴛᴀᴄᴇ ᴢʀᴜšᴇɴᴀ! ᴘᴏʜɴᴜʟ ᴊsɪ sᴇ"));
                    cancel();
                    pendingTeleports.remove(player.getUniqueId());
                    callback.accept(false);
                    return;
                }

                if (targetPlayer != null && !targetPlayer.isOnline()) {
                    player.sendMessage(FontUtils.parse("§c" + "ᴛᴇʟᴇᴘᴏʀᴛᴀᴄᴇ ᴢʀᴜšᴇɴᴀ! ʜʀáč sᴇ ᴏᴅᴘᴏᴊɪʟ"));
                    cancel();
                    pendingTeleports.remove(player.getUniqueId());
                    callback.accept(false);
                    return;
                }

                if (remaining <= 0) {
                    if (targetPlayer != null) {
                        player.teleport(targetPlayer.getLocation(), org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.PLUGIN);
                    } else {
                        player.teleport(targetLoc, org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.PLUGIN);
                    }
                    player.sendMessage(FontUtils.parse("&#00fbff" + "ʙʏʟ ᴊsɪ ᴛᴇʟᴇᴘᴏʀᴛᴏᴠáɴ"));
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                    cancel();
                    pendingTeleports.remove(player.getUniqueId());
                    callback.accept(true);
                    return;
                }

                // Styled action bar: &#37FF00[PREFIX] &#888888▶ §fᴛᴇʟᴇᴘᴏʀᴛᴀᴄᴇ ᴢᴀ &#37FF00[TIME]s
                String bar = "&#37FF00" + prefix.toUpperCase() + " &#888888▶ §fᴛᴇʟᴇᴘᴏʀᴛᴀᴄᴇ ᴢᴀ &#37FF00" + remaining + "s";
                player.sendActionBar(FontUtils.parse(bar));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 1f, 1f);
                remaining--;
            }
        };

        task.runTaskTimer(plugin, 0L, 20L);
        pendingTeleports.put(player.getUniqueId(), task);
    }

    public static void cancelPendingTeleport(Player player) {
        BukkitRunnable existing = pendingTeleports.remove(player.getUniqueId());
        if (existing != null) {
            existing.cancel();
        }
    }
}
