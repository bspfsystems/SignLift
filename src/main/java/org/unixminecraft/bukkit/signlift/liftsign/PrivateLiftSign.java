/*
 * SignLift Bukkit plugin for Minecraft
 * Copyright (C) 2011       Shannon Wynter (http://fremnet.net/)
 * Copyright (C) 2012       GoalieGuy6 (https://github.com/goalieguy6)
 * Copyright (C) 2018,2020  Matt Ciolkosz (https://github.com/mciolkosz)
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

package org.unixminecraft.bukkit.signlift.liftsign;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.unixminecraft.bukkit.signlift.SignLift;
import org.unixminecraft.bukkit.signlift.config.ConfigMessage;
import org.unixminecraft.bukkit.signlift.exception.SignLiftException;

@SerializableAs("private_lift_sign")
public final class PrivateLiftSign extends LiftSign implements ConfigurationSerializable {
	
	private UUID owner;
	private final HashSet<UUID> admins;
	private final HashSet<UUID> members;
	
	public PrivateLiftSign(final Block block, final Player player) throws SignLiftException {
		this(block.getState(), player);
	}
	
	public PrivateLiftSign(final BlockState state, final Player player) throws SignLiftException {
		
		super(state);
		
		owner = player.getUniqueId();
		admins = new HashSet<UUID>();
		members = new HashSet<UUID>();
	}
	
	public PrivateLiftSign(final Location location, final String[] lines, final Player player) throws SignLiftException {
		
		super(location, lines);
		
		owner = player.getUniqueId();
		admins = new HashSet<UUID>();
		members = new HashSet<UUID>();
	}
	
	private PrivateLiftSign(final Location location, final UUID owner, final HashSet<UUID> admins, final HashSet<UUID> members) throws SignLiftException {
		
		super(location.getBlock());
		
		this.owner = owner;
		this.admins = admins;
		this.members = members;
	}
	
	public static PrivateLiftSign deserialize(final Map<String, Object> config) {
		
		if(!config.containsKey("location")) {
			throw new IllegalArgumentException("PrivateSignLift config location config is missing.");
		}
		if(config.get("location") == null) {
			throw new IllegalArgumentException("PrivateSignLift config location config is null.");
		}
		if(!(config.get("location") instanceof Location)) {
			throw new IllegalArgumentException("PrivateSignLift config location config is not of right type.");
		}
		
		if(!config.containsKey("owner_unique_id")) {
			throw new IllegalArgumentException("PrivateSignLift config owner config is missing.");
		}
		if(config.get("owner_unique_id") == null) {
			throw new IllegalArgumentException("PrivateSignLift config owner config is null.");
		}
		if(!(config.get("owner_unique_id") instanceof String)) {
			throw new IllegalArgumentException("PrivateSignLift config owner config is not of right type.");
		}
		
		if(!config.containsKey("admin_unique_ids")) {
			throw new IllegalArgumentException("PrivateSignLift config admin config is missing.");
		}
		if(config.get("admin_unique_ids") == null) {
			throw new IllegalArgumentException("PrivateSignLift config admin config is null.");
		}
		if(!(config.get("admin_unique_ids") instanceof List<?>)) {
			throw new IllegalArgumentException("PrivateSignLift config admin config is not of right type.");
		}
		
		if(!config.containsKey("member_unique_ids")) {
			throw new IllegalArgumentException("PrivateSignLift config member config is missing.");
		}
		if(config.get("member_unique_ids") == null) {
			throw new IllegalArgumentException("PrivateSignLift config member config is null.");
		}
		if(!(config.get("member_unique_ids") instanceof List<?>)) {
			throw new IllegalArgumentException("PrivateSignLift config member config is not of right type.");
		}
		
		final Location location = (Location) config.get("location");
		final UUID owner = UUID.fromString((String) config.get("owner_unique_id"));
		
		final HashSet<UUID> admins = new HashSet<UUID>();
		for(final Object rawAdminUniqueId : (List<?>) config.get("admin_unique_ids")) {
			
			if(!(rawAdminUniqueId instanceof String)) {
				continue;
			}
			
			try {
				admins.add(UUID.fromString((String) rawAdminUniqueId));
			}
			catch(IllegalArgumentException e) {
				continue;
			}
		}
		
		final HashSet<UUID> members = new HashSet<UUID>();
		for(final Object rawMemberUniqueId : (List<?>) config.get("member_unique_ids")) {
			
			if(!(rawMemberUniqueId instanceof String)) {
				continue;
			}
			
			try {
				members.add(UUID.fromString((String) rawMemberUniqueId));
			}
			catch(IllegalArgumentException e) {
				continue;
			}
		}
		
		try {
			return new PrivateLiftSign(location, owner, admins, members);
		}
		catch(SignLiftException e) {
			throw new IllegalArgumentException("Cannot form PrivateLiftSign.", e);
		}
	}
	
	@Override
	public Map<String, Object> serialize() {
		
		final ArrayList<String> adminValues = new ArrayList<String>();
		for(final UUID admin : admins) {
			adminValues.add(admin.toString());
		}
		
		final ArrayList<String> memberValues = new ArrayList<String>();
		for(final UUID member : members) {
			memberValues.add(member.toString());
		}
		
		final HashMap<String, Object> config = new HashMap<String, Object>();
		
		config.put("location", location);
		config.put("owner_unique_id", owner.toString());
		config.put("admin_unique_ids", adminValues);
		config.put("member_unique_ids", memberValues);
		
		return config;
	}
	
	@Override
	public boolean isPrivate() {
		return true;
	}
	
	@Override
	public boolean activate(final Player player, final SignLift plugin) {
		
		if(!canUse(player)) {
			player.sendMessage(ConfigMessage.getLiftsignUseDenyPrivate());
			return false;
		}
		
		if(direction == Direction.NONE) {
			player.sendMessage(ConfigMessage.getLiftsignUseNoneDefault());
			return false;
		}
		
		final LiftSign destination = getDestination(player, plugin);
		if(destination == null) {
			player.sendMessage(ConfigMessage.getLiftsignUseDisconnectedPrivate());
			return false;
		}
		
		if(!destination.canUse(player)) {
			player.sendMessage(ConfigMessage.getLiftsignUseDenyPrivate());
			return false;
		}
		
		final Location playerLocation = player.getLocation();
		final Location teleportLocation = playerLocation.clone();
		
		final Block destinationBlock = destination.location.getBlock();
		boolean isSafe = false;
		
		if(destinationBlock.getY() < destinationBlock.getWorld().getMaxHeight()) {
			final Block destinationBlockOffset = destination.getAdjustedBlock(playerLocation, 1);
			teleportLocation.setY(destinationBlock.getY());
			isSafe = isBlockSafe(destinationBlock) && isBlockSafe(destinationBlockOffset);
		}
		
		if(destinationBlock.getY() > 0 && !isSafe) {
			final Block destinationBlockOffset = destination.getAdjustedBlock(playerLocation, -1);
			teleportLocation.setY(destinationBlock.getY() - 1);
			isSafe = isBlockSafe(destinationBlock) && isBlockSafe(destinationBlockOffset);
		}
		
		if(!isSafe) {
			player.sendMessage(ConfigMessage.getLiftsignUseBlockedPrivate());
			return false;
		}
		
		player.teleport(teleportLocation);
		player.sendMessage(getTeleportMessage(destination));
		return true;
	}
	
	public UUID getOwner() {
		return owner;
	}
	
	public HashSet<UUID> getAdmins() {
		return admins;
	}
	
	public HashSet<UUID> getMembers() {
		return members;
	}
	
	public boolean canModifyOwner(final Player player) {
		
		if(player.hasPermission("signlift.create.admin")) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public boolean canModifyAdmins(final Player player) {
		
		if(player.hasPermission("signlift.modify.admin")) {
			return true;
		}
		else if(owner.equals(player.getUniqueId())) {
			return player.hasPermission("signlift.modify.private");
		}
		else {
			return false;
		}
	}
	
	public boolean canModifyMembers(final Player player) {
		
		if(player.hasPermission("signlift.modify.admin")) {
			return true;
		}
		else if(owner.equals(player.getUniqueId())) {
			return player.hasPermission("signlift.modify.private");
		}
		else if(admins.contains(player.getUniqueId())) {
			return player.hasPermission("signlift.modify.private");
		}
		else {
			return false;
		}
	}
	
	@Override
	public boolean canRemove(final Player player) {
		
		if(player.hasPermission("signlift.remove.admin")) {
			return true;
		}
		else if(owner.equals(player.getUniqueId())) {
			return player.hasPermission("signlift.remove.private");
		}
		else {
			return false;
		}
	}
	
	@Override
	public boolean canUse(final Player player) {
		
		if(player.hasPermission("signlift.use.admin")) {
			return true;
		}
		else if(owner.equals(player.getUniqueId())) {
			return player.hasPermission("signlift.use.private");
		}
		else if(admins.contains(player.getUniqueId())) {
			return player.hasPermission("signlift.use.private");
		}
		else if(members.contains(player.getUniqueId())) {
			return player.hasPermission("signlift.use.private");
		}
		else {
			return false;
		}
	}
	
	public boolean changeOwner(final UUID owner) {
		this.owner = owner;
		return true;
	}
	
	public boolean isAdmin(final UUID admin) {
		return admins.contains(admin);
	}
	
	public boolean containsAdmins(final HashSet<UUID> players) {
		for(final UUID player : players) {
			if(admins.contains(player)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean addAdmin(final UUID admin) {
		return admins.add(admin);
	}
	
	public boolean removeAdmin(final UUID admin) {
		return admins.remove(admin);
	}
	
	public boolean isMember(final UUID member) {
		return members.contains(member);
	}
	
	public boolean addMember(final UUID member) {
		return members.add(member);
	}
	
	public boolean removeMember(final UUID member) {
		return members.remove(member);
	}
	
	public Location getLocation() {
		return location;
	}
}
