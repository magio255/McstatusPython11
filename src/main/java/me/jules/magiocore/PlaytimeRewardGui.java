package me.jules.magiocore;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
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
        int rows = config.getInt("gui.rows", 6);
        String title = config.getString("gui.title", "&8Odehraný čas - Strana %page%").replace("%page%", String.valueOf(page + 1));

        PlaytimeRewardHolder holder = new PlaytimeRewardHolder(page);
        Inventory inv = Bukkit.createInventory(holder, rows * 9, FontUtils.parse(title));
        holder.setInventory(inv);

        long playtimeTicks = player.getStatistic(Statistic.PLAY_ONE_MINUTE);
        long playtimeHours = playtimeTicks / (20 * 60 * 60);

        ConfigurationSection itemsSec = config.getConfigurationSection("gui.items");
        if (itemsSec != null) {
            for (String key : itemsSec.getKeys(false)) {
                ConfigurationSection itemSec = itemsSec.getConfigurationSection(key);
                if (itemSec == null) continue;

                if (key.equalsIgnoreCase("levels")) {
                    List<Integer> levelSlots = new ArrayList<>();
                    String slotsStr = itemSec.getString("slots");
                    for (String p : slotsStr.split(",")) {
                        if (p.contains("-")) {
                            String[] range = p.split("-");
                            for (int i = Integer.parseInt(range[0]); i <= Integer.parseInt(range[1]); i++) levelSlots.add(i);
                        } else levelSlots.add(Integer.parseInt(p.trim()));
                    }

                    int start = page * levelSlots.size();
                    for (int i = 0; i < levelSlots.size() && (start + i) < levels.size(); i++) {
                        PlaytimeLevel level = levels.get(start + i);
                        boolean claimed = rewardManager.hasClaimedPlaytime(player.getUniqueId(), level.id);
                        boolean unlocked = playtimeHours >= level.hours;

                        ItemStack item = new ItemStack(level.material);
                        ItemMeta meta = item.getItemMeta();
                        if (meta != null) {
                            meta.displayName(FontUtils.parse(config.getString("gui.level-name", "&6&lÚROVEŇ %id%").replace("%id%", String.valueOf(level.id))));
                            String status = claimed ? config.getString("gui.status-claimed", "&7JIŽ VYBRÁNO") :
                                            unlocked ? config.getString("gui.status-unlocked", "&aPŘIPRAVENO") :
                                            config.getString("gui.status-locked", "&cNEDOSTATEK ČASU");
                            String action = claimed ? config.getString("gui.action-claimed", "&7▶ VYBRÁNO") :
                                            unlocked ? config.getString("gui.action-unlocked", "&a▶ KLIKNI") :
                                            config.getString("gui.action-locked", "&c▶ ZAMČENO");

                            List<Component> lore = config.getStringList("gui.level-lore").stream()
                                    .map(s -> s.replace("%hours%", String.valueOf(level.hours))
                                            .replace("%amount%", FontUtils.formatMoney(level.amount))
                                            .replace("%status%", status)
                                            .replace("%status_action%", action))
                                    .map(FontUtils::parse).toList();
                            meta.lore(lore);
                            item.setItemMeta(meta);
                        }
                        inv.setItem(levelSlots.get(i), item);
                    }
                } else {
                    if (key.equalsIgnoreCase("back") && page == 0) continue;
                    if (key.equalsIgnoreCase("next") && (page + 1) * 21 >= levels.size()) continue; // Hardcoded 21 for now

                    Material mat = Material.valueOf(itemSec.getString("material", "AIR").toUpperCase());
                    ItemStack is = createNav(mat, itemSec.getString("name", " "), itemSec.getStringList("lore").stream().map(FontUtils::parse).toList());
                    if (itemSec.contains("slot")) inv.setItem(itemSec.getInt("slot"), is);
                    else if (itemSec.contains("slots")) {
                        for (int sIdx : FontUtils.parseSlots(itemSec.getString("slots"), inv.getSize())) {
                            inv.setItem(sIdx, is.clone());
                        }
                    }
                }
            }
        }

        player.openInventory(inv);
    }

    private ItemStack createNav(Material mat, String name, List<Component> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(FontUtils.parse(name));
            meta.lore(lore);
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
        ConfigurationSection items = config.getConfigurationSection("gui.items");
        if (items == null) return;

        for (String key : items.getKeys(false)) {
            if (items.getInt(key + ".slot", -1) == slot) {
                if (key.equalsIgnoreCase("back")) open(player, holder.page - 1);
                else if (key.equalsIgnoreCase("next")) open(player, holder.page + 1);
                return;
            }
        }

        if (items.contains("levels")) {
            List<Integer> levelSlots = new ArrayList<>();
            for (String p : items.getString("levels.slots").split(",")) {
                if (p.contains("-")) {
                    String[] range = p.split("-");
                    for (int i = Integer.parseInt(range[0]); i <= Integer.parseInt(range[1]); i++) levelSlots.add(i);
                } else levelSlots.add(Integer.parseInt(p.trim()));
            }

            for (int i = 0; i < levelSlots.size(); i++) {
                if (levelSlots.get(i) == slot) {
                    int levelIdx = holder.page * levelSlots.size() + i;
                    if (levelIdx < levels.size()) {
                        PlaytimeLevel level = levels.get(levelIdx);
                        long playtimeTicks = player.getStatistic(Statistic.PLAY_ONE_MINUTE);
                        long playtimeHours = playtimeTicks / (20 * 60 * 60);

                        if (playtimeHours >= level.hours && !rewardManager.hasClaimedPlaytime(player.getUniqueId(), level.id)) {
                            rewardManager.setClaimedPlaytime(player.getUniqueId(), level.id);
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), level.command.replace("%player%", player.getName()));
                            player.sendMessage(FontUtils.parse(config.getString("messages.claimed", "&8「&6Odměna&8」 &7Odměna za úroveň &6%id% &7byla vybrána!").replace("%id%", String.valueOf(level.id))));
                            open(player, holder.page); // Refresh
                        }
                    }
                    break;
                }
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
        private Inventory inventory;
        PlaytimeRewardHolder(int page) { this.page = page; }
        public void setInventory(Inventory inventory) { this.inventory = inventory; }
        @Override public @NotNull Inventory getInventory() { return inventory; }
    }
}
