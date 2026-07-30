package me.jules.magiocore.modules;

import me.jules.magiocore.FontUtils;
import me.jules.magiocore.MagioCore;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
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
import org.jetbrains.annotations.NotNull;

import java.util.List;

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
        String title = config.getString("gui-title", "[#EA427F]ᴘʀᴀᴠɪᴅʟᴀ sᴇʀᴠᴇʀᴜ");

        RulesHolder holder = new RulesHolder();
        Inventory inv = Bukkit.createInventory(holder, 54, FontUtils.parse(title));
        holder.setInventory(inv);

        // Glassmorphism Border
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

        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                inv.setItem(i, (i % 2 == 0) ? blackGlass : grayGlass);
            }
        }

        List<String> rules = config.getStringList("rules");
        int[] ruleSlots = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
        };

        for (int i = 0; i < rules.size() && i < ruleSlots.length; i++) {
            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.displayName(FontUtils.parse("[#EA427F]ᴘʀᴀᴠɪᴅʟᴏ #" + (i + 1), true));
                meta.lore(List.of(
                    FontUtils.parse("§7"),
                    FontUtils.parse(rules.get(i)),
                    FontUtils.parse("§7")
                ));
                item.setItemMeta(meta);
            }
            inv.setItem(ruleSlots[i], item);
        }

        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof RulesHolder) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof RulesHolder) {
            event.setCancelled(true);
        }
    }

    private static class RulesHolder implements InventoryHolder {
        private Inventory inventory;
        public void setInventory(Inventory inventory) { this.inventory = inventory; }
        @Override public @NotNull Inventory getInventory() { return inventory; }
    }
}
