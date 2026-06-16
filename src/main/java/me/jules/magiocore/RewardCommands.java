package me.jules.magiocore;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class RewardCommands implements CommandExecutor {
    private final DailyRewardGui dailyRewardGui;
    private final PlaytimeRewardGui playtimeRewardGui;

    public RewardCommands(DailyRewardGui dailyRewardGui, PlaytimeRewardGui playtimeRewardGui) {
        this.dailyRewardGui = dailyRewardGui;
        this.playtimeRewardGui = playtimeRewardGui;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (command.getName().equalsIgnoreCase("dailyrewards")) {
            dailyRewardGui.open(player);
        } else if (command.getName().equalsIgnoreCase("playtimerewards")) {
            playtimeRewardGui.open(player, 0);
        }

        return true;
    }
}
