package me.jules.magiocore;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import net.kyori.adventure.text.Component;
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
        int rows = config.getInt("gui.rows", 2);
        String title = config.getString("gui.title", "&8Rychlost létání");

        FlySpeedGuiHolder holder = new FlySpeedGuiHolder();
        Inventory inv = Bukkit.createInventory(holder, rows * 9, FontUtils.parse(title));
        holder.setInventory(inv);

        ConfigurationSection items = config.getConfigurationSection("gui.items");
        if (items != null) {
            for (String key : items.getKeys(false)) {
                ConfigurationSection sec = items.getConfigurationSection(key);
                if (sec == null) continue;

                if (key.startsWith("speed_")) {
                    int speed;
                    try {
                        speed = Integer.parseInt(key.split("_")[1]);
                    } catch (NumberFormatException e) {
                        continue;
                    }
                    ConfigurationSection template = config.getConfigurationSection("gui.items.speed_templates");

                    Material mat = Material.valueOf(sec.getString("material", template != null ? template.getString("material", "FEATHER") : "FEATHER").toUpperCase());
                    String name = sec.getString("name", template != null ? template.getString("name", "&b&lRychlost %speed%") : "&b&lRychlost %speed%").replace("%speed%", String.valueOf(speed));
                    List<String> loreList = sec.contains("lore") ? sec.getStringList("lore") : (template != null ? template.getStringList("lore") : new ArrayList<>());

                    List<Component> lore = loreList.stream()
                            .map(s -> s.replace("%speed%", String.valueOf(speed)))
                            .map(FontUtils::parse).toList();
                    inv.setItem(sec.getInt("slot"), createItem(mat, name, lore));
                } else {
                    Material mat = Material.valueOf(sec.getString("material", "AIR").toUpperCase());
                    ItemStack is = createItem(mat, sec.getString("name", " "), sec.getStringList("lore").stream().map(FontUtils::parse).toList());
                    if (sec.contains("slot")) inv.setItem(sec.getInt("slot"), is);
                    else if (sec.contains("slots")) {
                        for (String p : sec.getString("slots").split(",")) {
                            if (p.contains("-")) {
                                String[] range = p.split("-");
                                for (int i = Integer.parseInt(range[0]); i <= Integer.parseInt(range[1]); i++) inv.setItem(i, is.clone());
                            } else inv.setItem(Integer.parseInt(p.trim()), is.clone());
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
            meta.lore(lore);
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
        int slot = event.getRawSlot();
        FileConfiguration config = MagioCore.getPlugin(MagioCore.class).getModuleManager().getModuleConfig("flyspeed");
        ConfigurationSection items = config.getConfigurationSection("gui.items");
        if (items == null) return;

        for (String key : items.getKeys(false)) {
            if (items.getInt(key + ".slot", -1) == slot) {
                if (key.startsWith("speed_")) {
                    try {
                        int speed = Integer.parseInt(key.split("_")[1]);
                        setFlySpeed(player, speed);
                        player.closeInventory();
                    } catch (NumberFormatException ignored) {}
                }
                break;
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(org.bukkit.event.inventory.InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof FlySpeedGuiHolder) {
            event.setCancelled(true);
        }
    }

    private static class FlySpeedGuiHolder implements InventoryHolder {
        private Inventory inventory;
        public void setInventory(Inventory inventory) { this.inventory = inventory; }
        @Override public @NotNull Inventory getInventory() { return inventory; }
    }
}
