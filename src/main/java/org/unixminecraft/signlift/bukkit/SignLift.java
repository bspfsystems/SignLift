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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.unixminecraft.playerdata.bukkit.PlayerData;
import org.unixminecraft.signlift.bukkit.config.ConfigData;
import org.unixminecraft.signlift.bukkit.config.ConfigMessage;
import org.unixminecraft.signlift.bukkit.exception.SignLiftException;
import org.unixminecraft.signlift.bukkit.liftsign.LiftSign;
import org.unixminecraft.signlift.bukkit.liftsign.PrivateLiftSign;
import org.unixminecraft.signlift.bukkit.listener.SignLiftEventHandler;

/**
 * SignLift for Bukkit
 *
 * @author freman
 * @author GoalieGuy6
 * @author mciolkosz
 */
public final class SignLift extends JavaPlugin {
	
	private Logger logger;
	
	private File dataFolder;
	private File playerDataFolder;
	private File privateLiftSignFolder;
	
	private ConcurrentHashMap<String, UUID> nameToUniqueId;
	private ConcurrentHashMap<UUID, String> uniqueIdToName;
	
	private HashSet<UUID> pendingInformation;
	private ConcurrentHashMap<UUID, ChangeData> pendingModifications;
	
	public SignLift() {
		super();
	}
	
	@Override
	public void onEnable() {
		
		logger = getLogger();
		
		logger.log(Level.INFO, "///////////////////////////////////////////////////////////////////////////");
		logger.log(Level.INFO, "//                                                                       //");
		logger.log(Level.INFO, "// Copyright (C) 2011       Shannon Wynter (http://fremnet.net/)         //");
		logger.log(Level.INFO, "// Copyright (C) 2012       GoalieGuy6 (https://github.com/goalieguy6)   //");
		logger.log(Level.INFO, "// Copyright (C) 2018,2020  Matt Ciolkosz (https://github.com/mciolkosz) //");
		logger.log(Level.INFO, "//                                                                       //");
		logger.log(Level.INFO, "// This program is free software: you can redistribute it and/or modify  //");
		logger.log(Level.INFO, "// it under the terms of the GNU General Public License as published by  //");
		logger.log(Level.INFO, "// the Free Software Foundation, either version 3 of the License, or     //");
		logger.log(Level.INFO, "// (at your option) any later version.                                   //");
		logger.log(Level.INFO, "//                                                                       //");
		logger.log(Level.INFO, "// This program is distributed in the hope that it will be useful,       //");
		logger.log(Level.INFO, "// but WITHOUT ANY WARRANTY; without even the implied warranty of        //");
		logger.log(Level.INFO, "// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         //");
		logger.log(Level.INFO, "// GNU General Public License for more details.                          //");
		logger.log(Level.INFO, "//                                                                       //");
		logger.log(Level.INFO, "// You should have received a copy of the GNU General Public License     //");
		logger.log(Level.INFO, "// along with this program.  If not, see <http://www.gnu.org/licenses/>. //");
		logger.log(Level.INFO, "//                                                                       //");
		logger.log(Level.INFO, "///////////////////////////////////////////////////////////////////////////");
		
		ConfigData.loadConfig(getConfig());
		ConfigMessage.loadMessages(getMessages());
		
		final PluginManager pluginManager = getServer().getPluginManager();
		pluginManager.registerEvents(new SignLiftEventHandler(this), this);
		
		ConfigurationSerialization.registerClass(PlayerData.class);
		ConfigurationSerialization.registerClass(PrivateLiftSign.class);
		
		dataFolder = getDataFolder();
		
		playerDataFolder = new File(dataFolder, "PlayerData");
		final String playerDataFolderPath = playerDataFolder.getPath();
		try {
			if(!playerDataFolder.exists()) {
				try {
					playerDataFolder.mkdirs();
				}
				catch(SecurityException e) {
					logger.log(Level.SEVERE, "Unable to create PlayerData folder at " + playerDataFolderPath);
					logger.log(Level.SEVERE, "SecurityException thrown.", e);
					throw new RuntimeException("Unable to create PlayerData folder at " + playerDataFolderPath, e);
				}
			}
		}
		catch(SecurityException e) {
			logger.log(Level.SEVERE, "Unable to validate PlayerData folder existence at " + playerDataFolderPath);
			logger.log(Level.SEVERE, "SecurityException thrown.", e);
			throw new RuntimeException("Unable to validate PlayerData folder existence at " + playerDataFolderPath, e);
		}
		
		nameToUniqueId = new ConcurrentHashMap<String, UUID>();
		uniqueIdToName = new ConcurrentHashMap<UUID, String>();
		
		for(final File playerDataConfigFile : playerDataFolder.listFiles()) {
			
			final String playerDataConfigFilePath = playerDataConfigFile.getPath();
			final YamlConfiguration playerDataConfig = new YamlConfiguration();
			
			try {
				playerDataConfig.load(playerDataConfigFile);
			}
			catch(FileNotFoundException e) {
				logger.log(Level.WARNING, "Unable to load player configuration file at " + playerDataConfigFilePath);
				logger.log(Level.WARNING, "Skipping player.");
				logger.log(Level.WARNING, "FileNotFoundException thrown.", e);
				continue;
			}
			catch(IOException e) {
				logger.log(Level.WARNING, "Unable to load player configuration file at " + playerDataConfigFilePath);
				logger.log(Level.WARNING, "Skipping player.");
				logger.log(Level.WARNING, "IOException thrown.", e);
				continue;
			}
			catch(InvalidConfigurationException e) {
				logger.log(Level.WARNING, "Unable to load player configuration file at " + playerDataConfigFilePath);
				logger.log(Level.WARNING, "Skipping player.");
				logger.log(Level.WARNING, "InvalidConfigurationException thrown.", e);
				continue;
			}
			catch(IllegalArgumentException e) {
				logger.log(Level.WARNING, "Unable to load player configuration file at " + playerDataConfigFilePath);
				logger.log(Level.WARNING, "Skipping player.");
				logger.log(Level.WARNING, "IllegalArgumentException thrown.", e);
				continue;
			}
			
			final PlayerData playerData = playerDataConfig.getSerializable("player_data", PlayerData.class);
			nameToUniqueId.put(playerData.getName().toLowerCase(), playerData.getUniqueId());
			uniqueIdToName.put(playerData.getUniqueId(), playerData.getName());
		}
		
		privateLiftSignFolder = new File(dataFolder, "PrivateLiftSigns");
		final String privateLiftSignFolderPath = privateLiftSignFolder.getPath();
		try {
			if(!privateLiftSignFolder.exists()) {
				try {
					privateLiftSignFolder.mkdirs();
				}
				catch(SecurityException e) {
					logger.log(Level.SEVERE, "Unable to create private lift sign folder at " + privateLiftSignFolderPath);
					logger.log(Level.SEVERE, "SecurityException thrown.", e);
					throw new RuntimeException("Unable to create private lift sign folder at " + privateLiftSignFolderPath, e);
				}
			}
		}
		catch(SecurityException e) {
			logger.log(Level.SEVERE, "Unable to validate private lift sign folder existence at " + privateLiftSignFolderPath);
			logger.log(Level.SEVERE, "SecurityException thrown.", e);
			throw new RuntimeException("Unable to validate private lift sign folder existence at " + privateLiftSignFolderPath, e);
		}
		
		pendingInformation = new HashSet<UUID>();
		pendingModifications = new ConcurrentHashMap<UUID, ChangeData>();
	}
	
	@Override
	public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
		
		final String commandName = command.getName();
		if(!getDescription().getCommands().keySet().contains(commandName)) {
			
			logger.log(Level.WARNING, "The command " + commandName + " was triggered in the SignLift plugin.");
			logger.log(Level.WARNING, "This command is not registered to SignLift.");
			return true;
		}
		
		if(!(sender instanceof Player)) {
			sender.sendMessage(ConfigMessage.getCommandDenyPlayer());
			return true;
		}
		
		final Player player = (Player) sender;
		final Server server = getServer();
		
		if(commandName.equals("signlift")) {
			
			if(args.length == 0) {
				return false;
			}
			
			final String subcommand = args[0];
			if(subcommand.equals("help")) {
				
				final PluginCommand shelpCommand = server.getPluginCommand("shelp");
				if(!player.hasPermission(shelpCommand.getPermission())) {
					player.sendMessage(shelpCommand.getPermissionMessage());
					return true;
				}
				if(args.length > 1) {
					player.sendMessage(shelpCommand.getUsage());
					return true;
				}
				
				return helpCommand(player);
			}
			else if(subcommand.equals("info")) {
				
				final PluginCommand sinfoCommand = server.getPluginCommand("sinfo");
				if(!player.hasPermission(sinfoCommand.getPermission())) {
					player.sendMessage(sinfoCommand.getPermissionMessage());
					return true;
				}
				if(args.length > 1) {
					player.sendMessage(sinfoCommand.getUsage());
					return true;
				}
				
				return infoCommand(player);
			}
			else if(subcommand.equals("modify")) {
				
				final PluginCommand smodifyCommand = server.getPluginCommand("smodify");
				if(!player.hasPermission(smodifyCommand.getPermission())) {
					player.sendMessage(smodifyCommand.getPermissionMessage());
					return true;
				}
				if(args.length < 2) {
					player.sendMessage(smodifyCommand.getUsage());
					return true;
				}
				
				return modifyCommand(player, args, true);
			}
			else if(subcommand.equals("changeowner")) {
				
				final PluginCommand schangeownerCommand = server.getPluginCommand("schangeowner");
				if(!player.hasPermission(schangeownerCommand.getPermission())) {
					player.sendMessage(schangeownerCommand.getPermissionMessage());
					return true;
				}
				
				if(args.length != 2) {
					player.sendMessage(schangeownerCommand.getUsage());
					return true;
				}
				
				return changeOwnerCommand(player, args[1]);
			}
			else {
				return false;
			}
		}
		else if(commandName.equals("shelp")) {
			
			final PluginCommand signliftCommand = server.getPluginCommand("signlift");
			if(!player.hasPermission(signliftCommand.getPermission())) {
				player.sendMessage(signliftCommand.getPermissionMessage());
				return true;
			}
			if(args.length > 0) {
				return false;
			}
			
			return helpCommand(player);
		}
		else if(commandName.equals("sinfo")) {
			
			final PluginCommand signliftCommand = server.getPluginCommand("signlift");
			if(!player.hasPermission(signliftCommand.getPermission())) {
				player.sendMessage(signliftCommand.getPermissionMessage());
				return true;
			}
			if(args.length > 0) {
				return false;
			}
			
			return infoCommand(player);
		}
		else if(commandName.equals("smodify")) {
			
			final PluginCommand signliftCommand = server.getPluginCommand("signlift");
			if(!player.hasPermission(signliftCommand.getPermission())) {
				player.sendMessage(signliftCommand.getPermissionMessage());
				return true;
			}
			if(args.length < 1) {
				return false;
			}
			
			return modifyCommand(player, args, false);
		}
		else if(commandName.equals("schangeowner")) {
			
			final PluginCommand signliftCommand = server.getPluginCommand("signlift");
			if(!player.hasPermission(signliftCommand.getPermission())) {
				player.sendMessage(signliftCommand.getPermissionMessage());
				return true;
			}
			if(args.length != 1) {
				return false;
			}
			
			return changeOwnerCommand(player, args[0]);
		}
		else {
			
			logger.log(Level.WARNING, "The command " + commandName + " was triggered in the SignLift plugin.");
			logger.log(Level.WARNING, "This command is not registered to SignLift.");
			logger.log(Level.WARNING, "This command passed the earlier inspection undetected.");
			return true;
		}
	}
	
	public Collection<String> onPlayerCommandSend(final Player player) {
		
		final HashSet<String> removals = new HashSet<String>();
		for(final String commandName : getDescription().getCommands().keySet()) {
			
			removals.add("signlift:" + commandName);
			if(!player.hasPermission(getServer().getPluginCommand(commandName).getPermission())) {
				removals.add(commandName);
			}
		}
		
		return removals;
	}
	
	public void onPlayerJoin(final Player player) {
		
		final UUID uniqueId = player.getUniqueId();
		final String currentName = player.getName();
		
		if(!uniqueIdToName.containsKey(uniqueId)) {
			
			logger.log(Level.INFO, "================================================");
			logger.log(Level.INFO, "Player Login : NEW PLAYER");
			logger.log(Level.INFO, "------------------------------------------------");
			logger.log(Level.INFO, "Name : " + currentName);
			logger.log(Level.INFO, "UUID : " + uniqueId.toString());
			logger.log(Level.INFO, "================================================");
			
			nameToUniqueId.put(currentName.toLowerCase(), uniqueId);
			uniqueIdToName.put(uniqueId, currentName);
			
			if(!savePlayerData(new PlayerData(player))) {
				logger.log(Level.WARNING, "Unable to save PlayerData.");
				logger.log(Level.WARNING, "Name : " + currentName);
				logger.log(Level.WARNING, "UUID : " + uniqueId.toString());
			}
		}
		else if(!nameToUniqueId.containsKey(currentName.toLowerCase())) {
			
			logger.log(Level.INFO, "================================================");
			logger.log(Level.INFO, "Player Login: UPDATE NAME");
			logger.log(Level.INFO, "------------------------------------------------");
			logger.log(Level.INFO, "Old Name : " + uniqueIdToName.get(uniqueId));
			logger.log(Level.INFO, "New Name : " + currentName);
			logger.log(Level.INFO, "UUID     : " + uniqueId.toString());
			logger.log(Level.INFO, "================================================");
			
			nameToUniqueId.remove(uniqueIdToName.get(uniqueId));
			uniqueIdToName.remove(uniqueId);
			
			nameToUniqueId.put(currentName.toLowerCase(), uniqueId);
			uniqueIdToName.put(uniqueId, currentName);
			
			if(!savePlayerData(new PlayerData(player))) {
				logger.log(Level.WARNING, "Unable to save PlayerData.");
				logger.log(Level.WARNING, "Name : " + currentName);
				logger.log(Level.WARNING, "UUID : " + uniqueId.toString());
			}
		}
		else {
			
			logger.log(Level.INFO, "================================================");
			logger.log(Level.INFO, "Player Login: KNOWN PLAYER");
			logger.log(Level.INFO, "------------------------------------------------");
			logger.log(Level.INFO, "Name : " + currentName);
			logger.log(Level.INFO, "UUID : " + uniqueId.toString());
			logger.log(Level.INFO, "================================================");
		}
	}
	
	public List<String> onTabComplete(final String buffer, final CommandSender sender) {
		
		final Server server = getServer();
		final ArrayList<String> splitCommand = new ArrayList<String>();
		final String[] splitBuffer = buffer.split(" ");
		final boolean endsWithSpace = buffer.endsWith(" ");
		
		for(final String splitBufferItem : splitBuffer) {
			if(splitBufferItem.startsWith("/")) {
				splitCommand.add(splitBufferItem.substring(1));
			}
			else if(splitBufferItem.isEmpty()) {
				continue;
			}
			else {
				splitCommand.add(splitBufferItem);
			}
		}
		
		if(splitCommand.isEmpty()) {
			return null;
		}
		
		final String commandName = splitCommand.remove(0);
		if(commandName.equals("signlift")) {
			
			final ArrayList<String> subCommandNames = new ArrayList<String>();
			if(sender.hasPermission(server.getPluginCommand("shelp").getPermission())) {
				subCommandNames.add("help");
			}
			if(sender.hasPermission(server.getPluginCommand("sinfo").getPermission())) {
				subCommandNames.add("info");
			}
			if(sender.hasPermission(server.getPluginCommand("smodify").getPermission())) {
				subCommandNames.add("modify");
			}
			if(sender.hasPermission(server.getPluginCommand("schangeowner").getPermission())) {
				subCommandNames.add("changeowner");
			}
			
			if(splitCommand.isEmpty()) {
				return subCommandNames;
			}
			
			final String subCommandName = splitCommand.remove(0);
			if(!endsWithSpace) {
				
				final Iterator<String> subCommandNamesIterator = subCommandNames.iterator();
				while(subCommandNamesIterator.hasNext()) {
					if(!subCommandNamesIterator.next().toLowerCase().startsWith(subCommandName.toLowerCase())) {
						subCommandNamesIterator.remove();
					}
				}
				return subCommandNames;
			}
			
			if(subCommandName.equals("help")) {
				return new ArrayList<String>();
			}
			else if(subCommandName.equals("info")) {
				return new ArrayList<String>();
			}
			else if(subCommandName.equals("modify")) {
				return getPlayerSuggestions(splitCommand, endsWithSpace);
			}
			else if(subCommandName.equals("changeowner")) {
				if(!splitCommand.isEmpty()) {
					return new ArrayList<String>();
				}
				return getPlayerSuggestions(splitCommand, endsWithSpace);
			}
			else {
				return null;
			}
		}
		else if(commandName.equals("shelp")) {
			return new ArrayList<String>();
		}
		else if(commandName.equals("sinfo")) {
			return new ArrayList<String>();
		}
		else if(commandName.equals("smodify")) {
			return getPlayerSuggestions(splitCommand, endsWithSpace);
		}
		else if(commandName.equals("schangeowner")) {
			if(!splitCommand.isEmpty()) {
				return new ArrayList<String>();
			}
			return getPlayerSuggestions(splitCommand, endsWithSpace);
		}
		else {
			return null;
		}
	}
	
	public boolean isCommand(final String commandName) {
		return getDescription().getCommands().keySet().contains(commandName);
	}
	
	public PrivateLiftSign getPrivateLiftSign(final Location location) {
		return loadLiftSign(location);
	}
	
	public void addPrivateLiftSign(final PrivateLiftSign privateLiftSign, final Player player) {
		if(!saveLiftSign(privateLiftSign)) {
			player.sendMessage(ConfigMessage.getLiftsignFileErrorSave());
		}
	}
	
	public boolean isPendingInformation(final Player player) {
		return pendingInformation.contains(player.getUniqueId());
	}
	
	public void getInformation(final Location location, final Player player) {
		
		if(!pendingInformation.contains(player.getUniqueId())) {
			return;
		}
		pendingInformation.remove(player.getUniqueId());
		
		if(LiftSign.isPublicLiftSign(location)) {
			player.sendMessage(ConfigMessage.getLiftsignInfoPublic());
		}
		else if(LiftSign.isPrivateLiftSign(location)) {
			
			final PrivateLiftSign privateLiftSign = loadLiftSign(location);
			if(privateLiftSign == null) {
				player.sendMessage(ConfigMessage.getLiftsignInfoError());
				return;
			}
			
			final UUID owner = privateLiftSign.getOwner();
			final HashSet<UUID> admins = privateLiftSign.getAdmins();
			final HashSet<UUID> members = privateLiftSign.getMembers();
			
			player.sendMessage("§r§8================================§r");
			player.sendMessage(ConfigMessage.getLiftsignInfoPrivate());
			player.sendMessage("§r§8--------------------------------§r");
			player.sendMessage("§r§6Owner§r§f:§r");
			player.sendMessage("§r§f -§r §b" + uniqueIdToName.get(owner) + "§r §6[" + owner.toString() + "]§r");
			
			if(!admins.isEmpty()) {
				player.sendMessage("§r§8--------------------------------§r");
				player.sendMessage("§r§6Admin§r§f:§r");
				for(final UUID admin : admins) {
					player.sendMessage("§r§f -§r §b" + uniqueIdToName.get(admin) + "§r §6[" + admin.toString() + "]§r");
				}
			}
			if(!members.isEmpty()) {
				player.sendMessage("§r§8--------------------------------§r");
				player.sendMessage("§r§6Members§r§f:§r");
				for(final UUID member : members) {
					player.sendMessage("§r§f -§r §b" + uniqueIdToName.get(member) + "§r §6[" + member.toString() + "]§r");
				}
			}
			
			player.sendMessage("§r§8================================§r");
		}
		else {
			player.sendMessage(ConfigMessage.getLiftsignInfoDeny());
		}
	}
	
	public boolean isPendingModification(final Player player) {
		return pendingModifications.containsKey(player.getUniqueId());
	}
	
	public boolean removePendingModification(final Player player) {
		return pendingModifications.remove(player.getUniqueId()) != null;
	}
	
	public void modifyPrivateLiftSign(final Location location, final Player player) {
		
		if(!pendingModifications.containsKey(player.getUniqueId())) {
			return;
		}
		
		final ChangeData changeData = pendingModifications.get(player.getUniqueId());
		final boolean changeOwner = changeData.isOwnerChanging();
		final UUID newOwner = changeData.getNewOwner();
		final HashSet<UUID> admins = changeData.getAdmins();
		final HashSet<UUID> members = changeData.getMembers();
		final HashSet<UUID> removals = changeData.getRemovals();
		final ArrayList<String> unknowns = changeData.getUnknowns();
		
		pendingModifications.remove(player.getUniqueId());
		
		final PrivateLiftSign privateLiftSign = loadLiftSign(location);
		if(privateLiftSign == null) {
			if(LiftSign.isPublicLiftSign(location)) {
				player.sendMessage(ConfigMessage.getLiftsignMaintainModifyPublicDeny());
			}
			else {
				player.sendMessage(ConfigMessage.getLiftsignMaintainModifyOtherDeny());
			}
			return;
		}
		
		final boolean canModifyOwner = privateLiftSign.canModifyOwner(player);
		final boolean canModifyAdmins = privateLiftSign.canModifyAdmins(player);
		final boolean canModifyMembers = privateLiftSign.canModifyMembers(player);
		
		if(changeOwner) {
			if(canModifyOwner) {
				
				privateLiftSign.changeOwner(newOwner);
				player.sendMessage(ConfigMessage.getLiftsignMaintainModifyPrivateOwnerAllow().replace("%%player%%", uniqueIdToName.get(newOwner)));
			}
			else {
				player.sendMessage(ConfigMessage.getLiftsignMaintainModifyPrivateOwnerDeny());
			}
		}
		
		final ArrayList<String> messages = new ArrayList<String>();
		
		if(!admins.isEmpty()) {
			if(canModifyAdmins) {
				
				for(final UUID admin : admins) {
					if(privateLiftSign.addAdmin(admin)) {
						if(privateLiftSign.isMember(admin)) {
							privateLiftSign.removeMember(admin);
							messages.add(ConfigMessage.getLiftsignMaintainModifyPrivateAdminChange().replace("%%player%%", uniqueIdToName.get(admin)));
						}
						else {
							messages.add(ConfigMessage.getLiftsignMaintainModifyPrivateAdminTrue().replace("%%player%%", uniqueIdToName.get(admin)));
						}
					}
					else {
						messages.add(ConfigMessage.getLiftsignMaintainModifyPrivateAdminFalse().replace("%%player%%", uniqueIdToName.get(admin)));
					}
				}
			}
			else {
				messages.add(ConfigMessage.getLiftsignMaintainModifyPrivateAdminDeny());
			}
		}
		
		if(!members.isEmpty()) {
			if(canModifyMembers) {
				
				for(final UUID member : members) {
					if(privateLiftSign.addMember(member)) {
						if(privateLiftSign.isAdmin(member)) {
							if(canModifyAdmins) {
								privateLiftSign.removeAdmin(member);
								messages.add(ConfigMessage.getLiftsignMaintainModifyPrivateMemberChange().replace("%%player%%", uniqueIdToName.get(member)));
							}
							else {
								privateLiftSign.removeMember(member);
								messages.add(ConfigMessage.getLiftsignMaintainModifyPrivateAdminDeny());
							}
						}
						else {
							messages.add(ConfigMessage.getLiftsignMaintainModifyPrivateMemberTrue().replace("%%player%%", uniqueIdToName.get(member)));
						}
					}
					else {
						messages.add(ConfigMessage.getLiftsignMaintainModifyPrivateMemberFalse().replace("%%player%%", uniqueIdToName.get(member)));
					}
				}
			}
			else {
				messages.add(ConfigMessage.getLiftsignMaintainModifyPrivateMemberDeny());
			}
		}
		
		if(!removals.isEmpty()) {
			
			final HashSet<UUID> removeAdmins = new HashSet<UUID>();
			final HashSet<UUID> removeMembers = new HashSet<UUID>();
			
			for(final UUID removal : removals) {
				if(privateLiftSign.isAdmin(removal)) {
					removeAdmins.add(removal);
				}
				else if(privateLiftSign.isMember(removal)) {
					removeMembers.add(removal);
				}
			}
			
			removals.removeAll(removeAdmins);
			removals.removeAll(removeMembers);
			
			if(!removeAdmins.isEmpty()) {
				if(canModifyAdmins) {
					
					for(final UUID admin : removeAdmins) {
						if(privateLiftSign.removeAdmin(admin)) {
							messages.add(ConfigMessage.getLiftsignMaintainModifyPrivateRemoveTrue().replace("%%player%%", uniqueIdToName.get(admin)));
						}
						else {
							messages.add(ConfigMessage.getLiftsignMaintainModifyPrivateRemoveFalse().replace("%%player%%", uniqueIdToName.get(admin)));
						}
					}
				}
				else {
					messages.add(ConfigMessage.getLiftsignMaintainModifyPrivateRemoveDeny());
				}
			}
			
			if(!removeMembers.isEmpty()) {
				if(canModifyMembers) {
					
					for(final UUID member : removeMembers) {
						if(privateLiftSign.removeMember(member)) {
							messages.add(ConfigMessage.getLiftsignMaintainModifyPrivateRemoveTrue().replace("%%player%%", uniqueIdToName.get(member)));
						}
						else {
							messages.add(ConfigMessage.getLiftsignMaintainModifyPrivateRemoveFalse().replace("%%player%%", uniqueIdToName.get(member)));
						}
					}
				}
				else {
					messages.add(ConfigMessage.getLiftsignMaintainModifyPrivateRemoveDeny());
				}
			}
			
			if(!removals.isEmpty()) {
				if(!canModifyAdmins && !canModifyMembers) {
					messages.add(ConfigMessage.getLiftsignMaintainModifyPrivateRemoveDeny());
				}
				else {
					
					for(final UUID removal : removals) {
						messages.add(ConfigMessage.getLiftsignMaintainModifyPrivateRemoveFalse().replace("%%player%%", uniqueIdToName.get(removal)));
					}
				}
			}
		}
		
		if(!unknowns.isEmpty()) {
			if(!canModifyOwner && !canModifyAdmins && !canModifyMembers) {
				messages.add(ConfigMessage.getLiftsignMaintainModifyPrivateUnknownDeny());
			}
			else {
				
				for(final String unknown : unknowns) {
					messages.add(ConfigMessage.getLiftsignMaintainModifyPrivateUnknownUnknown().replace("%%player%%", unknown));
				}
			}
		}
		
		for(final String message : messages) {
			player.sendMessage(message);
		}
		
		if(!saveLiftSign(privateLiftSign)) {
			player.sendMessage(ConfigMessage.getLiftsignFileErrorSave());
		}
	}
	
	public boolean removePrivateLiftSign(final Location location) {
		return deleteLiftSign(location);
	}
	
	public void usePrivateLiftSign(final Location location, final Player player) {
		
		final PrivateLiftSign privateLiftSign = loadLiftSign(location);
		if(privateLiftSign == null) {
			return;
		}
		
		privateLiftSign.activate(player, this);
	}
	
	private FileConfiguration getMessages() {
		
		try {
			
			final InputStream messagesStream = getResource("messages.yml");
			final File messagesFile = new File(getDataFolder(), "tmp-messages.yml");
			
			messagesFile.createNewFile();
			
			final FileOutputStream outputStream = new FileOutputStream(messagesFile);
			final byte[] buffer = new byte[8192];
			int length = 0;
			
			while((length = messagesStream.read(buffer)) > 0) {
				outputStream.write(buffer, 0, length);
			}
			
			messagesStream.close();
			outputStream.close();
			
			final FileConfiguration messagesConfig = new YamlConfiguration();
			messagesConfig.load(messagesFile);
			
			messagesFile.delete();
			
			return messagesConfig;
		}
		catch(IOException e) {
			throw new IllegalArgumentException(e);
		}
		catch(NullPointerException e) {
			throw new IllegalArgumentException(e);
		}
		catch(InvalidConfigurationException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	private boolean helpCommand(final Player player) {
		
		player.sendMessage("§r§8================================================§r");
		player.sendMessage("§r§6SignLift Help§r");
		player.sendMessage("§r§8------------------------------------------------§r");
		
		final Server server = getServer();
		final ArrayList<String> allowedCommands = new ArrayList<String>();
		
		if(player.hasPermission(server.getPluginCommand("shelp").getPermission())) {
			allowedCommands.add("§r§ahelp§r §b(/shelp)§r");
		}
		if(player.hasPermission(server.getPluginCommand("sinfo").getPermission())) {
			allowedCommands.add("§r§ainfo§r §b(/sinfo)§r");
		}
		if(player.hasPermission(server.getPluginCommand("smodify").getPermission())) {
			allowedCommands.add("§r§amodify§r §b(/smodify)§r");
		}
		if(player.hasPermission(server.getPluginCommand("schangeowner").getPermission())) {
			allowedCommands.add("§r§achangeowner§r §b(/schangeowner)§r");
		}
		
		if(allowedCommands.isEmpty()) {
			player.hasPermission("§r§cNo commands.§r");
			player.sendMessage("§r§8================================================§r");
			return true;
		}
		
		for(final String allowedCommand : allowedCommands) {
			player.sendMessage("§r§f -§r §a/signlift " + allowedCommand + "§r");
		}
		
		player.sendMessage("§r§8================================§r");
		return true;
	}
	
	private boolean infoCommand(final Player player) {
		
		player.sendMessage(ConfigMessage.getCommandInfoDefault());
		pendingInformation.add(player.getUniqueId());
		return true;
	}
	
	private boolean modifyCommand(final Player player, final String[] args, final boolean skipFirst) {
		
		int index;
		if(skipFirst) {
			index = 1;
		}
		else {
			index = 0;
		}
		
		final ChangeData changeData = new ChangeData(false, null);
		for(; index < args.length; index++) {
			
			String playerName = args[index];
			
			if(playerName.startsWith("@")) {
				
				playerName = playerName.substring(1);
				final UUID admin = nameToUniqueId.get(playerName.toLowerCase());
				if(admin == null) {
					changeData.addUnknown(playerName);
					continue;
				}
				
				try {
					changeData.addAdmin(admin);
				}
				catch(SignLiftException e) {
					player.sendMessage("§r§c" + e.getMessage() + "§r");
					return true;
				}
			}
			else if(playerName.startsWith("-")) {
				
				playerName = playerName.substring(1);
				final UUID removal = nameToUniqueId.get(playerName.toLowerCase());
				if(removal == null) {
					changeData.addUnknown(playerName);
					continue;
				}
				
				try {
					changeData.remove(removal);
				}
				catch(SignLiftException e) {
					player.sendMessage("§r§c" + e.getMessage() + "§r");
					return true;
				}
			}
			else {
				
				final UUID member = nameToUniqueId.get(playerName.toLowerCase());
				if(member == null) {
					changeData.addUnknown(playerName);
					continue;
				}
				
				try {
					changeData.addMember(member);
				}
				catch(SignLiftException e) {
					player.sendMessage("§r§c" + e.getMessage() + "§r");
					return true;
				}
			}
		}
		
		pendingModifications.put(player.getUniqueId(), changeData);
		player.sendMessage(ConfigMessage.getCommandModifyDefault());
		return true;
	}
	
	private boolean changeOwnerCommand(final Player player, final String playerName) {
		
		final ChangeData changeData;
		final UUID newOwner = nameToUniqueId.get(playerName.toLowerCase());
		
		if(newOwner == null) {
			changeData = new ChangeData(false, null);
			changeData.addUnknown(playerName);
		}
		else {
			changeData = new ChangeData(true, newOwner);
		}
		
		pendingModifications.put(player.getUniqueId(), changeData);
		player.sendMessage(ConfigMessage.getCommandModifyDefault());
		return true;
	}
	
	private ArrayList<String> getPlayerSuggestions(final ArrayList<String> splitCommand, final boolean endsWithSpace) {
		
		final HashSet<String> rawPlayers = new HashSet<String>();
		for(final Player player : getServer().getOnlinePlayers()) {
			rawPlayers.add(player.getName());
		}
		for(final String player : uniqueIdToName.values()) {
			rawPlayers.add(player);
		}
		
		if(splitCommand.isEmpty() || endsWithSpace) {
			return new ArrayList<String>(rawPlayers);
		}
		
		final String lastPlayer = splitCommand.get(splitCommand.size() - 1);
		final String prefix;
		if(lastPlayer.startsWith("@")) {
			prefix = "@";
		}
		else if(lastPlayer.startsWith("-")) {
			prefix = "-";
		}
		else {
			prefix = "";
		}
		
		final ArrayList<String> players = new ArrayList<String>(rawPlayers);
		for(int index = 0; index < players.size(); index++) {
			players.set(index, prefix + players.get(index));
		}
		
		final Iterator<String> playerIterator = players.iterator();
		while(playerIterator.hasNext()) {
			if(!playerIterator.next().toLowerCase().startsWith(lastPlayer.toLowerCase())) {
				playerIterator.remove();
			}
		}
		
		return players;
	}
	
	private boolean savePlayerData(final PlayerData playerData) {
		
		final YamlConfiguration config = new YamlConfiguration();
		config.set("player_data", playerData);
		
		final File configFile = new File(playerDataFolder, playerData.getUniqueId().toString() + ".yml");
		final String configFilePath = configFile.getPath();
		
		try {
			if(!configFile.exists()) {
				try {
					configFile.createNewFile();
				}
				catch(IOException e) {
					logger.log(Level.WARNING, "Unable to create player data config file at " + configFilePath);
					logger.log(Level.WARNING, "IOException thrown.", e);
					return false;
				}
				catch(SecurityException e) {
					logger.log(Level.WARNING, "Unable to create player data config file at " + configFilePath);
					logger.log(Level.WARNING, "SecurityException thrown.", e);
					return false;
				}
			}
		}
		catch(SecurityException e) {
			logger.log(Level.WARNING, "Unable to verify if player data config file exists at " + configFilePath);
			logger.log(Level.WARNING, "SecurityException thrown.", e);
			return false;
		}
		
		try {
			config.save(configFile);
		}
		catch(IOException e) {
			logger.log(Level.WARNING, "Unable to save player data config file at " + configFilePath);
			logger.log(Level.WARNING, "IOException thrown.", e);
			return false;
		}
		
		return true;
	}
	
	private PrivateLiftSign loadLiftSign(final Location location) {
		
		final File configFile = new File(privateLiftSignFolder, getConfigFileName(location));
		final String configFilePath = configFile.getPath();
		
		try {
			if(!configFile.exists()) {
				return null;
			}
		}
		catch(SecurityException e) {
			logger.log(Level.WARNING, "Unable to verify if private lift sign config file exists for load at " + configFilePath);
			logger.log(Level.WARNING, "SecurityException thrown.", e);
			return null;
		}
		
		final YamlConfiguration config = new YamlConfiguration();
		try {
			config.load(configFile);
		}
		catch(FileNotFoundException e) {
			logger.log(Level.WARNING, "Unable to load private lift sign configuration file at " + configFilePath);
			logger.log(Level.WARNING, "Skipping private lift sign.");
			logger.log(Level.WARNING, "FileNotFoundException thrown.", e);
			return null;
		}
		catch(IOException e) {
			logger.log(Level.WARNING, "Unable to load private lift sign configuration file at " + configFilePath);
			logger.log(Level.WARNING, "Skipping private lift sign.");
			logger.log(Level.WARNING, "IOException thrown.", e);
			return null;
		}
		catch(InvalidConfigurationException e) {
			logger.log(Level.WARNING, "Unable to load private lift sign configuration file at " + configFilePath);
			logger.log(Level.WARNING, "Skipping private lift sign.");
			logger.log(Level.WARNING, "InvalidConfigurationException thrown.", e);
			return null;
		}
		catch(IllegalArgumentException e) {
			logger.log(Level.WARNING, "Unable to load private lift sign configuration file at " + configFilePath);
			logger.log(Level.WARNING, "Skipping private lift sign.");
			logger.log(Level.WARNING, "IllegalArgumentException thrown.", e);
			return null;
		}
		
		return config.getSerializable("private_lift_sign", PrivateLiftSign.class);
	}
	
	private boolean saveLiftSign(final PrivateLiftSign privateLiftSign) {
		
		final YamlConfiguration config = new YamlConfiguration();
		config.set("private_lift_sign", privateLiftSign);
		
		final File configFile = new File(privateLiftSignFolder, getConfigFileName(privateLiftSign.getLocation()));
		final String configFilePath = configFile.getPath();
		
		try {
			if(!configFile.exists()) {
				try {
					configFile.createNewFile();
				}
				catch(IOException e) {
					logger.log(Level.WARNING, "Unable to create private lift sign config file at " + configFilePath);
					logger.log(Level.WARNING, "IOException thrown.", e);
					return false;
				}
				catch(SecurityException e) {
					logger.log(Level.WARNING, "Unable to create private lift sign config file at " + configFilePath);
					logger.log(Level.WARNING, "SecurityException thrown.", e);
					return false;
				}
			}
		}
		catch(SecurityException e) {
			logger.log(Level.WARNING, "Unable to verify if private lift sign config file exists for save at " + configFilePath);
			logger.log(Level.WARNING, "SecurityException thrown.", e);
			return false;
		}
		
		try {
			config.save(configFile);
		}
		catch(IOException e) {
			logger.log(Level.WARNING, "Unable to save private lift sign config file at " + configFilePath);
			logger.log(Level.WARNING, "IOException thrown.", e);
			return false;
		}
		
		return true;
	}
	
	private boolean deleteLiftSign(final Location location) {
		
		final File configFile = new File(privateLiftSignFolder, getConfigFileName(location));
		final String configFilePath = configFile.getPath();
		
		final boolean deleted;
		try {
			deleted = configFile.delete();
		}
		catch(SecurityException e) {
			logger.log(Level.WARNING, "Unable to delete private lift sign config file at " + configFilePath);
			logger.log(Level.WARNING, "SecurityException thrown.", e);
			return false;
		}
		
		if(!deleted) {
			logger.log(Level.WARNING, "Private lift sign file was not deleted, no exception thrown.");
		}
		
		return deleted;
	}
	
	private String getConfigFileName(final Location location) {
		
		final String world = location.getWorld().getName();
		
		String x = String.valueOf(Math.abs(location.getBlockX()));
		String y = String.valueOf(Math.abs(location.getBlockY()));
		String z = String.valueOf(Math.abs(location.getBlockZ()));
		
		if(location.getBlockX() < 0) {
			x = "n" + x;
		}
		if(location.getBlockY() < 0) {
			y = "n" + y;
		}
		if(location.getBlockZ() < 0) {
			z = "n" + z;
		}
		
		return world + "-" + x + "-" + y + "-" + z + ".yml";
	}
}