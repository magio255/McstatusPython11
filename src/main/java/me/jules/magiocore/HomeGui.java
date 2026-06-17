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
        String title = config.getString("gui.title", "&#EA427F» ᴍᴇɴᴜ ᴅᴏᴍᴏᴠů");

        HomeGuiHolder holder = new HomeGuiHolder();
        Inventory inv = Bukkit.createInventory(holder, 45, FontUtils.parse(title));
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

        for (int i = 0; i < 45; i++) {
            if (i < 9 || i >= 36 || i % 9 == 0 || i % 9 == 8) {
                inv.setItem(i, glass);
            }
        }

        for (int i = 1; i <= 7; i++) {
            Home home = homes.get(i);
            boolean isLocked = i > maxHomes;

            // Bed (Teleport) - Row 2 (slots 10-16)
            Material bedMaterial = (home != null) ? Material.BLUE_BED : Material.GREEN_BED;
            String nameColor = (home != null) ? "&#00fbff" : "&#00ff44";

            ItemStack bed = new ItemStack(bedMaterial);
            ItemMeta bedMeta = bed.getItemMeta();
            if (bedMeta != null) {
                bedMeta.displayName(FontUtils.parse(nameColor + "ᴅᴏᴍᴏᴠ §7#" + i + (isLocked ? " §8(ᴢᴀᴍčᴇɴᴏ)" : "")));
                if (isLocked) {
                    String lockedMsg = config.getString("messages.locked", "§cɴᴇᴍáš ᴏᴘʀáᴠɴěɴí ɴᴀ ᴅᴀʟší ᴅᴏᴍᴏᴠʏ. §7(ʟɪᴍɪᴛ: %limit%)").replace("%limit%", String.valueOf(maxHomes));
                    String buyMore = config.getString("messages.buy-more", "§7ᴘʀᴏ ᴠíᴄᴇ ᴅᴏᴍᴏᴠů sɪ ᴋᴜᴘ ʀᴀɴᴋ ɴᴀ &#F1C40F/sᴛᴏʀᴇ");
                    bedMeta.lore(List.of(FontUtils.parse(lockedMsg), FontUtils.parse(buyMore)));
                } else if (home != null) {
                    bedMeta.lore(List.of(FontUtils.parse("§7ᴋʟɪᴋɴɪ ᴘʀᴏ ᴛᴇʟᴇᴘᴏʀᴛᴀᴄɪ")));
                } else {
                    String notSetMsg = config.getString("messages.not-set", "§cᴅᴏᴍᴏᴠ ɴᴇɴí ɴᴀsᴛᴀᴠᴇɴ");
                    bedMeta.lore(List.of(FontUtils.parse(notSetMsg)));
                }
                bed.setItemMeta(bedMeta);
            }
            inv.setItem(i + 9, bed);

            // Pearl (Set) - Row 3 (slots 19-25)
            ItemStack pearl = new ItemStack(Material.ENDER_PEARL);
            ItemMeta pearlMeta = pearl.getItemMeta();
            if (pearlMeta != null) {
                pearlMeta.displayName(FontUtils.parse(isLocked ? "§8ɴᴀsᴛᴀᴠɪᴛ ᴅᴏᴍᴏᴠ §7#" + i : "&#EA427Fɴᴀsᴛᴀᴠɪᴛ ᴅᴏᴍᴏᴠ §7#" + i));
                if (isLocked) {
                    String lockedMsg = config.getString("messages.locked", "§cɴᴇᴍáš ᴏᴘʀáᴠɴěɴí ɴᴀ ᴅᴀʟší ᴅᴏᴍᴏᴠʏ. §7(ʟɪᴍɪᴛ: %limit%)").replace("%limit%", String.valueOf(maxHomes));
                    String buyMore = config.getString("messages.buy-more", "§7ᴘʀᴏ ᴠíᴄᴇ ᴅᴏᴍᴏᴠů sɪ ᴋᴜᴘ ʀᴀɴᴋ ɴᴀ &#F1C40F/sᴛᴏʀᴇ");
                    pearlMeta.lore(List.of(FontUtils.parse(lockedMsg), FontUtils.parse(buyMore)));
                } else {
                    pearlMeta.lore(List.of(FontUtils.parse("§7ᴋʟɪᴋɴɪ ᴘʀᴏ ɴᴀsᴛᴀᴠᴇɴí ᴅᴏᴍᴏᴠᴀ")));
                }
                pearl.setItemMeta(pearlMeta);
            }
            inv.setItem(i + 18, pearl);

            // Barrier (Delete) - Row 4 (slots 28-34)
            ItemStack barrier = new ItemStack(Material.BARRIER);
            ItemMeta barrierMeta = barrier.getItemMeta();
            if (barrierMeta != null) {
                barrierMeta.displayName(FontUtils.parse(isLocked ? "§8sᴍᴀᴢᴀᴛ ᴅᴏᴍᴏᴠ §7#" + i : "§csᴍᴀᴢᴀᴛ ᴅᴏᴍᴏᴠ §7#" + i));
                if (isLocked) {
                    String lockedMsg = config.getString("messages.locked", "§cɴᴇᴍáš ᴏᴘʀáᴠɴěɴí ɴᴀ ᴅᴀʟší ᴅᴏᴍᴏᴠʏ. §7(ʟɪᴍɪᴛ: %limit%)").replace("%limit%", String.valueOf(maxHomes));
                    String buyMore = config.getString("messages.buy-more", "§7ᴘʀᴏ ᴠíᴄᴇ ᴅᴏᴍᴏᴠů sɪ ᴋᴜᴘ ʀᴀɴᴋ ɴᴀ &#F1C40F/sᴛᴏʀᴇ");
                    barrierMeta.lore(List.of(FontUtils.parse(lockedMsg), FontUtils.parse(buyMore)));
                } else if (home != null) {
                    barrierMeta.lore(List.of(FontUtils.parse("§7ᴋʟɪᴋɴɪ ᴘʀᴏ sᴍᴀᴢáɴí ᴅᴏᴍᴏᴠᴀ")));
                } else {
                    barrierMeta.lore(List.of(FontUtils.parse("§cᴅᴏᴍᴏᴠ ɴᴇɴí ɴᴀsᴛᴀᴠᴇɴ")));
                }
                barrier.setItemMeta(barrierMeta);
            }
            inv.setItem(i + 27, barrier);
        }

        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!(event.getInventory().getHolder() instanceof HomeGuiHolder)) return;

        event.setCancelled(true);
        int slot = event.getRawSlot();
        int maxHomes = PlaytimeUtils.getMaxHomes(player);

        FileConfiguration config = plugin.getModuleManager().getModuleConfig("home");
        if (slot >= 10 && slot <= 16) {
            int homeNum = slot - 9;
            if (homeNum > maxHomes) {
                String lockedMsg = config.getString("messages.locked", "§cɴᴇᴍáš ᴏᴘʀáᴠɴěɴí ɴᴀ ᴅᴀʟší ᴅᴏᴍᴏᴠʏ. §7(ʟɪᴍɪᴛ: %limit%)").replace("%limit%", String.valueOf(maxHomes));
                String buyMore = config.getString("messages.buy-more", "§7ᴘʀᴏ ᴠíᴄᴇ ᴅᴏᴍᴏᴠů sɪ ᴋᴜᴘ ʀᴀɴᴋ ɴᴀ &#F1C40F/sᴛᴏʀᴇ");
                player.sendMessage(FontUtils.parse(lockedMsg));
                player.sendMessage(FontUtils.parse(buyMore));
                return;
            }
            Home home = homeManager.getHome(player.getUniqueId(), homeNum);
            if (home != null) {
                player.closeInventory();
                String teleMsg = config.getString("messages.teleport", "&#00fbffᴅᴏᴍᴏᴠ §7#%number% &#888888» §7Teleportuji...").replace("%number%", String.valueOf(homeNum));
                player.sendMessage(FontUtils.parse(teleMsg));
                TeleportUtils.startTeleportCountdown(player, home.getLocation(), "ᴅᴏᴍᴏᴠ", plugin, success -> {});
            } else {
                String notSetMsg = config.getString("messages.not-set", "§cᴅᴏᴍᴏᴠ ɴᴇɴí ɴᴀsᴛᴀᴠᴇɴ");
                player.sendMessage(FontUtils.parse(notSetMsg));
            }
        } else if (slot >= 19 && slot <= 25) {
            int homeNum = slot - 18;
            if (homeNum > maxHomes) {
                String lockedMsg = config.getString("messages.locked", "§cɴᴇᴍáš ᴏᴘʀáᴠɴěɴí ɴᴀ ᴅᴀʟší ᴅᴏᴍᴏᴠʏ. §7(ʟɪᴍɪᴛ: %limit%)").replace("%limit%", String.valueOf(maxHomes));
                String buyMore = config.getString("messages.buy-more", "§7ᴘʀᴏ ᴠíᴄᴇ ᴅᴏᴍᴏᴠů sɪ ᴋᴜᴘ ʀᴀɴᴋ ɴᴀ &#F1C40F/sᴛᴏʀᴇ");
                player.sendMessage(FontUtils.parse(lockedMsg));
                player.sendMessage(FontUtils.parse(buyMore));
                return;
            }
            homeManager.setHome(player.getUniqueId(), homeNum, player.getLocation());
            String setMsg = config.getString("messages.set", "&#00ff44ᴅᴏᴍᴏᴠ §7#%number% &#888888» §7Nastaveno").replace("%number%", String.valueOf(homeNum));
            player.sendMessage(FontUtils.parse(setMsg));
            player.closeInventory();
            open(player); // Refresh
        } else if (slot >= 28 && slot <= 34) {
            int homeNum = slot - 27;
            if (homeNum > maxHomes) {
                String lockedMsg = config.getString("messages.locked", "§cɴᴇᴍáš ᴏᴘʀáᴠɴěɴí ɴᴀ ᴅᴀʟší ᴅᴏᴍᴏᴠʏ. §7(ʟɪᴍɪᴛ: %limit%)").replace("%limit%", String.valueOf(maxHomes));
                String buyMore = config.getString("messages.buy-more", "§7ᴘʀᴏ ᴠíᴄᴇ ᴅᴏᴍᴏᴠů sɪ ᴋᴜᴘ ʀᴀɴᴋ ɴᴀ &#F1C40F/sᴛᴏʀᴇ");
                player.sendMessage(FontUtils.parse(lockedMsg));
                player.sendMessage(FontUtils.parse(buyMore));
                return;
            }

            Home home = homeManager.getHome(player.getUniqueId(), homeNum);
            if (home != null) {
                homeManager.deleteHome(player.getUniqueId(), homeNum);
                String delMsg = config.getString("messages.delete", "§cᴅᴏᴍᴏᴠ §7#%number% &#888888» §7Smazáno").replace("%number%", String.valueOf(homeNum));
                player.sendMessage(FontUtils.parse(delMsg));
                player.closeInventory();
                open(player); // Refresh
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof HomeGuiHolder) {
            event.setCancelled(true);
        }
    }

    private static class HomeGuiHolder implements InventoryHolder {
        private Inventory inventory;
        public void setInventory(Inventory inventory) { this.inventory = inventory; }
        @Override public @NotNull Inventory getInventory() { return inventory; }
    }
}
