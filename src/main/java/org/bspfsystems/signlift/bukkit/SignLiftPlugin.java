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

package org.bspfsystems.signlift.bukkit;

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
import org.bspfsystems.signlift.bukkit.command.SignLiftTabExecutor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bspfsystems.playerdata.bukkit.PlayerDataEntry;
import org.bspfsystems.signlift.bukkit.config.ConfigData;
import org.bspfsystems.signlift.bukkit.config.ConfigMessage;
import org.bspfsystems.signlift.bukkit.exception.SignLiftException;
import org.bspfsystems.signlift.bukkit.liftsign.LiftSign;
import org.bspfsystems.signlift.bukkit.liftsign.PrivateLiftSign;
import org.bspfsystems.signlift.bukkit.listener.SignLiftEventHandler;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the main entrypoint for the {@link SignLiftPlugin} functionality.
 */
public final class SignLiftPlugin extends JavaPlugin {
    
    private Logger logger;
    
    private File dataFolder;
    private File playerDataFolder;
    private File privateLiftSignFolder;
    
    private ConcurrentHashMap<String, UUID> nameToUniqueId;
    private ConcurrentHashMap<UUID, String> uniqueIdToName;
    
    private HashSet<UUID> pendingInformation;
    private ConcurrentHashMap<UUID, ChangeData> pendingModifications;
    
    public SignLiftPlugin() {
        super();
    }
    
    @Override
    public void onEnable() {
        
        this.logger = this.getLogger();
        
        this.logger.log(Level.INFO, "///////////////////////////////////////////////////////////////////////////");
        this.logger.log(Level.INFO, "//                                                                       //");
        this.logger.log(Level.INFO, "// Copyright (C) 2011      Shannon Wynter (http://fremnet.net/)          //");
        this.logger.log(Level.INFO, "// Copyright (C) 2012      GoalieGuy6 (https://github.com/goalieguy6)    //");
        this.logger.log(Level.INFO, "// Copyright (C) 2018,2020 Matt Ciolkosz (https://github.com/mciolkosz)  //");
        this.logger.log(Level.INFO, "// Copyright (C) 2021      BSPF Systems, LLC (https://bspfsystems.org/)  //");
        this.logger.log(Level.INFO, "//                                                                       //");
        this.logger.log(Level.INFO, "// This program is free software: you can redistribute it and/or modify  //");
        this.logger.log(Level.INFO, "// it under the terms of the GNU General Public License as published by  //");
        this.logger.log(Level.INFO, "// the Free Software Foundation, either version 3 of the License, or     //");
        this.logger.log(Level.INFO, "// (at your option) any later version.                                   //");
        this.logger.log(Level.INFO, "//                                                                       //");
        this.logger.log(Level.INFO, "// This program is distributed in the hope that it will be useful, but   //");
        this.logger.log(Level.INFO, "// WITHOUT ANY WARRANTY; without even the implied warranty of            //");
        this.logger.log(Level.INFO, "// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU     //");
        this.logger.log(Level.INFO, "// General Public License for more details.                              //");
        this.logger.log(Level.INFO, "//                                                                       //");
        this.logger.log(Level.INFO, "// You should have received a copy of the GNU General Public License     //");
        this.logger.log(Level.INFO, "// along with this program.  If not, see <http://www.gnu.org/licenses/>. //");
        this.logger.log(Level.INFO, "//                                                                       //");
        this.logger.log(Level.INFO, "///////////////////////////////////////////////////////////////////////////");
        
        // Command handling setup
        
        final TabExecutor signLiftTabExecutor = new SignLiftTabExecutor(this);
        this.registerCommand("signlift", signLiftTabExecutor);
        this.registerCommand("sreload", signLiftTabExecutor);
        this.registerCommand("shelp", signLiftTabExecutor);
        this.registerCommand("sinfo", signLiftTabExecutor);
        this.registerCommand("smodify", signLiftTabExecutor);
        this.registerCommand("schangeowner", signLiftTabExecutor);
        
        // Data folder checks
        
        this.dataFolder = this.getDataFolder();
        try {
            if (!this.dataFolder.exists()) {
                if (!this.dataFolder.mkdirs()) {
                    this.logger.log(Level.WARNING, "SignLift data directory not created at " + this.dataFolder.getPath());
                    this.logger.log(Level.WARNING, "SignLift functionality will be disabled.");
                    return;
                }
            } else if (!this.dataFolder.isDirectory()) {
                this.logger.log(Level.WARNING, "SignLift data directory is not a directory: " + this.dataFolder.getPath());
                this.logger.log(Level.WARNING, "SignLift functionality will be disabled.");
                return;
            }
        } catch (SecurityException e) {
            this.logger.log(Level.WARNING, "Unable to validate if the SignLift data directory has been properly created at " + this.dataFolder.getPath());
            this.logger.log(Level.WARNING, "SignLift functionality will be disabled.");
            this.logger.log(Level.WARNING, e.getClass().getSimpleName() + " thrown.", e);
            return;
        }
        
        this.playerDataFolder = new File(this.dataFolder, "PlayerData");
        try {
            if (!this.playerDataFolder.exists()) {
                if (!this.playerDataFolder.mkdirs()) {
                    this.logger.log(Level.WARNING, "SignLift PlayerData directory not created at " + this.playerDataFolder.getPath());
                    this.logger.log(Level.WARNING, "SignLift functionality will be disabled.");
                    return;
                }
            } else if (!this.playerDataFolder.isDirectory()) {
                this.logger.log(Level.WARNING, "SignLift PlayerData directory is not a directory: " + this.playerDataFolder.getPath());
                this.logger.log(Level.WARNING, "SignLift functionality will be disabled.");
                return;
            }
        } catch (SecurityException e) {
            this.logger.log(Level.WARNING, "Unable to validate if the SignLift PlayerData directory has been properly created at " + this.playerDataFolder.getPath());
            this.logger.log(Level.WARNING, "SignLift functionality will be disabled.");
            this.logger.log(Level.WARNING, e.getClass().getSimpleName() + " thrown.", e);
            return;
        }
    
        this.privateLiftSignFolder = new File(this.dataFolder, "PrivateLiftSigns");
        try {
            if (!this.privateLiftSignFolder.exists()) {
                if (!this.privateLiftSignFolder.mkdirs()) {
                    this.logger.log(Level.WARNING, "SignLift PrivateLiftSign directory not created at " + this.privateLiftSignFolder.getPath());
                    this.logger.log(Level.WARNING, "SignLift functionality will be disabled.");
                    return;
                }
            } else if (!this.privateLiftSignFolder.isDirectory()) {
                this.logger.log(Level.WARNING, "SignLift PrivateLiftSign directory is not a directory: " + this.privateLiftSignFolder.getPath());
                this.logger.log(Level.WARNING, "SignLift functionality will be disabled.");
                return;
            }
        } catch (SecurityException e) {
            this.logger.log(Level.WARNING, "Unable to validate if the SignLift PrivateLiftSign directory has been properly created at " + this.privateLiftSignFolder.getPath());
            this.logger.log(Level.WARNING, "SignLift functionality will be disabled.");
            this.logger.log(Level.WARNING, e.getClass().getSimpleName() + " thrown.", e);
            return;
        }
        
        // PlayerData loading
        
        final File[] playerDataConfigFiles = this.playerDataFolder.listFiles();
        if (playerDataConfigFiles == null) {
            this.logger.log(Level.WARNING, "SignLift PlayerData directory is not a directory, failed previous check.");
            this.logger.log(Level.WARNING, "SignLift functionality will be disabled.");
            return;
        }
        
        this.nameToUniqueId = new ConcurrentHashMap<String, UUID>();
        this.uniqueIdToName = new ConcurrentHashMap<UUID, String>();
        
        for (final File playerDataConfigFile : playerDataConfigFiles) {
            
            final YamlConfiguration playerDataConfig = new YamlConfiguration();
            try {
                playerDataConfig.load(playerDataConfigFile);
            } catch (IOException | IllegalArgumentException | InvalidConfigurationException e) {
                this.logger.log(Level.WARNING, "Unable to load PlayerData configuration file at " + playerDataConfigFile.getPath());
                this.logger.log(Level.WARNING, "Skipping player.");
                this.logger.log(Level.WARNING, e.getClass().getSimpleName() + " thrown.", e);
                continue;
            }
            
            final PlayerDataEntry playerDataEntry = PlayerDataEntry.deserialize(playerDataConfig);
            this.nameToUniqueId.put(playerDataEntry.getName().toLowerCase(), playerDataEntry.getUniqueId());
            this.uniqueIdToName.put(playerDataEntry.getUniqueId(), playerDataEntry.getName());
        }
        
        // Miscellaneous setup
        
        this.pendingInformation = new HashSet<UUID>();
        this.pendingModifications = new ConcurrentHashMap<UUID, ChangeData>();
        
        this.getServer().getPluginManager().registerEvents(new SignLiftEventHandler(this), this);
        
        ConfigData.reloadConfig(this, this.getServer().getConsoleSender(), false);
        ConfigMessage.reloadMessages(this, this.getServer().getConsoleSender(), false);
    }
    
    /**
     * Registers the {@link PluginCommand} with the given name to the given
     * {@link TabExecutor}.
     * <p>
     * If no {@link PluginCommand} is found, an error will be logged and a
     * {@link RuntimeException} will be thrown.
     * 
     * @param commandName The name of the {@link PluginCommand} to retrieve.
     * @param tabExecutor The {@link TabExecutor} to register to the
     *                    {@link PluginCommand}.
     * @throws RuntimeException If no {@link PluginCommand} with the given name
     *                          can be found.
     */
    private void registerCommand(@NotNull final String commandName, @NotNull final TabExecutor tabExecutor) throws RuntimeException {
        final PluginCommand command = this.getCommand(commandName);
        if (command == null) {
            this.logger.log(Level.SEVERE, "Cannot find the /" + commandName + " command.");
            this.logger.log(Level.SEVERE, "SignLift functionality will be disabled.");
            throw new RuntimeException("Cannot find the /" + commandName + " command.");
        }
        command.setExecutor(tabExecutor);
        command.setTabCompleter(tabExecutor);
    }
    
    ////////////////////////////
    // EVENT LISTENER METHODS //
    ////////////////////////////
    
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
            
            this.logger.log(Level.INFO, "================================================");
            this.logger.log(Level.INFO, "Player Login : NEW PLAYER");
            this.logger.log(Level.INFO, "------------------------------------------------");
            this.logger.log(Level.INFO, "Name : " + currentName);
            this.logger.log(Level.INFO, "UUID : " + uniqueId.toString());
            this.logger.log(Level.INFO, "================================================");
            
            nameToUniqueId.put(currentName.toLowerCase(), uniqueId);
            uniqueIdToName.put(uniqueId, currentName);
            
            if(!savePlayerData(new PlayerDataEntry(player))) {
                this.logger.log(Level.WARNING, "Unable to save PlayerData.");
                this.logger.log(Level.WARNING, "Name : " + currentName);
                this.logger.log(Level.WARNING, "UUID : " + uniqueId.toString());
            }
        }
        else if(!nameToUniqueId.containsKey(currentName.toLowerCase())) {
            
            this.logger.log(Level.INFO, "================================================");
            this.logger.log(Level.INFO, "Player Login: UPDATE NAME");
            this.logger.log(Level.INFO, "------------------------------------------------");
            this.logger.log(Level.INFO, "Old Name : " + uniqueIdToName.get(uniqueId));
            this.logger.log(Level.INFO, "New Name : " + currentName);
            this.logger.log(Level.INFO, "UUID     : " + uniqueId.toString());
            this.logger.log(Level.INFO, "================================================");
            
            nameToUniqueId.remove(uniqueIdToName.get(uniqueId));
            uniqueIdToName.remove(uniqueId);
            
            nameToUniqueId.put(currentName.toLowerCase(), uniqueId);
            uniqueIdToName.put(uniqueId, currentName);
            
            if(!savePlayerData(new PlayerDataEntry(player))) {
                this.logger.log(Level.WARNING, "Unable to save PlayerData.");
                this.logger.log(Level.WARNING, "Name : " + currentName);
                this.logger.log(Level.WARNING, "UUID : " + uniqueId.toString());
            }
        }
        else {
            
            this.logger.log(Level.INFO, "================================================");
            this.logger.log(Level.INFO, "Player Login: KNOWN PLAYER");
            this.logger.log(Level.INFO, "------------------------------------------------");
            this.logger.log(Level.INFO, "Name : " + currentName);
            this.logger.log(Level.INFO, "UUID : " + uniqueId.toString());
            this.logger.log(Level.INFO, "================================================");
        }
    }
    
    public List<String> onTabComplete(final String buffer, final List<String> completions, final CommandSender sender) {
        
        if(!(sender instanceof Player)) {
            if(completions.contains("/signlift")) {
                completions.remove("/signlift");
            }
            if(completions.contains("/shelp")) {
                completions.remove("/shelp");
            }
            if(completions.contains("/sinfo")) {
                completions.remove("/sinfo");
            }
            if(completions.contains("/smodify")) {
                completions.remove("/smodify");
            }
            if(completions.contains("/schangeowner")) {
                completions.remove("/schangeowner");
            }
            return completions;
        }
        
        final Player player = (Player) sender;
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
            return completions;
        }
        
        final boolean permissionSignlift = player.hasPermission("signlift.command.signlift");
        final boolean permissionSignliftHelp = player.hasPermission("signlift.command.signlift.help");
        final boolean permissionSignliftInfo = player.hasPermission("signlift.command.signlift.info");
        final boolean permissionSignliftModify = player.hasPermission("signlift.command.signlift.modify");
        final boolean permissionSignliftChangeowner = player.hasPermission("signlift.command.signlift.changeowner");
        
        final String commandName = splitCommand.remove(0);
        if(splitCommand.isEmpty() && !endsWithSpace) {
            if(completions.contains("/signlift") && !permissionSignlift) {
                completions.remove("/signlift");
            }
            if(completions.contains("/shelp") && !permissionSignlift && !permissionSignliftHelp) {
                completions.remove("/shelp");
            }
            if(completions.contains("/sinfo") && !permissionSignlift && !permissionSignliftInfo) {
                completions.remove("/sinfo");
            }
            if(completions.contains("/smodify") && !permissionSignlift && !permissionSignliftModify) {
                completions.remove("/smodify");
            }
            if(completions.contains("/schangeowner") && !permissionSignlift && !permissionSignliftChangeowner) {
                completions.remove("/schangeowner");
            }
            return completions;
        }
        
        if(commandName.equals("signlift")) {
            completions.clear();
            
            if(sender.hasPermission(server.getPluginCommand("shelp").getPermission())) {
                completions.add("help");
            }
            if(sender.hasPermission(server.getPluginCommand("sinfo").getPermission())) {
                completions.add("info");
            }
            if(sender.hasPermission(server.getPluginCommand("smodify").getPermission())) {
                completions.add("modify");
            }
            if(sender.hasPermission(server.getPluginCommand("schangeowner").getPermission())) {
                completions.add("changeowner");
            }
            
            if(splitCommand.isEmpty()) {
                return completions;
            }
            
            final String subCommandName = splitCommand.remove(0);
            if(!endsWithSpace) {
                
                final Iterator<String> completionsIterator = completions.iterator();
                while(completionsIterator.hasNext()) {
                    if(!completionsIterator.next().toLowerCase().startsWith(subCommandName.toLowerCase())) {
                        completionsIterator.remove();
                    }
                }
                return completions;
            }
            
            completions.clear();
            
            if(subCommandName.equals("help")) {
                return completions;
            }
            else if(subCommandName.equals("info")) {
                return completions;
            }
            else if(subCommandName.equals("modify")) {
                return getPlayerSuggestions(completions, splitCommand, endsWithSpace);
            }
            else if(subCommandName.equals("changeowner")) {
                if(!splitCommand.isEmpty()) {
                    return completions;
                }
                return getPlayerSuggestions(completions, splitCommand, endsWithSpace);
            }
            else {
                return completions;
            }
        }
        else if(commandName.equals("shelp")) {
            completions.clear();
            return completions;
        }
        else if(commandName.equals("sinfo")) {
            completions.clear();
            return completions;
        }
        else if(commandName.equals("smodify")) {
            completions.clear();
            return getPlayerSuggestions(completions, splitCommand, endsWithSpace);
        }
        else if(commandName.equals("schangeowner")) {
            completions.clear();
            if(!splitCommand.isEmpty()) {
                return completions;
            }
            return getPlayerSuggestions(completions, splitCommand, endsWithSpace);
        }
        else {
            return completions;
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
        final UUID newOwner = changeData.getOwner();
        final HashSet<UUID> admins = changeData.getAdmins();
        final HashSet<UUID> members = changeData.getMembers();
        final HashSet<UUID> removals = changeData.getRemovals();
        final ArrayList<String> unknowns = changeData.getUnknowns();
        
        pendingModifications.remove(player.getUniqueId());
        
        final PrivateLiftSign privateLiftSign = loadLiftSign(location);
        if(privateLiftSign == null) {
            if(LiftSign.isPublicLiftSign(location)) {
                player.sendMessage(ConfigMessage.getLiftsignModifyPublic());
            }
            else {
                player.sendMessage(ConfigMessage.getLiftsignModifyOther());
            }
            return;
        }
        
        final boolean canModifyOwner = privateLiftSign.canModifyOwner(player);
        final boolean canModifyAdmins = privateLiftSign.canModifyAdmins(player);
        final boolean canModifyMembers = privateLiftSign.canModifyMembers(player);
        
        if(changeOwner) {
            if(canModifyOwner) {
                
                privateLiftSign.changeOwner(newOwner);
                player.sendMessage(ConfigMessage.getLiftsignModifyPrivateOwnerAllow().replace("%%player%%", uniqueIdToName.get(newOwner)));
            }
            else {
                player.sendMessage(ConfigMessage.getLiftsignModifyPrivateOwnerDeny());
            }
        }
        
        final ArrayList<String> messages = new ArrayList<String>();
        
        if(!admins.isEmpty()) {
            if(canModifyAdmins) {
                
                for(final UUID admin : admins) {
                    if(privateLiftSign.addAdmin(admin)) {
                        if(privateLiftSign.isMember(admin)) {
                            privateLiftSign.removeMember(admin);
                            messages.add(ConfigMessage.getLiftsignModifyPrivateAdminChange().replace("%%player%%", uniqueIdToName.get(admin)));
                        }
                        else {
                            messages.add(ConfigMessage.getLiftsignModifyPrivateAdminTrue().replace("%%player%%", uniqueIdToName.get(admin)));
                        }
                    }
                    else {
                        messages.add(ConfigMessage.getLiftsignModifyPrivateAdminFalse().replace("%%player%%", uniqueIdToName.get(admin)));
                    }
                }
            }
            else {
                messages.add(ConfigMessage.getLiftsignModifyPrivateAdminDeny());
            }
        }
        
        if(!members.isEmpty()) {
            if(canModifyMembers) {
                
                for(final UUID member : members) {
                    if(privateLiftSign.addMember(member)) {
                        if(privateLiftSign.isAdmin(member)) {
                            if(canModifyAdmins) {
                                privateLiftSign.removeAdmin(member);
                                messages.add(ConfigMessage.getLiftsignModifyPrivateMemberChange().replace("%%player%%", uniqueIdToName.get(member)));
                            }
                            else {
                                privateLiftSign.removeMember(member);
                                messages.add(ConfigMessage.getLiftsignModifyPrivateAdminDeny());
                            }
                        }
                        else {
                            messages.add(ConfigMessage.getLiftsignModifyPrivateMemberTrue().replace("%%player%%", uniqueIdToName.get(member)));
                        }
                    }
                    else {
                        messages.add(ConfigMessage.getLiftsignModifyPrivateMemberFalse().replace("%%player%%", uniqueIdToName.get(member)));
                    }
                }
            }
            else {
                messages.add(ConfigMessage.getLiftsignModifyPrivateMemberDeny());
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
                            messages.add(ConfigMessage.getLiftsignModifyPrivateRemoveTrue().replace("%%player%%", uniqueIdToName.get(admin)));
                        }
                        else {
                            messages.add(ConfigMessage.getLiftsignModifyPrivateRemoveFalse().replace("%%player%%", uniqueIdToName.get(admin)));
                        }
                    }
                }
                else {
                    messages.add(ConfigMessage.getLiftsignModifyPrivateRemoveDeny());
                }
            }
            
            if(!removeMembers.isEmpty()) {
                if(canModifyMembers) {
                    
                    for(final UUID member : removeMembers) {
                        if(privateLiftSign.removeMember(member)) {
                            messages.add(ConfigMessage.getLiftsignModifyPrivateRemoveTrue().replace("%%player%%", uniqueIdToName.get(member)));
                        }
                        else {
                            messages.add(ConfigMessage.getLiftsignModifyPrivateRemoveFalse().replace("%%player%%", uniqueIdToName.get(member)));
                        }
                    }
                }
                else {
                    messages.add(ConfigMessage.getLiftsignModifyPrivateRemoveDeny());
                }
            }
            
            if(!removals.isEmpty()) {
                if(!canModifyAdmins && !canModifyMembers) {
                    messages.add(ConfigMessage.getLiftsignModifyPrivateRemoveDeny());
                }
                else {
                    
                    for(final UUID removal : removals) {
                        messages.add(ConfigMessage.getLiftsignModifyPrivateRemoveFalse().replace("%%player%%", uniqueIdToName.get(removal)));
                    }
                }
            }
        }
        
        if(!unknowns.isEmpty()) {
            if(!canModifyOwner && !canModifyAdmins && !canModifyMembers) {
                messages.add(ConfigMessage.getLiftsignModifyPrivateUnknownDeny());
            }
            else {
                
                for(final String unknown : unknowns) {
                    messages.add(ConfigMessage.getLiftsignModifyPrivateUnknownUnknown().replace("%%player%%", unknown));
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
    
    private List<String> getPlayerSuggestions(final List<String> completions, final ArrayList<String> splitCommand, final boolean endsWithSpace) {
        
        for(final Player player : getServer().getOnlinePlayers()) {
            completions.add(player.getName());
        }
        for(final String player : uniqueIdToName.values()) {
            if(!completions.contains(player)) {
                completions.add(player);
            }
        }
        
        if(splitCommand.isEmpty() || endsWithSpace) {
            return completions;
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
        
        for(int index = 0; index < completions.size(); index++) {
            completions.set(index, prefix + completions.get(index));
        }
        
        final Iterator<String> completionsIterator = completions.iterator();
        while(completionsIterator.hasNext()) {
            if(!completionsIterator.next().toLowerCase().startsWith(lastPlayer.toLowerCase())) {
                completionsIterator.remove();
            }
        }
        
        return completions;
    }
    
    private boolean savePlayerData(final PlayerDataEntry playerDataEntry) {
        
        final YamlConfiguration config = new YamlConfiguration();
        config.set("player_data", playerDataEntry);
        
        final File configFile = new File(playerDataFolder, playerDataEntry.getUniqueId().toString() + ".yml");
        final String configFilePath = configFile.getPath();
        
        try {
            if(!configFile.exists()) {
                try {
                    configFile.createNewFile();
                }
                catch(IOException e) {
                    this.logger.log(Level.WARNING, "Unable to create player data config file at " + configFilePath);
                    this.logger.log(Level.WARNING, "IOException thrown.", e);
                    return false;
                }
                catch(SecurityException e) {
                    this.logger.log(Level.WARNING, "Unable to create player data config file at " + configFilePath);
                    this.logger.log(Level.WARNING, "SecurityException thrown.", e);
                    return false;
                }
            }
        }
        catch(SecurityException e) {
            this.logger.log(Level.WARNING, "Unable to verify if player data config file exists at " + configFilePath);
            this.logger.log(Level.WARNING, "SecurityException thrown.", e);
            return false;
        }
        
        try {
            config.save(configFile);
        }
        catch(IOException e) {
            this.logger.log(Level.WARNING, "Unable to save player data config file at " + configFilePath);
            this.logger.log(Level.WARNING, "IOException thrown.", e);
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
            this.logger.log(Level.WARNING, "Unable to verify if private lift sign config file exists for load at " + configFilePath);
            this.logger.log(Level.WARNING, "SecurityException thrown.", e);
            return null;
        }
        
        final YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(configFile);
        }
        catch(FileNotFoundException e) {
            this.logger.log(Level.WARNING, "Unable to load private lift sign configuration file at " + configFilePath);
            this.logger.log(Level.WARNING, "Skipping private lift sign.");
            this.logger.log(Level.WARNING, "FileNotFoundException thrown.", e);
            return null;
        }
        catch(IOException e) {
            this.logger.log(Level.WARNING, "Unable to load private lift sign configuration file at " + configFilePath);
            this.logger.log(Level.WARNING, "Skipping private lift sign.");
            this.logger.log(Level.WARNING, "IOException thrown.", e);
            return null;
        }
        catch(InvalidConfigurationException e) {
            this.logger.log(Level.WARNING, "Unable to load private lift sign configuration file at " + configFilePath);
            this.logger.log(Level.WARNING, "Skipping private lift sign.");
            this.logger.log(Level.WARNING, "InvalidConfigurationException thrown.", e);
            return null;
        }
        catch(IllegalArgumentException e) {
            this.logger.log(Level.WARNING, "Unable to load private lift sign configuration file at " + configFilePath);
            this.logger.log(Level.WARNING, "Skipping private lift sign.");
            this.logger.log(Level.WARNING, "IllegalArgumentException thrown.", e);
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
                    this.logger.log(Level.WARNING, "Unable to create private lift sign config file at " + configFilePath);
                    this.logger.log(Level.WARNING, "IOException thrown.", e);
                    return false;
                }
                catch(SecurityException e) {
                    this.logger.log(Level.WARNING, "Unable to create private lift sign config file at " + configFilePath);
                    this.logger.log(Level.WARNING, "SecurityException thrown.", e);
                    return false;
                }
            }
        }
        catch(SecurityException e) {
            this.logger.log(Level.WARNING, "Unable to verify if private lift sign config file exists for save at " + configFilePath);
            this.logger.log(Level.WARNING, "SecurityException thrown.", e);
            return false;
        }
        
        try {
            config.save(configFile);
        }
        catch(IOException e) {
            this.logger.log(Level.WARNING, "Unable to save private lift sign config file at " + configFilePath);
            this.logger.log(Level.WARNING, "IOException thrown.", e);
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
            this.logger.log(Level.WARNING, "Unable to delete private lift sign config file at " + configFilePath);
            this.logger.log(Level.WARNING, "SecurityException thrown.", e);
            return false;
        }
        
        if(!deleted) {
            this.logger.log(Level.WARNING, "Private lift sign file was not deleted, no exception thrown.");
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
