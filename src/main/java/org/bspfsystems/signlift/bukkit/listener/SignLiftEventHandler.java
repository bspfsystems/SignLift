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

package org.bspfsystems.signlift.bukkit.listener;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.bspfsystems.signlift.bukkit.SignLiftPlugin;
import org.bspfsystems.signlift.bukkit.config.ConfigData;
import org.bspfsystems.signlift.bukkit.config.ConfigMessage;
import org.bspfsystems.signlift.bukkit.exception.SignLiftException;
import org.bspfsystems.signlift.bukkit.liftsign.LiftSign;
import org.bspfsystems.signlift.bukkit.liftsign.PrivateLiftSign;
import org.bspfsystems.signlift.bukkit.liftsign.PublicLiftSign;

/**
 * Represents a {@link Listener} for all {@link Event}s that are relevant to
 * the {@link SignLiftPlugin}.
 */
public final class SignLiftEventHandler implements Listener {
    
    private final SignLiftPlugin signLiftPlugin;
    private final Logger logger;
    
    /**
     * Constructs a new {@link SignLiftEventHandler}.
     * 
     * @param signLiftPlugin The {@link SignLiftPlugin}.
     */
    public SignLiftEventHandler(@NotNull final SignLiftPlugin signLiftPlugin) {
        this.signLiftPlugin = signLiftPlugin;
        this.logger = this.signLiftPlugin.getLogger();
    }
    
    /**
     * Runs when {@link Block}s are broken by {@link Player}s to handle any
     * {@link Block}s that are {@link LiftSign}s or attached.
     * 
     * @param event The {@link BlockBreakEvent}.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(final BlockBreakEvent event) {
        
        final Player player = event.getPlayer();
        final Block block = event.getBlock();
        final BlockState state = block.getState();
        final Location location = block.getLocation();
        
        int signResult = -1;
        
        // Block is a Sign of some sort.
        if (state instanceof Sign && (signResult = this.checkSign(player, (Sign) state)) == 0) {
            event.setCancelled(true);
            if (LiftSign.isPublicLiftSign(location)) {
                player.sendMessage(ConfigMessage.getLiftsignRemovePublicDeny());
            } else {
                player.sendMessage(ConfigMessage.getLiftsignRemovePrivateDeny());
            }
            return;
        }
        
        // Block might have a LiftSign on it (or Sign chained to it).
        final int blockResult = this.checkBlocksAround(player, block, BlockFace.UP);
        if (blockResult == 0) {
            event.setCancelled(true);
            player.sendMessage(ConfigMessage.getLiftsignRemoveAttachedDeny());
            return;
        }
        
        // At least 1 chained LiftSign has been found that can be removed.
        if (blockResult == 1) {
            player.sendMessage(ConfigMessage.getLiftsignRemoveAttachedAllow());
            return;
        }
        
        // Only the given Block is a LiftSign, can be removed.
        if (signResult == 1) {
            if (LiftSign.isPublicLiftSign(location)) {
                player.sendMessage(ConfigMessage.getLiftsignRemovePublicAllow());
            } else {
                player.sendMessage(ConfigMessage.getLiftsignRemovePrivateAllow());
            }
        }
    }
    
    /**
     * Checks to see if the specified {@link Sign} can be broken by the
     * specified {@link Player}.
     * 
     * @param player The {@link Player} attempting to break the {@link Sign}.
     * @param sign The {@link Sign} attempting to be broken.
     * @return {@code -1} if the {@link Sign} is not a {@link LiftSign} and
     *         can be broken. {@code 1} if the {@link Sign} is a
     *         {@link LiftSign} and can be broken. Otherwise, {@code 0} (is a
     *         {@link LiftSign}, but cannot be broken).
     */
    private int checkSign(@NotNull final Player player, @NotNull final Sign sign) {
        
        final Location location = sign.getLocation();
        if (LiftSign.isPublicLiftSign(location)) {
            final PublicLiftSign liftSign;
            try {
                liftSign = new PublicLiftSign(sign);
            } catch (SignLiftException e) {
                this.logger.log(Level.WARNING, "PublicLiftSign found at Location, cannot \"create\".");
                this.logger.log(Level.WARNING, "World: " + (location.getWorld() == null ? "null" : location.getWorld().getName()));
                this.logger.log(Level.WARNING, "X: " + location.getBlockX());
                this.logger.log(Level.WARNING, "Y: " + location.getBlockY());
                this.logger.log(Level.WARNING, "Z: " + location.getBlockZ());
                this.logger.log(Level.WARNING, e.getClass().getSimpleName() + " thrown.", e);
                return -1; // Not LiftSign somehow, show LiftSign not found.
            }
            return liftSign.canRemove(player) ? 1 : 0;
        } else if (LiftSign.isPrivateLiftSign(location)) {
            final PrivateLiftSign liftSign = this.signLiftPlugin.getPrivateLiftSign(location);
            if (liftSign == null) {
                this.logger.log(Level.WARNING, "PrivateLiftSign found at Location, cannot retrieve from plugin.");
                this.logger.log(Level.WARNING, "World: " + (location.getWorld() == null ? "null" : location.getWorld().getName()));
                this.logger.log(Level.WARNING, "X: " + location.getBlockX());
                this.logger.log(Level.WARNING, "Y: " + location.getBlockY());
                this.logger.log(Level.WARNING, "Z: " + location.getBlockZ());
                return -1; // Not LiftSign somehow, show LiftSign not found.
            }
            return liftSign.canRemove(player) ? 1 : 0;
        }
        
        return -1; // No LiftSign found.
    }
    
    /**
     * Checks to see if the {@link Block}s surrounding the given {@link Block}
     * (always excluding the {@link BlockFace#UP} direction, and the given
     * excluded direction) can be broken.
     * <p>
     * This is used as the surrounding {@link Block}s could be other
     * {@link Sign}s that may not be allowed to break if they are
     * {@link LiftSign}s. Because {@link Sign}s are allowed to be built off of
     * one another, we must check all the way to the end of any chain to verify
     * that a {@link LiftSign} that would otherwise not be able to be broken
     * will not get broken.
     * 
     * @param player The {@link Player} attempting to break the {@link Block}
     *               (or chained {@link Block} that this one is a part of).
     * @param block The {@link Block} to check around.
     * @param exclude Any other {@link BlockFace} to exclude (other than
     *                {@link BlockFace#UP}). This is usually set to
     *                {@link BlockFace#UP} when no other {@link Block} is to be
     *                excluded.
     * @return {@code -1} if there are no {@link LiftSign}s surrounding the
     *         {@link Block}, and removal can happen. {@link 1} if there is at
     *         least 1 {@link LiftSign} surrounding, and it can be broken.
     *         Otherwise, {@link 0} if there is at least 1 {@link LiftSign} and
     *         it cannot be broken.
     */
    private int checkBlocksAround(@NotNull final Player player, @NotNull final Block block, @NotNull final BlockFace exclude) {
        
        boolean foundSign = false;
        final BlockFace[] blockFaces = { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP };
        for (final BlockFace blockFace : blockFaces) {
            
            if (exclude == blockFace) {
                continue;
            }
            
            final Block neighborBlock = block.getRelative(blockFace);
            final BlockState  neighborState = neighborBlock.getState();
            if (!(neighborState instanceof Sign)) {
                continue; // Not a Sign, does not matter.
            }
            
            final Sign neighborSign = (Sign) neighborState;
            final BlockData neighborData = neighborBlock.getBlockData();
            if (neighborData instanceof WallSign) {
                final WallSign neighborWallSign = (WallSign) neighborData;
                if (!neighborBlock.getRelative(neighborWallSign.getFacing().getOppositeFace()).getLocation().equals(block.getLocation())) {
                    continue; // WallSign is not attached to the given Block.
                }
            } else if (neighborData instanceof org.bukkit.block.data.type.Sign) {
                if (!neighborBlock.getRelative(BlockFace.DOWN).getLocation().equals(block.getLocation())) {
                    continue; // "Normal" Sign is not attached to the given Block.
                }
            }
            
            final int signResult = this.checkSign(player, neighborSign);
            if (signResult == 0) {
                 return 0;
            }
            foundSign = foundSign || signResult == 1;
            
            final int blockResult = this.checkBlocksAround(player, neighborBlock, blockFace.getOppositeFace());
            if (blockResult == 0) {
                return 0;
            }
            
            foundSign = foundSign || blockResult == 1;
        }
        
        return foundSign ? 1 : -1;
    }
    
    /**
     * Runs when a {@link Player} attempts to place a {@link Block}. Allows
     * handling of placing {@link Block}s on {@link LiftSign}s.
     * 
     * @param event The {@link BlockCanBuildEvent}.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockCanBuild(final BlockCanBuildEvent event) {
        
        final Player player = event.getPlayer();
        final Block block = event.getBlock();
        final BlockState state = block.getState();
        
        if (!(state instanceof Sign)) {
            return;
        }
        
        final Location location = block.getLocation();
        final boolean isPrivate;
        
        // See if the Block is being placed on a LiftSign. If so, prevent
        // that from happening.
        if (LiftSign.isPublicLiftSign(location)) {
            isPrivate = false;
        } else if (LiftSign.isPrivateLiftSign(location)) {
            isPrivate = true;
        } else {
            return;
        }
        
        event.setBuildable(false);
        
        if (isPrivate) {
            player.sendMessage(ConfigMessage.getLiftsignBuildDenyPrivate());
        } else {
            player.sendMessage(ConfigMessage.getLiftsignBuildDenyPublic());
        }
    }
    
    /**
     * Runs when a {@link Player} places a {@link Block}. Allows handling of
     * placing {@link Block}s on {@link LiftSign}s.
     * 
     * @param event The {@link BlockPlaceEvent}.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(final BlockPlaceEvent event) {
        
        final Player player = event.getPlayer();
        final Block blockAgainst = event.getBlockAgainst();
        final BlockState stateAgainst = blockAgainst.getState();
        
        if (!(stateAgainst instanceof Sign)) {
            return;
        }
        
        final Location locationAgainst = blockAgainst.getLocation();
        final boolean isPrivate;
        
        // See if the Block is being placed on a LiftSign. If so, prevent
        // that from happening.
        if (LiftSign.isPublicLiftSign(locationAgainst)) {
            isPrivate = false;
        } else if (LiftSign.isPrivateLiftSign(locationAgainst)) {
            isPrivate = true;
        } else {
            return;
        }
        
        event.setCancelled(true);
        if (isPrivate) {
            player.sendMessage(ConfigMessage.getLiftsignBuildDenyPrivate());
        } else {
            player.sendMessage(ConfigMessage.getLiftsignBuildDenyPublic());
        }
    }
    
    /**
     * Runs when a {@link Player} performs tab-completion for {@link Command}s.
     * Allows the removal of unwanted {@link Command} suggestions, or ones that
     * the {@link Player} does not have permission for.
     * 
     * @param event The {@link PlayerCommandSendEvent}.
     */
    @EventHandler
    public void onPlayerCommandSend(final PlayerCommandSendEvent event) {
        event.getCommands().removeAll(signLiftPlugin.onPlayerCommandSend(event.getPlayer()));
    }
    
    /**
     * Runs when a {@link Player} performs any interaction. Allows handling of
     * interacting with {@link LiftSign}s, and the operations that they perform.
     * 
     * @param event The {@link PlayerInteractEvent}.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(final PlayerInteractEvent event) {
        
        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }
        
        final Location location = block.getLocation();
        final Player player = event.getPlayer();
        
        switch (event.getAction()) {
            
            // Check for changing memberships on a PrivateLiftSign.
            case LEFT_CLICK_BLOCK:
                
                // Player has prepared the modification command.
                if (this.signLiftPlugin.isPendingModification(player)) {
                    
                    if (LiftSign.isPrivateLiftSign(location)) {
                        this.signLiftPlugin.modifyPrivateLiftSign(location, player);
                    } else if (LiftSign.isPublicLiftSign(location)) {
                        player.sendMessage(ConfigMessage.getLiftsignModifyPublic());
                    } else {
                        player.sendMessage(ConfigMessage.getLiftsignModifyOther());
                    }
                    
                    if (!this.signLiftPlugin.removePendingModification(player)) {
                        this.logger.log(Level.WARNING, "Pending modification was not stored for player.");
                        this.logger.log(Level.WARNING, "Player Name: " + player.getName());
                        this.logger.log(Level.WARNING, "Player UUID: " + player.getUniqueId());
                        this.logger.log(Level.WARNING, "No pending modification data stored (\"null\" stored).");
                    }
                    event.setCancelled(true);
                    
                // Player has prepared the information command.
                } else if (this.signLiftPlugin.isPendingInformation(player)) {
                    
                    this.signLiftPlugin.getInformation(location, player);
                    event.setCancelled(true);
                }
                break;
                
            // Check for trying to use a LiftSign.
            case RIGHT_CLICK_BLOCK:
                
                // If stone button, get the Block on the other side of the one
                // that the button is attached to.
                final Material material = block.getType();
                if (material == Material.STONE_BUTTON) {
                    block = block.getRelative(((Directional) block.getBlockData()).getFacing(), -2);
                }
                
                final BlockState state = block.getState();
                if (!(state instanceof Sign)) {
                    return;
                }
                
                if (LiftSign.isPublicLiftSign(location)) {
                    
                    final PublicLiftSign liftSign;
                    try {
                        liftSign = new PublicLiftSign(state);
                    } catch (SignLiftException e) {
                        this.logger.log(Level.WARNING, "PublicLiftSign found at Location, cannot \"create\".");
                        this.logger.log(Level.WARNING, "World: " + (location.getWorld() == null ? "null" : location.getWorld().getName()));
                        this.logger.log(Level.WARNING, "X: " + location.getBlockX());
                        this.logger.log(Level.WARNING, "Y: " + location.getBlockY());
                        this.logger.log(Level.WARNING, "Z: " + location.getBlockZ());
                        this.logger.log(Level.WARNING, e.getClass().getSimpleName() + " thrown.", e);
                        return; // Not LiftSign somehow, show LiftSign not found.
                    }
                    liftSign.activate(player, this.signLiftPlugin);
                } else if (LiftSign.isPrivateLiftSign(location)) {
                    
                    final PrivateLiftSign liftSign = this.signLiftPlugin.getPrivateLiftSign(location);
                    if (liftSign == null) {
                        this.logger.log(Level.WARNING, "PrivateLiftSign found at Location, cannot retrieve from plugin.");
                        this.logger.log(Level.WARNING, "World: " + (location.getWorld() == null ? "null" : location.getWorld().getName()));
                        this.logger.log(Level.WARNING, "X: " + location.getBlockX());
                        this.logger.log(Level.WARNING, "Y: " + location.getBlockY());
                        this.logger.log(Level.WARNING, "Z: " + location.getBlockZ());
                        return; // Not LiftSign somehow, show LiftSign not found.
                    }
                    this.signLiftPlugin.usePrivateLiftSign(liftSign.getLocation(), player);
                }
                break;
                
            // Literally any other action, do nothing.
            default:
                break;
        }
    }
    
    /**
     * Runs when a {@link Player} joins the server. Used to get the name to
     * {@link UUID} mapping of the {@link Player}.
     * 
     * @param event The {@link PlayerJoinEvent}.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        this.signLiftPlugin.onPlayerJoin(event.getPlayer());
    }
    
    /**
     * Runs when a {@link Sign} is changed (usually when it is placed and the
     * {@link Player} finishes editing the {@link Sign} text) to determine
     * if a {@link LiftSign} was attempting to be created. If one was created
     * incorrectly, an error will be given to the {@link Player}.
     * 
     * @param event The {@link SignChangeEvent}.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSignChange(final SignChangeEvent event) {
        
        final String[] lines = event.getLines();
        final String liftLine = lines[1];
        if (liftLine.length() < 3) {
            return; // Ignore short lines.
        }
        
        final String liftDirection = liftLine.substring(1, liftLine.length() - 1);
        if (!liftDirection.equalsIgnoreCase(ConfigData.getDirectionNone()) && !liftDirection.equalsIgnoreCase(ConfigData.getDirectionUp()) && !liftDirection.equalsIgnoreCase(ConfigData.getDirectionDown())) {
            return; // Ignore text that does not match a lift direction.
        }
        
        final Block block = event.getBlock();
        final Player player = event.getPlayer();
        final boolean isPrivate;
        
        if (liftLine.startsWith(ConfigData.getPublicStart()) && liftLine.endsWith(ConfigData.getPublicEnd())) {
            isPrivate = false;
            
            // Check for PublicLiftSign build permission.
            if (!player.hasPermission("signlift.create.public")) {
                player.sendMessage(ConfigMessage.getLiftsignCreatePublicDeny());
                event.setCancelled(true);
                this.returnSign(block, player);
                return;
            }
        } else if (liftLine.startsWith(ConfigData.getPrivateStart()) && liftLine.endsWith(ConfigData.getPrivateEnd())) {
            isPrivate = true;
            
            // Check for PrivateLiftSign build permission.
            if (!player.hasPermission("signlift.create.private")) {
                player.sendMessage(ConfigMessage.getLiftsignCreatePrivateDeny());
                event.setCancelled(true);
                this.returnSign(block, player);
                return;
            }
        } else {
            return;
        }
        
        final Location location = block.getLocation();
        
        // Create the LiftSign.
        try {
            if (isPrivate) {
                this.signLiftPlugin.addPrivateLiftSign(new PrivateLiftSign(location, lines, player), player);
                player.sendMessage(ConfigMessage.getLiftsignCreatePrivateAllow());
            } else {
                new PublicLiftSign(location, lines);
                player.sendMessage(ConfigMessage.getLiftsignCreatePublicAllow());
            }
        } catch (SignLiftException e) {
            
            this.logger.log(Level.INFO, "Player did not create LiftSign correctly, no need to panic.");
            this.logger.log(Level.INFO, "Player: " + player.getName());
            this.logger.log(Level.INFO, "UUID: " + player.getUniqueId().toString());
            this.logger.log(Level.INFO, "Type: " + (isPrivate ? "PRIVATE" : "PUBLIC"));
            this.logger.log(Level.INFO, "World: " + (location.getWorld() == null ? "null" : location.getWorld().getName()));
            this.logger.log(Level.INFO, "X: " + location.getBlockX());
            this.logger.log(Level.INFO, "Y: " + location.getBlockY());
            this.logger.log(Level.INFO, "Z: " + location.getBlockZ());
            this.logger.log(Level.INFO, "Line 1: " + lines[0]);
            this.logger.log(Level.INFO, "Line 2: " + lines[1]);
            this.logger.log(Level.INFO, "Line 3: " + lines[3]);
            this.logger.log(Level.INFO, "Line 4: " + lines[4]);
            this.logger.log(Level.INFO, e.getClass().getSimpleName() + " thrown.", e);
            
            event.setCancelled(true);
            this.returnSign(block, player);
            
            if (isPrivate) {
                player.sendMessage(ConfigMessage.getLiftsignCreatePublicError());
            } else {
                player.sendMessage(ConfigMessage.getLiftsignCreatePrivateError());
            }
        }
    }
    
    /**
     * Returns the specified {@link Block} (which should be a {@link Sign}) to
     * the specified {@link Player}.
     * <p>
     * This will return nothing if the {@link Block} is not a {@link Sign}.
     * 
     * @param block The {@link Block} ({@link Sign}) to return.
     * @param player The {@link Player} to return the {@link Sign} to.
     */
    private void returnSign(@NotNull final Block block, @NotNull final Player player) {
        
        if (!(block.getState() instanceof Sign)) {
            return;
        }
        
        block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(block.getType(), 1));
        block.setType(Material.AIR);
    }
    
    /*
    private Material getSignToDrop(final Player player) {
        
        final PlayerInventory playerInventory = player.getInventory();
        final Material mainHand = playerInventory.getItemInMainHand().getType();
        final Material offHand = playerInventory.getItemInOffHand().getType();
        
        if (this.getSign(mainHand) != null) {
            return mainHand;
        } else if(this.getSign(offHand) != null) {
            return offHand;
        } else {
            return null;
        }
    }
    */
    
    /*
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
    */
}
