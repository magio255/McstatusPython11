package me.jules.magiocore;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ChatListener implements Listener {
    private final MagioCore plugin;
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, Boolean> searchMode = new ConcurrentHashMap<>();

    public ChatListener(MagioCore plugin) {
        this.plugin = plugin;
    }

    public void setSearchMode(UUID uuid, boolean mode) {
        if (mode) searchMode.put(uuid, true);
        else searchMode.remove(uuid);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        SettingsManager.PlayerSettings senderSettings = plugin.getSettingsManager().getSettings(player.getUniqueId());
        if (!senderSettings.chat()) {
            event.setCancelled(true);
            player.sendMessage(FontUtils.parse("§c" + "ᴍáš ᴠʏᴘɴᴜᴛý ᴄʜᴀᴛ ᴠ ɴᴀsᴛᴀᴠᴇɴí."));
            return;
        }

        // Check ignored players and global chat settings
        event.getRecipients().removeIf(viewerPlayer -> {
            if (plugin.getIgnoreModule().isIgnored(viewerPlayer.getUniqueId(), player.getUniqueId())) return true;
            return !plugin.getSettingsManager().getSettings(viewerPlayer.getUniqueId()).chat();
        });

        String message = event.getMessage();

        if (ItemEditListener.pendingInput.containsKey(player.getUniqueId())) {
            String type = ItemEditListener.pendingInput.remove(player.getUniqueId());
            event.setCancelled(true);

            ItemStack item = player.getInventory().getItemInMainHand();
            if (item == null || item.getType().isAir()) return;
            ItemMeta meta = item.getItemMeta();

            Bukkit.getScheduler().runTask(plugin, () -> {
                try {
                    switch (type) {
                        case "rename":
                            meta.displayName(FontUtils.parse(message));
                            item.setItemMeta(meta);
                            break;
                        case "lore_add":
                            java.util.List<net.kyori.adventure.text.Component> lore = meta.lore();
                            if (lore == null) lore = new java.util.ArrayList<>();
                            lore.add(FontUtils.parse(message));
                            meta.lore(lore);
                            item.setItemMeta(meta);
                            break;
                        case "repaircost":
                            ((org.bukkit.inventory.meta.Repairable) meta).setRepairCost(Integer.parseInt(message));
                            item.setItemMeta(meta);
                            break;
                        case "amount":
                            item.setAmount(Integer.parseInt(message));
                            break;
                        case "durability":
                            int max = item.getType().getMaxDurability();
                            ((org.bukkit.inventory.meta.Damageable) meta).setDamage(max - Integer.parseInt(message));
                            item.setItemMeta(meta);
                            break;
                        case "skullowner":
                            ((org.bukkit.inventory.meta.SkullMeta) meta).setOwningPlayer(Bukkit.getOfflinePlayer(message));
                            item.setItemMeta(meta);
                            break;
                        case "custommodeldata":
                            meta.setCustomModelData(Integer.parseInt(message));
                            item.setItemMeta(meta);
                            break;
                        case "material":
                            org.bukkit.Material mat = org.bukkit.Material.matchMaterial(message.toUpperCase());
                            if (mat != null) {
                                item.setType(mat);
                                item.setItemMeta(meta);
                            } else {
                                player.sendMessage(FontUtils.parse("§c" + "ɴᴇᴘʟᴀᴛɴý ᴍᴀᴛᴇʀɪáʟ."));
                                return;
                            }
                            break;
                    }
                    player.sendMessage(FontUtils.parse("&#00fbff" + "ᴘřᴇᴅᴍěᴛ ʙʏʟ ᴜᴘʀᴀᴠᴇɴ."));
                } catch (Exception e) {
                    player.sendMessage(FontUtils.parse("§c" + "ᴄʜʏʙᴀ: " + e.getMessage()));
                }
            });
            return;
        }

        if (searchMode.getOrDefault(player.getUniqueId(), false)) {
            event.setCancelled(true);
            setSearchMode(player.getUniqueId(), false);
            plugin.getBaltopGui().handleSearch(player, message);
            return;
        }

        if (player.hasPermission("magiocore.admin") || player.isOp()) return;

        FileConfiguration config = plugin.getModuleManager().getModuleConfig("chat");
        long delay = config.getLong("anti-spam.delay", 2) * 1000L;
        long now = System.currentTimeMillis();
        if (cooldowns.containsKey(player.getUniqueId())) {
            long last = cooldowns.get(player.getUniqueId());
            if (now - last < delay) {
                event.setCancelled(true);
                player.sendMessage(FontUtils.parse("§c" + "ɴᴇᴘíš ᴛᴀᴋ ʀʏᴄʜʟᴇ."));
                return;
            }
        }
        cooldowns.put(player.getUniqueId(), now);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        cooldowns.remove(event.getPlayer().getUniqueId());
        searchMode.remove(event.getPlayer().getUniqueId());
    }
}
