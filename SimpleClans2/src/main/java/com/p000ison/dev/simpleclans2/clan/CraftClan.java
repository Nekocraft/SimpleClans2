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
 *     Last modified: 1/9/13 9:44 PM
 */


package com.p000ison.dev.simpleclans2.clan;

import com.p000ison.dev.simpleclans2.SimpleClans;
import com.p000ison.dev.simpleclans2.api.Balance;
import com.p000ison.dev.simpleclans2.api.RelationType;
import com.p000ison.dev.simpleclans2.api.chat.ChatBlock;
import com.p000ison.dev.simpleclans2.api.clan.Clan;
import com.p000ison.dev.simpleclans2.api.clan.ClanFlags;
import com.p000ison.dev.simpleclans2.api.clanplayer.ClanPlayer;
import com.p000ison.dev.simpleclans2.api.clanplayer.OnlineClanPlayer;
import com.p000ison.dev.simpleclans2.api.events.ClanRelationBreakEvent;
import com.p000ison.dev.simpleclans2.api.events.ClanRelationCreateEvent;
import com.p000ison.dev.simpleclans2.api.rank.Rank;
import com.p000ison.dev.simpleclans2.clan.ranks.CraftRank;
import com.p000ison.dev.simpleclans2.clanplayer.CraftClanPlayer;
import com.p000ison.dev.simpleclans2.database.response.responses.BBAddResponse;
import com.p000ison.dev.simpleclans2.language.Language;
import com.p000ison.dev.simpleclans2.util.DateHelper;
import com.p000ison.dev.simpleclans2.util.GeneralHelper;
import com.p000ison.dev.simpleclans2.util.JSONUtil;
import com.p000ison.dev.sqlapi.TableObject;
import com.p000ison.dev.sqlapi.annotation.DatabaseColumn;
import com.p000ison.dev.sqlapi.annotation.DatabaseColumnGetter;
import com.p000ison.dev.sqlapi.annotation.DatabaseColumnSetter;
import com.p000ison.dev.sqlapi.annotation.DatabaseTable;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.*;

/**
 * Represents a Clan
 */
@DatabaseTable(name = "sc2_clans")
public class CraftClan implements Clan, TableObject {

    public static final NumberFormat DECIMAL_FORMAT = new DecimalFormat("#.#");
    private static final long serialVersionUID = 2276260953605541164L;

    private transient SimpleClans plugin;
    private CraftClanFlags flags;
    private BankAccount bank;

    @DatabaseColumn(position = 0, databaseName = "id", id = true)
    private long id = -1;

    @DatabaseColumn(position = 1, databaseName = "tag", notNull = true, lenght = 26, unique = true)
    private String tag;
    @DatabaseColumn(position = 2, databaseName = "name", notNull = true, lenght = 100, unique = true)
    private String name;

    private long foundedDate;
    private long lastActionDate;
    private boolean verified;

    private Set<Clan> allies;
    private Set<Clan> rivals;
    private Set<Clan> warring;
    private Set<ClanPlayer> allMembers;
    private Set<Rank> ranks;

    private boolean update;

    /**
     * This is called by the SQLDatabaseAPI to build a clan
     *
     * @param plugin The plugin
     */
    public CraftClan(SimpleClans plugin) {
        flags = new CraftClanFlags();
        this.plugin = plugin;
    }

    /**
     * Creates a new clan
     *
     * @param plugin The plugin
     * @param tag    The tag of this clan
     * @param name   The full name of this clan
     */
    public CraftClan(SimpleClans plugin, String tag, String name) {
        this(plugin);
        flags = new CraftClanFlags();
        setTag(tag);
        setName(name);
    }

    public CraftClan() {
    }

    /**
     * Returns a formated string of the date this clan was founded
     *
     * @return Returns a formatted date
     */
    @Override
    public String getFoundedDateFormatted() {
        return new java.text.SimpleDateFormat("MMM dd, yyyy h:mm a").format(new Date(this.foundedDate));
    }

    /**
     * Gets the tag of a clan. This is unique and can contain colors.
     *
     * @return The tag.
     */
    @Override
    public String getTag() {
        return tag;
    }

    /**
     * Sets the tag of this clan
     *
     * @param tag The tag
     */
    @Override
    public void setTag(String tag) {
        Validate.notNull(tag, "The clan tag must not be null!");
        this.tag = tag;
    }

    /**
     * Gets the unique id of this clan.
     *
     * @return The id.
     */
    @Override
    public long getID() {
        return id;
    }

    /**
     * Returns the JSONFlags of this clan. {@link com.p000ison.dev.simpleclans2.api.clan.ClanFlags} contains the flags of this clan.
     *
     * @return The flags of this clan.
     * @see com.p000ison.dev.simpleclans2.api.clan.ClanFlags
     */
    @Override
    public ClanFlags getFlags() {
        return flags;
    }

    /**
     * Gets the name of this clan.
     *
     * @return The name of this clan.
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this clan
     *
     * @param name The name of this clan
     */
    @Override
    public void setName(String name) {
        Validate.notNull(tag, "The clan name must not be null!");
        this.name = name;
    }

    /**
     * Gets the time in milliseconds in the unix time
     *
     * @return The time
     */
    @Override
    @DatabaseColumnGetter(databaseName = "last_action")
    public Date getLastActionDate() {
        return new Date(lastActionDate);
    }

    /**
     * Sets the date when the last action happened.
     *
     * @param lastActionDate The date when the last action happened.
     */
    @DatabaseColumnSetter(position = 5, databaseName = "last_action")
    public void setLastActionDate(Date lastActionDate) {
        if (lastActionDate == null) {
            this.lastActionDate = System.currentTimeMillis();
            return;
        }
        this.lastActionDate = lastActionDate.getTime();
    }

    public void setLastAction(long lastActionDate) {
        this.lastActionDate = lastActionDate;
    }

    /**
     * Updates the last action to NOW!
     */
    @Override
    public void updateLastAction() {
        this.lastActionDate = System.currentTimeMillis();
    }

    /**
     * Returns the date then this clan was founded in milliseconds in the unix time.
     *
     * @return The found date,
     */
    @Override
    @DatabaseColumnGetter(databaseName = "founded")
    public Date getFoundedDate() {
        return new Date(foundedDate);
    }

    /**
     * Sets when the clan was founded.
     *
     * @param foundedDate The time when it was founded.
     */
    @DatabaseColumnSetter(position = 4, databaseName = "founded")
    public void setFoundedDate(Date foundedDate) {
        if (foundedDate == null) {
            this.foundedDate = System.currentTimeMillis();
            return;
        }
        this.foundedDate = foundedDate.getTime();
    }

    public void setFoundedTime(long foundedDate) {
        this.foundedDate = foundedDate;
    }

    public long getFoundedTime() {
        return this.foundedDate;
    }

    /**
     * Checks if this clan is verified.
     *
     * @return Weather this clan is verified.
     */
    @Override
    @DatabaseColumnGetter(databaseName = "verified")
    public boolean isVerified() {
        return verified;
    }

    /**
     * Sets this clan verified.
     *
     * @param verified Weather this clan should be verified
     */
    @Override
    @DatabaseColumnSetter(position = 3, databaseName = "verified", defaultValue = "0")
    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    /**
     * Checks if friendly fire is on.
     *
     * @return Weather friendly fire is on of of.
     */
    @Override
    public boolean isFriendlyFireOn() {
        return getFlags().isFriendlyFireEnabled();
    }

    /**
     * Sets friendly fire for this clan.
     *
     * @param friendlyFire Weather friendly fire is on or off.
     */
    @Override
    public void setFriendlyFire(boolean friendlyFire) {
        getFlags().setFriendlyFire(friendlyFire);
    }

    /**
     * Returns a set of allies
     *
     * @return The allies.
     */
    @Override
    public Set<Clan> getAllies() {
        return allies == null ? new HashSet<Clan>() : Collections.unmodifiableSet(allies);
    }

    /**
     * Returns a set of rivals
     *
     * @return The rival clans.
     */
    @Override
    public Set<Clan> getRivals() {
        return rivals == null ? new HashSet<Clan>() : Collections.unmodifiableSet(rivals);
    }

    /**
     * Returns a set of warring clans
     *
     * @return The warring clans.
     */
    @Override
    public Set<Clan> getWarringClans() {
        return warring == null ? new HashSet<Clan>() : Collections.unmodifiableSet(warring);
    }

    /**
     * Checks weather the clan is a ally.
     *
     * @param id The id of the other clan.
     * @return Weather they are allies or not.
     */
    @Override
    public boolean isAlly(long id) {
        if (this.getID() == id) {
            return true;
        }

        if (allies == null) {
            return false;
        }

        for (Clan clan : allies) {
            if (clan.getID() == id) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks weather the clan  is a ally.
     *
     * @param clan The other clan
     * @return Weather they are allies or not.
     */
    @Override
    public boolean isAlly(Clan clan) {
        return clan != null && (this.equals(clan) || (allies != null && allies.contains(clan)));
    }

    /**
     * Checks weather the clan with the id is a rival.
     *
     * @param id The id of the other clan.
     * @return Weather they are rivals or not.
     */
    @Override
    public boolean isRival(long id) {
        if (this.getID() == id) {
            return false;
        }

        if (rivals == null) {
            return false;
        }

        for (Clan clan : rivals) {
            if (clan.getID() == id) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks weather the clan is a rival.
     *
     * @param clan The other clan.
     * @return Weather they are rivals or not.
     */
    @Override
    public boolean isRival(Clan clan) {
        return clan != null && (this.equals(clan) || (rivals != null && rivals.contains(clan)));
    }

    /**
     * Checks weather the clan is warring with this one.
     *
     * @param id The id of the other clan.
     * @return Weather they are warring or not.
     */
    @Override
    public boolean isWarring(long id) {
        if (this.getID() == id) {
            return false;
        }

        if (warring == null) {
            return false;
        }

        for (Clan clan : warring) {
            if (clan.getID() == id) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks weather the clan is warring with this one.
     *
     * @param clan The other clan.
     * @return Weather they are warring or not.
     */
    @Override
    public boolean isWarring(Clan clan) {
        return clan != null && (this.equals(clan) || (warring != null && warring.contains(clan)));
    }

    /**
     * Gets the days this clan is inactive
     *
     * @return The days this clan is inactive
     */
    @Override
    public int getInactiveDays() {
        return (int) Math.round(DateHelper.differenceInDays(lastActionDate, System.currentTimeMillis()));
    }

    /**
     * Sets the leader of this clan
     *
     * @param clanPlayer The player to set
     * @return Weather it was successfully
     */
    @Override
    public boolean setLeader(ClanPlayer clanPlayer) {
        if (clanPlayer.getClanID() != id) {
            return false;
        }

        clanPlayer.setLeader(true);
        return true;
    }

    /**
     * Demotes a leader to a normal player
     *
     * @param clanPlayer The player to demote
     * @return Weather it was successfully
     */
    @Override
    public boolean demote(ClanPlayer clanPlayer) {

        if (clanPlayer.getClanID() != id) {
            return false;
        }

        clanPlayer.setLeader(false);
        return true;
    }

    /**
     * Checks if the player is a member of this clan. This means no leader.
     *
     * @param cp The player
     * @return Checks if the player is a member
     */
    @Override
    public boolean isMember(ClanPlayer cp) {
        if (allMembers == null) {
            return false;
        }

        for (ClanPlayer comparePlayer : allMembers) {

            if (!comparePlayer.equals(cp)) {
                continue;
            }

            if (cp.getClanID() == id && !cp.isLeader()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if the player the leader of this clan.
     *
     * @param cp The player
     * @return Checks if the player is the leader
     */
    @Override
    public boolean isLeader(ClanPlayer cp) {
        return cp.getClanID() == id && cp.isLeader();
    }

    /**
     * Checks if the player is any member of this clan. This means member/leader
     *
     * @param cp The player
     * @return Checks if the player is any member
     */
    @Override
    public boolean isAnyMember(ClanPlayer cp) {
        return allMembers != null && allMembers.contains(cp);
    }

    /**
     * Gets all the members excluded the leader
     *
     * @return A Set of the members
     */
    @Override
    public Set<ClanPlayer> getMembers() {
        Set<ClanPlayer> members = new HashSet<ClanPlayer>();

        for (ClanPlayer cp : getAllMembers()) {
            if (cp.getClanID() == id && !cp.isLeader()) {
                members.add(cp);
            }
        }

        return Collections.unmodifiableSet(members);
    }

    /**
     * Returns all members including the leader
     *
     * @return A set of all members of this clan
     */
    @Override
    public Set<ClanPlayer> getAllMembers() {
        return allMembers == null ? Collections.unmodifiableSet(new HashSet<ClanPlayer>()) : Collections.unmodifiableSet(allMembers);
    }

    /**
     * Returns a set of all leaders
     *
     * @return A Set of all leaders
     */
    @Override
    public Set<ClanPlayer> getLeaders() {
        Set<ClanPlayer> leaders = new HashSet<ClanPlayer>();

        for (ClanPlayer cp : getAllMembers()) {
            if (cp.isLeader()) {
                leaders.add(cp);
            }
        }

        return Collections.unmodifiableSet(leaders);
    }

    /**
     * Adds a player as member to this clan
     *
     * @param clanPlayer The player
     */
    @Override
    public void addMember(ClanPlayer clanPlayer) {
        Clan previous = clanPlayer.getClan();

        if (previous != null && isVerified()) {
            String pastClan = previous.getTag();

            if (clanPlayer.isLeader()) {
                pastClan += '*';
            }

            clanPlayer.addPastClan(pastClan);
        }

        if (plugin.getSettingsManager().isTrustMembersByDefault()) {
            clanPlayer.setTrusted(true);
        }

        if (allMembers == null) {
            allMembers = new HashSet<ClanPlayer>();
        }

        allMembers.add(clanPlayer);
        clanPlayer.setClan(this);
        ((CraftClanPlayer) clanPlayer).updatePermissions();
        clanPlayer.update();
    }

    /**
     * Gets the total kdr of all members
     *
     * @return The total KDR
     */
    @Override
    public float getKDR() {
        double totalWeightedKills = 0;
        int totalDeaths = 0;

        for (ClanPlayer member : getAllMembers()) {
            totalWeightedKills += member.getWeightedKills();
            totalDeaths += member.getDeaths();
        }

        if (totalDeaths == 0) {
            totalDeaths = 1;
        }

        return ((float) totalWeightedKills) / ((float) totalDeaths);
    }

    /**
     * The size of this clan
     *
     * @return The total size of this clan
     */
    @Override
    public int getSize() {
        return allMembers == null ? 0 : allMembers.size();
    }

    /**
     * Adds a message to the bb
     *
     * @param announcer The announcer of this message
     * @param msg       The message which should be posted
     */
    @Override
    public void addBBMessage(ClanPlayer announcer, String msg) {
        announce(announcer, msg);
        addBBRawMessage(ChatBlock.parseColors(plugin.getSettingsManager().getClanPlayerBB().replace("+player", announcer.getName()).replace("+message", msg)));
    }

    /**
     * Adds a message to the bb
     *
     * @param announcer The announcer clan of this message
     * @param msg       The message which should be posted
     */
    @Override
    public void addBBMessage(Clan announcer, String msg) {
        msg = ChatBlock.parseColors(plugin.getSettingsManager().getClanBB().replace("+clan", announcer.getTag()).replace("+message", msg));
        announce(msg);
        addBBRawMessage(msg);
    }

    /**
     * Announces a message to all clan members
     *
     * @param announcer The announcer of this message
     * @param msg       The message which should be announced
     */
    @Override
    public void announce(ClanPlayer announcer, String msg) {
        announceRaw(ChatBlock.parseColors(plugin.getSettingsManager().getClanPlayerAnnounce().replace("+player", announcer.getName()).replace("+message", msg)));
    }

    /**
     * Announces a message to all clan members
     *
     * @param announcer The announcer clan of this message
     * @param msg       The message which should be announced
     */
    @Override
    public void announce(Clan announcer, String msg) {
        announceRaw(ChatBlock.parseColors(plugin.getSettingsManager().getClanAnnounce().replace("+clan", announcer.getTag()).replace("+message", msg)));
    }

    /**
     * Announces a message to all clan members
     *
     * @param msg The message which should be announced
     */
    @Override
    public void announce(String msg) {
        announceRaw(ChatBlock.parseColors(plugin.getSettingsManager().getDefaultAnnounce().replace("+message", msg)));
    }

    /**
     * announces a message to all clan players raw
     * <p/>
     * <p><strong>Internally used!</strong></p>
     *
     * @param message The message
     */
    private void announceRaw(String message) {
        if (message == null) {
            return;
        }

        for (ClanPlayer clanPlayer : getAllMembers()) {
            Player player = clanPlayer.toPlayer();
            if (player != null) {
                ChatBlock.sendMessage(player, message);
            }
        }
    }

    /**
     * Gets a clean comparable tag of this clan
     *
     * @return The clean tag
     */
    @Override
    public String getCleanTag() {
        return ChatColor.stripColor(tag.toLowerCase(Locale.US));
    }

    @Override
    public void addBBMessage(String msg) {
        addBBRawMessage(ChatBlock.parseColors(plugin.getSettingsManager().getDefaultBB().replace("+message", msg)));
    }

    private void addBBRawMessage(String message) {
        plugin.getDataManager().addResponse(new BBAddResponse(plugin, message, this));
    }

    @Override
    public void clearBB() {
        plugin.getDataManager().purgeBB(this);
    }

    @Override
    public boolean equals(Object otherClan) {
        if (this == otherClan) {
            return true;
        }

        if (otherClan == null || !(otherClan instanceof Clan)) {
            return false;
        }

        Clan clan = (Clan) otherClan;

        return id == clan.getID();
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public void addRival(Clan rival) {
        if (rivals == null) {
            rivals = new HashSet<Clan>();
        }
        addRelation(RelationType.RIVAL, rivals, rival);
    }

    @Override
    public void addAlly(Clan ally) {
        if (allies == null) {
            allies = new HashSet<Clan>();
        }
        addRelation(RelationType.ALLY, allies, ally);
    }

    @Override
    public void addWarringClan(Clan warringClan) {
        if (warring == null) {
            warring = new HashSet<Clan>();
        }
        addRelation(RelationType.WAR, warring, warringClan);
    }

    private void addRelation(RelationType relationType, Set<Clan> relationSet, Clan clanToAdd) {
        ClanRelationCreateEvent relationEvent = new ClanRelationCreateEvent(this, clanToAdd, relationType);

        plugin.getServer().getPluginManager().callEvent(relationEvent);

        if (relationEvent.isCancelled()) {
            return;
        }

        relationSet.add(clanToAdd);
    }

    @Override
    public void removeRival(Clan rival) {
        removeRelation(RelationType.RIVAL, rivals, rival);
    }

    @Override
    public void removeAlly(Clan ally) {
        removeRelation(RelationType.ALLY, allies, ally);
    }

    @Override
    public void removeWarringClan(Clan warringClan) {
        removeRelation(RelationType.WAR, warring, warringClan);
    }

    private void removeRelation(RelationType relationType, Set<Clan> relationSet, Clan clanToRemove) {
        if (relationSet == null) {
            return;
        }

        ClanRelationBreakEvent relationEvent = new ClanRelationBreakEvent(this, clanToRemove, relationType);

        plugin.getServer().getPluginManager().callEvent(relationEvent);

        if (relationEvent.isCancelled()) {
            return;
        }

        relationSet.remove(clanToRemove);
    }

    /**
     * Removes a member from this clan
     *
     * @param clanPlayer The member to remove
     */
    @Override
    public void removeMember(ClanPlayer clanPlayer) {
        if (allMembers == null) {
            return;
        }

        if (allMembers.remove(clanPlayer)) {
            clanPlayer.unset();
            clanPlayer.update();
            if (clanPlayer.isLeader()) {
                disband();
            }
            plugin.getRequestManager().clearRequests(clanPlayer);
        }
    }

    /**
     * Disbands this (performs all necessary steps)
     */
    @Override
    public void disband() {
        if (allMembers != null) {
            for (ClanPlayer clanPlayer : allMembers) {
                clanPlayer.unset();

                String pastClan = getTag();

                if (clanPlayer.isLeader()) {
                    pastClan += '*';
                }

                clanPlayer.addPastClan(pastClan);
                clanPlayer.update();
            }
        }

        if (warring != null) {
            for (Clan warringClan : warring) {
                warringClan.removeWarringClan(this);
                warringClan.addBBMessage(this, Language.getTranslation("you.are.no.longer.at.war", this.getTag(), warringClan.getTag()));
            }
        }

        if (allies != null) {
            for (Clan allyClan : allies) {
                allyClan.removeAlly(this);
                allyClan.addBBMessage(this, Language.getTranslation("has.been.disbanded.alliance.ended", this.getTag()));
            }
        }

        if (rivals != null) {
            for (Clan rivalClan : rivals) {
                rivalClan.removeRival(this);
                rivalClan.addBBMessage(this, Language.getTranslation("has.been.disbanded.rivalry.ended", this.getTag()));
            }
        }

        plugin.getRequestManager().clearRequests(this);
        plugin.getClanManager().removeClan(this);

        plugin.getDataManager().getDatabase().delete(this);
    }

    /**
     * Checks if this clan has allies
     *
     * @return Weather this clan has allies
     */
    @Override
    public boolean hasAllies() {
        return allies != null && !allies.isEmpty();
    }

    /**
     * Checks if this clan has rivals
     *
     * @return Weather this clan has rivals
     */
    @Override
    public boolean hasRivals() {
        return rivals != null && !rivals.isEmpty();
    }

    /**
     * Checks if this clan has warring clans
     *
     * @return Weather this clan has warring clans
     */
    @Override
    public boolean hasWarringClans() {
        return warring != null && !warring.isEmpty();
    }

    /**
     * Checks if all leaders are online
     *
     * @return Weather all players are online
     */
    @Override
    public boolean allLeadersOnline() {
        return allLeadersOnline(null);
    }

    /**
     * Checks if all leaders are online
     *
     * @param ignore Null or a player to ignore
     * @return Weather all players are online
     */
    @Override
    public boolean allLeadersOnline(ClanPlayer ignore) {
        for (ClanPlayer clanPlayer : getLeaders()) {
            if (ignore != null && clanPlayer.equals(ignore)) {
                continue;
            }

            if (clanPlayer.toPlayer() == null) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if this clan needs a update
     *
     * @return Weather this clan needs a update
     */
    public boolean needsUpdate() {
        return update;
    }

    /**
     * Marks this clan to update
     */
    @Override
    public void update() {
        this.update = true;
    }

    /**
     * Marks this clan to update
     *
     * @param update Weather to update
     */
    @Override
    public void update(boolean update) {
        this.update = update;
    }

    @Override
    public long getLastUpdated() {
        return lastActionDate;
    }

    /**
     * This return a array of kills this clan made. This needs only one iteration.
     * <p/>
     * <p><strong>Total Deaths: </strong><i>Index 0</i></p>
     * <p><strong>Total Rival Kills: </strong><i>Index 1</i></p>
     * <p><strong>Total Civilian Kills: </strong><i>Index 2</i></p>
     * <p><strong>Total Neutral Kills: </strong><i>Index 3</i></p>
     *
     * @return A array of the kills of this clan from index 0 - 3
     */
    @Override
    public int[] getTotalKills() {
        int totalDeaths = 0;
        int totalRivalKills = 0;
        int totalCivilianKills = 0;
        int totalNeutralKills = 0;

        for (ClanPlayer clanPlayer : getAllMembers()) {
            totalDeaths += clanPlayer.getDeaths();
            totalRivalKills += clanPlayer.getRivalKills();
            totalCivilianKills += clanPlayer.getCivilianKills();
            totalNeutralKills += clanPlayer.getNeutralKills();
        }

        return new int[]{totalDeaths, totalRivalKills, totalCivilianKills, totalCivilianKills, totalNeutralKills};
    }

    /**
     * Adds a rank to this clan
     *
     * @param rank The rank to add
     */
    @Override
    public void addRank(Rank rank) {
        if (rank == null) {
            return;
        }
        if (ranks == null) {
            ranks = new HashSet<Rank>();
        }

        ranks.add(rank);
    }

    /**
     * Removes a rank and removes them also from the players
     *
     * @param rank The rank to look for
     * @return Weather is was successfully
     */
    @Override
    public long deleteRank(Rank rank) {
        if (rank == null || ranks == null) {
            return -1;
        }

        long id = rank.getID();

        if (ranks.remove(rank)) {
            for (ClanPlayer member : allMembers) {
                if (member.getRank().equals(rank)) {
                    member.assignRank(null);
                    member.update();
                }
            }
            return id;
        }

        return -1;
    }

    /**
     * Searches for a rank and removes it.
     *
     * @param tag The search query
     * @return Weather it was successfully
     */
    @Override
    public long deleteRank(String tag) {
        if (ranks == null) {
            return -1;
        }

        Iterator<Rank> it = ranks.iterator();

        while (it.hasNext()) {
            Rank rank = it.next();
            if (rank.getTag().startsWith(tag)) {
                long id = rank.getID();
                for (ClanPlayer member : allMembers) {
                    Rank memberRank = member.getRank();
                    if (memberRank != null) {
                        if (memberRank.equals(rank)) {
                            member.assignRank(null);
                            member.update();
                        }
                    }
                }
                it.remove();
                return id;
            }
        }
        return -1;
    }

    /**
     * Gets a set of all ranks of this clan
     *
     * @return All ranks of this clan
     */
    @Override
    public Set<Rank> getRanks() {
        return ranks == null ? Collections.unmodifiableSet(new HashSet<Rank>()) : Collections.unmodifiableSet(ranks);
    }

    /**
     * Loads the ranks for this clan
     *
     * @param ranks The ranks to load
     */
    public void loadRanks(Set<CraftRank> ranks) {
        if (this.ranks == null) {
            this.ranks = new HashSet<Rank>();
        }
        this.ranks.addAll(ranks);
    }

    /**
     * Returns a rank of this clan
     *
     * @param id The id to look for
     * @return The rank
     */
    @Override
    public Rank getRank(long id) {
        if (ranks == null) {
            return null;
        }

        for (Rank rank : ranks) {
            if (rank.getID() == id) {
                return rank;
            }
        }

        return null;
    }

    /**
     * Searches for a rank saved in this clan
     *
     * @param query The search query
     * @return The rank
     */
    @Override
    public Rank getRank(String query) {
        if (ranks == null) {
            return null;
        }

        String cleanQuery = query.toLowerCase(Locale.US);
        for (Rank rank : ranks) {
            if (rank.getTag().toLowerCase(Locale.US).startsWith(cleanQuery)) {
                return rank;
            }
        }

        return null;
    }

    /**
     * Turns the most important information about this clan into a string
     *
     * @return A string with information about this clan
     */
    @Override
    public String toString() {
        return "Clan{" +
                "id=" + id +
                ", tag='" + tag + '\'' +
                ", name='" + name + '\'' +
                ", lastActionDate=" + lastActionDate +
                ", verified=" + verified +
                ", update=" + update +
                '}';
    }

    /**
     * Compares this clan to another clan based on the inactive days.
     *
     * @param anotherClan Another clan
     */
    @Override
    public int compareTo(Clan anotherClan) {
        int thisInactiveDate = this.getInactiveDays();
        int anotherInactiveDate = anotherClan.getInactiveDays();
        return (thisInactiveDate < anotherInactiveDate ? -1 : (thisInactiveDate == anotherInactiveDate ? 0 : 1));
    }

    /**
     * Returns all members, including the leaders
     *
     * @return All members of this clan
     */
    @Override
    public Set<ClanPlayer> getAllAllyMembers() {
        Set<ClanPlayer> allyMembers = new HashSet<ClanPlayer>();

        for (Clan ally : getAllies()) {
            allyMembers.addAll(ally.getAllMembers());
        }

        return Collections.unmodifiableSet(allyMembers);
    }

    /**
     * Check whether the clan has crossed the rival limit
     * <p/>
     * <strong>limit = AllRivalAbleClans * rivalPercentLimit</strong>
     *
     * @return Weather the clan has reached the maximum rivalries
     */
    @Override
    public boolean reachedRivalLimit() {
        int rivalCount = rivals == null ? 0 : rivals.size();
        //minus 1 because this clan is rivable
        double clanCount = plugin.getClanManager().getRivalAbleClanCount() - 1;
        double rivalPercent = plugin.getSettingsManager().getRivalLimitPercent();

        double limit = clanCount * rivalPercent / 100.0D;

        return rivalCount > limit;
    }

    /**
     * Deletes the permission sets
     */
    public void removePermissions() {
        plugin.getServer().getPluginManager().removePermission("SC" + String.valueOf(id));
    }

    /**
     * Creates a permission set for this clan and registers it
     *
     * @param permissions The set of permissions
     */
    public void setupPermissions(Map<String, Boolean> permissions) {
        removePermissions();

        plugin.registerSimpleClansPermission("SC" + String.valueOf(id), permissions);
    }

    /**
     * Updates the permissions for every clanplayer of this clan
     */
    public void updatePermissions() {
        for (ClanPlayer clanPlayer : getAllMembers()) {
            OnlineClanPlayer online = clanPlayer.getOnlineVersion();
            if (online == null) {
                continue;
            }

            online.removePermissions();
            online.setupPermissions();
        }
    }

    /**
     * Announces a message to the server
     *
     * @param message The message
     */
    @Override
    public void serverAnnounce(String message) {
        SimpleClans.serverAnnounceRaw(ChatBlock.parseColors(plugin.getSettingsManager().getClanAnnounce().replace("+clan", this.getTag()).replace("+message", message)));
    }

    /**
     * Displays a profile of the clan the CommandSender
     *
     * @param sender The retriever
     */
    @Override
    public void showClanProfile(CommandSender sender) {
        ChatColor subColor = plugin.getSettingsManager().getSubPageColor();
        ChatColor headColor = plugin.getSettingsManager().getHeaderPageColor();

        ChatBlock.sendBlank(sender);
        ChatBlock.sendHead(sender, Language.getTranslation("profile", plugin.getSettingsManager().getClanColor() + this.getName()), null);
        ChatBlock.sendBlank(sender);

        String name = this.getName();
        String leaders = plugin.getSettingsManager().getLeaderColor() + GeneralHelper.clansPlayersToString(this.getLeaders(), ",");
        String onlineCount = ChatColor.WHITE.toString() + GeneralHelper.stripOfflinePlayers(this.getAllMembers()).size();
        String membersOnline = onlineCount + subColor + "/" + ChatColor.WHITE + this.getSize();
        String inactive = ChatColor.WHITE.toString() + this.getInactiveDays() + subColor + "/" + ChatColor.WHITE.toString() + (this.isVerified() ? plugin.getSettingsManager().getPurgeInactiveClansDays() : plugin.getSettingsManager().getPurgeUnverifiedClansDays()) + " " + Language.getTranslation("days");
        String founded = ChatColor.WHITE + this.getFoundedDateFormatted();

        String rawAllies = GeneralHelper.clansToString(this.getAllies(), ",");
        String allies = ChatColor.WHITE + (rawAllies == null ? Language.getTranslation("none") : rawAllies);

        String rawRivals = GeneralHelper.clansToString(this.getRivals(), ",");
        String rivals = ChatColor.WHITE + (rawRivals == null ? Language.getTranslation("none") : rawRivals);
        String kdr = ChatColor.YELLOW + DECIMAL_FORMAT.format(this.getKDR());

        int[] kills = this.getTotalKills();

        String deaths = ChatColor.WHITE.toString() + kills[0];
        String rival = ChatColor.WHITE.toString() + kills[1];
        String civ = ChatColor.WHITE.toString() + kills[2];
        String neutral = ChatColor.WHITE.toString() + kills[3];

        String status = ChatColor.WHITE + (this.isVerified() ? plugin.getSettingsManager().getTrustedColor() + Language.getTranslation("verified") : plugin.getSettingsManager().getUntrustedColor() + Language.getTranslation("unverified"));

        ChatBlock.sendMessage(sender, "  " + subColor + MessageFormat.format(Language.getTranslation("tag.0"), this.getTag()));
        ChatBlock.sendMessage(sender, "  " + subColor + MessageFormat.format(Language.getTranslation("name.0"), name));
        ChatBlock.sendMessage(sender, "  " + subColor + MessageFormat.format(Language.getTranslation("status.0"), status));
        ChatBlock.sendMessage(sender, "  " + subColor + MessageFormat.format(Language.getTranslation("leaders.0"), leaders));
        ChatBlock.sendMessage(sender, "  " + subColor + MessageFormat.format(Language.getTranslation("members.online.0"), membersOnline));
        ChatBlock.sendMessage(sender, "  " + subColor + MessageFormat.format(Language.getTranslation("kdr.0"), kdr));
        ChatBlock.sendMessage(sender, "  " + subColor + Language.getTranslation("kill.totals") + " " + headColor + "[" + Language.getTranslation("rival") + ":" + rival + " " + headColor + Language.getTranslation("neutral") + ":" + neutral + " " + headColor + Language.getTranslation("civilian") + ":" + civ + headColor + "]");
        ChatBlock.sendMessage(sender, "  " + subColor + MessageFormat.format(Language.getTranslation("deaths.0"), deaths));
        ChatBlock.sendMessage(sender, "  " + subColor + MessageFormat.format(Language.getTranslation("allies.0"), allies));
        ChatBlock.sendMessage(sender, "  " + subColor + MessageFormat.format(Language.getTranslation("rivals.0"), rivals));
        ChatBlock.sendMessage(sender, "  " + subColor + MessageFormat.format(Language.getTranslation("founded.0"), founded));
        ChatBlock.sendMessage(sender, "  " + subColor + MessageFormat.format(Language.getTranslation("inactive.0"), inactive));

        ChatBlock.sendBlank(sender);
    }

    @Override
    public boolean withdraw(double amount) {
        return this.getBank().withdraw(amount);
    }

    @Override
    public void deposit(double amount) {
        this.getBank().deposit(amount);
    }

    @Override
    public boolean transfer(Balance account, double amount) {
        return this.getBank().transfer(account, amount);
    }

    @DatabaseColumnGetter(databaseName = "balance")
    @Override
    public double getBalance() {
        return this.getBank().getBalance();
    }

    @DatabaseColumnSetter(position = 10, databaseName = "balance", defaultValue = "0.0", lenght = {20, 2})
    private void setBalance(double balance) {
        this.getBank().setBalance(balance);
    }

    private BankAccount getBank() {
        if (bank == null) {
            bank = new BankAccount();
        }

        return bank;
    }

    @DatabaseColumnGetter(databaseName = "allies")
    private String getDatabaseAllies() {
        return allies == null || allies.isEmpty() ? null : JSONUtil.clansToJSON(allies);
    }

    @DatabaseColumnSetter(position = 6, databaseName = "allies", saveValueAfterLoading = true)
    private void setDatabaseAllies(String allies) {
        addDatabaseRelative(allies, this.allies = new HashSet<Clan>());
    }

    @DatabaseColumnGetter(databaseName = "rivals")
    private String getDatabaseRivals() {
        return rivals == null || rivals.isEmpty() ? null : JSONUtil.clansToJSON(rivals);
    }

    @DatabaseColumnSetter(position = 7, databaseName = "rivals", saveValueAfterLoading = true)
    private void setDatabaseRivals(String rivals) {
        addDatabaseRelative(rivals, this.rivals = new HashSet<Clan>());
    }

    @DatabaseColumnGetter(databaseName = "warring")
    private String getDatabaseWarring() {
        return warring == null || warring.isEmpty() ? null : JSONUtil.clansToJSON(warring);
    }

    @DatabaseColumnSetter(position = 8, databaseName = "warring", saveValueAfterLoading = true)
    private void setDatabaseWarring(String warring) {
        addDatabaseRelative(warring, this.warring = new HashSet<Clan>());
    }

    private void addDatabaseRelative(String json, Set<Clan> clans) {
        if (json == null) {
            return;
        }

        Set<Long> ids = JSONUtil.JSONToLongSet(json);

        if (ids == null) {
            return;
        }

        for (long clanId : ids) {
            Clan clan = plugin.getClanManager().getClan(clanId);
            if (clan != null) {
                clans.add(clan);
            }
        }
    }

    @DatabaseColumnSetter(position = 9, databaseName = "flags")
    public void setDatabaseFlags(String flags) {
        if (flags == null) {
            this.flags = new CraftClanFlags();
        }
        this.flags.deserialize(flags);
    }

    @DatabaseColumnGetter(databaseName = "flags")
    public String getDatabaseFlags() {
        if (getFlags() == null) {
            return null;
        }
        return flags.serialize();
    }

    /**
     * This is only used INTERNALLY! Do not call this method if you do not know exactly what this does!
     *
     * @param cp The ClanPlayer to add
     */
    public void addMemberInternally(ClanPlayer cp) {
        if (allMembers == null) {
            this.allMembers = new HashSet<ClanPlayer>();
        }
        this.allMembers.add(cp);
    }
}
