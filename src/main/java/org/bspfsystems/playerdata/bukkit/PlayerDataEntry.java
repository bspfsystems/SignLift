/*
 * This file is part of the SignLift plugin for
 * Bukkit servers for Minecraft.
 *
 * Copyright (C) 2011      Shannon Wynter (http://fremnet.net/)
 * Copyright (C) 2012      GoalieGuy6 (https://github.com/goalieguy6/)
 * Copyright (C) 2018,2020 Matt Ciolkosz (https://github.com/mciolkosz/)
 * Copyright (C) 2021      BSPF Systems, LLC (https://bspfsystems.org/)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.bspfsystems.playerdata.bukkit;

import java.util.UUID;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.bspfsystems.signlift.bukkit.liftsign.LiftSign;

/**
 * Represents a mapping between a {@link Player}'s name and their {@link UUID}
 * for ease of determining ownership and membership of {@link LiftSign}s.
 */
public final class PlayerDataEntry {
    
    private static final String KEY_UNIQUE_ID = "unique_id";
    private static final String KEY_NAME = "name";
    
    private final UUID uniqueId;
    private String name;
    
    /**
     * Constructs a new {@link PlayerDataEntry} for the given {@link Player}.
     * 
     * @param player The {@link Player} to map.
     */
    public PlayerDataEntry(@NotNull final Player player) {
        this.uniqueId = player.getUniqueId();
        this.name = player.getName();
    }
    
    /**
     * Constructs a new {@link PlayerDataEntry} for the given name and
     * {@link UUID}, used when deserializing a serialized
     * {@link PlayerDataEntry}.
     * 
     * @param uniqueId The {@link UUID} to use for the mapping.
     * @param name The name to map the {@link UUID} to.
     */
    private PlayerDataEntry(@NotNull final UUID uniqueId, @NotNull final String name) {
        this.uniqueId = uniqueId;
        this.name = name;
    }
    
    /**
     * Deserializes the given {@link Configuration} data into a
     * {@link PlayerDataEntry}.
     *
     * @param data The {@link Configuration} data to deserialize.
     * @return The {@link PlayerDataEntry} represented by the given data.
     * @throws IllegalArgumentException If the {@link PlayerDataEntry} cannot be
     *                                  re-created or otherwise has bad data.
     */
    @NotNull
    public static PlayerDataEntry deserialize(@NotNull final Configuration data) throws IllegalArgumentException {
        
        final String uniqueIdRaw = data.getString(PlayerDataEntry.KEY_UNIQUE_ID, null);
        final String name = data.getString(PlayerDataEntry.KEY_NAME, null);
        
        if (uniqueIdRaw == null) {
            throw new IllegalArgumentException("PlayerDataEntry does not have a UUID.");
        }
        if (name == null) {
            throw new IllegalArgumentException("PlayerDataEntry does not have a name.");
        }
        
        if (!PlayerDataEntry.validateUniqueId(uniqueIdRaw)) {
            throw new IllegalArgumentException("UUID value is not a valid value.");
        }
        if (!PlayerDataEntry.validateString(name, 1, 16)) {
            throw new IllegalArgumentException("Name value is not a valid value.");
        }
        
        final UUID uniqueId;
        try {
            uniqueId = UUID.fromString(uniqueIdRaw);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("PlayerData invalid UUID config value.", e);
        }
        
        return new PlayerDataEntry(uniqueId, name);
    }
    
    /**
     * Serializes this {@link PlayerDataEntry} into a {@link Configuration} that
     * can be used to store it in a YAML file.
     *
     * @return The serialized version of this {@link PlayerDataEntry}.
     */
    @NotNull
    public Configuration serialize() {
        final Configuration data = new YamlConfiguration();
        data.set(PlayerDataEntry.KEY_UNIQUE_ID, this.uniqueId.toString());
        data.set(PlayerDataEntry.KEY_NAME, this.name);
        return data;
    }
    
    /**
     * Serializes this {@link PlayerDataEntry} into a {@link FileConfiguration}
     * for saving to disk.
     *
     * @return The serialized version of this {@link PlayerDataEntry} in a
     *         format convenient for saving to disk.
     */
    @NotNull
    public FileConfiguration serializeForSave() {
        final FileConfiguration data = new YamlConfiguration();
        data.set(PlayerDataEntry.KEY_UNIQUE_ID, this.uniqueId.toString());
        data.set(PlayerDataEntry.KEY_NAME, this.name);
        return data;
    }
    
    /**
     * Gets the {@link UUID} for this {@link PlayerDataEntry}.
     * 
     * @return The {@link UUID} for this {@link PlayerDataEntry}.
     */
    @NotNull
    public UUID getUniqueId() {
        return this.uniqueId;
    }
    
    /**
     * Gets the name for this {@link PlayerDataEntry}.
     * 
     * @return The name for this {@link PlayerDataEntry}.
     */
    @NotNull
    public String getName() {
        return this.name;
    }
    
    /**
     * Sets the name for this {@link PlayerDataEntry}.
     * 
     * @param name The new name to assign to this {@link PlayerDataEntry}.
     */
    public void setName(@NotNull final String name) {
        this.name = name;
    }
    
    /**
     * Validates the given {@link String} to make sure that it is not
     * {@code null}, does not have extra whitespace, and is the exact length.
     * 
     * @param string The {@link String} to validate.
     * @return {@code true} if the {@link String} meets the qualifications,
     *         {@code false} otherwise.
     * @see PlayerDataEntry#validateString(String, int, int)
     */
    private static boolean validateUniqueId(@Nullable final String string) {
        return PlayerDataEntry.validateString(string, 36, 36);
    }
    
    /**
     * Validates the given {@link String} to make sure that it is not
     * {@code null}, does not have extra whitespace, and has a length that
     * is between the lower and upper bounds, inclusive.
     *
     * @param string The {@link String} to validate.
     * @param lower The shortest allowable length.
     * @param upper The longest allowable length.   
     * @return {@code true} if the {@link String} meets the qualifications,
     *         {@code false} otherwise.
     */
    private static boolean validateString(@Nullable final String string, final int lower, final int upper) {
        return string != null && string.trim().length() == string.length() && string.length() >= lower && string.length() <= upper;
    }
}
