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
        String title = config.getString("gui.title", "&8Domovy");

        HomeGuiHolder holder = new HomeGuiHolder();
        Inventory inv = Bukkit.createInventory(holder, 36, FontUtils.parse(title));
        holder.setInventory(inv);

        Map<Integer, Home> homes = homeManager.getHomes(player.getUniqueId());
        int maxHomes = PlaytimeUtils.getMaxHomes(player);

        // Glassmorphism Border Design
        ItemStack blackGlass = createItem(Material.BLACK_STAINED_GLASS_PANE, " ", null);
        ItemStack grayGlass = createItem(Material.GRAY_STAINED_GLASS_PANE, " ", null);

        for (int i = 0; i < 36; i++) {
            if (i < 9 || i >= 27 || i % 9 == 0 || i % 9 == 8) {
                inv.setItem(i, (i % 2 == 0) ? blackGlass : grayGlass);
            }
        }

        for (int i = 1; i <= 7; i++) {
            Home home = homes.get(i);
            boolean isLocked = i > maxHomes;

            int bedSlot = i + 9;
            int dyeSlot = i + 18;

            if (isLocked) {
                inv.setItem(bedSlot, createItem(Material.BARRIER, "&c&lZAMČENO", List.of(
                        FontUtils.parse("&8ᴘᴏᴘɪѕ"),
                        FontUtils.parse("&7Tento slot pro domov"),
                        FontUtils.parse("&7je pro tebe uzamčen."),
                        Component.empty(),
                        FontUtils.parse("&#ff6969Informace:"),
                        FontUtils.parse(" &fPotřebuješ vyšší"),
                        FontUtils.parse(" &fodehraný čas."),
                        Component.empty(),
                        FontUtils.parse("&e▶ Klikni&f pro informace")
                )));
                inv.setItem(dyeSlot, createItem(Material.GRAY_DYE, "&8&lZAMČENO", List.of(
                        FontUtils.parse("&8ᴘᴏᴘɪѕ"),
                        FontUtils.parse("&7Tento slot pro domov"),
                        FontUtils.parse("&7je pro tebe uzamčen.")
                )));
                continue;
            }

            // Bed Row
            if (home != null) {
                inv.setItem(bedSlot, createItem(Material.GREEN_BED, "&a&lDOMOV #" + i, List.of(
                        FontUtils.parse("&8ᴘᴏᴘɪѕ"),
                        FontUtils.parse("&7Klikni pro teleportaci"),
                        FontUtils.parse("&7na tento domovský bod."),
                        Component.empty(),
                        FontUtils.parse("&#ff6969Informace:"),
                        FontUtils.parse(" &fLevý klik: &aTeleportovat"),
                        FontUtils.parse(" &fPravý klik: &cSmazat"),
                        Component.empty(),
                        FontUtils.parse("&e▶ Klikni&f pro teleport")
                )));
            } else {
                inv.setItem(bedSlot, createItem(Material.LIGHT_BLUE_BED, "&b&lDOMOV #" + i, List.of(
                        FontUtils.parse("&8ᴘᴏᴘɪѕ"),
                        FontUtils.parse("&7Tento domov zatím"),
                        FontUtils.parse("&7není nastaven."),
                        Component.empty(),
                        FontUtils.parse("&#ff6969Informace:"),
                        FontUtils.parse(" &fKlikni na barvivo"),
                        FontUtils.parse(" &fníže pro nastavení."),
                        Component.empty(),
                        FontUtils.parse("&e▶ Klikni&f pro nastavení")
                )));
            }

            // Dye Row
            if (home != null) {
                inv.setItem(dyeSlot, createItem(Material.LIME_DYE, "&a&lPŘENASTAVIT DOMOV #" + i, List.of(
                        FontUtils.parse("&8ᴘᴏᴘɪѕ"),
                        FontUtils.parse("&7Klikni pro uložení tvé"),
                        FontUtils.parse("&7nové pozice domova."),
                        Component.empty(),
                        FontUtils.parse("&#ff6969Informace:"),
                        FontUtils.parse(" &fKlikni pro změnu"),
                        FontUtils.parse(" &fpozice domova."),
                        Component.empty(),
                        FontUtils.parse("&e▶ Klikni&f pro přenastavení")
                )));
            } else {
                inv.setItem(dyeSlot, createItem(Material.LIGHT_BLUE_DYE, "&b&lNASTAVIT DOMOV #" + i, List.of(
                        FontUtils.parse("&8ᴘᴏᴘɪѕ"),
                        FontUtils.parse("&7Klikni pro nastavení"),
                        FontUtils.parse("&7domova na tvoji pozici."),
                        Component.empty(),
                        FontUtils.parse("&#ff6969Informace:"),
                        FontUtils.parse(" &fKlikni pro uložení"),
                        FontUtils.parse(" &faktuální pozice."),
                        Component.empty(),
                        FontUtils.parse("&e▶ Klikni&f pro nastavení")
                )));
            }
        }

        player.openInventory(inv);
    }

    private ItemStack createItem(Material mat, String name, List<Component> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(FontUtils.parse(name));
            if (lore != null) meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
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
                        String teleMsg = config.getString("messages.teleport", "&8「&bDomov&8」 &7Teleportuji na domov &b#%number%&7...").replace("%number%", String.valueOf(homeNum));
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
                String setMsg = config.getString("messages.set", "&8「&aDomov&8」 &7Domov &a#%number% &7byl úspěšně nastaven.").replace("%number%", String.valueOf(homeNum));
                player.sendMessage(FontUtils.parse(setMsg));
                player.closeInventory();
                open(player);
            }
        } else if (holder instanceof HomeConfirmHolder confirmHolder) {
            int homeNum = confirmHolder.homeNum;
            if (slot == 11) { // Confirm
                homeManager.deleteHome(player.getUniqueId(), homeNum);
                String delMsg = config.getString("messages.delete", "&8「&cDomov&8」 &7Domov &c#%number% &7byl smazán.").replace("%number%", String.valueOf(homeNum));
                player.sendMessage(FontUtils.parse(delMsg));
                player.closeInventory();
                open(player);
            } else if (slot == 15) { // Cancel
                open(player);
            }
        }
    }

    public void openConfirm(Player player, int homeNum) {
        FileConfiguration config = plugin.getModuleManager().getModuleConfig("home");
        HomeConfirmHolder holder = new HomeConfirmHolder(homeNum);
        Inventory inv = Bukkit.createInventory(holder, 27, FontUtils.parse(config.getString("gui.confirm-title", "&8Opravdu smazat?")));
        holder.setInventory(inv);

        ItemStack blackGlass = createItem(Material.BLACK_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < 27; i++) inv.setItem(i, blackGlass);

        inv.setItem(11, createItem(Material.LIME_STAINED_GLASS_PANE, "&a&lPOTVRDIT SMAZÁNÍ", List.of(
                FontUtils.parse("&8ᴘᴏᴘɪѕ"),
                FontUtils.parse("&7Kliknutím trvale smažeš"),
                FontUtils.parse("&7vybraný domovský bod."),
                Component.empty(),
                FontUtils.parse("&#ff6969Informace:"),
                FontUtils.parse(" &fTato akce je"),
                FontUtils.parse(" &fnevratná!"),
                Component.empty(),
                FontUtils.parse("&e▶ Klikni&f pro potvrzení")
        )));

        inv.setItem(15, createItem(Material.RED_STAINED_GLASS_PANE, "&c&lZRUŠIT", List.of(
                FontUtils.parse("&8ᴘᴏᴘɪѕ"),
                FontUtils.parse("&7Kliknutím se vrátíš"),
                FontUtils.parse("&7zpět do seznamu."),
                Component.empty(),
                FontUtils.parse("&#ff6969Informace:"),
                FontUtils.parse(" &fKlikni pro návrat"),
                FontUtils.parse(" &fbez smazání."),
                Component.empty(),
                FontUtils.parse("&e▶ Klikni&f pro návrat")
        )));

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
