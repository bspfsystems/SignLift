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

package org.unixminecraft.bukkit.signlift.config;

import org.bukkit.configuration.file.FileConfiguration;

public final class ConfigData {
	
	private static boolean checkDestination;
	private static String liftsignDirectionNone;
	private static String liftsignDirectionUp;
	private static String liftsignDirectionDown;
	private static String liftsignPublicOpen;
	private static String liftsignPublicClose;
	private static String liftsignPrivateOpen;
	private static String liftsignPrivateClose;
	
	private ConfigData() {
		
	}
	
	public static void loadConfig(final FileConfiguration config) {
		
		checkDestination = config.getBoolean("check_destination", true);
		liftsignDirectionNone = config.getString("liftsign.direction.none", "LIFT");
		liftsignDirectionUp = config.getString("liftsign.direction.up", "LIFT UP");
		liftsignDirectionDown = config.getString("liftsign.direction.down", "LIFT DOWN");
		liftsignPublicOpen = config.getString("liftsign.public.open", "[");
		liftsignPublicClose = config.getString("liftsign.public.close", "]");
		liftsignPrivateOpen = config.getString("liftsign.private.open", "{");
		liftsignPrivateClose = config.getString("liftsign.private.close", "}");
	}
	
	public static boolean getCheckDestination() {
		return checkDestination;
	}
	
	public static String getLiftsignDirectionNone() {
		return liftsignDirectionNone;
	}
	
	public static String getLiftsignDirectionUp() {
		return liftsignDirectionUp;
	}
	
	public static String getLiftsignDirectionDown() {
		return liftsignDirectionDown;
	}
	
	public static String getLiftsignPublicOpen() {
		return liftsignPublicOpen;
	}
	
	public static String getLiftsignPublicClose() {
		return liftsignPublicClose;
	}
	
	public static String getLiftsignPrivateOpen() {
		return liftsignPrivateOpen;
	}
	
	public static String getLiftsignPrivateClose() {
		return liftsignPrivateClose;
	}
}
