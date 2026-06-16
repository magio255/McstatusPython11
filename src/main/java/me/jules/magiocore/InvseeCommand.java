package me.jules.magiocore;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class InvseeCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (!player.hasPermission("magiocore.invsee") && !player.isOp()) {
            player.sendMessage(FontUtils.parse("§c" + "ɴᴇᴍáš ᴏᴘʀáᴠɴěɴí"));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(FontUtils.parse("§c" + "ᴘᴏᴜžɪᴛí: /ɪɴᴠsᴇᴇ <ʜʀáč>"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(FontUtils.parse("§c" + "ᴛᴇɴᴛᴏ ʜʀáč ɴᴇɴí ᴏɴʟɪɴᴇ"));
            return true;
        }

        player.openInventory(target.getInventory());
        player.sendMessage(FontUtils.parse("&#00fbff» " + "ᴏᴛᴠᴇᴠřᴇɴ ɪɴᴠᴇɴᴛář ʜʀáčᴇ " + target.getName()));

        return true;
    }
}
