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
    private final String title = "&#EA427F» " + "COINFLIP MENU";

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

            meta.displayName(FontUtils.parse("&#ff8000COINFLIP HRáčE " + bet.creatorName));

            List<Component> lore = new ArrayList<>();
            lore.add(FontUtils.parse("§8COINFLIP MENU"));
            lore.add(Component.empty());
            lore.add(FontUtils.parse("§f  COINFLIP HRáčE " + bet.creatorName));
            lore.add(Component.empty());
            lore.add(FontUtils.parse("&#ff8000INFORMACE"));
            lore.add(FontUtils.parse("§6⦿ §fꜱTAV: &#00ff44čEKá"));
            lore.add(FontUtils.parse("§6$ §fčásTKA: &#ff8000" + FontUtils.formatMoney(bet.amount) + " $"));
            lore.add(Component.empty());
            lore.add(FontUtils.parse("§8➡ &#ff8000KLIKNI PRO sázKU"));

            meta.lore(lore);
            head.setItemMeta(meta);
            inv.setItem(i, head);
        }

        // Statistics Item
        ItemStack statsItem = new ItemStack(Material.PAPER);
        ItemMeta statsMeta = statsItem.getItemMeta();
        statsMeta.displayName(FontUtils.parse("&#ff8000TVOJE sTATIsTIKY"));

        CoinflipManager.CoinflipStats stats = manager.getStats(player.getUniqueId());
        List<Component> statsLore = new ArrayList<>();
        statsLore.add(FontUtils.parse("§8sTATIsTIKY HRáčE"));
        statsLore.add(Component.empty());
        statsLore.add(FontUtils.parse("&#ff8000INFORMACE"));
        statsLore.add(FontUtils.parse("§a⚑ §fVýHRY: §a" + stats.wins + " §8(+§a" + FontUtils.formatMoney(stats.wonAmount) + " $§8)"));
        statsLore.add(FontUtils.parse("§c☹ §fPROHRY: §c" + stats.losses + " §8(-§c" + FontUtils.formatMoney(stats.lostAmount) + " $§8)"));

        statsMeta.lore(statsLore);
        statsItem.setItemMeta(statsMeta);
        inv.setItem(33, statsItem);

        // Tutorial Book
        ItemStack book = new ItemStack(Material.BOOK);
        ItemMeta bookMeta = book.getItemMeta();
        bookMeta.displayName(FontUtils.parse("&#ffbb00JAK VYTVOřIT COINFLIP?"));
        bookMeta.lore(List.of(
            FontUtils.parse("§7PříKAZ: &#00fbff/CF <čásTKA>"),
            FontUtils.parse("§7PříKLAD: &#00fbff/CF 1000"),
            Component.empty(),
            FontUtils.parse("§7TVá sázKA sE POTé"),
            FontUtils.parse("§7ZOBRAZí ZDE V MENU.")
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
                player.sendMessage(FontUtils.parse("§c" + "NEMůžEš HRáT PROTI sOBě"));
                return;
            }

            if (plugin.getEconomy().getBalance(player) < bet.amount) {
                player.sendMessage(FontUtils.parse("§c" + "NEMáš DOsTATEK PENěZ"));
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
