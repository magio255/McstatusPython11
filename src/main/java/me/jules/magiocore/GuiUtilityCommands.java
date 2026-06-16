package me.jules.magiocore;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiUtilityCommands implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;

        String cmd = command.getName().toLowerCase();

        switch (cmd) {
            case "anvil" -> {
                if (!player.hasPermission("magiocore.anvil")) {
                    player.sendMessage(FontUtils.parse("§c" + "ɴᴇᴍáš ᴘřísᴛᴜᴘ ᴋ ᴛᴏᴍᴜᴛᴏ ᴘříᴋᴀᴢᴜ."));
                    return true;
                }
                player.openAnvil(null, true);
            }
            case "disposal" -> {
                if (!player.hasPermission("magiocore.disposal")) {
                    player.sendMessage(FontUtils.parse("§c" + "ɴᴇᴍáš ᴘřísᴛᴜᴘ ᴋ ᴛᴏᴍᴜᴛᴏ ᴘříᴋᴀᴢᴜ."));
                    return true;
                }
                Inventory inv = Bukkit.createInventory(null, 54, FontUtils.parse("&#EA427F" + "ᴏᴅᴘᴀᴅᴋᴏᴠý ᴋᴏš"));
                player.openInventory(inv);
            }
            case "grindstone" -> {
                if (!player.hasPermission("magiocore.grindstone")) {
                    player.sendMessage(FontUtils.parse("§c" + "ɴᴇᴍáš ᴘřísᴛᴜᴘ ᴋ ᴛᴏᴍᴜᴛᴏ ᴘříᴋᴀᴢᴜ."));
                    return true;
                }
                player.openGrindstone(null, true);
            }
            case "loom" -> {
                if (!player.hasPermission("magiocore.loom")) {
                    player.sendMessage(FontUtils.parse("§c" + "ɴᴇᴍáš ᴘřísᴛᴜᴘ ᴋ ᴛᴏᴍᴜᴛᴏ ᴘříᴋᴀᴢᴜ."));
                    return true;
                }
                player.openLoom(null, true);
            }
            case "smithingtable" -> {
                if (!player.hasPermission("magiocore.smithingtable")) {
                    player.sendMessage(FontUtils.parse("§c" + "ɴᴇᴍáš ᴘřísᴛᴜᴘ ᴋ ᴛᴏᴍᴜᴛᴏ ᴘříᴋᴀᴢᴜ."));
                    return true;
                }
                player.openSmithingTable(null, true);
            }
            case "workbench" -> {
                if (!player.hasPermission("magiocore.workbench")) {
                    player.sendMessage(FontUtils.parse("§c" + "ɴᴇᴍáš ᴘřísᴛᴜᴘ ᴋ ᴛᴏᴍᴜᴛᴏ ᴘříᴋᴀᴢᴜ."));
                    return true;
                }
                player.openWorkbench(null, true);
            }
        }

        return true;
    }
}
