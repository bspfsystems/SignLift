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

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.bspfsystems.signlift.bukkit.SignLiftPlugin;
import org.bspfsystems.signlift.bukkit.config.ConfigData;
import org.bspfsystems.signlift.bukkit.config.ConfigMessage;
import org.bspfsystems.signlift.bukkit.exception.SignLiftException;

/**
 * Represents the base lift sign that can be used for vertical transportation.
 */
public abstract class LiftSign {
    
    /**
     * Represents the direction of the {@link LiftSign}.
     */
    public enum Direction {
        UP,
        DOWN,
        NONE;
    }
    
    protected final World world;
    protected final int x;
    protected final int y;
    protected final int z;
    protected final String label;
    protected final Direction direction;
    
    /**
     * Creates a new {@link LiftSign} from the given {@link Block}.
     * 
     * @param block The {@link Block} to create the {@link LiftSign} from.
     * @throws SignLiftException If the given {@link Block} is not a
     *                           {@link Sign} or has no
     *                           {@link LiftSign.Direction}.
     * @see LiftSign#LiftSign(BlockState)
     */
    public LiftSign(@NotNull final Block block) throws SignLiftException {
        this(block.getState());
    }
    
    /**
     * Creates a new {@link LiftSign} from the given {@link BlockState}.
     * <p>
     * The {@link BlockState} must be a {@link Sign} and have a
     * {@link LiftSign.Direction} associated with it.
     * 
     * @param state The {@link BlockState} to create the {@link LiftSign} from.
     * @throws SignLiftException If the given {@link BlockState} is not a
     *                           {@link Sign} or has no
     *                           {@link LiftSign.Direction}.
     */
    public LiftSign(@NotNull final BlockState state) throws SignLiftException {
        
        if (!(state instanceof Sign)) {
            throw new SignLiftException("Block is not a sign.");
        }
        
        final Sign sign = (Sign) state;
        final String liftLine = sign.getLine(1);
        
        LiftSign.validateLiftLine(liftLine);
        
        final Location location = sign.getLocation();
        this.world = location.getWorld();
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();
        this.label = sign.getLine(0);
        this.direction = LiftSign.getLiftDirection(liftLine.substring(1, liftLine.length() - 1));
        
        if (this.world == null) {
            throw new SignLiftException("Location is in a null World.");
        }
        if (this.direction == null) {
            throw new SignLiftException("Sign lift line does not have a valid direction.");
        }
    }
    
    /**
     * Creates a new {@link LiftSign} at the given {@link Location} with the
     * given {@link String} array of lines.
     * <p>
     * The given {@link Location} must contain a {@link Sign}, and the lines
     * must have valid lift lines on them. This is most commonly used when the
     * lines on a {@link Sign} have been changed.
     * 
     * @param location The location to use for the {@link LiftSign}.
     * @param lines The lines that will be applied to the {@link LiftSign}.
     * @throws SignLiftException If the given {@link BlockState} is not a
     *                           {@link Sign}, the {@link World} is
     *                           {@code null}, or has no
     *                           {@link LiftSign.Direction}.
     * @see SignChangeEvent
     */
    public LiftSign(@NotNull final Location location, @NotNull final String[] lines) throws SignLiftException {
        
        if (!(location.getBlock().getState() instanceof Sign)) {
            throw new SignLiftException("Block at location is not a sign.");
        }
        
        final String liftLine = lines[1];
        LiftSign.validateLiftLine(liftLine);
        
        this.world = location.getWorld();
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();
        this.label = lines[0];
        this.direction = LiftSign.getLiftDirection(liftLine.substring(1, liftLine.length() - 1));
    
        if (this.world == null) {
            throw new SignLiftException("Location is in a null World.");
        }
        if (this.direction == null) {
            throw new SignLiftException("Lift lines do not have a valid direction.");
        }
    }
    
    /**
     * Gets whether or not this {@link LiftSign} is private or not.
     * 
     * @return {@code true} if this {@link LiftSign} is private, {@code false}
     *         otherwise.
     */
    public abstract boolean isPrivate();
    
    /**
     * Determines if the given {@link Player} can remove (destroy) this
     * {@link LiftSign}.
     * 
     * @param player The {@link Player} attempting to remove this
     *               {@link LiftSign}.
     * @return {@code true} if the {@link Player} can remove this
     *         {@link LiftSign}, {@code false} otherwise.
     */
    public abstract boolean canRemove(@NotNull final Player player);
    
    /**
     * Determines if the given {@link Player} can use (activate) this
     * {@link LiftSign}.
     * 
     * @param player The {@link Player} attempting to use this
     *               {@link LiftSign}.
     * @return {@code true} if the {@link Player} can use this
     *         {@link LiftSign}, {@code false} otherwise.
     */
    public abstract boolean canUse(@NotNull final Player player);
    
    /**
     * Triggered when the given {@link Player} attempts to activate this
     * {@link LiftSign} (right-clicks on it).
     * <p>
     * This will attempt to teleport the {@link Player}, governed by the safety
     * of teleporting the {@link Player}, their permissions, whether the
     * {@link LiftSign} has a destination, and other items.
     *
     * @param player The {@link Player} activating this {@link LiftSign}.
     * @param plugin The {@link SignLiftPlugin}.
     * @return {@code true} if this {@link LiftSign} was activated and the
     *         {@link Player} was attempted to be teleported, {@code false}
     *         otherwise.
     */
    public boolean activate(@NotNull final Player player, @NotNull final SignLiftPlugin plugin) {
    
        if (this.direction == Direction.NONE) {
            player.sendMessage(ConfigMessage.getLiftsignUseNoneDefault());
            return false;
        }
    
        final LiftSign destination = this.getDestination(plugin);
        if (destination == null) {
            player.sendMessage(this.isPrivate() ? ConfigMessage.getLiftsignUseDisconnectedPrivate() : ConfigMessage.getLiftsignUseDisconnectedPublic());
            return false;
        }
    
        if (!destination.canUse(player)) {
            player.sendMessage(this.isPrivate() ? ConfigMessage.getLiftsignUseDenyPrivate() : ConfigMessage.getLiftsignUseDenyPublic());
            return false;
        }
    
        final Location playerLocation = player.getLocation();
        final Location teleportLocation = playerLocation.clone();
    
        final Block destinationBlock = destination.world.getBlockAt(destination.x, destination.y, destination.z);
        boolean isSafe = false;
    
        if (destinationBlock.getY() < destinationBlock.getWorld().getMaxHeight()) {
            final Block destinationBlockOffset = destination.getAdjustedBlock(playerLocation, 1);
            teleportLocation.setY(destinationBlock.getY());
            isSafe = LiftSign.isBlockSafe(destinationBlock) && LiftSign.isBlockSafe(destinationBlockOffset);
        }
    
        if (destinationBlock.getY() > 0 && !isSafe) {
            final Block destinationBlockOffset = destination.getAdjustedBlock(playerLocation, -1);
            teleportLocation.setY(destinationBlock.getY() - 1);
            isSafe = LiftSign.isBlockSafe(destinationBlock) && LiftSign.isBlockSafe(destinationBlockOffset);
        }
    
        if (!isSafe) {
            player.sendMessage(this.isPrivate() ? ConfigMessage.getLiftsignUseBlockedPrivate() : ConfigMessage.getLiftsignUseBlockedPublic());
            return false;
        }
    
        player.teleport(teleportLocation);
        player.sendMessage(this.getTeleportMessage(destination));
        return true;
    }
    
    /**
     * Gets the {@link Location} of this {@link PrivateLiftSign}.
     *
     * @return The {@link Location} of this {@link PrivateLiftSign}.
     */
    @NotNull
    public final Location getLocation() {
        return new Location(this.world, this.x, this.y, this.z);
    }
    
    /**
     * Gets the destination {@link LiftSign} from this {@link LiftSign}, or
     * {@code null} if there is no destination
     * ({@link LiftSign.Direction#NONE} or no other {@link LiftSign} in the
     * corresponding vertical direction).
     *
     * @param plugin The {@link SignLiftPlugin}, used to obtain any
     *               destination {@link PrivateLiftSign}.
     * @return The destination {@link LiftSign} if one exists, or {@code null}.
     */
    @Nullable
    protected final LiftSign getDestination(@NotNull final SignLiftPlugin plugin) {
        
        final int worldHeight = this.world.getMaxHeight();
        final int change;
        if (this.direction == Direction.UP) {
            change = 1;
        } else if (this.direction == Direction.DOWN) {
            change = -1;
        } else {
            return null;
        }
        
        for (int checkY = y + change; 0 < checkY && checkY < worldHeight; checkY += change) {
            
            final Block block = this.world.getBlockAt(x, checkY, z);
            final BlockState state = block.getState();
            if (!(state instanceof Sign)) {
                continue;
            }
            
            final Location location = block.getLocation();
            if (LiftSign.isPublicLiftSign(location)) {
                try {
                    return new PublicLiftSign(state);
                } catch (SignLiftException e) {
                    return null;
                }
            } else if (LiftSign.isPrivateLiftSign(location)) {
                return plugin.getPrivateLiftSign(location);
            }
        }
        
        return null;
    }
    
    /**
     * Gets the {@link Block} that is the y-offset of the given
     * {@link Location}. This is usually used when determining if a
     * {@link Location} is safe to teleport to.
     * 
     * @param location The destination teleport {@link Location}.
     * @param offset The y-offset to check around the given {@link Location}.
     * @return The {@link Block} at the given {@link Location} and offset.
     */
    @NotNull
    protected final Block getAdjustedBlock(@NotNull final Location location, final int offset) {
        
        final int x;
        final int y;
        final int z;
        
        // Find the actual (X,Z) of the Block that the Player would teleport
        // into. Otherwise, just use the integer portion of the (X,Z)
        // coordinates to do the check.
        if (ConfigData.getCheckDestination()) {
            x = (int) Math.round(location.getX());
            y = this.y + offset;
            z = (int) Math.round(location.getZ());
        } else {
            x = location.getBlockX();
            y = this.y + offset;
            z = location.getBlockZ();
        }
        
        return this.world.getBlockAt(x, y, z);
    }
    
    /**
     * Gets the message that should be displayed in chat when a {@link Player}
     * teleports to the specified {@link LiftSign}.
     * 
     * @param destination The {@link LiftSign} that is the destination of this
     *                    {@link LiftSign}.
     * @return The message to display in chat.
     */
    @NotNull
    protected final String getTeleportMessage(@NotNull final LiftSign destination) {
        
        final String teleportMessage;
        if (this.direction == Direction.UP && destination.label.isEmpty()) {
            teleportMessage = ConfigMessage.getLiftsignUseUpDefault();
        } else if (this.direction == Direction.UP) {
            teleportMessage = ConfigMessage.getLiftsignUseUpCustom().replace("%%destination%%", destination.label);
        } else if (this.direction == Direction.DOWN && destination.label.isEmpty()) {
            teleportMessage = ConfigMessage.getLiftsignUseDownDefault();
        } else {
            teleportMessage = ConfigMessage.getLiftsignUseDownCustom().replace("%%destination%%", destination.label);
        }
        
        return teleportMessage;
    }
    
    /**
     * Checks to see if the given {@link Location} contains a {@link LiftSign}.
     * 
     * @param location The {@link Location} to check.
     * @return {@code true} if the {@link Location} contains a {@link LiftSign},
     *         {@code false} otherwise.
     * @see LiftSign#isPublicLiftSign(Location)
     * @see LiftSign#isPrivateLiftSign(Location)
     */
    public static boolean isLiftSign(@NotNull final Location location) {
        return LiftSign.isPublicLiftSign(location) || LiftSign.isPrivateLiftSign(location);
    }
    
    /**
     * Checks to see if the given {@link Block} is a {@link LiftSign}.
     * 
     * @param block The {@link Block} to check.
     * @return {@code true} if the {@link Block} is a {@link LiftSign},
     *         {@code false} otherwise.
     * @see LiftSign#isPublicLiftSign(Block)
     * @see LiftSign#isPrivateLiftSign(Block)
     */
    public static boolean isLiftSign(@NotNull final Block block) {
        return LiftSign.isPublicLiftSign(block) || LiftSign.isPrivateLiftSign(block);
    }
    
    /**
     * Checks to see if the given {@link BlockState} represents a
     * {@link LiftSign}.
     * 
     * @param state The {@link BlockState} to check.
     * @return {@code true} if the {@link BlockState} represents a
     *         {@link LiftSign}, {@code false} otherwise.
     * @see LiftSign#isPublicLiftSign(BlockState)
     * @see LiftSign#isPrivateLiftSign(BlockState)
     */
    public static boolean isLiftSign(@NotNull final BlockState state) {
        return LiftSign.isPublicLiftSign(state) || LiftSign.isPrivateLiftSign(state);
    }
    
    /**
     * Checks to see if the given {@link Location} contains a
     * {@link PublicLiftSign}.
     *
     * @param location The {@link Location} to check.
     * @return {@code true} if the {@link Location} contains a
     *         {@link PublicLiftSign}, {@code false} otherwise.
     */
    public static boolean isPublicLiftSign(@NotNull final Location location) {
        return LiftSign.checkLiftSign(location, true);
    }
    
    /**
     * Checks to see if the given {@link Block} is a {@link PublicLiftSign}.
     *
     * @param block The {@link Block} to check.
     * @return {@code true} if the {@link Block} is a {@link PublicLiftSign},
     *         {@code false} otherwise.
     */
    public static boolean isPublicLiftSign(@NotNull final Block block) {
        return LiftSign.checkLiftSign(block, true);
    }
    
    /**
     * Checks to see if the given {@link BlockState} represents a
     * {@link PublicLiftSign}.
     *
     * @param state The {@link BlockState} to check.
     * @return {@code true} if the {@link BlockState} represents a
     *         {@link PublicLiftSign}, {@code false} otherwise.
     */
    public static boolean isPublicLiftSign(@NotNull final BlockState state) {
        return LiftSign.checkLiftSign(state, true);
    }
    
    /**
     * Checks to see if the given {@link Location} contains a
     * {@link PrivateLiftSign}.
     *
     * @param location The {@link Location} to check.
     * @return {@code true} if the {@link Location} contains a
     *         {@link PrivateLiftSign}, {@code false} otherwise.
     */
    public static boolean isPrivateLiftSign(@NotNull final Location location) {
        return LiftSign.checkLiftSign(location, false);
    }
    
    /**
     * Checks to see if the given {@link Block} is a {@link PrivateLiftSign}.
     *
     * @param block The {@link Block} to check.
     * @return {@code true} if the {@link Block} is a {@link PrivateLiftSign},
     *         {@code false} otherwise.
     */
    public static boolean isPrivateLiftSign(@NotNull final Block block) {
        return LiftSign.checkLiftSign(block, false);
    }
    
    /**
     * Checks to see if the given {@link BlockState} represents a
     * {@link PrivateLiftSign}.
     *
     * @param state The {@link BlockState} to check.
     * @return {@code true} if the {@link BlockState} represents a
     *         {@link PrivateLiftSign}, {@code false} otherwise.
     */
    public static boolean isPrivateLiftSign(@NotNull final BlockState state) {
        return LiftSign.checkLiftSign(state, false);
    }
    
    /**
     * Checks to see if the given {@link Location} contains a {@link LiftSign},
     * and whether that {@link LiftSign} is a {@link PublicLiftSign} or a
     * {@link PrivateLiftSign}.
     * <p>
     * If {@code checkPublic} is {@code true} and the {@link Location} contains
     * anything other than a {@link PublicLiftSign}, this will return
     * {@code false}.
     * <p>
     * On the other hand, if {@code checkPublic} is {@code false}, and the
     * {@link Location} contains anything other than a {@link PrivateLiftSign},
     * this will return {@code false}.
     * 
     * @param location The {@link Location} to check.
     * @param checkPublic If {@code true}, the {@link LiftSign} needs to be a
     *                    {@link PublicLiftSign}, {@code false} if it needs to
     *                    be a {@link PrivateLiftSign}.
     * @return {@code true} if the {@link Location} contains a {@link LiftSign}
     *         that matches the requirements, {@code false} otherwise.
     */
    private static boolean checkLiftSign(@NotNull final Location location, final boolean checkPublic) {
        return LiftSign.checkLiftSign(location.getBlock(), checkPublic);
    }
    
    /**
     * Checks to see if the given {@link Block} contains a {@link LiftSign}, and
     * whether that {@link LiftSign} is a {@link PublicLiftSign} or a
     * {@link PrivateLiftSign}.
     * <p>
     * If {@code checkPublic} is {@code true} and the {@link Location} contains
     * anything other than a {@link PublicLiftSign}, this will return
     * {@code false}.
     * <p>
     * On the other hand, if {@code checkPublic} is {@code false}, and the
     * {@link Location} contains anything other than a {@link PrivateLiftSign},
     * this will return {@code false}.
     * 
     * @param block The {@link Block} to check.
     * @param checkPublic If {@code true}, the {@link LiftSign} needs to be a
     *                    {@link PublicLiftSign}, {@code false} if it needs to
     *                    be a {@link PrivateLiftSign}.
     * @return {@code true} if the {@link Block} is a {@link LiftSign} that
     *         matches the requirements, {@code false} otherwise.
     */
    private static boolean checkLiftSign(@NotNull final Block block, final boolean checkPublic) {
        return LiftSign.checkLiftSign(block.getState(), checkPublic);
    }
    
    /**
     * Checks to see if the given {@link BlockState} represents a
     * {@link LiftSign}, and whether that {@link LiftSign} is a
     * {@link PublicLiftSign} or a {@link PrivateLiftSign}.
     * <p>
     * If {@code checkPublic} is {@code true} and the {@link Location} contains
     * anything other than a {@link PublicLiftSign}, this will return
     * {@code false}.
     * <p>
     * On the other hand, if {@code checkPublic} is {@code false}, and the
     * {@link Location} contains anything other than a {@link PrivateLiftSign},
     * this will return {@code false}.
     * 
     * @param state The {@link BlockState} to check.
     * @param checkPublic If {@code true}, the {@link LiftSign} needs to be a
     *                    {@link PublicLiftSign}, {@code false} if it needs to
     *                    be a {@link PrivateLiftSign}.
     * @return {@code true} if the {@link BlockState} represents a
     *         {@link LiftSign} that matches the requirements, {@code false}
     *         otherwise.
     */
    private static boolean checkLiftSign(@NotNull final BlockState state, final boolean checkPublic) {
        if(!(state instanceof Sign)) {
            return false;
        }
    
        final String liftLine = ((Sign) state).getLine(1);
        try {
            LiftSign.validateLiftLine(liftLine);
        } catch (SignLiftException e) {
            return false;
        }
    
        if (liftLine.startsWith(ConfigData.getPublicStart()) && liftLine.endsWith(ConfigData.getPublicEnd()) && checkPublic) {
            return true;
        }
        
        return liftLine.startsWith(ConfigData.getPrivateStart()) && liftLine.endsWith(ConfigData.getPrivateEnd()) && !checkPublic;
    }
    
    /**
     * Validates that the lift line passed in is valid for either a
     * {@link PublicLiftSign} or a {@link PrivateLiftSign}.
     * 
     * @param liftLine The line to validate.
     * @throws SignLiftException If the line is not valid for a
     *                           {@link LiftSign}.
     */
    private static void validateLiftLine(@NotNull final String liftLine) throws SignLiftException {
        
        if (liftLine.length() < 3) {
            throw new SignLiftException("Lift line is less than 3 characters long: " + liftLine);
        }
        
        if (liftLine.startsWith(ConfigData.getPublicStart()) && liftLine.endsWith(ConfigData.getPublicEnd())) {
            if (LiftSign.getLiftDirection(liftLine.substring(1, liftLine.length() - 1)) == null) {
                throw new SignLiftException("Invalid lift direction for public lift sign: " + liftLine);
            }
        } else if (liftLine.startsWith(ConfigData.getPrivateStart()) && liftLine.endsWith(ConfigData.getPrivateEnd())) {
            if (LiftSign.getLiftDirection(liftLine.substring(1, liftLine.length() - 1)) == null) {
                throw new SignLiftException("Invalid lift direction for private lift sign: " + liftLine);
            }
        }
        else {
            throw new SignLiftException("Invalid lift brackets: " + liftLine);
        }
    }
    
    /**
     * Gets the {@link LiftSign.Direction} based on the value of the given
     * {@link String}.
     * 
     * @param liftDirection The value representing the
     *                      {@link LiftSign.Direction}.
     * @return The {@link LiftSign.Direction} represented by the given
     *         {@link String}, or {@code null} if the value is not valid.
     */
    @Nullable
    private static Direction getLiftDirection(@NotNull final String liftDirection) {
        if (liftDirection.equalsIgnoreCase(ConfigData.getDirectionNone())) {
            return Direction.NONE;
        } else if (liftDirection.equalsIgnoreCase(ConfigData.getDirectionUp())) {
            return Direction.UP;
        } else if (liftDirection.equalsIgnoreCase(ConfigData.getDirectionDown())) {
            return Direction.DOWN;
        } else {
            return null;
        }
    }
    
    /**
     * Checks to see if the given {@link Block} is a safe one to teleport
     * into (i.e., is transparent or any other sort like that).
     * 
     * @param block The {@link Block} to check.
     * @return {@code true} if it is safe to teleport into, {@code false}
     *         otherwise.
     */
    protected static boolean isBlockSafe(final Block block) {
        
        switch (block.getType()) {
            case AIR:
            case OAK_SAPLING:
            case SPRUCE_SAPLING:
            case BIRCH_SAPLING:
            case JUNGLE_SAPLING:
            case ACACIA_SAPLING:
            case DARK_OAK_SAPLING:
            case POWERED_RAIL:
            case DETECTOR_RAIL:
            case COBWEB:
            case GRASS:
            case FERN:
            case DEAD_BUSH:
            case SEAGRASS:
            case SEA_PICKLE:
            case DANDELION:
            case POPPY:
            case BLUE_ORCHID:
            case ALLIUM:
            case AZURE_BLUET:
            case RED_TULIP:
            case ORANGE_TULIP:
            case WHITE_TULIP:
            case PINK_TULIP:
            case OXEYE_DAISY:
            case CORNFLOWER:
            case LILY_OF_THE_VALLEY:
            case BROWN_MUSHROOM:
            case RED_MUSHROOM:
            case CRIMSON_FUNGUS:
            case WARPED_FUNGUS:
            case CRIMSON_ROOTS:
            case WARPED_ROOTS:
            case NETHER_SPROUTS:
            case WEEPING_VINES:
            case TWISTING_VINES:
            case SUGAR_CANE:
            case KELP:
            case RAIL:
            case LEVER:
            case STONE_PRESSURE_PLATE:
            case OAK_PRESSURE_PLATE:
            case SPRUCE_PRESSURE_PLATE:
            case BIRCH_PRESSURE_PLATE:
            case JUNGLE_PRESSURE_PLATE:
            case ACACIA_PRESSURE_PLATE:
            case DARK_OAK_PRESSURE_PLATE:
            case CRIMSON_PRESSURE_PLATE:
            case WARPED_PRESSURE_PLATE:
            case POLISHED_BLACKSTONE_PRESSURE_PLATE:
            case REDSTONE_TORCH:
            case SOUL_TORCH:
            case VINE:
            case TRIPWIRE_HOOK:
            case STONE_BUTTON:
            case OAK_BUTTON:
            case SPRUCE_BUTTON:
            case BIRCH_BUTTON:
            case JUNGLE_BUTTON:
            case ACACIA_BUTTON:
            case DARK_OAK_BUTTON:
            case CRIMSON_BUTTON:
            case WARPED_BUTTON:
            case POLISHED_BLACKSTONE_BUTTON:
            case LIGHT_WEIGHTED_PRESSURE_PLATE:
            case HEAVY_WEIGHTED_PRESSURE_PLATE:
            case ACTIVATOR_RAIL:
            case SUNFLOWER:
            case LILAC:
            case ROSE_BUSH:
            case PEONY:
            case TALL_GRASS:
            case LARGE_FERN:
            case WHEAT:
            case OAK_SIGN:
            case SPRUCE_SIGN:
            case BIRCH_SIGN:
            case JUNGLE_SIGN:
            case ACACIA_SIGN:
            case DARK_OAK_SIGN:
            case CRIMSON_SIGN:
            case WARPED_SIGN:
            case NETHER_WART:
            case ITEM_FRAME:
            case WHITE_BANNER:
            case ORANGE_BANNER:
            case MAGENTA_BANNER:
            case LIGHT_BLUE_BANNER:
            case YELLOW_BANNER:
            case LIME_BANNER:
            case PINK_BANNER:
            case GRAY_BANNER:
            case LIGHT_GRAY_BANNER:
            case CYAN_BANNER:
            case PURPLE_BANNER:
            case BLUE_BANNER:
            case BROWN_BANNER:
            case GREEN_BANNER:
            case RED_BANNER:
            case BLACK_BANNER:
            case WATER:
            case TALL_SEAGRASS:
            case WALL_TORCH:
            case REDSTONE_WIRE:
            case OAK_WALL_SIGN:
            case SPRUCE_WALL_SIGN:
            case BIRCH_WALL_SIGN:
            case JUNGLE_WALL_SIGN:
            case ACACIA_WALL_SIGN:
            case DARK_OAK_WALL_SIGN:
            case CRIMSON_WALL_SIGN:
            case WARPED_WALL_SIGN:
            case SOUL_WALL_TORCH:
            case NETHER_PORTAL:
            case ATTACHED_PUMPKIN_STEM:
            case ATTACHED_MELON_STEM:
            case PUMPKIN_STEM:
            case MELON_STEM:
            case TRIPWIRE:
            case CARROTS:
            case POTATOES:
            case WHITE_WALL_BANNER:
            case ORANGE_WALL_BANNER:
            case MAGENTA_WALL_BANNER:
            case LIGHT_BLUE_WALL_BANNER:
            case YELLOW_WALL_BANNER:
            case LIME_WALL_BANNER:
            case PINK_WALL_BANNER:
            case GRAY_WALL_BANNER:
            case LIGHT_GRAY_WALL_BANNER:
            case CYAN_WALL_BANNER:
            case PURPLE_WALL_BANNER:
            case BLUE_WALL_BANNER:
            case BROWN_WALL_BANNER:
            case GREEN_WALL_BANNER:
            case RED_WALL_BANNER:
            case BLACK_WALL_BANNER:
            case BEETROOTS:
            case KELP_PLANT:
            case BUBBLE_COLUMN:
                return true;
            default:
                return false;
        }
    }
}
