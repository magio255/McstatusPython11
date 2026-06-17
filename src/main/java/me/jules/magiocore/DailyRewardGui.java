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

        Inventory inv = Bukkit.createInventory(new DailyRewardHolder(), 27, FontUtils.parse(gui.getString("title", "ᴅᴇɴɴí ᴏᴅᴍěɴᴀ")));

        // Fill background
        ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        if (glassMeta != null) {
            glassMeta.displayName(Component.empty());
            glass.setItemMeta(glassMeta);
        }
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, glass);
        }

        long lastClaim = rewardManager.getLastDailyClaim(player.getUniqueId());
        long now = System.currentTimeMillis();
        long diff = now - lastClaim;
        boolean canClaim = diff >= 24 * 60 * 60 * 1000;

        int streak = rewardManager.getDailyStreak(player.getUniqueId());
        if (diff > 48 * 60 * 60 * 1000) streak = 0;

        long baseAmount = config.getLong("base-amount", 100000);
        double multiplier = 1.0 + (Math.min(streak, 40) * 0.1);
        long amount = (long) (baseAmount * multiplier);

        ItemStack chest = new ItemStack(canClaim ? Material.CHEST : Material.MINECART);
        ItemMeta meta = chest.getItemMeta();
        if (meta != null) {
            meta.displayName(FontUtils.parse(gui.getString("chest-name")));
            List<Component> lore = new ArrayList<>();
            if (canClaim) {
                for (String s : gui.getStringList("chest-lore")) {
                    lore.add(FontUtils.parse(s));
                }
                lore.add(FontUtils.parse("§7"));
                lore.add(FontUtils.parse(gui.getString("streak-format", "&#ffbb00ᴀᴋᴛᴜáʟɴí sᴛʀᴇᴀᴋ: &#00fbff%streak% ᴅɴí").replace("%streak%", String.valueOf(streak + 1))));
                lore.add(FontUtils.parse(gui.getString("reward-format", "&#ffbb00ᴏᴅᴍěɴᴀ: &#00ff44%amount% $").replace("%amount%", FontUtils.formatMoney(amount))));

                double nextMultiplier = 1.0 + (Math.min(streak + 1, 40) * 0.1);
                long nextAmount = (long) (baseAmount * nextMultiplier);
                lore.add(FontUtils.parse(gui.getString("next-reward-format", "&#ffbb00ᴘříšᴛí ᴏᴅᴍěɴᴀ: &#00fbff%amount% $").replace("%amount%", FontUtils.formatMoney(nextAmount))));
            } else {
                long remaining = 24 * 60 * 60 * 1000 - diff;
                String timeStr = formatTime(remaining);
                for (String s : gui.getStringList("cooldown-lore")) {
                    lore.add(FontUtils.parse(s.replace("%time%", timeStr)));
                }
                lore.add(FontUtils.parse("§7"));
                lore.add(FontUtils.parse(gui.getString("streak-format", "&#ffbb00ᴛᴠůj sᴛʀᴇᴀᴋ: &#00fbff%streak% ᴅɴí").replace("%streak%", String.valueOf(streak))));

                double nextMultiplier = 1.0 + (Math.min(streak, 40) * 0.1);
                long nextAmount = (long) (baseAmount * nextMultiplier);
                lore.add(FontUtils.parse(gui.getString("next-reward-format", "&#ffbb00ᴘříšᴛí ᴏᴅᴍěɴᴀ: &#00fbff%amount% $").replace("%amount%", FontUtils.formatMoney(nextAmount))));
            }
            meta.lore(lore);
            chest.setItemMeta(meta);
        }

        inv.setItem(13, chest);
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
        if (event.getRawSlot() == 13) {
            FileConfiguration config = plugin.getModuleManager().getModuleConfig("dailyrewards");
            long lastClaim = rewardManager.getLastDailyClaim(player.getUniqueId());
            long now = System.currentTimeMillis();
            long diff = now - lastClaim;

            if (diff >= 24 * 60 * 60 * 1000) {
                int streak = rewardManager.getDailyStreak(player.getUniqueId());
                if (diff > 48 * 60 * 60 * 1000) streak = 0;

                long baseAmount = config.getLong("base-amount", 100000);
                double multiplier = 1.0 + (Math.min(streak, 40) * 0.1);
                long amount = (long) (baseAmount * multiplier);

                rewardManager.setLastDailyClaim(player.getUniqueId(), now);
                rewardManager.setDailyStreak(player.getUniqueId(), streak + 1);

                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "money give " + player.getName() + " " + amount);

                player.sendMessage(FontUtils.parse(config.getString("messages.claimed", "&#00ff44ᴅᴇɴɴí ᴏᴅᴍěɴᴀ ʙʏʟᴀ ᴠʏʙʀáɴᴀ!")));
                player.sendMessage(FontUtils.parse(config.getString("messages.summary", "&#ffbb00ᴢísᴋᴀʟ ᴊsɪ: &#00ff44%amount% $ §7(sᴛʀᴇᴀᴋ: %streak% ᴅɴí)")
                        .replace("%amount%", FontUtils.formatMoney(amount))
                        .replace("%streak%", String.valueOf(streak + 1))));
                player.closeInventory();
            } else {
                player.sendMessage(FontUtils.parse(config.getString("messages.cooldown", "§cᴏᴅᴍěɴᴜ sɪ ᴍůžᴇš ᴠʏʙʀáᴛ ᴀž ᴢᴀ 24 ʜᴏᴅɪɴ.")));
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
        @Override
        public @NotNull Inventory getInventory() { return null; }
    }
}
