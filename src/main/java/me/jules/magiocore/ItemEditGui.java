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
        this.inventory = Bukkit.createInventory(this, 54, FontUtils.parse("&#00fbffɪᴛᴇᴍ ᴇᴅɪᴛ"));
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

        inventory.setItem(10, createGuiItem(Material.NAME_TAG, "&#00fbffᴢᴍěɴɪᴛ ɴáᴢᴇᴠ", "§7ᴋʟɪᴋɴɪ ᴘʀᴏ ᴢᴍěɴᴜ ɴáᴢᴠᴜ ᴘřᴇᴅᴍěᴛᴜ"));
        inventory.setItem(11, createGuiItem(Material.BOOK, "&#00fbffᴜᴘʀᴀᴠɪᴛ ʟᴏʀᴇ", "§7ʟᴇᴠý ᴋʟɪᴋ: ᴘřɪᴅᴀᴛ řáᴅᴇᴋ", "§7ᴘʀᴀᴠý ᴋʟɪᴋ: ᴏᴅsᴛʀᴀɴɪᴛ ᴘᴏsʟᴇᴅɴí řáᴅᴇᴋ", "§7sʜɪғᴛ + ʟᴇᴠý: ʀᴇsᴇᴛᴏᴠᴀᴛ ʟᴏʀᴇ"));
        inventory.setItem(12, createGuiItem(Material.ENCHANTED_BOOK, "&#00fbffᴇɴᴄʜᴀɴᴛʏ", "§7ᴏᴛᴠᴇřᴇ ᴍᴇɴᴜ ᴇɴᴄʜᴀɴᴛů"));
        inventory.setItem(13, createGuiItem(Material.BARRIER, "&#00fbffsᴋʀýᴛ ᴘříᴢɴᴀᴋʏ", "§7sᴋʀʏᴊᴇ ᴇɴᴄʜᴀɴᴛʏ, ᴀᴛʀɪʙᴜᴛʏ ᴀᴛᴅ."));
        inventory.setItem(14, createGuiItem(Material.BEDROCK, "&#00fbffɴᴇᴢɴɪčɪᴛᴇʟɴᴏsᴛ", "§7ᴘřᴇᴘɴᴇ ɴᴇᴢɴɪčɪᴛᴇʟɴᴏsᴛ ᴘřᴇᴅᴍěᴛᴜ"));
        inventory.setItem(15, createGuiItem(Material.ANVIL, "&#00fbffᴄᴇɴᴀ ᴏᴘʀᴀᴠʏ", "§7ɴᴀsᴛᴀᴠí ᴄᴇɴᴜ ᴏᴘʀᴀᴠʏ ᴠ ᴀɴᴠɪʟᴜ"));
        inventory.setItem(16, createGuiItem(Material.CHEST, "&#00fbffᴍɴᴏžsᴛᴠí", "§7ɴᴀsᴛᴀᴠí ᴍɴᴏžsᴛᴠí ᴘřᴇᴅᴍěᴛů ᴠ sᴛᴀᴄᴋᴜ"));

        inventory.setItem(19, createGuiItem(Material.IRON_INGOT, "&#00fbffᴏᴅᴏʟɴᴏsᴛ", "§7ɴᴀsᴛᴀᴠí ᴀᴋᴛᴜáʟɴí ᴏᴅᴏʟɴᴏsᴛ (ᴅᴜʀᴀʙɪʟɪᴛʏ)"));
        inventory.setItem(20, createGuiItem(Material.PLAYER_HEAD, "&#00fbffᴠʟᴀsᴛɴíᴋ ʜʟᴀᴠʏ", "§7ɴᴀsᴛᴀᴠí ᴠʟᴀsᴛɴíᴋᴀ ʜʟᴀᴠʏ (ᴘᴏᴜᴢᴇ ᴘʀᴏ ʜʟᴀᴠʏ)"));
        inventory.setItem(21, createGuiItem(Material.NETHERITE_SWORD, "&#00fbffᴀᴛʀɪʙᴜᴛʏ", "§7ᴏᴛᴠᴇřᴇ ᴍᴇɴᴜ ᴀᴛʀɪʙᴜᴛů"));
        inventory.setItem(22, createGuiItem(Material.COMMAND_BLOCK, "&#00fbffᴄᴜsᴛᴏᴍ ᴍᴏᴅᴇʟ ᴅᴀᴛᴀ", "§7ɴᴀsᴛᴀᴠí ᴄᴜsᴛᴏᴍ ᴍᴏᴅᴇʟ ᴅᴀᴛᴀ"));
        inventory.setItem(23, createGuiItem(Material.POTION, "&#00fbffʙᴀʀᴠᴀ ᴘᴏᴛɪᴏɴᴜ", "§7ɴᴀsᴛᴀᴠí ʙᴀʀᴠᴜ ᴘᴏᴛɪᴏɴᴜ/ᴋůžᴇ"));
        inventory.setItem(24, createGuiItem(Material.FIREWORK_ROCKET, "&#00fbffᴏʜɴᴏsᴛʀᴏᴊ", "§7ɴᴀsᴛᴀᴠí síʟᴜ ᴏʜɴᴏsᴛʀᴏᴊᴇ"));
        inventory.setItem(25, createGuiItem(Material.COMPASS, "&#00fbffᴋᴏᴍᴘᴀs", "§7ɴᴀsᴛᴀᴠí ᴄíʟ ᴋᴏᴍᴘᴀsᴜ"));
        inventory.setItem(28, createGuiItem(Material.GRASS_BLOCK, "&#00fbffᴢᴍěɴɪᴛ ᴍᴀᴛᴇʀɪáʟ", "§7ᴢᴍěɴí ᴛʏᴘ ᴘřᴇᴅᴍěᴛᴜ s ᴢᴀᴄʜᴏᴠáɴíᴍ ᴇɴᴄʜᴀɴᴛů"));
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
