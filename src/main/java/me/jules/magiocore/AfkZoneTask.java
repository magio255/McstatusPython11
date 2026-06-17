package me.jules.magiocore;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class AfkZoneTask extends BukkitRunnable {
    private final MagioCore plugin;
    private int secondsRemaining = 60;

    public AfkZoneTask(MagioCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        secondsRemaining--;

        FileConfiguration config = plugin.getModuleManager().getModuleConfig("afkzone");
        String regionName = config.getString("region-name", "afk");
        String command = config.getString("reward-command");
        String actionbarFormat = config.getString("messages.actionbar", "&#00fbffᴏᴅᴍěɴᴀ ᴢᴀ: %time%s");

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isInRegion(player, regionName)) {
                // Show action bar
                if (plugin.getSettingsManager().getSettings(player.getUniqueId()).bossbar()) {
                    player.sendActionBar(FontUtils.parse(actionbarFormat.replace("%time%", String.valueOf(secondsRemaining))));
                }

                if (secondsRemaining <= 0 && command != null && !command.isEmpty()) {
                    String finalCmd = command.replace("%player%", player.getName());
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCmd);
                }
            }
        }

        if (secondsRemaining <= 0) {
            secondsRemaining = 60;
        }
    }

    private boolean isInRegion(Player player, String regionName) {
        if (Bukkit.getPluginManager().getPlugin("WorldGuard") == null) return false;

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(player.getLocation()));

        for (ProtectedRegion region : set) {
            if (region.getId().equalsIgnoreCase(regionName)) {
                return true;
            }
        }
        return false;
    }
}
