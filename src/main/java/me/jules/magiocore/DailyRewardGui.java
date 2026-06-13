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
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DailyRewardGui implements Listener {
    private final MagioCore plugin;
    private final RewardManager rewardManager;

    public DailyRewardGui(MagioCore plugin, RewardManager rewardManager) {
        this.plugin = plugin;
        this.rewardManager = rewardManager;
    }

    public void open(Player player) {
        FileConfiguration config = plugin.getModuleManager().getModuleConfig("dailyrewards");
        ConfigurationSection gui = config.getConfigurationSection("gui");
        if (gui == null) return;

        int rows = gui.getInt("rows", 3);
        DailyRewardHolder holder = new DailyRewardHolder();
        Inventory inv = Bukkit.createInventory(holder, rows * 9, FontUtils.parse(gui.getString("title", "&8Denní odměna")));
        holder.setInventory(inv);

        ConfigurationSection itemsSec = gui.getConfigurationSection("items");
        if (itemsSec != null) {
            for (String key : itemsSec.getKeys(false)) {
                ConfigurationSection itemSec = itemsSec.getConfigurationSection(key);
                if (itemSec == null) continue;

                if (key.equalsIgnoreCase("reward")) {
                    long lastClaim = rewardManager.getLastDailyClaim(player.getUniqueId());
                    long now = System.currentTimeMillis();
                    long diff = now - lastClaim;
                    boolean canClaim = diff >= 24 * 60 * 60 * 1000;
                    int streak = rewardManager.getDailyStreak(player.getUniqueId());
                    if (diff > 48 * 60 * 60 * 1000) streak = 0;
                    long baseAmount = config.getLong("base-amount", 100000);
                    double multiplier = 1.0 + (Math.min(streak, 40) * 0.1);
                    long amount = (long) (baseAmount * multiplier);

                    Material mat = Material.valueOf(itemSec.getString(canClaim ? "material" : "cooldown-material", "CHEST").toUpperCase());
                    ItemStack is = new ItemStack(mat);
                    ItemMeta meta = is.getItemMeta();
                    if (meta != null) {
                        meta.displayName(FontUtils.parse(itemSec.getString("name", "&6&lDENNÍ TRUHLA")));
                        List<Component> lore = new ArrayList<>();
                        if (canClaim) {
                            for (String s : itemSec.getStringList("lore")) lore.add(FontUtils.parse(s));
                            lore.add(Component.empty());
                            lore.add(FontUtils.parse(gui.getString("streak-format", " &fStreak: &e%streak% dní").replace("%streak%", String.valueOf(streak + 1))));
                            lore.add(FontUtils.parse(gui.getString("reward-format", " &fOdměna: &a%amount%$").replace("%amount%", FontUtils.formatMoney(amount))));
                            double nextMultiplier = 1.0 + (Math.min(streak + 1, 40) * 0.1);
                            lore.add(FontUtils.parse(gui.getString("next-reward-format", " &fPříští: &b%amount%$").replace("%amount%", FontUtils.formatMoney((long)(baseAmount * nextMultiplier)))));
                            lore.add(Component.empty());
                            lore.add(FontUtils.parse(gui.getString("action-claim", "&6▶ &lKLIKNI &6Pro vybrání!")));
                        } else {
                            long remaining = 24 * 60 * 60 * 1000 - diff;
                            for (String s : itemSec.getStringList("cooldown-lore")) lore.add(FontUtils.parse(s.replace("%time%", formatTime(remaining))));
                            lore.add(Component.empty());
                            lore.add(FontUtils.parse(gui.getString("streak-format", " &fStreak: &e%streak% dní").replace("%streak%", String.valueOf(streak))));
                            double nextMultiplier = 1.0 + (Math.min(streak, 40) * 0.1);
                            lore.add(FontUtils.parse(gui.getString("next-reward-format", " &fPříští: &b%amount%$").replace("%amount%", FontUtils.formatMoney((long)(baseAmount * nextMultiplier)))));
                            lore.add(Component.empty());
                            lore.add(FontUtils.parse(gui.getString("action-cooldown", "&c▶ &lČEKEJ &cNa další odměnu!")));
                        }
                        meta.lore(lore);
                        is.setItemMeta(meta);
                    }
                    inv.setItem(itemSec.getInt("slot"), is);
                } else {
                    Material mat = Material.valueOf(itemSec.getString("material", "AIR").toUpperCase());
                    ItemStack is = new ItemStack(mat);
                    ItemMeta meta = is.getItemMeta();
                    if (meta != null) {
                        meta.displayName(FontUtils.parse(itemSec.getString("name", " ")));
                        meta.lore(itemSec.getStringList("lore").stream().map(FontUtils::parse).toList());
                        is.setItemMeta(meta);
                    }
                    if (itemSec.contains("slot")) inv.setItem(itemSec.getInt("slot"), is);
                    else if (itemSec.contains("slots")) {
                        for (String p : itemSec.getString("slots").split(",")) {
                            if (p.contains("-")) {
                                String[] range = p.split("-");
                                for (int i = Integer.parseInt(range[0]); i <= Integer.parseInt(range[1]); i++) inv.setItem(i, is.clone());
                            } else inv.setItem(Integer.parseInt(p.trim()), is.clone());
                        }
                    }
                }
            }
        }

        player.openInventory(inv);
    }

    private String formatTime(long ms) {
        long hours = ms / (60 * 60 * 1000);
        long minutes = (ms % (60 * 60 * 1000)) / (60 * 1000);
        long seconds = (ms % (60 * 1000)) / 1000;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!(event.getInventory().getHolder() instanceof DailyRewardHolder)) return;

        event.setCancelled(true);
        int slot = event.getRawSlot();
        FileConfiguration config = plugin.getModuleManager().getModuleConfig("dailyrewards");
        ConfigurationSection items = config.getConfigurationSection("gui.items");
        if (items == null) return;

        if (items.contains("reward") && items.getInt("reward.slot") == slot) {
            long lastClaim = rewardManager.getLastDailyClaim(player.getUniqueId());
            long now = System.currentTimeMillis();
            long diff = now - lastClaim;

            if (diff >= 24 * 60 * 60 * 1000) {
                int streak = rewardManager.getDailyStreak(player.getUniqueId());
                if (diff > 48 * 60 * 60 * 1000) streak = 0;
                long baseAmount = config.getLong("base-amount", 100000);
                long amount = (long) (baseAmount * (1.0 + (Math.min(streak, 40) * 0.1)));
                rewardManager.setLastDailyClaim(player.getUniqueId(), now);
                rewardManager.setDailyStreak(player.getUniqueId(), streak + 1);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "money give " + player.getName() + " " + amount);
                player.sendMessage(FontUtils.parse(config.getString("messages.claimed", "&8「&6Odměna&8」 &7Denní odměna byla úspěšně vybrána!")));
                player.sendMessage(FontUtils.parse(config.getString("messages.summary", "&8「&6Odměna&8」 &7Získal jsi &a%amount%$ &7(Streak: &e%streak% dní&7)")
                        .replace("%amount%", FontUtils.formatMoney(amount)).replace("%streak%", String.valueOf(streak + 1))));
                player.closeInventory();
            } else {
                player.sendMessage(FontUtils.parse(config.getString("messages.cooldown", "&8「&cOdměna&8」 &7Tuto odměnu můžeš vybrat až za 24 hodin.")));
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof DailyRewardHolder) {
            event.setCancelled(true);
        }
    }

    private static class DailyRewardHolder implements InventoryHolder {
        private Inventory inventory;
        public void setInventory(Inventory inventory) { this.inventory = inventory; }
        @Override public @NotNull Inventory getInventory() { return inventory; }
    }
}
