package me.jules.magiocore.modules;

import me.jules.magiocore.FontUtils;
import me.jules.magiocore.MagioCore;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
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
import java.util.stream.Collectors;

public class RulesModule implements CommandExecutor, Listener {
    private final MagioCore plugin;

    public RulesModule(MagioCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;
        openGui(player);
        return true;
    }

    public void openGui(Player player) {
        FileConfiguration config = plugin.getModuleManager().getModuleConfig("rules");
        int size = config.getInt("size", 27);
        String title = config.getString("menu_title", "&8▶ Pravidla serveru");

        RulesHolder holder = new RulesHolder();
        Inventory inv = Bukkit.createInventory(holder, size, FontUtils.parse(title));
        holder.setInventory(inv);

        ConfigurationSection items = config.getConfigurationSection("items");
        if (items != null) {
            for (String key : items.getKeys(false)) {
                ConfigurationSection itemSec = items.getConfigurationSection(key);
                if (itemSec == null) continue;

                Material mat = Material.valueOf(itemSec.getString("material", "BARRIER").toUpperCase());
                String name = itemSec.getString("display_name", "");
                List<String> lore = itemSec.getStringList("lore");

                ItemStack is = new ItemStack(mat);
                ItemMeta meta = is.getItemMeta();
                if (meta != null) {
                    meta.displayName(FontUtils.parse(name));
                    meta.lore(lore.stream().map(FontUtils::parse).collect(Collectors.toList()));
                    is.setItemMeta(meta);
                }

                if (itemSec.contains("slot")) {
                    inv.setItem(itemSec.getInt("slot"), is);
                } else if (itemSec.contains("slots")) {
                    String slotsStr = itemSec.getString("slots");
                    if (slotsStr.contains("-")) {
                        String[] parts = slotsStr.split("-");
                        int start = Integer.parseInt(parts[0]);
                        int end = Integer.parseInt(parts[1]);
                        for (int i = start; i <= end; i++) {
                            inv.setItem(i, is.clone());
                        }
                    }
                }
            }
        }

        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!(event.getInventory().getHolder() instanceof RulesHolder)) return;

        event.setCancelled(true);
        int slot = event.getRawSlot();

        FileConfiguration config = plugin.getModuleManager().getModuleConfig("rules");
        ConfigurationSection items = config.getConfigurationSection("items");
        if (items == null) return;

        for (String key : items.getKeys(false)) {
            ConfigurationSection itemSec = items.getConfigurationSection(key);
            if (itemSec == null) continue;

            boolean match = false;
            if (itemSec.contains("slot") && itemSec.getInt("slot") == slot) {
                match = true;
            } else if (itemSec.contains("slots")) {
                String slotsStr = itemSec.getString("slots");
                if (slotsStr.contains("-")) {
                    String[] parts = slotsStr.split("-");
                    int start = Integer.parseInt(parts[0]);
                    int end = Integer.parseInt(parts[1]);
                    if (slot >= start && slot <= end) match = true;
                }
            }

            if (match) {
                List<String> cmds = itemSec.getStringList("click_commands");
                for (String cmd : cmds) {
                    if (cmd.equalsIgnoreCase("[close]")) {
                        player.closeInventory();
                    } else if (cmd.startsWith("[player] ")) {
                        player.performCommand(cmd.substring(9));
                    } else if (cmd.startsWith("[console] ")) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.substring(10).replace("%player%", player.getName()));
                    }
                }
                break;
            }
        }
    }

    private static class RulesHolder implements InventoryHolder {
        private Inventory inventory;
        public void setInventory(Inventory inventory) { this.inventory = inventory; }
        @Override public @NotNull Inventory getInventory() { return inventory; }
    }
}
