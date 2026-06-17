package me.jules.magiocore;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FlySpeedCommand implements CommandExecutor, Listener {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;

        FileConfiguration config = MagioCore.getPlugin(MagioCore.class).getModuleManager().getModuleConfig("flyspeed");
        if (!player.hasPermission("magiocore.flyspeed") && !player.isOp()) {
            player.sendMessage(FontUtils.parse(config.getString("messages.no-permission", "§c" + "ɴᴇᴍáš ᴏᴘʀáᴠɴěɴí")));
            return true;
        }

        if (args.length > 0) {
            try {
                int speed = Integer.parseInt(args[0]);
                if (speed < 1 || speed > 10) {
                    player.sendMessage(FontUtils.parse(config.getString("messages.invalid-range", "§c" + "ʀʏᴄʜʟᴏsᴛ ᴍᴜsí ʙýᴛ 1-10")));
                    return true;
                }
                setFlySpeed(player, speed);
            } catch (NumberFormatException e) {
                player.sendMessage(FontUtils.parse(config.getString("messages.usage", "§c" + "ᴘᴏᴜžɪᴛí: //ꜰʟʏsᴘᴇᴇᴅ [1-10]")));
            }
        } else {
            openGui(player);
        }

        return true;
    }

    private void openGui(Player player) {
        FileConfiguration config = MagioCore.getPlugin(MagioCore.class).getModuleManager().getModuleConfig("flyspeed");
        String title = config.getString("gui.title", "&#EA427F» ʀʏᴄʜʟᴏsᴛ ʟéᴛáɴí");

        Inventory inv = Bukkit.createInventory(new FlySpeedGuiHolder(), 18, FontUtils.parse(title));

        // Background
        ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        if (glassMeta != null) {
            glassMeta.displayName(net.kyori.adventure.text.Component.empty());
            glass.setItemMeta(glassMeta);
        }
        for (int i = 0; i < 18; i++) {
            inv.setItem(i, glass);
        }

        for (int i = 1; i <= 9; i++) {
            inv.setItem(i - 1, createFeather(i));
        }
        inv.setItem(13, createFeather(10));

        player.openInventory(inv);
    }

    private ItemStack createFeather(int speed) {
        ItemStack item = new ItemStack(Material.FEATHER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(FontUtils.parse("&#00fbffʀʏᴄʜʟᴏsᴛ " + speed + ""));
            meta.lore(List.of(FontUtils.parse("§7" + "ᴋʟɪᴋɴɪ ᴘʀᴏ ɴᴀsᴛᴀᴠᴇɴí ʀʏᴄʜʟᴏsᴛɪ ɴᴀ " + speed)));
            item.setItemMeta(meta);
        }
        return item;
    }

    private void setFlySpeed(Player player, int speed) {
        float fSpeed = (float) speed / 10.0f;
        player.setFlySpeed(fSpeed);
        FileConfiguration config = MagioCore.getPlugin(MagioCore.class).getModuleManager().getModuleConfig("flyspeed");
        player.sendMessage(FontUtils.parse(config.getString("messages.changed", "&#00fbffᴛᴠᴏᴊᴇ ʀʏᴄʜʟᴏsᴛ ʟéᴛáɴí ʙʏʟᴀ ɴᴀsᴛᴀᴠᴇɴᴀ ɴᴀ %speed%").replace("%speed%", String.valueOf(speed))));
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!(event.getInventory().getHolder() instanceof FlySpeedGuiHolder)) return;

        event.setCancelled(true);
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() != Material.FEATHER) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String name = LegacyComponentSerializer.legacySection().serialize(meta.displayName());
        try {
            int speed = Integer.parseInt(name.replaceAll("[^0-9]", ""));
            setFlySpeed(player, speed);
            player.closeInventory();
        } catch (Exception e) {
            // ignore
        }
    }

    @EventHandler
    public void onInventoryDrag(org.bukkit.event.inventory.InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof FlySpeedGuiHolder) {
            event.setCancelled(true);
        }
    }

    private static class FlySpeedGuiHolder implements InventoryHolder {
        @Override
        public @NotNull Inventory getInventory() { return null; }
    }
}
