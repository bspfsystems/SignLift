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

package org.unixminecraft.signlift.bukkit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;

import org.unixminecraft.signlift.bukkit.exception.SignLiftException;

final class ChangeData {
	
	private final boolean changeOwner;
	private final UUID newOwner;
	private final HashSet<UUID> admins;
	private final HashSet<UUID> members;
	private final HashSet<UUID> removals;
	private final ArrayList<String> unknowns;
	
	ChangeData(final boolean changeOwner, final UUID newOwner) {
		
		if(!changeOwner) {
			this.changeOwner = false;
			this.newOwner = null;
		}
		else if(newOwner == null) {
			this.changeOwner = false;
			this.newOwner = null;
		}
		else {
			this.changeOwner = true;
			this.newOwner = newOwner;
		}
		
		admins = new HashSet<UUID>();
		members = new HashSet<UUID>();
		removals = new HashSet<UUID>();
		unknowns = new ArrayList<String>();
	}
	
	boolean addAdmin(final UUID admin) throws SignLiftException {
		
		if(members.contains(admin)) {
			throw new SignLiftException("You cannot add someone as an admin and a member at the same time.");
		}
		if(removals.contains(admin)) {
			throw new SignLiftException("You cannot add someone as an admin and remove them at the same time.");
		}
		
		return admins.add(admin);
	}
	
	boolean addMember(final UUID member) throws SignLiftException {
		
		if(admins.contains(member)) {
			throw new SignLiftException("You cannot add someone as a member and an admin at the same time.");
		}
		if(removals.contains(member)) {
			throw new SignLiftException("You cannot add someone as a member and remove them at the same time.");
		}
		
		return members.add(member);
	}
	
	boolean remove(final UUID removal) throws SignLiftException {
		
		if(admins.contains(removal)) {
			throw new SignLiftException("You cannot remove someone and add them as an admin at the same time.");
		}
		if(members.contains(removal)) {
			throw new SignLiftException("You cannot remove someone and add them as a member at the same time.");
		}
		
		return removals.add(removal);
	}
	
	void addUnknown(final String unknown) {
		unknowns.add(unknown);
	}
	
	boolean isOwnerChanging() {
		return changeOwner;
	}
	
	UUID getNewOwner() {
		return newOwner;
	}
	
	HashSet<UUID> getAdmins() {
		return admins;
	}
	
	HashSet<UUID> getMembers() {
		return members;
	}
	
	HashSet<UUID> getRemovals() {
		return removals;
	}
	
	ArrayList<String> getUnknowns() {
		return unknowns;
	}
}
