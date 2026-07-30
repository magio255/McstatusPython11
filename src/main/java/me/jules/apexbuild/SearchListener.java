package me.jules.apexbuild;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SearchListener implements Listener {
    private final DataManager dataManager;
    private final Set<UUID> searchingPlayers = new HashSet<>();

    public SearchListener(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof HistoryGUI gui)) return;
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();

        if (slot == 45 && event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.ARROW) {
            player.openInventory(new HistoryGUI(dataManager, gui.getPage() - 1, gui.getFilter()).getInventory());
        } else if (slot == 53 && event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.ARROW) {
            player.openInventory(new HistoryGUI(dataManager, gui.getPage() + 1, gui.getFilter()).getInventory());
        } else if (slot == 49) {
            if (event.isRightClick()) {
                player.openInventory(new HistoryGUI(dataManager, 0, null).getInventory());
            } else {
                searchingPlayers.add(player.getUniqueId());
                player.closeInventory();
                player.sendMessage(Component.text("Napiš jméno hráče do chatu pro vyhledávání:", NamedTextColor.YELLOW));
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        if (searchingPlayers.contains(player.getUniqueId())) {
            event.setCancelled(true);
            searchingPlayers.remove(player.getUniqueId());

            String query = PlainTextComponentSerializer.plainText().serialize(event.message());

            // Re-open GUI on the main thread
            Bukkit.getScheduler().runTask(dataManager.getPlugin(), () -> {
                player.openInventory(new HistoryGUI(dataManager, 0, query).getInventory());
            });
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        searchingPlayers.remove(event.getPlayer().getUniqueId());
    }

    // This is needed because DataManager is passed as is, but we might need the plugin instance for scheduling
    // I will add a getPlugin() to DataManager
}
