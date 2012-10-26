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
 *     Last modified: 11.10.12 14:29
 */

package com.p000ison.dev.simpleclans2.api.events;

import com.p000ison.dev.simpleclans2.clan.Clan;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This even is used to store a clan for the event
 */
class ClanEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Clan clan;

    protected ClanEvent(Clan clan)
    {
        this.clan = clan;
    }

    @Override
    public HandlerList getHandlers()
    {
        return handlers;
    }

    public Clan getClan()
    {
        return clan;
    }
}
