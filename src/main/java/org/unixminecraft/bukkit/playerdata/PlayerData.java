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

package org.unixminecraft.bukkit.playerdata;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;

@SerializableAs("player_data")
public final class PlayerData implements ConfigurationSerializable {
	
	private final UUID uniqueId;
	private String name;
	
	public PlayerData(final Player player) {
		
		uniqueId = player.getUniqueId();
		name = player.getName();
	}
	
	private PlayerData(final UUID uniqueId, final String name) {
		
		this.uniqueId = uniqueId;
		this.name = name;
	}
	
	public static PlayerData deserialize(final Map<String, Object> config) {
		
		if(!config.containsKey("unique_id")) {
			throw new IllegalArgumentException("PlayerData config missing UUID.");
		}
		if(config.get("unique_id") == null) {
			throw new IllegalArgumentException("PlayerData null UUID config value.");
		}
		if(!(config.get("unique_id") instanceof String)) {
			throw new IllegalArgumentException("PlayerData invalid UUID config value type.");
		}
		
		final UUID uniqueId;
		try {
			uniqueId = UUID.fromString((String) config.get("unique_id"));
		}
		catch(IllegalArgumentException e) {
			throw new IllegalArgumentException("PlayerData invalid UUID config value.", e);
		}
		
		if(!config.containsKey("name")) {
			throw new IllegalArgumentException("PlayerData config missing name.");
		}
		if(config.get("name") == null) {
			throw new IllegalArgumentException("PlayerData null name config value.");
		}
		if(!(config.get("name") instanceof String)) {
			throw new IllegalArgumentException("PlayerData invalid name config value type.");
		}
		
		return new PlayerData(uniqueId, (String) config.get("name"));
	}
	
	@Override
	public Map<String, Object> serialize() {
		
		final HashMap<String, Object> config = new HashMap<String, Object>();
		
		config.put("unique_id", uniqueId.toString());
		config.put("name", name);
		
		return config;
	}
	
	public UUID getUniqueId() {
		return uniqueId;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(final String name) {
		this.name = name;
	}
}
