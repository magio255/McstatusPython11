package me.jules.vipmaker;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class LuckPermsService {

    private final LuckPerms luckPerms;

    public LuckPermsService() {
        this.luckPerms = LuckPermsProvider.get();
    }

    public CompletableFuture<Void> addGroup(Player player, String groupName, int days) {
        UUID uuid = player.getUniqueId();
        return luckPerms.getUserManager().modifyUser(uuid, user -> {
            InheritanceNode node = InheritanceNode.builder(groupName)
                    .expiry(Duration.ofDays(days))
                    .build();

            user.data().add(node);
        });
    }

    public boolean hasGroup(Player player, String groupName) {
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null) return false;

        return user.getNodes().stream()
                .filter(node -> node instanceof InheritanceNode)
                .map(node -> (InheritanceNode) node)
                .anyMatch(node -> node.getGroupName().equalsIgnoreCase(groupName));
    }
}
