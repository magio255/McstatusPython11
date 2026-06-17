package me.jules.magiocore.modules;

import me.jules.magiocore.FontUtils;
import me.jules.magiocore.MagioCore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FreezeModule implements CommandExecutor, Listener {
    private final MagioCore plugin;
    private final Set<UUID> frozenPlayers = new HashSet<>();
    private final String prefix = "§c[Freeze] §7";

    public FreezeModule(MagioCore plugin) {
        this.plugin = plugin;
        startFreezeTitleTask();
    }

    private void startFreezeTitleTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                FileConfiguration config = plugin.getModuleManager().getModuleConfig("freeze");
                for (UUID uuid : frozenPlayers) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null) {
                        String title = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().serialize(FontUtils.parse(config.getString("messages.title", "&#ff0000ᴊsɪ ᴢᴍʀᴀžᴇɴ")));
                        String sub = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().serialize(FontUtils.parse(config.getString("messages.subtitle", "§7Dostav se do čekárny na Discordu")));
                        player.sendTitle(title, sub, 0, 40, 0);
                        player.sendActionBar(FontUtils.parse(config.getString("messages.actionbar", "&#ff0000ꜰʀᴇᴇᴢᴇ ᴀᴋᴛɪᴠɴí")));
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 40L);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("freeze.use")) {
            sender.sendMessage(FontUtils.parse("§c" + "ɴᴇᴍáš ᴏᴘʀáᴠɴěɴí."));
            return true;
        }

        if (args.length == 0) return false;

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(FontUtils.parse("§c" + "ʜʀáč ɴᴇɴí ᴏɴʟɪɴᴇ."));
            return true;
        }

        FileConfiguration config = plugin.getModuleManager().getModuleConfig("freeze");
        if (command.getName().equalsIgnoreCase("freeze")) {
            if (frozenPlayers.contains(target.getUniqueId())) {
                sender.sendMessage(prefix + "Hráč už je frozen.");
                return true;
            }

            frozenPlayers.add(target.getUniqueId());
            Location freezeLoc = getFreezeLocation();
            target.teleport(freezeLoc);
            target.playSound(target.getLocation(), Sound.ENTITY_ENDERMAN_STARE, 1f, 0.5f);

            sender.sendMessage(prefix + "Zmrazil jsi " + target.getName() + ".");
            target.sendMessage(FontUtils.parse(config.getString("messages.frozen", "&#ff0000ʙʏʟ ᴊsɪ ꜰʀᴇᴇᴢɴᴜᴛ")));
            return true;
        }

        if (command.getName().equalsIgnoreCase("unfreeze")) {
            if (!frozenPlayers.contains(target.getUniqueId())) {
                sender.sendMessage(prefix + "Hráč není frozen.");
                return true;
            }

            frozenPlayers.remove(target.getUniqueId());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mvtp " + target.getName() + " spawn");
            target.playSound(target.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);

            sender.sendMessage(prefix + "Odmrazil jsi " + target.getName() + ".");
            target.sendMessage(FontUtils.parse(config.getString("messages.unfrozen", "&#00ff44ʙʏʟ ᴊsɪ ᴜɴꜰʀᴇᴇᴢɴᴜᴛ.")));
            return true;
        }

        return false;
    }

    private Location getFreezeLocation() {
        FileConfiguration config = plugin.getModuleManager().getModuleConfig("freeze");
        String worldName = config.getString("spawn-location.world", "spawn");
        World world = Bukkit.getWorld(worldName);
        if (world == null) world = Bukkit.getWorlds().get(0);

        double x = config.getDouble("spawn-location.x", 39.5);
        double y = config.getDouble("spawn-location.y", 94.0);
        double z = config.getDouble("spawn-location.z", -13.5);
        float yaw = (float) config.getDouble("spawn-location.yaw", 0.0);
        float pitch = (float) config.getDouble("spawn-location.pitch", 0.0);

        return new Location(world, x, y, z, yaw, pitch);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (frozenPlayers.contains(event.getPlayer().getUniqueId())) {
            Location from = event.getFrom();
            Location to = event.getTo();
            if (to == null) return;
            if (from.getX() != to.getX() || from.getZ() != to.getZ()) {
                event.setTo(from.setDirection(to.getDirection()));
            }
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (frozenPlayers.contains(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(FontUtils.parse("&#ff0000" + "ᴊsɪ ꜰʀᴇᴇᴢᴇɴ! ɴᴇᴍůžᴇš ᴘsáᴛ ᴅᴏ ᴄʜᴀᴛᴜ."));
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (frozenPlayers.contains(event.getPlayer().getUniqueId())) {
            String cmd = event.getMessage().toLowerCase();
            if (!cmd.startsWith("/msg") && !cmd.startsWith("/r") && !cmd.startsWith("/w") && !cmd.startsWith("/reply")) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(FontUtils.parse("&#ff0000" + "ᴊsɪ ꜰʀᴇᴇᴢᴇɴ! ɴᴇᴍůžᴇš ᴘᴏᴜžíᴠᴀᴛ ᴘŘíᴋᴀᴢʏ."));
            }
        }
    }

    @EventHandler public void onDrop(PlayerDropItemEvent e) { if (frozenPlayers.contains(e.getPlayer().getUniqueId())) e.setCancelled(true); }
    @EventHandler public void onPickup(PlayerPickupItemEvent e) { if (frozenPlayers.contains(e.getPlayer().getUniqueId())) e.setCancelled(true); }
    @EventHandler public void onBreak(BlockBreakEvent e) { if (frozenPlayers.contains(e.getPlayer().getUniqueId())) e.setCancelled(true); }
    @EventHandler public void onPlace(BlockPlaceEvent e) { if (frozenPlayers.contains(e.getPlayer().getUniqueId())) e.setCancelled(true); }
    @EventHandler public void onInvClick(InventoryClickEvent e) { if (frozenPlayers.contains(e.getWhoClicked().getUniqueId())) e.setCancelled(true); }
    @EventHandler public void onDamage(EntityDamageEvent e) {
        if (frozenPlayers.contains(e.getEntity().getUniqueId())) e.setCancelled(true);
        if (e instanceof org.bukkit.event.entity.EntityDamageByEntityEvent ev) {
            if (frozenPlayers.contains(ev.getDamager().getUniqueId())) e.setCancelled(true);
        }
    }
}
