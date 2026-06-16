package me.jules.magiocore;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SpawnCommands implements CommandExecutor {
    private final MagioCore plugin;

    public SpawnCommands(MagioCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (command.getName().equalsIgnoreCase("setspawn")) {
            if (!player.hasPermission("magiocore.admin") && !player.isOp()) {
                player.sendMessage(FontUtils.parse("§c" + "ɴᴇᴍáš ᴏᴘʀáᴠɴěɴí"));
                return true;
            }
            plugin.getConfig().set("spawn", player.getLocation());
            plugin.saveConfig();
            player.sendMessage(FontUtils.parse("&#00ff44" + "sᴘᴀᴡɴ ʙʏʟ ɴᴀsᴛᴀᴠᴇɴ"));
            return true;
        }

        if (command.getName().equalsIgnoreCase("spawn")) {
            Location spawn = plugin.getConfig().getLocation("spawn");
            if (spawn == null) {
                player.sendMessage(FontUtils.parse("§c" + "sᴘᴀᴡɴ ɴᴇɴí ɴᴀsᴛᴀᴠᴇɴ"));
                return true;
            }

            TeleportUtils.startTeleportCountdown(player, spawn, plugin, success -> {});
            return true;
        }

        return false;
    }
}
