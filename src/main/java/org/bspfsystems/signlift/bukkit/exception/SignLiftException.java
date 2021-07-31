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

package org.bspfsystems.signlift.bukkit.exception;

import org.bspfsystems.signlift.bukkit.SignLiftPlugin;

/**
 * Represents an {@link Exception} used by the {@link SignLiftPlugin}.
 */
public final class SignLiftException extends Exception {
    
    private static final long serialVersionUID = -4604200347378025447L;
    
    /**
     * Constructs a basic {@link SignLiftException}.
     * 
     * @see Exception#Exception()
     */
    public SignLiftException() {
        super();
    }
    
    /**
     * Constructs a {@link SignLiftException} with a message.
     * 
     * @param message The (error) message associated with the
     *                {@link SignLiftException}.
     * @see Exception#Exception(String)
     */
    public SignLiftException(final String message) {
        super(message);
    }
    
    /**
     * Constructs a {@link SignLiftException} with a message and a
     * {@link Throwable} as the cause.
     * 
     * @param message The (error) message associated with the
     *                {@link SignLiftException}.
     * @param cause The upstream cause of the {@link SignLiftException}.
     * @see Exception#Exception(String, Throwable)
     */
    public SignLiftException(final String message, final Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Constructs a {@link SignLiftException} with the {@link Throwable} as the
     * cause.
     * 
     * @param cause The upstream cause of the {@link SignLiftException}.
     * @see Exception#Exception(Throwable)
     */
    public SignLiftException(final Throwable cause) {
        super(cause);
    }
}
