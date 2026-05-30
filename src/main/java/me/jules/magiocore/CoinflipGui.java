package me.jules.magiocore;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CoinflipGui implements Listener {
    private final MagioCore plugin;
    private final CoinflipManager manager;
    private final String title = "&#EA427F» " + "ᴄᴏɪɴꜰʟɪᴘ ᴍᴇɴᴜ";

    public CoinflipGui(MagioCore plugin, CoinflipManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    public CoinflipManager getManager() {
        return manager;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(new CoinflipGuiHolder(), 36, FontUtils.parse(title));

        // Border
        ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.displayName(Component.empty());
        glass.setItemMeta(glassMeta);
        for (int i = 27; i < 36; i++) {
            inv.setItem(i, glass);
        }

        List<CoinflipManager.CoinflipBet> bets = manager.getActiveBets();
        for (int i = 0; i < bets.size() && i < 27; i++) {
            CoinflipManager.CoinflipBet bet = bets.get(i);
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setOwningPlayer(Bukkit.getOfflinePlayer(bet.creator));

            meta.displayName(FontUtils.parse("&#ff8000ᴄᴏɪɴꜰʟɪᴘ ʜʀáčᴇ " + bet.creatorName));

            List<Component> lore = new ArrayList<>();
            lore.add(FontUtils.parse("§8ᴄᴏɪɴꜰʟɪᴘ ᴍᴇɴᴜ"));
            lore.add(Component.empty());
            lore.add(FontUtils.parse("§f  ᴄᴏɪɴꜰʟɪᴘ ʜʀáčᴇ " + bet.creatorName));
            lore.add(Component.empty());
            lore.add(FontUtils.parse("&#ff8000ɪɴꜰᴏʀᴍᴀᴄᴇ"));
            lore.add(FontUtils.parse("§6⦿ §fꜱᴛᴀᴠ: &#00ff44čᴇᴋá"));
            lore.add(FontUtils.parse("§6$ §fčásᴛᴋᴀ: &#ff8000" + FontUtils.formatMoney(bet.amount) + " $"));
            lore.add(Component.empty());
            lore.add(FontUtils.parse("§8➡ &#ff8000ᴋʟɪᴋɴɪ ᴘʀᴏ sázᴋᴜ"));

            meta.lore(lore);
            head.setItemMeta(meta);
            inv.setItem(i, head);
        }

        // Statistics Item
        ItemStack statsItem = new ItemStack(Material.PAPER);
        ItemMeta statsMeta = statsItem.getItemMeta();
        statsMeta.displayName(FontUtils.parse("&#ff8000ᴛᴠᴏᴊᴇ sᴛᴀᴛɪsᴛɪᴋʏ"));

        CoinflipManager.CoinflipStats stats = manager.getStats(player.getUniqueId());
        List<Component> statsLore = new ArrayList<>();
        statsLore.add(FontUtils.parse("§8sᴛᴀᴛɪsᴛɪᴋʏ ʜʀáčᴇ"));
        statsLore.add(Component.empty());
        statsLore.add(FontUtils.parse("&#ff8000ɪɴꜰᴏʀᴍᴀᴄᴇ"));
        statsLore.add(FontUtils.parse("§a⚑ §fᴠýʜʀʏ: §a" + stats.wins + " §8(+§a" + FontUtils.formatMoney(stats.wonAmount) + " $§8)"));
        statsLore.add(FontUtils.parse("§c☹ §fᴘʀᴏʜʀʏ: §c" + stats.losses + " §8(-§c" + FontUtils.formatMoney(stats.lostAmount) + " $§8)"));

        statsMeta.lore(statsLore);
        statsItem.setItemMeta(statsMeta);
        inv.setItem(33, statsItem);

        // Tutorial Book
        ItemStack book = new ItemStack(Material.BOOK);
        ItemMeta bookMeta = book.getItemMeta();
        bookMeta.displayName(FontUtils.parse("&#ffbb00ᴊᴀᴋ ᴠʏᴛᴠᴏřɪᴛ ᴄᴏɪɴꜰʟɪᴘ?"));
        bookMeta.lore(List.of(
            FontUtils.parse("§7ᴘříᴋᴀᴢ: &#00fbff/ᴄꜰ <čásᴛᴋᴀ>"),
            FontUtils.parse("§7ᴘříᴋʟᴀᴅ: &#00fbff/ᴄꜰ 1000"),
            Component.empty(),
            FontUtils.parse("§7ᴛᴠá sázᴋᴀ sᴇ ᴘᴏᴛé"),
            FontUtils.parse("§7ᴢᴏʙʀᴀᴢí ᴢᴅᴇ ᴠ ᴍᴇɴᴜ.")
        ));
        book.setItemMeta(bookMeta);
        inv.setItem(31, book);

        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!(event.getInventory().getHolder() instanceof CoinflipGuiHolder)) return;

        event.setCancelled(true);
        int slot = event.getRawSlot();
        List<CoinflipManager.CoinflipBet> bets = manager.getActiveBets();

        if (slot >= 0 && slot < bets.size() && slot < 27) {
            CoinflipManager.CoinflipBet bet = bets.get(slot);
            if (bet.creator.equals(player.getUniqueId())) {
                player.sendMessage(FontUtils.parse("§c" + "ɴᴇᴍůžᴇš ʜʀáᴛ ᴘʀᴏᴛɪ sᴏʙě"));
                return;
            }

            if (plugin.getEconomy().getBalance(player) < bet.amount) {
                player.sendMessage(FontUtils.parse("§c" + "ɴᴇᴍáš ᴅᴏsᴛᴀᴛᴇᴋ ᴘᴇɴěᴢ"));
                return;
            }

            plugin.getEconomy().withdrawPlayer(player, bet.amount);
            manager.removeBet(bet);
            player.closeInventory();
            new CoinflipAnimation(plugin, Bukkit.getPlayer(bet.creator), player, bet.amount).start();
        }
    }

    private static class CoinflipGuiHolder implements InventoryHolder {
        @Override
        public @NotNull Inventory getInventory() {
            return null;
        }
    }
}
