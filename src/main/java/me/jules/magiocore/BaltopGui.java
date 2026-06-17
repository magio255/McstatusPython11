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
        String title = config.getString("gui.title", "&#EA427F» ʙᴀʟᴛᴏᴘ");

        Inventory inv = Bukkit.createInventory(new BaltopGuiHolder(), 54, FontUtils.parse(title));
        List<BaltopManager.BaltopEntry> top = manager.getCachedTop();

        int maxPerPage = 28; // 4 rows of 7
        int start = (page - 1) * maxPerPage;
        if (start >= top.size() && !top.isEmpty()) {
            page = (int) Math.ceil((double) top.size() / maxPerPage);
            start = (page - 1) * maxPerPage;
        }
        playerPages.put(player.getUniqueId(), page);

        // Border Design
        ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        if (glassMeta != null) {
            glassMeta.displayName(Component.empty());
            glass.setItemMeta(glassMeta);
        }

        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                inv.setItem(i, glass);
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
                    meta.displayName(FontUtils.parse("&#ffbb00" + (index + 1) + ". §f" + entry.name()));
                    meta.lore(List.of(FontUtils.parse("§7" + "ʙᴀʟᴀɴᴄᴇ" + ": &#00ff44" + FontUtils.formatMoney(entry.balance()) + " $")));
                    head.setItemMeta(meta);
                }
                inv.setItem(slots[i], head);
            }
        }

        // Navigation
        inv.setItem(48, createNav(config.getString("gui.nav-back", "§c" + "ᴢᴘěᴛ"), Material.ARROW));
        inv.setItem(49, createNav(config.getString("gui.nav-search", "&#ffbb00" + "ʜʟᴇᴅᴀᴛ ʜʀáčᴇ"), Material.OAK_SIGN));
        inv.setItem(50, createNav(config.getString("gui.nav-next", "&#00ff44" + "ᴅᴀʟší"), Material.ARROW));

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
            String resultFormat = config.getString("gui.search-result", "&#EA427Fʙᴀʟᴛᴏᴘ &#888888» &#ffbb00%rank%. §f%player% §7- &#00ff44%balance% $");
            player.sendMessage(FontUtils.parse(resultFormat
                    .replace("%rank%", String.valueOf(rank))
                    .replace("%player%", entry.name())
                    .replace("%balance%", FontUtils.formatMoney(entry.balance()))));
        } else {
            String notFound = config.getString("gui.not-found", "&#EA427Fʙᴀʟᴛᴏᴘ &#888888» §cʜʀáč ɴᴇʙʏʟ ɴᴀʟᴇᴢᴇɴ.");
            player.sendMessage(FontUtils.parse(notFound));
        }
    }

    private ItemStack createNav(String name, Material mat) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(FontUtils.parse(name));
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
            player.sendMessage(FontUtils.parse(config.getString("gui.search-prompt", "&#EA427Fʙᴀʟᴛᴏᴘ &#888888» §fɴᴀᴘɪš ᴊᴍéɴᴏ ʜʀáčᴇ ᴅᴏ ᴄʜᴀᴛᴜ:")));
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
        @Override
        public @NotNull Inventory getInventory() { return null; }
    }
}
