package me.jules.magiocore;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class CoinflipAnimation {
    private final MagioCore plugin;
    private final Player p1;
    private final Player p2;
    private final double amount;
    private final Inventory inv;
    private final Random random = new Random();

    public CoinflipAnimation(MagioCore plugin, Player p1, Player p2, double amount) {
        this.plugin = plugin;
        this.p1 = p1;
        this.p2 = p2;
        this.amount = amount;
        CoinflipAnimationHolder holder = new CoinflipAnimationHolder();
        this.inv = Bukkit.createInventory(holder, 27, FontUtils.parse("&#EA427Fᴄᴏɪɴꜰʟɪᴘ: " + (p1 != null ? p1.getName() : "ᴏꜰꜰʟɪɴᴇ") + " vs " + p2.getName()));
        holder.setInventory(this.inv);
    }

    public void start() {
        if (p1 != null) p1.openInventory(inv);
        p2.openInventory(inv);

        String style = plugin.getSettingsManager().getSettings(p2.getUniqueId()).coinflipStyle();
        Material m1, m2;
        switch (style) {
            case "COSMIC" -> {
                m1 = Material.PURPLE_STAINED_GLASS_PANE;
                m2 = Material.MAGENTA_STAINED_GLASS_PANE;
            }
            case "FLAME" -> {
                m1 = Material.RED_STAINED_GLASS_PANE;
                m2 = Material.ORANGE_STAINED_GLASS_PANE;
            }
            default -> {
                m1 = Material.ORANGE_STAINED_GLASS_PANE;
                m2 = Material.YELLOW_STAINED_GLASS_PANE;
            }
        }

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= 20) {
                    finish();
                    cancel();
                    return;
                }

                Material mat = (ticks % 2 == 0) ? m1 : m2;
                ItemStack glass = new ItemStack(mat);
                ItemMeta meta = glass.getItemMeta();
                if (meta != null) {
                    meta.displayName(FontUtils.parse("§7" + "ʟᴏsᴏᴠáɴí..."));
                    glass.setItemMeta(meta);
                }

                for (int i = 0; i < 27; i++) {
                    inv.setItem(i, glass);
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }

    private void finish() {
        Player winner = random.nextBoolean() ? p1 : p2;
        if (winner == null) winner = p2; // Fallback if p1 is offline

        Player loser = (winner == p1) ? p2 : p1;

        double prize = amount * 2;
        plugin.getEconomy().depositPlayer(winner, prize);

        plugin.getCoinflipGui().getManager().updateStats(winner.getUniqueId(), true, amount);
        if (loser != null) {
            plugin.getCoinflipGui().getManager().updateStats(loser.getUniqueId(), false, amount);
        }

        String msg = "&#EA427Fʜʀáč §f" + winner.getName() + " &#EA427Fᴠʏʜʀáʟ ᴠ ᴄᴏɪɴꜰɪʟᴘᴜ ᴏ &#00ff44" + FontUtils.formatMoney(prize) + " $!";
        Bukkit.broadcast(FontUtils.parse(msg));

        if (p1 != null) p1.closeInventory();
        p2.closeInventory();
    }

    public static class CoinflipAnimationHolder implements InventoryHolder {
        private Inventory inventory;
        public void setInventory(Inventory inventory) { this.inventory = inventory; }
        @Override public @NotNull Inventory getInventory() { return inventory; }
    }
}
