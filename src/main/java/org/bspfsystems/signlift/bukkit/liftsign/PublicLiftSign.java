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
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.jetbrains.annotations.NotNull;
import org.bspfsystems.signlift.bukkit.SignLiftPlugin;
import org.bspfsystems.signlift.bukkit.exception.SignLiftException;

/**
 * Represents the public (non-access-controlled) implementation of a
 * {@link LiftSign}. This is not serialized or stored in any way as there is no
 * access control associated with it.
 */
public final class PublicLiftSign extends LiftSign {
    
    /**
     * Creates a new {@link PublicLiftSign} from the given {@link Block}.
     * 
     * @param block The {@link Block} to create the {@link PublicLiftSign} from.
     * @throws SignLiftException If the {@link PublicLiftSign} cannot be
     *                           created.
     * @see LiftSign#LiftSign(Block)
     */
    public PublicLiftSign(@NotNull final Block block) throws SignLiftException {
        super(block);
    }
    
    /**
     * Creates a new {@link PublicLiftSign} from the given {@link BlockState}.
     * 
     * @param state The {@link BlockState} to create the {@link PublicLiftSign}
     *              from.
     * @throws SignLiftException If the {@link PublicLiftSign} cannot be
     *                           created.
     * @see LiftSign#LiftSign(BlockState)
     */
    public PublicLiftSign(@NotNull final BlockState state) throws SignLiftException {
        super(state);
    }
    
    /**
     * Creates a new {@link PublicLiftSign} from the given {@link Location} and
     * {@link String} array of lines.
     * <p>
     * The given {@link Location} must contain a {@link Sign}, and the lines
     * must have valid lift lines on them. This is most commonly used when the
     * lines of a {@link Sign} have been changed.
     * 
     * @param location The {@link Location} that is to contain the
     *                 {@link PublicLiftSign}.
     * @param lines The lines that will be applied to the
     *              {@link PublicLiftSign}.
     * @throws SignLiftException If the {@link PublicLiftSign} cannot be
     *                           created.
     * @see LiftSign#LiftSign(Location, String[])
     * @see SignChangeEvent
     */
    public PublicLiftSign(@NotNull final Location location, @NotNull final String[] lines) throws SignLiftException {
        super(location, lines);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPrivate() {
        return false;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean activate(@NotNull final Player player, @NotNull final SignLiftPlugin plugin) {
        return super.activate(player, plugin);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canRemove(@NotNull final Player player) {
        return player.hasPermission("signlift.remove.public");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canUse(@NotNull final Player player) {
        return player.hasPermission("signlift.use.public");
    }
}
