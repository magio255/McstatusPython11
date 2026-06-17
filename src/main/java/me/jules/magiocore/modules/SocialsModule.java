package me.jules.magiocore.modules;

import me.jules.magiocore.FontUtils;
import me.jules.magiocore.MagioCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SocialsModule implements CommandExecutor {
    private final MagioCore plugin;

    public SocialsModule(MagioCore plugin) {
        this.plugin = plugin;
        startAnnouncementTasks();
    }

    private void startAnnouncementTasks() {
        FileConfiguration config = plugin.getModuleManager().getModuleConfig("socials");

        // Discord Announcement
        if (config.getBoolean("discord.periodic-announcement.enabled", false)) {
            long interval = config.getInt("discord.periodic-announcement.interval", 15) * 1200L;
            new BukkitRunnable() {
                @Override
                public void run() {
                    FileConfiguration c = plugin.getModuleManager().getModuleConfig("socials");
                    broadcast(c.getStringList("discord.periodic-announcement.messages"), c.getString("discord.periodic-announcement.sound"));
                }
            }.runTaskTimer(plugin, interval, interval);
        }

        // Store Announcement
        if (config.getBoolean("store.periodic-announcement.enabled", false)) {
            long interval = config.getInt("store.periodic-announcement.interval", 20) * 1200L;
            new BukkitRunnable() {
                @Override
                public void run() {
                    FileConfiguration c = plugin.getModuleManager().getModuleConfig("socials");
                    broadcast(c.getStringList("store.periodic-announcement.messages"), c.getString("store.periodic-announcement.sound"));
                }
            }.runTaskTimer(plugin, interval, interval);
        }
    }

    private void broadcast(List<String> messages, String soundName) {
        for (String msg : messages) {
            if (msg.isEmpty()) Bukkit.broadcast(Component.empty());
            else Bukkit.broadcast(FontUtils.parse(msg));
        }
        if (soundName != null && !soundName.isEmpty()) {
            try {
                Sound sound = Sound.valueOf(soundName);
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.playSound(player.getLocation(), sound, 1f, 1f);
                }
            } catch (Exception ignored) {}
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        FileConfiguration config = plugin.getModuleManager().getModuleConfig("socials");
        String type = command.getName().equalsIgnoreCase("discord") ? "discord" : "store";

        String prefix = config.getString(type + ".prefix");
        String message = config.getString(type + ".message");
        String link = config.getString(type + ".link");

        sender.sendMessage(FontUtils.parse(prefix));
        sender.sendMessage("");
        sender.sendMessage(FontUtils.parse(message));

        Component linkComp = FontUtils.parse("&#888888⤷ &n" + link, false)
                .clickEvent(ClickEvent.openUrl(link));
        sender.sendMessage(linkComp);
        return true;
    }
}
