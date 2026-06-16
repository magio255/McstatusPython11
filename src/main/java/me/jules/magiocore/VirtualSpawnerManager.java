package me.jules.magiocore;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class VirtualSpawnerManager {
    private final MagioCore plugin;
    private final File file;
    private FileConfiguration config;
    private final Map<Location, VirtualSpawnerData> spawners = new ConcurrentHashMap<>();
    private BukkitTask task;
    private final NamespacedKey hologramKey;

    public VirtualSpawnerManager(MagioCore plugin) {
        this.plugin = plugin;
        this.hologramKey = new NamespacedKey(plugin, "vspawner_hologram");
        this.file = new File(plugin.getDataFolder(), "spawners.yml");
        load();
        startTask();
    }

    public void load() {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
        spawners.clear();

        ConfigurationSection section = config.getConfigurationSection("spawners");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                ConfigurationSection s = section.getConfigurationSection(key);
                if (s == null) continue;

                Location loc = s.getLocation("location");
                if (loc == null) continue;

                EntityType type = EntityType.valueOf(s.getString("type", "ZOMBIE"));
                int count = s.getInt("count", 1);
                int timeLeft = s.getInt("timeLeft", 15);
                List<ItemStack> loot = (List<ItemStack>) s.getList("loot", new ArrayList<>());

                spawners.put(loc, new VirtualSpawnerData(loc, type, count, timeLeft, loot));
            }
        }
    }

    public void save() {
        config.set("spawners", null);
        int i = 0;
        for (VirtualSpawnerData data : spawners.values()) {
            String path = "spawners.s" + i++;
            config.set(path + ".location", data.location);
            config.set(path + ".type", data.type.name());
            config.set(path + ".count", data.count);
            config.set(path + ".timeLeft", data.timeLeft);
            config.set(path + ".loot", data.loot);
        }
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startTask() {
        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (VirtualSpawnerData data : spawners.values()) {
                if (!data.location.isChunkLoaded()) continue;

                boolean playerNearby = false;
                for (Player p : data.location.getWorld().getPlayers()) {
                    if (p.getLocation().distanceSquared(data.location) <= 225) { // 15 blocks
                        playerNearby = true;
                        break;
                    }
                }

                if (playerNearby) {
                    data.timeLeft--;
                    if (data.timeLeft <= 0) {
                        data.timeLeft = 15;
                        generateLoot(data);
                    }
                }
                updateHologram(data);
            }
        }, 20L, 20L);
    }

    private void updateHologram(VirtualSpawnerData data) {
        if (!data.location.isChunkLoaded()) return;

        if (data.hologram == null || !data.hologram.isValid()) {
            Location loc = data.location.clone().add(0.5, 1.5, 0.5);

            // Try to find existing hologram in chunk to avoid duplicates
            for (Entity entity : loc.getChunk().getEntities()) {
                if (entity instanceof TextDisplay td && entity.getPersistentDataContainer().has(hologramKey, PersistentDataType.BYTE)) {
                    if (entity.getLocation().distanceSquared(loc) < 0.1) {
                        data.hologram = td;
                        break;
                    }
                }
            }

            if (data.hologram == null || !data.hologram.isValid()) {
                data.hologram = data.location.getWorld().spawn(loc, TextDisplay.class);
                data.hologram.setBillboard(TextDisplay.Billboard.CENTER);
                data.hologram.setShadowed(true);
                data.hologram.setBackgroundColor(org.bukkit.Color.fromARGB(0, 0, 0, 0));
                data.hologram.getPersistentDataContainer().set(hologramKey, PersistentDataType.BYTE, (byte) 1);
                // Standard block scale is 1.0. 15 blocks is roughly 0.15 in float range?
                // View range is usually in blocks for TextDisplay? Documentation says view range is roughly blocks.
                data.hologram.setViewRange(0.15f); // 15 blocks (0.1f = 10 blocks usually in Paper/Spigot TextDisplay)
            }
        }

        int lootCount = data.loot.stream().mapToInt(ItemStack::getAmount).sum();
        String text = "&#00fbff&l" + data.type.name() + " sᴘᴀᴡɴᴇʀ §8(x" + data.count + ")\n" +
                     "&7ꜱᴇʀᴠᴇʀ ᴠɪʀᴛᴜᴀʟ ꜱʏꜱᴛᴇᴍ\n" +
                     "&r\n" +
                     "&fᴘᴏčᴇᴛ ᴘřᴇᴅᴍěᴛů: &#00fbff" + lootCount + " ᴋs\n" +
                     "&fᴅᴀʟší sᴘᴀᴡɴ ᴢᴀ: &#00fbff" + data.timeLeft + "s\n" +
                     "&r\n" +
                     "&#FCD05C⬇ &#4498DBᴋʟɪᴋɴɪ ᴘʀᴏ ᴍᴇɴᴜ &#FCD05C⬇";
        data.hologram.text(FontUtils.parse(text));
    }

    private void generateLoot(VirtualSpawnerData data) {
        Random rand = new Random();
        for (int i = 0; i < data.count; i++) {
            List<ItemStack> items = new ArrayList<>();
            switch (data.type) {
                case ZOMBIE -> {
                    items.add(new ItemStack(Material.ROTTEN_FLESH, rand.nextInt(3))); // 0-2
                    if (rand.nextInt(100) < 5) items.add(new ItemStack(Material.IRON_INGOT));
                    if (rand.nextInt(100) < 5) items.add(new ItemStack(Material.CARROT));
                    if (rand.nextInt(100) < 5) items.add(new ItemStack(Material.POTATO));
                }
                case SKELETON -> {
                    items.add(new ItemStack(Material.BONE, rand.nextInt(3))); // 0-2
                    items.add(new ItemStack(Material.ARROW, rand.nextInt(3))); // 0-2
                    if (rand.nextInt(100) < 10) {
                        ItemStack bow = new ItemStack(Material.BOW);
                        org.bukkit.inventory.meta.Damageable meta = (org.bukkit.inventory.meta.Damageable) bow.getItemMeta();
                        meta.setDamage(rand.nextInt(Material.BOW.getMaxDurability()));
                        bow.setItemMeta(meta);
                        items.add(bow);
                    }
                }
                case SPIDER -> {
                    items.add(new ItemStack(Material.STRING, rand.nextInt(3)));
                    if (rand.nextInt(100) < 15) items.add(new ItemStack(Material.SPIDER_EYE));
                }
                case CREEPER -> {
                    items.add(new ItemStack(Material.GUNPOWDER, rand.nextInt(3)));
                    if (rand.nextInt(100) < 5) items.add(new ItemStack(Material.TNT));
                }
                case PIG -> {
                    items.add(new ItemStack(Material.PORKCHOP, rand.nextInt(4) + 1)); // 1-3
                }
                case COW -> {
                    items.add(new ItemStack(Material.BEEF, rand.nextInt(4) + 1));
                    items.add(new ItemStack(Material.LEATHER, rand.nextInt(3)));
                }
                case CHICKEN -> {
                    items.add(new ItemStack(Material.CHICKEN, 1));
                    items.add(new ItemStack(Material.FEATHER, rand.nextInt(3)));
                }
                default -> items.add(new ItemStack(Material.IRON_INGOT));
            }

            for (ItemStack item : items) {
                if (item.getAmount() <= 0 && !item.getType().name().contains("BOW")) continue;
                addLootItem(data, item);
            }
        }
    }

    private void addLootItem(VirtualSpawnerData data, ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return;

        boolean merged = false;
        if (item.getMaxStackSize() > 1) {
            for (ItemStack lootItem : data.loot) {
                if (lootItem != null && lootItem.isSimilar(item) && lootItem.getAmount() < lootItem.getMaxStackSize()) {
                    int canAdd = lootItem.getMaxStackSize() - lootItem.getAmount();
                    int adding = Math.min(item.getAmount(), canAdd);
                    lootItem.setAmount(lootItem.getAmount() + adding);
                    item.setAmount(item.getAmount() - adding);
                    if (item.getAmount() <= 0) {
                        merged = true;
                        break;
                    }
                }
            }
        }
        if (!merged && item.getAmount() > 0) {
            data.loot.add(item.clone());
        }
    }

    public void addSpawner(Location loc, EntityType type) {
        VirtualSpawnerData data = new VirtualSpawnerData(loc, type, 1, 15, new ArrayList<>());
        spawners.put(loc, data);
        updateHologram(data);
        save();
    }

    public void removeSpawner(Location loc) {
        VirtualSpawnerData data = spawners.remove(loc);
        if (data != null && data.hologram != null) {
            data.hologram.remove();
        }
        save();
    }

    public VirtualSpawnerData getSpawner(Location loc) {
        return spawners.get(loc);
    }

    public Collection<VirtualSpawnerData> getAllSpawners() {
        return spawners.values();
    }

    public int forceCleanup(Player player) {
        int count = 0;
        // Search all TextDisplays in the player's world
        for (org.bukkit.entity.Entity entity : player.getWorld().getEntitiesByClass(TextDisplay.class)) {
            // Remove if tagged or within a 10-block radius
            if (entity.getPersistentDataContainer().has(hologramKey, PersistentDataType.BYTE) ||
                entity.getLocation().distanceSquared(player.getLocation()) <= 100) {
                entity.remove();
                count++;
            }
        }
        return count;
    }

    public void stopTask() {
        if (task != null) task.cancel();
        for (VirtualSpawnerData data : spawners.values()) {
            if (data.hologram != null) data.hologram.remove();
        }
    }

    public static class VirtualSpawnerData {
        public Location location;
        public EntityType type;
        public int count;
        public int timeLeft;
        public List<ItemStack> loot;
        public TextDisplay hologram;

        public VirtualSpawnerData(Location location, EntityType type, int count, int timeLeft, List<ItemStack> loot) {
            this.location = location;
            this.type = type;
            this.count = count;
            this.timeLeft = timeLeft;
            this.loot = loot;
        }
    }
}
