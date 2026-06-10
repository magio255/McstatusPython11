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
        this.inventory = Bukkit.createInventory(this, 54, FontUtils.parse("&8Úprava předmětu", true));
        fillGui();
    }

    private void fillGui() {
        // Decorative glass
        ItemStack blackGlass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta blackMeta = blackGlass.getItemMeta();
        blackMeta.displayName(Component.empty());
        blackGlass.setItemMeta(blackMeta);

        ItemStack grayGlass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta grayMeta = grayGlass.getItemMeta();
        grayMeta.displayName(Component.empty());
        grayGlass.setItemMeta(grayMeta);

        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, (i % 2 == 0) ? blackGlass : grayGlass);
            }
        }

        inventory.setItem(10, createGuiItem(Material.NAME_TAG, "&b&lZMĚNIT NÁZEV", "&7Změní zobrazované jméno", "&7tohoto předmětu.", "", "&bInformace:", " &fKlikni pro zadání", " &fnového názvu.", "", "&b▶ &lKLIKNI &bPro změnu!"));
        inventory.setItem(11, createGuiItem(Material.BOOK, "&e&lUPRAVIT LORE", "&7Umožňuje přidávat nebo", "&7mazat řádky popisu.", "", "&eInformace:", " &fLevý: &aPřidat řádek", " &fPravý: &cOdebrat řádek", " &fShift: &4Resetovat", "", "&e▶ &lKLIKNI &ePro úpravu!"));
        inventory.setItem(12, createGuiItem(Material.ENCHANTED_BOOK, "&d&lENCHANTY", "&7Otevře rozšířené menu", "&7všech dostupných enchantů.", "", "&dInformace:", " &fKlikni pro správu", " &fenchantů předmětu.", "", "&d▶ &lKLIKNI &dPro otevření!"));
        inventory.setItem(13, createGuiItem(Material.BARRIER, "&c&lSKRÝT PŘÍZNAKY", "&7Skryje veškeré příznaky", "&7jako enchanty a atributy.", "", "&cInformace:", " &fKlikni pro přepnutí", " &fviditelnosti tagů.", "", "&c▶ &lKLIKNI &cPro přepnutí!"));
        inventory.setItem(14, createGuiItem(Material.BEDROCK, "&8&lNEZNIČITELNOST", "&7Nastaví předmět tak,", "&7aby se nikdy nerozbil.", "", "&8Informace:", " &fKlikni pro přepnutí", " &fnezničitelnosti.", "", "&8▶ &lKLIKNI &8Pro přepnutí!"));
        inventory.setItem(15, createGuiItem(Material.ANVIL, "&7&lCENA OPRAVY", "&7Nastaví cenu v levelech", "&7pro opravu v anvilu.", "", "&7Informace:", " &fKlikni pro nastavení", " &fceny opravy.", "", "&7▶ &lKLIKNI &7Pro změnu!"));
        inventory.setItem(16, createGuiItem(Material.CHEST, "&6&lMNOŽSTVÍ", "&7Změní počet předmětů", "&7v tomto stacku.", "", "&6Informace:", " &fKlikni pro zadání", " &fnového počtu.", "", "&6▶ &lKLIKNI &6Pro změnu!"));

        inventory.setItem(19, createGuiItem(Material.IRON_INGOT, "&f&lODOLNOST", "&7Nastaví aktuální zbývající", "&7odolnost předmětu.", "", "&fInformace:", " &fKlikni pro zadání", " &faktuální durability.", "", "&f▶ &lKLIKNI &fPro změnu!"));
        inventory.setItem(20, createGuiItem(Material.PLAYER_HEAD, "&3&lVLASTNÍK HLAVY", "&7Nastaví skin hlavy", "&7podle jména hráče.", "", "&3Informace:", " &fKlikni pro zadání", " &fjména vlastníka.", "", "&3▶ &lKLIKNI &3Pro změnu!"));
        inventory.setItem(21, createGuiItem(Material.NETHERITE_SWORD, "&c&lATRIBUTY", "&7Otevře menu pro úpravu", "&7základních atributů.", "", "&cInformace:", " &fKlikni pro správu", " &fatributů předmětu.", "", "&c▶ &lKLIKNI &cPro otevření!"));
        inventory.setItem(22, createGuiItem(Material.COMMAND_BLOCK, "&5&lCUSTOM MODEL DATA", "&7Nastaví ID vlastního", "&7modelu předmětu.", "", "&5Informace:", " &fKlikni pro zadání", " &fmodel data ID.", "", "&5▶ &lKLIKNI &5Pro změnu!"));
        inventory.setItem(23, createGuiItem(Material.POTION, "&d&lBARVA POTIONU", "&7Nastaví barvu obsahu", "&7potionu nebo kůže.", "", "&dInformace:", " &fKlikni pro zadání", " &fhex barvy.", "", "&d▶ &lKLIKNI &dPro změnu!"));
        inventory.setItem(24, createGuiItem(Material.FIREWORK_ROCKET, "&e&lOHŇOSTROJ", "&7Nastaví sílu a styl", "&7tohoto ohňostroje.", "", "&eInformace:", " &fKlikni pro správu", " &fohňostroje.", "", "&e▶ &lKLIKNI &ePro otevření!"));
        inventory.setItem(25, createGuiItem(Material.COMPASS, "&b&lKOMPAS", "&7Nastaví souřadnice,", "&7kam má kompas ukazovat.", "", "&bInformace:", " &fKlikni pro nastavení", " &fcíle kompasu.", "", "&b▶ &lKLIKNI &bPro změnu!"));
        inventory.setItem(28, createGuiItem(Material.GRASS_BLOCK, "&2&lZMĚNIT MATERIÁL", "&7Změní typ předmětu", "&7při zachování dat.", "", "&2Informace:", " &fKlikni pro výběr", " &fnového materiálu.", "", "&2▶ &lKLIKNI &2Pro změnu!"));
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
