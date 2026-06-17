package me.jules.magiocore;

import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathListener implements Listener {

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        MagioCore plugin = MagioCore.getPlugin(MagioCore.class);
        FileConfiguration config = plugin.getModuleManager().getModuleConfig("deathsystem");

        // Dynamic format from config
        String format = config.getString("format", "&#FF1010☠ §f%player% umrel");
        Component message = FontUtils.parse(format.replace("%player%", victim.getName()));
        event.deathMessage(message);

        // Module logic for titles
        if (plugin.getModuleManager().isEnabled("deathsystem")) {
            String dagger = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection()
                    .serialize(FontUtils.parse(config.getString("titles.victim.title", "&#ff0000🗡")));

            String killerName = (killer != null ? killer.getName() : "ᴇɴᴠɪʀᴏɴᴍᴇɴᴛ");
            String victimSub = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection()
                    .serialize(FontUtils.parse(config.getString("titles.victim.subtitle", "§fʏᴏᴜ ʜᴀᴠᴇ ʙᴇᴇɴ ᴋɪʟʟᴇᴅ ʙʏ %killer%").replace("%killer%", killerName)));

            victim.sendTitle(dagger, victimSub, 10, 40, 10);

            if (killer != null) {
                String kTitle = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection()
                        .serialize(FontUtils.parse(config.getString("titles.killer.title", "&#ff0000🗡")));
                String killerSub = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection()
                        .serialize(FontUtils.parse(config.getString("titles.killer.subtitle", "§fʏᴏᴜ ʜᴀᴠᴇ ᴋɪʟʟᴇᴅ %victim%").replace("%victim%", victim.getName())));
                killer.sendTitle(kTitle, killerSub, 10, 40, 10);
            }
        }
    }
}
