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
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.unixminecraft.bukkit.signlift.SignLift;
import org.unixminecraft.bukkit.signlift.config.ConfigData;
import org.unixminecraft.bukkit.signlift.config.ConfigMessage;
import org.unixminecraft.bukkit.signlift.exception.SignLiftException;

public abstract class LiftSign {
	
	public enum Direction {
		
		UP("up"),
		DOWN("down"),
		NONE("none");
		
		private final String label;
		
		private Direction(final String label) {
			this.label = label;
		}
		
		@Override
		public String toString() {
			return label;
		}
	}
	
	protected final Location location;
	protected final String label;
	protected final Direction direction;
	
	public LiftSign(final Block block) throws SignLiftException {
		this(block.getState());
	}
	
	public LiftSign(final BlockState state) throws SignLiftException {
		
		if(!(state instanceof Sign)) {
			throw new SignLiftException("Block is not a sign.");
		}
		
		final Sign sign = (Sign) state;
		final String liftLine = sign.getLine(1);
		
		validateLiftLine(liftLine);
		
		location = sign.getLocation();
		label = sign.getLine(0);
		direction = getLiftDirection(liftLine.substring(1, liftLine.length() - 1));
		
		if(direction == null) {
			throw new SignLiftException("Sign lift line does not have a valid direction.");
		}
	}
	
	public LiftSign(final Location location, final String[] lines) throws SignLiftException {
		
		if(!(location.getBlock().getState() instanceof Sign)) {
			throw new SignLiftException("Block at location is not a sign.");
		}
		
		final String liftLine = lines[1];
		
		validateLiftLine(liftLine);
		
		this.location = location;
		this.label = lines[0];
		direction = getLiftDirection(liftLine.substring(1, liftLine.length() - 1));
		
		if(direction == null) {
			throw new SignLiftException("Lift lines do not have a valid direction.");
		}
	}
	
	public abstract boolean isPrivate();
	public abstract boolean activate(final Player player, final SignLift plugin);
	public abstract boolean canRemove(final Player player);
	public abstract boolean canUse(final Player player);
	
	protected final LiftSign getDestination(final Player player, final SignLift plugin) {
		
		final World world = location.getWorld();
		final int x = location.getBlockX();
		final int y = location.getBlockY();
		final int z = location.getBlockZ();
		
		final int worldHeight = world.getMaxHeight();
		final int change;
		if(direction == Direction.UP) {
			change = 1;
		}
		else if(direction == Direction.DOWN) {
			change = -1;
		}
		else {
			return null;
		}
		
		for(int checkY = y + change; 0 < checkY && checkY < worldHeight; checkY += change) {
			
			final Block block = world.getBlockAt(x, checkY, z);
			final BlockState state = block.getState();
			if(!(state instanceof Sign)) {
				continue;
			}
			
			final Location location = block.getLocation();
			if(isPublicLiftSign(location)) {
				try {
					return new PublicLiftSign((Sign) state);
				}
				catch(SignLiftException e) {
					return null;
				}
			}
			else if(isPrivateLiftSign(location)) {
				return plugin.getPrivateLiftSign(location);
			}
		}
		
		return null;
	}
	
	protected final Block getAdjustedBlock(final Location location, int offset) {
		
		final int x;
		final int y;
		final int z;
		
		if(ConfigData.getCheckDestination()) {
			x = (int) Math.round(location.getX());
			y = this.location.getBlockY() + offset;
			z = (int) Math.round(location.getZ());
		}
		else {
			x = location.getBlockX();
			y = this.location.getBlockY() + offset;
			z = location.getBlockZ();
		}
		
		return this.location.getWorld().getBlockAt(x, y, z);
	}
	
	protected final String getTeleportMessage(final LiftSign destination) {
		
		final String teleportMessage;
		if(direction == Direction.UP && destination.label.isEmpty()) {
			teleportMessage = ConfigMessage.getLiftsignUseUpDefault();
		}
		else if(direction == Direction.UP && !destination.label.isEmpty()) {
			teleportMessage = ConfigMessage.getLiftsignUseUpCustom().replace("%%destination%%", destination.label);
		}
		else if(direction == Direction.DOWN && destination.label.isEmpty()) {
			teleportMessage = ConfigMessage.getLiftsignUseDownDefault();
		}
		else {
			teleportMessage = ConfigMessage.getLiftsignUseDownCustom().replace("%%destination%%", destination.label);
		}
		
		return teleportMessage;
	}
	
	public static final boolean isPublicLiftSign(final String liftLine) {
		
		try {
			validateLiftLine(liftLine);
		}
		catch(SignLiftException e) {
			return false;
		}
		
		return liftLine.startsWith(ConfigData.getLiftsignPublicOpen()) && liftLine.endsWith(ConfigData.getLiftsignPublicClose());
	}
	
	public static final boolean isPublicLiftSign(final Location location) {
		
		final BlockState state = location.getWorld().getBlockAt(location).getState();
		if(!(state instanceof Sign)) {
			return false;
		}
		
		return isPublicLiftSign(((Sign) state).getLine(1));
	}
	
	public static final boolean isPrivateLiftSign(final String liftLine) {
		
		try {
			validateLiftLine(liftLine);
		}
		catch(SignLiftException e) {
			return false;
		}
		
		return liftLine.startsWith(ConfigData.getLiftsignPrivateOpen()) && liftLine.endsWith(ConfigData.getLiftsignPrivateClose());
	}
	
	public static final boolean isPrivateLiftSign(final Location location) {
		
		final BlockState state = location.getWorld().getBlockAt(location).getState();
		if(!(state instanceof Sign)) {
			return false;
		}
		
		return isPrivateLiftSign(((Sign) state).getLine(1));
	}
	
	protected static final boolean isBlockSafe(final Block block) {
		
		switch(block.getType()) {
			case ACACIA_BUTTON:
			case ACACIA_PRESSURE_PLATE:
			case ACACIA_SAPLING:
			case ACACIA_SIGN:
			case ACACIA_WALL_SIGN:
			case ACTIVATOR_RAIL:
			case AIR:
			case ALLIUM:
			case AZURE_BLUET:
			case BEETROOTS:
			case BIRCH_BUTTON:
			case BIRCH_PRESSURE_PLATE:
			case BIRCH_SAPLING:
			case BIRCH_SIGN:
			case BIRCH_WALL_SIGN:
			case BLACK_BANNER:
			case BLACK_WALL_BANNER:
			case BLUE_BANNER:
			case BLUE_ORCHID:
			case BLUE_WALL_BANNER:
			case BROWN_BANNER:
			case BROWN_MUSHROOM:
			case BROWN_WALL_BANNER:
			case CARROTS:
			case CAVE_AIR:
			case COMPARATOR:
			case CYAN_BANNER:
			case CYAN_WALL_BANNER:
			case DANDELION:
			case DARK_OAK_BUTTON:
			case DARK_OAK_PRESSURE_PLATE:
			case DARK_OAK_SAPLING:
			case DARK_OAK_SIGN:
			case DARK_OAK_WALL_SIGN:
			case DEAD_BUSH:
			case DETECTOR_RAIL:
			case FERN:
			case GRAY_BANNER:
			case GRAY_WALL_BANNER:
			case GREEN_BANNER:
			case GREEN_WALL_BANNER:
			case HEAVY_WEIGHTED_PRESSURE_PLATE:
			case ITEM_FRAME:
			case JUNGLE_BUTTON:
			case JUNGLE_PRESSURE_PLATE:
			case JUNGLE_SAPLING:
			case JUNGLE_SIGN:
			case JUNGLE_WALL_SIGN:
			case KELP:
			case LADDER:
			case LARGE_FERN:
			case LEVER:
			case LIGHT_BLUE_BANNER:
			case LIGHT_BLUE_WALL_BANNER:
			case LIGHT_GRAY_BANNER:
			case LIGHT_GRAY_WALL_BANNER:
			case LIGHT_WEIGHTED_PRESSURE_PLATE:
			case LILAC:
			case LILY_OF_THE_VALLEY:
			case LIME_BANNER:
			case LIME_WALL_BANNER:
			case MAGENTA_BANNER:
			case MAGENTA_WALL_BANNER:
			case MELON_STEM:
			case OAK_BUTTON:
			case OAK_PRESSURE_PLATE:
			case OAK_SAPLING:
			case OAK_SIGN:
			case OAK_WALL_SIGN:
			case ORANGE_BANNER:
			case ORANGE_TULIP:
			case ORANGE_WALL_BANNER:
			case OXEYE_DAISY:
			case PAINTING:
			case PEONY:
			case PINK_BANNER:
			case PINK_TULIP:
			case PINK_WALL_BANNER:
			case POPPY:
			case POTATOES:
			case POWERED_RAIL:
			case PUMPKIN_STEM:
			case PURPLE_BANNER:
			case PURPLE_WALL_BANNER:
			case RAIL:
			case RED_BANNER:
			case RED_MUSHROOM:
			case RED_TULIP:
			case RED_WALL_BANNER:
			case REDSTONE_TORCH:
			case REDSTONE_WALL_TORCH:
			case REDSTONE_WIRE:
			case REPEATER:
			case ROSE_BUSH:
			case SCAFFOLDING:
			case SEAGRASS:
			case SPRUCE_BUTTON:
			case SPRUCE_PRESSURE_PLATE:
			case SPRUCE_SAPLING:
			case SPRUCE_SIGN:
			case SPRUCE_WALL_SIGN:
			case STONE_BUTTON:
			case STONE_PRESSURE_PLATE:
			case STRING:
			case SUGAR_CANE:
			case SUNFLOWER:
			case TALL_GRASS:
			case TALL_SEAGRASS:
			case TORCH:
			case TRIPWIRE:
			case TRIPWIRE_HOOK:
			case VINE:
			case WALL_TORCH:
			case WATER:
			case WHEAT:
			case WHITE_BANNER:
			case WHITE_TULIP:
			case WHITE_WALL_BANNER:
			case YELLOW_BANNER:
			case YELLOW_WALL_BANNER:
				return true;
			default:
				return false;
		}
	}
	
	private static void validateLiftLine(final String liftLine) throws SignLiftException {
		
		if(liftLine.length() < 3) {
			throw new SignLiftException("Lift line is less than 3 characters long: " + liftLine);
		}
		
		if(liftLine.startsWith(ConfigData.getLiftsignPublicOpen()) && liftLine.endsWith(ConfigData.getLiftsignPublicClose())) {
			if(getLiftDirection(liftLine.substring(1, liftLine.length() - 1)) == null) {
				throw new SignLiftException("Invalid lift direction for public lift sign: " + liftLine);
			}
			else {
				return;
			}
		}
		else if(liftLine.startsWith(ConfigData.getLiftsignPrivateOpen()) && liftLine.endsWith(ConfigData.getLiftsignPrivateClose())) {
			if(getLiftDirection(liftLine.substring(1, liftLine.length() - 1)) == null) {
				throw new SignLiftException("Invalid lift direction for private lift sign: " + liftLine);
			}
			else {
				return;
			}
		}
		else {
			throw new SignLiftException("Invalid lift brackets: " + liftLine);
		}
	}
	
	private static Direction getLiftDirection(final String liftDirection) {
		
		if(liftDirection.equalsIgnoreCase(ConfigData.getLiftsignDirectionNone())) {
			return Direction.NONE;
		}
		else if(liftDirection.equalsIgnoreCase(ConfigData.getLiftsignDirectionUp())) {
			return Direction.UP;
		}
		else if(liftDirection.equalsIgnoreCase(ConfigData.getLiftsignDirectionDown())) {
			return Direction.DOWN;
		}
		else {
			return null;
		}
	}
}
