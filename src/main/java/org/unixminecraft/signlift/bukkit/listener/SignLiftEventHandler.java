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

package org.unixminecraft.signlift.bukkit.listener;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.unixminecraft.signlift.bukkit.SignLift;
import org.unixminecraft.signlift.bukkit.config.ConfigData;
import org.unixminecraft.signlift.bukkit.config.ConfigMessage;
import org.unixminecraft.signlift.bukkit.exception.SignLiftException;
import org.unixminecraft.signlift.bukkit.liftsign.LiftSign;
import org.unixminecraft.signlift.bukkit.liftsign.PrivateLiftSign;
import org.unixminecraft.signlift.bukkit.liftsign.PublicLiftSign;

public final class SignLiftEventHandler implements Listener {
	
	private final SignLift plugin;
	
	public SignLiftEventHandler(final SignLift plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockBreak(final BlockBreakEvent event) {
		
		final Player player = event.getPlayer();
		final Block block = event.getBlock();
		final BlockState state = block.getState();
		final Location location = block.getLocation();
		
		if(state instanceof Sign) {
			if(LiftSign.isPublicLiftSign(location)) {
				
				final PublicLiftSign publicLiftSign;
				try {
					publicLiftSign = new PublicLiftSign(state);
				}
				catch(SignLiftException e) {
					return;
				}
				
				if(publicLiftSign.canRemove(player)) {
					player.sendMessage(ConfigMessage.getLiftsignMaintainRemovePublicAllow());
				}
				else {
					player.sendMessage(ConfigMessage.getLiftsignMaintainRemovePublicDeny());
					
					event.setCancelled(true);
					plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
						
						@Override
		                public void run() {
							
		                    final BlockState state = block.getState();
		                    if (state instanceof Sign) {
		                    	((Sign) state).update();
		                    }
		                }
		            });
				}
			}
			else if(LiftSign.isPrivateLiftSign(location)) {
				
				final PrivateLiftSign privateLiftSign = plugin.getPrivateLiftSign(location);
				if(privateLiftSign == null) {
					return;
				}
				
				if(privateLiftSign.canRemove(player)) {
					plugin.removePrivateLiftSign(privateLiftSign.getLocation());
					player.sendMessage(ConfigMessage.getLiftsignMaintainRemovePrivateAllow());
				}
				else {
					player.sendMessage(ConfigMessage.getLiftsignMaintainRemovePrivateDeny());
					
					event.setCancelled(true);
					plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
						
						@Override
		                public void run() {
							
		                    final BlockState state = block.getState();
		                    if (state instanceof Sign) {
		                    	((Sign) state).update();
		                    }
		                }
		            });
				}
			}
		}
		else {
			
			boolean canRemove = true;
			boolean liftSignFound = false;
			final ArrayList<PrivateLiftSign> privateLiftSigns = new ArrayList<PrivateLiftSign>();
			final BlockFace[] blockFaces = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP};
			
			for(final BlockFace blockFace : blockFaces) {
				
				final Block neighborBlock = block.getRelative(blockFace);
				final BlockState neighborState = neighborBlock.getState();
				
				if(!(neighborState instanceof Sign)) {
					continue;
				}
				
				final BlockData blockData = neighborBlock.getBlockData();
				if(blockData instanceof WallSign) {
					
					final WallSign wallSign = (WallSign) blockData;
					final Block attachedBlock = neighborBlock.getRelative(wallSign.getFacing().getOppositeFace());
					if(!(attachedBlock.getLocation().equals(block.getLocation()))) {
						continue;
					}
				}
				else if(blockData instanceof org.bukkit.block.data.type.Sign) {
					
					final Block attachedBlock = neighborBlock.getRelative(BlockFace.DOWN);
					if(!(attachedBlock.getLocation().equals(block.getLocation()))) {
						continue;
					}
				}
				
				final Location neighborLocation = neighborBlock.getLocation();
				
				if(LiftSign.isPublicLiftSign(neighborLocation)) {
					
					final PublicLiftSign publicLiftSign;
					try {
						publicLiftSign = new PublicLiftSign(neighborState);
					}
					catch(SignLiftException e) {
						continue;
					}
					
					liftSignFound = true;
					if(!publicLiftSign.canRemove(player)) {
						canRemove = false;
						break;
					}
				}
				else if(LiftSign.isPrivateLiftSign(neighborLocation)) {
					
					final PrivateLiftSign privateLiftSign = plugin.getPrivateLiftSign(neighborLocation);
					if(privateLiftSign == null) {
						continue;
					}
					
					liftSignFound = true;
					if(!privateLiftSign.canRemove(player)) {
						canRemove = false;
						break;
					}
					
					privateLiftSigns.add(privateLiftSign);
				}
			}
			
			if(!liftSignFound) {
				return;
			}
			
			if(canRemove) {
				
				for(final PrivateLiftSign privateLiftSign : privateLiftSigns) {
					if(!plugin.removePrivateLiftSign(privateLiftSign.getLocation())){
						player.sendMessage(ConfigMessage.getLiftsignFileErrorDelete());
					}
				}
				player.sendMessage(ConfigMessage.getLiftsignMaintainRemoveMultipleAllow());
			}
			else {
				
				event.setCancelled(true);
				player.sendMessage(ConfigMessage.getLiftsignMaintainRemoveMultipleDeny());
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockCanBuild(final BlockCanBuildEvent event) {
		
		final Player player = event.getPlayer();
		final Block block = event.getBlock();
		final BlockState state = block.getState();
		
		if(!(state instanceof Sign)) {
			return;
		}
		
		final Location location = block.getLocation();
		final boolean isLiftSign;
		final boolean isPrivate;
		
		if(LiftSign.isPublicLiftSign(location)) {
			isLiftSign = true;
			isPrivate = false;
		}
		else if(LiftSign.isPrivateLiftSign(location)) {
			isLiftSign = true;
			isPrivate = true;
		}
		else {
			isLiftSign = false;
			isPrivate = false;
		}
		
		if(!isLiftSign) {
			return;
		}
		
		event.setBuildable(false);
		
		if(isPrivate) {
			player.sendMessage(ConfigMessage.getLiftsignMaintainBuildDenyPrivate());
		}
		else {
			player.sendMessage(ConfigMessage.getLiftsignMaintainBuildDenyPublic());
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockPlace(final BlockPlaceEvent event) {
		
		final Player player = event.getPlayer();
		final Block blockAgainst = event.getBlockAgainst();
		final BlockState stateAgainst = blockAgainst.getState();
		
		if(!(stateAgainst instanceof Sign)) {
			return;
		}
		
		final Location locationAgainst = blockAgainst.getLocation();
		final boolean isLiftSign;
		final boolean isPrivate;
		
		if(LiftSign.isPublicLiftSign(locationAgainst)) {
			isLiftSign = true;
			isPrivate = false;
		}
		else if(LiftSign.isPrivateLiftSign(locationAgainst)) {
			isLiftSign = true;
			isPrivate = true;
		}
		else {
			isLiftSign = false;
			isPrivate = false;
		}
		
		if(!isLiftSign) {
			return;
		}
		
		event.setCancelled(true);
		
		if(isPrivate) {
			player.sendMessage(ConfigMessage.getLiftsignMaintainBuildDenyPrivate());
		}
		else {
			player.sendMessage(ConfigMessage.getLiftsignMaintainBuildDenyPublic());
		}
	}
	
	@EventHandler
	public void onPlayerCommandSend(final PlayerCommandSendEvent event) {
		event.getCommands().removeAll(plugin.onPlayerCommandSend(event.getPlayer()));
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerInteract(final PlayerInteractEvent event) {
		
		if(!event.hasBlock()) {
			return;
		}
		
		final Player player = event.getPlayer();
		
		if(event.getAction() == Action.LEFT_CLICK_BLOCK) {
			
			final Block block = event.getClickedBlock();
			if(block == null) {
				return;
			}
			
			final Location location = block.getLocation();
			if(plugin.isPendingModification(player)) {
				
				if(LiftSign.isPrivateLiftSign(location)) {
					plugin.modifyPrivateLiftSign(location, player);
				}
				else if(LiftSign.isPublicLiftSign(location)) {
					player.sendMessage(ConfigMessage.getLiftsignMaintainModifyPublicDeny());
				}
				else {
					player.sendMessage(ConfigMessage.getLiftsignMaintainModifyOtherDeny());
				}
				
				plugin.removePendingModification(player);
				event.setCancelled(true);
				return;
			}
			else if(plugin.isPendingInformation(player)) {
				
				plugin.getInformation(location, player);
				event.setCancelled(true);
				return;
			}
		}
		
		if(event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}
		
		Block block = event.getClickedBlock();
		if(block == null) {
			return;
		}
		
		BlockState state = block.getState();
		final Material material = block.getType();
		
		if(material == Material.STONE_BUTTON) {
			block = block.getRelative(((Directional) block.getBlockData()).getFacing(), -2);
		}
		
		if(block == null) {
			return;
		}
		
		state = block.getState();
		if(!(state instanceof Sign)) {
			return;
		}
		
		final Location location = block.getLocation();
		if(LiftSign.isPublicLiftSign(location)) {
			
			final PublicLiftSign publicLiftSign;
			try {
				publicLiftSign = new PublicLiftSign((Sign) state);
			}
			catch(SignLiftException e) {
				return;
			}
			
			publicLiftSign.activate(player, plugin);
		}
		else if(LiftSign.isPrivateLiftSign(location)) {
			
			final PrivateLiftSign privateLiftSign = plugin.getPrivateLiftSign(location);
			if(privateLiftSign == null) {
				return;
			}
			
			plugin.usePrivateLiftSign(privateLiftSign.getLocation(), player);
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(final PlayerJoinEvent event) {
		plugin.onPlayerJoin(event.getPlayer());
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onSignChange(final SignChangeEvent event) {
		
		final String[] lines = event.getLines();
		final String liftLine = lines[1];
		if(liftLine.length() < 3) {
			return;
		}
		
		if(liftLine.length() < 3) {
			return;
		}
		
		final String liftDirection = liftLine.substring(1, liftLine.length() - 1);
		if(!liftDirection.equalsIgnoreCase(ConfigData.getLiftsignDirectionNone()) && !liftDirection.equalsIgnoreCase(ConfigData.getLiftsignDirectionUp()) && !liftDirection.equalsIgnoreCase(ConfigData.getLiftsignDirectionDown())) {
			return;
		}
		
		final Player player = event.getPlayer();
		final boolean isLiftSign;
		final boolean canBuild;
		final boolean isPrivate;
		
		if(liftLine.startsWith(ConfigData.getLiftsignPublicOpen()) && liftLine.endsWith(ConfigData.getLiftsignPublicClose())) {
			
			isLiftSign = true;
			canBuild = player.hasPermission("signlift.create.public");
			isPrivate = false;
		}
		else if(liftLine.startsWith(ConfigData.getLiftsignPrivateOpen()) && liftLine.endsWith(ConfigData.getLiftsignPrivateClose())) {
			
			isLiftSign = true;
			canBuild = player.hasPermission("signlift.create.private");
			isPrivate = true;
		}
		else {
			
			isLiftSign = false;
			canBuild = false;
			isPrivate = false;
		}
		
		
		if(!isLiftSign) {
			return;
		}
		
		final Block block = event.getBlock();
		final Location location = block.getLocation();
		
		if(!canBuild) {
			
			if(isPrivate) {
				player.sendMessage(ConfigMessage.getLiftsignMaintainCreatePrivateDeny());
			}
			else {
				player.sendMessage(ConfigMessage.getLiftsignMaintainCreatePublicDeny());
			}
			
			event.setCancelled(true);
			block.setType(Material.AIR);
			
			final Material material = getSignToDrop(player);
			if(material == null) {
				return;
			}
			
			block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(material, 1));
			return;
		}
		
		if(isPrivate) {
			try {
				final PrivateLiftSign privateLiftSign = new PrivateLiftSign(location, lines, player);
				plugin.addPrivateLiftSign(privateLiftSign, player);
				player.sendMessage(ConfigMessage.getLiftsignMaintainCreatePrivateAllow());
			}
			catch(SignLiftException e) {
				event.setCancelled(true);
				player.sendMessage(ConfigMessage.getLiftsignMaintainCreatePrivateDeny());
				block.setType(Material.AIR);
				
				final Material material = getSignToDrop(player);
				if(material == null) {
					return;
				}
				
				block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(material, 1));
			}
		}
		else {
			try {
				new PublicLiftSign(location, lines);
				player.sendMessage(ConfigMessage.getLiftsignMaintainCreatePublicAllow());
			}
			catch(SignLiftException e) {
				event.setCancelled(true);
				player.sendMessage(ConfigMessage.getLiftsignMaintainCreatePublicDeny());
				block.setType(Material.AIR);
				
				final Material material = getSignToDrop(player);
				if(material == null) {
					return;
				}
				
				block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(material, 1));
			}
		}
	}
	
	@EventHandler
	public void onTabComplete(final TabCompleteEvent event) {
		
		final List<String> completions = plugin.onTabComplete(event.getBuffer(), event.getSender());
		if(completions == null) {
			return;
		}
		
		event.setCompletions(completions);
	}
	
	private Material getSignToDrop(final Player player) {
		
		final PlayerInventory playerInventory = player.getInventory();
		final Material mainHand = playerInventory.getItemInMainHand().getType();
		final Material offHand = playerInventory.getItemInOffHand().getType();
		
		if(getSign(mainHand) != null) {
			return mainHand;
		}
		else if(getSign(offHand) != null) {
			return offHand;
		}
		else {
			return null;
		}
	}
	
	private Material getSign(final Material material) {
		
		if(material == Material.ACACIA_SIGN) {
			return Material.ACACIA_SIGN;
		}
		else if(material == Material.BIRCH_SIGN) {
			return Material.BIRCH_SIGN;
		}
		else if(material == Material.DARK_OAK_SIGN) {
			return Material.DARK_OAK_SIGN;
		}
		else if(material == Material.JUNGLE_SIGN) {
			return Material.JUNGLE_SIGN;
		}
		else if(material == Material.OAK_SIGN) {
			return Material.OAK_SIGN;
		}
		else if(material == Material.SPRUCE_SIGN) {
			return Material.SPRUCE_SIGN;
		}
		else {
			return null;
		}
	}
}
