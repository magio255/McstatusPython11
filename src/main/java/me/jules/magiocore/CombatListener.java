package me.jules.magiocore;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class CombatListener implements Listener {
    private final MagioCore plugin;

    public CombatListener(MagioCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onKill(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer != null) {
            Location spawn = plugin.getConfig().getLocation("spawn");
            if (spawn != null) {
                killer.teleport(spawn);
                killer.sendMessage(FontUtils.parse("&#00fbff" + "ZABIL JsI HRáčE " + victim.getName() + " A BYL JsI TELEPORTOVáN NA sPAWN."));
            }
        }
    }
}
