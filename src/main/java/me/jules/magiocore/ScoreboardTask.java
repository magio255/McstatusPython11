package me.jules.magiocore;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.List;

public class ScoreboardTask extends BukkitRunnable {
    private final MagioCore plugin;

    public ScoreboardTask(MagioCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (plugin.getSettingsManager().getSettings(player.getUniqueId()).scoreboard()) {
                updateScoreboard(player);
            } else {
                player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
            }
        }
    }

    private void updateScoreboard(Player player) {
        FileConfiguration config = plugin.getModuleManager().getModuleConfig("scoreboard");
        if (config == null) return;

        Scoreboard sb = player.getScoreboard();
        if (sb == Bukkit.getScoreboardManager().getMainScoreboard()) {
            sb = Bukkit.getScoreboardManager().getNewScoreboard();
        }

        Objective obj = sb.getObjective("stats");
        String title = config.getString("title", "&#EA427F&lᴄᴢsᴋ sᴍᴘ");
        if (obj == null) {
            obj = sb.registerNewObjective("stats", "dummy", FontUtils.parse(title));
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        } else {
            obj.displayName(FontUtils.parse(title));
        }

        // To avoid ghosting lines when text changes, we should ideally reset the scores.
        // However, to keep it simple and consistent with the previous implementation:
        List<String> lines = config.getStringList("lines");
        int scoreValue = lines.size() - 1;

        // Reset scores if we want to be safe, but Bukkit Scoreboard API is a bit clunky for this without tracking.
        // For now, we just overwrite.
        for (String line : lines) {
            String formattedLine = line
                    .replace("%player%", player.getName())
                    .replace("%money%", FontUtils.formatMoney(plugin.getEconomy().getBalance(player)))
                    .replace("%online%", String.valueOf(Bukkit.getOnlinePlayers().size()));

            replaceScore(sb, obj, scoreValue, formattedLine);
            scoreValue--;
        }

        player.setScoreboard(sb);
    }

    private void replaceScore(Scoreboard sb, Objective obj, int score, String text) {
        String legacyText = LegacyComponentSerializer.legacySection().serialize(FontUtils.parse(text));

        // Find if there's already a different text at this score and remove it to avoid duplicates
        for (String entry : sb.getEntries()) {
            if (obj.getScore(entry).getScore() == score && !entry.equals(legacyText)) {
                sb.resetScores(entry);
            }
        }

        obj.getScore(legacyText).setScore(score);
    }
}
