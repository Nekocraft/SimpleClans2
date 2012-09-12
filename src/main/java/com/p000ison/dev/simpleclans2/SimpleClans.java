/*
 * This file is part of SimpleClans2 (2012).
 *
 *     SimpleClans2 is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     SimpleClans2 is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with SimpleClans2.  If not, see <http://www.gnu.org/licenses/>.
 *
 *     Created: 02.09.12 18:33
 */


package com.p000ison.dev.simpleclans2;

import com.p000ison.dev.simpleclans2.clan.ClanManager;
import com.p000ison.dev.simpleclans2.clanplayer.ClanPlayerManager;
import com.p000ison.dev.simpleclans2.commands.CommandManager;
import com.p000ison.dev.simpleclans2.commands.commands.admin.BanCommand;
import com.p000ison.dev.simpleclans2.commands.commands.admin.DisbandCommand;
import com.p000ison.dev.simpleclans2.commands.commands.admin.GlobalFFCommand;
import com.p000ison.dev.simpleclans2.commands.commands.admin.ModDisbandCommand;
import com.p000ison.dev.simpleclans2.commands.commands.clan.*;
import com.p000ison.dev.simpleclans2.commands.commands.clan.bb.BBAddCommand;
import com.p000ison.dev.simpleclans2.commands.commands.clan.bb.BBClearCommand;
import com.p000ison.dev.simpleclans2.commands.commands.clan.bb.BBCommand;
import com.p000ison.dev.simpleclans2.commands.commands.clan.home.HomeCommand;
import com.p000ison.dev.simpleclans2.commands.commands.clan.home.HomeRegroupCommand;
import com.p000ison.dev.simpleclans2.commands.commands.clan.home.HomeSetCommand;
import com.p000ison.dev.simpleclans2.commands.commands.general.AlliancesCommand;
import com.p000ison.dev.simpleclans2.commands.commands.general.HelpCommand;
import com.p000ison.dev.simpleclans2.commands.commands.general.LeaderboardCommand;
import com.p000ison.dev.simpleclans2.commands.commands.general.ListCommand;
import com.p000ison.dev.simpleclans2.commands.commands.voting.AbstainCommand;
import com.p000ison.dev.simpleclans2.commands.commands.voting.DenyCommand;
import com.p000ison.dev.simpleclans2.data.DataManager;
import com.p000ison.dev.simpleclans2.database.Database;
import com.p000ison.dev.simpleclans2.database.DatabaseManager;
import com.p000ison.dev.simpleclans2.listeners.SCPlayerListener;
import com.p000ison.dev.simpleclans2.ranks.RankManager;
import com.p000ison.dev.simpleclans2.requests.RequestManager;
import com.p000ison.dev.simpleclans2.settings.SettingsManager;
import com.p000ison.dev.simpleclans2.support.PreciousStonesSupport;
import com.p000ison.dev.simpleclans2.teleportation.TeleportManager;
import com.p000ison.dev.simpleclans2.util.Announcer;
import com.p000ison.dev.simpleclans2.util.Logging;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

/**
 * Represents a SimpleClans
 */
public class SimpleClans extends JavaPlugin {

    private ClanManager clanManager;
    private DatabaseManager databaseManager;
    private ClanPlayerManager clanPlayerManager;
    private SettingsManager settingsManager;
    private RequestManager requestManager;
    private CommandManager commandManager;
    private DataManager dataManager;
    private RankManager rankManager;
    private Announcer announcer;
    private TeleportManager teleportManager;
    private PreciousStonesSupport preciousStonesSupport;
    private static Economy economy;


    @Override
    public void onEnable()
    {
        long startup = System.currentTimeMillis();

        new Logging(getLogger());
        new Language("en_EN");

        if (!setupEconomy()) {
            Logging.debug(Level.SEVERE, "Economy features disabled due to no Economy dependency found!");
        } else {
            Logging.debug("Hooked economy system: %s!", economy.getName());
        }

        Logging.debug("Loading managers...");
        loadManagers();
        Logging.debug("Loading the managers finished!");

        registerEvents();

        new Announcer(this);

        long finish = System.currentTimeMillis();

        Logging.debug(String.format("Enabling took %s ms", finish - startup));
    }


    @Override
    public void onDisable()
    {
        databaseManager.getDatabase().close();

        economy = null;

        Language.clear();
        Logging.close();
    }

    private void registerEvents()
    {
        PluginManager pm = getServer().getPluginManager();

        pm.registerEvents(new SCPlayerListener(this), this);
    }

    private boolean setupEconomy()
    {

        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }

        economy = rsp.getProvider();
        return economy != null;
    }


    private void loadManagers()
    {
        settingsManager = new SettingsManager(this);
        databaseManager = new DatabaseManager(this);
        clanManager = new ClanManager(this);
        clanPlayerManager = new ClanPlayerManager(this);
        dataManager = new DataManager(this);
        requestManager = new RequestManager();
        announcer = new Announcer(this);
        teleportManager = new TeleportManager(this);
        rankManager = new RankManager(this);
        preciousStonesSupport = new PreciousStonesSupport(this);
        setupCommands();
    }

    private void setupCommands()
    {
        commandManager = new CommandManager(this);

        commandManager.addCommand(new ListCommand(this));
        commandManager.addCommand(new CreateCommand(this));
        commandManager.addCommand(new AlliancesCommand(this));
        commandManager.addCommand(new AllyCommand(this));
        commandManager.addCommand(new CreateCommand(this));
        commandManager.addCommand(new DenyCommand(this));
        commandManager.addCommand(new AbstainCommand(this));
        commandManager.addCommand(new RankCreateCommand(this));
        commandManager.addCommand(new BBAddCommand(this));
        commandManager.addCommand(new BBClearCommand(this));
        commandManager.addCommand(new BBCommand(this));
        commandManager.addCommand(new CoordsCommand(this));
        commandManager.addCommand(new DemoteCommand(this));
        commandManager.addCommand(new BanCommand(this));
        commandManager.addCommand(new HelpCommand(this));
        commandManager.addCommand(new HomeCommand(this));
        commandManager.addCommand(new HomeRegroupCommand(this));
        commandManager.addCommand(new HomeSetCommand(this));
        commandManager.addCommand(new GlobalFFCommand(this));
        commandManager.addCommand(new ModDisbandCommand(this));
        commandManager.addCommand(new DisbandCommand(this));
        commandManager.addCommand(new LeaderboardCommand(this));
        commandManager.addCommand(new KickCommand(this));
        commandManager.addCommand(new InviteCommand(this));
    }

    @Override
    public boolean onCommand(org.bukkit.command.CommandSender sender, org.bukkit.command.Command command, java.lang.String label, java.lang.String[] args)
    {
        commandManager.executeAll(null, sender, command.getName(), label, args);
        return true;
    }

    public Database getSimpleClansDatabase()
    {
        if (databaseManager == null) {
            return null;
        }
        return databaseManager.getDatabase();
    }

    public DatabaseManager getDatabaseManager()
    {
        return databaseManager;
    }

    public ClanManager getClanManager()
    {
        return clanManager;
    }

    public ClanPlayerManager getClanPlayerManager()
    {
        return clanPlayerManager;
    }

    public SettingsManager getSettingsManager()
    {
        return settingsManager;
    }

    public RequestManager getRequestManager()
    {
        return requestManager;
    }

    public CommandManager getCommandManager()
    {
        return commandManager;
    }

    public DataManager getDataManager()
    {
        return dataManager;
    }

    public static boolean withdrawBalance(String player, double balance)
    {
        return economy.withdrawPlayer(player, balance).transactionSuccess();
    }

    public static void depositBalance(String player, double balance)
    {
        economy.withdrawPlayer(player, balance);
    }

    public static boolean hasEconomy()
    {
        return economy != null;
    }

    public static Economy getEconomy()
    {
        return economy;
    }

    public RankManager getRankManager()
    {
        return rankManager;
    }

    public Announcer getAnnouncer()
    {
        return announcer;
    }

    public PreciousStonesSupport getPreciousStonesSupport()
    {
        return preciousStonesSupport;
    }

    public TeleportManager getTeleportManager()
    {
        return teleportManager;
    }
}
