package me.jules.magiocore.modules;

import me.jules.magiocore.FontUtils;
import me.jules.magiocore.MagioCore;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class IgnoreModule implements CommandExecutor {
    private final MagioCore plugin;
    private final Map<UUID, Set<UUID>> ignoredPlayers = new HashMap<>();
    private final Set<UUID> tpaIgnored = new HashSet<>();

    public IgnoreModule(MagioCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (command.getName().equalsIgnoreCase("ignore")) {
            if (args.length == 0) {
                player.sendMessage(FontUtils.parse("§cᴘᴏᴜžɪᴛí: /ɪɢɴᴏʀᴇ <ʜʀáč>"));
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage(FontUtils.parse("§cʜʀáč ɴᴇɴí ᴏɴʟɪɴᴇ."));
                return true;
            }

            UUID playerUUID = player.getUniqueId();
            UUID targetUUID = target.getUniqueId();

            Set<UUID> ignored = ignoredPlayers.computeIfAbsent(playerUUID, k -> new HashSet<>());
            if (ignored.contains(targetUUID)) {
                ignored.remove(targetUUID);
                player.sendMessage(FontUtils.parse("&#00ff44ᴜž ɴᴇɪɢɴᴏʀᴜᴊᴇš ʜʀáčᴇ " + target.getName()));
            } else {
                ignored.add(targetUUID);
                player.sendMessage(FontUtils.parse("§cɴʏɴí ɪɢɴᴏʀᴜᴊᴇš ʜʀáčᴇ " + target.getName()));
            }
            return true;
        }

        if (command.getName().equalsIgnoreCase("tpaignore")) {
            UUID uuid = player.getUniqueId();
            if (tpaIgnored.contains(uuid)) {
                tpaIgnored.remove(uuid);
                player.sendMessage(FontUtils.parse("&#00ff44žáᴅᴏsᴛɪ ᴏ ᴛᴇʟᴇᴘᴏʀᴛ ᴊsᴏᴜ ɴʏɴí ᴘᴏᴠᴏʟᴇɴʏ."));
            } else {
                tpaIgnored.add(uuid);
                player.sendMessage(FontUtils.parse("§cžáᴅᴏsᴛɪ ᴏ ᴛᴇʟᴇᴘᴏʀᴛ ᴊsᴏᴜ ɴʏɴí ɪɢɴᴏʀᴏᴠáɴʏ."));
            }
            return true;
        }

        return false;
    }

    public boolean isIgnored(UUID player, UUID target) {
        return ignoredPlayers.getOrDefault(player, new HashSet<>()).contains(target);
    }

    public boolean isTpaIgnored(UUID player) {
        return tpaIgnored.contains(player);
    }
}
