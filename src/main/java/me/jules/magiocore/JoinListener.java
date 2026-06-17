package me.jules.magiocore;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.List;
import java.util.UUID;

public class JoinListener implements Listener {
    private final MagioCore plugin;

    public JoinListener(MagioCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        event.joinMessage(null); // Suppress default join message

        FileConfiguration config = plugin.getModuleManager().getModuleConfig("join");

        if (!player.hasPlayedBefore()) {
            String format = config.getString("join-message.first-join.format", "&#00fbff%player% §7se poprvé připojil!");
            Component msg = FontUtils.parse(format.replace("%player%", player.getName()), false);
            Bukkit.broadcast(msg);

            if (config.getBoolean("join-message.show-head", true)) {
                sendHeadMessage(player, config.getStringList("join-message.first-join.side-messages"));
            }

            // Starter Kit logic
            if (config.getBoolean("starter-kit.enabled", true)) {
                for (String entry : config.getStringList("starter-kit.items")) {
                    try {
                        String[] parts = entry.split(":");
                        org.bukkit.Material mat = org.bukkit.Material.valueOf(parts[0]);
                        int amount = Integer.parseInt(parts[1]);

                        org.bukkit.inventory.ItemStack item = new org.bukkit.inventory.ItemStack(mat, amount);

                        // Check if it's armor and equip it automatically if slots are empty
                        if (mat.name().endsWith("_HELMET") && player.getInventory().getHelmet() == null) player.getInventory().setHelmet(item);
                        else if (mat.name().endsWith("_CHESTPLATE") && player.getInventory().getChestplate() == null) player.getInventory().setChestplate(item);
                        else if (mat.name().endsWith("_LEGGINGS") && player.getInventory().getLeggings() == null) player.getInventory().setLeggings(item);
                        else if (mat.name().endsWith("_BOOTS") && player.getInventory().getBoots() == null) player.getInventory().setBoots(item);
                        else player.getInventory().addItem(item);

                    } catch (Exception ignored) {}
                }
            }

            // Teleport to spawn on first join
            FileConfiguration spawnConfig = plugin.getModuleManager().getModuleConfig("spawn");
            org.bukkit.Location spawn = spawnConfig.getLocation("location");
            if (spawn != null) {
                player.teleport(spawn);
            }
        } else {
            String format = config.getString("join-message.private-welcome.format", "§7Vítej zpět, &#00fbff%player%§7!");
            Component msg = FontUtils.parse(format.replace("%player%", player.getName()), false);
            player.sendMessage(msg);

            if (config.getBoolean("join-message.show-head", true)) {
                sendHeadMessage(player, config.getStringList("join-message.private-welcome.side-messages"));
            }
        }

        handleInventoryManagement(player, !player.hasPlayedBefore());
    }

    private void handleInventoryManagement(Player player, boolean firstJoin) {
        FileConfiguration config = plugin.getModuleManager().getModuleConfig("join");
        ConfigurationSection invSec = config.getConfigurationSection("inventory-management");
        if (invSec == null) return;

        if (invSec.getBoolean("clear-on-join", false)) {
            if (!invSec.getBoolean("clear-only-first-join", false) || firstJoin) {
                player.getInventory().clear();
            }
        }

        ConfigurationSection joinItems = invSec.getConfigurationSection("join-items");
        if (joinItems != null && joinItems.getBoolean("enabled", false)) {
            if (!joinItems.getBoolean("only-first-join", true) || firstJoin) {
                ConfigurationSection items = joinItems.getConfigurationSection("items");
                if (items != null) {
                    for (String slotStr : items.getKeys(false)) {
                        try {
                            int slot = Integer.parseInt(slotStr);
                            ConfigurationSection itemSec = items.getConfigurationSection(slotStr);
                            if (itemSec == null) continue;

                            ItemStack is = new ItemStack(Material.valueOf(itemSec.getString("material", "AIR").toUpperCase()), itemSec.getInt("amount", 1));
                            ItemMeta meta = is.getItemMeta();
                            if (meta != null && itemSec.contains("name")) {
                                meta.displayName(FontUtils.parse(itemSec.getString("name")));
                                is.setItemMeta(meta);
                            }

                            if (slot >= 100 && slot <= 103) {
                                if (slot == 100) player.getInventory().setBoots(is);
                                else if (slot == 101) player.getInventory().setLeggings(is);
                                else if (slot == 102) player.getInventory().setChestplate(is);
                                else if (slot == 103) player.getInventory().setHelmet(is);
                            } else {
                                player.getInventory().setItem(slot, is);
                            }
                        } catch (Exception ignored) {}
                    }
                }
            }
        }
    }

    @EventHandler
    public void onJoinSettings(org.bukkit.event.player.PlayerJoinEvent event) {
        Player player = event.getPlayer();
        SettingsManager.PlayerSettings s = plugin.getSettingsManager().getSettings(player.getUniqueId());

        if (s.nightVision()) {
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.NIGHT_VISION, -1, 0, false, false));
        }
    }

    private void giveKit(Player player) {
        org.bukkit.inventory.PlayerInventory inv = player.getInventory();
        inv.setHelmet(new org.bukkit.inventory.ItemStack(org.bukkit.Material.CHAINMAIL_HELMET));
        inv.setChestplate(new org.bukkit.inventory.ItemStack(org.bukkit.Material.CHAINMAIL_CHESTPLATE));
        inv.setLeggings(new org.bukkit.inventory.ItemStack(org.bukkit.Material.CHAINMAIL_LEGGINGS));
        inv.setBoots(new org.bukkit.inventory.ItemStack(org.bukkit.Material.CHAINMAIL_BOOTS));

        inv.addItem(new org.bukkit.inventory.ItemStack(org.bukkit.Material.STONE_SWORD));
        inv.addItem(new org.bukkit.inventory.ItemStack(org.bukkit.Material.STONE_PICKAXE));
        inv.addItem(new org.bukkit.inventory.ItemStack(org.bukkit.Material.STONE_AXE));
        inv.addItem(new org.bukkit.inventory.ItemStack(org.bukkit.Material.STONE_SHOVEL));
    }

    private void sendHeadMessage(Player player, List<String> sideMessages) {
        SkinUtils.getHeadRows(player).thenAccept(rows -> {
            player.sendMessage(Component.empty());
            for (int i = 0; i < 8; i++) {
                Component headRow = rows.get(i);

                // Text in the middle (rows 3, 4, 5, 6)
                int sideIndex = -1;
                if (i == 2) sideIndex = 0;
                else if (i == 3) sideIndex = 1;
                else if (i == 4) sideIndex = 2;
                else if (i == 5) sideIndex = 3;

                String sideText = (sideIndex != -1 && sideIndex < sideMessages.size())
                    ? sideMessages.get(sideIndex).replace("%player%", player.getName())
                    : "";

                Component line = headRow.append(Component.text("  ")).append(FontUtils.parse(sideText, false));
                player.sendMessage(line);
            }
            player.sendMessage(Component.empty());
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        event.quitMessage(null); // Suppress default quit message
        UUID uuid = event.getPlayer().getUniqueId();

        // Memory Leak Cleanup
        plugin.getChatListener().clearData(uuid);
        plugin.getCoinflipGui().clearData(uuid);
        TeleportUtils.cancelPendingTeleport(event.getPlayer());
        ItemEditListener.pendingInput.remove(uuid);
    }
}
