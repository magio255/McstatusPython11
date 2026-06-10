package me.jules.magiocore;

import org.bukkit.Bukkit;
import org.bukkit.Material;
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
        String title = config.getString("gui.title", "&8Baltop");

        BaltopGuiHolder holder = new BaltopGuiHolder();
        Inventory inv = Bukkit.createInventory(holder, 54, FontUtils.parse(title));
        holder.setInventory(inv);
        List<BaltopManager.BaltopEntry> top = manager.getCachedTop();

        int maxPerPage = 28; // 4 rows of 7
        int start = (page - 1) * maxPerPage;
        if (start >= top.size() && !top.isEmpty()) {
            page = (int) Math.ceil((double) top.size() / maxPerPage);
            start = (page - 1) * maxPerPage;
        }
        playerPages.put(player.getUniqueId(), page);

        // Glassmorphism Border Design
        ItemStack blackGlass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta blackMeta = blackGlass.getItemMeta();
        if (blackMeta != null) {
            blackMeta.displayName(Component.empty());
            blackGlass.setItemMeta(blackMeta);
        }

        ItemStack grayGlass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta grayMeta = grayGlass.getItemMeta();
        if (grayMeta != null) {
            grayMeta.displayName(Component.empty());
            grayGlass.setItemMeta(grayMeta);
        }

        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                inv.setItem(i, (i % 2 == 0) ? blackGlass : grayGlass);
            }
        }

        // Available slots for entries: 10-16, 19-25, 28-34, 37-43
        int[] slots = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
        };

        for (int i = 0; i < slots.length; i++) {
            int index = start + i;
            if (index < top.size()) {
                BaltopManager.BaltopEntry entry = top.get(index);
                ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta meta = (SkullMeta) head.getItemMeta();

                if (meta != null) {
                    meta.setOwningPlayer(Bukkit.getOfflinePlayer(entry.uuid()));
                    int rank = index + 1;
                    meta.displayName(FontUtils.parse(config.getString("gui.entry.name", "&e&l%rank%. &f%player%")
                            .replace("%rank%", String.valueOf(rank))
                            .replace("%player%", entry.name())));

                    List<Component> lore = config.getStringList("gui.entry.lore").stream()
                            .map(s -> s.replace("%rank%", String.valueOf(rank))
                                    .replace("%balance%", FontUtils.formatMoney(entry.balance())))
                            .map(FontUtils::parse)
                            .toList();
                    meta.lore(lore);
                    head.setItemMeta(meta);
                }
                inv.setItem(slots[i], head);
            }
        }

        // Navigation
        inv.setItem(48, createNav(config.getString("gui.nav-back", "&c&lZPĚT"), Material.ARROW, List.of(
                FontUtils.parse("&7Vrátí tě na předchozí"),
                FontUtils.parse("&7stránku se seznamem."),
                Component.empty(),
                FontUtils.parse("&cInformace:"),
                FontUtils.parse(" &fKlikni pro přechod"),
                FontUtils.parse(" &fna předchozí stranu."),
                Component.empty(),
                FontUtils.parse("&c▶ &lKLIKNI &cPro přechod!")
        )));
        inv.setItem(49, createNav(config.getString("gui.nav-search", "&e&lHLEDAT HRÁČE"), Material.OAK_SIGN, List.of(
                FontUtils.parse("&7Umožňuje ti najít konkrétního"),
                FontUtils.parse("&7hráče a jeho zůstatek."),
                Component.empty(),
                FontUtils.parse("&eInformace:"),
                FontUtils.parse(" &fKlikni pro vyhledání"),
                FontUtils.parse(" &fkonkrétního hráče."),
                Component.empty(),
                FontUtils.parse("&e▶ &lKLIKNI &ePro vyhledání!")
        )));
        inv.setItem(50, createNav(config.getString("gui.nav-next", "&a&lDALŠÍ"), Material.ARROW, List.of(
                FontUtils.parse("&7Posune tě na další"),
                FontUtils.parse("&7stránku se seznamem."),
                Component.empty(),
                FontUtils.parse("&aInformace:"),
                FontUtils.parse(" &fKlikni pro přechod"),
                FontUtils.parse(" &fna další stranu."),
                Component.empty(),
                FontUtils.parse("&a▶ &lKLIKNI &aPro přechod!")
        )));

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

        if (slot == 48) { // Back
            if (page > 1) open(player, page - 1);
        } else if (slot == 50) { // Next
            if (page * 28 < manager.getCachedTop().size()) open(player, page + 1);
        } else if (slot == 49) { // Search
            FileConfiguration config = plugin.getModuleManager().getModuleConfig("baltop");
            player.closeInventory();
            player.sendMessage(FontUtils.parse(config.getString("gui.search-prompt", "&8「&dBaltop&8」 &7Napiš jméno hráče do chatu:")));
            plugin.getChatListener().setSearchMode(player.getUniqueId(), true);
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
