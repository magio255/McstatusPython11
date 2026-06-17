package me.jules.magiocore;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MessageCommand implements CommandExecutor {
    private static final Map<UUID, UUID> lastMessaged = new HashMap<>();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        FileConfiguration config = MagioCore.getPlugin(MagioCore.class).getModuleManager().getModuleConfig("msg");

        if (command.getName().equalsIgnoreCase("msg")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Pouze pro hráče.");
                return true;
            }

            if (args.length < 2) {
                player.sendMessage(FontUtils.parse(config.getString("messages.usage", "§cᴘᴏᴜžɪᴛí: /ᴍsɢ <ʜʀáč> <ᴢᴘʀáᴠᴀ>")));
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage(FontUtils.parse(config.getString("messages.offline", "§cʜʀáč ɴᴇɴí ᴏɴʟɪɴᴇ.")));
                return true;
            }

            StringBuilder message = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                message.append(args[i]).append(" ");
            }

            sendPrivateMessage(player, target, message.toString().trim());
            return true;
        }

        if (command.getName().equalsIgnoreCase("reply")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Pouze pro hráče.");
                return true;
            }

            if (args.length < 1) {
                player.sendMessage(FontUtils.parse(config.getString("messages.usage-reply", "§cᴘᴏᴜžɪᴛí: /ʀ <ᴢᴘʀáᴠᴀ>")));
                return true;
            }

            UUID targetUUID = lastMessaged.get(player.getUniqueId());
            if (targetUUID == null) {
                player.sendMessage(FontUtils.parse(config.getString("messages.no-reply", "§cɴᴇᴍáš ᴋᴏᴍᴜ ᴏᴅᴘᴏᴠěᴅěᴛ.")));
                return true;
            }

            Player target = Bukkit.getPlayer(targetUUID);
            if (target == null) {
                player.sendMessage(FontUtils.parse(config.getString("messages.offline", "§cʜʀáč ᴊíž ɴᴇɴí ᴏɴʟɪɴᴇ.")));
                return true;
            }

            StringBuilder message = new StringBuilder();
            for (String arg : args) {
                message.append(arg).append(" ");
            }

            sendPrivateMessage(player, target, message.toString().trim());
            return true;
        }

        return false;
    }

    private void sendPrivateMessage(Player sender, Player target, String message) {
        MagioCore plugin = MagioCore.getPlugin(MagioCore.class);
        FileConfiguration config = plugin.getModuleManager().getModuleConfig("msg");

        if (!plugin.getSettingsManager().getSettings(sender.getUniqueId()).msg()) {
            sender.sendMessage(FontUtils.parse("§cᴍáš ᴠʏᴘɴᴜᴛé sᴏᴜᴋʀᴏᴍé ᴢᴘʀáᴠʏ ᴠ ɴᴀsᴛᴀᴠᴇɴí."));
            return;
        }

        if (!plugin.getSettingsManager().getSettings(target.getUniqueId()).msg()) {
            sender.sendMessage(FontUtils.parse("§cʜʀáč ᴍá ᴠʏᴘɴᴜᴛé sᴏᴜᴋʀᴏᴍé ᴢᴘʀáᴠʏ."));
            return;
        }

        if (plugin.getIgnoreModule().isIgnored(target.getUniqueId(), sender.getUniqueId())) {
            sender.sendMessage(FontUtils.parse(config.getString("messages.ignored", "§cᴛᴇɴᴛᴏ ʜʀáč ᴛě ɪɢɴᴏʀᴜᴊᴇ.")));
            return;
        }

        String senderName = sender.getName();
        String targetName = target.getName();

        String headerFormat = config.getString("format.header", "&#00fbff%sender% &#888888» &#00fbff%receiver%§7: ");
        String contentFormat = config.getString("format.content", "§f%message%");

        Component header = FontUtils.parse(headerFormat.replace("%sender%", senderName).replace("%receiver%", targetName));
        Component content = FontUtils.parse(contentFormat.replace("%message%", message), false);
        Component fullMessage = header.append(content);

        sender.sendMessage(fullMessage);
        target.sendMessage(fullMessage);

        lastMessaged.put(sender.getUniqueId(), target.getUniqueId());
        lastMessaged.put(target.getUniqueId(), sender.getUniqueId());
    }
}
