package me.jules.apexbuild;

import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;

public class WorldManager {

    private static final long COOLDOWN_TIME = 5 * 60 * 1000; // 5 minut v ms

    public static void createWorld(ApexBuild plugin, Player player) {
        String worldName = "build_" + player.getName();
        MVWorldManager wm = plugin.getMultiverseCore().getMVWorldManager();

        if (wm.isMVWorld(worldName)) {
            player.sendMessage(Component.text("Tvůj svět již existuje! /build -> Seznam světů.", NamedTextColor.RED));
            return;
        }

        // Kontrola cooldownu
        long now = System.currentTimeMillis();
        if (plugin.getCooldowns().containsKey(player.getUniqueId())) {
            long lastUsed = plugin.getCooldowns().get(player.getUniqueId());
            if (now - lastUsed < COOLDOWN_TIME) {
                long remaining = (COOLDOWN_TIME - (now - lastUsed)) / 1000;
                player.sendMessage(Component.text("Musíš počkat ještě " + remaining + " sekund před vytvořením dalšího světa.", NamedTextColor.RED));
                return;
            }
        }

        player.sendMessage(Component.text("Vytvářím tvůj svět... To může chvíli trvat.", NamedTextColor.GREEN));

        // addWorld(String name, World.Environment env, String seed, WorldType type, Boolean generateStructures, String generator)
        boolean success = wm.addWorld(worldName, World.Environment.NORMAL, null, WorldType.FLAT, true, null);

        if (success) {
            plugin.getCooldowns().put(player.getUniqueId(), now);
            player.sendMessage(Component.text("Tvůj svět ", NamedTextColor.GREEN)
                .append(Component.text(worldName, NamedTextColor.WHITE))
                .append(Component.text(" byl úspěšně vytvořen!", NamedTextColor.GREEN)));
            teleportToWorld(plugin, player, worldName);
        } else {
            player.sendMessage(Component.text("Chyba při vytváření světa přes Multiverse-Core.", NamedTextColor.RED));
        }
    }

    public static void teleportToWorld(ApexBuild plugin, Player player, String worldName) {
        MultiverseWorld mvWorld = plugin.getMultiverseCore().getMVWorldManager().getMVWorld(worldName);
        if (mvWorld != null) {
            player.teleport(mvWorld.getCBWorld().getSpawnLocation());
            player.sendMessage(Component.text("Byl jsi teleportován do světa ", NamedTextColor.GREEN)
                .append(Component.text(worldName, NamedTextColor.WHITE))
                .append(Component.text(".", NamedTextColor.GREEN)));
        } else {
            player.sendMessage(Component.text("Tento svět neexistuje.", NamedTextColor.RED));
        }
    }

    public static void deleteWorld(ApexBuild plugin, Player player) {
        String worldName = "build_" + player.getName();
        MVWorldManager wm = plugin.getMultiverseCore().getMVWorldManager();

        if (!wm.isMVWorld(worldName)) {
            player.sendMessage(Component.text("Nemáš žádný svět k smazání.", NamedTextColor.RED));
            return;
        }

        player.sendMessage(Component.text("Mažu tvůj svět ", NamedTextColor.YELLOW)
            .append(Component.text(worldName, NamedTextColor.WHITE))
            .append(Component.text("...", NamedTextColor.YELLOW)));

        // Před smazáním teleportuj hráče pryč, pokud tam je
        World world = Bukkit.getWorld(worldName);
        if (world != null) {
            for (Player p : world.getPlayers()) {
                p.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
                p.sendMessage(Component.text("Svět, ve kterém jsi byl, byl smazán. Byl jsi teleportován na spawn.", NamedTextColor.YELLOW));
            }
        }

        boolean success = wm.deleteWorld(worldName, true, true);
        if (success) {
            player.sendMessage(Component.text("Tvůj svět byl úspěšně smazán.", NamedTextColor.GREEN));
        } else {
            player.sendMessage(Component.text("Chyba při mazání světa.", NamedTextColor.RED));
        }
    }
}
