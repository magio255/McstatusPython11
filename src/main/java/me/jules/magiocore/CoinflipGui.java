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

    public void open(Player player) {
        FileConfiguration config = plugin.getModuleManager().getModuleConfig("coinflip");
        String title = config.getString("gui.title", "&#EA427F» ᴄᴏɪɴꜰɪʟᴘ ᴍᴇɴᴜ");

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

        List<CoinflipManager.CoinflipBet> bets = manager.getActiveBets();
        String entryName = config.getString("gui.entry.name", "&#00fbffʜʀáč: §f%player%");
        List<String> entryLore = config.getStringList("gui.entry.lore");
        if (entryLore.isEmpty()) {
            entryLore = List.of("§7sázᴋᴀ: &#00ff44%amount% $", "", "&#EA427Fᴋʟɪᴋɴɪ ᴘʀᴏ sázᴋᴜ!");
        }

        for (int i = 0; i < bets.size() && i < 27; i++) {
            CoinflipManager.CoinflipBet bet = bets.get(i);
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(Bukkit.getOfflinePlayer(bet.creator));
                meta.displayName(FontUtils.parse(entryName.replace("%player%", bet.creatorName)));

                List<Component> finalLore = entryLore.stream()
                        .map(s -> FontUtils.parse(s.replace("%amount%", String.valueOf(bet.amount))))
                        .collect(Collectors.toList());
                meta.lore(finalLore);
                head.setItemMeta(meta);
            }
            inv.setItem(i, head);
        }

        // Tutorial Book
        ConfigurationSection bookSec = config.getConfigurationSection("gui.tutorial-book");
        if (bookSec != null) {
            ItemStack book = new ItemStack(Material.BOOK);
            ItemMeta bookMeta = book.getItemMeta();
            if (bookMeta != null) {
                bookMeta.displayName(FontUtils.parse(bookSec.getString("name", "&#ffbb00ᴊᴀᴋ ᴠʏᴛᴠᴏřɪᴛ ᴄᴏɪɴꜰɪʟᴘ?")));
                bookMeta.lore(bookSec.getStringList("lore").stream().map(FontUtils::parse).collect(Collectors.toList()));
                book.setItemMeta(bookMeta);
                inv.setItem(31, book);
            }
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
