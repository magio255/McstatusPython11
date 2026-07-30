package me.jules.apexbuild;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class HistoryGUI implements InventoryHolder {
    private final DataManager dataManager;
    private final int page;
    private final String filter;
    private final Inventory inventory;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    public HistoryGUI(DataManager dataManager, int page, String filter) {
        this.dataManager = dataManager;
        this.page = page;
        this.filter = filter;
        this.inventory = Bukkit.createInventory(this, 54, Component.text("Historie Připojení - Strana " + (page + 1)));
        populate();
    }

    private void populate() {
        List<JoinEntry> allHistory = dataManager.getHistory();
        if (filter != null && !filter.isEmpty()) {
            allHistory = allHistory.stream()
                    .filter(entry -> entry.getPlayerName().toLowerCase().contains(filter.toLowerCase()))
                    .collect(Collectors.toList());
        }

        int start = page * 45;
        int end = Math.min(start + 45, allHistory.size());

        for (int i = start; i < end; i++) {
            JoinEntry entry = allHistory.get(i);
            inventory.setItem(i - start, createHead(entry));
        }

        // Navigation and Search
        if (page > 0) {
            inventory.setItem(45, createItem(Material.ARROW, "Předchozí Strana"));
        }

        inventory.setItem(49, createItem(Material.OAK_SIGN, "Hledat hráče",
                filter == null ? "Klikni pro vyhledávání" : "Aktuální filtr: " + filter,
                "Klikni pro nový filtr",
                "Klikni pravým pro zrušení filtru"));

        if (end < allHistory.size()) {
            inventory.setItem(53, createItem(Material.ARROW, "Další Strana"));
        }
    }

    private ItemStack createHead(JoinEntry entry) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(Bukkit.getOfflinePlayer(entry.getPlayerUUID()));
            meta.displayName(Component.text(entry.getPlayerName(), NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("IP: ", NamedTextColor.GRAY).append(Component.text(entry.getIpAddress(), NamedTextColor.WHITE)).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Čas: ", NamedTextColor.GRAY).append(Component.text(DATE_FORMAT.format(new Date(entry.getTimestamp())), NamedTextColor.WHITE)).decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
            head.setItemMeta(meta);
        }
        return head;
    }

    private ItemStack createItem(Material material, String name, String... loreLines) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(name, NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
            if (loreLines.length > 0) {
                List<Component> lore = new ArrayList<>();
                for (String line : loreLines) {
                    lore.add(Component.text(line, NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
                }
                meta.lore(lore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public int getPage() {
        return page;
    }

    public String getFilter() {
        return filter;
    }
}
