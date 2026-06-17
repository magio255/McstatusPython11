package me.jules.magiocore;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

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
        Scoreboard sb = player.getScoreboard();
        if (sb == Bukkit.getScoreboardManager().getMainScoreboard()) {
            sb = Bukkit.getScoreboardManager().getNewScoreboard();
        }

        Objective obj = sb.getObjective("stats");
        if (obj == null) {
            obj = sb.registerNewObjective("stats", "dummy", FontUtils.parse("&#EA427F&lᴄᴢsᴋ sᴍᴘ"));
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        }

        // Simple scoreboard
        replaceScore(obj, 6, "§1");
        replaceScore(obj, 5, "§fHráč: &#00fbff" + player.getName());
        replaceScore(obj, 4, "§fPeníze: &#00ff44" + FontUtils.formatMoney(plugin.getEconomy().getBalance(player)) + " $");
        replaceScore(obj, 3, "§2");
        replaceScore(obj, 2, "§fOnline: &#00fbff" + Bukkit.getOnlinePlayers().size());
        replaceScore(obj, 1, "§3");
        replaceScore(obj, 0, "§7web.czsksmp.eu");

        player.setScoreboard(sb);
    }

    private void replaceScore(Objective obj, int score, String text) {
        String legacyText = LegacyComponentSerializer.legacySection().serialize(FontUtils.parse(text));
        obj.getScore(legacyText).setScore(score);
    }
}
