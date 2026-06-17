package me.jules.magiocore.modules;

import me.jules.magiocore.FontUtils;
import me.jules.magiocore.MagioCore;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class StaffReportModule implements CommandExecutor {
    private final MagioCore plugin;
    private final List<String> reports = new ArrayList<>();

    public StaffReportModule(MagioCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        FileConfiguration config = plugin.getModuleManager().getModuleConfig("report");
        String noPerm = config.getString("messages.no-permission", "§cɴᴇᴍáš ᴏᴘʀáᴠɴěɴí.");

        if (command.getName().equalsIgnoreCase("staffchat")) {
            if (!sender.hasPermission("staff.chat")) {
                sender.sendMessage(FontUtils.parse(noPerm));
                return true;
            }
            if (args.length == 0) return false;

            String message = String.join(" ", args);
            String format = config.getString("messages.staffchat-format", "§c§lSTAFFCHAT §e%player%§7: §f%message%")
                    .replace("%player%", sender.getName()).replace("%message%", message);

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.hasPermission("staff.chat")) {
                    player.sendMessage(format);
                }
            }
            Bukkit.getConsoleSender().sendMessage(format);
            return true;
        }

        if (command.getName().equalsIgnoreCase("report")) {
            if (!sender.hasPermission("report.use")) {
                sender.sendMessage(FontUtils.parse(config.getString("messages.no-permission-report", "§cɴᴇᴍáš ᴏᴘʀáᴠɴěɴí ᴘᴏᴜžíᴛ /ʀᴇᴘᴏʀᴛ.")));
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage(FontUtils.parse(config.getString("messages.usage-report", "§cᴘᴏᴜžɪᴛí: /ʀᴇᴘᴏʀᴛ <ʜʀáč> <ᴅůᴠᴏᴅ>")));
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(FontUtils.parse(config.getString("messages.offline", "§cᴛᴇɴᴛᴏ ʜʀáč ɴᴇɴí ᴏɴʟɪɴᴇ.")));
                return true;
            }

            String reason = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length)).trim();

            reports.add(sender.getName() + " nahlásil " + target.getName() + ": " + reason);
            sender.sendMessage(FontUtils.parse(config.getString("messages.report-sent", "&#00ff44ɴᴀʜʟásɪʟ ᴊsɪ ʜʀáčᴇ &#f1c40f%target% &#00ff44s ᴅůᴠᴏᴅᴇᴍ: §f%reason%")
                    .replace("%target%", target.getName()).replace("%reason%", reason)));

            String broadcast = config.getString("messages.report-broadcast", "§c§lREPORT §e%player% §7nahlásil §e%target%§7: §f%reason%")
                    .replace("%player%", sender.getName()).replace("%target%", target.getName()).replace("%reason%", reason);
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.hasPermission("report.check")) {
                    player.sendMessage(FontUtils.parse(broadcast));
                }
            }
            return true;
        }

        if (command.getName().equalsIgnoreCase("checkreport")) {
            if (!sender.hasPermission("report.check")) {
                sender.sendMessage(FontUtils.parse(noPerm));
                return true;
            }
            if (reports.isEmpty()) {
                sender.sendMessage(FontUtils.parse(config.getString("messages.no-reports", "§7žáᴅɴé ʀᴇᴘᴏʀᴛʏ ɴᴇᴊsᴏᴜ ᴋ ᴅɪsᴘᴏᴢɪᴄɪ.")));
                return true;
            }
            sender.sendMessage(FontUtils.parse(config.getString("messages.reports-header", "&#f1c40f===== ʀᴇᴘᴏʀᴛʏ =====")));
            for (String report : reports) {
                sender.sendMessage(FontUtils.parse("&#f1c40f" + report));
            }
            sender.sendMessage(FontUtils.parse(config.getString("messages.reports-footer", "&#f1c40f===================")));
            return true;
        }

        if (command.getName().equalsIgnoreCase("clearreports")) {
            if (!sender.hasPermission("report.admin")) {
                sender.sendMessage(FontUtils.parse(noPerm));
                return true;
            }
            reports.clear();
            sender.sendMessage(FontUtils.parse(config.getString("messages.reports-cleared", "&#00ff44ᴠšᴇᴄʜɴʏ ʀᴇᴘᴏʀᴛʏ ʙʏʟʏ sᴍᴀᴢáɴʏ.")));
            return true;
        }

        return false;
    }
}
