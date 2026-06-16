package me.jules.vipmaker;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.logging.Logger;

public class VipMaker extends JavaPlugin {

    private static VipMaker instance;
    private Logger logger;
    private DataManager dataManager;
    private LuckPermsService lpService;

    @Override
    public void onEnable() {
        instance = this;
        this.logger = getLogger();

        // Save default config
        saveDefaultConfig();

        // Check for LuckPerms
        if (Bukkit.getPluginManager().getPlugin("LuckPerms") == null) {
            logger.severe("LuckPerms nebyl nalezen! Plugin se vypíná.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        this.lpService = new LuckPermsService();
        this.dataManager = new DataManager(this);
        this.dataManager.loadBlocks();

        // Register commands
        getCommand("setvip").setExecutor(new SetVipCommand(this));

        // Register listeners
        getServer().getPluginManager().registerEvents(new VipListener(this, dataManager, lpService), this);

        logger.info("VipMaker byl úspěšně zapnut!");
    }

    @Override
    public void onDisable() {
        logger.info("VipMaker byl vypnut!");
    }

    public static VipMaker getInstance() {
        return instance;
    }
}
