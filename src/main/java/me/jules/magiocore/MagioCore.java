package me.jules.magiocore;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class MagioCore extends JavaPlugin implements Listener {
    private HomeManager homeManager;
    private HomeGui homeGui;
    private TpaManager tpaManager;
    private Economy econ = null;
    private CoinflipManager coinflipManager;
    private CoinflipGui coinflipGui;
    private BaltopManager baltopManager;
    private BaltopGui baltopGui;
    private ChatListener chatListener;
    private RewardManager rewardManager;
    private DailyRewardGui dailyRewardGui;
    private PlaytimeRewardGui playtimeRewardGui;
    private VirtualSpawnerManager spawnerManager;
    private VirtualSpawnerListener spawnerListener;
    private VanishCommand vanishCommand;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // Disable advancement announcements in all worlds
        for (World world : Bukkit.getWorlds()) {
            world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        }
        getServer().getPluginManager().registerEvents(this, this);

        if (!setupEconomy()) {
            getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        homeManager = new HomeManager(this);
        homeGui = new HomeGui(this, homeManager);
        tpaManager = new TpaManager(this);

        HomeCommands homeCommands = new HomeCommands(this, homeManager);
        getCommand("home").setExecutor(homeCommands);
        getCommand("home").setTabCompleter(homeCommands);
        getCommand("sethome").setExecutor(homeCommands);
        getCommand("sethome").setTabCompleter(homeCommands);

        TpaCommands tpaCommands = new TpaCommands(this, tpaManager);
        getCommand("tpa").setExecutor(tpaCommands);
        getCommand("tpa").setTabCompleter(tpaCommands);
        getCommand("tpahere").setExecutor(tpaCommands);
        getCommand("tpahere").setTabCompleter(tpaCommands);
        getCommand("tpacancel").setExecutor(tpaCommands);
        getCommand("tpaoff").setExecutor(tpaCommands);
        getCommand("tpaccept").setExecutor(tpaCommands);
        getCommand("tpadeny").setExecutor(tpaCommands);

        SpawnCommands spawnCommands = new SpawnCommands(this);
        getCommand("spawn").setExecutor(spawnCommands);
        getCommand("setspawn").setExecutor(spawnCommands);

        GamemodeCommands gmCommands = new GamemodeCommands();
        getCommand("gmc").setExecutor(gmCommands);
        getCommand("gms").setExecutor(gmCommands);
        getCommand("gmsp").setExecutor(gmCommands);
        getCommand("gma").setExecutor(gmCommands);

        RtpCommand rtpCommand = new RtpCommand(this);
        getCommand("rtp").setExecutor(rtpCommand);
        getCommand("rtp").setTabCompleter(rtpCommand);

        FlySpeedCommand flySpeedCommand = new FlySpeedCommand();
        getCommand("flyspeed").setExecutor(flySpeedCommand);

        PlaytimeCommand playtimeCommand = new PlaytimeCommand();
        getCommand("playtime").setExecutor(playtimeCommand);

        InvseeCommand invseeCommand = new InvseeCommand();
        getCommand("invsee").setExecutor(invseeCommand);

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new MagioCoreExpansion(this).register();
        }

        getServer().getPluginManager().registerEvents(homeGui, this);
        getServer().getPluginManager().registerEvents(rtpCommand, this);
        getServer().getPluginManager().registerEvents(flySpeedCommand, this);
        getServer().getPluginManager().registerEvents(new JoinListener(this), this);
        chatListener = new ChatListener(this);
        getServer().getPluginManager().registerEvents(chatListener, this);

        coinflipManager = new CoinflipManager();
        coinflipGui = new CoinflipGui(this, coinflipManager);
        CoinflipCommand coinflipCommand = new CoinflipCommand(this, coinflipManager);
        getCommand("coinflip").setExecutor(coinflipCommand);
        getCommand("coinflip").setTabCompleter(coinflipCommand);
        getServer().getPluginManager().registerEvents(coinflipGui, this);

        baltopManager = new BaltopManager(this);
        baltopGui = new BaltopGui(this, baltopManager);
        getCommand("baltop").setExecutor(new BaltopCommand(this));
        getServer().getPluginManager().registerEvents(baltopGui, this);

        UtilityCommands utilityCommands = new UtilityCommands(this);
        getCommand("broadcast").setExecutor(utilityCommands);
        getCommand("feed").setExecutor(utilityCommands);
        getCommand("feed").setTabCompleter(utilityCommands);
        getCommand("fly").setExecutor(utilityCommands);
        getCommand("fly").setTabCompleter(utilityCommands);
        getCommand("hat").setExecutor(utilityCommands);
        getCommand("heal").setExecutor(utilityCommands);
        getCommand("heal").setTabCompleter(utilityCommands);
        getCommand("repair").setExecutor(utilityCommands);
        getCommand("repair").setTabCompleter(utilityCommands);
        getCommand("suicide").setExecutor(utilityCommands);
        getCommand("ptime").setExecutor(utilityCommands);
        getCommand("ptime").setTabCompleter(utilityCommands);
        getCommand("pweather").setExecutor(utilityCommands);
        getCommand("pweather").setTabCompleter(utilityCommands);

        GuiUtilityCommands guiUtilityCommands = new GuiUtilityCommands();
        getCommand("anvil").setExecutor(guiUtilityCommands);
        getCommand("disposal").setExecutor(guiUtilityCommands);
        getCommand("grindstone").setExecutor(guiUtilityCommands);
        getCommand("loom").setExecutor(guiUtilityCommands);
        getCommand("smithingtable").setExecutor(guiUtilityCommands);
        getCommand("workbench").setExecutor(guiUtilityCommands);

        getCommand("afk").setExecutor(utilityCommands);
        getCommand("setafk").setExecutor(utilityCommands);
        getCommand("book").setExecutor(utilityCommands);
        getCommand("compass").setExecutor(utilityCommands);

        rewardManager = new RewardManager(this);
        dailyRewardGui = new DailyRewardGui(this, rewardManager);
        playtimeRewardGui = new PlaytimeRewardGui(this, rewardManager);

        spawnerManager = new VirtualSpawnerManager(this);
        spawnerListener = new VirtualSpawnerListener(this, spawnerManager);
        VirtualSpawnerCommands spawnerCommands = new VirtualSpawnerCommands(this, spawnerManager);
        getCommand("ss").setExecutor(spawnerCommands);
        getCommand("ss").setTabCompleter(spawnerCommands);
        getCommand("virtualspawner").setExecutor(spawnerCommands);
        getCommand("virtualspawner").setTabCompleter(spawnerCommands);
        getServer().getPluginManager().registerEvents(spawnerListener, this);

        vanishCommand = new VanishCommand(this);
        getCommand("vanish").setExecutor(vanishCommand);
        getServer().getPluginManager().registerEvents(vanishCommand, this);

        RewardCommands rewardCommands = new RewardCommands(dailyRewardGui, playtimeRewardGui);
        getCommand("dailyrewards").setExecutor(rewardCommands);
        getCommand("playtimerewards").setExecutor(rewardCommands);
        ItemEditCommand itemEditCommand = new ItemEditCommand();
        getCommand("itemedit").setExecutor(itemEditCommand);
        getCommand("itemedit").setTabCompleter(itemEditCommand);

        getServer().getPluginManager().registerEvents(dailyRewardGui, this);
        getServer().getPluginManager().registerEvents(playtimeRewardGui, this);
        getServer().getPluginManager().registerEvents(new ItemEditListener(this), this);
        getServer().getPluginManager().registerEvents(new CombatListener(this), this);
        getServer().getPluginManager().registerEvents(new DeathListener(), this);

        new AfkZoneTask(this).runTaskTimer(this, 20L, 20L); // Every second

        getLogger().info("MagioCore has been enabled!");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public Economy getEconomy() {
        return econ;
    }

    public CoinflipGui getCoinflipGui() {
        return coinflipGui;
    }

    public BaltopManager getBaltopManager() {
        return baltopManager;
    }

    public BaltopGui getBaltopGui() {
        return baltopGui;
    }

    public ChatListener getChatListener() {
        return chatListener;
    }

    public HomeGui getHomeGui() {
        return homeGui;
    }

    public VirtualSpawnerListener getSpawnerListener() {
        return spawnerListener;
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        event.getWorld().setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
    }

    @Override
    public void onDisable() {
        if (spawnerManager != null) {
            spawnerManager.save();
            spawnerManager.stopTask();
        }
        getLogger().info("MagioCore has been disabled!");
    }
}
