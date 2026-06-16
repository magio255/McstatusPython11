package me.jules.magiocore;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TropicalFish;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ItemEditCommand implements CommandExecutor, TabCompleter {

    private final List<String> subcommands = Arrays.asList(
            "rename", "lore", "enchant", "hide", "unhide", "hideall", "unbreakable", "repaircost",
            "amount", "durability", "skullowner", "custommodeldata", "type", "leathercolor",
            "potioncolor", "bookauthor", "fireworkpower", "potioneffect", "attribute",
            "banner", "booktype", "tropicalfish", "compass", "spawnereggtype", "listaliases"
    );

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (args.length == 0) {
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item == null || item.getType().isAir()) {
                player.sendMessage(FontUtils.parse("§c" + "ᴍᴜsíš ᴅʀžᴇᴛ ᴘřᴇᴅᴍěᴛ ᴠ ʀᴜᴄᴇ."));
                return true;
            }
            player.openInventory(new ItemEditGui().getInventory());
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            player.sendMessage(FontUtils.parse("§c" + "ᴍᴜsíš ᴅʀžᴇᴛ ᴘřᴇᴅᴍěᴛ ᴠ ʀᴜᴄᴇ."));
            return true;
        }

        String sub = args[0].toLowerCase();
        if (!player.hasPermission("itemedit.itemedit." + sub)) {
            player.sendMessage(FontUtils.parse("§c" + "ɴᴇᴍáš ᴘřísᴛᴜᴘ ᴋ ᴛᴏᴍᴜᴛᴏ ᴘříᴋᴀᴢᴜ."));
            return true;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return true;

        try {
            switch (sub) {
                case "rename" -> {
                    if (args.length < 2) return false;
                    meta.displayName(FontUtils.parse(String.join(" ", Arrays.copyOfRange(args, 1, args.length))));
                }
                case "lore" -> {
                    if (args.length < 2) return false;
                    List<Component> lore = meta.lore();
                    if (lore == null) lore = new ArrayList<>();
                    String action = args[1].toLowerCase();
                    String text = args.length > 2 ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)) : "";
                    switch (action) {
                        case "add" -> lore.add(FontUtils.parse(text));
                        case "set" -> {
                            lore.clear();
                            lore.add(FontUtils.parse(text));
                        }
                        case "remove" -> {
                            if (!lore.isEmpty()) lore.remove(lore.size() - 1);
                        }
                        case "reset" -> lore.clear();
                    }
                    meta.lore(lore);
                }
                case "enchant" -> {
                    if (args.length < 2) return false;
                    Enchantment ench = Enchantment.getByKey(NamespacedKey.minecraft(args[1].toLowerCase()));
                    if (ench == null) {
                        player.sendMessage(FontUtils.parse("§c" + "ɴᴇᴘʟᴀᴛɴý ᴇɴᴄʜᴀɴᴛ."));
                        return true;
                    }
                    int level = args.length > 2 ? Integer.parseInt(args[2]) : 1;
                    if (level <= 0) meta.removeEnchant(ench);
                    else meta.addEnchant(ench, level, true);
                }
                case "hide" -> {
                    if (args.length < 2) return false;
                    ItemFlag flag = ItemFlag.valueOf(args[1].toUpperCase());
                    boolean bool = args.length <= 2 || Boolean.parseBoolean(args[2]);
                    if (bool) meta.addItemFlags(flag);
                    else meta.removeItemFlags(flag);
                }
                case "hideall" -> meta.addItemFlags(ItemFlag.values());
                case "unbreakable" -> {
                    boolean bool = args.length <= 1 || Boolean.parseBoolean(args[1]);
                    meta.setUnbreakable(bool);
                }
                case "repaircost" -> {
                    if (args.length < 2) return false;
                    if (meta instanceof Repairable r) r.setRepairCost(Integer.parseInt(args[1]));
                }
                case "amount" -> {
                    if (args.length < 2) return false;
                    item.setAmount(Integer.parseInt(args[1]));
                }
                case "durability" -> {
                    if (args.length < 2) return false;
                    if (meta instanceof Damageable d) d.setDamage(item.getType().getMaxDurability() - Integer.parseInt(args[1]));
                }
                case "skullowner" -> {
                    if (args.length < 2) return false;
                    if (meta instanceof SkullMeta s) s.setOwningPlayer(Bukkit.getOfflinePlayer(args[1]));
                }
                case "custommodeldata" -> {
                    if (args.length < 2) return false;
                    meta.setCustomModelData(Integer.parseInt(args[1]));
                }
                case "type" -> {
                    if (args.length < 2) return false;
                    Material mat = Material.matchMaterial(args[1].toUpperCase());
                    if (mat != null) item.setType(mat);
                }
                case "leathercolor", "potioncolor" -> {
                    if (args.length < 4) return false;
                    Color color = Color.fromRGB(Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]));
                    if (meta instanceof LeatherArmorMeta l) l.setColor(color);
                    else if (meta instanceof PotionMeta p) p.setColor(color);
                }
                case "bookauthor" -> {
                    if (args.length < 2) return false;
                    if (meta instanceof BookMeta b) b.setAuthor(args[1]);
                }
                case "unhide" -> {
                     if (args.length < 2) return false;
                     meta.removeItemFlags(ItemFlag.valueOf(args[1].toUpperCase()));
                }
                case "fireworkpower" -> {
                    if (args.length < 2) return false;
                    if (meta instanceof FireworkMeta f) f.setPower(Integer.parseInt(args[1]));
                }
                case "potioneffect" -> {
                    if (args.length < 2 || !(meta instanceof PotionMeta p)) return false;
                    String action = args[1].toLowerCase();
                    if (action.equals("add")) {
                        if (args.length < 5) return false;
                        PotionEffectType type = PotionEffectType.getByName(args[2].toUpperCase());
                        if (type != null) p.addCustomEffect(new PotionEffect(type, Integer.parseInt(args[3]), Integer.parseInt(args[4])), true);
                    } else if (action.equals("remove")) {
                        if (args.length < 3) return false;
                        PotionEffectType type = PotionEffectType.getByName(args[2].toUpperCase());
                        if (type != null) p.removeCustomEffect(type);
                    } else if (action.equals("reset")) {
                        p.clearCustomEffects();
                    }
                }
                case "attribute" -> {
                    if (args.length < 2) return false;
                    String action = args[1].toLowerCase();
                    if (action.equals("add")) {
                        if (args.length < 5) return false;
                        Attribute attr = Attribute.valueOf(args[2].toUpperCase());
                        AttributeModifier mod = new AttributeModifier(NamespacedKey.minecraft(UUID.randomUUID().toString().substring(0, 8)), Double.parseDouble(args[3]), AttributeModifier.Operation.valueOf(args[4].toUpperCase()));
                        meta.addAttributeModifier(attr, mod);
                    } else if (action.equals("remove")) {
                        if (args.length < 3) return false;
                        meta.removeAttributeModifier(Attribute.valueOf(args[2].toUpperCase()));
                    }
                }
                case "banner" -> {
                    if (args.length < 2 || !(meta instanceof BannerMeta b)) return false;
                    String action = args[1].toLowerCase();
                    switch (action) {
                        case "add" -> {
                            if (args.length < 4) return false;
                            b.addPattern(new Pattern(DyeColor.valueOf(args[2].toUpperCase()), PatternType.valueOf(args[3].toUpperCase())));
                        }
                        case "set" -> {
                            if (args.length < 5) return false;
                            int index = Integer.parseInt(args[2]);
                            b.setPattern(index, new Pattern(DyeColor.valueOf(args[3].toUpperCase()), PatternType.valueOf(args[4].toUpperCase())));
                        }
                        case "remove" -> {
                            if (args.length < 3) return false;
                            b.removePattern(Integer.parseInt(args[2]));
                        }
                    }
                }
                case "booktype" -> {
                    if (args.length < 2 || !(meta instanceof BookMeta b)) return false;
                    b.setGeneration(BookMeta.Generation.valueOf(args[1].toUpperCase()));
                }
                case "tropicalfish" -> {
                    if (args.length < 2 || !(meta instanceof TropicalFishBucketMeta b)) return false;
                    String action = args[1].toLowerCase();
                    if (action.equals("pattern")) b.setPattern(TropicalFish.Pattern.valueOf(args[2].toUpperCase()));
                    else if (action.equals("bodycolor"))
                        b.setBodyColor(DyeColor.valueOf(args[2].toUpperCase()));
                    else if (action.equals("patterncolor"))
                        b.setPatternColor(DyeColor.valueOf(args[2].toUpperCase()));
                }
                case "compass" -> {
                    if (args.length < 2 || !(meta instanceof CompassMeta c)) return false;
                    if (args[1].equalsIgnoreCase("set")) c.setLodestone(player.getLocation());
                    else if (args[1].equalsIgnoreCase("clear")) c.setLodestone(null);
                }
                case "spawnereggtype" -> {
                    if (args.length < 2 || !(meta instanceof SpawnEggMeta s)) return false;
                    s.setCustomSpawnedType(EntityType.valueOf(args[1].toUpperCase()));
                }
                case "listaliases" -> {
                    player.sendMessage(FontUtils.parse("&#00fbff" + "ᴅᴏsᴛᴜᴘɴé ᴀʟɪᴀsʏ: ʀᴇɴᴀᴍᴇ, ʟᴏʀᴇ, ᴇɴᴄʜᴀɴᴛ, ʜɪᴅᴇ, ᴜɴʙʀᴇᴀᴋᴀʙʟᴇ, ʀᴇᴘᴀɪʀᴄᴏsᴛ, ᴀᴍᴏᴜɴᴛ, ᴅᴜʀᴀʙɪʟɪᴛʏ, sᴋᴜʟʟᴏᴡɴᴇʀ, ᴄᴜsᴛᴏᴍᴍᴏᴅᴇʟᴅᴀᴛᴀ, ᴛʏᴘᴇ, ʟᴇᴀᴛʜᴇʀᴄᴏʟᴏʀ, ᴘᴏᴛɪᴏɴᴄᴏʟᴏʀ, ʙᴏᴏᴋᴀᴜᴛʜᴏʀ, ғɪʀᴇᴡᴏʀᴋᴘᴏᴡᴇʀ, ᴘᴏᴛɪᴏɴᴇғғᴇᴄᴛ, ᴀᴛᴛʀɪʙᴜᴛᴇ, ʙᴀɴɴᴇʀ, ʙᴏᴏᴋᴛʏᴘᴇ, ᴛʀᴏᴘɪᴄᴀʟғɪsʜ, ᴄᴏᴍᴘᴀss, sᴘᴀᴡɴᴇʀᴇɢɢᴛʏᴘᴇ"));
                }
                default -> {
                    player.sendMessage(FontUtils.parse("§c" + "ᴛᴇɴᴛᴏ sᴜʙᴘříᴋᴀᴢ ɴᴇɴí ᴢᴀᴛíᴍ ɪᴍᴘʟᴇᴍᴇɴᴛᴏᴠáɴ ɴᴇʙᴏ ᴊᴇ ɴᴇᴘʟᴀᴛɴý."));
                    return true;
                }
            }
            item.setItemMeta(meta);
            player.sendMessage(FontUtils.parse("&#00fbff" + "ᴘřᴇᴅᴍěᴛ ʙʏʟ ᴜᴘʀᴀᴠᴇɴ."));
        } catch (Exception e) {
            player.sendMessage(FontUtils.parse("§c" + "ᴄʜʏʙᴀ: " + e.getMessage()));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return subcommands.stream().filter(s -> s.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            switch (sub) {
                case "lore" -> {
                    return Arrays.asList("add", "set", "remove", "reset").stream()
                            .filter(s -> s.startsWith(args[1].toLowerCase())).collect(Collectors.toList());
                }
                case "enchant" -> {
                    return Arrays.stream(Enchantment.values())
                            .map(e -> e.getKey().getKey())
                            .filter(s -> s.startsWith(args[1].toLowerCase())).collect(Collectors.toList());
                }
                case "hide", "unhide" -> {
                    return Arrays.stream(ItemFlag.values())
                            .map(Enum::name)
                            .map(String::toLowerCase)
                            .filter(s -> s.startsWith(args[1].toLowerCase())).collect(Collectors.toList());
                }
                case "type" -> {
                    return Arrays.stream(Material.values())
                            .map(Enum::name)
                            .map(String::toLowerCase)
                            .filter(s -> s.startsWith(args[1].toLowerCase())).collect(Collectors.toList());
                }
                case "potioneffect" -> {
                    return Arrays.asList("add", "remove", "reset").stream()
                            .filter(s -> s.startsWith(args[1].toLowerCase())).collect(Collectors.toList());
                }
                case "attribute" -> {
                    return Arrays.asList("add", "remove").stream()
                            .filter(s -> s.startsWith(args[1].toLowerCase())).collect(Collectors.toList());
                }
                case "banner" -> {
                    return Arrays.asList("add", "set", "remove").stream()
                            .filter(s -> s.startsWith(args[1].toLowerCase())).collect(Collectors.toList());
                }
                case "tropicalfish" -> {
                    return Arrays.asList("pattern", "bodycolor", "patterncolor").stream()
                            .filter(s -> s.startsWith(args[1].toLowerCase())).collect(Collectors.toList());
                }
                case "compass" -> {
                    return Arrays.asList("set", "clear").stream()
                            .filter(s -> s.startsWith(args[1].toLowerCase())).collect(Collectors.toList());
                }
            }
        }
        if (args.length == 3) {
            String sub = args[0].toLowerCase();
            if (sub.equals("potioneffect") && args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("remove")) {
                return Arrays.stream(PotionEffectType.values())
                        .map(p -> p.getName().toLowerCase())
                        .filter(s -> s.startsWith(args[2].toLowerCase())).collect(Collectors.toList());
            }
            if (sub.equals("attribute") && (args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("remove"))) {
                return Arrays.stream(Attribute.values())
                        .map(a -> a.name().toLowerCase())
                        .filter(s -> s.startsWith(args[2].toLowerCase())).collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }
}
