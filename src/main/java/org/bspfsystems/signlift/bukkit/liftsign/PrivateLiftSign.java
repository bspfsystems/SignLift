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


package org.bspfsystems.signlift.bukkit.liftsign;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.bspfsystems.signlift.bukkit.config.ConfigMessage;
import org.bspfsystems.signlift.bukkit.exception.SignLiftException;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.jetbrains.annotations.NotNull;
import org.bspfsystems.signlift.bukkit.SignLiftPlugin;

/**
 * Represents the private (access-controlled) implementation of a
 * {@link LiftSign}. This is serialized and able to be stored as a
 * {@link Configuration} item to maintain the access lists.
 */
public final class PrivateLiftSign extends LiftSign {
    
    private static final String KEY_LOCATION = "location";
    private static final String KEY_OWNER_UNIQUE_ID = "owner_unique_id";
    private static final String KEY_ADMIN_UNIQUE_IDS = "admin_unique_ids";
    private static final String KEY_MEMBER_UNIQUE_IDS = "member_unqiue_ids";
    
    private UUID owner;
    private final HashSet<UUID> admins;
    private final HashSet<UUID> members;
    
    /**
     * Creates a new {@link PrivateLiftSign} from the given {@link Block}, owned
     * by the given {@link Player}.
     * 
     * @param block The {@link Block} to create the {@link PrivateLiftSign}
     *              from.
     * @param player The owner of the {@link PrivateLiftSign}.
     * @throws SignLiftException If the {@link PrivateLiftSign} cannot be
     *                           created.
     * @see LiftSign#LiftSign(Block)
     */
    public PrivateLiftSign(@NotNull final Block block, @NotNull final Player player) throws SignLiftException {
        super(block.getState());
        this.owner = player.getUniqueId();
        this.admins = new HashSet<UUID>();
        this.members = new HashSet<UUID>();
    }
    
    /**
     * Creates a new {@link PrivateLiftSign} from the given {@link BlockState},
     * owned by the given {@link Player}.
     * 
     * @param state The {@link BlockState} to create the {@link PrivateLiftSign}
     *              from.
     * @param player The owner of the {@link PrivateLiftSign}.
     * @throws SignLiftException If the {@link PrivateLiftSign} cannot be
     *                           created.
     * @see LiftSign#LiftSign(BlockState)
     */
    public PrivateLiftSign(@NotNull final BlockState state, @NotNull final Player player) throws SignLiftException {
        super(state);
        this.owner = player.getUniqueId();
        this.admins = new HashSet<UUID>();
        this.members = new HashSet<UUID>();
    }
    
    /**
     * Creates a new {@link PrivateLiftSign} from the given {@link Location} and
     * {@link String} array of lines, owned by the given {@link Player}.
     * <p>
     * The given {@link Location} must contain a {@link Sign}, and the lines
     * must have valid lift lines on them. This is most commonly used when the
     * lines on a {@link Sign} have been changed.
     * 
     * @param location The {@link Location} that is to contain the
     *                 {@link PrivateLiftSign}.
     * @param lines The lines that will be applied to the
     *              {@link PrivateLiftSign}.
     * @param player The owner of the {@link PrivateLiftSign}.
     * @throws SignLiftException If the {@link PrivateLiftSign} cannot be
     *                           created.
     * @see LiftSign#LiftSign(Location, String[])
     * @see SignChangeEvent
     */
    public PrivateLiftSign(@NotNull final Location location, @NotNull final String[] lines, @NotNull final Player player) throws SignLiftException {
        super(location, lines);
        this.owner = player.getUniqueId();
        this.admins = new HashSet<UUID>();
        this.members = new HashSet<UUID>();
    }
    
    /**
     * Creates a new {@link PrivateLiftSign} from the given {@link Location}
     * with the given {@link UUID} as the owner, along with the given admins
     * and members.
     * <p>
     * This is used when deserializing the {@link PrivateLiftSign} from a
     * {@link Configuration}.
     *
     * @param location The {@link Location} that is to contain the
     *                 {@link PrivateLiftSign}.
     * @param owner The {@link UUID} of the owner of the
     *              {@link PrivateLiftSign}.
     * @param admins The {@link HashSet} of {@link UUID}s of the admins of the
     *               {@link PrivateLiftSign}.
     * @param members The {@link HashSet} of {@link UUID}s of the members of the
     *                {@link PrivateLiftSign}.
     * @throws SignLiftException If the {@link PrivateLiftSign} cannot be
     *                           deserialized.
     */
    private PrivateLiftSign(@NotNull final Location location, @NotNull final UUID owner, @NotNull final HashSet<UUID> admins, @NotNull final HashSet<UUID> members) throws SignLiftException {
        super(location.getBlock());
        this.owner = owner;
        this.admins = admins;
        this.members = members;
    }
    
    /**
     * Deserializes the given {@link Configuration} data into a
     * {@link PrivateLiftSign}.
     * 
     * @param data The {@link Configuration} data to deserialize.
     * @return The {@link PrivateLiftSign} represented by the given data.
     * @throws SignLiftException If the {@link PrivateLiftSign} cannot be
     *                           re-created or otherwise has bad data.
     */
    @NotNull
    public static PrivateLiftSign deserialize(@NotNull final Configuration data) throws SignLiftException {
        
        final Location location = data.getLocation(PrivateLiftSign.KEY_LOCATION, null);
        final String ownerIdRaw = data.getString(PrivateLiftSign.KEY_OWNER_UNIQUE_ID, null);
        final List<String> adminIdsRaw = data.getStringList(PrivateLiftSign.KEY_ADMIN_UNIQUE_IDS);
        final List<String> memberIdsRaw = data.getStringList(PrivateLiftSign.KEY_MEMBER_UNIQUE_IDS);
        
        if (location == null) {
            throw new SignLiftException("PrivateLiftSign does not have Location data.");
        }
        if (ownerIdRaw == null) {
            throw new SignLiftException("PrivateLiftSign does not have owner UUID data.");
        }
        
        final UUID ownerId;
        try {
            ownerId = UUID.fromString(ownerIdRaw);
        } catch (IllegalArgumentException e) {
            throw new SignLiftException("Unable to parse owner UUID for PrivateLiftSign.", e);
        }
        
        final HashSet<UUID> adminIds = new HashSet<UUID>();
        for (final String adminIdRaw : adminIdsRaw) {
            
            final UUID adminId;
            try {
                adminId = UUID.fromString(adminIdRaw);
            } catch (IllegalArgumentException e) {
                throw new SignLiftException("Unable to parse admin UUID for PrivateLiftSign.", e);
            }
            
            if (ownerId.equals(adminId)) {
                throw new SignLiftException("Admin UUID (" + adminIdRaw + ") matches owner UUID for PrivateLiftSign.");
            }
            if (!adminIds.add(adminId)) {
                throw new SignLiftException("Duplicate admin UUID (" + adminIdRaw + ") for PrivateLiftSign.");
            }
        }
        
        final HashSet<UUID> memberIds = new HashSet<UUID>();
        for (final String memberIdRaw : memberIdsRaw) {
            
            final UUID memberId;
            try {
                memberId = UUID.fromString(memberIdRaw);
            } catch (IllegalArgumentException e) {
                throw new SignLiftException("Unable to parse member UUID for PrivateLiftSign.", e);
            }
            
            if (ownerId.equals(memberId)) {
                throw new SignLiftException("Member UUID (" + memberIdRaw + ") matches owner UUID for PrivateLiftSign.");
            }
            if (adminIds.contains(memberId)) {
                throw new SignLiftException("Member UUID (" + memberIdRaw + ") matches an admin UUID for PrivateLiftSign.");
            }
            if (!memberIds.add(memberId)) {
                throw new SignLiftException("Duplicate member UUID (" + memberIdRaw + ") for PrivateLiftSign.");
            }
        }
        
        try {
            return new PrivateLiftSign(location, ownerId, adminIds, memberIds);
        } catch (SignLiftException e) {
            throw new SignLiftException("Cannot re-create the PrivateLiftSign.", e);
        }
    }
    
    /**
     * Serializes this {@link PrivateLiftSign} into a {@link Configuration} that
     * can be used to store it in a YAML file.
     * 
     * @return The serialized version of this {@link PrivateLiftSign}.
     */
    @NotNull
    public Configuration serialize() {
        
        final Location location = new Location(this.world, this.x, this.y, this.z);
        final String ownerId = this.owner.toString();
        
        final List<String> adminIds = new ArrayList<String>();
        for (final UUID admin : this.admins) {
            adminIds.add(admin.toString());
        }
        
        final List<String> memberIds = new ArrayList<String>();
        for (final UUID member : this.members) {
            memberIds.add(member.toString());
        }
        
        final Configuration data = new YamlConfiguration();
        data.set(PrivateLiftSign.KEY_LOCATION, location);
        data.set(PrivateLiftSign.KEY_OWNER_UNIQUE_ID, ownerId);
        data.set(PrivateLiftSign.KEY_ADMIN_UNIQUE_IDS, adminIds);
        data.set(PrivateLiftSign.KEY_MEMBER_UNIQUE_IDS, memberIds);
        return data;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPrivate() {
        return true;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean activate(@NotNull final Player player, @NotNull final SignLiftPlugin plugin) {
        if (!this.canUse(player)) {
            player.sendMessage(ConfigMessage.getLiftsignUseDenyPrivate());
            return false;
        }
        return super.activate(player, plugin);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canRemove(@NotNull final Player player) {
        if (player.hasPermission("signlift.remove.admin")) {
            return true;
        } else if (this.owner.equals(player.getUniqueId())) {
            return player.hasPermission("signlift.remove.private");
        }
        return false;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canUse(@NotNull final Player player) {
        
        if (player.hasPermission("signlift.use.admin")) {
            return true;
        } else if (this.owner.equals(player.getUniqueId()) || this.admins.contains(player.getUniqueId()) || this.members.contains(player.getUniqueId())) {
            return player.hasPermission("signlift.use.private");
        }
        return false;
    }
    
    /**
     * Gets the {@link UUID} of the owner of this {@link PrivateLiftSign}.
     * 
     * @return The {@link UUID} of the owner of this {@link PrivateLiftSign}.
     */
    @NotNull
    public UUID getOwner() {
        return this.owner;
    }
    
    /**
     * Gets a {@link Set} of {@link UUID}s that are the admins on this
     * {@link PrivateLiftSign}.
     * 
     * @return A {@link Set} of {@link UUID}s of the admins on this
     *         {@link PrivateLiftSign}.
     */
    @NotNull
    public Set<UUID> getAdmins() {
        return this.admins;
    }
    
    /**
     * Gets a {@link Set} of {@link UUID}s that are the members on this
     * {@link PrivateLiftSign}.
     * 
     * @return A {@link Set} of {@link UUID}s of the members on this
     *         {@link PrivateLiftSign}.
     */
    @NotNull
    public Set<UUID> getMembers() {
        return this.members;
    }
    
    /**
     * Checks to see if the given {@link Player} can modify the owner of this
     * {@link PrivateLiftSign}.
     * <p>
     * Only {@link Player}s with the admin override permission node
     * ({@code signlift.create.admin}) will be able to change the owner. The
     * owner cannot change it themselves; they must destroy this
     * {@link PrivateLiftSign} and have the new owner re-create the
     * {@link PrivateLiftSign}.
     * 
     * @param player The {@link Player} to check.
     * @return {@code true} if the {@link Player} has permission, {@code false}
     *         otherwise.
     */
    public boolean canModifyOwner(@NotNull final Player player) {
        return player.hasPermission("signlift.create.admin");
    }
    
    /**
     * Checks to see if the given {@link Player} can modify the admins of this
     * {@link PrivateLiftSign}.
     * <p>
     * Only {@link Player}s with the admin override permission node
     * ({@code signlift.modify.admin}), or the owner (with the proper
     * permissions), can change the admins on this {@link PrivateLiftSign}.
     * Other admins cannot update the admins of this {@link PrivateLiftSign}.
     * 
     * @param player The {@link Player} to check.
     * @return {@code true} if the {@link Player} has permission, {@code false}
     *         otherwise.
     */
    public boolean canModifyAdmins(@NotNull final Player player) {
        if (player.hasPermission("signlift.modify.admin")) {
            return true;
        } else if (this.owner.equals(player.getUniqueId())) {
            return player.hasPermission("signlift.modify.private");
        }
        return false;
    }
    
    /**
     * Checks to see if the given {@link Player} can modify the members of this
     * {@link PrivateLiftSign}.
     * <p>
     * Only {@link Player}s with the admin override permission node
     * ({@code signlift.modify.private}), or the owner or admins (with the
     * proper permissions), can change the members on this
     * {@link PrivateLiftSign}. Other members cannot update the members of this
     * {@link PrivateLiftSign}.
     * 
     * @param player The {@link Player} to check.
     * @return {@code true} if the {@link Player} has permission, {@code false}
     *         otherwise.
     */
    public boolean canModifyMembers(@NotNull final Player player) {
        if (player.hasPermission("signlift.modify.admin")) {
            return true;
        } else if (this.owner.equals(player.getUniqueId()) || this.admins.contains(player.getUniqueId())) {
            return player.hasPermission("signlift.modify.private");
        }
        return false;
    }
    
    /**
     * Changes the owner of this {@link PrivateLiftSign}.
     * 
     * @param owner The {@link UUID} of the new owner.
     * @return {@code true} as a successful change.
     */
    public boolean changeOwner(@NotNull final UUID owner) {
        this.owner = owner;
        return true;
    }
    
    /**
     * Checks to see if the given {@link Player}'s {@link UUID} is that of an
     * admin on this {@link PrivateLiftSign}.
     * 
     * @param admin The {@link UUID} to check.
     * @return {@code true} if the {@link UUID} is that of an admin,
     *         {@code false} otherwise.
     */
    public boolean isAdmin(@NotNull final UUID admin) {
        return this.admins.contains(admin);
    }
    
    /**
     * Checks to see if the given {@link Set} of {@link UUID}s contains any that
     * are admins on this {@link PrivateLiftSign}.
     * 
     * @param playerIds The {@link Set} of {@link UUID}s to check.
     * @return {@code true} if at least 1 {@link UUID} in the given {@link Set}
     *         is an admin on this {@link PrivateLiftSign}, {@code false}
     *         otherwise.
     */
    public boolean containsAdmins(@NotNull final Set<UUID> playerIds) {
        for (final UUID playerId : playerIds) {
            if (this.admins.contains(playerId)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Adds a {@link UUID} as an admin on this {@link PrivateLiftSign}.
     * 
     * @param admin The {@link UUID} to add.
     * @return The success of adding the {@link UUID}.
     */
    public boolean addAdmin(@NotNull final UUID admin) {
        return this.admins.add(admin);
    }
    
    /**
     * Removes a {@link UUID} as an admin from this {@link PrivateLiftSign}.
     * 
     * @param admin The {@link UUID} to remove.
     * @return The success of removing the {@link UUID}.
     */
    public boolean removeAdmin(@NotNull final UUID admin) {
        return this.admins.remove(admin);
    }
    
    /**
     * Checks to see if the given {@link Player}'s {@link UUID} is that of a
     * member on this {@link PrivateLiftSign}.
     *
     * @param member The {@link UUID} to check.
     * @return {@code true} if the {@link UUID} is that of a member,
     *         {@code false} otherwise.
     */
    public boolean isMember(@NotNull final UUID member) {
        return members.contains(member);
    }
    
    /**
     * Checks to see if the given {@link Set} of {@link UUID}s contains any that
     * are members on this {@link PrivateLiftSign}.
     *
     * @param playerIds The {@link Set} of {@link UUID}s to check.
     * @return {@code true} if at least 1 {@link UUID} in the given {@link Set}
     *         is a member on this {@link PrivateLiftSign}, {@code false}
     *         otherwise.
     */
    public boolean containsMembers(@NotNull final Set<UUID> playerIds) {
        for (final UUID playerId : playerIds) {
            if (this.members.contains(playerId)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Adds a {@link UUID} as a member on this {@link PrivateLiftSign}.
     *
     * @param member The {@link UUID} to add.
     * @return The success of adding the {@link UUID}.
     */
    public boolean addMember(@NotNull final UUID member) {
        return this.members.add(member);
    }
    
    /**
     * Removes a {@link UUID} as a member from this {@link PrivateLiftSign}.
     *
     * @param member The {@link UUID} to remove.
     * @return The success of removing the {@link UUID}.
     */
    public boolean removeMember(@NotNull final UUID member) {
        return this.members.remove(member);
    }
}
