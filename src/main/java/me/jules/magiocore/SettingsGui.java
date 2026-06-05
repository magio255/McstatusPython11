package me.jules.magiocore;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
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

import java.util.List;

public class SettingsGui implements CommandExecutor, Listener {
    private final MagioCore plugin;
    private final SettingsManager manager;

    public SettingsGui(MagioCore plugin, SettingsManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;

        open(player);
        return true;
    }

    public void open(Player player) {
        FileConfiguration config = plugin.getModuleManager().getModuleConfig("settings");
        String title = config.getString("gui.title", "&#EA427F» ɴᴀsᴛᴀᴠᴇɴí");

        Inventory inv = Bukkit.createInventory(new SettingsHolder(), 36, FontUtils.parse(title));
        SettingsManager.PlayerSettings s = manager.getSettings(player.getUniqueId());

        // Background
        ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        if (glassMeta != null) {
            glassMeta.displayName(Component.empty());
            glass.setItemMeta(glassMeta);
        }
        for (int i = 0; i < 36; i++) inv.setItem(i, glass);

        ConfigurationSection items = config.getConfigurationSection("gui.items");
        if (items != null) {
            addItem(inv, items.getConfigurationSection("chat"), s.chat());
            addItem(inv, items.getConfigurationSection("msg"), s.msg());
            addItem(inv, items.getConfigurationSection("bossbar"), s.bossbar());
            addItem(inv, items.getConfigurationSection("kitOnDeath"), s.kitOnDeath());
            addItem(inv, items.getConfigurationSection("tpaInvites"), s.tpaInvites());
            addItem(inv, items.getConfigurationSection("tpaAuto"), s.tpaAuto());
            addItem(inv, items.getConfigurationSection("mobSpawn"), s.mobSpawn());
            addItem(inv, items.getConfigurationSection("nightVision"), s.nightVision());

            // Scoreboard (External command)
            ConfigurationSection sb = items.getConfigurationSection("scoreboard");
            if (sb != null) {
                ItemStack item = new ItemStack(Material.PAPER);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.displayName(FontUtils.parse(sb.getString("name")));
                    meta.lore(List.of(FontUtils.parse(sb.getString("lore"))));
                    item.setItemMeta(meta);
                }
                inv.setItem(sb.getInt("slot"), item);
            }
        }

        player.openInventory(inv);
    }

    private void addItem(Inventory inv, ConfigurationSection sec, boolean state) {
        if (sec == null) return;
        ItemStack item = new ItemStack(state ? Material.LIME_DYE : Material.GRAY_DYE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(FontUtils.parse(sec.getString("name")));
            String lore = state ? sec.getString("lore-enabled") : sec.getString("lore-disabled");
            meta.lore(List.of(FontUtils.parse(lore), Component.empty(), FontUtils.parse("&#EA427Fᴋʟɪᴋɴɪ ᴘʀᴏ ᴢᴍěɴᴜ")));
            item.setItemMeta(meta);
        }
        inv.setItem(sec.getInt("slot"), item);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!(event.getInventory().getHolder() instanceof SettingsHolder)) return;

        event.setCancelled(true);
        int slot = event.getRawSlot();
        FileConfiguration config = plugin.getModuleManager().getModuleConfig("settings");
        ConfigurationSection items = config.getConfigurationSection("gui.items");
        if (items == null) return;

        SettingsManager.PlayerSettings s = manager.getSettings(player.getUniqueId());

        if (slot == items.getInt("chat.slot")) {
            manager.updateSettings(player.getUniqueId(), s.withChat(!s.chat()));
        } else if (slot == items.getInt("msg.slot")) {
            manager.updateSettings(player.getUniqueId(), s.withMsg(!s.msg()));
        } else if (slot == items.getInt("bossbar.slot")) {
            manager.updateSettings(player.getUniqueId(), s.withBossbar(!s.bossbar()));
        } else if (slot == items.getInt("kitOnDeath.slot")) {
            manager.updateSettings(player.getUniqueId(), s.withKitOnDeath(!s.kitOnDeath()));
        } else if (slot == items.getInt("tpaInvites.slot")) {
            manager.updateSettings(player.getUniqueId(), s.withTpaInvites(!s.tpaInvites()));
        } else if (slot == items.getInt("tpaAuto.slot")) {
            manager.updateSettings(player.getUniqueId(), s.withTpaAuto(!s.tpaAuto()));
        } else if (slot == items.getInt("mobSpawn.slot")) {
            manager.updateSettings(player.getUniqueId(), s.withMobSpawn(!s.mobSpawn()));
        } else if (slot == items.getInt("nightVision.slot")) {
            boolean newState = !s.nightVision();
            manager.updateSettings(player.getUniqueId(), s.withNightVision(newState));
            if (newState) {
                player.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.NIGHT_VISION, -1, 0, false, false));
            } else {
                player.removePotionEffect(org.bukkit.potion.PotionEffectType.NIGHT_VISION);
            }
        } else if (slot == items.getInt("scoreboard.slot")) {
            player.performCommand("sb");
            player.closeInventory();
            return;
        } else {
            return;
        }

        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 1f, 1f);
        open(player);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof SettingsHolder) {
            event.setCancelled(true);
        }
    }

    private static class SettingsHolder implements InventoryHolder {
        @Override public @NotNull Inventory getInventory() { return null; }
    }
}
