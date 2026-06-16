package me.jules.magiocore;

import org.bukkit.Location;
import java.util.UUID;

public class Home {
    private final UUID owner;
    private final int number;
    private final Location location;

    public Home(UUID owner, int number, Location location) {
        this.owner = owner;
        this.number = number;
        this.location = location;
    }

    public UUID getOwner() {
        return owner;
    }

    public int getNumber() {
        return number;
    }

    public Location getLocation() {
        return location;
    }
}
