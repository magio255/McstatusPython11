package me.jules.magiocore;

import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ItemEditListener implements Listener {
    private final MagioCore plugin;
    public static final Map<UUID, String> pendingInput = new ConcurrentHashMap<>();

    public ItemEditListener(MagioCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof ItemEditGui)) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            player.closeInventory();
            player.sendMessage(FontUtils.parse("§c" + "ᴍᴜsíš ᴅʀžᴇᴛ ᴘřᴇᴅᴍěᴛ ᴠ ʀᴜᴄᴇ."));
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        int slot = event.getRawSlot();
        switch (slot) {
            case 10 -> { // Rename
                player.closeInventory();
                player.sendMessage(FontUtils.parse("&#00fbff" + "ɴᴀᴘɪš ɴᴏᴠý ɴáᴢᴇᴠ ᴘřᴇᴅᴍěᴛᴜ ᴅᴏ ᴄʜᴀᴛᴜ:"));
                pendingInput.put(player.getUniqueId(), "rename");
            }
            case 11 -> { // Lore
                List<Component> lore = meta.lore();
                if (lore == null) lore = new ArrayList<>();

                if (event.getClick() == ClickType.LEFT) {
                    player.closeInventory();
                    player.sendMessage(FontUtils.parse("&#00fbff" + "ɴᴀᴘɪš ɴᴏᴠý řáᴅᴇᴋ ʟᴏʀᴇ ᴅᴏ ᴄʜᴀᴛᴜ:"));
                    pendingInput.put(player.getUniqueId(), "lore_add");
                } else if (event.getClick() == ClickType.RIGHT) {
                    if (!lore.isEmpty()) {
                        lore.remove(lore.size() - 1);
                        meta.lore(lore);
                        item.setItemMeta(meta);
                        player.sendMessage(FontUtils.parse("&#00fbff" + "ᴘᴏsʟᴇᴅɴí řáᴅᴇᴋ ʙʏʟ ᴏᴅsᴛʀᴀɴěɴ."));
                    }
                } else if (event.getClick().isShiftClick() && event.getClick().isLeftClick()) {
                    lore.clear();
                    meta.lore(lore);
                    item.setItemMeta(meta);
                    player.sendMessage(FontUtils.parse("&#00fbff" + "ʟᴏʀᴇ ʙʏʟᴏ ʀᴇsᴇᴛᴏᴠáɴᴏ."));
                }
            }
            case 13 -> { // Hide flags
                if (meta.getItemFlags().isEmpty()) {
                    meta.addItemFlags(ItemFlag.values());
                    player.sendMessage(FontUtils.parse("&#00fbff" + "ᴘříᴢɴᴀᴋʏ ʙʏʟʏ sᴋʀʏᴛʏ."));
                } else {
                    meta.removeItemFlags(ItemFlag.values());
                    player.sendMessage(FontUtils.parse("&#00fbff" + "ᴘříᴢɴᴀᴋʏ ʙʏʟʏ ᴢᴏʙʀᴀᴢᴇɴʏ."));
                }
                item.setItemMeta(meta);
            }
            case 14 -> { // Unbreakable
                meta.setUnbreakable(!meta.isUnbreakable());
                item.setItemMeta(meta);
                player.sendMessage(FontUtils.parse("&#00fbff" + "ɴᴇᴢɴɪčɪᴛᴇʟɴᴏsᴛ ʙʏʟᴀ " + (meta.isUnbreakable() ? "ᴢᴀᴘɴᴜᴛᴀ" : "ᴠʏᴘɴᴜᴛᴀ") + "."));
            }
            case 15 -> { // Repair cost
                player.closeInventory();
                player.sendMessage(FontUtils.parse("&#00fbff" + "ɴᴀᴘɪš ɴᴏᴠᴏᴜ ᴄᴇɴᴜ ᴏᴘʀᴀᴠʏ (čísʟᴏ) ᴅᴏ ᴄʜᴀᴛᴜ:"));
                pendingInput.put(player.getUniqueId(), "repaircost");
            }
            case 16 -> { // Amount
                player.closeInventory();
                player.sendMessage(FontUtils.parse("&#00fbff" + "ɴᴀᴘɪš ɴᴏᴠé ᴍɴᴏžsᴛᴠí (1-64) ᴅᴏ ᴄʜᴀᴛᴜ:"));
                pendingInput.put(player.getUniqueId(), "amount");
            }
            case 19 -> { // Durability
                player.closeInventory();
                player.sendMessage(FontUtils.parse("&#00fbff" + "ɴᴀᴘɪš ɴᴏᴠᴏᴜ ᴏᴅᴏʟɴᴏsᴛ (čísʟᴏ) ᴅᴏ ᴄʜᴀᴛᴜ:"));
                pendingInput.put(player.getUniqueId(), "durability");
            }
            case 20 -> { // Skull owner
                player.closeInventory();
                player.sendMessage(FontUtils.parse("&#00fbff" + "ɴᴀᴘɪš ᴊᴍéɴᴏ ᴠʟᴀsᴛɴíᴋᴀ ʜʟᴀᴠʏ ᴅᴏ ᴄʜᴀᴛᴜ:"));
                pendingInput.put(player.getUniqueId(), "skullowner");
            }
            case 22 -> { // Custom model data
                player.closeInventory();
                player.sendMessage(FontUtils.parse("&#00fbff" + "ɴᴀᴘɪš ᴄᴜsᴛᴏᴍ ᴍᴏᴅᴇʟ ᴅᴀᴛᴀ (čísʟᴏ) ᴅᴏ ᴄʜᴀᴛᴜ:"));
                pendingInput.put(player.getUniqueId(), "custommodeldata");
            }
            case 28 -> { // Change material
                player.closeInventory();
                player.sendMessage(FontUtils.parse("&#00fbff" + "ɴᴀᴘɪš ɴᴏᴠý ᴍᴀᴛᴇʀɪáʟ (ɴᴀᴘř. DIAMOND_SWORD) ᴅᴏ ᴄʜᴀᴛᴜ:"));
                pendingInput.put(player.getUniqueId(), "material");
            }
            case 12, 21, 23, 24, 25 -> {
                player.sendMessage(FontUtils.parse("§c" + "ᴛᴀᴛᴏ ғᴜɴᴋᴄᴇ ᴊᴇ ᴅᴏsᴛᴜᴘɴá ᴘᴏᴜᴢᴇ ᴘřᴇs ᴘříᴋᴀᴢ /ɪᴇ <sᴜʙᴘříᴋᴀᴢ>."));
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(org.bukkit.event.inventory.InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof ItemEditGui) {
            event.setCancelled(true);
        }
    }
}
