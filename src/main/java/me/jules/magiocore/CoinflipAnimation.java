package me.jules.magiocore;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

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
        this.inv = Bukkit.createInventory(null, 27, FontUtils.parse("&#EA427Fᴄᴏɪɴꜰʟɪᴘ: " + (p1 != null ? p1.getName() : "ᴏꜰꜰʟɪɴᴇ") + " vs " + p2.getName()));
    }

    public void start() {
        if (p1 != null) p1.openInventory(inv);
        p2.openInventory(inv);

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= 20) {
                    finish();
                    cancel();
                    return;
                }

                Material mat = (ticks % 2 == 0) ? Material.ORANGE_STAINED_GLASS_PANE : Material.YELLOW_STAINED_GLASS_PANE;
                ItemStack glass = new ItemStack(mat);
                ItemMeta meta = glass.getItemMeta();
                meta.displayName(FontUtils.parse("§7" + "ʟᴏsᴏᴠáɴí..."));
                glass.setItemMeta(meta);

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

        double prize = amount * 2;
        plugin.getEconomy().depositPlayer(winner, prize);

        String msg = "&#EA427Fʜʀáč §f" + winner.getName() + " &#EA427Fᴠʏʜʀáʟ ᴠ ᴄᴏɪɴꜰɪʟᴘᴜ ᴏ &#00ff44" + prize + " $!";
        Bukkit.broadcast(FontUtils.parse(msg));

        if (p1 != null) p1.closeInventory();
        p2.closeInventory();
    }
}
