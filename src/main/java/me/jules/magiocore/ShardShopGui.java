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
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ShardShopGui implements Listener {
    private final MagioCore plugin;

    public ShardShopGui(MagioCore plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        FileConfiguration config = plugin.getModuleManager().getModuleConfig("shardshop");
        String title = config.getString("gui.title", "&8Shard Shop");

        ShardShopHolder holder = new ShardShopHolder();
        Inventory inv = Bukkit.createInventory(holder, 27, FontUtils.parse(title));
        holder.setInventory(inv);

        // Fill background
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        if (fillerMeta != null) {
            fillerMeta.displayName(Component.empty());
            filler.setItemMeta(fillerMeta);
        }
        for (int i = 0; i < 27; i++) inv.setItem(i, filler);

        ConfigurationSection items = config.getConfigurationSection("items");
        if (items != null) {
            for (String key : items.getKeys(false)) {
                ConfigurationSection itemSec = items.getConfigurationSection(key);
                if (itemSec == null) continue;

                Material mat = Material.valueOf(itemSec.getString("material", "PAPER").toUpperCase());
                String name = itemSec.getString("name", "");
                int slot = itemSec.getInt("slot");
                int price = config.getInt("prices." + key, 0);

                ItemStack is = new ItemStack(mat);
                ItemMeta meta = is.getItemMeta();
                if (meta != null) {
                    meta.displayName(FontUtils.parse(name));
                    List<Component> lore = new ArrayList<>();
                    lore.add(FontUtils.parse("&7Tento předmět si můžeš"));
                    lore.add(FontUtils.parse("&7zakoupit za Shardy."));
                    lore.add(Component.empty());
                    lore.add(FontUtils.parse("&eInformace:"));
                    lore.add(FontUtils.parse(" &fCena: &d" + price + " Shardů"));
                    lore.add(Component.empty());
                    lore.add(FontUtils.parse("&e▶ &lKLIKNI &ePro nákup!"));
                    meta.lore(lore);
                    is.setItemMeta(meta);
                }
                inv.setItem(slot, is);
            }
        }

        player.openInventory(inv);
    }

    public void openConfirm(Player player, String itemKey) {
        FileConfiguration config = plugin.getModuleManager().getModuleConfig("shardshop");
        ShardConfirmHolder holder = new ShardShopGui.ShardConfirmHolder(itemKey);
        Inventory inv = Bukkit.createInventory(holder, 27, FontUtils.parse(config.getString("gui.confirm-title", "&8Potvrdit nákup")));
        holder.setInventory(inv);

        ItemStack black = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta blackMeta = black.getItemMeta();
        if (blackMeta != null) {
            blackMeta.displayName(Component.empty());
            black.setItemMeta(blackMeta);
        }
        for (int i = 0; i < 27; i++) inv.setItem(i, black);

        ItemStack confirm = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta confirmMeta = confirm.getItemMeta();
        if (confirmMeta != null) {
            confirmMeta.displayName(FontUtils.parse("&a&lPOTVRDIT NÁKUP"));
            confirm.setItemMeta(confirmMeta);
        }
        inv.setItem(15, confirm);

        ItemStack cancel = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta cancelMeta = cancel.getItemMeta();
        if (cancelMeta != null) {
            cancelMeta.displayName(FontUtils.parse("&c&lZRUŠIT"));
            cancel.setItemMeta(cancelMeta);
        }
        inv.setItem(11, cancel);

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
                if (items.getInt(key + ".slot") == slot) {
                    if (key.equalsIgnoreCase("back")) {
                        String cmd = items.getString(key + ".command");
                        if (cmd != null) player.performCommand(cmd);
                    } else {
                        openConfirm(player, key);
                    }
                    break;
                }
            }
        } else if (holder instanceof ShardConfirmHolder confirmHolder) {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            if (slot == 11) {
                open(player);
                player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1f);
            } else if (slot == 15) {
                FileConfiguration config = plugin.getModuleManager().getModuleConfig("shardshop");
                int price = config.getInt("prices." + confirmHolder.itemKey);

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
