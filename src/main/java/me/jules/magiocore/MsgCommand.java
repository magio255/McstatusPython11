package me.jules.magiocore;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Set;
import java.util.HashSet;

public class MsgCommand implements CommandExecutor, TabCompleter {
    private final MagioCore plugin;
    private final Map<UUID, UUID> lastMessenger = new HashMap<>();
    private final Set<UUID> ignoredPlayers = new HashSet<>(); // Simplified for now

    public MsgCommand(MagioCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;

        String cmd = command.getName().toLowerCase();

        if (cmd.equals("msg")) {
            if (args.length < 2) {
                player.sendMessage(FontUtils.parse("§c" + "ᴘᴏᴜžɪᴛí: /ᴍsɢ <ʜʀáč> <ᴢᴘʀáᴠᴀ>"));
                return true;
            }
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage(FontUtils.parse("§c" + "ʜʀáč ɴᴇʙʏʟ ɴᴀʟᴇᴢᴇɴ."));
                return true;
            }

            if (!plugin.getSettingsManager().getSettings(target.getUniqueId()).dms) {
                player.sendMessage(FontUtils.parse("§c" + "ʜʀáč ᴍá ᴠʏᴘɴᴜᴛé sᴏᴜᴋʀᴏᴍé ᴢᴘʀáᴠʏ."));
                return true;
            }

            StringBuilder msg = new StringBuilder();
            for (int i = 1; i < args.length; i++) msg.append(args[i]).append(" ");

            sendMessage(player, target, msg.toString().trim());
            return true;
        }

        if (cmd.equals("reply") || cmd.equals("r")) {
            if (args.length < 1) {
                player.sendMessage(FontUtils.parse("§c" + "ᴘᴏᴜžɪᴛí: /ʀ <ᴢᴘʀáᴠᴀ>"));
                return true;
            }
            UUID targetUUID = lastMessenger.get(player.getUniqueId());
            if (targetUUID == null) {
                player.sendMessage(FontUtils.parse("§c" + "ɴᴇᴍáš ᴋᴏᴍᴜ ᴏᴅᴘᴏᴠěᴅěᴛ."));
                return true;
            }
            Player target = Bukkit.getPlayer(targetUUID);
            if (target == null) {
                player.sendMessage(FontUtils.parse("§c" + "ʜʀáč ᴊᴇ ᴏꜰꜰʟɪɴᴇ."));
                return true;
            }

            if (!plugin.getSettingsManager().getSettings(target.getUniqueId()).dms) {
                player.sendMessage(FontUtils.parse("§c" + "ʜʀáč ᴍá ᴠʏᴘɴᴜᴛé sᴏᴜᴋʀᴏᴍé ᴢᴘʀáᴠʏ."));
                return true;
            }

            StringBuilder msg = new StringBuilder();
            for (String arg : args) msg.append(arg).append(" ");

            sendMessage(player, target, msg.toString().trim());
            return true;
        }

        return true;
    }

    private void sendMessage(Player from, Player to, String message) {
        String fromFormat = "&#EA427Fᴊá &#888888» &#EA427F" + to.getName() + " §8| §f" + message;
        String toFormat = "&#EA427F" + from.getName() + " &#888888» &#EA427Fᴊá §8| §f" + message;

        from.sendMessage(FontUtils.parse(fromFormat));
        to.sendMessage(FontUtils.parse(toFormat));

        lastMessenger.put(from.getUniqueId(), to.getUniqueId());
        lastMessenger.put(to.getUniqueId(), from.getUniqueId());
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1 && command.getName().equalsIgnoreCase("msg")) {
            return null; // Online players
        }
        return new ArrayList<>();
    }
}
