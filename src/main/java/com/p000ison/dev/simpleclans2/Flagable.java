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
 *     Last modified: 10.11.12 16:49
 */

package com.p000ison.dev.simpleclans2;

/**
 * Represents a Flagable
 */
public interface Flagable {

    /**
     * Serializes a this object into a string
     *
     * @return The string
     */
    String serialize();

    /**
     * Deserializes the object and returns itself
     *
     * @param deserialize The string to deserialize
     * @return Itself
     */
    Object deserialize(String deserialize);
}