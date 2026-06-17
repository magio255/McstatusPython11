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
    private WarpManager warpManager;
    private ModuleManager moduleManager;
    private me.jules.magiocore.modules.IgnoreModule ignoreModule;
    private SettingsManager settingsManager;
    private SettingsGui settingsGui;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        moduleManager = new ModuleManager(this);
        settingsManager = new SettingsManager(this);

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

        if (moduleManager.isEnabled("home")) {
            homeManager = new HomeManager(this);
            homeGui = new HomeGui(this, homeManager);
            HomeCommands homeCommands = new HomeCommands(this, homeManager);
            getCommand("home").setExecutor(homeCommands);
            getCommand("home").setTabCompleter(homeCommands);
            getCommand("sethome").setExecutor(homeCommands);
            getCommand("sethome").setTabCompleter(homeCommands);
            getCommand("delhome").setExecutor(homeCommands);
            getCommand("delhome").setTabCompleter(homeCommands);
            getServer().getPluginManager().registerEvents(homeGui, this);
        }

        if (moduleManager.isEnabled("tpa")) {
            tpaManager = new TpaManager(this);
            TpaCommands tpaCommands = new TpaCommands(this, tpaManager);
            getCommand("tpa").setExecutor(tpaCommands);
            getCommand("tpa").setTabCompleter(tpaCommands);
            getCommand("tpahere").setExecutor(tpaCommands);
            getCommand("tpahere").setTabCompleter(tpaCommands);
            getCommand("tpacancel").setExecutor(tpaCommands);
            getCommand("tpaoff").setExecutor(tpaCommands);
            getCommand("tpaccept").setExecutor(tpaCommands);
            getCommand("tpadeny").setExecutor(tpaCommands);
        }

        if (moduleManager.isEnabled("spawn")) {
            SpawnCommands spawnCommands = new SpawnCommands(this);
            getCommand("spawn").setExecutor(spawnCommands);
            getCommand("setspawn").setExecutor(spawnCommands);
        }

        if (moduleManager.isEnabled("gamemode")) {
            GamemodeCommands gmCommands = new GamemodeCommands();
            getCommand("gmc").setExecutor(gmCommands);
            getCommand("gms").setExecutor(gmCommands);
            getCommand("gmsp").setExecutor(gmCommands);
            getCommand("gma").setExecutor(gmCommands);
        }

        if (moduleManager.isEnabled("rtp")) {
            RtpCommand rtpCommand = new RtpCommand(this);
            getCommand("rtp").setExecutor(rtpCommand);
            getCommand("rtp").setTabCompleter(rtpCommand);
            getServer().getPluginManager().registerEvents(rtpCommand, this);
        }

        if (moduleManager.isEnabled("flyspeed")) {
            FlySpeedCommand flySpeedCommand = new FlySpeedCommand();
            getCommand("flyspeed").setExecutor(flySpeedCommand);
            getServer().getPluginManager().registerEvents(flySpeedCommand, this);
        }

        if (moduleManager.isEnabled("playtime")) {
            PlaytimeCommand playtimeCommand = new PlaytimeCommand();
            getCommand("playtime").setExecutor(playtimeCommand);
        }

        if (moduleManager.isEnabled("invsee")) {
            InvseeCommand invseeCommand = new InvseeCommand();
            getCommand("invsee").setExecutor(invseeCommand);
        }

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new MagioCoreExpansion(this).register();
        }

        getServer().getPluginManager().registerEvents(new JoinListener(this), this);
        chatListener = new ChatListener(this);
        getServer().getPluginManager().registerEvents(chatListener, this);

        if (moduleManager.isEnabled("coinflip")) {
            coinflipManager = new CoinflipManager();
            coinflipGui = new CoinflipGui(this, coinflipManager);
            CoinflipCommand coinflipCommand = new CoinflipCommand(this, coinflipManager);
            getCommand("coinflip").setExecutor(coinflipCommand);
            getCommand("coinflip").setTabCompleter(coinflipCommand);
            getServer().getPluginManager().registerEvents(coinflipGui, this);
        }

        if (moduleManager.isEnabled("baltop")) {
            baltopManager = new BaltopManager(this);
            baltopGui = new BaltopGui(this, baltopManager);
            getCommand("baltop").setExecutor(new BaltopCommand(this));
            getServer().getPluginManager().registerEvents(baltopGui, this);
        }

        UtilityCommands utilityCommands = new UtilityCommands(this);
        if (moduleManager.isEnabled("utilities")) {
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
            getCommand("shardshop").setExecutor(utilityCommands);
        }

        if (moduleManager.isEnabled("dailyrewards") || moduleManager.isEnabled("playtimerewards")) {
            rewardManager = new RewardManager(this);
            dailyRewardGui = new DailyRewardGui(this, rewardManager);
            playtimeRewardGui = new PlaytimeRewardGui(this, rewardManager);
            RewardCommands rewardCommands = new RewardCommands(dailyRewardGui, playtimeRewardGui);

            if (moduleManager.isEnabled("dailyrewards")) {
                getCommand("dailyrewards").setExecutor(rewardCommands);
                getServer().getPluginManager().registerEvents(dailyRewardGui, this);
            }
            if (moduleManager.isEnabled("playtimerewards")) {
                getCommand("playtimerewards").setExecutor(rewardCommands);
                getServer().getPluginManager().registerEvents(playtimeRewardGui, this);
            }
        }

        if (moduleManager.isEnabled("virtualspawner")) {
            spawnerManager = new VirtualSpawnerManager(this);
            spawnerListener = new VirtualSpawnerListener(this, spawnerManager);
            VirtualSpawnerCommands spawnerCommands = new VirtualSpawnerCommands(this, spawnerManager);
            getCommand("ss").setExecutor(spawnerCommands);
            getCommand("ss").setTabCompleter(spawnerCommands);
            getCommand("virtualspawner").setExecutor(spawnerCommands);
            getCommand("virtualspawner").setTabCompleter(spawnerCommands);
            getServer().getPluginManager().registerEvents(spawnerListener, this);
        }

        if (moduleManager.isEnabled("vanish")) {
            vanishCommand = new VanishCommand(this);
            getCommand("vanish").setExecutor(vanishCommand);
            getServer().getPluginManager().registerEvents(vanishCommand, this);
        }

        ignoreModule = new me.jules.magiocore.modules.IgnoreModule(this);
        getCommand("ignore").setExecutor(ignoreModule);
        getCommand("tpaignore").setExecutor(ignoreModule);

        if (moduleManager.isEnabled("warp")) {
            warpManager = new WarpManager(this);
            WarpCommands warpCommands = new WarpCommands(this, warpManager);
            getCommand("warp").setExecutor(warpCommands);
            getCommand("warp").setTabCompleter(warpCommands);
            getCommand("setwarp").setExecutor(warpCommands);
            getCommand("delwarp").setExecutor(warpCommands);
            getCommand("delwarp").setTabCompleter(warpCommands);
        }

        MessageCommand messageCommand = new MessageCommand();
        getCommand("msg").setExecutor(messageCommand);
        getCommand("reply").setExecutor(messageCommand);

        if (moduleManager.isEnabled("itemedit")) {
            ItemEditCommand itemEditCommand = new ItemEditCommand();
            getCommand("itemedit").setExecutor(itemEditCommand);
            getCommand("itemedit").setTabCompleter(itemEditCommand);
            getServer().getPluginManager().registerEvents(new ItemEditListener(this), this);
        }

        getServer().getPluginManager().registerEvents(new CombatListener(this), this);
        getServer().getPluginManager().registerEvents(new DeathListener(), this);

        if (moduleManager.isEnabled("settings")) {
            settingsGui = new SettingsGui(this, settingsManager);
            getCommand("settings").setExecutor(settingsGui);
            getCommand("sb").setExecutor(settingsGui);
            getServer().getPluginManager().registerEvents(settingsGui, this);

            if (moduleManager.isEnabled("scoreboard")) {
                new ScoreboardTask(this).runTaskTimer(this, 20L, 20L);
            }
        }

        registerModules();

        if (moduleManager.isEnabled("afkzone")) {
            new AfkZoneTask(this).runTaskTimer(this, 20L, 20L); // Every second
        }

        getLogger().info("MagioCore has been enabled!");
    }

    private void registerModules() {
        if (moduleManager.isEnabled("autorestart")) {
            new me.jules.magiocore.modules.AutoRestartModule(this);
        }
        if (moduleManager.isEnabled("staffchat") || moduleManager.isEnabled("report")) {
            me.jules.magiocore.modules.StaffReportModule staffReportModule = new me.jules.magiocore.modules.StaffReportModule(this);
            if (getCommand("staffchat") != null) getCommand("staffchat").setExecutor(staffReportModule);
            if (getCommand("report") != null) getCommand("report").setExecutor(staffReportModule);
            if (getCommand("checkreport") != null) getCommand("checkreport").setExecutor(staffReportModule);
            if (getCommand("clearreports") != null) getCommand("clearreports").setExecutor(staffReportModule);
        }
        if (moduleManager.isEnabled("socials")) {
            me.jules.magiocore.modules.SocialsModule socialsModule = new me.jules.magiocore.modules.SocialsModule(this);
            if (getCommand("discord") != null) getCommand("discord").setExecutor(socialsModule);
            if (getCommand("store") != null) getCommand("store").setExecutor(socialsModule);
        }
        if (moduleManager.isEnabled("keyall")) {
            if (getCommand("keyall") != null) getCommand("keyall").setExecutor(new me.jules.magiocore.modules.KeyAllModule(this));
        }
        if (moduleManager.isEnabled("freeze")) {
            me.jules.magiocore.modules.FreezeModule freezeModule = new me.jules.magiocore.modules.FreezeModule(this);
            if (getCommand("freeze") != null) getCommand("freeze").setExecutor(freezeModule);
            if (getCommand("unfreeze") != null) getCommand("unfreeze").setExecutor(freezeModule);
            getServer().getPluginManager().registerEvents(freezeModule, this);
        }
        if (moduleManager.isEnabled("antigrief")) {
            getServer().getPluginManager().registerEvents(new me.jules.magiocore.modules.AntiGriefModule(this), this);
        }
    }

    public ModuleManager getModuleManager() {
        return moduleManager;
    }

    public me.jules.magiocore.modules.IgnoreModule getIgnoreModule() {
        return ignoreModule;
    }

    public SettingsManager getSettingsManager() {
        return settingsManager;
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
