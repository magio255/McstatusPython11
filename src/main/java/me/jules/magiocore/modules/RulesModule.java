package me.jules.magiocore.modules;

import me.jules.magiocore.FontUtils;
import me.jules.magiocore.MagioCore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RulesModule implements CommandExecutor {
    private final MagioCore plugin;

    public RulesModule(MagioCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        FileConfiguration config = plugin.getModuleManager().getModuleConfig("rules");
        if (config == null) return true;

        List<String> rules = config.getStringList("rules");
        String header = config.getString("header", "&#EA427F===== ᴘʀᴀᴠɪᴅʟᴀ sᴇʀᴠᴇʀᴜ =====");
        String footer = config.getString("footer", "&#EA427F==========================");

        sender.sendMessage(FontUtils.parse(header));
        for (String rule : rules) {
            sender.sendMessage(FontUtils.parse(rule));
        }
        sender.sendMessage(FontUtils.parse(footer));

        return true;
    }
}
