package me.jules.apexbuild;

import com.onarandombox.MultiverseCore.MultiverseCore;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

public class ApexBuild extends JavaPlugin {

    private MultiverseCore multiverseCore;
    private final HashMap<UUID, Long> cooldowns = new HashMap<>();
    private BuildGUI buildGUI;

    @Override
    public void onEnable() {
        getComponentLogger().info(Component.text("Zapínám Apex-Build..."));

        // Kontrola Multiverse-Core
        if (Bukkit.getPluginManager().getPlugin("Multiverse-Core") instanceof MultiverseCore core) {
            this.multiverseCore = core;
            getComponentLogger().info(Component.text("Multiverse-Core byl úspěšně nalezen."));
        } else {
            getComponentLogger().error(Component.text("Multiverse-Core nebyl nalezen! Plugin se vypíná."));
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        this.buildGUI = new BuildGUI(this);

        // Registrace příkazů
        if (getCommand("build") != null) {
            getCommand("build").setExecutor(new BuildCommand(this));
            getComponentLogger().info(Component.text("Příkaz /build byl registrován."));
        } else {
            getComponentLogger().error(Component.text("Příkaz /build se nepodařilo najít v plugin.yml!"));
        }

        // Registrace eventů
        Bukkit.getPluginManager().registerEvents(buildGUI, this);

        getComponentLogger().info(Component.text("Apex-Build byl úspěšně zapnut."));
    }

    @Override
    public void onDisable() {
        getComponentLogger().info(Component.text("Apex-Build byl vypnut."));
    }

    public MultiverseCore getMultiverseCore() {
        return multiverseCore;
    }

    public HashMap<UUID, Long> getCooldowns() {
        return cooldowns;
    }

    public BuildGUI getBuildGUI() {
        return buildGUI;
    }
}
