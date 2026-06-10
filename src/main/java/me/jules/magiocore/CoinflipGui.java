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

    public void clearData(UUID uuid) {
        playerPages.remove(uuid);
    }

    public void open(Player player) {
        open(player, 1);
    }

    public void open(Player player, int page) {
        FileConfiguration config = plugin.getModuleManager().getModuleConfig("coinflip");
        String title = config.getString("gui.title", "&8Coinflip - Hlavní");
        playerPages.put(player.getUniqueId(), page);

        CoinflipGuiHolder holder = new CoinflipGuiHolder();
        Inventory inv = Bukkit.createInventory(holder, 54, FontUtils.parse(title));
        holder.setInventory(inv);

        // Glassmorphism Border Design
        ItemStack blackGlass = createItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        ItemStack grayGlass = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");

        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                inv.setItem(i, (i % 2 == 0) ? blackGlass : grayGlass);
            }
        }

        // Stats item
        CoinflipManager.CoinflipStats stats = manager.getStats(player.getUniqueId());
        ItemStack statsItem = createItem(Material.WRITABLE_BOOK, config.getString("gui.stats.name", "&e&lTVÉ STATISTIKY"));
        ItemMeta statsMeta = statsItem.getItemMeta();
        double winRate = (stats.wins() + stats.losses() == 0) ? 0 : (double) stats.wins() / (stats.wins() + stats.losses()) * 100;

        List<Component> statsLore = config.getStringList("gui.stats.lore").stream()
                .map(s -> s.replace("%wins%", String.valueOf(stats.wins()))
                        .replace("%losses%", String.valueOf(stats.losses()))
                        .replace("%ratio%", String.format("%.1f", winRate))
                        .replace("%won%", FontUtils.formatMoney(stats.wonAmount()))
                        .replace("%lost%", FontUtils.formatMoney(stats.lostAmount()))
                        .replace("%profit%", FontUtils.formatMoney(stats.wonAmount() - stats.lostAmount())))
                .map(FontUtils::parse)
                .toList();

        statsMeta.lore(statsLore);
        statsItem.setItemMeta(statsMeta);
        inv.setItem(45, statsItem);

        inv.setItem(46, createItem(Material.GLOWSTONE_DUST, "&a&lSEŘAZENÍ", List.of(
                FontUtils.parse("&7Změní způsob řazení"),
                FontUtils.parse("&7všech aktivních sázek."),
                FontUtils.parse(""),
                FontUtils.parse("&aInformace:"),
                FontUtils.parse(" &fKlikni pro změnu"),
                FontUtils.parse(" &fřazení sázek."),
                FontUtils.parse(""),
                FontUtils.parse("&a▶ &lKLIKNI &aPro změnu!")
        )));
        inv.setItem(48, createItem(Material.RED_SHULKER_BOX, "&c&lPŘEDCHOZÍ", List.of(
                FontUtils.parse("&7Vrátí tě na předchozí"),
                FontUtils.parse("&7stránku se sázkami."),
                FontUtils.parse(""),
                FontUtils.parse("&cInformace:"),
                FontUtils.parse(" &fKlikni pro přechod"),
                FontUtils.parse(" &fna předchozí stranu."),
                FontUtils.parse(""),
                FontUtils.parse("&c▶ &lKLIKNI &cPro přechod!")
        )));
        inv.setItem(49, createItem(Material.BELL, "&b&lAKTUALIZOVAT", List.of(
                FontUtils.parse("&7Aktualizuje seznam všech"),
                FontUtils.parse("&7právě probíhajících sázek."),
                FontUtils.parse(""),
                FontUtils.parse("&bInformace:"),
                FontUtils.parse(" &fKlikni pro aktualizaci"),
                FontUtils.parse(" &fseznamu sázek."),
                FontUtils.parse(""),
                FontUtils.parse("&b▶ &lKLIKNI &bPro aktualizaci!")
        )));
        inv.setItem(50, createItem(Material.LIME_SHULKER_BOX, "&a&lDALŠÍ", List.of(
                FontUtils.parse("&7Posune tě na další"),
                FontUtils.parse("&7stránku se sázkami."),
                FontUtils.parse(""),
                FontUtils.parse("&aInformace:"),
                FontUtils.parse(" &fKlikni pro přechod"),
                FontUtils.parse(" &fna další stranu."),
                FontUtils.parse(""),
                FontUtils.parse("&a▶ &lKLIKNI &aPro přechod!")
        )));

        SettingsManager.PlayerSettings settings = plugin.getSettingsManager().getSettings(player.getUniqueId());
        String currentStyle = settings.coinflipStyle();
        inv.setItem(52, createItem(Material.PURPLE_DYE, "&d&lSTYL ANIMACE", List.of(
                FontUtils.parse("&7Změní vizuální styl"),
                FontUtils.parse("&7tvé výherní animace."),
                FontUtils.parse(""),
                FontUtils.parse("&dInformace:"),
                FontUtils.parse(" &fAktuální: &d" + currentStyle),
                FontUtils.parse(""),
                FontUtils.parse("&fDostupné styly:"),
                FontUtils.parse(" &d» &fCLASSIC"),
                FontUtils.parse(" &d» &fCOSMIC"),
                FontUtils.parse(" &d» &fFLAME"),
                FontUtils.parse(""),
                FontUtils.parse("&d▶ &lKLIKNI &dPro změnu stylu!")
        )));
        inv.setItem(53, createItem(Material.SUNFLOWER, config.getString("gui.tutorial-book.name", "&6&lJAK HRÁT?"), config.getStringList("gui.tutorial-book.lore").stream().map(FontUtils::parse).toList()));

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
                meta.displayName(FontUtils.parse(config.getString("gui.entry.name", "&a&lSÁZKA HRÁČE %player%").replace("%player%", bet.creatorName)));

                List<Component> betLore = config.getStringList("gui.entry.lore").stream()
                        .map(s -> s.replace("%total%", FontUtils.formatMoney(bet.amount * 2))
                                .replace("%amount%", FontUtils.formatMoney(bet.amount)))
                        .map(FontUtils::parse)
                        .toList();

                meta.lore(betLore);
                head.setItemMeta(meta);
            }
            inv.setItem(betSlots[i], head);
        }

        player.openInventory(inv);
    }

    private ItemStack createItem(Material mat, String name) {
        return createItem(mat, name, null);
    }

    private ItemStack createItem(Material mat, String name, List<Component> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(FontUtils.parse(name, true));
            if (lore != null) meta.lore(lore);
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
        } else if (slot == 52) { // Style cycle
            SettingsManager.PlayerSettings settings = plugin.getSettingsManager().getSettings(player.getUniqueId());
            String current = settings.coinflipStyle();
            String next = current.equals("CLASSIC") ? "COSMIC" : current.equals("COSMIC") ? "FLAME" : "CLASSIC";
            plugin.getSettingsManager().updateSettings(player.getUniqueId(), settings.withCoinflipStyle(next));
            player.sendMessage(FontUtils.parse("[#4498DB]「[#6BFF00]ᴄᴏɪɴꜰʟɪᴘ[#4498DB]」 [#B445FF]sᴛʏʟ ᴀɴɪᴍᴀᴄᴇ ᴢᴍěɴěɴ ɴᴀ: [#B445FF]" + next));
            player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 1f, 1f);
            open(player, page);
        } else if (slot == 53) { // Information
            player.sendMessage(FontUtils.parse("[#4498DB]「[#6BFF00]ᴄᴏɪɴꜰʟɪᴘ[#4498DB]」 [#FFD34A]ɪɴꜰᴏʀᴍᴀᴄᴇ:"));
            player.sendMessage(FontUtils.parse("[#FFD34A]- sázíš ᴘʀᴏᴛɪ ᴏsᴛᴀᴛɴíᴍ ʜʀáčůᴍ."));
            player.sendMessage(FontUtils.parse("[#FFD34A]- šᴀɴᴄᴇ ɴᴀ ᴠýʜʀᴜ ᴊᴇ 50/50."));
            player.sendMessage(FontUtils.parse("[#FFD34A]- ᴠýʜʀᴀ ᴊᴇ 2x ᴛᴠá sázᴋᴀ."));
            player.closeInventory();
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
