package me.jules.magiocore;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
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

    public ItemEditGui() {
        this.inventory = Bukkit.createInventory(this, 54, FontUtils.parse("&#00fbffÚprava předmětu", true));
        fillGui();
    }

    private void fillGui() {
        // Decorative glass
        ItemStack glass = new ItemStack(Material.CYAN_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.displayName(Component.empty());
        glass.setItemMeta(glassMeta);

        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, glass);
            }
        }

        inventory.setItem(10, createGuiItem(Material.NAME_TAG, "&#00fbffZměnit název", "§7Klikni pro změnu názvu předmětu"));
        inventory.setItem(11, createGuiItem(Material.BOOK, "&#00fbffUpravit lore", "§7Levý klik: Přidat řádek", "§7Pravý klik: Odstranit poslední řádek", "§7Shift + levý: Resetovat lore"));
        inventory.setItem(12, createGuiItem(Material.ENCHANTED_BOOK, "&#00fbffEnchanty", "§7Otevře menu enchantů"));
        inventory.setItem(13, createGuiItem(Material.BARRIER, "&#00fbffSkrýt příznaky", "§7Skryje enchanty, atributy atd."));
        inventory.setItem(14, createGuiItem(Material.BEDROCK, "&#00fbffNezničitelnost", "§7Přepne nezničitelnost předmětu"));
        inventory.setItem(15, createGuiItem(Material.ANVIL, "&#00fbffCena opravy", "§7Nastaví cenu opravy v anvilu"));
        inventory.setItem(16, createGuiItem(Material.CHEST, "&#00fbffMnožství", "§7Nastaví množství předmětů v stacku"));

        inventory.setItem(19, createGuiItem(Material.IRON_INGOT, "&#00fbffOdolnost", "§7Nastaví aktuální odolnost (durability)"));
        inventory.setItem(20, createGuiItem(Material.PLAYER_HEAD, "&#00fbffVlastník hlavy", "§7Nastaví vlastníka hlavy (pouze pro hlavy)"));
        inventory.setItem(21, createGuiItem(Material.NETHERITE_SWORD, "&#00fbffAtributy", "§7Otevře menu atributů"));
        inventory.setItem(22, createGuiItem(Material.COMMAND_BLOCK, "&#00fbffCustom model data", "§7Nastaví custom model data"));
        inventory.setItem(23, createGuiItem(Material.POTION, "&#00fbffBarva potionu", "§7Nastaví barvu potionu/kůže"));
        inventory.setItem(24, createGuiItem(Material.FIREWORK_ROCKET, "&#00fbffOhňostroj", "§7Nastaví sílu ohňostroje"));
        inventory.setItem(25, createGuiItem(Material.COMPASS, "&#00fbffKompas", "§7Nastaví cíl kompasu"));
        inventory.setItem(28, createGuiItem(Material.GRASS_BLOCK, "&#00fbffZměnit materiál", "§7Změní typ předmětu s zachováním enchantů"));
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
