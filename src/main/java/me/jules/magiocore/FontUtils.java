package me.jules.magiocore;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FontUtils {
    private static final Map<Character, Character> SMALL_CAPS = new HashMap<>();
    private static final Pattern CODE_PATTERN = Pattern.compile("(&#[A-Fa-f0-9]{6}|&[0-9a-fk-orA-FK-OR]|§[0-9a-fk-orA-FK-OR])");

    static {
        String normal = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String smallCaps = "ᴀʙᴄᴅᴇꜰɢʜɪᴊᴋʟᴍɴᴏᴘǫʀsᴛᴜᴠᴡxʏᴢᴀʙᴄᴅᴇꜰɢʜɪᴊᴋʟᴍɴᴏᴘǫʀsᴛᴜᴠᴡxʏᴢ";

        for (int i = 0; i < normal.length(); i++) {
            SMALL_CAPS.put(normal.charAt(i), smallCaps.charAt(i));
        }

        // Czech diacritics mapping to accented small caps equivalents as requested
        SMALL_CAPS.put('á', 'á'); SMALL_CAPS.put('Á', 'á');
        SMALL_CAPS.put('č', 'č'); SMALL_CAPS.put('Č', 'č');
        SMALL_CAPS.put('ď', 'ď'); SMALL_CAPS.put('Ď', 'ď');
        SMALL_CAPS.put('é', 'é'); SMALL_CAPS.put('É', 'é');
        SMALL_CAPS.put('ě', 'ě'); SMALL_CAPS.put('Ě', 'ě');
        SMALL_CAPS.put('í', 'í'); SMALL_CAPS.put('Í', 'í');
        SMALL_CAPS.put('ň', 'ň'); SMALL_CAPS.put('Ň', 'ň');
        SMALL_CAPS.put('ó', 'ó'); SMALL_CAPS.put('Ó', 'ó');
        SMALL_CAPS.put('ř', 'ř'); SMALL_CAPS.put('Ř', 'ř');
        SMALL_CAPS.put('š', 'š'); SMALL_CAPS.put('Š', 'š');
        SMALL_CAPS.put('ť', 'ť'); SMALL_CAPS.put('Ť', 'ť');
        SMALL_CAPS.put('ú', 'ú'); SMALL_CAPS.put('Ú', 'ú');
        SMALL_CAPS.put('ů', 'ů'); SMALL_CAPS.put('Ů', 'ů');
        SMALL_CAPS.put('ý', 'ý'); SMALL_CAPS.put('Ý', 'ý');
        SMALL_CAPS.put('ž', 'ž'); SMALL_CAPS.put('Ž', 'ž');

        // Ensure they are small caps where possible if the user meant specific small-caps with accents
        // but often 'ᴍáᴍ' just uses standard accented chars if small-caps accented ones don't exist in unicode.
        // User example 'ᴍáᴍ ʀáᴅé' uses standard 'á' and 'é'.
    }

    public static String toSmallCaps(String input) {
        if (input == null) return null;
        StringBuilder sb = new StringBuilder();
        for (char c : input.toCharArray()) {
            sb.append(SMALL_CAPS.getOrDefault(c, c));
        }
        return sb.toString();
    }

    public static Component parse(String input) {
        if (input == null) return Component.empty();

        Matcher matcher = CODE_PATTERN.matcher(input);
        StringBuilder sb = new StringBuilder();
        int lastEnd = 0;
        while (matcher.find()) {
            String before = input.substring(lastEnd, matcher.start());
            sb.append(toSmallCaps(before));

            String code = matcher.group();
            if (code.startsWith("&#")) {
                String hex = code.substring(2);
                sb.append("§x");
                for (char c : hex.toCharArray()) {
                    sb.append("§").append(c);
                }
            } else {
                sb.append(code.replace("&", "§"));
            }
            lastEnd = matcher.end();
        }
        sb.append(toSmallCaps(input.substring(lastEnd)));

        return LegacyComponentSerializer.legacySection().deserialize(sb.toString())
                .decoration(TextDecoration.ITALIC, false);
    }

    public static String formatMoney(double amount) {
        if (amount < 1000) return String.format("%.1f", amount);
        int exp = (int) (Math.log(amount) / Math.log(1000));
        char unit = "kmbtq".charAt(exp - 1);
        return String.format("%.1f%c", amount / Math.pow(1000, exp), unit);
    }
}
