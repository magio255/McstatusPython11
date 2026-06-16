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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Collectors;

public class RtpCommand implements CommandExecutor, Listener, TabCompleter {
    private final MagioCore plugin;
    private final Random random = new Random();
    private final Map<Integer, String> slotToWorld = new HashMap<>();

    public RtpCommand(MagioCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (args.length > 0) {
            String targetWorld = args[0].toLowerCase();
            if (targetWorld.equals("world") || targetWorld.equals("overworld")) {
                findRandomLocation(player, "world");
            } else if (targetWorld.equals("nether")) {
                findRandomLocation(player, "world_nether");
            } else if (targetWorld.equals("end")) {
                findRandomLocation(player, "world_the_end");
            } else {
                findRandomLocation(player, targetWorld);
            }
            return true;
        }

        openGui(player);
        return true;
    }

    public void openGui(Player player) {
        ConfigurationSection config = plugin.getConfig().getConfigurationSection("rtp.gui");
        if (config == null) return;

        String titleStr = config.getString("title", "World Selection");
        int rows = config.getInt("rows", 3);
        Inventory inv = Bukkit.createInventory(new RtpGuiHolder(), rows * 9, FontUtils.parse("§8» §b" + titleStr));

        Material bgMaterial;
        try {
            bgMaterial = Material.valueOf(config.getString("background", "AIR"));
        } catch (IllegalArgumentException e) {
            bgMaterial = Material.AIR;
        }

        if (bgMaterial != Material.AIR) {
            ItemStack glass = new ItemStack(bgMaterial);
            ItemMeta glassMeta = glass.getItemMeta();
            if (glassMeta != null) {
                glassMeta.displayName(Component.empty());
                glass.setItemMeta(glassMeta);
            }

            for (int i = 0; i < inv.getSize(); i++) {
                inv.setItem(i, glass);
            }
        }

        slotToWorld.clear();
        ConfigurationSection worldsConfig = config.getConfigurationSection("worlds");
        if (worldsConfig != null) {
            for (String worldKey : worldsConfig.getKeys(false)) {
                ConfigurationSection itemConfig = worldsConfig.getConfigurationSection(worldKey);
                if (itemConfig == null) continue;

                Material material;
                try {
                    material = Material.valueOf(itemConfig.getString("material", "PLAYER_HEAD"));
                } catch (IllegalArgumentException e) {
                    material = Material.PLAYER_HEAD;
                }

                ItemStack item = new ItemStack(material);
                ItemMeta meta = item.getItemMeta();

                meta.displayName(FontUtils.parse("§b" + itemConfig.getString("name", worldKey)));
                List<String> lore = itemConfig.getStringList("lore");
                meta.lore(lore.stream().map(FontUtils::parse).collect(Collectors.toList()));

                if (material == Material.PLAYER_HEAD && itemConfig.contains("texture")) {
                    applyTexture((SkullMeta) meta, itemConfig.getString("texture"));
                }

                item.setItemMeta(meta);
                int slot = itemConfig.getInt("slot");
                inv.setItem(slot, item);
                slotToWorld.put(slot, worldKey);
            }
        }

        player.openInventory(inv);
    }

    private void applyTexture(SkullMeta meta, String base64) {
        UUID uuid = UUID.nameUUIDFromBytes(base64.getBytes());
        PlayerProfile profile = Bukkit.createProfile(uuid, "CustomHead");
        PlayerTextures textures = profile.getTextures();

        try {
            String decoded = new String(Base64.getDecoder().decode(base64));
            String urlStr = decoded.substring(decoded.indexOf("http"), decoded.lastIndexOf("\""));
            textures.setSkin(new URL(urlStr));
        } catch (Exception e) {
            // ignore
        }

        profile.setTextures(textures);
        meta.setOwnerProfile(profile);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!(event.getInventory().getHolder() instanceof RtpGuiHolder)) return;

        event.setCancelled(true);
        int slot = event.getRawSlot();

        if (slotToWorld.containsKey(slot)) {
            String worldName = slotToWorld.get(slot);
            player.closeInventory();
            if (worldName.equalsIgnoreCase("nether")) findRandomLocation(player, "world_nether");
            else if (worldName.equalsIgnoreCase("end")) findRandomLocation(player, "world_the_end");
            else findRandomLocation(player, worldName);
        }
    }

    private void findRandomLocation(Player player, String worldName) {
        player.sendMessage(FontUtils.parse("§b" + "Hledám bezpečné místo..."));

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            // Fallback to world environment mapping if exact name fails
            for (World w : Bukkit.getWorlds()) {
                if (w.getName().toLowerCase().contains(worldName.toLowerCase()) ||
                    w.getEnvironment().name().equalsIgnoreCase(worldName)) {
                    world = w;
                    break;
                }
            }
        }

        if (world == null) {
            player.sendMessage(FontUtils.parse("§c" + "Svět nebyl nalezen."));
            return;
        }

        int radius = plugin.getConfig().getInt("rtp.settings.radius", 5000);
        int maxAttempts = plugin.getConfig().getInt("rtp.settings.max-attempts", 25);

        for (int i = 0; i < maxAttempts; i++) {
            int x = random.nextInt(radius * 2) - radius;
            int z = random.nextInt(radius * 2) - radius;

            Location loc = null;
            if (world.getEnvironment() == World.Environment.NETHER) {
                loc = findSafeNetherLocation(world, x, z);
            } else {
                int y = world.getHighestBlockYAt(x, z);
                loc = new Location(world, x + 0.5, y + 1, z + 0.5);
            }

            if (loc != null && isSafe(loc)) {
                TeleportUtils.startTeleportCountdown(player, loc, plugin, success -> {});
                return;
            }
        }

        player.sendMessage(FontUtils.parse("§c" + "Nepodařilo se najít bezpečné místo, zkus to znovu."));
    }

    private Location findSafeNetherLocation(World world, int x, int z) {
        // Scan from Y=120 down to 30
        for (int y = 120; y > 30; y--) {
            Material block = world.getBlockAt(x, y, z).getType();
            if (block.isSolid()) {
                Location loc = new Location(world, x + 0.5, y + 1, z + 0.5);
                if (isSafe(loc)) return loc;
            }
        }
        return null;
    }

    private boolean isSafe(Location loc) {
        Material feet = loc.getBlock().getType();
        Material head = loc.clone().add(0, 1, 0).getBlock().getType();
        Material ground = loc.clone().add(0, -1, 0).getBlock().getType();

        // Check if there is space for the player
        if (feet != Material.AIR && feet != Material.CAVE_AIR && feet != Material.TALL_GRASS && feet != Material.SHORT_GRASS) return false;
        if (head != Material.AIR && head != Material.CAVE_AIR) return false;

        // Check if ground is safe
        if (!ground.isSolid()) return false;
        if (ground == Material.LAVA || ground == Material.MAGMA_BLOCK || ground == Material.CACTUS || ground == Material.SWEET_BERRY_BUSH) return false;
        if (ground == Material.WATER) return false;

        return true;
    }

    private static class RtpGuiHolder implements InventoryHolder {
        @Override
        public @NotNull Inventory getInventory() {
            return null;
        }

    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return Arrays.asList("world", "nether", "end").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
