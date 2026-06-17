package me.jules.magiocore;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ItemEditGui implements InventoryHolder {
    private final Inventory inventory;

    public ItemEditGui(MagioCore plugin) {
        FileConfiguration config = plugin.getModuleManager().getModuleConfig("itemedit");
        int rows = config.getInt("gui.rows", 6);
        String title = config.getString("gui.title", "&8Úprava předmětu");
        this.inventory = Bukkit.createInventory(this, rows * 9, FontUtils.parse(title, false));
        fillGui(config);
    }

    private void fillGui(FileConfiguration config) {
        ConfigurationSection items = config.getConfigurationSection("gui.items");
        if (items != null) {
            for (String key : items.getKeys(false)) {
                ConfigurationSection sec = items.getConfigurationSection(key);
                if (sec == null) continue;

                Material mat = Material.valueOf(sec.getString("material", "AIR").toUpperCase());
                ItemStack is = new ItemStack(mat);
                ItemMeta meta = is.getItemMeta();
                if (meta != null) {
                    meta.displayName(FontUtils.parse(sec.getString("name", " ")));
                    meta.lore(sec.getStringList("lore").stream().map(FontUtils::parse).toList());
                    is.setItemMeta(meta);
                }
                if (sec.contains("slot")) inventory.setItem(sec.getInt("slot"), is);
                else if (sec.contains("slots")) {
                    for (String p : sec.getString("slots").split(",")) {
                        if (p.contains("-")) {
                            String[] range = p.split("-");
                            for (int i = Integer.parseInt(range[0]); i <= Integer.parseInt(range[1]); i++) inventory.setItem(i, is.clone());
                        } else inventory.setItem(Integer.parseInt(p.trim()), is.clone());
                    }
                }
            }
        }
    }

    private ItemStack createGuiItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(FontUtils.parse(name));
        List<Component> l = new ArrayList<>();
        for (String s : lore) {
            l.add(FontUtils.parse(s));
        }
        meta.lore(l);
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
