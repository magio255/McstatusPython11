package me.jules.magiocore;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
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

        FileConfiguration config = plugin.getModuleManager().getModuleConfig("spawn");

        if (command.getName().equalsIgnoreCase("setspawn")) {
            if (!player.hasPermission("magiocore.admin") && !player.isOp()) {
                player.sendMessage(FontUtils.parse("§c" + "ɴᴇᴍáš ᴏᴘʀáᴠɴěɴí"));
                return true;
            }
            config.set("location", player.getLocation());
            plugin.getModuleManager().saveModuleConfig("spawn");
            player.sendMessage(FontUtils.parse(config.getString("messages.set", "&#00ff44sᴘᴀᴡɴ ʙʏʟ ɴᴀsᴛᴀᴠᴇɴ")));
            return true;
        }

        if (command.getName().equalsIgnoreCase("spawn")) {
            Location spawn = config.getLocation("location");
            if (spawn == null) {
                player.sendMessage(FontUtils.parse(config.getString("messages.not-set", "§csᴘᴀᴡɴ ɴᴇɴí ɴᴀsᴛᴀᴠᴇɴ")));
                return true;
            }

            player.sendMessage(FontUtils.parse(config.getString("messages.teleporting", "&#00fbffsᴘᴀᴡɴ &#888888» §7Teleportuji...")));
            TeleportUtils.startTeleportCountdown(player, spawn, "sᴘᴀᴡɴ", plugin, success -> {});
            return true;
        }

        return false;
    }
}
