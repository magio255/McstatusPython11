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

import java.util.ArrayList;
import java.util.List;

public class SettingsGui implements Listener {
    private final MagioCore plugin;
    private final SettingsManager manager;
    private final String title = "&#EA427F» " + "NAsTAVENí";

    public SettingsGui(MagioCore plugin, SettingsManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(new SettingsGuiHolder(), 27, FontUtils.parse(title));
        SettingsManager.PlayerSettings settings = manager.getSettings(player.getUniqueId());

        inv.setItem(10, createItem(Material.PAPER, "CHAT", settings.chat));
        inv.setItem(12, createItem(Material.WRITABLE_BOOK, "sOUKROMé ZPRáVY", settings.dms));
        inv.setItem(14, createItem(Material.BELL, "ACTION BAR", settings.actionbar));
        inv.setItem(16, createItem(Material.PAINTING, "sCOREBOARD", settings.scoreboard));

        player.openInventory(inv);
    }

    private ItemStack createItem(Material material, String name, boolean enabled) {

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(FontUtils.parse("&#00fbff" + name));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(FontUtils.parse("§fStav: " + (enabled ? "&#00ff44Zapnuto" : "§cVypnuto")));
        lore.add(Component.empty());
        lore.add(FontUtils.parse("&#EA427FKlikni pro změnu!"));
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!(event.getInventory().getHolder() instanceof SettingsGuiHolder)) return;

        event.setCancelled(true);
        int slot = event.getRawSlot();
        SettingsManager.PlayerSettings settings = manager.getSettings(player.getUniqueId());

        boolean changed = false;
        if (slot == 10) { settings.chat = !settings.chat; changed = true; }
        else if (slot == 12) { settings.dms = !settings.dms; changed = true; }
        else if (slot == 14) { settings.actionbar = !settings.actionbar; changed = true; }
        else if (slot == 16) { settings.scoreboard = !settings.scoreboard; changed = true; }

        if (changed) {
            manager.saveSettings(player.getUniqueId());
            open(player);
        }
    }

    private static class SettingsGuiHolder implements InventoryHolder {
        @Override
        public @NotNull Inventory getInventory() {
            return null;
        }
    }
}
