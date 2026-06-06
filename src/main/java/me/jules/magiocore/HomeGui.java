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
        String title = config.getString("gui.title", "&#4498DB&lDomovy");

        HomeGuiHolder holder = new HomeGuiHolder();
        Inventory inv = Bukkit.createInventory(holder, 36, FontUtils.parse(title));
        holder.setInventory(inv);

        Map<Integer, Home> homes = homeManager.getHomes(player.getUniqueId());
        int maxHomes = PlaytimeUtils.getMaxHomes(player);

        // Border Design
        ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        if (glassMeta != null) {
            glassMeta.displayName(Component.empty());
            glass.setItemMeta(glassMeta);
        }

        for (int i = 0; i < 36; i++) {
            if (i < 9 || i >= 27 || i % 9 == 0 || i % 9 == 8) {
                inv.setItem(i, glass);
            }
        }

        for (int i = 1; i <= 7; i++) {
            Home home = homes.get(i);
            boolean isLocked = i > maxHomes;

            // Bed (Teleport/Delete) - Row 2 (slots 10-16)
            ItemStack bed = new ItemStack(home != null ? Material.GREEN_BED : Material.BLUE_BED);
            ItemMeta bedMeta = bed.getItemMeta();
            if (bedMeta != null) {
                String bedColor = home != null ? "&#00ff44" : "&#00fbff";
                bedMeta.displayName(FontUtils.parse(bedColor + "Domov " + i + (isLocked ? " §8(Zamčeno)" : ""), true));
                if (isLocked) {
                    String lockedMsg = config.getString("messages.locked", "§cɴᴇᴍáš ᴏᴘʀáᴠɴěɴí ɴᴀ ᴅᴀʟší ᴅᴏᴍᴏᴠʏ. §7(ʟɪᴍɪᴛ: %limit%)").replace("%limit%", String.valueOf(maxHomes));
                    String buyMore = config.getString("messages.buy-more", "§7ᴘʀᴏ ᴠíᴄᴇ ᴅᴏᴍᴏᴠů sɪ ᴋᴜᴘ ʀᴀɴᴋ ɴᴀ &#F1C40F/sᴛᴏʀᴇ");
                    bedMeta.lore(List.of(FontUtils.parse(lockedMsg), FontUtils.parse(buyMore)));
                } else if (home != null) {
                    bedMeta.lore(List.of(
                            FontUtils.parse("§7Levým teleport na domov"),
                            FontUtils.parse("§7Pravým smazat")
                    ));
                } else {
                    bedMeta.lore(List.of(FontUtils.parse("§c§lDomov není nastaven", false)));
                }
                bed.setItemMeta(bedMeta);
            }
            inv.setItem(i + 9, bed);

            // Dye (Set) - Row 3 (slots 19-25)
            ItemStack dye = new ItemStack(home != null ? Material.LIME_DYE : Material.BLUE_DYE);
            ItemMeta dyeMeta = dye.getItemMeta();
            if (dyeMeta != null) {
                String dyeColor = home != null ? "&#00ff44" : "&#00fbff";
                dyeMeta.displayName(FontUtils.parse(isLocked ? "§8Nastavit domov " + i : dyeColor + "Nastavit domov " + i, true));
                if (isLocked) {
                    String lockedMsg = config.getString("messages.locked", "§cɴᴇᴍáš ᴏᴘʀáᴠɴěɴí ɴᴀ ᴅᴀʟší ᴅᴏᴍᴏᴠʏ. §7(ʟɪᴍɪᴛ: %limit%)").replace("%limit%", String.valueOf(maxHomes));
                    String buyMore = config.getString("messages.buy-more", "§7ᴘʀᴏ ᴠíᴄᴇ ᴅᴏᴍᴏᴠů sɪ ᴋᴜᴘ ʀᴀɴᴋ ɴᴀ &#F1C40F/sᴛᴏʀᴇ");
                    dyeMeta.lore(List.of(FontUtils.parse(lockedMsg), FontUtils.parse(buyMore)));
                } else {
                    dyeMeta.lore(List.of(FontUtils.parse("§7Klikni pro nastavení domova")));
                }
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
                        String teleMsg = config.getString("messages.teleport", "&#00fbffᴅᴏᴍᴏᴠ §7#%number% &#888888» §7Teleportuji...").replace("%number%", String.valueOf(homeNum));
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
                String setMsg = config.getString("messages.set", "&#00ff44ᴅᴏᴍᴏᴠ §7#%number% &#888888» §7Nastaveno").replace("%number%", String.valueOf(homeNum));
                player.sendMessage(FontUtils.parse(setMsg));
                player.closeInventory();
                open(player);
            }
        } else if (holder instanceof HomeConfirmHolder confirmHolder) {
            int homeNum = confirmHolder.homeNum;
            if (slot == 11) { // Confirm
                homeManager.deleteHome(player.getUniqueId(), homeNum);
                String delMsg = config.getString("messages.delete", "§cᴅᴏᴍᴏᴠ §7#%number% &#888888» §7Smazáno").replace("%number%", String.valueOf(homeNum));
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
        Inventory inv = Bukkit.createInventory(holder, 27, FontUtils.parse("&#00fbffOpravdu chceš smazat domov?", true));
        holder.setInventory(inv);

        ItemStack confirm = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta confirmMeta = confirm.getItemMeta();
        if (confirmMeta != null) {
            confirmMeta.displayName(FontUtils.parse("&#00ff44Potvrdit smazání", true));
            confirm.setItemMeta(confirmMeta);
        }
        inv.setItem(11, confirm);

        ItemStack cancel = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta cancelMeta = cancel.getItemMeta();
        if (cancelMeta != null) {
            cancelMeta.displayName(FontUtils.parse("§cZrušit", true));
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
