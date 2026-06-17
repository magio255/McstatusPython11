package me.jules.magiocore;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public class TpaCommands implements CommandExecutor, TabCompleter {
    private final MagioCore plugin;
    private final TpaManager tpaManager;
    private final Map<UUID, Long> tpaCooldown = new HashMap<>();

    public TpaCommands(MagioCore plugin, TpaManager tpaManager) {
        this.plugin = plugin;
        this.tpaManager = tpaManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;

        String cmd = command.getName().toLowerCase();

        switch (cmd) {
            case "tpa":
                handleTpa(player, args, "to");
                break;
            case "tpahere":
                handleTpa(player, args, "here");
                break;
            case "tpacancel":
                handleTpaCancel(player);
                break;
            case "tpaoff":
            case "tptoggle":
                handleTpaOff(player);
                break;
            case "tpaccept":
                handleTpaAccept(player);
                break;
            case "tpadeny":
                handleTpaDeny(player);
                break;
        }

        return true;
    }

    private void handleTpa(Player player, String[] args, String type) {
        FileConfiguration config = plugin.getModuleManager().getModuleConfig("tpa");
        if (args.length == 0) {
            String usage = config.getString("messages.usage", "§cᴛᴘᴀ &#888888» §7ᴘᴏᴜžɪᴛí: /%cmd% <ʜʀáč>")
                    .replace("%cmd%", type.equals("to") ? "ᴛᴘᴀ" : "ᴛᴘᴀʜᴇʀᴇ");
            player.sendMessage(FontUtils.parse(usage));
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(FontUtils.parse(config.getString("messages.offline", "§cʜʀáč ɴᴇɴí ᴏɴʟɪɴᴇ.")));
            return;
        }

        if (target.equals(player)) {
            String selfMsg = config.getString("messages.teleport-self", "§cᴛᴘᴀ &#888888» §7ɴᴇᴍůžᴇš sᴇ ᴛᴇʟᴇᴘᴏʀᴛᴏᴠᴀᴛ sáᴍ ᴋ sᴇʙě");
            player.sendMessage(FontUtils.parse(selfMsg));
            return;
        }

        if (tpaManager.isTpaOff(target.getUniqueId())) {
            String offMsg = config.getString("messages.tpaoff", "§cᴛᴘᴀ &#888888» §7Hráč má vypnuté žádosti.");
            player.sendMessage(FontUtils.parse(offMsg));
            return;
        }

        if (plugin.getIgnoreModule().isTpaIgnored(target.getUniqueId()) || plugin.getIgnoreModule().isIgnored(target.getUniqueId(), player.getUniqueId())) {
            String ignoreMsg = config.getString("messages.ignored", "§cᴛᴘᴀ &#888888» §7Tento hráč tě ignoruje.");
            player.sendMessage(FontUtils.parse(ignoreMsg));
            return;
        }

        long now = System.currentTimeMillis();
        long last = tpaCooldown.getOrDefault(player.getUniqueId(), 0L);
        long delayMs = config.getLong("delay", 60) * 1000;
        if (now - last < delayMs) {
            long remaining = (delayMs - (now - last)) / 1000;
            String cdMsg = config.getString("messages.cooldown", "§cᴛᴘᴀ &#888888» §7Musíš počkat ještě %time%s.").replace("%time%", String.valueOf(remaining));
            player.sendMessage(FontUtils.parse(cdMsg));
            return;
        }

        tpaManager.sendRequest(player.getUniqueId(), target.getUniqueId(), type);
        tpaCooldown.put(player.getUniqueId(), now);

        String sentMsg = config.getString("messages.sent", "&#00fbffᴛᴘᴀ &#888888» §7Zaslal jsi žádost hráči &#00fbff%player%").replace("%player%", target.getName());
        player.sendMessage(FontUtils.parse(sentMsg));

        String key = type.equals("to") ? "messages.received-tpa" : "messages.received-tpahere";
        String def = type.equals("to") ? "&#00fbffᴛᴘᴀ &#888888» §7Hráč &#00fbff%player% §7se chce k tobě teleportovat." : "&#00fbffᴛᴘᴀ &#888888» §7Hráč &#00fbff%player% §7chce, abys se k němu teleportoval.";
        String receivedMsg = config.getString(key, def).replace("%player%", player.getName());
        target.sendMessage(FontUtils.parse(receivedMsg));

        // Action bar and sound
        String abKey = type.equals("to") ? "messages.actionbar-tpa" : "messages.actionbar-tpahere";
        String abDef = type.equals("to") ? "&#37FF00ᴛᴘᴀ &#888888▶ §fʜʀáč %player% ᴛɪ ᴘᴏsʟᴀʟ ᴛᴘᴀ" : "&#37FF00ᴛᴘᴀ &#888888▶ §fʜʀáč %player% ᴛɪ ᴘᴏsʟᴀʟ ᴛᴘᴀʜᴇʀᴇ";
        String ab = config.getString(abKey, abDef).replace("%player%", player.getName());
        target.sendActionBar(FontUtils.parse(ab));
        try {
            org.bukkit.Sound sound = org.bukkit.Sound.valueOf(config.getString("messages.received-sound", "ENTITY_EXPERIENCE_ORB_PICKUP"));
            target.playSound(target.getLocation(), sound, 1f, 1f);
        } catch (Exception ignored) {}

        Component accept = FontUtils.parse(config.getString("messages.click-accept", "&#00ff44[ᴘᴏᴛᴠʀᴅɪᴛ]"))
                .hoverEvent(HoverEvent.showText(FontUtils.parse(config.getString("messages.click-accept-hover", "&#00ff44ᴋʟɪᴋɴɪ ᴘʀᴏ ᴘᴏᴛᴠʀᴢᴇɴí"))))
                .clickEvent(ClickEvent.runCommand("/tpaccept"));

        Component deny = FontUtils.parse(config.getString("messages.click-deny", "§c§l[ᴏᴅᴍíᴛɴᴏᴜᴛ]"))
                .hoverEvent(HoverEvent.showText(FontUtils.parse(config.getString("messages.click-deny-hover", "§cᴋʟɪᴋɴɪ ᴘʀᴏ ᴏᴅᴍíᴛɴᴜᴛí"))))
                .clickEvent(ClickEvent.runCommand("/tpadeny"));

        target.sendMessage(FontUtils.parse("&#00fbffᴛᴘᴀ &#888888» §7").append(accept).append(Component.text(config.getString("messages.separator", " §7§l/ "))).append(deny));

        // Schedule expiry message
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            TpaManager.TpaRequest req = tpaManager.getRequest(target.getUniqueId());
            if (req != null && req.requester.equals(player.getUniqueId())) {
                tpaManager.removeRequest(target.getUniqueId());
                player.sendMessage(FontUtils.parse(config.getString("messages.expired", "&#00fbffᴛᴘᴀ &#888888» §7Žádost o teleport pro &#00fbff%player% §7vypršela").replace("%player%", target.getName())));
            }
        }, 1200L); // 60 seconds
    }

    private void handleTpaCancel(Player player) {
        FileConfiguration config = plugin.getModuleManager().getModuleConfig("tpa");
        UUID targetUuid = tpaManager.getSentRequestTarget(player.getUniqueId());
        if (targetUuid != null) {
            Player target = Bukkit.getPlayer(targetUuid);
            tpaManager.removeRequest(targetUuid);
            String msg = config.getString("messages.cancelled", "&#00fbffᴛᴘᴀ &#888888» §7Zrušil jsi žádost pro &#00fbff%player%").replace("%player%", target != null ? target.getName() : "ʜʀáčᴇ");
            player.sendMessage(FontUtils.parse(msg));
        } else {
            player.sendMessage(FontUtils.parse(config.getString("messages.no-sent-request", "§cᴛᴘᴀ &#888888» §7Nemáš žádnou odeslanou žádost")));
        }
    }

    private void handleTpaOff(Player player) {
        FileConfiguration config = plugin.getModuleManager().getModuleConfig("tpa");
        tpaManager.toggleTpa(player.getUniqueId());
        if (tpaManager.isTpaOff(player.getUniqueId())) {
            player.sendMessage(FontUtils.parse(config.getString("messages.tpaoff-disabled", "&#00fbffᴛᴘᴀ &#888888» §7Žádosti o teleport byly &#EA427Fᴠʏᴘɴᴜᴛʏ")));
        } else {
            player.sendMessage(FontUtils.parse(config.getString("messages.tpaoff-enabled", "&#00fbffᴛᴘᴀ &#888888» §7Žádosti o teleport byly &#00ff44ᴢᴀᴘɴᴜᴛʏ")));
        }
    }

    private void handleTpaAccept(Player player) {
        FileConfiguration config = plugin.getModuleManager().getModuleConfig("tpa");
        TpaManager.TpaRequest req = tpaManager.getRequest(player.getUniqueId());
        if (req == null) {
            player.sendMessage(FontUtils.parse(config.getString("messages.no-active-request", "§cᴛᴘᴀ &#888888» §7Nemáš žádnou aktivní žádost")));
            return;
        }

        Player requester = Bukkit.getPlayer(req.requester);
        if (requester == null) {
            player.sendMessage(FontUtils.parse(config.getString("messages.player-offline", "§cᴛᴘᴀ &#888888» §7Hráč již není připojen")));
            tpaManager.removeRequest(player.getUniqueId());
            return;
        }

        tpaManager.removeRequest(player.getUniqueId());

        String accMsg = config.getString("messages.accepted", "&#00fbffᴛᴘᴀ &#888888» §7Žádost přijata.");
        player.sendMessage(FontUtils.parse(accMsg));
        requester.sendMessage(FontUtils.parse(accMsg));

        Player toTeleport = req.type.equals("to") ? requester : player;
        Player targetLocPlayer = req.type.equals("to") ? player : requester;

        TeleportUtils.startTeleportCountdown(toTeleport, targetLocPlayer, "ᴛᴘᴀ", plugin, success -> {});
    }

    private void handleTpaDeny(Player player) {
        FileConfiguration config = plugin.getModuleManager().getModuleConfig("tpa");
        TpaManager.TpaRequest req = tpaManager.getRequest(player.getUniqueId());
        if (req == null) {
            player.sendMessage(FontUtils.parse(config.getString("messages.no-active-request", "§cᴛᴘᴀ &#888888» §7Nemáš žádnou aktivní žádost")));
            return;
        }

        Player requester = Bukkit.getPlayer(req.requester);
        tpaManager.removeRequest(player.getUniqueId());

        String denMsg = config.getString("messages.denied", "§cᴛᴘᴀ &#888888» §7Žádost odmítnuta.");
        player.sendMessage(FontUtils.parse(denMsg));
        if (requester != null) {
            requester.sendMessage(FontUtils.parse(denMsg));
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            String sub = command.getName().toLowerCase();
            if (sub.equals("tpa") || sub.equals("tpahere")) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }
}
