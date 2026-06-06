package me.jules.magiocore;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class VirtualSpawnerListener implements Listener {
    private final MagioCore plugin;
    private final VirtualSpawnerManager manager;

    public VirtualSpawnerListener(MagioCore plugin, VirtualSpawnerManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        NamespacedKey key = new NamespacedKey(plugin, "virtual_spawner");
        if (meta.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
            String worldName = event.getBlock().getWorld().getName();
            if (!worldName.equals("world") && !worldName.equals("world_nether") && !worldName.equals("world_the_end")) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(FontUtils.parse("§c" + "ᴠɪʀᴛᴜáʟɴí sᴘᴀᴡɴᴇʀʏ ʟᴢᴇ ᴘᴏᴋʟáᴅᴀᴛ ᴘᴏᴜᴢᴇ ᴠ ʜʟᴀᴠɴíᴄʜ sᴠěᴛᴇᴄʜ (ᴡᴏʀʟᴅ, ɴᴇᴛʜᴇʀ, ᴇɴᴅ)."));
                return;
            }

            String typeStr = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
            EntityType type = EntityType.valueOf(typeStr);
            manager.addSpawner(event.getBlock().getLocation(), type);
            event.getPlayer().sendMessage(FontUtils.parse("&#00fbff" + "ᴠɪʀᴛᴜáʟɴí sᴘᴀᴡɴᴇʀ ʙʏʟ ᴠʏᴛᴠᴏřᴇɴ."));
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Location loc = event.getBlock().getLocation();
        VirtualSpawnerManager.VirtualSpawnerData data = manager.getSpawner(loc);
        if (data != null) {
            manager.removeSpawner(loc);

            ItemStack spawner = new ItemStack(Material.SPAWNER, 1);
            ItemMeta meta = spawner.getItemMeta();
            meta.displayName(FontUtils.parse("&#00fbff&lᴠɪʀᴛᴜáʟɴí sᴘᴀᴡɴᴇʀ"));
            meta.lore(Arrays.asList(
                    FontUtils.parse("§7ᴛʏᴘ: &#00fbff" + data.type.name()),
                    FontUtils.parse(""),
                    FontUtils.parse("&#00fbff» §7ᴘᴏʟᴏž ᴘʀᴏ ᴠʏᴛᴠᴏřᴇɴí sᴘᴀᴡɴᴇʀᴜ"),
                    FontUtils.parse("&#00fbff» §7ᴋʟɪᴋɴɪ sᴛᴇᴊɴýᴍ ᴛʏᴘᴇᴍ ᴘʀᴏ sᴛᴀᴄᴋᴏᴠáɴí"),
                    FontUtils.parse(""),
                    FontUtils.parse("&#FCD05Cᴅɪsᴘʟᴀʏ &#4498DBꜱᴇʀᴠᴇʀ ꜱʏꜱᴛᴇᴍ")
            ));
            meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "virtual_spawner"), PersistentDataType.STRING, data.type.name());
            spawner.setItemMeta(meta);

            event.setDropItems(false);
            int count = data.count;
            while (count > 0) {
                int drop = Math.min(count, 64);
                ItemStack toDrop = spawner.clone();
                toDrop.setAmount(drop);
                loc.getWorld().dropItemNaturally(loc, toDrop);
                count -= drop;
            }

            event.getPlayer().sendMessage(FontUtils.parse("§c" + "ᴠɪʀᴛᴜáʟɴí sᴘᴀᴡɴᴇʀ ʙʏʟ ᴏᴅsᴛʀᴀɴěɴ."));
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.SPAWNER) return;

        VirtualSpawnerManager.VirtualSpawnerData data = manager.getSpawner(block.getLocation());
        if (data != null) {
            event.setCancelled(true);

            ItemStack item = event.getItem();
            if (item != null && item.getType() == Material.SPAWNER) {
                ItemMeta meta = item.getItemMeta();
                NamespacedKey key = new NamespacedKey(plugin, "virtual_spawner");
                if (meta != null && meta.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
                    String typeStr = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
                    if (data.type.name().equalsIgnoreCase(typeStr)) {
                        int amount = event.getPlayer().isSneaking() ? item.getAmount() : 1;
                        data.count += amount;
                        item.setAmount(item.getAmount() - amount);
                        event.getPlayer().sendMessage(FontUtils.parse("&#00fbff" + "sᴘᴀᴡɴᴇʀ ʙʏʟ ᴠʏʟᴇᴘšᴇɴ (sᴛᴀᴄᴋᴇᴅ). ᴀᴋᴛᴜáʟɴě: " + data.count + "x"));
                        manager.save();
                        return;
                    }
                }
            }

            openSpawnerGui(event.getPlayer(), data);
        }
    }

    private void openSpawnerGui(Player player, VirtualSpawnerManager.VirtualSpawnerData data) {
        SpawnerGuiHolder holder = new SpawnerGuiHolder(data);
        Inventory inv = Bukkit.createInventory(holder, 27, FontUtils.parse("&#00fbffᴠɪʀᴛᴜáʟɴí sᴘᴀᴡɴᴇʀ"));
        holder.setInventory(inv);

        ItemStack glass = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 27; i++) inv.setItem(i, glass);

        updateSpawnerGui(inv, data);
        player.openInventory(inv);

        Bukkit.getScheduler().runTaskTimer(plugin, task -> {
            if (player.getOpenInventory().getTopInventory().getHolder() instanceof SpawnerGuiHolder spawnerHolder && spawnerHolder.data == data) {
                updateSpawnerGui(player.getOpenInventory().getTopInventory(), data);
            } else {
                task.cancel();
            }
        }, 20L, 20L);
    }

    private void updateSpawnerGui(Inventory inv, VirtualSpawnerManager.VirtualSpawnerData data) {
        // Storage item (Slot 11)
        ItemStack chest = new ItemStack(Material.CHEST);
        ItemMeta meta = chest.getItemMeta();
        meta.displayName(FontUtils.parse("&#00fbffSkladování Předmětů"));
        int lootCount = data.loot.stream().mapToInt(ItemStack::getAmount).sum();
        meta.lore(Arrays.asList(
                FontUtils.parse("§7Aktuálně uschováno: &#00fbff" + lootCount + " předmětů"),
                FontUtils.parse(""),
                FontUtils.parse("&#00fbff» §7Klikni pro otevření skladu")
        ));
        chest.setItemMeta(meta);
        inv.setItem(11, chest);

        // Information item (Slot 13)
        ItemStack book = new ItemStack(Material.BOOK);
        ItemMeta bookMeta = book.getItemMeta();
        bookMeta.displayName(FontUtils.parse("&#ffbb00Informace a Filtr"));
        bookMeta.lore(Arrays.asList(
                FontUtils.parse("§7Typ: &#ffbb00" + data.type.name()),
                FontUtils.parse("§7Počet spawnerů: &#ffbb00" + data.count + "x"),
                FontUtils.parse("§7Další spawn za: &#ffbb00" + data.timeLeft + "s"),
                FontUtils.parse(""),
                FontUtils.parse("&#ffbb00» §7Klikni pro nastavení filtru")
        ));
        book.setItemMeta(bookMeta);
        inv.setItem(13, book);

        // XP item (Slot 15)
        ItemStack xp = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta xpMeta = xp.getItemMeta();
        xpMeta.displayName(FontUtils.parse("&#fab170Zkušenosti"));
        xpMeta.lore(Arrays.asList(
                FontUtils.parse("§7Nasbírané XP: &#fab170" + data.xp),
                FontUtils.parse(""),
                FontUtils.parse("&#fab170» §7Klikni pro vybrání XP")
        ));
        xp.setItemMeta(xpMeta);
        inv.setItem(15, xp);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof AdminGuiHolder holder) {
            event.setCancelled(true);
            if (event.getRawSlot() >= 54 || event.getRawSlot() < 0) return;

            int slot = event.getRawSlot();
            Player player = (Player) event.getWhoClicked();
            List<VirtualSpawnerManager.VirtualSpawnerData> list = new ArrayList<>(manager.getAllSpawners());

            if (slot < 45) {
                int index = holder.page * 45 + slot;
                if (index < list.size()) {
                    VirtualSpawnerManager.VirtualSpawnerData data = list.get(index);
                    if (event.getClick().isLeftClick()) {
                        player.teleport(data.location.clone().add(0.5, 1, 0.5));
                        player.sendMessage(FontUtils.parse("&#00fbff" + "ʙʏʟ ᴊsɪ ᴛᴇʟᴇᴘᴏʀᴛᴏᴠáɴ ᴋ sᴘᴀᴡɴᴇʀᴜ."));
                    } else if (event.getClick().isRightClick()) {
                        manager.removeSpawner(data.location);
                        data.location.getBlock().setType(Material.AIR);
                        openAdminGui(player, holder.page);
                        player.sendMessage(FontUtils.parse("&#EA427F" + "sᴘᴀᴡɴᴇʀ ʙʏʟ ᴏᴅsᴛʀᴀɴěɴ."));
                    }
                }
            } else if (slot == 45 && holder.page > 0) {
                openAdminGui(player, holder.page - 1);
            } else if (slot == 53 && (holder.page + 1) * 45 < list.size()) {
                openAdminGui(player, holder.page + 1);
            }
            return;
        }

        if (event.getInventory().getHolder() instanceof SpawnerGuiHolder holder) {
            event.setCancelled(true);
            if (event.getRawSlot() == 11) {
                openLootGui((Player) event.getWhoClicked(), holder.data, 0);
            } else if (event.getRawSlot() == 13) {
                openFilterGui((Player) event.getWhoClicked(), holder.data);
            } else if (event.getRawSlot() == 15) {
                Player player = (Player) event.getWhoClicked();
                if (holder.data.xp > 0) {
                    player.giveExp(holder.data.xp);
                    player.sendMessage(FontUtils.parse("&#fab170Vyzvedl jsi si &#ffffff" + holder.data.xp + " &#fab170zkušeností."));
                    holder.data.xp = 0;
                    manager.save();
                } else {
                    player.sendMessage(FontUtils.parse("§cNemáš žádné zkušenosti k vyzvednutí."));
                }
            }
            return;
        }

        if (event.getInventory().getHolder() instanceof FilterGuiHolder holder) {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            if (slot >= 0 && slot < 54) {
                ItemStack item = event.getCurrentItem();
                if (item != null && item.getType() != Material.AIR && item.getType() != Material.GRAY_STAINED_GLASS_PANE && item.getType() != Material.BOOK) {
                    Material mat = item.getType();
                    if (holder.data.blockedMaterials.contains(mat)) {
                        holder.data.blockedMaterials.remove(mat);
                    } else {
                        holder.data.blockedMaterials.add(mat);
                    }
                    manager.save();
                    openFilterGui((Player) event.getWhoClicked(), holder.data);
                }
            }
            return;
        }

        if (event.getInventory().getHolder() instanceof LootGuiHolder holder) {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            Player player = (Player) event.getWhoClicked();

            if (slot < 45) {
                int itemIndex = holder.page * 45 + slot;
                if (itemIndex < holder.data.loot.size()) {
                    ItemStack item = holder.data.loot.get(itemIndex);
                    if (player.getInventory().firstEmpty() != -1) {
                        player.getInventory().addItem(item);
                        holder.data.loot.remove(itemIndex);
                        openLootGui(player, holder.data, holder.page);
                        manager.save();
                    } else {
                        player.sendMessage(FontUtils.parse("§c" + "ᴍáš ᴘʟɴý ɪɴᴠᴇɴᴛář."));
                    }
                }
            } else if (slot == 45 && holder.page > 0) {
                openLootGui(player, holder.data, holder.page - 1);
            } else if (slot == 53 && (holder.page + 1) * 45 < holder.data.loot.size()) {
                openLootGui(player, holder.data, holder.page + 1);
            } else if (slot == 48) {
                if (holder.data.loot.isEmpty()) {
                    player.sendMessage(FontUtils.parse("§c" + "ᴢᴅᴇ ɴᴇɴí žáᴅɴý ʟᴏᴏᴛ ᴋ ᴠʏʙʀáɴí."));
                    return;
                }
                for (ItemStack item : new ArrayList<>(holder.data.loot)) {
                    if (player.getInventory().firstEmpty() != -1) {
                        player.getInventory().addItem(item);
                        holder.data.loot.remove(item);
                    } else {
                        player.sendMessage(FontUtils.parse("§c" + "ɪɴᴠᴇɴᴛář ᴊᴇ ᴘʟɴý! ᴢʙýᴠᴀᴊíᴄí ᴘřᴇᴅᴍěᴛʏ ᴢůsᴛᴀʟʏ ᴠᴇ sᴘᴀᴡɴᴇʀᴜ."));
                        break;
                    }
                }
                openLootGui(player, holder.data, 0);
                manager.save();
            } else if (slot == 50) {
                if (holder.data.loot.isEmpty()) {
                    player.sendMessage(FontUtils.parse("§c" + "ᴢᴅᴇ ɴᴇɴí žáᴅɴý ʟᴏᴏᴛ ᴋ ᴠʏʜᴏᴢᴇɴí."));
                    return;
                }
                for (ItemStack item : holder.data.loot) {
                    holder.data.location.getWorld().dropItemNaturally(holder.data.location.clone().add(0.5, 1, 0.5), item);
                }
                holder.data.loot.clear();
                player.sendMessage(FontUtils.parse("&#00fbff" + "ᴠšᴇᴄʜᴇɴ ʟᴏᴏᴛ ʙʏʟ ᴠʏʜᴏᴢᴇɴ ɴᴀ ᴢᴇᴍ."));
                openLootGui(player, holder.data, 0);
                manager.save();
            }
        }
    }

    private void openLootGui(Player player, VirtualSpawnerManager.VirtualSpawnerData data, int page) {
        LootGuiHolder holder = new LootGuiHolder(data, page);
        Inventory inv = Bukkit.createInventory(holder, 54, FontUtils.parse("&#00fbffᴜsᴄʜᴏᴠᴀɴý ʟᴏᴏᴛ"));
        holder.setInventory(inv);

        ItemStack glass = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 45; i < 54; i++) inv.setItem(i, glass);

        int start = page * 45;
        for (int i = 0; i < 45 && start + i < data.loot.size(); i++) {
            inv.setItem(i, data.loot.get(start + i));
        }

        if (page > 0) {
            inv.setItem(45, createItem(Material.ARROW, "&#00fbffᴘřᴇᴅᴄʜᴏᴢí sᴛʀᴀɴᴀ"));
        }
        if ((page + 1) * 45 < data.loot.size()) {
            inv.setItem(53, createItem(Material.ARROW, "&#00fbffᴅᴀʟší sᴛʀᴀɴᴀ"));
        }

        inv.setItem(48, createItem(Material.HOPPER, "&#00ff44ᴠʏʙʀᴀᴛ ᴠšᴇᴄʜᴇɴ ʟᴏᴏᴛ", "§7ᴋʟɪᴋɴɪ ᴘʀᴏ ᴠʏʙʀáɴí ᴠšᴇᴄʜ ᴘřᴇᴅᴍěᴛů."));
        inv.setItem(50, createItem(Material.DISPENSER, "&#ffbb00ᴠʏʜᴏᴅɪᴛ ᴠšᴇᴄʜᴇɴ ʟᴏᴏᴛ", "§7ᴋʟɪᴋɴɪ ᴘʀᴏ ᴠʏʜᴏᴢᴇɴí ᴠšᴇᴄʜ ᴘřᴇᴅᴍěᴛů ɴᴀ ᴢᴇᴍ."));

        player.openInventory(inv);
    }

    private ItemStack createItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(FontUtils.parse(name));
        if (lore.length > 0) {
            List<Component> l = new ArrayList<>();
            for (String s : lore) l.add(FontUtils.parse(s));
            meta.lore(l);
        }
        item.setItemMeta(meta);
        return item;
    }

    public static class SpawnerGuiHolder implements InventoryHolder {
        public final VirtualSpawnerManager.VirtualSpawnerData data;
        private Inventory inventory;
        public SpawnerGuiHolder(VirtualSpawnerManager.VirtualSpawnerData data) { this.data = data; }
        public void setInventory(Inventory inventory) { this.inventory = inventory; }
        @Override public @NotNull Inventory getInventory() { return inventory; }
    }

    public static class LootGuiHolder implements InventoryHolder {
        public final VirtualSpawnerManager.VirtualSpawnerData data;
        public final int page;
        private Inventory inventory;
        public LootGuiHolder(VirtualSpawnerManager.VirtualSpawnerData data, int page) { this.data = data; this.page = page; }
        public void setInventory(Inventory inventory) { this.inventory = inventory; }
        @Override public @NotNull Inventory getInventory() { return inventory; }
    }

    public static class FilterGuiHolder implements InventoryHolder {
        public final VirtualSpawnerManager.VirtualSpawnerData data;
        private Inventory inventory;
        public FilterGuiHolder(VirtualSpawnerManager.VirtualSpawnerData data) { this.data = data; }
        public void setInventory(Inventory inventory) { this.inventory = inventory; }
        @Override public @NotNull Inventory getInventory() { return inventory; }
    }

    private void openFilterGui(Player player, VirtualSpawnerManager.VirtualSpawnerData data) {
        FilterGuiHolder holder = new FilterGuiHolder(data);
        Inventory inv = Bukkit.createInventory(holder, 54, FontUtils.parse("&#ffbb00ꜰɪʟᴛᴇʀ ᴘřᴇᴅᴍěᴛů"));
        holder.setInventory(inv);

        ItemStack glass = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 18; i < 27; i++) inv.setItem(i, glass);

        ItemStack book = createItem(Material.BOOK, "&#ffbb00ᴊᴀᴋ ꜰɪʟᴛᴇʀ ꜰᴜɴɢᴜᴊᴇ?",
                "§7ᴠ ʜᴏʀɴí čásᴛɪ ᴊsᴏᴜ ᴘᴏᴠᴏʟᴇɴé ᴘřᴇᴅᴍěᴛʏ.",
                "§7ᴠ ᴅᴏʟɴí čásᴛɪ ᴊsᴏᴜ ᴢᴀᴋáᴢᴀɴé ᴘřᴇᴅᴍěᴛʏ.",
                "",
                "§7ᴋʟɪᴋɴɪ ɴᴀ ᴘřᴇᴅᴍěᴛ ᴘʀᴏ ᴢᴍěɴᴜ sᴛᴀᴠᴜ.");
        inv.setItem(22, book);

        List<Material> possibleLoot = getPossibleLoot(data.type);
        int allowedIdx = 0;
        int blockedIdx = 27;

        for (Material mat : possibleLoot) {
            ItemStack item = new ItemStack(mat);
            ItemMeta meta = item.getItemMeta();
            if (data.blockedMaterials.contains(mat)) {
                meta.displayName(FontUtils.parse("§c" + "ᴢᴀᴋáᴢáɴᴏ: " + mat.name()));
                item.setItemMeta(meta);
                if (blockedIdx < 45) inv.setItem(blockedIdx++, item);
            } else {
                meta.displayName(FontUtils.parse("&#00ff44" + "ᴘᴏᴠᴏʟᴇɴᴏ: " + mat.name()));
                item.setItemMeta(meta);
                if (allowedIdx < 18) inv.setItem(allowedIdx++, item);
            }
        }

        player.openInventory(inv);
    }

    private List<Material> getPossibleLoot(EntityType type) {
        List<Material> materials = new ArrayList<>();
        switch (type) {
            case ZOMBIE -> materials.addAll(Arrays.asList(Material.ROTTEN_FLESH, Material.IRON_INGOT, Material.CARROT, Material.POTATO));
            case SKELETON -> materials.addAll(Arrays.asList(Material.BONE, Material.ARROW, Material.BOW));
            case SPIDER -> materials.addAll(Arrays.asList(Material.STRING, Material.SPIDER_EYE));
            case CREEPER -> materials.addAll(Arrays.asList(Material.GUNPOWDER, Material.TNT));
            case PIG -> materials.add(Material.PORKCHOP);
            case COW -> materials.addAll(Arrays.asList(Material.BEEF, Material.LEATHER));
            case CHICKEN -> materials.addAll(Arrays.asList(Material.CHICKEN, Material.FEATHER));
        }
        return materials;
    }

    public void openAdminGui(Player player, int page) {
        AdminGuiHolder holder = new AdminGuiHolder(page);
        Inventory inv = Bukkit.createInventory(holder, 54, FontUtils.parse("&#00fbffsᴘᴀᴡɴᴇʀ ᴍᴀɴᴀɢᴇᴍᴇɴᴛ"));
        holder.setInventory(inv);

        ItemStack glass = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 45; i < 54; i++) inv.setItem(i, glass);

        List<VirtualSpawnerManager.VirtualSpawnerData> list = new ArrayList<>(manager.getAllSpawners());
        int start = page * 45;
        for (int i = 0; i < 45 && start + i < list.size(); i++) {
            VirtualSpawnerManager.VirtualSpawnerData data = list.get(start + i);
            inv.setItem(i, createItem(Material.SPAWNER, "&#00fbff" + data.type.name(),
                "§7ʟᴏᴋᴀᴄᴇ: &#00fbff" + data.location.getWorld().getName() + " " + data.location.getBlockX() + " " + data.location.getBlockY() + " " + data.location.getBlockZ(),
                "§7sᴛᴀᴄᴋ: &#00fbff" + data.count + "x",
                "",
                "&#00ff44ʟᴇᴠý ᴋʟɪᴋ §7- ᴛᴇʟᴇᴘᴏʀᴛ",
                "&#EA427Fᴘʀᴀᴠý ᴋʟɪᴋ §7- sᴍᴀᴢᴀᴛ"
            ));
        }

        if (page > 0) inv.setItem(45, createItem(Material.ARROW, "&#00fbffᴘřᴇᴅᴄʜᴏᴢí sᴛʀᴀɴᴀ"));
        if ((page + 1) * 45 < list.size()) inv.setItem(53, createItem(Material.ARROW, "&#00fbffᴅᴀʟší sᴛʀᴀɴᴀ"));

        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof AdminGuiHolder || holder instanceof SpawnerGuiHolder || holder instanceof LootGuiHolder || holder instanceof FilterGuiHolder) {
            event.setCancelled(true);
        }
    }

    public static class AdminGuiHolder implements InventoryHolder {
        public final int page;
        private Inventory inventory;
        public AdminGuiHolder(int page) { this.page = page; }
        public void setInventory(Inventory inventory) { this.inventory = inventory; }
        @Override public @NotNull Inventory getInventory() { return inventory; }
    }
}
