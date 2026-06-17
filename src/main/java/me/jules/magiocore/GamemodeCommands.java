package me.jules.magiocore;

import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GamemodeCommands implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;

        FileConfiguration config = MagioCore.getPlugin(MagioCore.class).getModuleManager().getModuleConfig("gamemode");
        String noPerm = config.getString("messages.no-permission", "§cɢᴀᴍᴇᴍᴏᴅᴇ &#888888» §7Nemáš oprávnění.");

        GameMode targetGM = switch (command.getName().toLowerCase()) {
            case "gmc" -> GameMode.CREATIVE;
            case "gms" -> GameMode.SURVIVAL;
            case "gmsp" -> GameMode.SPECTATOR;
            case "gma" -> GameMode.ADVENTURE;
            default -> null;
        };

        if (targetGM == null) return false;

        String perm = "magiocore." + targetGM.name().toLowerCase();
        if (!player.hasPermission(perm) && !player.isOp()) {
            player.sendMessage(FontUtils.parse(noPerm));
            return true;
        }

        player.setGameMode(targetGM);
        String gmName = targetGM.name().substring(0, 1).toUpperCase() + targetGM.name().substring(1).toLowerCase();
        player.sendMessage(FontUtils.parse(config.getString("messages.changed", "&#00fbffɢᴀᴍᴇᴍᴏᴅᴇ &#888888» §7Tvůj herní mód byl změněn na &#00fbff%gamemode%§7.").replace("%gamemode%", gmName)));

        return true;
    }
}
