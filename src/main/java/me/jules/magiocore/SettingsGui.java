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
        String title = config.getString("gui.title", "SETTINGS");

        SettingsHolder holder = new SettingsHolder();
        Inventory inv = Bukkit.createInventory(holder, 36, FontUtils.parse(title, false));
        holder.setInventory(inv);
        SettingsManager.PlayerSettings s = manager.getSettings(player.getUniqueId());

        ConfigurationSection items = config.getConfigurationSection("gui.items");
        if (items != null) {
            addItem(inv, items.getConfigurationSection("chat"), s.chat(), Material.OAK_SIGN);
            addItem(inv, items.getConfigurationSection("msg"), s.msg(), Material.PAPER);
            addItem(inv, items.getConfigurationSection("bossbar"), s.bossbar(), Material.EMERALD);
            addItem(inv, items.getConfigurationSection("kitOnDeath"), s.kitOnDeath(), Material.CHAINMAIL_HELMET);
            addItem(inv, items.getConfigurationSection("tpaInvites"), s.tpaInvites(), Material.FEATHER);
            addItem(inv, items.getConfigurationSection("tpaAuto"), s.tpaAuto(), Material.ENDER_PEARL);
            addItem(inv, items.getConfigurationSection("mobSpawn"), s.mobSpawn(), Material.ZOMBIE_HEAD);
            addItem(inv, items.getConfigurationSection("nightVision"), s.nightVision(), Material.ENDER_EYE);

            // Scoreboard (External command)
            ConfigurationSection sb = items.getConfigurationSection("scoreboard");
            if (sb != null) {
                ItemStack item = new ItemStack(Material.LECTERN);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.displayName(FontUtils.parse(sb.getString("name", "Scoreboard"), false));
                    meta.lore(List.of(FontUtils.parse(sb.getString("lore", "§7Klikni pro zapnutí/vypnutí."), false)));
                    item.setItemMeta(meta);
                }
                inv.setItem(sb.getInt("slot", 10), item);
            }

            // Deco items
            ConfigurationSection deco = items.getConfigurationSection("deco");
            if (deco != null) {
                for (String key : deco.getKeys(false)) {
                    int slot = deco.getInt(key + ".slot");
                    Material mat = Material.valueOf(deco.getString(key + ".material"));
                    ItemStack item = new ItemStack(mat);
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null) {
                        meta.displayName(Component.empty());
                        item.setItemMeta(meta);
                    }
                    inv.setItem(slot, item);
                }
            }
        }

        player.openInventory(inv);
    }

    private void addItem(Inventory inv, ConfigurationSection sec, boolean state, Material fallback) {
        if (sec == null) return;
        Material mat = fallback;
        if (sec.contains("material")) {
            try {
                mat = Material.valueOf(sec.getString("material"));
            } catch (Exception ignored) {}
        }
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(FontUtils.parse(sec.getString("name", "Settings"), false));
            String lore = state ? sec.getString("lore-enabled", "§7Stav: &#00ff44Zapnuto") : sec.getString("lore-disabled", "§7Stav: &#ff0000Vypnuto");
            meta.lore(List.of(FontUtils.parse(lore, false), Component.empty(), FontUtils.parse("&#EA427FKlikni pro zmenu", false)));
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
        private Inventory inventory;
        public void setInventory(Inventory inventory) { this.inventory = inventory; }
        @Override public @NotNull Inventory getInventory() { return inventory; }
    }
}
