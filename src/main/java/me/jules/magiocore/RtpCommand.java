package me.jules.magiocore;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

public class RtpCommand implements CommandExecutor, TabCompleter, Listener {
    private final MagioCore plugin;
    private final Random random = new Random();

    public RtpCommand(MagioCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;
        openGui(player);
        return true;
    }

    private void openGui(Player player) {
        FileConfiguration config = plugin.getModuleManager().getModuleConfig("rtp");
        ConfigurationSection gui = config.getConfigurationSection("gui");
        if (gui == null) return;

        Inventory inv = Bukkit.createInventory(new RtpGuiHolder(), gui.getInt("rows", 3) * 9, FontUtils.parse(gui.getString("title", "ᴠýʙěʀ sᴠěᴛᴀ")));

        String bg = gui.getString("background", "AIR");
        if (!bg.equalsIgnoreCase("AIR")) {
            ItemStack glass = new ItemStack(Material.valueOf(bg));
            ItemMeta meta = glass.getItemMeta();
            if (meta != null) {
                meta.displayName(Component.empty());
                glass.setItemMeta(meta);
            }
            for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, glass);
        }

        ConfigurationSection worlds = gui.getConfigurationSection("worlds");
        if (worlds != null) {
            for (String key : worlds.getKeys(false)) {
                ConfigurationSection w = worlds.getConfigurationSection(key);
                if (w == null) continue;

                ItemStack item = new ItemStack(Material.valueOf(w.getString("material", "GRASS_BLOCK")));
                ItemMeta meta = item.getItemMeta();
                if (meta instanceof SkullMeta skull && w.contains("texture")) {
                    applyTexture(skull, w.getString("texture"));
                }

                meta.displayName(FontUtils.parse(w.getString("name", key)));
                meta.lore(w.getStringList("lore").stream().map(FontUtils::parse).collect(Collectors.toList()));
                item.setItemMeta(meta);
                inv.setItem(w.getInt("slot"), item);
            }
        }

        player.openInventory(inv);
    }

    private void applyTexture(SkullMeta meta, String base64) {
        UUID uuid = UUID.nameUUIDFromBytes(base64.getBytes());
        PlayerProfile profile = Bukkit.createProfile(uuid, "RtpHead");
        PlayerTextures textures = profile.getTextures();
        try {
            String decoded = new String(Base64.getDecoder().decode(base64));
            String urlStr = decoded.substring(decoded.indexOf("http"), decoded.lastIndexOf("\""));
            textures.setSkin(new URL(urlStr));
        } catch (Exception ignored) {}
        profile.setTextures(textures);
        meta.setOwnerProfile(profile);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!(event.getInventory().getHolder() instanceof RtpGuiHolder)) return;

        event.setCancelled(true);
        int slot = event.getRawSlot();

        FileConfiguration config = plugin.getModuleManager().getModuleConfig("rtp");
        ConfigurationSection worlds = config.getConfigurationSection("gui.worlds");
        if (worlds == null) return;

        for (String key : worlds.getKeys(false)) {
            if (worlds.getInt(key + ".slot") == slot) {
                player.closeInventory();
                String worldName = worlds.getString(key + ".world-name", key);
                teleportRandomly(player, worldName);
                break;
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof RtpGuiHolder) {
            event.setCancelled(true);
        }
    }

    private void teleportRandomly(Player player, String worldName) {
        // RTP usually happens instantly or with a specific prefix
        FileConfiguration config = plugin.getModuleManager().getModuleConfig("rtp");
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            player.sendMessage(FontUtils.parse("§c" + "sᴠěᴛ ɴᴇɴí ᴋ ᴅɪsᴘᴏᴢɪᴄɪ."));
            return;
        }

        player.sendMessage(FontUtils.parse(config.getString("messages.teleporting", "&#00fbffʀᴛᴘ &#888888» §7Hledám bezpečnou lokaci...")));

        int radius = config.getInt("settings.radius", 5000);
        int maxAttempts = config.getInt("settings.max-attempts", 10);

        findSafeLocation(world, radius, maxAttempts, loc -> {
            if (loc != null) {
                player.teleport(loc);
                String success = config.getString("messages.success", "&#00ff44ʀᴛᴘ &#888888» §7Teleportováno na §f%x% %y% %z%")
                        .replace("%x%", String.valueOf(loc.getBlockX()))
                        .replace("%y%", String.valueOf(loc.getBlockY()))
                        .replace("%z%", String.valueOf(loc.getBlockZ()));
                player.sendMessage(FontUtils.parse(success));
            } else {
                player.sendMessage(FontUtils.parse(config.getString("messages.failure", "§cʀᴛᴘ &#888888» §7Nepodařilo se najít bezpečnou lokaci.")));
            }
        });
    }

    private void findSafeLocation(World world, int radius, int attempts, java.util.function.Consumer<Location> callback) {
        if (attempts <= 0) {
            callback.accept(null);
            return;
        }

        int x = random.nextInt(radius * 2) - radius;
        int z = random.nextInt(radius * 2) - radius;
        int y = world.getHighestBlockYAt(x, z);

        Location loc = new Location(world, x + 0.5, y + 1, z + 0.5);
        if (isSafe(loc)) {
            callback.accept(loc);
        } else {
            findSafeLocation(world, radius, attempts - 1, callback);
        }
    }

    private boolean isSafe(Location loc) {
        Material base = loc.clone().subtract(0, 1, 0).getBlock().getType();
        Material foot = loc.getBlock().getType();
        Material head = loc.clone().add(0, 1, 0).getBlock().getType();

        return base.isSolid() && !base.name().contains("LAVA") && foot.isAir() && head.isAir();
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return Collections.emptyList();
    }

    private static class RtpGuiHolder implements InventoryHolder {
        @Override
        public @NotNull Inventory getInventory() { return null; }
    }
}
