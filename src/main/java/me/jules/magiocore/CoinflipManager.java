package me.jules.magiocore;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CoinflipManager {
    private final List<CoinflipBet> activeBets = new ArrayList<>();

    public static class CoinflipBet {
        public final UUID creator;
        public final String creatorName;
        public final double amount;

        public CoinflipBet(UUID creator, String creatorName, double amount) {
            this.creator = creator;
            this.creatorName = creatorName;
            this.amount = amount;
        }
    }

    public void addBet(Player player, double amount) {
        activeBets.add(new CoinflipBet(player.getUniqueId(), player.getName(), amount));
    }

    public List<CoinflipBet> getActiveBets() {
        return activeBets;
    }

    public void removeBet(CoinflipBet bet) {
        activeBets.remove(bet);
    }
}
