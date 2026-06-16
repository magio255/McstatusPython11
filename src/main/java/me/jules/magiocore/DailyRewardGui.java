package me.jules.magiocore;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class DailyRewardGui implements Listener {
    private final MagioCore plugin;
    private final RewardManager rewardManager;

    public DailyRewardGui(MagioCore plugin, RewardManager rewardManager) {
        this.plugin = plugin;
        this.rewardManager = rewardManager;
    }

    public void open(Player player) {
        ConfigurationSection config = plugin.getConfig().getConfigurationSection("daily-rewards.gui");
        if (config == null) return;

        Inventory inv = Bukkit.createInventory(new DailyRewardHolder(), 27, FontUtils.parse(config.getString("title", "ᴅᴇɴɴí ᴏᴅᴍěɴᴀ")));

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

        ItemStack chest = new ItemStack(canClaim ? Material.CHEST : Material.MINECART);
        ItemMeta meta = chest.getItemMeta();
        if (meta != null) {
            meta.displayName(FontUtils.parse(config.getString("chest-name")));
            if (canClaim) {
                List<String> lore = config.getStringList("chest-lore");
                meta.lore(lore.stream().map(FontUtils::parse).collect(Collectors.toList()));
            } else {
                long remaining = 24 * 60 * 60 * 1000 - diff;
                String timeStr = formatTime(remaining);
                List<String> lore = config.getStringList("cooldown-lore");
                meta.lore(lore.stream().map(s -> FontUtils.parse(s.replace("%time%", timeStr))).collect(Collectors.toList()));
            }
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
            long lastClaim = rewardManager.getLastDailyClaim(player.getUniqueId());
            long now = System.currentTimeMillis();
            if (now - lastClaim >= 24 * 60 * 60 * 1000) {
                rewardManager.setLastDailyClaim(player.getUniqueId(), now);
                String command = plugin.getConfig().getString("daily-rewards.command");
                if (command != null) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName()));
                }
                player.sendMessage(FontUtils.parse("&#00ff44" + "ᴅᴇɴɴí ᴏᴅᴍěɴᴀ ʙʏʟᴀ ᴠʏʙʀáɴᴀ!"));
                player.closeInventory();
            } else {
                player.sendMessage(FontUtils.parse("§c" + "ᴏᴅᴍěɴᴜ sɪ ᴍůžᴇš ᴠʏʙʀáᴛ ᴀž ᴢᴀ 24 ʜᴏᴅɪɴ."));
            }
        }
    }

    private static class DailyRewardHolder implements InventoryHolder {
        @Override
        public @NotNull Inventory getInventory() {
            return null;
        }
    }
}
