package me.jules.magiocore;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class RespawnListener implements Listener {
    private final MagioCore plugin;

    public RespawnListener(MagioCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (plugin.getSettingsManager().getSettings(player.getUniqueId()).kitOnDeath()) {
            giveKit(player);
        }
    }

    private void giveKit(Player player) {
        PlayerInventory inv = player.getInventory();
        inv.setHelmet(new ItemStack(Material.CHAINMAIL_HELMET));
        inv.setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE));
        inv.setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS));
        inv.setBoots(new ItemStack(Material.CHAINMAIL_BOOTS));

        inv.addItem(new ItemStack(Material.STONE_SWORD));
        inv.addItem(new ItemStack(Material.STONE_PICKAXE));
        inv.addItem(new ItemStack(Material.STONE_AXE));
        inv.addItem(new ItemStack(Material.STONE_SHOVEL));
    }
}
