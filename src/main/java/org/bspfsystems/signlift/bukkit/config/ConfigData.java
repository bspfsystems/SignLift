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

package org.bspfsystems.signlift.bukkit.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;
import org.bspfsystems.signlift.bukkit.SignLiftPlugin;
import org.bspfsystems.signlift.bukkit.liftsign.LiftSign;

/**
 * Represents the configuration management for all in-game {@link LiftSign} data
 * related to {@link SignLiftPlugin} operations.
 */
public final class ConfigData {
    
    private static final String KEY_CHECK_DESTINATION = "check_destination";
    private static final String KEY_RELATIVE_TELEPORTING = "relative_teleporting";
    private static final String KEY_DIRECTION_NONE = "direction_none";
    private static final String KEY_DIRECTION_UP = "direction_up";
    private static final String KEY_DIRECTION_DOWN = "direction_down";
    private static final String KEY_PUBLIC_START = "public_start";
    private static final String KEY_PUBLIC_END = "public_end";
    private static final String KEY_PRIVATE_START = "private_start";
    private static final String KEY_PRIVATE_END = "private_end";
    
    private static final boolean DEFAULT_CHECK_DESTINATION = true;
    private static final boolean DEFAULT_RELATIVE_TELEPORTING = true;
    private static final String DEFAULT_DIRECTION_NONE = "NONE";
    private static final String DEFAULT_DIRECTION_UP = "UP";
    private static final String DEFAULT_DIRECTION_DOWN = "DOWN";
    private static final String DEFAULT_PUBLIC_START = "[";
    private static final String DEFAULT_PUBLIC_END = "]";
    private static final String DEFAULT_PRIVATE_START = "{";
    private static final String DEFAULT_PRIVATE_END = "}";
    
    private static boolean checkDestination;
    private static boolean relativeTeleporting;
    private static String directionNone;
    private static String directionUp;
    private static String directionDown;
    private static String publicStart;
    private static String publicEnd;
    private static String privateStart;
    private static String privateEnd;
    
    /**
     * Prevent instantiation.
     */
    private ConfigData() {
        // No instances.
    }
    
    /**
     * Loads the various config data items.
     *
     * @param signLiftPlugin The {@link SignLiftPlugin}.
     * @param sender The {@link CommandSender} triggering the reload.
     * @param command {@code true} if this was triggered by a command,
     *                {@code false} otherwise (during initial plugin loading).
     */
    public static void reloadConfig(@NotNull final SignLiftPlugin signLiftPlugin, @NotNull final CommandSender sender, final boolean command) {
        if (command) {
            signLiftPlugin.getServer().getScheduler().runTaskAsynchronously(signLiftPlugin, () -> ConfigData.performReload(signLiftPlugin, sender, command));
        } else {
            ConfigData.performReload(signLiftPlugin, sender, command);
        }
    }
    
    /**
     * Performs the actual logic of reloading the configuration.
     *
     * @param signLiftPlugin The {@link SignLiftPlugin}.
     * @param sender The {@link CommandSender} triggering the reload.
     * @param command {@code true} if this was triggered by a command,
     *                {@code false} otherwise (during initial plugin loading).
     */
    private static void performReload(@NotNull final SignLiftPlugin signLiftPlugin, @NotNull final CommandSender sender, final boolean command) {
    
    
        final Logger logger = signLiftPlugin.getLogger();
        final BukkitScheduler scheduler = signLiftPlugin.getServer().getScheduler();
    
        File configFile = new File(signLiftPlugin.getDataFolder(), "signlift.yml");
        try {
        
            if (!configFile.exists() || !configFile.isFile()) {
                configFile = new File(signLiftPlugin.getDataFolder(), "config.yml");
            }
        
            if (configFile.exists()) {
                if (!configFile.isFile()) {
                    logger.log(Level.WARNING, "SignLift configuration file is not a file: " + configFile.getPath());
                    logger.log(Level.WARNING, "SignLift will use the default configuration.");
                    if (command) {
                        sender.sendMessage("§r§cAn error has occurred while reloading the SignLift configuration. Please try again. If the error persists, please contact a server administrator.§r");
                        scheduler.runTask(signLiftPlugin, ConfigData::setDefaults);
                    } else {
                        ConfigData.setDefaults();
                    }
                    
                    return;
                }
            } else {
                if (!configFile.createNewFile()) {
                    logger.log(Level.WARNING, "SignLift configuration file not created at " + configFile.getPath());
                    logger.log(Level.WARNING, "SignLift will use the default configuration.");
                    if (command) {
                        sender.sendMessage("§r§cAn error has occurred while reloading the SignLift configuration. Please try again. If the error persists, please contact a server administrator.§r");
                        scheduler.runTask(signLiftPlugin, ConfigData::setDefaults);
                    } else {
                        ConfigData.setDefaults();
                    }
                    return;
                }
            
                final InputStream defaultConfig = signLiftPlugin.getResource(configFile.getName());
                final FileOutputStream outputStream = new FileOutputStream(configFile);
                final byte[] buffer = new byte[4096];
                int bytesRead;
            
                if (defaultConfig == null) {
                    logger.log(Level.WARNING, "SignLift default configuration file not found. Possible compilation/build issue with the plugin.");
                    logger.log(Level.WARNING, "SignLift will use the default configuration.");
                    if (command) {
                        sender.sendMessage("§r§cAn error has occurred while reloading the SignLift configuration. Please try again. If the error persists, please contact a server administrator.§r");
                        scheduler.runTask(signLiftPlugin, ConfigData::setDefaults);
                    } else {
                        ConfigData.setDefaults();
                    }
                    return;
                }
            
                while ((bytesRead = defaultConfig.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            
                outputStream.flush();
                defaultConfig.close();
            
                if (command) {
                    sender.sendMessage("§r§cThe SignLift configuration file did not exist; a copy of the default has been made and placed in the correct location.§r");
                    sender.sendMessage("§r§cPlease update the configuration as required for the installation, and then run§r §b/signlift reload config§r§c.§r");
                }
            
                logger.log(Level.WARNING, "SignLift configuration file did not exist at " + configFile.getPath());
                logger.log(Level.WARNING, "SignLift will use the default configuration.");
                logger.log(Level.WARNING, "Please update the configuration as required for your installation, and then run \"/signlift reload config\".");
            }
        } catch (SecurityException | IOException e) {
            logger.log(Level.WARNING, "Unable to load the SignLift configuration file.");
            logger.log(Level.WARNING, "SignLift will use the default configuration.");
            logger.log(Level.WARNING, e.getClass().getSimpleName() + " thrown.", e);
            if (command) {
                sender.sendMessage("§r§cAn error has occurred while reloading the SignLift configuration. Please try again. If the error persists, please contact a server administrator.§r");
                scheduler.runTask(signLiftPlugin, ConfigData::setDefaults);
            } else {
                ConfigData.setDefaults();
            }
            return;
        }
    
        final YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(configFile);
        } catch (IOException | IllegalArgumentException | InvalidConfigurationException e) {
            logger.log(Level.WARNING, "Unable to load the SignLift configuration.");
            logger.log(Level.WARNING, "SignLift will use the default configuration.");
            logger.log(Level.WARNING, e.getClass().getSimpleName() + " thrown.", e);
            if (command) {
                sender.sendMessage("§r§cAn error has occurred while reloading the SignLift configuration. Please try again. If the error persists, please contact a server administrator.§r");
                scheduler.runTask(signLiftPlugin, ConfigData::setDefaults);
            } else {
                ConfigData.setDefaults();
            }
            return;
        }
        
        if (command) {
            scheduler.runTask(signLiftPlugin, () -> {
                ConfigData.setValues(config);
                sender.sendMessage("§r§aThe SignLift configuration has been reloaded. Please verify that all LiftSigns are working as intended.§r");
            });
        } else {
            ConfigData.setValues(config);
        }
    }
    
    /**
     * Performs the logic for setting the values from the configuration.
     * 
     * @param config The {@link YamlConfiguration} to set the values from.
     */
    private static void setValues(@NotNull final YamlConfiguration config) {
        ConfigData.checkDestination = config.getBoolean(ConfigData.KEY_CHECK_DESTINATION, ConfigData.DEFAULT_CHECK_DESTINATION);
        ConfigData.relativeTeleporting = config.getBoolean(ConfigData.KEY_RELATIVE_TELEPORTING, ConfigData.DEFAULT_RELATIVE_TELEPORTING);
        ConfigData.directionNone = config.getString(ConfigData.KEY_DIRECTION_NONE, ConfigData.DEFAULT_DIRECTION_NONE);
        ConfigData.directionUp = config.getString(ConfigData.KEY_DIRECTION_UP, ConfigData.DEFAULT_DIRECTION_UP);
        ConfigData.directionDown = config.getString(ConfigData.KEY_DIRECTION_DOWN, ConfigData.DEFAULT_DIRECTION_DOWN);
        ConfigData.publicStart = config.getString(ConfigData.KEY_PUBLIC_START, ConfigData.DEFAULT_PUBLIC_START);
        ConfigData.publicEnd = config.getString(ConfigData.KEY_PUBLIC_END, ConfigData.DEFAULT_PUBLIC_END);
        ConfigData.privateStart = config.getString(ConfigData.KEY_PRIVATE_START, ConfigData.DEFAULT_PRIVATE_START);
        ConfigData.privateEnd = config.getString(ConfigData.KEY_PRIVATE_END, ConfigData.DEFAULT_PRIVATE_END);
    }
    
    /**
     * Sets the configuration items to their default values.
     */
    private static void setDefaults() {
        ConfigData.checkDestination = ConfigData.DEFAULT_CHECK_DESTINATION;
        ConfigData.relativeTeleporting = ConfigData.DEFAULT_RELATIVE_TELEPORTING;
        ConfigData.directionNone = ConfigData.DEFAULT_DIRECTION_NONE;
        ConfigData.directionUp = ConfigData.DEFAULT_DIRECTION_UP;
        ConfigData.directionDown = ConfigData.DEFAULT_DIRECTION_DOWN;
        ConfigData.publicStart = ConfigData.DEFAULT_PUBLIC_START;
        ConfigData.publicEnd = ConfigData.DEFAULT_PUBLIC_END;
        ConfigData.privateStart = ConfigData.DEFAULT_PRIVATE_START;
        ConfigData.privateEnd = ConfigData.DEFAULT_PRIVATE_END;
    }
    
    public static boolean getCheckDestination() {
        return ConfigData.checkDestination;
    }
    
    public static boolean getRelativeTeleporting() {
        return ConfigData.relativeTeleporting;
    }
    
    @NotNull
    public static String getDirectionNone() {
        return ConfigData.directionNone;
    }
    
    @NotNull
    public static String getDirectionUp() {
        return ConfigData.directionUp;
    }
    
    @NotNull
    public static String getDirectionDown() {
        return ConfigData.directionDown;
    }
    
    @NotNull
    public static String getPublicStart() {
        return ConfigData.publicStart;
    }
    
    @NotNull
    public static String getPublicEnd() {
        return ConfigData.publicEnd;
    }
    
    @NotNull
    public static String getPrivateStart() {
        return ConfigData.privateStart;
    }
    
    @NotNull
    public static String getPrivateEnd() {
        return ConfigData.privateEnd;
    }
}
