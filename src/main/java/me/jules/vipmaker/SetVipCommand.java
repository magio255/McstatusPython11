package me.jules.vipmaker;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SetVipCommand implements CommandExecutor {

    private final VipMaker plugin;

    public SetVipCommand(VipMaker plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Tento příkaz může použít pouze hráč.");
            return true;
        }

        if (!player.hasPermission("vipmaker.setvip")) {
            String prefix = plugin.getConfig().getString("messages.prefix", "");
            String msg = plugin.getConfig().getString("messages.no_permission", "&cNa tohle nemáš práva!");
            player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + msg));
            return true;
        }

        RankSelectionGUI gui = new RankSelectionGUI(plugin);
        player.openInventory(gui.getInventory());
        return true;
    }
}
