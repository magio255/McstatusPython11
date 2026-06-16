package me.jules.vipmaker;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RankSelectionGUI implements InventoryHolder {

    private final VipMaker plugin;
    private final Inventory inventory;
    private final List<RankOption> rankOptions = new ArrayList<>();

    public RankSelectionGUI(VipMaker plugin) {
        this.plugin = plugin;
        String title = plugin.getConfig().getString("messages.gui_title", "Vyber VIP a délku");
        this.inventory = Bukkit.createInventory(this, 54, LegacyComponentSerializer.legacyAmpersand().deserialize(title));
        loadRanks();
    }

    private void loadRanks() {
        List<Map<?, ?>> configRanks = plugin.getConfig().getMapList("available_ranks");
        int slot = 0;

        for (Map<?, ?> rankMap : configRanks) {
            String name = (String) rankMap.get("name");
            String group = (String) rankMap.get("group");
            List<Integer> durations = (List<Integer>) rankMap.get("durations");

            for (int days : durations) {
                if (slot >= 54) break;

                ItemStack item = new ItemStack(Material.GOLD_BLOCK);
                ItemMeta meta = item.getItemMeta();
                meta.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize("&e" + name + " &7(" + days + " dní)"));

                List<Component> lore = new ArrayList<>();
                lore.add(LegacyComponentSerializer.legacyAmpersand().deserialize("&7Klikni pro výběr"));
                meta.lore(lore);

                item.setItemMeta(meta);
                inventory.setItem(slot, item);

                rankOptions.add(new RankOption(slot, name, group, days));
                slot++;
            }
        }
    }

    public RankOption getOption(int slot) {
        return rankOptions.stream()
                .filter(opt -> opt.slot() == slot)
                .findFirst()
                .orElse(null);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public record RankOption(int slot, String name, String group, int days) {}
}
