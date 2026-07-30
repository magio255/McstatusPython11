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
            player.sendMessage(FontUtils.parse(config.getString("messages.no-permission", "[#FF1010]ɪɴᴠsᴇᴇ [#888888]» §7ɴᴇᴍáš ᴏᴘʀáᴠɴěɴí.")));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(FontUtils.parse(config.getString("messages.usage", "[#FF1010]ɪɴᴠsᴇᴇ [#888888]» §7ᴘᴏᴜžɪᴛí: /ɪɴᴠsᴇᴇ <ʜʀáč>")));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(FontUtils.parse("[#FF1010]ʜʀáč ɴᴇɴí ᴏɴʟɪɴᴇ."));
            return true;
        }

        player.sendMessage(FontUtils.parse(config.getString("messages.viewing", "[#4498DB]「[#00FBFF]ɪɴᴠsᴇᴇ[#4498DB]」 [#888888]» §7ᴏᴛᴇᴠíʀáᴍ ɪɴᴠᴇɴᴛář ʜʀáčᴇ [#00FBFF]%player%§7.").replace("%player%", target.getName())));
        player.openInventory(target.getInventory());
        return true;
    }
}
