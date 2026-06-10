package me.jules.magiocore;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
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
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class LeaderboardGui implements Listener {
    private final MagioCore plugin;

    public LeaderboardGui(MagioCore plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        FileConfiguration config = plugin.getModuleManager().getModuleConfig("leaderboard");
        if (config == null) return;

        int size = config.getInt("size", 27);
        String title = config.getString("menu_title", "&8&lLeaderboard");

        LeaderboardHolder holder = new LeaderboardHolder();
        Inventory inv = Bukkit.createInventory(holder, size, FontUtils.parse(title));
        holder.setInventory(inv);

        ConfigurationSection items = config.getConfigurationSection("items");
        if (items != null) {
            for (String key : items.getKeys(false)) {
                ConfigurationSection itemSec = items.getConfigurationSection(key);
                if (itemSec == null) continue;

                ItemStack is;
                String matStr = itemSec.getString("material", "PAPER").toUpperCase();

                if (matStr.equals("PLAYER_HEAD") && itemSec.contains("texture")) {
                    is = new ItemStack(Material.PLAYER_HEAD);
                    SkullMeta meta = (SkullMeta) is.getItemMeta();
                    applyTexture(meta, itemSec.getString("texture"));
                    is.setItemMeta(meta);
                } else {
                    is = new ItemStack(Material.valueOf(matStr));
                }

                ItemMeta meta = is.getItemMeta();
                if (meta != null) {
                    meta.displayName(FontUtils.parse(itemSec.getString("display_name", "")));
                    List<String> lore = itemSec.getStringList("lore");
                    meta.lore(lore.stream().map(s -> applyPlaceholders(player, s)).map(FontUtils::parse).collect(Collectors.toList()));
                    is.setItemMeta(meta);
                }

                if (itemSec.contains("slot")) {
                    inv.setItem(itemSec.getInt("slot"), is);
                } else if (itemSec.contains("slots")) {
                    String slotsStr = itemSec.getString("slots");
                    if (slotsStr.contains("-")) {
                        String[] parts = slotsStr.split("-");
                        int start = Integer.parseInt(parts[0]);
                        int end = Integer.parseInt(parts[1]);
                        for (int i = start; i <= end; i++) {
                            inv.setItem(i, is.clone());
                        }
                    }
                }
            }
        }

        player.openInventory(inv);
    }

    private String applyPlaceholders(Player player, String text) {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            return me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, text);
        }
        return text.replace("%player_name%", player.getName());
    }

    private void applyTexture(SkullMeta meta, String base64) {
        UUID uuid = UUID.nameUUIDFromBytes(base64.getBytes());
        PlayerProfile profile = Bukkit.createProfile(uuid, "LbHead");
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
        if (!(event.getInventory().getHolder() instanceof LeaderboardHolder)) return;

        event.setCancelled(true);
        int slot = event.getRawSlot();

        FileConfiguration config = plugin.getModuleManager().getModuleConfig("leaderboard");
        ConfigurationSection items = config.getConfigurationSection("items");
        if (items == null) return;

        for (String key : items.getKeys(false)) {
            ConfigurationSection itemSec = items.getConfigurationSection(key);
            if (itemSec == null) continue;

            boolean match = false;
            if (itemSec.contains("slot") && itemSec.getInt("slot") == slot) {
                match = true;
            } else if (itemSec.contains("slots")) {
                String slotsStr = itemSec.getString("slots");
                if (slotsStr.contains("-")) {
                    String[] parts = slotsStr.split("-");
                    int start = Integer.parseInt(parts[0]);
                    int end = Integer.parseInt(parts[1]);
                    if (slot >= start && slot <= end) match = true;
                }
            }

            if (match) {
                List<String> cmds = itemSec.getStringList("click_commands");
                for (String cmd : cmds) {
                    if (cmd.equalsIgnoreCase("[close]")) {
                        player.closeInventory();
                    } else if (cmd.startsWith("[player] ")) {
                        player.performCommand(cmd.substring(9));
                    } else if (cmd.startsWith("[console] ")) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.substring(10).replace("%player%", player.getName()));
                    }
                }
                break;
            }
        }
    }

    private static class LeaderboardHolder implements InventoryHolder {
        private Inventory inventory;
        public void setInventory(Inventory inventory) { this.inventory = inventory; }
        @Override public @NotNull Inventory getInventory() { return inventory; }
    }
}
