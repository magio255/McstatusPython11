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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            Map<String, String> enabled = new HashMap<>();
            if (plugin.getModuleManager().isEnabled("chat")) enabled.put("chat", "OAK_SIGN");
            if (plugin.getModuleManager().isEnabled("msg")) enabled.put("msg", "PAPER");
            if (plugin.getModuleManager().isEnabled("afkzone")) enabled.put("bossbar", "EMERALD");
            if (plugin.getModuleManager().isEnabled("deathsystem")) enabled.put("kitOnDeath", "CHAINMAIL_HELMET");
            if (plugin.getModuleManager().isEnabled("tpa")) {
                enabled.put("tpaInvites", "FEATHER");
                enabled.put("tpaAuto", "ENDER_PEARL");
            }
            if (plugin.getModuleManager().isEnabled("mobspawn")) enabled.put("mobSpawn", "ZOMBIE_HEAD");
            enabled.put("nightVision", "ENDER_EYE");

            List<String> keys = List.of("chat", "msg", "bossbar", "kitOnDeath", "tpaInvites", "tpaAuto", "mobSpawn", "nightVision");
            List<String> active = new ArrayList<>();
            for (String key : keys) if (enabled.containsKey(key)) active.add(key);

            int count = active.size();
            int startSlot = 13 - (count / 2);
            if (count > 7) startSlot = 10;

            int currentSlot = startSlot;
            for (int i = 0; i < count; i++) {
                String key = active.get(i);
                if (i == 7) currentSlot = 22 - ((count - 7) / 2);

                addItem(inv, items.getConfigurationSection(key), getVal(s, key), Material.valueOf(enabled.get(key)), currentSlot);
                holder.getSlotMap().put(currentSlot, key);
                currentSlot++;
            }
        }

        player.openInventory(inv);
    }

    private boolean getVal(SettingsManager.PlayerSettings s, String key) {
        return switch (key) {
            case "chat" -> s.chat();
            case "msg" -> s.msg();
            case "bossbar" -> s.bossbar();
            case "kitOnDeath" -> s.kitOnDeath();
            case "tpaInvites" -> s.tpaInvites();
            case "tpaAuto" -> s.tpaAuto();
            case "mobSpawn" -> s.mobSpawn();
            case "nightVision" -> s.nightVision();
            default -> false;
        };
    }

    private void addItem(Inventory inv, ConfigurationSection sec, boolean state, Material fallback, int slot) {
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
        inv.setItem(slot, item);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!(event.getInventory().getHolder() instanceof SettingsHolder holder)) return;

        event.setCancelled(true);
        int slot = event.getRawSlot();
        String key = holder.getSlotMap().get(slot);
        if (key == null) return;

        SettingsManager.PlayerSettings s = manager.getSettings(player.getUniqueId());

        switch (key) {
            case "chat" -> manager.updateSettings(player.getUniqueId(), s.withChat(!s.chat()));
            case "msg" -> manager.updateSettings(player.getUniqueId(), s.withMsg(!s.msg()));
            case "bossbar" -> manager.updateSettings(player.getUniqueId(), s.withBossbar(!s.bossbar()));
            case "kitOnDeath" -> manager.updateSettings(player.getUniqueId(), s.withKitOnDeath(!s.kitOnDeath()));
            case "tpaInvites" -> manager.updateSettings(player.getUniqueId(), s.withTpaInvites(!s.tpaInvites()));
            case "tpaAuto" -> manager.updateSettings(player.getUniqueId(), s.withTpaAuto(!s.tpaAuto()));
            case "mobSpawn" -> manager.updateSettings(player.getUniqueId(), s.withMobSpawn(!s.mobSpawn()));
            case "nightVision" -> {
                boolean newState = !s.nightVision();
                manager.updateSettings(player.getUniqueId(), s.withNightVision(newState));
                if (newState) {
                    player.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.NIGHT_VISION, -1, 0, false, false));
                } else {
                    player.removePotionEffect(org.bukkit.potion.PotionEffectType.NIGHT_VISION);
                }
            }
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
        private final Map<Integer, String> slotMap = new HashMap<>();

        public void setInventory(Inventory inventory) { this.inventory = inventory; }
        public Map<Integer, String> getSlotMap() { return slotMap; }
        @Override public @NotNull Inventory getInventory() { return inventory; }
    }
}
