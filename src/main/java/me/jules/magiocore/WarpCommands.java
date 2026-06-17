package me.jules.magiocore;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class WarpCommands implements CommandExecutor, TabCompleter {
    private final MagioCore plugin;
    private final WarpManager manager;

    public WarpCommands(MagioCore plugin, WarpManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        FileConfiguration config = plugin.getModuleManager().getModuleConfig("warp");

        if (command.getName().equalsIgnoreCase("setwarp")) {
            if (!(sender instanceof Player player)) return true;
            if (!player.hasPermission("magiocore.admin") && !player.isOp()) {
                player.sendMessage(FontUtils.parse(config.getString("messages.no-permission", "§cᴡᴀʀᴘ &#888888» §7Nemáš oprávnění.")));
                return true;
            }
            if (args.length == 0) {
                player.sendMessage(FontUtils.parse(config.getString("messages.usage-set", "§cᴡᴀʀᴘ &#888888» §7Použití: /setwarp <název>")));
                return true;
            }
            manager.setWarp(args[0], player.getLocation());
            player.sendMessage(FontUtils.parse(config.getString("messages.set", "&#00ff44ᴡᴀʀᴘ &#888888» §7Warp &#00ff44%name% §7byl nastaven.").replace("%name%", args[0])));
            return true;
        }

        if (command.getName().equalsIgnoreCase("delwarp")) {
            if (!(sender instanceof Player player)) return true;
            if (!player.hasPermission("magiocore.admin") && !player.isOp()) {
                player.sendMessage(FontUtils.parse(config.getString("messages.no-permission", "§cᴡᴀʀᴘ &#888888» §7Nemáš oprávnění.")));
                return true;
            }
            if (args.length == 0) {
                player.sendMessage(FontUtils.parse(config.getString("messages.usage-del", "§cᴡᴀʀᴘ &#888888» §7Použití: /delwarp <název>")));
                return true;
            }
            if (manager.getWarp(args[0]) == null) {
                player.sendMessage(FontUtils.parse(config.getString("messages.not-found", "§cᴡᴀʀᴘ &#888888» §7Warp nebyl nalezen.")));
                return true;
            }
            manager.deleteWarp(args[0]);
            player.sendMessage(FontUtils.parse(config.getString("messages.deleted", "§cᴡᴀʀᴘ &#888888» §7Warp &#00fbff%name% §7byl smazán.").replace("%name%", args[0])));
            return true;
        }

        if (command.getName().equalsIgnoreCase("warp")) {
            if (!(sender instanceof Player player)) return true;
            if (args.length == 0) {
                showWarpList(player);
                return true;
            }

            Warp warp = manager.getWarp(args[0]);
            if (warp == null) {
                player.sendMessage(FontUtils.parse(config.getString("messages.not-found", "§cᴡᴀʀᴘ &#888888» §7Warp nebyl nalezen.")));
                return true;
            }

            player.sendMessage(FontUtils.parse(config.getString("messages.teleport", "&#00fbffᴡᴀʀᴘ &#888888» §7Teleportuji na warp &#00fbff%name%§7...").replace("%name%", warp.getName())));
            TeleportUtils.startTeleportCountdown(player, warp.getLocation(), "ᴡᴀʀᴘ", plugin, success -> {});
            return true;
        }

        return false;
    }

    private void showWarpList(Player player) {
        FileConfiguration config = plugin.getModuleManager().getModuleConfig("warp");
        List<Warp> warps = new ArrayList<>(manager.getWarps());
        if (warps.isEmpty()) {
            player.sendMessage(FontUtils.parse(config.getString("messages.not-found", "§cᴡᴀʀᴘ &#888888» §7Žádné warpy nejsou nastaveny.")));
            return;
        }

        player.sendMessage(FontUtils.parse(config.getString("messages.list-header", "&#EA427F» ʟɪsᴛ ᴡᴀʀᴘů")));
        for (Warp warp : warps) {
            String format = config.getString("messages.list-format", " &#888888• &#00fbff%name%").replace("%name%", warp.getName());
            String hover = config.getString("messages.list-hover", "&#00fbffᴋʟɪᴋɴɪ ᴘʀᴏ ᴛᴇʟᴇᴘᴏʀᴛ");

            Component line = FontUtils.parse(format)
                    .hoverEvent(HoverEvent.showText(FontUtils.parse(hover)))
                    .clickEvent(ClickEvent.runCommand("/warp " + warp.getName()));
            player.sendMessage(line);
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return manager.getWarps().stream()
                    .map(Warp::getName)
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
