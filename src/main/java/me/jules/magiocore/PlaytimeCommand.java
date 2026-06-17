package me.jules.magiocore;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.Statistic;
import org.jetbrains.annotations.NotNull;

public class PlaytimeCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;

        FileConfiguration config = MagioCore.getPlugin(MagioCore.class).getModuleManager().getModuleConfig("playtime");
        if (args.length == 0) {
            String time = PlaytimeUtils.formatPlaytime(player);
            player.sendMessage(FontUtils.parse(config.getString("messages.own", "&#00fbffᴘʟᴀʏᴛɪᴍᴇ &#888888» §7Tvůj odehraný čas je &#00fbff%time%§7.").replace("%time%", time)));
        } else {
            Player target = Bukkit.getPlayer(args[0]);
            if (target != null) {
                String time = PlaytimeUtils.formatPlaytime(target);
                player.sendMessage(FontUtils.parse(config.getString("messages.others", "&#00fbffᴘʟᴀʏᴛɪᴍᴇ &#888888» §7Hráč &#00fbff%player% §7odehrál &#00fbff%time%§7.").replace("%player%", target.getName()).replace("%time%", time)));
            } else {
                player.sendMessage(FontUtils.parse(config.getString("messages.never-joined", "§cᴘʟᴀʏᴛɪᴍᴇ &#888888» §7Tento hráč tu nikdy nehrál.")));
            }
        }

        return true;
    }
}
