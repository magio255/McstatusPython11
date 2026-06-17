package me.jules.magiocore;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CoinflipCommand implements CommandExecutor, TabCompleter {
    private final MagioCore plugin;
    private final CoinflipManager manager;

    public CoinflipCommand(MagioCore plugin, CoinflipManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;

        FileConfiguration config = plugin.getModuleManager().getModuleConfig("coinflip");
        if (args.length > 0) {
            try {
                double amount = Double.parseDouble(args[0]);
                if (amount <= 0) {
                    player.sendMessage(FontUtils.parse(config.getString("messages.bet-positive", "§csázᴋᴀ ᴍᴜsí ʙýᴛ ᴋʟᴀᴅɴá")));
                    return true;
                }

                if (plugin.getEconomy().getBalance(player) < amount) {
                    player.sendMessage(FontUtils.parse(config.getString("messages.no-money", "§cɴᴇᴍáš ᴅᴏsᴛᴀᴛᴇᴋ ᴘᴇɴěᴢ")));
                    return true;
                }

                plugin.getEconomy().withdrawPlayer(player, amount);
                manager.addBet(player, amount);
                player.sendMessage(FontUtils.parse(config.getString("messages.bet-created", "&#00ff44ᴠʏᴛᴠᴏřɪʟ ᴊsɪ ᴄᴏɪɴꜰɪʟᴘ ᴏ §f%amount% $").replace("%amount%", String.valueOf(amount))));
            } catch (NumberFormatException e) {
                player.sendMessage(FontUtils.parse(config.getString("messages.usage", "§cᴘᴏᴜžɪᴛí: /ᴄꜰ <čásᴛᴋᴀ>")));
            }
            return true;
        }

        plugin.getCoinflipGui().open(player);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("100", "500", "1000", "3000", "8000");
        }
        return new ArrayList<>();
    }
}
