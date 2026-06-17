package me.jules.magiocore;

import me.jules.magiocore.modules.SocialsModule;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.WeatherType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.bukkit.Location;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class UtilityCommands implements CommandExecutor, TabCompleter {
    private final MagioCore plugin;
    private final Map<UUID, Long> bookCooldown = new HashMap<>();
    private final Map<UUID, Long> compassCooldown = new HashMap<>();

    public UtilityCommands(MagioCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        String cmd = command.getName().toLowerCase();
        FileConfiguration config = plugin.getModuleManager().getModuleConfig("utilities");
        String noPerm = config.getString("messages.no-permission", "§cɴᴇᴍáš ᴘřísᴛᴜᴘ ᴋ ᴛᴏᴍᴜᴛᴏ ᴘříᴋᴀᴢᴜ.");

        switch (cmd) {
            case "afk" -> {
                if (!(sender instanceof Player player)) return true;
                FileConfiguration afkConfig = plugin.getModuleManager().getModuleConfig("afkzone");
                Location afkLoc = afkConfig.getLocation("location");
                if (afkLoc == null) {
                    player.sendMessage(FontUtils.parse(config.getString("messages.afk-not-set", "§cᴀꜰᴋ ᴢóɴᴀ ɴᴇɴí ɴᴀsᴛᴀᴠᴇɴᴀ.")));
                    return true;
                }
                player.sendMessage(FontUtils.parse(config.getString("messages.afk-teleporting", "&#00fbffᴀꜰᴋ &#888888» §7Teleportuji do AFK zóny...")));
                TeleportUtils.startTeleportCountdown(player, afkLoc, "ᴀꜰᴋ", plugin, success -> {});
            }
            case "setafk" -> {
                if (!(sender instanceof Player player)) return true;
                if (!player.hasPermission("magiocore.admin") && !player.isOp()) {
                    player.sendMessage(FontUtils.parse(noPerm));
                    return true;
                }
                FileConfiguration afkConfig = plugin.getModuleManager().getModuleConfig("afkzone");
                afkConfig.set("location", player.getLocation());
                plugin.getModuleManager().saveModuleConfig("afkzone");
                player.sendMessage(FontUtils.parse("&#00ff44" + "ᴀꜰᴋ ᴢóɴᴀ ʙʏʟᴀ ɴᴀsᴛᴀᴠᴇɴᴀ."));
            }
            case "book" -> {
                if (!(sender instanceof Player player)) return true;
                if (checkCooldown(player, bookCooldown)) {
                    player.getInventory().addItem(new ItemStack(Material.WRITABLE_BOOK));
                    player.sendMessage(FontUtils.parse(config.getString("messages.book-give", "&#00fbffᴅᴏsᴛᴀʟ ᴊsɪ ᴘsᴀᴄí ᴋɴížᴋᴜ.")));
                }
            }
            case "compass" -> {
                if (!(sender instanceof Player player)) return true;
                if (checkCooldown(player, compassCooldown)) {
                    player.getInventory().addItem(new ItemStack(Material.COMPASS));
                    player.sendMessage(FontUtils.parse(config.getString("messages.compass-give", "&#00fbffᴅᴏsᴛᴀʟ ᴊsɪ ᴋᴏᴍᴘᴀs.")));
                }
            }
            case "broadcast" -> {
                if (!sender.hasPermission("magiocore.broadcast")) {
                    sender.sendMessage(FontUtils.parse(noPerm));
                    return true;
                }
                if (args.length == 0) return false;
                String message = String.join(" ", args);
                String prefix = config.getString("messages.broadcast-prefix", "&#ffbb00ᴏᴢɴáᴍᴇɴí &#888888» §f");
                Bukkit.broadcast(FontUtils.parse(prefix + message));
            }
            case "feed" -> {
                if (!sender.hasPermission("magiocore.feed")) {
                    sender.sendMessage(FontUtils.parse(noPerm));
                    return true;
                }
                Player target = (args.length > 0) ? Bukkit.getPlayer(args[0]) : (sender instanceof Player ? (Player) sender : null);
                if (target == null) {
                    sender.sendMessage(FontUtils.parse("§c" + "ʜʀáč ɴᴇʙʏʟ ɴᴀʟᴇᴢᴇɴ."));
                    return true;
                }
                target.setFoodLevel(20);
                target.setSaturation(20);
                if (target.equals(sender)) {
                    sender.sendMessage(FontUtils.parse(config.getString("messages.feed-self", "&#00fbffɴᴀsʏᴄᴇɴí ʙʏʟᴏ ᴅᴏᴘʟɴěɴᴏ.")));
                } else {
                    sender.sendMessage(FontUtils.parse(config.getString("messages.feed-others", "&#00fbffɴᴀsʏᴄᴇɴí ʜʀáčᴇ %player% ʙʏʟᴏ ᴅᴏᴘʟɴěɴᴏ.").replace("%player%", target.getName())));
                }
            }
            case "fly" -> {
                if (!sender.hasPermission("magiocore.fly")) {
                    sender.sendMessage(FontUtils.parse(noPerm));
                    return true;
                }
                Player target;
                boolean toggle;
                if (args.length == 0) {
                    if (!(sender instanceof Player p)) return true;
                    target = p;
                    toggle = !target.getAllowFlight();
                } else if (args.length == 1) {
                    if (args[0].equalsIgnoreCase("on")) {
                        if (!(sender instanceof Player p)) return true;
                        target = p; toggle = true;
                    } else if (args[0].equalsIgnoreCase("off")) {
                        if (!(sender instanceof Player p)) return true;
                        target = p; toggle = false;
                    } else {
                        target = Bukkit.getPlayer(args[0]);
                        if (target == null) {
                            sender.sendMessage(FontUtils.parse("§c" + "ʜʀáč ɴᴇʙʏʟ ɴᴀʟᴇᴢᴇɴ."));
                            return true;
                        }
                        toggle = !target.getAllowFlight();
                    }
                } else {
                    target = Bukkit.getPlayer(args[0]);
                    if (target == null) {
                        sender.sendMessage(FontUtils.parse("§c" + "ʜʀáč ɴᴇʙʏʟ ɴᴀʟᴇᴢᴇɴ."));
                        return true;
                    }
                    toggle = args[1].equalsIgnoreCase("on");
                }
                target.setAllowFlight(toggle);
                String flyMsg = toggle ? config.getString("messages.fly-enabled", "&#00fbffʟéᴛáɴí ᴘʀᴏ %player% ʙʏʟᴏ ᴢᴀᴘɴᴜᴛᴏ.") : config.getString("messages.fly-disabled", "&#00fbffʟéᴛáɴí ᴘʀᴏ %player% ʙʏʟᴏ ᴠʏᴘɴᴜᴛᴏ.");
                sender.sendMessage(FontUtils.parse(flyMsg.replace("%player%", target.getName())));
            }
            case "hat" -> {
                if (!(sender instanceof Player player)) return true;
                if (!player.hasPermission("magiocore.hat")) {
                    player.sendMessage(FontUtils.parse(noPerm));
                    return true;
                }
                if (args.length > 0 && args[0].equalsIgnoreCase("remove")) {
                    player.getInventory().setHelmet(null);
                    player.sendMessage(FontUtils.parse(config.getString("messages.hat-removed", "&#00fbffᴘřᴇᴅᴍěᴛ ᴢ ʜʟᴀᴠʏ ʙʏʟ ᴏᴅsᴛʀᴀɴěɴ.")));
                    return true;
                }
                ItemStack hand = player.getInventory().getItemInMainHand();
                if (hand.getType().isAir()) {
                    player.sendMessage(FontUtils.parse("§c" + "ᴍᴜsíš ᴅʀžᴇᴛ ᴘřᴇᴅᴍěᴛ ᴠ ʀᴜᴄᴇ."));
                    return true;
                }
                ItemStack head = player.getInventory().getHelmet();
                player.getInventory().setHelmet(hand);
                player.getInventory().setItemInMainHand(head);
                player.sendMessage(FontUtils.parse(config.getString("messages.hat-set", "&#00fbffɴʏɴí ᴍáš ɴᴀ ʜʟᴀᴠě sᴠůj ᴘřᴇᴅᴍěᴛ.")));
            }
            case "heal" -> {
                if (!sender.hasPermission("magiocore.heal")) {
                    sender.sendMessage(FontUtils.parse(noPerm));
                    return true;
                }
                Player target = (args.length > 0) ? Bukkit.getPlayer(args[0]) : (sender instanceof Player ? (Player) sender : null);
                if (target == null) {
                    sender.sendMessage(FontUtils.parse("§c" + "ʜʀáč ɴᴇʙʏʟ ɴᴀʟᴇᴢᴇɴ."));
                    return true;
                }
                target.setHealth(target.getMaxHealth());
                target.setFoodLevel(20);
                target.setFireTicks(0);
                target.getActivePotionEffects().forEach(effect -> target.removePotionEffect(effect.getType()));
                if (target.equals(sender)) {
                    sender.sendMessage(FontUtils.parse(config.getString("messages.heal-self", "&#00fbffʙʏʟ ᴊsɪ ᴠʏʟéčᴇɴ.")));
                } else {
                    sender.sendMessage(FontUtils.parse(config.getString("messages.heal-others", "&#00fbffʜʀáč %player% ʙʏʟ ᴠʏʟéčᴇɴ.").replace("%player%", target.getName())));
                }
            }
            case "repair" -> {
                if (!(sender instanceof Player player)) return true;
                if (!player.hasPermission("magiocore.repair")) {
                    player.sendMessage(FontUtils.parse(noPerm));
                    return true;
                }
                if (args.length > 0 && args[0].equalsIgnoreCase("all")) {
                    for (ItemStack item : player.getInventory().getContents()) {
                        repairItem(item);
                    }
                    player.sendMessage(FontUtils.parse(config.getString("messages.repair-all", "&#00fbffᴠšᴇᴄʜɴʏ ᴘřᴇᴅᴍěᴛʏ ʙʏʟʏ ᴏᴘʀᴀᴠᴇɴʏ.")));
                } else {
                    ItemStack item = player.getInventory().getItemInMainHand();
                    if (repairItem(item)) {
                        player.sendMessage(FontUtils.parse(config.getString("messages.repair-hand", "&#00fbffᴘřᴇᴅᴍěᴛ ᴠ ʀᴜᴄᴇ ʙʏʟ ᴏᴘʀᴀᴠᴇɴ.")));
                    } else {
                        player.sendMessage(FontUtils.parse("§c" + "ᴛᴇɴᴛᴏ ᴘřᴇᴅᴍěᴛ ɴᴇʟᴢᴇ ᴏᴘʀᴀᴠɪᴛ."));
                    }
                }
            }
            case "suicide" -> {
                if (!(sender instanceof Player player)) return true;
                player.setHealth(0);
                player.sendMessage(FontUtils.parse(config.getString("messages.suicide", "&#00fbffʀᴏᴢʜᴏᴅʟ sᴇs ᴜᴋᴏɴčɪᴛ sᴠůj žɪᴠᴏᴛ.")));
            }
            case "ptime" -> {
                if (!sender.hasPermission("magiocore.ptime")) {
                    sender.sendMessage(FontUtils.parse(noPerm));
                    return true;
                }
                if (args.length == 0) return false;
                Player target = (sender instanceof Player p) ? p : null;
                if (args.length > 1) {
                    target = Bukkit.getPlayer(args[1]);
                }
                if (target == null) {
                    sender.sendMessage(FontUtils.parse("§c" + "ʜʀáč ɴᴇʙʏʟ ɴᴀʟᴇᴢᴇɴ."));
                    return true;
                }
                String timeArg = args[0].toLowerCase();
                boolean fixed = timeArg.startsWith("@");
                if (fixed) timeArg = timeArg.substring(1);

                long time;
                switch (timeArg) {
                    case "day" -> time = 1000;
                    case "night" -> time = 13000;
                    case "dawn" -> time = 23000;
                    case "reset" -> {
                        target.resetPlayerTime();
                        sender.sendMessage(FontUtils.parse("&#00fbff" + "čᴀs ᴘʀᴏ " + target.getName() + " ʙʏʟ ʀᴇsᴇᴛᴏᴠáɴ."));
                        return true;
                    }
                    default -> {
                        try {
                            if (timeArg.endsWith("ticks")) {
                                time = Long.parseLong(timeArg.replace("ticks", ""));
                            } else {
                                time = Long.parseLong(timeArg); // Simplified
                            }
                        } catch (NumberFormatException e) {
                            sender.sendMessage(FontUtils.parse("§c" + "ɴᴇᴘʟᴀᴛɴý ꜰᴏʀᴍáᴛ čᴀsᴜ."));
                            return true;
                        }
                    }
                }
                target.setPlayerTime(time, !fixed);
                sender.sendMessage(FontUtils.parse("&#00fbff" + "čᴀs ᴘʀᴏ " + target.getName() + " ɴᴀsᴛᴀᴠᴇɴ ɴᴀ " + timeArg + "."));
            }
            case "pweather" -> {
                if (!sender.hasPermission("magiocore.pweather")) {
                    sender.sendMessage(FontUtils.parse(noPerm));
                    return true;
                }
                if (args.length == 0) return false;
                Player target = (sender instanceof Player p) ? p : null;
                if (args.length > 1) {
                    target = Bukkit.getPlayer(args[1]);
                }
                if (target == null) {
                    sender.sendMessage(FontUtils.parse("§c" + "ʜʀáč ɴᴇʙʏʟ ɴᴀʟᴇᴢᴇɴ."));
                    return true;
                }
                switch (args[0].toLowerCase()) {
                    case "sun", "clear" -> target.setPlayerWeather(WeatherType.CLEAR);
                    case "storm" -> target.setPlayerWeather(WeatherType.DOWNFALL);
                    case "reset" -> target.resetPlayerWeather();
                    default -> {
                        sender.sendMessage(FontUtils.parse("§c" + "ɴᴇᴘʟᴀᴛɴý ᴛʏᴘ ᴘᴏčᴀsí."));
                        return true;
                    }
                }
                sender.sendMessage(FontUtils.parse("&#00fbff" + "ᴘᴏčᴀsí ᴘʀᴏ " + target.getName() + " ʙʏʟᴏ ᴢᴍěɴěɴᴏ."));
            }
            case "shardshop" -> {
                if (!(sender instanceof Player player)) return true;
                player.sendMessage(FontUtils.parse(config.getString("messages.shardshop-open", "&#00ff44sʜᴀʀᴅ sʜᴏᴘ ᴏᴛᴇᴠŘᴇɴ!")));
            }
        }
        return true;
    }

    private boolean checkCooldown(Player player, Map<UUID, Long> cooldownMap) {
        long now = System.currentTimeMillis();
        long last = cooldownMap.getOrDefault(player.getUniqueId(), 0L);
        long diff = now - last;
        if (diff < 300000) { // 5 minutes
            long remaining = (300000 - diff) / 1000;
            player.sendMessage(FontUtils.parse("§c" + "ᴍᴜsíš ᴘᴏčᴋᴀᴛ ᴊᴇšᴛě " + remaining + "s."));
            return false;
        }
        cooldownMap.put(player.getUniqueId(), now);
        return true;
    }

    private boolean repairItem(ItemStack item) {
        if (item == null || item.getType().isAir()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof Damageable damageable) {
            damageable.setDamage(0);
            item.setItemMeta(meta);
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        String sub = command.getName().toLowerCase();
        if (args.length == 1) {
            if (Arrays.asList("feed", "heal", "fly").contains(sub)) {
                return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                        .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
            }
            if (sub.equals("ptime")) {
                return Arrays.asList("list", "reset", "day", "night", "dawn", "ticks").stream()
                        .filter(s -> s.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
            }
            if (sub.equals("pweather")) {
                return Arrays.asList("list", "reset", "storm", "sun", "clear").stream()
                        .filter(s -> s.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
            }
            if (sub.equals("repair")) {
                return Arrays.asList("hand", "all").stream()
                        .filter(s -> s.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
            }
        }
        if (args.length == 2 && (sub.equals("ptime") || sub.equals("pweather"))) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase())).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
