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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bspfsystems.signlift.bukkit.command.SignLiftTabExecutor;
import org.bspfsystems.signlift.bukkit.exception.SignLiftException;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bspfsystems.playerdata.bukkit.PlayerDataEntry;
import org.bspfsystems.signlift.bukkit.config.ConfigData;
import org.bspfsystems.signlift.bukkit.config.ConfigMessage;
import org.bspfsystems.signlift.bukkit.liftsign.LiftSign;
import org.bspfsystems.signlift.bukkit.liftsign.PrivateLiftSign;
import org.bspfsystems.signlift.bukkit.listener.SignLiftEventHandler;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the main entrypoint for the {@link SignLiftPlugin} functionality.
 */
public final class SignLiftPlugin extends JavaPlugin {
    
    private Logger logger;
    
    private Server server;
    private BukkitScheduler scheduler;
    
    private File playerDataFolder;
    private ConcurrentHashMap<String, UUID> nameToUniqueId;
    private ConcurrentHashMap<UUID, String> uniqueIdToName;
    
    private File privateLiftSignFolder;
    private ConcurrentHashMap<Location, PrivateLiftSign> privateLiftSigns;
    
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
        
        // Server setup
        
        this.server = this.getServer();
        this.scheduler = this.server.getScheduler();
        
        // Configuration setup
    
        this.reloadConfig(this.server.getConsoleSender(), false);
        this.reloadMessages(this.server.getConsoleSender(), false);
        
        // Command handling setup
        
        final TabExecutor signLiftTabExecutor = new SignLiftTabExecutor(this);
        this.registerCommand("signlift", signLiftTabExecutor);
        this.registerCommand("sreload", signLiftTabExecutor);
        this.registerCommand("shelp", signLiftTabExecutor);
        this.registerCommand("sinfo", signLiftTabExecutor);
        this.registerCommand("smodify", signLiftTabExecutor);
        this.registerCommand("schangeowner", signLiftTabExecutor);
    
        // PlayerData loading
        
        final File dataFolder = this.getDataFolder();
        try {
            if (!dataFolder.exists()) {
                if (!dataFolder.mkdirs()) {
                    this.logger.log(Level.WARNING, "SignLift data directory not created at " + dataFolder.getPath());
                    this.logger.log(Level.WARNING, "SignLift functionality will be disabled.");
                    return;
                }
            } else if (!dataFolder.isDirectory()) {
                this.logger.log(Level.WARNING, "SignLift data directory is not a directory: " + dataFolder.getPath());
                this.logger.log(Level.WARNING, "SignLift functionality will be disabled.");
                return;
            }
        } catch (SecurityException e) {
            this.logger.log(Level.WARNING, "Unable to validate if the SignLift data directory has been properly created at " + dataFolder.getPath());
            this.logger.log(Level.WARNING, "SignLift functionality will be disabled.");
            this.logger.log(Level.WARNING, e.getClass().getSimpleName() + " thrown.", e);
            return;
        }
        
        this.playerDataFolder = new File(dataFolder, "PlayerData");
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
            
            try {
                final PlayerDataEntry playerDataEntry = PlayerDataEntry.deserialize(playerDataConfig);
                this.nameToUniqueId.put(playerDataEntry.getName().toLowerCase(), playerDataEntry.getUniqueId());
                this.uniqueIdToName.put(playerDataEntry.getUniqueId(), playerDataEntry.getName());
            } catch (IllegalArgumentException e) {
                this.logger.log(Level.WARNING, "Could not deserialize PlayerDataEntry configuration file at " + playerDataConfigFile.getPath());
                this.logger.log(Level.WARNING, "Skipping PlayerDataEntry.");
                this.logger.log(Level.WARNING, e.getClass().getSimpleName() + " thrown.", e);
            }
        }
        
        // PrivateLiftSign loading
    
        this.privateLiftSignFolder = new File(dataFolder, "PrivateLiftSigns");
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
        
        final File[] privateLiftSignConfigFiles = this.privateLiftSignFolder.listFiles();
        if (privateLiftSignConfigFiles == null) {
            this.logger.log(Level.WARNING, "SignLift PrivateLiftSign directory is not a directory, failed previous check.");
            this.logger.log(Level.WARNING, "SignLift functionality will be disabled.");
            return;
        }
        
        this.privateLiftSigns = new ConcurrentHashMap<Location, PrivateLiftSign>();
        
        for (final File privateLiftSignConfigFile : privateLiftSignConfigFiles) {
            
            final YamlConfiguration privateLiftSignConfig = new YamlConfiguration();
            try {
                privateLiftSignConfig.load(privateLiftSignConfigFile);
            } catch (IOException | IllegalArgumentException | InvalidConfigurationException e) {
                this.logger.log(Level.WARNING, "Unable to load PrivateLiftSign configuration file at " + privateLiftSignConfigFile.getPath());
                this.logger.log(Level.WARNING, "Skipping PrivateLiftSign.");
                this.logger.log(Level.WARNING, e.getClass().getSimpleName() + " thrown.", e);
                continue;
            }
            
            try {
                final PrivateLiftSign privateLiftSign = PrivateLiftSign.deserialize(privateLiftSignConfig);
                this.privateLiftSigns.put(privateLiftSign.getLocation(), privateLiftSign);
            } catch (SignLiftException e) {
                this.logger.log(Level.WARNING, "Could not deserialize PrivateLiftSign configuration file at " + privateLiftSignConfigFile.getPath());
                this.logger.log(Level.WARNING, "Skipping PrivateLiftSign.");
                this.logger.log(Level.WARNING, e.getClass().getSimpleName() + " thrown.", e);
            }
        }
        
        // Miscellaneous setup
        
        this.pendingInformation = new HashSet<UUID>();
        this.pendingModifications = new ConcurrentHashMap<UUID, ChangeData>();
        
        this.server.getPluginManager().registerEvents(new SignLiftEventHandler(this), this);
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
    // COMMAND ACCESS METHODS //
    ////////////////////////////
    
    /**
     * Reloads the configuration information for this plugin.
     *
     * @param player The {@link Player} triggering the configuration reload.
     */
    public void reloadConfig(@NotNull final Player player) {
        this.reloadConfig(player, true);
    }
    
    /**
     * Reloads the configuration information for this plugin.
     *
     * @param sender The {@link CommandSender} triggering the configuration
     *               reload.
     * @param command {@code true} if this was triggered by a command,
     *                {@code false} otherwise (during initial plugin loading).
     */
    private void reloadConfig(@NotNull final CommandSender sender, final boolean command) {
        ConfigData.reloadConfig(this, sender, command);
    }
    
    /**
     * Reloads the messages for this plugin.
     *
     * @param player The {@link Player} triggering the message reload.
     */
    public void reloadMessages(@NotNull final Player player) {
        this.reloadMessages(player, true);
    }
    
    /**
     * Reloads the messages for this plugin.
     *
     * @param sender The {@link CommandSender} triggering the message reload.
     * @param command {@code true} if this was triggered by a command,
     *                {@code false} otherwise (during initial plugin loading).
     */
    private void reloadMessages(@NotNull final CommandSender sender, final boolean command) {
        ConfigMessage.reloadMessages(this, sender, command);
    }
    
    ////////////////////////////
    // EVENT LISTENER METHODS //
    ////////////////////////////
    
    /**
     * Gets all {@link Command} suggestions to be removed from tab-completion
     * suggestions. Suggestions are removed on a permission basis, as well as
     * the suggestions in the form of {@code <plugin name>:<command>}.
     *
     * @param player The {@link Player} to check the permission of.
     * @return A {@link Collection} of {@link Command} names to remove.
     */
    @NotNull
    public Collection<String> onPlayerCommandSend(@NotNull final Player player) {
        
        final Collection<String> removals = new HashSet<String>();
        for (final String commandName : this.getDescription().getCommands().keySet()) {
            
            removals.add(this.getDescription().getName().toLowerCase() + ":" + commandName);
            final PluginCommand command = this.server.getPluginCommand(commandName);
            if (command != null && !command.testPermissionSilent(player)) {
                removals.add(commandName);
            }
        }
        
        return removals;
    }
    
    /**
     * Runs when a {@link Player} joins the {@link Server}, is used to get their
     * name and {@link UUID} for a {@link PlayerDataEntry}.
     *
     * @param player The {@link Player} that joined the {@link Server}.
     */
    public void onPlayerJoin(@NotNull final Player player) {
        
        final UUID uniqueId = player.getUniqueId();
        final String currentName = player.getName();
        
        if (!this.uniqueIdToName.containsKey(uniqueId)) {
            this.logger.log(Level.CONFIG, "================================================");
            this.logger.log(Level.CONFIG, "Player Login : NEW PLAYER");
            this.logger.log(Level.CONFIG, "------------------------------------------------");
            this.logger.log(Level.CONFIG, "Name : " + currentName);
            this.logger.log(Level.CONFIG, "UUID : " + uniqueId.toString());
            this.logger.log(Level.CONFIG, "================================================");
            
            this.nameToUniqueId.put(currentName.toLowerCase(), uniqueId);
            this.uniqueIdToName.put(uniqueId, currentName);
            this.savePlayerData(new PlayerDataEntry(player));
        } else if (!this.nameToUniqueId.containsKey(currentName.toLowerCase())) {
            this.logger.log(Level.CONFIG, "================================================");
            this.logger.log(Level.CONFIG, "Player Login: UPDATE NAME");
            this.logger.log(Level.CONFIG, "------------------------------------------------");
            this.logger.log(Level.CONFIG, "Old Name : " + uniqueIdToName.get(uniqueId));
            this.logger.log(Level.CONFIG, "New Name : " + currentName);
            this.logger.log(Level.CONFIG, "UUID     : " + uniqueId.toString());
            this.logger.log(Level.CONFIG, "================================================");
            
            this.nameToUniqueId.remove(this.uniqueIdToName.get(uniqueId));
            this.uniqueIdToName.remove(uniqueId);
            
            this.nameToUniqueId.put(currentName.toLowerCase(), uniqueId);
            this.uniqueIdToName.put(uniqueId, currentName);
            
            this.savePlayerData(new PlayerDataEntry(player));
        } else {
            this.logger.log(Level.CONFIG, "================================================");
            this.logger.log(Level.CONFIG, "Player Login: KNOWN PLAYER");
            this.logger.log(Level.CONFIG, "------------------------------------------------");
            this.logger.log(Level.CONFIG, "Name : " + currentName);
            this.logger.log(Level.CONFIG, "UUID : " + uniqueId.toString());
            this.logger.log(Level.CONFIG, "================================================");
        }
    }
    
    ///////////////////////////////////////
    // PLAYER DATA ACCESS PUBLIC METHODS //
    ///////////////////////////////////////
    
    /**
     * Gets the {@link UUID} for the given {@link Player} name, or {@code null}
     * if one does not exist or is otherwise unknown.
     *
     * @param name The name of the {@link Player}.
     * @return The {@link UUID} of the {@link Player} if it is known, otherwise
     *         {@code null}.
     */
    @Nullable
    public UUID getUniqueId(@NotNull final String name) {
        return this.nameToUniqueId.get(name.toLowerCase());
    }
    
    /**
     * Gets the name for the given {@link Player} {@link UUID}, or {@code null}
     * if one does not exist or is otherwise unknown.
     *
     * @param uniqueId The {@link UUID} of the {@link Player}.
     * @return The name of the {@link Player} if it is known, otherwise
     *         {@code null}.
     */
    @Nullable
    public String getName(@NotNull final UUID uniqueId) {
        return this.uniqueIdToName.get(uniqueId);
    }
    
    /**
     * Gets a {@link Collection} of all {@link Player} names known by this
     * {@link SignLiftPlugin}.
     *
     * @return An unmodifiable {@link Collection} of all {@link Player} names.
     */
    @NotNull
    public Collection<String> getAllNames() {
        return Collections.unmodifiableCollection(this.uniqueIdToName.values());
    }
    
    /////////////////////////////////////////
    // LIFTSIGN INFORMATION PUBLIC METHODS //
    /////////////////////////////////////////
    
    /**
     * Adds the given {@link Player} to the {@link Set} of all {@link Player}s
     * that have entered the {@code /signlift info} command, but have not yet
     * punched a {@link Block}.
     *
     * @param player The {@link Player} to mark as pending information.
     */
    public void addPendingInformation(@NotNull final Player player) {
        this.pendingInformation.add(player.getUniqueId());
    }
    
    /**
     * Checks to see if the given {@link Player} has entered the
     * {@code /signlift info} {@link Command}, but has not yet punched a
     * {@link Block}.
     *
     * @param player The {@link Player} to check for.
     * @return {@code true} if the {@link Player} has entered the
     *         {@code /signlift info} {@link Command}, but has not yet punched a
     *         {@link Block}, {@code false} otherwise.
     */
    public boolean isPendingInformation(@NotNull final Player player) {
        return this.pendingInformation.contains(player.getUniqueId());
    }
    
    /**
     * Displays information about the given {@link Block} (ideally a
     * {@link LiftSign}) at the given {@link Location} to the given
     * {@link Player}.
     * <p>
     * If the {@link Block} at the given {@link Location} is not a
     * {@link LiftSign}, an error message will be displayed. Also, if the
     * {@link Player} has not entered the {@link Command} to allow them to see
     * the information, no message will be displayed.
     *
     * @param location The {@link Location} to get the information of, if any.
     * @param player The {@link Player} to display the information to, if any.
     */
    public void getInformation(@NotNull final Location location, @NotNull final Player player) {
        
        if (!this.pendingInformation.remove(player.getUniqueId())) {
            this.logger.log(Level.WARNING, "Player attempting to get LiftSign information, Player is not pending information.");
            this.logger.log(Level.WARNING, "Somehow got past the isPendingInformation() check.");
            this.logger.log(Level.WARNING, "Player Name: " + player.getName());
            this.logger.log(Level.WARNING, "Player UUID: " + player.getUniqueId());
            return;
        }
        
        if (LiftSign.isPublicLiftSign(location)) {
            player.sendMessage(ConfigMessage.getLiftsignInfoPublic());
        } else if (LiftSign.isPrivateLiftSign(location)) {
            
            final PrivateLiftSign privateLiftSign = this.getPrivateLiftSign(location);
            if (privateLiftSign == null) {
                this.logger.log(Level.WARNING, "Player checking info for PrivateLiftSign, no LiftSign found.");
                this.logger.log(Level.WARNING, "Previous check showed that the Location had a PrivateLiftSign.");
                this.logger.log(Level.WARNING, "Player Name: " + player.getName());
                this.logger.log(Level.WARNING, "Player UUID: " + player.getUniqueId());
                this.logger.log(Level.WARNING, "World: " + (location.getWorld() == null ? "null" : location.getWorld().getName()));
                this.logger.log(Level.WARNING, "X: " + location.getBlockX());
                this.logger.log(Level.WARNING, "Y: " + location.getBlockY());
                this.logger.log(Level.WARNING, "Z: " + location.getBlockZ());
                player.sendMessage(ConfigMessage.getLiftsignInfoError());
                return;
            }
            
            final UUID owner = privateLiftSign.getOwner();
            final Set<UUID> admins = privateLiftSign.getAdmins();
            final Set<UUID> members = privateLiftSign.getMembers();
            
            player.sendMessage("§r§8================================§r");
            player.sendMessage(ConfigMessage.getLiftsignInfoPrivate());
            player.sendMessage("§r§8--------------------------------§r");
            player.sendMessage("§r§6Owner§r§f:§r");
            player.sendMessage("§r §f-§r §b" + this.getName(owner) + "§r §6[" + owner.toString() + "]§r");
            
            if (!admins.isEmpty()) {
                player.sendMessage("§r§8--------------------------------§r");
                player.sendMessage("§r§6Admin§r§f:§r");
                for (final UUID admin : admins) {
                    player.sendMessage("§r §f-§r §b" + this.getName(admin) + "§r §6[" + admin.toString() + "]§r");
                }
            }
            if (!members.isEmpty()) {
                player.sendMessage("§r§8--------------------------------§r");
                player.sendMessage("§r§6Members§r§f:§r");
                for (final UUID member : members) {
                    player.sendMessage("§r §f-§r §b" + this.getName(member) + "§r §6[" + member.toString() + "]§r");
                }
            }
            
            player.sendMessage("§r§8================================§r");
        } else {
            player.sendMessage(ConfigMessage.getLiftsignInfoDeny());
        }
    }
    
    ////////////////////////////////////
    // PRIVATE LIFTSIGN BASIC METHODS //
    ////////////////////////////////////
    
    /**
     * Gets the {@link PrivateLiftSign} at the given {@link Location}, if one
     * exists. Otherwise, returns {@code null}.
     *
     * @param location The {@link Location} to check for a
     *                 {@link PrivateLiftSign}.
     * @return The {@link PrivateLiftSign}, if one exists. {@code null}
     *         otherwise.
     */
    @Nullable
    public PrivateLiftSign getPrivateLiftSign(@NotNull final Location location) {
        return this.privateLiftSigns.get(location);
    }
    
    /**
     * Adds a new {@link PrivateLiftSign} to the known {@link PrivateLiftSign}s.
     * This is usually used when a {@link Player} creates a new
     * {@link PrivateLiftSign}.
     *
     * @param privateLiftSign The new {@link PrivateLiftSign}.
     * @param player The {@link Player} that created the
     *               {@link PrivateLiftSign}.
     */
    public void addPrivateLiftSign(@NotNull final PrivateLiftSign privateLiftSign, @NotNull final Player player) {
        this.saveLiftSign(privateLiftSign, player);
    }
    
    /**
     * Removes a {@link PrivateLiftSign} at the given {@link Location}, if one
     * exists.
     *
     * @param privateLiftSign The {@link PrivateLiftSign} to remove.
     * @param player The {@link Player} triggering the removal.
     */
    public void removePrivateLiftSign(@NotNull final PrivateLiftSign privateLiftSign, @NotNull final Player player) {
        this.deleteLiftSign(privateLiftSign, player);
    }
    
    /**
     * Activates the {@link PrivateLiftSign} at the given {@link Location} (if
     * one exists) for the specified {@link Player}.
     *
     * @param location The {@link Location} of the {@link PrivateLiftSign} to
     *                 activate (if one exists).
     * @param player The {@link Player} activating the {@link PrivateLiftSign}.
     * @see LiftSign#activate(Player, SignLiftPlugin)
     */
    public void usePrivateLiftSign(@NotNull final Location location, @NotNull final Player player) {
        
        final PrivateLiftSign privateLiftSign = this.getPrivateLiftSign(location);
        if (privateLiftSign == null) {
            this.logger.log(Level.WARNING, "PrivateLiftSign found twice at Location, cannot retrieve for activation.");
            this.logger.log(Level.WARNING, "Player Name: " + player.getName());
            this.logger.log(Level.WARNING, "Player UUID: " + player.getUniqueId());
            this.logger.log(Level.WARNING, "World: " + (location.getWorld() == null ? "null" : location.getWorld().getName()));
            this.logger.log(Level.WARNING, "X: " + location.getBlockX());
            this.logger.log(Level.WARNING, "Y: " + location.getBlockY());
            this.logger.log(Level.WARNING, "Z: " + location.getBlockZ());
            return;
        }
        
        privateLiftSign.activate(player, this);
    }
    
    ///////////////////////////////////////////
    // PRIVATE LIFTSIGN MODIFICATION METHODS //
    ///////////////////////////////////////////
    
    /**
     * Adds a pending modification item (as {@link ChangeData}) from the given
     * {@link Player}, so that the {@link Player} can click on the
     * {@link PrivateLiftSign} they wish to modify and apply the changes.
     *
     * @param player The {@link Player} preparing the {@link ChangeData}.
     * @param changeData The pending modifications for the
     *                   {@link PrivateLiftSign} of the {@link Player}'s
     *                   choosing.
     */
    public void addPendingModification(@NotNull final Player player, @NotNull final ChangeData changeData) {
        this.pendingModifications.put(player.getUniqueId(), changeData);
    }
    
    /**
     * Checks to see if the given {@link Player} has a modification pending for
     * a {@link PrivateLiftSign}.
     *
     * @param player The {@link Player} to check.
     * @return {@code true} if the given {@link Player} has a modification
     *         pending, {@code false} otherwise.
     */
    public boolean isPendingModification(@NotNull final Player player) {
        return this.pendingModifications.containsKey(player.getUniqueId());
    }
    
    /**
     * Removes the pending modification for the given {@link Player}, if any
     * exists.
     *
     * @param player The {@link Player} to remove the pending modification for.
     * @return {@code true} if there was a pending modification that was
     *         removed, {@code false} otherwise.
     */
    public boolean removePendingModification(@NotNull final Player player) {
        return this.pendingModifications.remove(player.getUniqueId()) != null;
    }
    
    /**
     * Modifies the {@link PrivateLiftSign} at the given {@link Location} (if
     * one exists).
     * <p>
     * If one does not exist, and error will be displayed and logged.
     *
     * @param location The {@link Location} of the {@link PrivateLiftSign}.
     * @param player The {@link Player} performing the modification.
     */
    public void modifyPrivateLiftSign(@NotNull final Location location, @NotNull final Player player) {
        
        if (!this.pendingModifications.containsKey(player.getUniqueId())) {
            this.logger.log(Level.WARNING, "Player attempting to modify PrivateLiftSign, Player is not pending modification.");
            this.logger.log(Level.WARNING, "Somehow got past the isPendingModification() check.");
            this.logger.log(Level.WARNING, "Player Name: " + player.getName());
            this.logger.log(Level.WARNING, "Player UUID: " + player.getUniqueId());
            return;
        }
        
        final ChangeData changeData = this.pendingModifications.remove(player.getUniqueId());
        final UUID newOwner = changeData.getOwner();
        final Set<UUID> admins = changeData.getAdmins();
        final Set<UUID> members = changeData.getMembers();
        final Set<UUID> removals = changeData.getRemovals();
        final List<String> unknowns = changeData.getUnknowns();
        
        final PrivateLiftSign privateLiftSign = this.getPrivateLiftSign(location);
        if (privateLiftSign == null) {
            if (LiftSign.isPublicLiftSign(location)) {
                player.sendMessage(ConfigMessage.getLiftsignModifyPublic());
            } else {
                player.sendMessage(ConfigMessage.getLiftsignModifyOther());
            }
            return;
        }
        
        final boolean canModifyOwner = privateLiftSign.canModifyOwner(player);
        final boolean canModifyAdmins = privateLiftSign.canModifyAdmins(player);
        final boolean canModifyMembers = privateLiftSign.canModifyMembers(player);
        
        if (newOwner != null) {
            if (canModifyOwner) {
                privateLiftSign.changeOwner(newOwner);
                String name = this.getName(newOwner);
                if (name == null) {
                    name = newOwner.toString();
                }
                
                player.sendMessage(ConfigMessage.getLiftsignModifyPrivateOwnerAllow().replace("%%player%%", name));
            } else {
                player.sendMessage(ConfigMessage.getLiftsignModifyPrivateOwnerDeny());
            }
        }
        
        final List<String> messages = new ArrayList<String>();
        
        if (!admins.isEmpty()) {
            if (canModifyAdmins) {
                
                for (final UUID admin : admins) {
                    String name = this.getName(admin);
                    if (name == null) {
                        name = admin.toString();
                    }
                    
                    if (privateLiftSign.addAdmin(admin)) {
                        if (privateLiftSign.isMember(admin)) {
                            privateLiftSign.removeMember(admin);
                            messages.add(ConfigMessage.getLiftsignModifyPrivateAdminChange().replace("%%player%%", name));
                        } else {
                            messages.add(ConfigMessage.getLiftsignModifyPrivateAdminTrue().replace("%%player%%", name));
                        }
                    } else {
                        messages.add(ConfigMessage.getLiftsignModifyPrivateAdminFalse().replace("%%player%%", name));
                    }
                }
            } else {
                messages.add(ConfigMessage.getLiftsignModifyPrivateAdminDeny());
            }
        }
        
        if (!members.isEmpty()) {
            if (canModifyMembers) {
                
                for (final UUID member : members) {
                    String name = this.getName(member);
                    if (name == null) {
                        name = member.toString();
                    }
                    
                    if (privateLiftSign.addMember(member)) {
                        if (privateLiftSign.isAdmin(member)) {
                            if (canModifyAdmins) {
                                privateLiftSign.removeAdmin(member);
                                messages.add(ConfigMessage.getLiftsignModifyPrivateMemberChange().replace("%%player%%", name));
                            } else {
                                privateLiftSign.removeMember(member);
                                messages.add(ConfigMessage.getLiftsignModifyPrivateAdminDeny());
                            }
                        } else {
                            messages.add(ConfigMessage.getLiftsignModifyPrivateMemberTrue().replace("%%player%%", name));
                        }
                    } else {
                        messages.add(ConfigMessage.getLiftsignModifyPrivateMemberFalse().replace("%%player%%", name));
                    }
                }
            } else {
                messages.add(ConfigMessage.getLiftsignModifyPrivateMemberDeny());
            }
        }
        
        if (!removals.isEmpty()) {
            
            final Set<UUID> removeAdmins = new HashSet<UUID>();
            final Set<UUID> removeMembers = new HashSet<UUID>();
            
            for (final UUID removal : removals) {
                if (privateLiftSign.isAdmin(removal)) {
                    removeAdmins.add(removal);
                } else if (privateLiftSign.isMember(removal)) {
                    removeMembers.add(removal);
                }
            }
            
            removals.removeAll(removeAdmins);
            removals.removeAll(removeMembers);
            
            if (!removeAdmins.isEmpty()) {
                if (canModifyAdmins) {
                    
                    for (final UUID admin : removeAdmins) {
                        String name = this.getName(admin);
                        if (name == null) {
                            name = admin.toString();
                        }
                        
                        if (privateLiftSign.removeAdmin(admin)) {
                            messages.add(ConfigMessage.getLiftsignModifyPrivateRemoveTrue().replace("%%player%%", name));
                        } else {
                            messages.add(ConfigMessage.getLiftsignModifyPrivateRemoveFalse().replace("%%player%%", name));
                        }
                    }
                } else {
                    messages.add(ConfigMessage.getLiftsignModifyPrivateRemoveDeny());
                }
            }
            
            if (!removeMembers.isEmpty()) {
                if (canModifyMembers) {
                    
                    for (final UUID member : removeMembers) {
                        String name = this.getName(member);
                        if (name == null) {
                            name = member.toString();
                        }
                        
                        if (privateLiftSign.removeMember(member)) {
                            messages.add(ConfigMessage.getLiftsignModifyPrivateRemoveTrue().replace("%%player%%", name));
                        } else {
                            messages.add(ConfigMessage.getLiftsignModifyPrivateRemoveFalse().replace("%%player%%", name));
                        }
                    }
                } else {
                    messages.add(ConfigMessage.getLiftsignModifyPrivateRemoveDeny());
                }
            }
            
            if (!removals.isEmpty()) {
                if (!canModifyAdmins && !canModifyMembers) {
                    messages.add(ConfigMessage.getLiftsignModifyPrivateRemoveDeny());
                } else {
                    for (final UUID removal : removals) {
                        String name = this.getName(removal);
                        if (name == null) {
                            name = removal.toString();
                        }
                        
                        messages.add(ConfigMessage.getLiftsignModifyPrivateRemoveFalse().replace("%%player%%", name));
                    }
                }
            }
        }
        
        if (!unknowns.isEmpty()) {
            if(!canModifyOwner && !canModifyAdmins && !canModifyMembers) {
                messages.add(ConfigMessage.getLiftsignModifyPrivateUnknownDeny());
            } else {
                for (final String unknown : unknowns) {
                    messages.add(ConfigMessage.getLiftsignModifyPrivateUnknownUnknown().replace("%%player%%", unknown));
                }
            }
        }
        
        for (final String message : messages) {
            player.sendMessage(message);
        }
        
        this.saveLiftSign(privateLiftSign, player);
    }
    
    ////////////////////////////
    // PRIVATE HELPER METHODS //
    ////////////////////////////
    
    /**
     * Saves the given {@link PlayerDataEntry} to a file as a
     * {@link YamlConfiguration}.
     *
     * @param playerDataEntry The {@link PlayerDataEntry} to save.
     */
    private void savePlayerData(@NotNull final PlayerDataEntry playerDataEntry) {
    
        this.scheduler.runTaskAsynchronously(this, () -> {
            
            final File configFile = new File(this.playerDataFolder, playerDataEntry.getUniqueId().toString() + ".yml");
            try {
                if (!configFile.exists()) {
                    if (!configFile.createNewFile()) {
                        this.logger.log(Level.WARNING, "PlayerDataEntry configuration file not created at " + configFile.getPath());
                        this.logger.log(Level.WARNING, "Unable to save PlayerDataEntry.");
                        this.logger.log(Level.WARNING, "Name : " + playerDataEntry.getName());
                        this.logger.log(Level.WARNING, "UUID : " + playerDataEntry.getUniqueId().toString());
                    }
                } else if (!configFile.isFile()) {
                    this.logger.log(Level.WARNING, "PlayerDataEntry configuration file is not a file: " + configFile.getPath());
                    this.logger.log(Level.WARNING, "Unable to save PlayerDataEntry.");
                    this.logger.log(Level.WARNING, "Name : " + playerDataEntry.getName());
                    this.logger.log(Level.WARNING, "UUID : " + playerDataEntry.getUniqueId().toString());
                }
            } catch (SecurityException | IOException e) {
                this.logger.log(Level.WARNING, "Unable to verify if PlayerDataEntry configuration file exists/is a file at " + configFile.getPath());
                this.logger.log(Level.WARNING, "Unable to save PlayerDataEntry.");
                this.logger.log(Level.WARNING, "Name : " + playerDataEntry.getName());
                this.logger.log(Level.WARNING, "UUID : " + playerDataEntry.getUniqueId().toString());
                this.logger.log(Level.WARNING, e.getClass().getSimpleName() + " thrown.", e);
            }
            
            try {
                playerDataEntry.serializeForSave().save(configFile);
            } catch (IOException e) {
                this.logger.log(Level.WARNING, "Unable to save PlayerDataEntry configuration file at " + configFile.getPath());
                this.logger.log(Level.WARNING, "Unable to save PlayerDataEntry.");
                this.logger.log(Level.WARNING, "Name : " + playerDataEntry.getName());
                this.logger.log(Level.WARNING, "UUID : " + playerDataEntry.getUniqueId().toString());
                this.logger.log(Level.WARNING, e.getClass().getSimpleName() + " thrown.", e);
            }
        });
    }
    
    /**
     * Saves an updated {@link PrivateLiftSign}, usually used after creation or
     * after a modification.
     *
     * @param privateLiftSign The {@link PrivateLiftSign} to save.
     * @param player The {@link Player} triggering the save.
     */
    private void saveLiftSign(@NotNull final PrivateLiftSign privateLiftSign, @NotNull final Player player) {
        
        this.privateLiftSigns.put(privateLiftSign.getLocation(), privateLiftSign);
        this.scheduler.runTaskAsynchronously(this, () -> {
        
            final File configFile = new File(this.privateLiftSignFolder, this.getConfigFileName(privateLiftSign));
            try {
                if (!configFile.exists()) {
                    if (!configFile.createNewFile()) {
                        this.logger.log(Level.WARNING, "PrivateLiftSign configuration file not created at " + configFile.getPath());
                        this.logger.log(Level.WARNING, "Unable to save PrivateLiftSign.");
                        this.logger.log(Level.WARNING, "World: " + (privateLiftSign.getLocation().getWorld() == null ? "null" : privateLiftSign.getLocation().getWorld().getName()));
                        this.logger.log(Level.WARNING, "X: " + privateLiftSign.getLocation().getBlockX());
                        this.logger.log(Level.WARNING, "Y: " + privateLiftSign.getLocation().getBlockY());
                        this.logger.log(Level.WARNING, "Z: " + privateLiftSign.getLocation().getBlockZ());
                        player.sendMessage(ConfigMessage.getLiftsignFileErrorSave());
                    }
                } else if (!configFile.isFile()) {
                    this.logger.log(Level.WARNING, "PrivateLiftSign configuration file is not a file: " + configFile.getPath());
                    this.logger.log(Level.WARNING, "Unable to save PrivateLiftSign.");
                    this.logger.log(Level.WARNING, "World: " + (privateLiftSign.getLocation().getWorld() == null ? "null" : privateLiftSign.getLocation().getWorld().getName()));
                    this.logger.log(Level.WARNING, "X: " + privateLiftSign.getLocation().getBlockX());
                    this.logger.log(Level.WARNING, "Y: " + privateLiftSign.getLocation().getBlockY());
                    this.logger.log(Level.WARNING, "Z: " + privateLiftSign.getLocation().getBlockZ());
                    player.sendMessage(ConfigMessage.getLiftsignFileErrorSave());
                }
            } catch (SecurityException | IOException e) {
                this.logger.log(Level.WARNING, "Unable to verify if PrivateLiftSign configuration file exists/is a file at " + configFile.getPath());
                this.logger.log(Level.WARNING, "Unable to save PrivateLiftSign.");
                this.logger.log(Level.WARNING, "World: " + (privateLiftSign.getLocation().getWorld() == null ? "null" : privateLiftSign.getLocation().getWorld().getName()));
                this.logger.log(Level.WARNING, "X: " + privateLiftSign.getLocation().getBlockX());
                this.logger.log(Level.WARNING, "Y: " + privateLiftSign.getLocation().getBlockY());
                this.logger.log(Level.WARNING, "Z: " + privateLiftSign.getLocation().getBlockZ());
                this.logger.log(Level.WARNING, e.getClass().getSimpleName() + " thrown.", e);
                player.sendMessage(ConfigMessage.getLiftsignFileErrorSave());
            }
        
            try {
                privateLiftSign.serializeForSave().save(configFile);
            } catch (IOException e) {
                this.logger.log(Level.WARNING, "Unable to save PrivateLiftSign configuration file at " + configFile.getPath());
                this.logger.log(Level.WARNING, "Unable to save PrivateLiftSign.");
                this.logger.log(Level.WARNING, "World: " + (privateLiftSign.getLocation().getWorld() == null ? "null" : privateLiftSign.getLocation().getWorld().getName()));
                this.logger.log(Level.WARNING, "X: " + privateLiftSign.getLocation().getBlockX());
                this.logger.log(Level.WARNING, "Y: " + privateLiftSign.getLocation().getBlockY());
                this.logger.log(Level.WARNING, "Z: " + privateLiftSign.getLocation().getBlockZ());
                this.logger.log(Level.WARNING, e.getClass().getSimpleName() + " thrown.", e);
                player.sendMessage(ConfigMessage.getLiftsignFileErrorSave());
            }
        });
    }
    
    /**
     * Deletes the configuration file for the {@link PrivateLiftSign} (usually
     * used when a {@link PrivateLiftSign} is broken or otherwise removed).
     *
     * @param privateLiftSign The {@link PrivateLiftSign} to remove.
     * @param player The {@link Player} triggering the removal.
     */
    private void deleteLiftSign(@NotNull final PrivateLiftSign privateLiftSign, @NotNull final Player player) {
        
        this.privateLiftSigns.remove(privateLiftSign.getLocation());
        this.scheduler.runTaskAsynchronously(this, () -> {
    
            final File configFile = new File(this.privateLiftSignFolder, this.getConfigFileName(privateLiftSign));
            try {
                if (!configFile.delete()) {
                    this.logger.log(Level.WARNING, "PrivateLiftSign configuration file was not deleted, no Exception thrown.");
                    this.logger.log(Level.WARNING, "World: " + (privateLiftSign.getLocation().getWorld() == null ? "null" : privateLiftSign.getLocation().getWorld().getName()));
                    this.logger.log(Level.WARNING, "X: " + privateLiftSign.getLocation().getBlockX());
                    this.logger.log(Level.WARNING, "Y: " + privateLiftSign.getLocation().getBlockY());
                    this.logger.log(Level.WARNING, "Z: " + privateLiftSign.getLocation().getBlockZ());
                    player.sendMessage(ConfigMessage.getLiftsignFileErrorDelete());
                }
            } catch (SecurityException e) {
                this.logger.log(Level.WARNING, "Unable to delete PrivateLiftSign configuration file at " + configFile.getPath());
                this.logger.log(Level.WARNING, "World: " + (privateLiftSign.getLocation().getWorld() == null ? "null" : privateLiftSign.getLocation().getWorld().getName()));
                this.logger.log(Level.WARNING, "X: " + privateLiftSign.getLocation().getBlockX());
                this.logger.log(Level.WARNING, "Y: " + privateLiftSign.getLocation().getBlockY());
                this.logger.log(Level.WARNING, "Z: " + privateLiftSign.getLocation().getBlockZ());
                this.logger.log(Level.WARNING, e.getClass().getSimpleName() + " thrown.", e);
                player.sendMessage(ConfigMessage.getLiftsignFileErrorDelete());
            }
        });
    }
    
    /**
     * Gets the name of the configuration file that is used for
     * {@link PrivateLiftSign}s, based on the {@link PrivateLiftSign}'s
     * {@link Location}.
     *
     * @param privateLiftSign The {@link PrivateLiftSign} to get the
     *                        configuration file name for.
     * @return The name of the configuration file.
     */
    @NotNull
    private String getConfigFileName(@NotNull final PrivateLiftSign privateLiftSign) {
        
        final Location location = privateLiftSign.getLocation();
        final String world = location.getWorld() == null ? "null" : location.getWorld().getName();
        final String x = String.valueOf(location.getBlockX()).replace("-", "n");
        final String y = String.valueOf(location.getBlockY()).replace("-", "n");
        final String z = String.valueOf(location.getBlockZ()).replace("-", "n");
        
        return world + "-" + x + "-" + y + "-" + z + ".yml";
    }
}
