package me.jules.magiocore;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathListener implements Listener {

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer != null) {
            // Killed by player
            // » ☠ victim, was killed by ⚔ killer
            Component message = FontUtils.parse("&#888888» &#ff0000☠ &#ff0000" + victim.getName() + "§7, was killed by &#ff0000⚔ &#00fbff" + killer.getName());
            event.deathMessage(message);
        } else {
            // Normal death
            // » ☠ victim has died
            Component message = FontUtils.parse("&#888888» &#ff0000☠ &#ff0000" + victim.getName() + " §7has died");
            event.deathMessage(message);
        }
    }
}
