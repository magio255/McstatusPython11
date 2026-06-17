package me.jules.magiocore;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class VirtualSpawnerCommands implements CommandExecutor, TabCompleter {
    private final MagioCore plugin;
    private final VirtualSpawnerManager manager;

    public VirtualSpawnerCommands(MagioCore plugin, VirtualSpawnerManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("magiocore.virtualspawner")) {
            sender.sendMessage(FontUtils.parse("§c" + "ɴᴇᴍáš ᴘřísᴛᴜᴘ ᴋ ᴛᴏᴍᴜᴛᴏ ᴘříᴋᴀᴢᴜ."));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(FontUtils.parse("&#00fbff" + "ᴘᴏᴜžɪᴛí: /ss <ʟɪsᴛ|ɢɪᴠᴇ|ꜰɪx>"));
            return true;
        }

        String sub = args[0].toLowerCase();
        if (sub.equals("list")) {
            if (!(sender instanceof Player player)) return true;
            plugin.getSpawnerListener().openAdminGui(player, 0);
        } else if (sub.equals("fix")) {
            if (!(sender instanceof Player player)) return true;
            int count = manager.forceCleanup(player);
            player.sendMessage(FontUtils.parse("&#00fbff" + "ᴠʏčɪšᴛěɴᴏ &#ffbb00" + count + " §7ᴏsɪřᴇʟýᴄʜ ʜᴏʟᴏɢʀᴀᴍů."));
        } else if (sub.equals("give")) {
            if (args.length < 3) {
                sender.sendMessage(FontUtils.parse("§c" + "ᴘᴏᴜžɪᴛí: /ss ɢɪᴠᴇ <ʜʀáč|@a> <ᴛʏᴘᴇ> [ᴍɴᴏžsᴛᴠí]"));
                return true;
            }

            String targetName = args[1];
            List<Player> targets = new ArrayList<>();
            if (targetName.equalsIgnoreCase("@a")) {
                targets.addAll(Bukkit.getOnlinePlayers());
            } else {
                Player target = Bukkit.getPlayer(targetName);
                if (target == null) {
                    sender.sendMessage(FontUtils.parse("§c" + "ʜʀáč ɴᴇɴí ᴏɴʟɪɴᴇ."));
                    return true;
                }
                targets.add(target);
            }

            try {
                EntityType type = EntityType.valueOf(args[2].toUpperCase());
                int amount = args.length > 3 ? Integer.parseInt(args[3]) : 1;

                ItemStack spawner = new ItemStack(Material.SPAWNER, amount);
                ItemMeta meta = spawner.getItemMeta();

                meta.displayName(FontUtils.parse("&#00fbff&lᴠɪʀᴛᴜáʟɴí sᴘᴀᴡɴᴇʀ"));
                meta.lore(Arrays.asList(
                    FontUtils.parse("§7ᴛʏᴘ: &#00fbff" + type.name()),
                    FontUtils.parse(""),
                    FontUtils.parse("&#00fbff» §7ᴘᴏʟᴏž ᴘʀᴏ ᴠʏᴛᴠᴏřᴇɴí sᴘᴀᴡɴᴇʀᴜ"),
                    FontUtils.parse("&#00fbff» §7ᴋʟɪᴋɴɪ sᴛᴇᴊɴýᴍ ᴛʏᴘᴇᴍ ᴘʀᴏ sᴛᴀᴄᴋᴏᴠáɴí"),
                    FontUtils.parse(""),
                    FontUtils.parse("&#FCD05Cᴅɪsᴘʟᴀʏ &#4498DBꜱᴇʀᴠᴇʀ ꜱʏꜱᴛᴇᴍ")
                ));

                meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "virtual_spawner"), PersistentDataType.STRING, type.name());
                spawner.setItemMeta(meta);

                for (Player target : targets) {
                    target.getInventory().addItem(spawner.clone());
                    target.sendMessage(FontUtils.parse("&#00fbff" + "ᴅᴏsᴛᴀʟ ᴊsɪ " + amount + "x ᴠɪʀᴛᴜáʟɴí sᴘᴀᴡɴᴇʀ " + type.name() + "."));
                }

                if (targetName.equalsIgnoreCase("@a")) {
                    sender.sendMessage(FontUtils.parse("&#00fbff" + "ᴅᴀʟ ᴊsɪ " + amount + "x ᴠɪʀᴛᴜáʟɴí sᴘᴀᴡɴᴇʀ " + type.name() + " ᴠšᴇᴍ ʜʀáčůᴍ."));
                } else {
                    sender.sendMessage(FontUtils.parse("&#00fbff" + "ᴅᴀʟ ᴊsɪ " + amount + "x ᴠɪʀᴛᴜáʟɴí sᴘᴀᴡɴᴇʀ " + type.name() + " ʜʀáčɪ " + targets.get(0).getName() + "."));
                }
            } catch (Exception e) {
                sender.sendMessage(FontUtils.parse("§c" + "ɴᴇᴘʟᴀᴛɴý ᴛʏᴘ ᴍᴏʙᴀ ɴᴇʙᴏ ᴍɴᴏžsᴛᴠí."));
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return Arrays.asList("list", "give", "fix").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            List<String> players = Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toList());
            players.add("@a");
            return players.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            return Arrays.stream(EntityType.values())
                    .map(EntityType::name)
                    .map(String::toLowerCase)
                    .filter(s -> s.startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 4 && args[0].equalsIgnoreCase("give")) {
            return Arrays.asList("1", "10", "64").stream()
                    .filter(s -> s.startsWith(args[3]))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
