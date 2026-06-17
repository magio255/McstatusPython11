package me.jules.magiocore.modules;

import me.jules.magiocore.FontUtils;
import me.jules.magiocore.MagioCore;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class KeyAllModule implements CommandExecutor {
    private final MagioCore plugin;

    public KeyAllModule(MagioCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("admin.keyall")) {
            sender.sendMessage(FontUtils.parse("§c" + "ɴᴀ ᴛᴏʜʟᴇ ɴᴇᴍáš ᴘʀáᴠᴀ!"));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(FontUtils.parse("&#888888" + "ᴘᴏᴜžɪᴛí: &f/ᴋᴇʏᴀʟʟ <ᴛʏᴘ>"));
            return true;
        }

        String type = args[0].toLowerCase();
        FileConfiguration config = plugin.getModuleManager().getModuleConfig("keyall");
        ConfigurationSection typeSection = config.getConfigurationSection("types." + type);

        if (typeSection == null) {
            sender.sendMessage(FontUtils.parse("§c" + "ᴛᴇɴᴛᴏ ᴛʏᴘ ɴᴇᴇxɪsᴛᴜᴊᴇ."));
            return true;
        }

        startKeyAll(typeSection);
        return true;
    }

    private void startKeyAll(ConfigurationSection section) {
        String title = section.getString("title", "");
        String subtitle = section.getString("subtitle", "");
        List<String> commands = section.getStringList("commands");
        String rewardMsg = section.getString("reward-message", "");
        String prefix = section.getString("prefix", "KeyAll");

        String mainTitle = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().serialize(FontUtils.parse(title));
        String subTitle = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().serialize(FontUtils.parse(subtitle));

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendTitle(mainTitle, subTitle, 10, 60, 10);
            p.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 60, 1, true, false));
        }

        new BukkitRunnable() {
            int step = 3;
            FileConfiguration config = plugin.getModuleManager().getModuleConfig("keyall");

            @Override
            public void run() {
                if (step > 0) {
                    String countdownFormat = config.getString("messages.countdown", "&#888888[&6&lᴋᴇʏᴀʟʟ&r&#888888] §7%type% ᴋᴇʏᴀʟʟ ᴢᴀ &#f1c40f%time%...");
                    Bukkit.broadcast(FontUtils.parse(countdownFormat.replace("%type%", prefix).replace("%time%", String.valueOf(step))));

                    Particle p;
                    try {
                        p = Particle.valueOf(section.getString("particles.step-" + step, "FLAME"));
                    } catch (Exception e) { p = Particle.FLAME; }

                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.getWorld().spawnParticle(p, player.getLocation().add(0, 1, 0), 100, 0.5, 0.5, 0.5, 0.1);
                    }
                    step--;
                } else {
                    for (String cmd : commands) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                    }
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, player.getLocation().add(0, 1, 0), 1, 0, 0, 0, 0);
                    }

                    String finishFormat = config.getString("messages.finished", "&#888888[&6&lᴋᴇʏᴀʟʟ&r&#888888] &#00ff44&lʙᴜᴍ! %reward%");
                    Bukkit.broadcast(FontUtils.parse(finishFormat.replace("%reward%", rewardMsg)));
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }
}
