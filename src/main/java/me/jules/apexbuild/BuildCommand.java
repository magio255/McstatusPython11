package me.jules.apexbuild;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BuildCommand implements CommandExecutor {

    private final ApexBuild plugin;

    public BuildCommand(ApexBuild plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        try {
            plugin.getComponentLogger().info(Component.text("Příkaz /build byl spuštěn hráčem: " + sender.getName()));

            if (!(sender instanceof Player player)) {
                sender.sendMessage(Component.text("Tento příkaz může použít pouze hráč.", NamedTextColor.RED));
                return true;
            }

            if (!player.hasPermission("build.use")) {
                player.sendMessage(Component.text("Nemáš oprávnění k použití tohoto příkazu (build.use).", NamedTextColor.RED));
                plugin.getComponentLogger().info(Component.text("Hráč " + player.getName() + " nemá oprávnění build.use"));
                return true;
            }

            plugin.getComponentLogger().info(Component.text("Otevírám GUI pro hráče " + player.getName()));
            plugin.getBuildGUI().openGUI(player);
            return true;

        } catch (Exception e) {
            plugin.getComponentLogger().error(Component.text("Chyba při vykonávání příkazu /build: " + e.getMessage()));
            e.printStackTrace();
            sender.sendMessage(Component.text("Došlo k interní chybě při otevírání menu. Zkontroluj konzoli.", NamedTextColor.RED));
            return true;
        }
    }
}
