package me.jules.magiocore;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
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
        int rows = config.getInt("gui.main.rows", 4);
        String title = config.getString("gui.title", "&8Domovy");

        HomeGuiHolder holder = new HomeGuiHolder();
        Inventory inv = Bukkit.createInventory(holder, rows * 9, FontUtils.parse(title));
        holder.setInventory(inv);

        Map<Integer, Home> homes = homeManager.getHomes(player.getUniqueId());
        int maxHomes = PlaytimeUtils.getMaxHomes(player);

        ConfigurationSection items = config.getConfigurationSection("gui.main.items");
        if (items != null) {
            for (String key : items.getKeys(false)) {
                ConfigurationSection sec = items.getConfigurationSection(key);
                if (sec == null) continue;

                if (key.startsWith("home_") || key.startsWith("set_")) {
                    boolean isSetMode = key.startsWith("set_");
                    int homeNum = Integer.parseInt(key.split("_")[1]);
                    Home home = homes.get(homeNum);
                    boolean isLocked = homeNum > maxHomes;

                    Material mat;
                    String name;
                    List<String> lore;

                    if (isLocked) {
                        mat = Material.valueOf(config.getString("gui.main.templates.locked.material", "BARRIER").toUpperCase());
                        name = config.getString("gui.main.templates.locked.name", "&c&lZAMČENO");
                        lore = config.getStringList("gui.main.templates.locked.lore");
                    } else if (isSetMode) {
                        String type = home != null ? "reset" : "unset";
                        mat = Material.valueOf(config.getString("gui.main.templates.set." + type + ".material", "LIME_DYE").toUpperCase());
                        name = config.getString("gui.main.templates.set." + type + ".name", "&a&lPřenastavit").replace("%id%", String.valueOf(homeNum));
                        lore = config.getStringList("gui.main.templates.set." + type + ".lore");
                    } else {
                        String type = home != null ? "active" : "empty";
                        mat = Material.valueOf(config.getString("gui.main.templates.home." + type + ".material", "GREEN_BED").toUpperCase());
                        name = config.getString("gui.main.templates.home." + type + ".name", "&a&lDomov").replace("%id%", String.valueOf(homeNum));
                        lore = config.getStringList("gui.main.templates.home." + type + ".lore");
                    }

                    inv.setItem(sec.getInt("slot"), createItem(mat, name, lore.stream().map(FontUtils::parse).toList()));
                } else {
                    Material mat = Material.valueOf(sec.getString("material", "AIR").toUpperCase());
                    ItemStack is = createItem(mat, sec.getString("name", " "), sec.getStringList("lore").stream().map(FontUtils::parse).toList());
                    if (sec.contains("slot")) inv.setItem(sec.getInt("slot"), is);
                    else if (sec.contains("slots")) {
                        for (int sIdx : FontUtils.parseSlots(sec.getString("slots"), inv.getSize())) {
                            inv.setItem(sIdx, is.clone());
                        }
                    }
                }
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
        int rows = config.getInt("gui.confirm.rows", 3);
        HomeConfirmHolder holder = new HomeConfirmHolder(homeNum);
        Inventory inv = Bukkit.createInventory(holder, rows * 9, FontUtils.parse(config.getString("gui.confirm-title", "&8Opravdu smazat?")));
        holder.setInventory(inv);

        ConfigurationSection items = config.getConfigurationSection("gui.confirm.items");
        if (items != null) {
            for (String key : items.getKeys(false)) {
                ConfigurationSection sec = items.getConfigurationSection(key);
                if (sec == null) continue;

                Material mat = Material.valueOf(sec.getString("material", "AIR").toUpperCase());
                ItemStack is = createItem(mat, sec.getString("name", " "), sec.getStringList("lore").stream().map(FontUtils::parse).toList());

                if (sec.contains("slot")) inv.setItem(sec.getInt("slot"), is);
                else if (sec.contains("slots")) {
                    for (int sIdx : FontUtils.parseSlots(sec.getString("slots"), inv.getSize())) {
                        inv.setItem(sIdx, is.clone());
                    }
                }
            }
        }

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
