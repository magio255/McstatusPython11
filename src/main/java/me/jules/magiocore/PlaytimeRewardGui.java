package me.jules.magiocore;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Statistic;
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
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PlaytimeRewardGui implements Listener {
    private final MagioCore plugin;
    private final RewardManager rewardManager;
    private final List<PlaytimeLevel> levels = new ArrayList<>();

    public PlaytimeRewardGui(MagioCore plugin, RewardManager rewardManager) {
        this.plugin = plugin;
        this.rewardManager = rewardManager;
        setupLevels();
    }

    private void setupLevels() {
        long[] fixedRewards = {
            50000, 120000, 300000, 450000, 500000, 600000, 700000, 1000000,
            1100000, 1300000, 1500000, 1600000, 1700000, 2000000, 2100000, 2300000
        };

        for (int i = 1; i <= 147; i++) {
            int hours = (int) (i * 8.85);
            if (i == 1) hours = 1;
            if (i == 147) hours = 1300;

            long amount;
            if (i <= fixedRewards.length) {
                amount = fixedRewards[i - 1];
            } else {
                amount = 2300000L + (long) (i - 16) * 250000L;
            }

            Material mat = getLevelMaterial(i);
            levels.add(new PlaytimeLevel(i, hours, "money give %player% " + amount, amount, mat));
        }
    }

    private Material getLevelMaterial(int level) {
        Material[] mats = {
            Material.RED_CANDLE, Material.ORANGE_CANDLE, Material.YELLOW_CANDLE,
            Material.LIME_CANDLE, Material.LIGHT_BLUE_CANDLE, Material.PURPLE_CANDLE,
            Material.PINK_CANDLE
        };
        return mats[(level - 1) % mats.length];
    }

    public void open(Player player, int page) {
        FileConfiguration config = plugin.getModuleManager().getModuleConfig("playtimerewards");
        String title = config.getString("gui.title", "ᴏᴅᴇʜʀᴀɴý čᴀs - sᴛʀᴀɴᴀ %page%").replace("%page%", String.valueOf(page + 1));

        Inventory inv = Bukkit.createInventory(new PlaytimeRewardHolder(page), 54, FontUtils.parse(title));

        ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        if (glassMeta != null) {
            glassMeta.displayName(Component.empty());
            glass.setItemMeta(glassMeta);
        }
        for (int i = 0; i < 9; i++) inv.setItem(i, glass);
        for (int i = 45; i < 54; i++) inv.setItem(i, glass);
        for (int i = 0; i < 54; i += 9) inv.setItem(i, glass);
        for (int i = 8; i < 54; i += 9) inv.setItem(i, glass);

        if (page > 0) inv.setItem(48, createItem(Material.ARROW, config.getString("gui.nav-back", "&#00fbffᴘřᴇᴅᴄʜᴏᴢí sᴛʀᴀɴᴀ")));
        if (page < 6) inv.setItem(50, createItem(Material.ARROW, config.getString("gui.nav-next", "&#00fbffᴅᴀʟší sᴛʀᴀɴᴀ")));

        int start = page * 21;
        int[] slots = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
        };

        long playtimeTicks = player.getStatistic(Statistic.PLAY_ONE_MINUTE);
        long playtimeHours = playtimeTicks / (20 * 60 * 60);

        for (int i = 0; i < 21 && (start + i) < levels.size(); i++) {
            PlaytimeLevel level = levels.get(start + i);
            boolean claimed = rewardManager.hasClaimedPlaytime(player.getUniqueId(), level.id);
            boolean unlocked = playtimeHours >= level.hours;

            ItemStack item = new ItemStack(level.material);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.displayName(FontUtils.parse(config.getString("gui.level-name", "&#ffbb00úʀᴏᴠᴇň %id%").replace("%id%", String.valueOf(level.id))));

                String status = claimed ? config.getString("gui.status-claimed", "&#EA427Fᴊɪž ᴠʏʙʀáɴᴏ") :
                                unlocked ? config.getString("gui.status-unlocked", "&#00ff44ᴋʟɪᴋɴɪ ᴘʀᴏ ᴠʏʙʀáɴí") :
                                config.getString("gui.status-locked", "§cɴᴇᴍáš ᴅᴏsᴛᴀᴛᴇᴋ čᴀsᴜ");

                List<Component> lore = config.getStringList("gui.level-lore").stream()
                        .map(s -> s.replace("%hours%", String.valueOf(level.hours))
                                  .replace("%amount%", FontUtils.formatMoney(level.amount))
                                  .replace("%status%", status))
                        .map(FontUtils::parse)
                        .collect(Collectors.toList());

                meta.lore(lore);
                item.setItemMeta(meta);
            }
            inv.setItem(slots[i], item);
        }

        player.openInventory(inv);
    }

    private ItemStack createItem(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(FontUtils.parse(name));
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!(event.getInventory().getHolder() instanceof PlaytimeRewardHolder holder)) return;

        event.setCancelled(true);
        int slot = event.getRawSlot();

        FileConfiguration config = plugin.getModuleManager().getModuleConfig("playtimerewards");

        if (slot == 48 && holder.page > 0) {
            open(player, holder.page - 1);
            return;
        }
        if (slot == 50 && holder.page < 6) {
            open(player, holder.page + 1);
            return;
        }

        int[] slots = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
        };

        for (int i = 0; i < slots.length; i++) {
            if (slot == slots[i]) {
                int levelIdx = holder.page * 21 + i;
                if (levelIdx < levels.size()) {
                    PlaytimeLevel level = levels.get(levelIdx);
                    long playtimeTicks = player.getStatistic(Statistic.PLAY_ONE_MINUTE);
                    long playtimeHours = playtimeTicks / (20 * 60 * 60);

                    if (playtimeHours >= level.hours && !rewardManager.hasClaimedPlaytime(player.getUniqueId(), level.id)) {
                        rewardManager.setClaimedPlaytime(player.getUniqueId(), level.id);
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), level.command.replace("%player%", player.getName()));
                        player.sendMessage(FontUtils.parse(config.getString("messages.claimed", "&#00ff44ᴏᴅᴍěɴᴀ ᴢᴀ úʀᴏᴠᴇň %id% ʙʏʟᴀ ᴠʏʙʀáɴᴀ!").replace("%id%", String.valueOf(level.id))));
                        open(player, holder.page); // Refresh
                    }
                }
                break;
            }
        }
    }

    private static class PlaytimeLevel {
        int id;
        int hours;
        String command;
        long amount;
        Material material;

        PlaytimeLevel(int id, int hours, String command, long amount, Material material) {
            this.id = id;
            this.hours = hours;
            this.command = command;
            this.amount = amount;
            this.material = material;
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof PlaytimeRewardHolder) {
            event.setCancelled(true);
        }
    }

    private static class PlaytimeRewardHolder implements InventoryHolder {
        int page;
        PlaytimeRewardHolder(int page) { this.page = page; }
        @Override public @NotNull Inventory getInventory() { return null; }
    }
}
