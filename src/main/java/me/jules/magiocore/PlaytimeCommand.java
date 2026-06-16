package me.jules.magiocore;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlaytimeCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length > 0) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(FontUtils.parse("§c" + "ᴛᴇɴᴛᴏ ʜʀáč ɴᴇɴí ᴏɴʟɪɴᴇ"));
                return true;
            }
            sender.sendMessage(FontUtils.parse("&#00fbffʜʀáč §f" + target.getName() + " &#00fbffɴᴀʜʀáʟ: §f" + PlaytimeUtils.formatPlaytime(target) + ""));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Tento prikaz je jen pro hrace.");
            return true;
        }

        player.sendMessage(FontUtils.parse("&#00fbffᴛᴠůᴊ ᴏᴅᴇʜʀᴀɴý čᴀs: §f" + PlaytimeUtils.formatPlaytime(player) + ""));
        return true;
    }
}
