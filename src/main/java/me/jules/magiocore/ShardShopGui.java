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
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

public class ShardShopGui implements Listener {
    private final MagioCore plugin;

    public ShardShopGui(MagioCore plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        FileConfiguration config = plugin.getModuleManager().getModuleConfig("shardshop");
        int rows = config.getInt("gui.rows", 3);
        String title = config.getString("gui.title", "&8Shard Shop");

        ShardShopHolder holder = new ShardShopHolder();
        Inventory inv = Bukkit.createInventory(holder, rows * 9, FontUtils.parse(title));
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

                String name = itemSec.getString("name", "");
                int price = config.getInt("prices." + key, 0);
                ItemMeta meta = is.getItemMeta();
                if (meta != null) {
                    meta.displayName(FontUtils.parse(name));
                    meta.lore(itemSec.getStringList("lore").stream()
                            .map(s -> applyPlaceholders(player, s).replace("%price%", String.valueOf(price)))
                            .map(FontUtils::parse).toList());
                    is.setItemMeta(meta);
                }
                if (itemSec.contains("slot")) inv.setItem(itemSec.getInt("slot"), is);
                else if (itemSec.contains("slots")) {
                    for (int sIdx : FontUtils.parseSlots(itemSec.getString("slots"), inv.getSize())) {
                        inv.setItem(sIdx, is.clone());
                    }
                }
            }
        }

        player.openInventory(inv);
    }

    public void openConfirm(Player player, String itemKey) {
        FileConfiguration config = plugin.getModuleManager().getModuleConfig("shardshop");
        int rows = config.getInt("gui.confirm.rows", 3);
        ShardConfirmHolder holder = new ShardShopGui.ShardConfirmHolder(itemKey);
        Inventory inv = Bukkit.createInventory(holder, rows * 9, FontUtils.parse(config.getString("gui.confirm-title", "&8Potvrdit nákup")));
        holder.setInventory(inv);

        ConfigurationSection items = config.getConfigurationSection("gui.confirm.items");
        if (items != null) {
            for (String key : items.getKeys(false)) {
                ConfigurationSection sec = items.getConfigurationSection(key);
                if (sec == null) continue;

                Material mat = Material.valueOf(sec.getString("material", "AIR").toUpperCase());
                ItemStack is = new ItemStack(mat);
                ItemMeta meta = is.getItemMeta();
                if (meta != null) {
                    meta.displayName(FontUtils.parse(sec.getString("name", " ")));
                    meta.lore(sec.getStringList("lore").stream().map(FontUtils::parse).toList());
                    is.setItemMeta(meta);
                }
                if (sec.contains("slot")) inv.setItem(sec.getInt("slot"), is);
                else if (sec.contains("slots")) {
                    for (int sIdx : FontUtils.parseSlots(sec.getString("slots"), inv.getSize())) {
                        inv.setItem(sIdx, is.clone());
                    }
                }
            }
        }

        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        InventoryHolder holder = event.getInventory().getHolder();

        if (holder instanceof ShardShopHolder) {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            FileConfiguration config = plugin.getModuleManager().getModuleConfig("shardshop");
            ConfigurationSection items = config.getConfigurationSection("items");
            if (items == null) return;

            for (String key : items.getKeys(false)) {
                if (items.getInt(key + ".slot", -1) == slot) {
                    if (key.equalsIgnoreCase("back")) {
                        String cmd = items.getString(key + ".command");
                        if (cmd != null) player.performCommand(cmd);
                    } else if (!key.startsWith("filler")) {
                        openConfirm(player, key);
                    }
                    break;
                }
            }
        } else if (holder instanceof ShardConfirmHolder confirmHolder) {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            FileConfiguration config = plugin.getModuleManager().getModuleConfig("shardshop");
            ConfigurationSection items = config.getConfigurationSection("gui.confirm.items");
            if (items == null) return;

            String clickedKey = null;
            for (String key : items.getKeys(false)) {
                if (items.getInt(key + ".slot", -1) == slot) {
                    clickedKey = key;
                    break;
                }
            }

            if (clickedKey != null) {
                if (clickedKey.equalsIgnoreCase("cancel")) {
                    open(player);
                    player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                } else if (clickedKey.equalsIgnoreCase("confirm")) {
                    int price = config.getInt("prices." + confirmHolder.itemKey);

                    if (config.getString("economy.type", "VAULT").equalsIgnoreCase("PAPI")) {
                        String placeholder = config.getString("economy.balance-placeholder");
                        String balStr = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, placeholder);
                        double balance = 0;
                        try {
                            balance = Double.parseDouble(balStr.replaceAll("[^0-9.]", ""));
                        } catch (Exception ignored) {}

                        if (balance < price) {
                            player.sendMessage(FontUtils.parse(config.getString("messages.no-shards", "&8「&cShop&8」 &7Nemáš dostatek Shardů!")));
                            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 0.5f, 1f);
                            return;
                        }

                        String withdrawCmd = config.getString("economy.withdraw-command")
                                .replace("%player%", player.getName())
                                .replace("%price%", String.valueOf(price));
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), withdrawCmd);
                    } else {
                        if (plugin.getEconomy().getBalance(player) < price) {
                            player.sendMessage(FontUtils.parse(config.getString("messages.no-shards", "&8「&cShop&8」 &7Nemáš dostatek Shardů!")));
                            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 0.5f, 1f);
                            return;
                        }
                        plugin.getEconomy().withdrawPlayer(player, price);
                    }

                    String cmd = config.getString("items." + confirmHolder.itemKey + ".command");
                    String itemName = config.getString("items." + confirmHolder.itemKey + ".name");

                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", player.getName()));
                    player.sendMessage(FontUtils.parse(config.getString("messages.bought", "&8「&bShop&8」 &7Koupil jsi &b1x %item% &7za &d%price% Shardů&7.")
                            .replace("%item%", itemName).replace("%price%", String.valueOf(price))));

                    player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1f);
                    player.closeInventory();
                }
            }
        }
    }

    private String applyPlaceholders(Player player, String text) {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            return me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, text);
        }
        return text;
    }

    private void applyTexture(SkullMeta meta, String base64) {
        UUID uuid = UUID.nameUUIDFromBytes(base64.getBytes());
        PlayerProfile profile = Bukkit.createProfile(uuid, "ShopHead");
        PlayerTextures textures = profile.getTextures();
        try {
            String decoded = new String(Base64.getDecoder().decode(base64));
            String urlStr = decoded.substring(decoded.indexOf("http"), decoded.lastIndexOf("\""));
            textures.setSkin(new URL(urlStr));
        } catch (Exception ignored) {}
        profile.setTextures(textures);
        meta.setOwnerProfile(profile);
    }

    private static class ShardShopHolder implements InventoryHolder {
        private Inventory inventory;
        public void setInventory(Inventory inventory) { this.inventory = inventory; }
        @Override public @NotNull Inventory getInventory() { return inventory; }
    }

    private static class ShardConfirmHolder implements InventoryHolder {
        public final String itemKey;
        private Inventory inventory;
        public ShardConfirmHolder(String itemKey) { this.itemKey = itemKey; }
        public void setInventory(Inventory inventory) { this.inventory = inventory; }
        @Override public @NotNull Inventory getInventory() { return inventory; }
    }
}
