package me.jules.magiocore;

import org.bukkit.Bukkit;
import org.bukkit.Material;
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
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BaltopGui implements Listener {
    private final MagioCore plugin;
    private final BaltopManager manager;
    private final Map<UUID, Integer> playerPages = new HashMap<>();

    public BaltopGui(MagioCore plugin, BaltopManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    public void open(Player player, int page) {
        FileConfiguration config = plugin.getModuleManager().getModuleConfig("baltop");
        int rows = config.getInt("gui.rows", 6);
        String title = config.getString("gui.title", "&8Baltop");

        BaltopGuiHolder holder = new BaltopGuiHolder();
        Inventory inv = Bukkit.createInventory(holder, rows * 9, FontUtils.parse(title));
        holder.setInventory(inv);
        List<BaltopManager.BaltopEntry> top = manager.getCachedTop();

        playerPages.put(player.getUniqueId(), page);

        ConfigurationSection itemsSec = config.getConfigurationSection("gui.items");
        if (itemsSec != null) {
            for (String key : itemsSec.getKeys(false)) {
                ConfigurationSection itemSec = itemsSec.getConfigurationSection(key);
                if (itemSec == null) continue;

                if (key.equalsIgnoreCase("entries")) {
                    List<Integer> entrySlots = new ArrayList<>();
                    String slotsStr = itemSec.getString("slots");
                    for (String p : slotsStr.split(",")) {
                        if (p.contains("-")) {
                            String[] range = p.split("-");
                            for (int i = Integer.parseInt(range[0]); i <= Integer.parseInt(range[1]); i++) entrySlots.add(i);
                        } else entrySlots.add(Integer.parseInt(p.trim()));
                    }

                    int start = (page - 1) * entrySlots.size();
                    for (int i = 0; i < entrySlots.size(); i++) {
                        int index = start + i;
                        if (index < top.size()) {
                            BaltopManager.BaltopEntry entry = top.get(index);
                            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                            SkullMeta meta = (SkullMeta) head.getItemMeta();
                            if (meta != null) {
                                meta.setOwningPlayer(Bukkit.getOfflinePlayer(entry.uuid()));
                                int rank = index + 1;
                                meta.displayName(FontUtils.parse(config.getString("gui.entry.name", "&e&l%rank%. &f%player%")
                                        .replace("%rank%", String.valueOf(rank)).replace("%player%", entry.name())));
                                List<Component> lore = config.getStringList("gui.entry.lore").stream()
                                        .map(s -> s.replace("%rank%", String.valueOf(rank)).replace("%balance%", FontUtils.formatMoney(entry.balance())))
                                        .map(FontUtils::parse).toList();
                                meta.lore(lore);
                                head.setItemMeta(meta);
                            }
                            inv.setItem(entrySlots.get(i), head);
                        }
                    }
                } else {
                    Material mat = Material.valueOf(itemSec.getString("material", "AIR").toUpperCase());
                    ItemStack is = createNav(itemSec.getString("name", " "), mat, itemSec.getStringList("lore").stream().map(FontUtils::parse).toList());
                    if (itemSec.contains("slot")) inv.setItem(itemSec.getInt("slot"), is);
                    else if (itemSec.contains("slots")) {
                for (int sIdx : FontUtils.parseSlots(itemSec.getString("slots"), inv.getSize())) {
                    inv.setItem(sIdx, is.clone());
                        }
                    }
                }
            }
        }

        player.openInventory(inv);
    }

    public void handleSearch(Player player, String targetName) {
        FileConfiguration config = plugin.getModuleManager().getModuleConfig("baltop");
        BaltopManager.BaltopEntry entry = null;
        for (BaltopManager.BaltopEntry e : manager.getCachedTop()) {
            if (e.name().equalsIgnoreCase(targetName)) {
                entry = e;
                break;
            }
        }

        if (entry != null) {
            int rank = manager.getCachedTop().indexOf(entry) + 1;
            String resultFormat = config.getString("gui.search-result", "&8「&dBaltop&8」 &e%rank%. &f%player% &7- &a%balance%$");
            player.sendMessage(FontUtils.parse(resultFormat
                    .replace("%rank%", String.valueOf(rank))
                    .replace("%player%", entry.name())
                    .replace("%balance%", FontUtils.formatMoney(entry.balance()))));
        } else {
            String notFound = config.getString("gui.not-found", "&8「&dBaltop&8」 &c%player% nebyl nalezen.");
            player.sendMessage(FontUtils.parse(notFound.replace("%player%", targetName)));
        }
    }

    private ItemStack createNav(String name, Material mat, List<Component> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(FontUtils.parse(name));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!(event.getInventory().getHolder() instanceof BaltopGuiHolder)) return;

        event.setCancelled(true);
        int slot = event.getRawSlot();
        int page = playerPages.getOrDefault(player.getUniqueId(), 1);
        FileConfiguration config = plugin.getModuleManager().getModuleConfig("baltop");

        ConfigurationSection items = config.getConfigurationSection("gui.items");
        if (items == null) return;

        for (String key : items.getKeys(false)) {
            if (items.getInt(key + ".slot", -1) == slot) {
                switch (key.toLowerCase()) {
                    case "back" -> { if (page > 1) open(player, page - 1); }
                    case "next" -> {
                        ConfigurationSection entries = config.getConfigurationSection("gui.items.entries");
                        if (entries != null) {
                            String slotsStr = entries.getString("slots");
                            int perPage = slotsStr.split(",").length; // Very simplified
                            if (page * perPage < manager.getCachedTop().size()) open(player, page + 1);
                        }
                    }
                    case "search" -> {
                        player.closeInventory();
                        player.sendMessage(FontUtils.parse(config.getString("gui.search-prompt", "&8「&dBaltop&8」 &7Napiš jméno hráče do chatu:")));
                        plugin.getChatListener().setSearchMode(player.getUniqueId(), true);
                    }
                }
                break;
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof BaltopGuiHolder) {
            event.setCancelled(true);
        }
    }

    private static class BaltopGuiHolder implements InventoryHolder {
        private Inventory inventory;
        public void setInventory(Inventory inventory) { this.inventory = inventory; }
        @Override public @NotNull Inventory getInventory() { return inventory; }
    }
}
