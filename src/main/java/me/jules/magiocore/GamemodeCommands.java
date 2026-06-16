package me.jules.magiocore;

import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GamemodeCommands implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;

        String cmd = command.getName().toLowerCase();
        GameMode gm = null;
        String perm = "";

        switch (cmd) {
            case "gmc":
                gm = GameMode.CREATIVE;
                perm = "magiocore.gmc";
                break;
            case "gms":
                gm = GameMode.SURVIVAL;
                perm = "magiocore.gms";
                break;
            case "gmsp":
                gm = GameMode.SPECTATOR;
                perm = "magiocore.gmsp";
                break;
            case "gma":
                gm = GameMode.ADVENTURE;
                perm = "magiocore.gma";
                break;
        }

        if (gm != null) {
            if (!player.hasPermission(perm) && !player.isOp()) {
                player.sendMessage(FontUtils.parse("§c" + "ɴᴇᴍáš ᴏᴘʀáᴠɴěɴí"));
                return true;
            }
            player.setGameMode(gm);
            player.sendMessage(FontUtils.parse("&#00fbff" + "ᴛᴠůᴊ ʜᴇʀɴí ᴍóᴅ ʙʏʟ ᴢᴍěɴěɴ ɴᴀ " + gm.name().toLowerCase() + ""));
        }

        return true;
    }
}
