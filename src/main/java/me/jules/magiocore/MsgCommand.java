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
import java.util.concurrent.ConcurrentHashMap;

public class MsgCommand implements CommandExecutor, TabCompleter {
    private final MagioCore plugin;
    private final Map<UUID, UUID> lastMessenger = new ConcurrentHashMap<>();
    private final Map<UUID, Set<UUID>> ignoredPlayers = new ConcurrentHashMap<>();
    private final Map<UUID, Set<UUID>> tpaIgnoredPlayers = new ConcurrentHashMap<>();

    public MsgCommand(MagioCore plugin) {
        this.plugin = plugin;
    }

    public boolean isIgnored(UUID player, UUID target) {
        return ignoredPlayers.getOrDefault(player, new HashSet<>()).contains(target);
    }

    public boolean isTpaIgnored(UUID player, UUID target) {
        return tpaIgnoredPlayers.getOrDefault(player, new HashSet<>()).contains(target);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;

        String cmd = command.getName().toLowerCase();

        switch (cmd) {
            case "msg" -> {
                if (args.length < 2) {
                    player.sendMessage(FontUtils.parse("§c" + "ᴘᴏᴜžɪᴛí: /ᴍsɢ <ʜʀáč> <ᴢᴘʀáᴠᴀ>"));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    player.sendMessage(FontUtils.parse("§c" + "ʜʀáč ɴᴇʙʏʟ ɴᴀʟᴇᴢᴇɴ."));
                    return true;
                }
                if (isIgnored(target.getUniqueId(), player.getUniqueId())) {
                    player.sendMessage(FontUtils.parse("§c" + "ᴛᴇɴᴛᴏ ʜʀáč ᴛě ɪɢɴᴏʀᴜᴊᴇ."));
                    return true;
                }
                if (!plugin.getSettingsManager().getSettings(target.getUniqueId()).dms) {
                    player.sendMessage(FontUtils.parse("§c" + "ʜʀáč ᴍá ᴠʏᴘɴᴜᴛé sᴏᴜᴋʀᴏᴍé ᴢᴘʀáᴠʏ."));
                    return true;
                }
                StringBuilder msg = new StringBuilder();
                for (int i = 1; i < args.length; i++) msg.append(args[i]).append(" ");
                sendMessage(player, target, msg.toString().trim());
            }
            case "reply", "r" -> {
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
                if (isIgnored(target.getUniqueId(), player.getUniqueId())) {
                    player.sendMessage(FontUtils.parse("§c" + "ᴛᴇɴᴛᴏ ʜʀáč ᴛě ɪɢɴᴏʀᴜᴊᴇ."));
                    return true;
                }
                if (!plugin.getSettingsManager().getSettings(target.getUniqueId()).dms) {
                    player.sendMessage(FontUtils.parse("§c" + "ʜʀáč ᴍá ᴠʏᴘɴᴜᴛé sᴏᴜᴋʀᴏᴍé ᴢᴘʀáᴠʏ."));
                    return true;
                }
                StringBuilder msg = new StringBuilder();
                for (String arg : args) msg.append(arg).append(" ");
                sendMessage(player, target, msg.toString().trim());
            }
            case "ignore" -> {
                if (args.length < 1) {
                    player.sendMessage(FontUtils.parse("§c" + "ᴘᴏᴜžɪᴛí: /ɪɢɴᴏʀᴇ <ʜʀáč>"));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    player.sendMessage(FontUtils.parse("§c" + "ʜʀáč ɴᴇʙʏʟ ɴᴀʟᴇᴢᴇɴ."));
                    return true;
                }
                UUID targetId = target.getUniqueId();
                Set<UUID> ignored = ignoredPlayers.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>());
                if (ignored.contains(targetId)) {
                    ignored.remove(targetId);
                    player.sendMessage(FontUtils.parse("&#00fbff" + "ʜʀáč " + target.getName() + " ᴊɪž ɴᴇɴí ɪɢɴᴏʀᴏᴠáɴ."));
                } else {
                    ignored.add(targetId);
                    player.sendMessage(FontUtils.parse("&#00fbff" + "ɴʏɴí ɪɢɴᴏʀᴜᴊᴇš ʜʀáčᴇ " + target.getName() + "."));
                }
            }
            case "tpaignore" -> {
                if (args.length < 1) {
                    player.sendMessage(FontUtils.parse("§c" + "ᴘᴏᴜžɪᴛí: /ᴛᴘᴀɪɢɴᴏʀᴇ <ʜʀáč>"));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    player.sendMessage(FontUtils.parse("§c" + "ʜʀáč ɴᴇʙʏʟ ɴᴀʟᴇᴢᴇɴ."));
                    return true;
                }
                UUID targetId = target.getUniqueId();
                Set<UUID> tpaIgnored = tpaIgnoredPlayers.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>());
                if (tpaIgnored.contains(targetId)) {
                    tpaIgnored.remove(targetId);
                    player.sendMessage(FontUtils.parse("&#00fbff" + "ʜʀáč " + target.getName() + " ᴊɪž ɴᴇɴí ɪɢɴᴏʀᴏᴠáɴ ᴘʀᴏ ᴛᴘᴀ."));
                } else {
                    tpaIgnored.add(targetId);
                    player.sendMessage(FontUtils.parse("&#00fbff" + "ɴʏɴí ɪɢɴᴏʀᴜᴊᴇš ᴛᴘᴀ žáᴅᴏsᴛɪ ᴏᴅ ʜʀáčᴇ " + target.getName() + "."));
                }
            }
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
        if (args.length == 1 && (command.getName().equalsIgnoreCase("msg") || command.getName().equalsIgnoreCase("ignore") || command.getName().equalsIgnoreCase("tpaignore"))) {
            return null; // Online players
        }
        return new ArrayList<>();
    }
}
