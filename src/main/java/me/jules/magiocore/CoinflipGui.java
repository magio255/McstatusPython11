package me.jules.magiocore;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class CoinflipGui implements Listener {
    private final MagioCore plugin;
    private final CoinflipManager manager;

    public CoinflipGui(MagioCore plugin, CoinflipManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    public CoinflipManager getManager() {
        return manager;
    }

    public void open(Player player) {
        FileConfiguration config = plugin.getModuleManager().getModuleConfig("coinflip");
        String title = config.getString("gui.title", "&#69CA23&l💲 &#6BFF00&lCOINFLIP");

        Inventory inv = Bukkit.createInventory(new CoinflipGuiHolder(), 36, FontUtils.parse(title));

        // Border
        ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        if (glassMeta != null) {
            glassMeta.displayName(Component.empty());
            glass.setItemMeta(glassMeta);
        }
        for (int i = 27; i < 36; i++) {
            inv.setItem(i, glass);
        }

        // Stats item
        CoinflipManager.CoinflipStats stats = manager.getStats(player.getUniqueId());
        ItemStack statsItem = new ItemStack(Material.PAPER);
        ItemMeta statsMeta = statsItem.getItemMeta();
        if (statsMeta != null) {
            statsMeta.displayName(FontUtils.parse("&#EA427Fᴛᴠᴏᴊᴇ sᴛᴀᴛɪsᴛɪᴋʏ"));
            statsMeta.lore(List.of(
                    FontUtils.parse("§8sᴛᴀᴛɪsᴛɪᴋʏ ʜʀáčᴇ"),
                    Component.empty(),
                    FontUtils.parse("&#EA427Fɪɴꜰᴏʀᴍᴀᴄᴇ"),
                    FontUtils.parse("&#00ff44⚑ ᴠýʜʀʏ: " + stats.wins() + " §8(+&#00ff44$" + FontUtils.formatMoney(stats.wonAmount()) + "§8)", false),
                    FontUtils.parse("&#ff0000☹ ᴘʀᴏʜʀʏ: " + stats.losses() + " §8(-&#ff0000$" + FontUtils.formatMoney(stats.lostAmount()) + "§8)", false)
            ));
            statsItem.setItemMeta(statsMeta);
        }
        inv.setItem(31, statsItem);

        List<CoinflipManager.CoinflipBet> bets = manager.getActiveBets();

        for (int i = 0; i < bets.size() && i < 27; i++) {
            CoinflipManager.CoinflipBet bet = bets.get(i);
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(Bukkit.getOfflinePlayer(bet.creator));
                meta.displayName(FontUtils.parse("&#EA427F" + bet.creatorName + "'s ᴄᴏɪɴꜰʟɪᴘ"));

                meta.lore(List.of(
                        FontUtils.parse("§8ᴄᴏɪɴꜰʟɪᴘ ᴍᴇɴᴜ"),
                        Component.empty(),
                        FontUtils.parse("§f" + bet.creatorName + "'s ᴄᴏɪɴꜰʟɪᴘ"),
                        Component.empty(),
                        FontUtils.parse("&#EA427Fɪɴꜰᴏʀᴍᴀᴄᴇ"),
                        FontUtils.parse("§7◉ sᴛᴀᴛᴜs: &#00ff44čᴇᴋá"),
                        FontUtils.parse("§7$ sázᴋᴀ: &#EA427F$" + FontUtils.formatMoney(bet.amount)),
                        Component.empty(),
                        FontUtils.parse("§7➡ &#EA427Fᴋʟɪᴋɴɪ §7ᴘʀᴏ ᴘřɪᴘᴏᴊᴇɴí")
                ));
                head.setItemMeta(meta);
            }
            inv.setItem(i, head);
        }

        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof CoinflipGuiHolder) && !(holder instanceof CoinflipAnimation.CoinflipAnimationHolder)) return;

        event.setCancelled(true);
        if (holder instanceof CoinflipAnimation.CoinflipAnimationHolder) return;

        int slot = event.getRawSlot();
        List<CoinflipManager.CoinflipBet> bets = manager.getActiveBets();

        if (slot >= 0 && slot < bets.size() && slot < 27) {
            FileConfiguration config = plugin.getModuleManager().getModuleConfig("coinflip");
            CoinflipManager.CoinflipBet bet = bets.get(slot);
            if (bet.creator.equals(player.getUniqueId())) {
                player.sendMessage(FontUtils.parse(config.getString("messages.cannot-play-self", "§cɴᴇᴍůžᴇš ʜʀáᴛ ᴘʀᴏᴛɪ sᴏʙě")));
                return;
            }

            if (plugin.getEconomy().getBalance(player) < bet.amount) {
                player.sendMessage(FontUtils.parse(config.getString("messages.no-money", "§cɴᴇᴍáš ᴅᴏsᴛᴀᴛᴇᴋ ᴘᴇɴěᴢ")));
                return;
            }

            plugin.getEconomy().withdrawPlayer(player, bet.amount);
            manager.removeBet(bet);
            player.closeInventory();
            new CoinflipAnimation(plugin, Bukkit.getPlayer(bet.creator), player, bet.amount).start();
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof CoinflipGuiHolder || holder instanceof CoinflipAnimation.CoinflipAnimationHolder) {
            event.setCancelled(true);
        }
    }

    private static class CoinflipGuiHolder implements InventoryHolder {
        @Override
        public @NotNull Inventory getInventory() { return null; }
    }
}
