package me.jules.magiocore;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class HomeGui implements Listener {
    private final MagioCore plugin;
    private final HomeManager homeManager;
    private final String title = "&#EA427F» " + "ᴍᴇɴᴜ ᴅᴏᴍᴏᴠů";

    public HomeGui(MagioCore plugin, HomeManager homeManager) {
        this.plugin = plugin;
        this.homeManager = homeManager;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(new HomeGuiHolder(), 36, FontUtils.parse(title));
        Map<Integer, Home> homes = homeManager.getHomes(player.getUniqueId());
        int maxHomes = PlaytimeUtils.getMaxHomes(player);

        // Border Design
        ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.displayName(Component.empty());
        glass.setItemMeta(glassMeta);

        for (int i = 0; i < 36; i++) {
            if (i < 9 || i >= 27 || i % 9 == 0 || i % 9 == 8) {
                inv.setItem(i, glass);
            }
        }

        for (int i = 1; i <= 7; i++) {
            Home home = homes.get(i);
            boolean isLocked = i > maxHomes;

            // Bed (Teleport) - Row 2 (slots 10-16)
            Material bedMaterial = isLocked ? Material.BARRIER : ((home != null) ? Material.BLUE_BED : Material.GREEN_BED);
            String nameColor = isLocked ? "§8" : ((home != null) ? "&#00fbff" : "&#00ff44");

            ItemStack bed = new ItemStack(bedMaterial);
            ItemMeta bedMeta = bed.getItemMeta();
            // User requested '#' not to be highlighted. We'll use §7 for it.
            bedMeta.displayName(FontUtils.parse(nameColor + "ᴅᴏᴍᴏᴠ §7#" + i + (isLocked ? " (ᴢᴀᴍčᴇɴᴏ)" : "")));
            if (isLocked) {
                bedMeta.lore(List.of(FontUtils.parse("§c" + "ʟɪᴍɪᴛ ᴊᴇ " + maxHomes)));
            } else if (home != null) {
                bedMeta.lore(List.of(FontUtils.parse("§7" + "ᴋʟɪᴋɴɪ ᴘʀᴏ ᴛᴇʟᴇᴘᴏʀᴛᴀᴄɪ")));
            } else {
                bedMeta.lore(List.of(FontUtils.parse("§c" + "ᴅᴏᴍᴏᴠ ɴᴇɴí ɴᴀsᴛᴀᴠᴇɴ")));
            }
            bed.setItemMeta(bedMeta);
            inv.setItem(i + 9, bed);

            // Pearl (Set) - Row 3 (slots 19-25)
            ItemStack pearl = new ItemStack(isLocked ? Material.BARRIER : Material.ENDER_PEARL);
            ItemMeta pearlMeta = pearl.getItemMeta();
            pearlMeta.displayName(FontUtils.parse(isLocked ? "§8" + "ɴᴀsᴛᴀᴠɪᴛ ᴅᴏᴍᴏᴠ §7#" + i : "&#EA427F" + "ɴᴀsᴛᴀᴠɪᴛ ᴅᴏᴍᴏᴠ §7#" + i));
            if (isLocked) {
                pearlMeta.lore(List.of(FontUtils.parse("§c" + "ʟɪᴍɪᴛ ᴊᴇ " + maxHomes)));
            } else {
                pearlMeta.lore(List.of(FontUtils.parse("§7" + "ᴋʟɪᴋɴɪ ᴘʀᴏ ɴᴀsᴛᴀᴠᴇɴí ᴅᴏᴍᴏᴠᴀ")));
            }
            pearl.setItemMeta(pearlMeta);
            inv.setItem(i + 18, pearl);
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

        if (slot >= 10 && slot <= 16) {
            int homeNum = slot - 9;
            if (homeNum > maxHomes) {
                player.sendMessage(FontUtils.parse("§c" + "ᴛᴇɴᴛᴏ sʟᴏᴛ ᴊᴇ ᴢᴀᴍčᴇɴý" + " §7(" + "ʟɪᴍɪᴛ" + ": " + maxHomes + ")."));
                return;
            }
            Home home = homeManager.getHome(player.getUniqueId(), homeNum);
            if (home != null) {
                player.closeInventory();
                TeleportUtils.startTeleportCountdown(player, home.getLocation(), plugin, success -> {});
            } else {
                player.sendMessage(FontUtils.parse("§c" + "ᴅᴏᴍᴏᴠ ɴᴇɴí ɴᴀsᴛᴀᴠᴇɴ" + "."));
            }
        } else if (slot >= 19 && slot <= 25) {
            int homeNum = slot - 18;
            if (homeNum > maxHomes) {
                player.sendMessage(FontUtils.parse("§c" + "ᴛᴇɴᴛᴏ sʟᴏᴛ ᴊᴇ ᴢᴀᴍčᴇɴý" + " §7(" + "ʟɪᴍɪᴛ" + ": " + maxHomes + ")."));
                return;
            }
            homeManager.setHome(player.getUniqueId(), homeNum, player.getLocation());
            player.sendMessage(FontUtils.parse("&#00ff44" + "ᴅᴏᴍᴏᴠ #" + homeNum + " ɴᴀsᴛᴀᴠᴇɴ"));
            player.closeInventory();
            open(player); // Refresh
        }
    }

    private static class HomeGuiHolder implements InventoryHolder {
        @Override
        public @NotNull Inventory getInventory() {
            return null;
        }
    }
}
