package me.jules.magiocore;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;

public class HomeGui implements Listener {
    private final MagioCore plugin;
    private final HomeManager homeManager;

    public HomeGui(MagioCore plugin, HomeManager homeManager) {
        this.plugin = plugin;
        this.homeManager = homeManager;
    }

    public void open(Player player) {
        FileConfiguration config = plugin.getModuleManager().getModuleConfig("home");
        String title = config.getString("gui.title", "[#4498DB]ᴅᴏᴍᴏᴠʏ");

        HomeGuiHolder holder = new HomeGuiHolder();
        Inventory inv = Bukkit.createInventory(holder, 36, FontUtils.parse(title));
        holder.setInventory(inv);

        Map<Integer, Home> homes = homeManager.getHomes(player.getUniqueId());
        int maxHomes = PlaytimeUtils.getMaxHomes(player);

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

        for (int i = 0; i < 36; i++) {
            if (i < 9 || i >= 27 || i % 9 == 0 || i % 9 == 8) {
                inv.setItem(i, (i % 2 == 0) ? blackGlass : grayGlass);
            }
        }

        for (int i = 1; i <= 7; i++) {
            Home home = homes.get(i);
            boolean isLocked = i > maxHomes;

            if (isLocked) continue;

            // Bed (Teleport/Delete) - Row 2 (slots 10-16)
            if (home != null) {
                ItemStack bed = new ItemStack(Material.GREEN_BED);
                ItemMeta bedMeta = bed.getItemMeta();
                if (bedMeta != null) {
                    bedMeta.displayName(FontUtils.parse("[#00FF44]ᴅᴏᴍᴏᴠ #" + i, true));
                    bedMeta.lore(List.of(
                            FontUtils.parse("§7"),
                            FontUtils.parse("[#00FF44]ɪɴꜰᴏʀᴍᴀᴄᴇ:"),
                            FontUtils.parse("§7ᴋʟɪᴋɴɪ ᴘʀᴏ ᴛᴇʟᴇᴘᴏʀᴛᴀᴄɪ"),
                            FontUtils.parse("§7ɴᴀ ᴛᴇɴᴛᴏ ᴅᴏᴍᴏᴠsᴋý ʙᴏᴅ."),
                            FontUtils.parse("§7"),
                            FontUtils.parse("§7ʟᴇᴠýᴍ: [#00FF44]ᴛᴇʟᴇᴘᴏʀᴛᴏᴠᴀᴛ"),
                            FontUtils.parse("§7ᴘʀᴀᴠýᴍ: [#FF1010]sᴍᴀᴢᴀᴛ"),
                            FontUtils.parse("§7")
                    ));
                    bed.setItemMeta(bedMeta);
                }
                inv.setItem(i + 9, bed);
            }

            // Dye (Set) - Row 3 (slots 19-25)
            ItemStack dye = new ItemStack(home != null ? Material.LIME_DYE : Material.BLUE_DYE);
            ItemMeta dyeMeta = dye.getItemMeta();
            if (dyeMeta != null) {
                String dyeColor = home != null ? "[#00FF44]" : "[#00FBFF]";
                dyeMeta.displayName(FontUtils.parse(dyeColor + "ɴᴀsᴛᴀᴠɪᴛ ᴅᴏᴍᴏᴠ #" + i, true));
                dyeMeta.lore(List.of(
                        FontUtils.parse("§7"),
                        FontUtils.parse("§7ᴋʟɪᴋɴɪ ᴘʀᴏ ɴᴀsᴛᴀᴠᴇɴí"),
                        FontUtils.parse("§7ᴅᴏᴍᴏᴠᴀ ɴᴀ ᴛᴠᴏᴊɪ ᴘᴏᴢɪᴄɪ."),
                        FontUtils.parse("§7")
                ));
                dye.setItemMeta(dyeMeta);
            }
            inv.setItem(i + 18, dye);
        }

        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof HomeGuiHolder) && !(holder instanceof HomeConfirmHolder)) return;

        event.setCancelled(true);
        int slot = event.getRawSlot();
        int maxHomes = PlaytimeUtils.getMaxHomes(player);
        FileConfiguration config = plugin.getModuleManager().getModuleConfig("home");

        if (holder instanceof HomeGuiHolder) {
            if (slot >= 10 && slot <= 16) {
                int homeNum = slot - 9;
                if (homeNum > maxHomes) return;
                Home home = homeManager.getHome(player.getUniqueId(), homeNum);
                if (home != null) {
                    if (event.isLeftClick()) {
                        player.closeInventory();
                        String teleMsg = config.getString("messages.teleport", "[#00FBFF]ᴅᴏᴍᴏᴠ §7#%number% [#888888]» §7ᴛᴇʟᴇᴘᴏʀᴛᴜᴊɪ...").replace("%number%", String.valueOf(homeNum));
                        player.sendMessage(FontUtils.parse(teleMsg));
                        TeleportUtils.startTeleportCountdown(player, home.getLocation(), "ᴅᴏᴍᴏᴠ", plugin, success -> {
                        });
                    } else if (event.isRightClick()) {
                        openConfirm(player, homeNum);
                    }
                }
            } else if (slot >= 19 && slot <= 25) {
                int homeNum = slot - 18;
                if (homeNum > maxHomes) return;
                homeManager.setHome(player.getUniqueId(), homeNum, player.getLocation());
                String setMsg = config.getString("messages.set", "[#00FF44]ᴅᴏᴍᴏᴠ §7#%number% [#888888]» §7ɴᴀsᴛᴀᴠᴇɴᴏ").replace("%number%", String.valueOf(homeNum));
                player.sendMessage(FontUtils.parse(setMsg));
                player.closeInventory();
                open(player);
            }
        } else if (holder instanceof HomeConfirmHolder confirmHolder) {
            int homeNum = confirmHolder.homeNum;
            if (slot == 11) { // Confirm
                homeManager.deleteHome(player.getUniqueId(), homeNum);
                String delMsg = config.getString("messages.delete", "[#FF1010]ᴅᴏᴍᴏᴠ §7#%number% [#888888]» §7sᴍᴀᴢáɴᴏ").replace("%number%", String.valueOf(homeNum));
                player.sendMessage(FontUtils.parse(delMsg));
                player.closeInventory();
                open(player);
            } else if (slot == 15) { // Cancel
                open(player);
            }
        }
    }

    public void openConfirm(Player player, int homeNum) {
        HomeConfirmHolder holder = new HomeConfirmHolder(homeNum);
        Inventory inv = Bukkit.createInventory(holder, 27, FontUtils.parse("[#00FBFF]ᴏᴘʀᴀᴠᴅᴜ sᴍᴀᴢᴀᴛ ᴅᴏᴍᴏᴠ?", true));
        holder.setInventory(inv);

        ItemStack blackGlass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta blackMeta = blackGlass.getItemMeta();
        if (blackMeta != null) {
            blackMeta.displayName(Component.empty());
            blackGlass.setItemMeta(blackMeta);
        }
        for (int i = 0; i < 27; i++) inv.setItem(i, blackGlass);

        ItemStack confirm = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta confirmMeta = confirm.getItemMeta();
        if (confirmMeta != null) {
            confirmMeta.displayName(FontUtils.parse("[#00FF44]ᴘᴏᴛᴠʀᴅɪᴛ sᴍᴀᴢáɴí", true));
            confirm.setItemMeta(confirmMeta);
        }
        inv.setItem(11, confirm);

        ItemStack cancel = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta cancelMeta = cancel.getItemMeta();
        if (cancelMeta != null) {
            cancelMeta.displayName(FontUtils.parse("[#FF1010]ᴢʀᴜšɪᴛ", true));
            cancel.setItemMeta(cancelMeta);
        }
        inv.setItem(15, cancel);

        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof HomeGuiHolder || event.getInventory().getHolder() instanceof HomeConfirmHolder) {
            event.setCancelled(true);
        }
    }

    private static class HomeGuiHolder implements InventoryHolder {
        private Inventory inventory;
        public void setInventory(Inventory inventory) { this.inventory = inventory; }
        @Override public @NotNull Inventory getInventory() { return inventory; }
    }

    private static class HomeConfirmHolder implements InventoryHolder {
        public final int homeNum;
        private Inventory inventory;
        public HomeConfirmHolder(int homeNum) { this.homeNum = homeNum; }
        public void setInventory(Inventory inventory) { this.inventory = inventory; }
        @Override public @NotNull Inventory getInventory() { return inventory; }
    }
}
