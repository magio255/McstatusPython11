package me.jules.apexbuild;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class HistoryCommand implements CommandExecutor {
    private final DataManager dataManager;

    public HistoryCommand(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command is only for players.");
            return true;
        }

        if (!player.hasPermission("apexbuild.history")) {
            player.sendMessage("You don't have permission to use this command.");
            return true;
        }

        player.openInventory(new HistoryGUI(dataManager, 0, null).getInventory());
        return true;
    }
}
