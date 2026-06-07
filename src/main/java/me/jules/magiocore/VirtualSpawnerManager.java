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
                int delay = plugin.getModuleManager().getModuleConfig("virtualspawner").getInt("delay", 25);
                int timeLeft = s.getInt("timeLeft", delay);
                int xp = s.getInt("xp", 0);
                List<ItemStack> loot = (List<ItemStack>) s.getList("loot", new ArrayList<>());
                Set<Material> blocked = new HashSet<>();
                List<String> blockedNames = s.getStringList("blocked");
                for (String name : blockedNames) {
                    try { blocked.add(Material.valueOf(name)); } catch (Exception ignored) {}
                }

                spawners.put(loc, new VirtualSpawnerData(loc, type, count, timeLeft, loot, blocked, xp));
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
            config.set(path + ".xp", data.xp);
            config.set(path + ".loot", data.loot);
            config.set(path + ".blocked", data.blockedMaterials.stream().map(Enum::name).toList());
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
                if (data.location.getWorld() == null || !data.location.isChunkLoaded()) continue;

                boolean playerNearby = false;
                for (Player p : data.location.getWorld().getPlayers()) {
                    if (p.getLocation().distanceSquared(data.location) <= 900) { // 30 blocks
                        playerNearby = true;
                        break;
                    }
                }

                if (playerNearby) {
                    data.timeLeft--;
                    if (data.timeLeft <= 0) {
                        int delay = plugin.getModuleManager().getModuleConfig("virtualspawner").getInt("delay", 25);
                        data.timeLeft = delay;
                        generateLoot(data);
                    }
                }
                updateHologram(data);
            }
        }, 20L, 20L);
    }

    private void updateHologram(VirtualSpawnerData data) {
        if (data.location.getWorld() == null || !data.location.isChunkLoaded()) return;

        if (data.hologram == null || !data.hologram.isValid()) {
            Location center = data.location.clone().add(0.5, 0.5, 0.5);
            Location targetLoc = data.location.clone().add(0.5, 2.2, 0.5);

            for (Entity entity : data.location.getChunk().getEntities()) {
                if (entity instanceof TextDisplay td && entity.getPersistentDataContainer().has(hologramKey, PersistentDataType.BYTE)) {
                    if (entity.getLocation().distanceSquared(center) < 4.0) {
                        data.hologram = td;
                        data.hologram.teleport(targetLoc);
                        break;
                    }
                }
            }

            if (data.hologram == null || !data.hologram.isValid()) {
                data.hologram = data.location.getWorld().spawn(targetLoc, TextDisplay.class);
                data.hologram.setBillboard(TextDisplay.Billboard.CENTER);
                data.hologram.setShadowed(true);
                data.hologram.setBackgroundColor(org.bukkit.Color.fromARGB(0, 0, 0, 0));
                data.hologram.getPersistentDataContainer().set(hologramKey, PersistentDataType.BYTE, (byte) 1);
                data.hologram.setViewRange(0.35f);
            }
        }

        int lootCount = data.loot.stream().mapToInt(ItemStack::getAmount).sum();
        int maxLoot = 1000 * data.count; // Example capacity
        int maxXP = 5000 * data.count;

        String storageBar = createProgressBar(lootCount, maxLoot);
        String xpBar = createProgressBar(data.xp, maxXP);

        List<String> lines = plugin.getModuleManager().getModuleConfig("virtualspawner").getStringList("hologram");
        if (lines.isEmpty()) {
            lines = List.of(
                "#c2c2c2(#fff9c2%count%#969696x#c2c2c2) &#00fbff&l%type% SPAWNER",
                "#34eb98☁ Skladování #6e6d6d➤ %storage_bar%",
                "#fab170❆ Zkušenosti #6e6d6d➤ %xp_bar%",
                "&r",
                "&#FCD05C⬇ &#4498DBᴋʟɪᴋɴɪ ᴘʀᴏ ᴍᴇɴᴜ &#FCD05C⬇"
            );
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i)
                .replace("%count%", String.valueOf(data.count))
                .replace("%type%", data.type.name())
                .replace("%storage_bar%", storageBar)
                .replace("%xp_bar%", xpBar);
            sb.append(line);
            if (i < lines.size() - 1) sb.append("\n");
        }

        data.hologram.text(FontUtils.parse(sb.toString(), true));
    }

    private String createProgressBar(int current, int max) {
        int length = 12;
        int completed = (int) ((double) current / max * length);
        completed = Math.min(length, Math.max(0, completed));

        return "&#00ff00" + "☰".repeat(completed) + "#dedede" + "☰".repeat(length - completed);
    }

    private void generateLoot(VirtualSpawnerData data) {
        Random rand = new Random();
        for (int i = 0; i < data.count; i++) {
            data.xp += rand.nextInt(5) + 2; // 2-6 XP per spawn

            List<ItemStack> items = new ArrayList<>();
            switch (data.type) {
                case ZOMBIE -> {
                    items.add(new ItemStack(Material.ROTTEN_FLESH, rand.nextInt(3) + 1));
                    if (rand.nextInt(100) < 5) items.add(new ItemStack(Material.IRON_INGOT));
                    if (rand.nextInt(100) < 5) items.add(new ItemStack(Material.CARROT));
                    if (rand.nextInt(100) < 5) items.add(new ItemStack(Material.POTATO));
                }
                case SKELETON -> {
                    items.add(new ItemStack(Material.BONE, rand.nextInt(3) + 1));
                    items.add(new ItemStack(Material.ARROW, rand.nextInt(3) + 1));
                    if (rand.nextInt(100) < 10) {
                        ItemStack bow = new ItemStack(Material.BOW);
                        org.bukkit.inventory.meta.Damageable meta = (org.bukkit.inventory.meta.Damageable) bow.getItemMeta();
                        meta.setDamage(rand.nextInt(Material.BOW.getMaxDurability()));
                        bow.setItemMeta(meta);
                        items.add(bow);
                    }
                }
                case SPIDER -> {
                    items.add(new ItemStack(Material.STRING, rand.nextInt(3) + 1));
                    if (rand.nextInt(100) < 15) items.add(new ItemStack(Material.SPIDER_EYE));
                }
                case CREEPER -> {
                    items.add(new ItemStack(Material.GUNPOWDER, rand.nextInt(3) + 1));
                    if (rand.nextInt(100) < 5) items.add(new ItemStack(Material.TNT));
                }
                case PIG -> {
                    items.add(new ItemStack(Material.PORKCHOP, rand.nextInt(4) + 1));
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
                if (data.blockedMaterials.contains(item.getType())) continue;
                addLootItem(data, item);
            }
        }
        save();
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
        int delay = plugin.getModuleManager().getModuleConfig("virtualspawner").getInt("delay", 25);
        VirtualSpawnerData data = new VirtualSpawnerData(loc, type, 1, delay, new ArrayList<>(), new HashSet<>(), 0);
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
        for (org.bukkit.entity.Entity entity : player.getWorld().getEntitiesByClass(TextDisplay.class)) {
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
        public final Set<Material> blockedMaterials;
        public int xp;
        public TextDisplay hologram;

        public VirtualSpawnerData(Location location, EntityType type, int count, int timeLeft, List<ItemStack> loot, Set<Material> blocked, int xp) {
            this.location = location;
            this.type = type;
            this.count = count;
            this.timeLeft = timeLeft;
            this.loot = loot;
            this.blockedMaterials = blocked;
            this.xp = xp;
        }
    }
}
