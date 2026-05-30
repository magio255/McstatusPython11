package me.jules.magiocore;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class HomeCommands implements CommandExecutor, TabCompleter {
    private final MagioCore plugin;
    private final HomeManager homeManager;

    public HomeCommands(MagioCore plugin, HomeManager homeManager) {
        this.plugin = plugin;
        this.homeManager = homeManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command is only for players.");
            return true;
        }

        if (command.getName().equalsIgnoreCase("sethome")) {
            int number = 1;
            if (args.length > 0) {
                try {
                    number = Integer.parseInt(args[0]);
                } catch (NumberFormatException e) {
                    player.sendMessage(FontUtils.parse("§c" + "POUžITí: /sETHOME [1-7]"));
                    return true;
                }
            }

            if (number < 1 || number > 7) {
                player.sendMessage(FontUtils.parse("§c" + "čísLO DOMOVA MUsí BýT MEZI 1 A 7"));
                return true;
            }

            if (number > PlaytimeUtils.getMaxHomes(player)) {
                player.sendMessage(FontUtils.parse("§c" + "NEMáš OPRáVNěNí NAsTAVIT sI TOLIK DOMOVů. TVůJ LIMIT JE: " + PlaytimeUtils.getMaxHomes(player) + ""));
                return true;
            }

            homeManager.setHome(player.getUniqueId(), number, player.getLocation());
            player.sendMessage(FontUtils.parse("&#00ff44" + "DOMOV #" + number + " BYL NAsTAVEN"));
            return true;
        }

        if (command.getName().equalsIgnoreCase("home")) {
            if (args.length > 0) {
                int number;
                try {
                    number = Integer.parseInt(args[0]);
                } catch (NumberFormatException e) {
                    player.sendMessage(FontUtils.parse("§c" + "POUžITí: /HOME [1-7]"));
                    return true;
                }

                Home home = homeManager.getHome(player.getUniqueId(), number);
                if (home == null) {
                    player.sendMessage(FontUtils.parse("§c" + "TENTO DOMOV NEMáš NAsTAVENý"));
                    return true;
                }

                if (number > PlaytimeUtils.getMaxHomes(player)) {
                    player.sendMessage(FontUtils.parse("§c" + "NEMáš OPRáVNěNí TELEPORTOVAT sE K TOMUTO DOMOVU. LIMIT: " + PlaytimeUtils.getMaxHomes(player) + ""));
                    return true;
                }

                TeleportUtils.startTeleportCountdown(player, home.getLocation(), plugin, success -> {});
                return true;
            } else {
                plugin.getHomeGui().open(player);
                return true;
            }
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return Arrays.asList("1", "2", "3", "4", "5", "6", "7").stream()
                    .filter(s -> s.startsWith(args[0]))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
