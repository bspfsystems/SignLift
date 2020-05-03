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

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.unixminecraft.bukkit.signlift.SignLift;
import org.unixminecraft.bukkit.signlift.config.ConfigMessage;
import org.unixminecraft.bukkit.signlift.exception.SignLiftException;

public final class PublicLiftSign extends LiftSign {
	
	public PublicLiftSign(final Block block) throws SignLiftException {
		this(block.getState());
	}
	
	public PublicLiftSign(final BlockState state) throws SignLiftException {
		super(state);
	}
	
	public PublicLiftSign(final Location location, final String[] lines) throws SignLiftException {
		super(location, lines);
	}
	
	@Override
	public boolean isPrivate() {
		return false;
	}
	
	@Override
	public boolean activate(final Player player, final SignLift plugin) {
		
		if(direction == Direction.NONE) {
			player.sendMessage(ConfigMessage.getLiftsignUseNoneDefault());
			return false;
		}
		
		final LiftSign destination = getDestination(player, plugin);
		if(destination == null) {
			player.sendMessage(ConfigMessage.getLiftsignUseDisconnectedPublic());
			return false;
		}
		
		if(!destination.canUse(player)) {
			player.sendMessage(ConfigMessage.getLiftsignUseDenyPublic());
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
			player.sendMessage(ConfigMessage.getLiftsignUseBlockedPublic());
			return false;
		}
		
		player.teleport(teleportLocation);
		player.sendMessage(getTeleportMessage(destination));
		return true;
	}
	
	@Override
	public boolean canRemove(final Player player) {
		return player.hasPermission("signlift.remove.public");
	}
	
	@Override
	public boolean canUse(final Player player) {
		return player.hasPermission("signlift.use.public");
	}
}
