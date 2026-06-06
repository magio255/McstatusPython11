package me.jules.magiocore;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CoinflipGui implements Listener {
    private final MagioCore plugin;
    private final CoinflipManager manager;
    private final Map<UUID, Integer> playerPages = new HashMap<>();

    public CoinflipGui(MagioCore plugin, CoinflipManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    public CoinflipManager getManager() {
        return manager;
    }

    public void open(Player player) {
        open(player, 1);
    }

    public void open(Player player, int page) {
        FileConfiguration config = plugin.getModuleManager().getModuleConfig("coinflip");
        String title = config.getString("gui.title", "&#69CA23&l💲 &#6BFF00&lCOINFLIP");
        playerPages.put(player.getUniqueId(), page);

        CoinflipGuiHolder holder = new CoinflipGuiHolder();
        Inventory inv = Bukkit.createInventory(holder, 54, FontUtils.parse(title));
        holder.setInventory(inv);

        // Layout items from reference
        ItemStack filler = createItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        int[] fillerSlots = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 47, 51};
        for (int slot : fillerSlots) inv.setItem(slot, filler);

        // Stats item
        CoinflipManager.CoinflipStats stats = manager.getStats(player.getUniqueId());
        ItemStack statsItem = createItem(Material.WRITABLE_BOOK, "&#FFD34A&lTVÉ STATISTIKY");
        ItemMeta statsMeta = statsItem.getItemMeta();
        double winRate = (stats.wins() + stats.losses() == 0) ? 0 : (double) stats.wins() / (stats.wins() + stats.losses()) * 100;
        statsMeta.lore(List.of(
                FontUtils.parse("§8"),
                FontUtils.parse("&#FFD34AStatistiky"),
                FontUtils.parse(" &#56E364✔ &fVýhry: &#56E364" + stats.wins()),
                FontUtils.parse(" &#FF6B8A✘ &fProhry: &#FF6B8A" + stats.losses()),
                FontUtils.parse(" &#FFB347&l⚡ &fPoměr: &#FFB347" + String.format("%.1f", winRate) + "%"),
                FontUtils.parse("§8"),
                FontUtils.parse("&#FFD34APeníze"),
                FontUtils.parse(" &#56E364$ &fVyhráno: &#56E364" + FontUtils.formatMoney(stats.wonAmount()) + "$"),
                FontUtils.parse(" &#FF6B8A$ &fVsazeno: &#FF6B8A" + FontUtils.formatMoney(stats.lostAmount()) + "$"),
                FontUtils.parse(" &#56E364$ &fProfit: &#56E364" + FontUtils.formatMoney(stats.wonAmount() - stats.lostAmount()) + "$"),
                FontUtils.parse("§8"),
                FontUtils.parse("&#FFD34APřejeď pro tvé statistiky!")
        ));
        statsItem.setItemMeta(statsMeta);
        inv.setItem(45, statsItem);

        inv.setItem(46, createItem(Material.GLOWSTONE_DUST, "&#45FF93&lŘAZENÍ"));
        inv.setItem(48, createItem(Material.RED_SHULKER_BOX, "&#FF2300&lPŘEDCHOZÍ STRANA"));
        inv.setItem(49, createItem(Material.BELL, "&#4ACFFF&lAKTUALIZOVAT"));
        inv.setItem(50, createItem(Material.LIME_SHULKER_BOX, "&#7CFF00&lDALŠÍ STRANA"));
        inv.setItem(52, createItem(Material.PURPLE_DYE, "&#B445FF&lSTYL ANIMACE"));
        inv.setItem(53, createItem(Material.SUNFLOWER, "&#FFD34A&lINFORMACE"));

        List<CoinflipManager.CoinflipBet> bets = manager.getActiveBets();
        int[] betSlots = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
        };

        int start = (page - 1) * betSlots.length;
        for (int i = 0; i < betSlots.length && (start + i) < bets.size(); i++) {
            CoinflipManager.CoinflipBet bet = bets.get(start + i);
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(Bukkit.getOfflinePlayer(bet.creator));
                meta.displayName(FontUtils.parse("&#71FF00Sázka hráče " + bet.creatorName));

                meta.lore(List.of(
                        FontUtils.parse("&8Coinflip"),
                        Component.empty(),
                        FontUtils.parse("&#71FF00Informace:"),
                        FontUtils.parse("&fTato hra má &#77FFB050% &fšanci"),
                        FontUtils.parse("&fna výhru v &#77FFB0každém hodu"),
                        Component.empty(),
                        FontUtils.parse(" &#71FF00&l$ &fČástka: &#71FF00" + FontUtils.formatMoney(bet.amount)),
                        FontUtils.parse(" &#FF732C⌚ &fVyprší za: &#FFFF0060m"),
                        FontUtils.parse(" &#FF428A🏹 &fMěna: &#FF428APeníze"),
                        Component.empty(),
                        FontUtils.parse("&#71FF00Klikni pro sázku!")
                ));
                head.setItemMeta(meta);
            }
            inv.setItem(betSlots[i], head);
        }

        player.openInventory(inv);
    }

    private ItemStack createItem(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(FontUtils.parse(name, true));
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof CoinflipGuiHolder) && !(holder instanceof CoinflipAnimation.CoinflipAnimationHolder)) return;

        event.setCancelled(true);
        if (holder instanceof CoinflipAnimation.CoinflipAnimationHolder) return;

        int slot = event.getRawSlot();
        int page = playerPages.getOrDefault(player.getUniqueId(), 1);
        List<CoinflipManager.CoinflipBet> bets = manager.getActiveBets();

        int[] betSlots = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
        };

        int betIndex = -1;
        for (int i = 0; i < betSlots.length; i++) {
            if (slot == betSlots[i]) {
                betIndex = (page - 1) * betSlots.length + i;
                break;
            }
        }

        if (betIndex != -1 && betIndex < bets.size()) {
            FileConfiguration config = plugin.getModuleManager().getModuleConfig("coinflip");
            CoinflipManager.CoinflipBet bet = bets.get(betIndex);
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
            return;
        }

        if (slot == 48 && page > 1) {
            open(player, page - 1);
        } else if (slot == 50 && page * betSlots.length < bets.size()) {
            open(player, page + 1);
        } else if (slot == 49) {
            open(player, page);
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
        private Inventory inventory;
        public void setInventory(Inventory inventory) { this.inventory = inventory; }
        @Override public @NotNull Inventory getInventory() { return inventory; }
    }
}
