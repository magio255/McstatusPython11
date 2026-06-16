package me.jules.vipmaker;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VipListener implements Listener {

    private final VipMaker plugin;
    private final DataManager dataManager;
    private final LuckPermsService lpService;
    private final Map<UUID, RankSelectionGUI.RankOption> pendingSelections = new HashMap<>();
    private final Map<UUID, Boolean> waitingForBlock = new HashMap<>();

    public VipListener(VipMaker plugin, DataManager dataManager, LuckPermsService lpService) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.lpService = lpService;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;

        UUID uuid = player.getUniqueId();
        Location loc = event.getClickedBlock().getLocation();

        // 1. Setting up a VIP block
        if (waitingForBlock.getOrDefault(uuid, false)) {
            event.setCancelled(true);
            RankSelectionGUI.RankOption option = pendingSelections.get(uuid);
            if (option == null) {
                waitingForBlock.remove(uuid);
                return;
            }

            dataManager.saveBlock(new DataManager.VipBlock(loc, option.group(), option.days(), option.name()));

            String prefix = plugin.getConfig().getString("messages.prefix", "");
            String msg = plugin.getConfig().getString("messages.block_set_success", "&aVIP blok byl úspěšně vytvořen!");
            player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + msg));

            waitingForBlock.remove(uuid);
            pendingSelections.remove(uuid);
            return;
        }

        // 2. Interacting with an existing VIP block
        DataManager.VipBlock vipBlock = dataManager.getBlock(loc);
        if (vipBlock != null) {
            event.setCancelled(true);
            boolean overwrite = plugin.getConfig().getBoolean("overwrite_existing", true);

            if (!overwrite && lpService.hasGroup(player, vipBlock.group())) {
                String prefix = plugin.getConfig().getString("messages.prefix", "");
                String msg = plugin.getConfig().getString("messages.already_has_vip", "&cJiž máš aktivní VIP! Počkej, až vyprší.");
                player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + msg));
                return;
            }

            lpService.addGroup(player, vipBlock.group(), vipBlock.days()).thenRun(() -> {
                String prefix = plugin.getConfig().getString("messages.prefix", "");
                String msg = plugin.getConfig().getString("messages.vip_activated", "&aAktivoval jsi &e{rank} &ana &e{days} &adní!")
                        .replace("{rank}", vipBlock.name())
                        .replace("{days}", String.valueOf(vipBlock.days()));
                player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + msg));

                if (plugin.getConfig().getBoolean("effects.enabled", true)) {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        String soundName = plugin.getConfig().getString("effects.sound", "ENTITY_PLAYER_LEVELUP");
                        String particleName = plugin.getConfig().getString("effects.particles", "FIREWORKS_SPARK");

                        try {
                            player.playSound(player.getLocation(), Sound.valueOf(soundName), 1.0f, 1.0f);
                            player.spawnParticle(Particle.valueOf(particleName), player.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.1);
                        } catch (IllegalArgumentException e) {
                            plugin.getLogger().warning("Neznámý zvuk nebo částice v configu: " + e.getMessage());
                        }
                    });
                }
            });
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!(event.getInventory().getHolder() instanceof RankSelectionGUI gui)) return;

        event.setCancelled(true);
        int slot = event.getRawSlot();
        RankSelectionGUI.RankOption option = gui.getOption(slot);

        if (option != null) {
            UUID uuid = player.getUniqueId();
            pendingSelections.put(uuid, option);
            waitingForBlock.put(uuid, true);
            player.closeInventory();

            String prefix = plugin.getConfig().getString("messages.prefix", "");
            String msg = plugin.getConfig().getString("messages.setvip_start", "&7Nyní klikni pravým tlačítkem na blok, který chceš nastavit jako VIP.");
            player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + msg));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        pendingSelections.remove(uuid);
        waitingForBlock.remove(uuid);
    }
}
