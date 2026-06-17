package me.jules.magiocore;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class InvseeCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;

        FileConfiguration config = MagioCore.getPlugin(MagioCore.class).getModuleManager().getModuleConfig("invsee");
        if (!player.hasPermission("magiocore.invsee") && !player.isOp()) {
            player.sendMessage(FontUtils.parse(config.getString("messages.no-permission", "§cɪɴᴠsᴇᴇ &#888888» §7Nemáš oprávnění.")));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(FontUtils.parse(config.getString("messages.usage", "§cɪɴᴠsᴇᴇ &#888888» §7Použití: /invsee <hráč>")));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(FontUtils.parse("§c" + "ʜʀáč ɴᴇɴí ᴏɴʟɪɴᴇ."));
            return true;
        }

        player.sendMessage(FontUtils.parse(config.getString("messages.viewing", "&#00fbffɪɴᴠsᴇᴇ &#888888» §7Otevírám inventář hráče &#00fbff%player%§7.").replace("%player%", target.getName())));
        player.openInventory(target.getInventory());
        return true;
    }
}
