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
        this.inventory = Bukkit.createInventory(this, 54, FontUtils.parse("&#00fbffITEM EDIT"));
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

        inventory.setItem(10, createGuiItem(Material.NAME_TAG, "&#00fbffZMěNIT NáZEV", "§7KLIKNI PRO ZMěNU NáZVU PřEDMěTU"));
        inventory.setItem(11, createGuiItem(Material.BOOK, "&#00fbffUPRAVIT LORE", "§7LEVý KLIK: PřIDAT řáDEK", "§7PRAVý KLIK: ODsTRANIT POsLEDNí řáDEK", "§7sHIғT + LEVý: REsETOVAT LORE"));
        inventory.setItem(12, createGuiItem(Material.ENCHANTED_BOOK, "&#00fbffENCHANTY", "§7OTVEřE MENU ENCHANTů"));
        inventory.setItem(13, createGuiItem(Material.BARRIER, "&#00fbffsKRýT PříZNAKY", "§7sKRYJE ENCHANTY, ATRIBUTY ATD."));
        inventory.setItem(14, createGuiItem(Material.BEDROCK, "&#00fbffNEZNIčITELNOsT", "§7PřEPNE NEZNIčITELNOsT PřEDMěTU"));
        inventory.setItem(15, createGuiItem(Material.ANVIL, "&#00fbffCENA OPRAVY", "§7NAsTAVí CENU OPRAVY V ANVILU"));
        inventory.setItem(16, createGuiItem(Material.CHEST, "&#00fbffMNOžsTVí", "§7NAsTAVí MNOžsTVí PřEDMěTů V sTACKU"));

        inventory.setItem(19, createGuiItem(Material.IRON_INGOT, "&#00fbffODOLNOsT", "§7NAsTAVí AKTUáLNí ODOLNOsT (DURABILITY)"));
        inventory.setItem(20, createGuiItem(Material.PLAYER_HEAD, "&#00fbffVLAsTNíK HLAVY", "§7NAsTAVí VLAsTNíKA HLAVY (POUZE PRO HLAVY)"));
        inventory.setItem(21, createGuiItem(Material.NETHERITE_SWORD, "&#00fbffATRIBUTY", "§7OTVEřE MENU ATRIBUTů"));
        inventory.setItem(22, createGuiItem(Material.COMMAND_BLOCK, "&#00fbffCUsTOM MODEL DATA", "§7NAsTAVí CUsTOM MODEL DATA"));
        inventory.setItem(23, createGuiItem(Material.POTION, "&#00fbffBARVA POTIONU", "§7NAsTAVí BARVU POTIONU/KůžE"));
        inventory.setItem(24, createGuiItem(Material.FIREWORK_ROCKET, "&#00fbffOHNOsTROJ", "§7NAsTAVí síLU OHNOsTROJE"));
        inventory.setItem(25, createGuiItem(Material.COMPASS, "&#00fbffKOMPAs", "§7NAsTAVí CíL KOMPAsU"));
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
