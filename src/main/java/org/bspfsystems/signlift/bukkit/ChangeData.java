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

package org.bspfsystems.signlift.bukkit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.bspfsystems.signlift.bukkit.exception.SignLiftException;
import org.bspfsystems.signlift.bukkit.liftsign.PrivateLiftSign;

/**
 * Represents the data to change on a {@link PrivateLiftSign}.
 */
public final class ChangeData {
    
    private final UUID owner;
    private final Set<UUID> admins;
    private final Set<UUID> members;
    private final Set<UUID> removals;
    private final List<String> unknowns;
    
    /**
     * Constructs a new {@link ChangeData} where the owner will not change.
     * 
     * @see ChangeData#ChangeData(UUID)
     */
    public ChangeData() {
        this(null);
    }
    
    /**
     * Constructs a new {@link ChangeData} where the owner may change.
     * <p>
     * If the specified {@link UUID} is {@code null}, then the owner will not be
     * changing. Otherwise, the owner will be changed to the specified
     * {@link UUID}.
     *
     * @param owner The {@link UUID} of the new owner to set on the
     *              {@link PrivateLiftSign}, or {@code null} if no owner change
     *              is to happen.
     */
    public ChangeData(@Nullable final UUID owner) {
        this.owner = owner;
        this.admins = new HashSet<UUID>();
        this.members = new HashSet<UUID>();
        this.removals = new HashSet<UUID>();
        this.unknowns = new ArrayList<String>();
    }
    
    /**
     * Stages a {@link Player}'s {@link UUID} for being added as an admin on the
     * {@link PrivateLiftSign}.
     * 
     * @param admin The {@link UUID} to add as an admin.
     * @return {@code true} if the {@link UUID} was staged, {@code false} if it
     *         has already been staged.
     * @throws SignLiftException If the specified {@link UUID} has already been
     *                           staged as a member addition or a general
     *                           removal.
     */
    public boolean addAdmin(@NotNull final UUID admin) throws SignLiftException {
        
        if (this.members.contains(admin)) {
            throw new SignLiftException("You cannot add someone as an admin and a member at the same time.");
        }
        if (this.removals.contains(admin)) {
            throw new SignLiftException("You cannot add someone as an admin and remove them at the same time.");
        }
        
        return this.admins.add(admin);
    }
    
    /**
     * Stages a {@link Player}'s {@link UUID} for being added as a member on the
     * {@link PrivateLiftSign}.
     * 
     * @param member The {@link UUID} to add as a member.
     * @return {@code true} if the {@link UUID} was staged, {@code false} if it
     *         has already been staged.
     * @throws SignLiftException If the specified {@link UUID} has already been
     *                           staged as an admin addition or a general
     *                           removal.
     */
    public boolean addMember(@NotNull final UUID member) throws SignLiftException {
        
        if (this.admins.contains(member)) {
            throw new SignLiftException("You cannot add someone as a member and an admin at the same time.");
        }
        if (this.removals.contains(member)) {
            throw new SignLiftException("You cannot add someone as a member and remove them at the same time.");
        }
        
        return this.members.add(member);
    }
    
    /**
     * Stages a {@link Player}'s {@link UUID} for being removed from the
     * {@link PrivateLiftSign}.
     * 
     * @param removal The {@link UUID} to remove.
     * @return {@code true} if the {@link UUID} was staged, {@code false} if it
     *         has already been staged.
     * @throws SignLiftException If the specified {@link UUID} has already been
     *                           staged as an admin or a member addition.
     */
    public boolean remove(@NotNull final UUID removal) throws SignLiftException {
        
        if (this.admins.contains(removal)) {
            throw new SignLiftException("You cannot remove someone and add them as an admin at the same time.");
        }
        if (this.members.contains(removal)) {
            throw new SignLiftException("You cannot remove someone and add them as a member at the same time.");
        }
        
        return this.removals.add(removal);
    }
    
    /**
     * Adds a name to the list of unknown {@link Player}s. This will later be
     * used to show the requester which names cannot be resolved.
     * 
     * @param unknown The unknown player name.
     */
    public void addUnknown(final String unknown) {
        this.unknowns.add(unknown);
    }
    
    /**
     * Gets the {@link UUID} of the {@link Player} that will become the new
     * owner of the {@link PrivateLiftSign}. If this is {@code null}, then the
     * owner will not be changing.
     * 
     * @return The {@link UUID} of the {@link Player} that is to be the new
     *         owner, or {@code null} if the owner is not changing.
     */
    @Nullable
    public UUID getOwner() {
        return this.owner;
    }
    
    /**
     * Gets the {@link Set} of {@link UUID}s that are staged to be added to the
     * {@link PrivateLiftSign} as admins.
     * 
     * @return The {@link Set} of {@link UUID}s staged for being added as
     *         admins.
     */
    @NotNull
    public Set<UUID> getAdmins() {
        return this.admins;
    }
    
    /**
     * Gets the {@link Set} of {@link UUID}s that are staged to be added to the
     * {@link PrivateLiftSign} as members.
     * 
     * @return The {@link Set} of {@link UUID}s staged for being added as
     *         members.
     */
    @NotNull
    public Set<UUID> getMembers() {
        return this.members;
    }
    
    /**
     * Gets the {@link Set} of {@link UUID}s that are staged to be removed from
     * the {@link PrivateLiftSign}.
     * 
     * @return The {@link Set} of {@link UUID}s staged for being removed.
     */
    @NotNull
    public Set<UUID> getRemovals() {
        return this.removals;
    }
    
    /**
     * Gets the {@link List} of {@link Player} names that cannot be resolved
     * into {@link UUID}s, and are thus unknown.
     * 
     * @return The {@link List} of unknown {@link Player} names.
     */
    @NotNull
    public List<String> getUnknowns() {
        return this.unknowns;
    }
}
