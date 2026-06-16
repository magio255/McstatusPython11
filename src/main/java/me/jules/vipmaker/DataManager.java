package me.jules.vipmaker;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class DataManager {

    private final VipMaker plugin;
    private final Map<Location, VipBlock> blocks = new HashMap<>();

    public DataManager(VipMaker plugin) {
        this.plugin = plugin;
    }

    public void loadBlocks() {
        blocks.clear();
        FileConfiguration config = plugin.getConfig();
        List<Map<?, ?>> blockList = config.getMapList("vip_blocks");

        for (Map<?, ?> blockMap : blockList) {
            String worldName = (String) blockMap.get("world");
            World world = Bukkit.getWorld(worldName);
            if (world == null) continue;

            int x = (int) blockMap.get("x");
            int y = (int) blockMap.get("y");
            int z = (int) blockMap.get("z");
            Location loc = new Location(world, x, y, z);

            String group = (String) blockMap.get("group");
            int days = (int) blockMap.get("days");
            String name = (String) blockMap.get("name");

            blocks.put(loc, new VipBlock(loc, group, days, name));
        }
    }

    public void saveBlock(VipBlock block) {
        blocks.put(block.location(), block);
        updateConfig();
    }

    private void updateConfig() {
        List<Map<String, Object>> blockList = new ArrayList<>();
        for (VipBlock block : blocks.values()) {
            Map<String, Object> map = new HashMap<>();
            map.put("world", block.location().getWorld().getName());
            map.put("x", block.location().getBlockX());
            map.put("y", block.location().getBlockY());
            map.put("z", block.location().getBlockZ());
            map.put("group", block.group());
            map.put("days", block.days());
            map.put("name", block.name());
            blockList.add(map);
        }
        plugin.getConfig().set("vip_blocks", blockList);
        plugin.saveConfig();
    }

    public VipBlock getBlock(Location loc) {
        // Just block location, not precise double coordinates
        Location blockLoc = loc.getBlock().getLocation();
        return blocks.get(blockLoc);
    }

    public record VipBlock(Location location, String group, int days, String name) {}
}
