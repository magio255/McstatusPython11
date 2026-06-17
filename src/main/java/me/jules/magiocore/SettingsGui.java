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
        int rows = config.getInt("gui.rows", 4);
        String title = config.getString("gui.title", "&8Nastavení");

        SettingsHolder holder = new SettingsHolder();
        Inventory inv = Bukkit.createInventory(holder, rows * 9, FontUtils.parse(title));
        holder.setInventory(inv);
        SettingsManager.PlayerSettings s = manager.getSettings(player.getUniqueId());

        ConfigurationSection items = config.getConfigurationSection("gui.items");
        if (items != null) {
            for (String key : items.getKeys(false)) {
                ConfigurationSection sec = items.getConfigurationSection(key);
                if (sec == null) continue;

                if (isToggle(key)) {
                    addItem(inv, sec, getVal(s, key), Material.BARRIER, sec.getInt("slot", 0));
                    holder.getSlotMap().put(sec.getInt("slot"), key);
                } else {
                    Material mat = Material.valueOf(sec.getString("material", "AIR").toUpperCase());
                    ItemStack is = new ItemStack(mat);
                    ItemMeta meta = is.getItemMeta();
                    if (meta != null) {
                        meta.displayName(FontUtils.parse(sec.getString("name", " ")));
                        meta.lore(sec.getStringList("lore").stream().map(FontUtils::parse).toList());
                        is.setItemMeta(meta);
                    }
                    if (sec.contains("slot")) {
                        inv.setItem(sec.getInt("slot"), is);
                    } else if (sec.contains("slots")) {
                        for (int sIdx : FontUtils.parseSlots(sec.getString("slots"), inv.getSize())) {
                            inv.setItem(sIdx, is.clone());
                        }
                    }
                }
            }
        }

        player.openInventory(inv);
    }

    private boolean isToggle(String key) {
        return List.of("chat", "msg", "bossbar", "kitOnDeath", "tpaInvites", "tpaAuto", "mobSpawn", "nightVision").contains(key);
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
        Material mat = Material.valueOf(sec.getString("material", fallback.name()).toUpperCase());
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(FontUtils.parse(sec.getString("name", "&6&lNASTAVENÍ")));
            String status = state ? "&aZapnuto" : "&cVypnuto";
            List<Component> lore = new ArrayList<>();
            for (String line : sec.getStringList("lore")) {
                lore.add(FontUtils.parse(line.replace("%status%", status)));
            }
            meta.lore(lore);
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
            default -> {
                FileConfiguration config = plugin.getModuleManager().getModuleConfig("settings");
                String cmd = config.getString("gui.items." + key + ".command");
                if (cmd != null) {
                    if (cmd.startsWith("[player] ")) player.performCommand(cmd.substring(9));
                    else if (cmd.startsWith("[console] ")) Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.substring(10).replace("%player%", player.getName()));
                    else player.performCommand(cmd);
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
