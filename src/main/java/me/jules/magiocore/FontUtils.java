package me.jules.magiocore;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FontUtils {
    private static final Pattern CODE_PATTERN = Pattern.compile("(&#[A-Fa-f0-9]{6}|&[0-9a-fk-orA-FK-OR]|§[0-9a-fk-orA-FK-OR])");

    public static String toSmallCaps(String input) {
        return input; // Disabled Small Caps
    }

    public static Component parse(String input) {
        if (input == null) return Component.empty();

        Matcher matcher = CODE_PATTERN.matcher(input);
        StringBuilder sb = new StringBuilder();
        int lastEnd = 0;
        while (matcher.find()) {
            sb.append(input.substring(lastEnd, matcher.start()));

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
        sb.append(input.substring(lastEnd));

        return LegacyComponentSerializer.legacySection().deserialize(sb.toString())
                .decoration(TextDecoration.ITALIC, false);
    }

    public static String formatMoney(double amount) {
        if (amount >= 1000000000000000.0) { // Quadrillion
            return String.format("%.1fQ", amount / 1000000000000000.0);
        } else if (amount >= 1000000000000.0) { // Trillion
            return String.format("%.1fT", amount / 1000000000000.0);
        } else if (amount >= 1000000000.0) { // Billion
            return String.format("%.1fB", amount / 1000000000.0);
        } else if (amount >= 1000000.0) { // Million
            return String.format("%.1fM", amount / 1000000.0);
        } else if (amount >= 1000.0) { // Thousand
            return String.format("%.1fK", amount / 1000.0);
        } else {
            return String.format("%.0f", amount);
        }
    }
}
