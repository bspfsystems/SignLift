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

/**
 * Represents the configuration management for all in-game messages related to
 * {@link SignLiftPlugin} operations.
 */
public final class ConfigMessage {
    
    private static final String KEY_COMMAND_INFO = "command.info";
    private static final String KEY_COMMAND_MODIFY = "command.modify";
    private static final String KEY_COMMAND_DENY = "command.deny";
    private static final String KEY_LIFTSIGN_INFO_PUBLIC = "liftsign.info.public";
    private static final String KEY_LIFTSIGN_INFO_PRIVATE = "liftsign.info.private";
    private static final String KEY_LIFTSIGN_INFO_ERROR = "liftsign.info.error";
    private static final String KEY_LIFTSIGN_INFO_DENY = "liftsign.info.deny";
    private static final String KEY_LIFTSIGN_BUILD_DENY_PUBLIC = "liftsign.build.deny.public";
    private static final String KEY_LIFTSIGN_BUILD_DENY_PRIVATE = "liftsign.build.deny.private";
    private static final String KEY_LIFTSIGN_CREATE_PUBLIC_ALLOW = "liftsign.create.public.allow";
    private static final String KEY_LIFTSIGN_CREATE_PUBLIC_DENY = "liftsign.create.public.deny";
    private static final String KEY_LIFTSIGN_CREATE_PUBLIC_ERROR = "liftsign.create.public.error";
    private static final String KEY_LIFTSIGN_CREATE_PRIVATE_ALLOW = "liftsign.create.private.allow";
    private static final String KEY_LIFTSIGN_CREATE_PRIVATE_DENY = "liftsign.create.private.deny";
    private static final String KEY_LIFTSIGN_CREATE_PRIVATE_ERROR = "liftsign.create.private.error";
    private static final String KEY_LIFTSIGN_MODIFY_PUBLIC = "liftsign.modify.public";
    private static final String KEY_LIFTSIGN_MODIFY_PRIVATE_OWNER_ALLOW = "liftsign.modify.private.owner.allow";
    private static final String KEY_LIFTSIGN_MODIFY_PRIVATE_OWNER_DENY = "liftsign.modify.private.owner.deny";
    private static final String KEY_LIFTSIGN_MODIFY_PRIVATE_ADMIN_TRUE = "liftsign.modify.private.admin.true";
    private static final String KEY_LIFTSIGN_MODIFY_PRIVATE_ADMIN_CHANGE = "liftsign.modify.private.admin.change";
    private static final String KEY_LIFTSIGN_MODIFY_PRIVATE_ADMIN_FALSE = "liftsign.modify.private.admin.false";
    private static final String KEY_LIFTSIGN_MODIFY_PRIVATE_ADMIN_DENY = "liftsign.modify.private.admin.deny";
    private static final String KEY_LIFTSIGN_MODIFY_PRIVATE_MEMBER_TRUE = "liftsign.modify.private.member.true";
    private static final String KEY_LIFTSIGN_MODIFY_PRIVATE_MEMBER_CHANGE = "liftsign.modify.private.member.change";
    private static final String KEY_LIFTSIGN_MODIFY_PRIVATE_MEMBER_FALSE = "liftsign.modify.private.member.false";
    private static final String KEY_LIFTSIGN_MODIFY_PRIVATE_MEMBER_DENY = "liftsign.modify.private.member.deny";
    private static final String KEY_LIFTSIGN_MODIFY_PRIVATE_REMOVE_TRUE = "liftsign.modify.private.remove.true";
    private static final String KEY_LIFTSIGN_MODIFY_PRIVATE_REMOVE_FALSE = "liftsign.modify.private.remove.false";
    private static final String KEY_LIFTSIGN_MODIFY_PRIVATE_REMOVE_DENY = "liftsign.modify.private.remove.deny";
    private static final String KEY_LIFTSIGN_MODIFY_PRIVATE_UNKNOWN_UNKNOWN = "liftsign.modify.private.unknown.unknown";
    private static final String KEY_LIFTSIGN_MODIFY_PRIVATE_UNKNOWN_DENY = "liftsign.modify.private.unknown.deny";
    private static final String KEY_LIFTSIGN_MODIFY_OTHER = "liftsign.modify.other";
    private static final String KEY_LIFTSIGN_REMOVE_PUBLIC_ALLOW = "liftsign.remove.public.allow";
    private static final String KEY_LIFTSIGN_REMOVE_PUBLIC_DENY = "liftsign.remove.public.deny";
    private static final String KEY_LIFTSIGN_REMOVE_PRIVATE_ALLOW = "liftsign.remove.private.allow";
    private static final String KEY_LIFTSIGN_REMOVE_PRIVATE_DENY = "liftsign.remove.private.deny";
    private static final String KEY_LIFTSIGN_REMOVE_ATTACHED_ALLOW = "liftsign.remove.attached.allow";
    private static final String KEY_LIFTSIGN_REMOVE_ATTACHED_DENY = "liftsign.remove.attached.deny";
    private static final String KEY_LIFTSIGN_USE_NONE_DEFAULT = "liftsign.use.none.default";
    private static final String KEY_LIFTSIGN_USE_UP_DEFAULT = "liftsign.use.up.default";
    private static final String KEY_LIFTSIGN_USE_UP_CUSTOM = "liftsign.use.up.custom";
    private static final String KEY_LIFTSIGN_USE_DOWN_DEFAULT = "liftsign.use.down.default";
    private static final String KEY_LIFTSIGN_USE_DOWN_CUSTOM = "liftsign.use.down.custom";
    private static final String KEY_LIFTSIGN_USE_DENY_PUBLIC = "liftsign.use.deny.public";
    private static final String KEY_LIFTSIGN_USE_DENY_PRIVATE = "liftsign.use.deny.private";
    private static final String KEY_LIFTSIGN_USE_DISCONNECTED_PUBLIC = "liftsign.use.disconnected.public";
    private static final String KEY_LIFTSIGN_USE_DISCONNECTED_PRIVATE = "liftsign.use.disconnected.private";
    private static final String KEY_LIFTSIGN_USE_BLOCKED_PUBLIC = "liftsign.use.blocked.public";
    private static final String KEY_LIFTSIGN_USE_BLOCKED_PRIVATE = "liftsign.use.blocked.private";
    private static final String KEY_LIFTSIGN_FILE_ERROR_SAVE = "liftsign.file.error.save";
    private static final String KEY_LIFTSIGN_FILE_ERROR_DELETE = "liftsign.file.error.delete";
    
    private static final String DEFAULT_COMMAND_INFO = "§r§bRight-click on a lift sign to see information about it.§r";
    private static final String DEFAULT_COMMAND_MODIFY = "§r§bLeft-click on a private lift sign to make your changes.§r";
    private static final String DEFAULT_COMMAND_DENY = "§r§cOnly players may use this command.§r";
    private static final String DEFAULT_LIFTSIGN_INFO_PUBLIC = "§r§aThis is a public lift sign.§r";
    private static final String DEFAULT_LIFTSIGN_INFO_PRIVATE = "§r§aThis is a private lift sign.§r";
    private static final String DEFAULT_LIFTSIGN_INFO_ERROR = "§r§cInternal error, please try again. If the issue persists, please contact a server administrator.§r";
    private static final String DEFAULT_LIFTSIGN_INFO_DENY = "§r§cThat is not a liftsign.§r";
    private static final String DEFAULT_LIFTSIGN_BUILD_DENY_PUBLIC = "§r§cYou cannot build on a lift sign.§r";
    private static final String DEFAULT_LIFTSIGN_BUILD_DENY_PRIVATE = "§r§cYou cannot build on a private lift sign.§r";
    private static final String DEFAULT_LIFTSIGN_CREATE_PUBLIC_ALLOW = "§r§aLift sign created.§r";
    private static final String DEFAULT_LIFTSIGN_CREATE_PUBLIC_DENY = "§r§cYou do not have permission to create a lift sign.§r";
    private static final String DEFAULT_LIFTSIGN_CREATE_PUBLIC_ERROR = "§r§cThere was an error while creating the lift sign. Please try again. If the issue persists, please contact a server administrator.§r";
    private static final String DEFAULT_LIFTSIGN_CREATE_PRIVATE_ALLOW = "§r§aPrivate lift sign created.§r";
    private static final String DEFAULT_LIFTSIGN_CREATE_PRIVATE_DENY = "§r§cYou do not have permission to create a private lift sign.§r";
    private static final String DEFAULT_LIFTSIGN_CREATE_PRIVATE_ERROR = "§r§cThere was an error while creating the private lift sign. Please try again. If the issue persists, please contact a server administrator.§r";
    private static final String DEFAULT_LIFTSIGN_MODIFY_PUBLIC = "§r§cPublic lift signs cannot be modified.§r";
    private static final String DEFAULT_LIFTSIGN_MODIFY_PRIVATE_OWNER_ALLOW = "§r§6%%player%%§r §bhas been made owner.§r";
    private static final String DEFAULT_LIFTSIGN_MODIFY_PRIVATE_OWNER_DENY = "§r§cYou cannot change the owner of this private lift sign.§r";
    private static final String DEFAULT_LIFTSIGN_MODIFY_PRIVATE_ADMIN_TRUE = "§r§a%%player%%§r §bhas been added as an admin.§r";
    private static final String DEFAULT_LIFTSIGN_MODIFY_PRIVATE_ADMIN_CHANGE = "§r§a%%player%%§r §bhas been changed to an admin.§r";
    private static final String DEFAULT_LIFTSIGN_MODIFY_PRIVATE_ADMIN_FALSE = "§r§6%%player%%§r §bis already an admin.§r";
    private static final String DEFAULT_LIFTSIGN_MODIFY_PRIVATE_ADMIN_DENY = "§r§cYou cannot change the admins of this private lift sign.§r";
    private static final String DEFAULT_LIFTSIGN_MODIFY_PRIVATE_MEMBER_TRUE = "§r§a%%player%%§r §bhas been added as a member.§r";
    private static final String DEFAULT_LIFTSIGN_MODIFY_PRIVATE_MEMBER_CHANGE = "§r§a%%player%%§r §bhas been changed to a member.§r";
    private static final String DEFAULT_LIFTSIGN_MODIFY_PRIVATE_MEMBER_FALSE = "§r§6%%player%%§r §bis already a member.§r";
    private static final String DEFAULT_LIFTSIGN_MODIFY_PRIVATE_MEMBER_DENY = "§r§cYou do not have permission to modify this private lift sign.§r";
    private static final String DEFAULT_LIFTSIGN_MODIFY_PRIVATE_REMOVE_TRUE = "§r§a%%player%%§r §bhas been removed.§r";
    private static final String DEFAULT_LIFTSIGN_MODIFY_PRIVATE_REMOVE_FALSE = "§r§6%%player%%§r §bis not a member or admin.§r";
    private static final String DEFAULT_LIFTSIGN_MODIFY_PRIVATE_REMOVE_DENY = "§r§cYou do not have permission to modify this private lift sign.§r";
    private static final String DEFAULT_LIFTSIGN_MODIFY_PRIVATE_UNKNOWN_UNKNOWN = "§r§cUnknown player %%player%%.§r";
    private static final String DEFAULT_LIFTSIGN_MODIFY_PRIVATE_UNKNOWN_DENY = "§r§cYou do not have permission to modify this private lift sign.§r";
    private static final String DEFAULT_LIFTSIGN_MODIFY_OTHER = "§r§cThis is not a lift sign.§r";
    private static final String DEFAULT_LIFTSIGN_REMOVE_PUBLIC_ALLOW = "§r§aLift sign removed.§r";
    private static final String DEFAULT_LIFTSIGN_REMOVE_PUBLIC_DENY = "§r§cYou do not have permission to remove this lift sign.§r";
    private static final String DEFAULT_LIFTSIGN_REMOVE_PRIVATE_ALLOW = "§r§aPrivate lift sign removed.§r";
    private static final String DEFAULT_LIFTSIGN_REMOVE_PRIVATE_DENY = "§r§cYou do not have permission to remove this private lift sign.§r";
    private static final String DEFAULT_LIFTSIGN_REMOVE_ATTACHED_ALLOW = "§r§aAttached lift sign(s) removed.§r";
    private static final String DEFAULT_LIFTSIGN_REMOVE_ATTACHED_DENY = "§r§cYou do not have permission to remove the attached lift sign(s).§r";
    private static final String DEFAULT_LIFTSIGN_USE_NONE_DEFAULT = "§r§cThis lift sign does not go anywhere.§r";
    private static final String DEFAULT_LIFTSIGN_USE_UP_DEFAULT = "§r§fGoing Up§r";
    private static final String DEFAULT_LIFTSIGN_USE_UP_CUSTOM = "§r§fGoing to§r %%destination%%§r";
    private static final String DEFAULT_LIFTSIGN_USE_DOWN_DEFAULT = "§r§fGoing Down§r";
    private static final String DEFAULT_LIFTSIGN_USE_DOWN_CUSTOM = "§r§fGoing to§r %%destination%%§r";
    private static final String DEFAULT_LIFTSIGN_USE_DENY_PUBLIC = "§r§cYou do not have permission to use this lift sign.§r";
    private static final String DEFAULT_LIFTSIGN_USE_DENY_PRIVATE = "§r§cYou do not have permission to use this private lift sign.§r";
    private static final String DEFAULT_LIFTSIGN_USE_DISCONNECTED_PUBLIC = "§r§6This lift sign is not connected to another lift sign.§r";
    private static final String DEFAULT_LIFTSIGN_USE_DISCONNECTED_PRIVATE = "§r§6This private lift sign is not connected to another lift sign.§r";
    private static final String DEFAULT_LIFTSIGN_USE_BLOCKED_PUBLIC = "§r§6The destination for this lift sign is blocked.§r";
    private static final String DEFAULT_LIFTSIGN_USE_BLOCKED_PRIVATE = "§r§6The destination for this private lift sign is blocked.§r";
    private static final String DEFAULT_LIFTSIGN_FILE_ERROR_SAVE = "§r§cInternal error saving private lift file data. Please contact a server administrator with your location, and where the sign is.§r";
    private static final String DEFAULT_LIFTSIGN_FILE_ERROR_DELETE = "§r§cInternal error deleting private lift file data. Please contact a server administrator with your location, and where the sign was.§r";
    
    private static String commandInfo;
    private static String commandModify;
    private static String commandDeny;
    private static String liftsignInfoPublic;
    private static String liftsignInfoPrivate;
    private static String liftsignInfoError;
    private static String liftsignInfoDeny;
    private static String liftsignBuildDenyPublic;
    private static String liftsignBuildDenyPrivate;
    private static String liftsignCreatePublicAllow;
    private static String liftsignCreatePublicDeny;
    private static String liftsignCreatePublicError;
    private static String liftsignCreatePrivateAllow;
    private static String liftsignCreatePrivateDeny;
    private static String liftsignCreatePrivateError;
    private static String liftsignModifyPublic;
    private static String liftsignModifyPrivateOwnerAllow;
    private static String liftsignModifyPrivateOwnerDeny;
    private static String liftsignModifyPrivateAdminTrue;
    private static String liftsignModifyPrivateAdminChange;
    private static String liftsignModifyPrivateAdminFalse;
    private static String liftsignModifyPrivateAdminDeny;
    private static String liftsignModifyPrivateMemberTrue;
    private static String liftsignModifyPrivateMemberChange;
    private static String liftsignModifyPrivateMemberFalse;
    private static String liftsignModifyPrivateMemberDeny;
    private static String liftsignModifyPrivateRemoveTrue;
    private static String liftsignModifyPrivateRemoveFalse;
    private static String liftsignModifyPrivateRemoveDeny;
    private static String liftsignModifyPrivateUnknownUnknown;
    private static String liftsignModifyPrivateUnknownDeny;
    private static String liftsignModifyOther;
    private static String liftsignRemovePublicAllow;
    private static String liftsignRemovePublicDeny;
    private static String liftsignRemovePrivateAllow;
    private static String liftsignRemovePrivateDeny;
    private static String liftsignRemoveAttachedAllow;
    private static String liftsignRemoveAttachedDeny;
    private static String liftsignUseNoneDefault;
    private static String liftsignUseUpDefault;
    private static String liftsignUseUpCustom;
    private static String liftsignUseDownDefault;
    private static String liftsignUseDownCustom;
    private static String liftsignUseDenyPublic;
    private static String liftsignUseDenyPrivate;
    private static String liftsignUseDisconnectedPublic;
    private static String liftsignUseDisconnectedPrivate;
    private static String liftsignUseBlockedPublic;
    private static String liftsignUseBlockedPrivate;
    private static String liftsignFileErrorSave;
    private static String liftsignFileErrorDelete;
    
    /**
     * Prevent instantiation.
     */
    private ConfigMessage() {
        // No instances.
    }
    
    /**
     * Loads the various messages.
     * 
     * @param signLiftPlugin The {@link SignLiftPlugin}.
     * @param sender The {@link CommandSender} triggering the reload.
     * @param command {@code true} if this was triggered by a command,
     *                {@code false} otherwise (during initial plugin loading).
     */
    public static void reloadMessages(@NotNull final SignLiftPlugin signLiftPlugin, @NotNull final CommandSender sender, final boolean command) {
        
        final Logger logger = signLiftPlugin.getLogger();
        final BukkitScheduler scheduler = signLiftPlugin.getServer().getScheduler();
        scheduler.runTaskAsynchronously(signLiftPlugin, () -> {
            
            File messagesFile = new File(signLiftPlugin.getDataFolder(), "messages.yml");
            try {
    
                if (messagesFile.exists()) {
                    if (!messagesFile.isFile()) {
                        if (command) {
                            sender.sendMessage("§r§cAn error has occurred while reloading the SignLift messages. Please try again. If the error persists, please contact a server administrator.§r");
                        }
                        logger.log(Level.WARNING, "SignLift messages file is not a file: " + messagesFile.getPath());
                        logger.log(Level.WARNING, "SignLift will use the default messages.");
                        scheduler.runTask(signLiftPlugin, ConfigMessage::setDefaults);
                        return;
                    }
                } else {
                    if (!messagesFile.createNewFile()) {
                        if (command) {
                            sender.sendMessage("§r§cAn error has occurred while reloading the SignLift messages. Please try again. If the error persists, please contact a server administrator.§r");
                        }
                        logger.log(Level.WARNING, "SignLift messages file not created at " + messagesFile.getPath());
                        logger.log(Level.WARNING, "SignLift will use the default messages.");
                        scheduler.runTask(signLiftPlugin, ConfigMessage::setDefaults);
                        return;
                    }
                    
                    final InputStream defaultMessages = signLiftPlugin.getResource(messagesFile.getName());
                    final FileOutputStream outputStream = new FileOutputStream(messagesFile);
                    final byte[] buffer = new byte[4096];
                    int bytesRead;
                    
                    if (defaultMessages == null) {
                        if (command) {
                            sender.sendMessage("§r§cAn error has occurred while reloading the SignLift messages. Please try again. If the error persists, please contact a server administrator.§r");
                        }
                        logger.log(Level.WARNING, "SignLift default messages file not found. Possible compilation/build issue with the plugin.");
                        logger.log(Level.WARNING, "SignLift will use the default messages.");
                        scheduler.runTask(signLiftPlugin, ConfigMessage::setDefaults);
                        return;
                    }
                    
                    while ((bytesRead = defaultMessages.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    
                    outputStream.flush();
                    defaultMessages.close();
                    
                    if (command) {
                        sender.sendMessage("§r§cThe SignLift messages file did not exist; a copy of the default has been made and placed in the correct location.§r");
                        sender.sendMessage("§r§cPlease update the message as required for the installation, and then run§r §b/signlift reload message§r§c.§r");
                    }
    
                    logger.log(Level.WARNING, "SignLift messages file did not exist at " + messagesFile.getPath());
                    logger.log(Level.WARNING, "SignLift will use the default messages.");
                    logger.log(Level.WARNING, "Please update the messages as required for your installation, and then run \"/signlift reload message\".");
                }
            } catch (SecurityException | IOException e) {
                if (command) {
                    sender.sendMessage("§r§cAn error has occurred while reloading the SignLift messages. Please try again. If the error persists, please contact a server administrator.§r");
                }
                logger.log(Level.WARNING, "Unable to load the SignLift messages file.");
                logger.log(Level.WARNING, "SignLift will use the default messages.");
                logger.log(Level.WARNING, e.getClass().getSimpleName() + " thrown.", e);
                scheduler.runTask(signLiftPlugin, ConfigMessage::setDefaults);
                return;
            }
            
            final YamlConfiguration messages = new YamlConfiguration();
            try {
                messages.load(messagesFile);
            } catch (IOException | IllegalArgumentException | InvalidConfigurationException e) {
                logger.log(Level.WARNING, "Unable to load the SignLift messages.");
                logger.log(Level.WARNING, "SignLift will use the default messages.");
                logger.log(Level.WARNING, e.getClass().getSimpleName() + " thrown.", e);
                scheduler.runTask(signLiftPlugin, ConfigMessage::setDefaults);
                return;
            }
            
            scheduler.runTask(signLiftPlugin, () -> {
    
                ConfigMessage.commandInfo = messages.getString(ConfigMessage.KEY_COMMAND_INFO, ConfigMessage.DEFAULT_COMMAND_INFO);
                ConfigMessage.commandModify = messages.getString(ConfigMessage.KEY_COMMAND_MODIFY, ConfigMessage.DEFAULT_COMMAND_MODIFY);
                ConfigMessage.commandDeny = messages.getString(ConfigMessage.KEY_COMMAND_DENY, ConfigMessage.DEFAULT_COMMAND_DENY);
                ConfigMessage.liftsignInfoPublic = messages.getString(ConfigMessage.KEY_LIFTSIGN_INFO_PUBLIC, ConfigMessage.DEFAULT_LIFTSIGN_INFO_PUBLIC);
                ConfigMessage.liftsignInfoPrivate = messages.getString(ConfigMessage.KEY_LIFTSIGN_INFO_PRIVATE, ConfigMessage.DEFAULT_LIFTSIGN_INFO_PRIVATE);
                ConfigMessage.liftsignInfoError = messages.getString(ConfigMessage.KEY_LIFTSIGN_INFO_ERROR, ConfigMessage.DEFAULT_LIFTSIGN_INFO_ERROR);
                ConfigMessage.liftsignInfoDeny = messages.getString(ConfigMessage.KEY_LIFTSIGN_INFO_DENY, ConfigMessage.DEFAULT_LIFTSIGN_INFO_DENY);
                ConfigMessage.liftsignBuildDenyPublic = messages.getString(ConfigMessage.KEY_LIFTSIGN_BUILD_DENY_PUBLIC, ConfigMessage.DEFAULT_LIFTSIGN_BUILD_DENY_PUBLIC);
                ConfigMessage.liftsignBuildDenyPrivate = messages.getString(ConfigMessage.KEY_LIFTSIGN_BUILD_DENY_PRIVATE, ConfigMessage.DEFAULT_LIFTSIGN_BUILD_DENY_PRIVATE);
                ConfigMessage.liftsignCreatePublicAllow = messages.getString(ConfigMessage.KEY_LIFTSIGN_CREATE_PUBLIC_ALLOW, ConfigMessage.DEFAULT_LIFTSIGN_CREATE_PUBLIC_ALLOW);
                ConfigMessage.liftsignCreatePublicDeny = messages.getString(ConfigMessage.KEY_LIFTSIGN_CREATE_PUBLIC_DENY, ConfigMessage.DEFAULT_LIFTSIGN_CREATE_PUBLIC_DENY);
                ConfigMessage.liftsignCreatePublicError = messages.getString(ConfigMessage.KEY_LIFTSIGN_CREATE_PUBLIC_ERROR, ConfigMessage.DEFAULT_LIFTSIGN_CREATE_PUBLIC_ERROR);
                ConfigMessage.liftsignCreatePrivateAllow = messages.getString(ConfigMessage.KEY_LIFTSIGN_CREATE_PRIVATE_ALLOW, ConfigMessage.DEFAULT_LIFTSIGN_CREATE_PRIVATE_ALLOW);
                ConfigMessage.liftsignCreatePrivateDeny = messages.getString(ConfigMessage.KEY_LIFTSIGN_CREATE_PRIVATE_DENY, ConfigMessage.DEFAULT_LIFTSIGN_CREATE_PRIVATE_DENY);
                ConfigMessage.liftsignCreatePrivateError = messages.getString(ConfigMessage.KEY_LIFTSIGN_CREATE_PRIVATE_ERROR, ConfigMessage.DEFAULT_LIFTSIGN_CREATE_PRIVATE_ERROR);
                ConfigMessage.liftsignModifyPublic = messages.getString(ConfigMessage.KEY_LIFTSIGN_MODIFY_PUBLIC, ConfigMessage.DEFAULT_LIFTSIGN_MODIFY_PUBLIC);
                ConfigMessage.liftsignModifyPrivateOwnerAllow = messages.getString(ConfigMessage.KEY_LIFTSIGN_MODIFY_PRIVATE_OWNER_ALLOW, ConfigMessage.DEFAULT_LIFTSIGN_MODIFY_PRIVATE_OWNER_ALLOW);
                ConfigMessage.liftsignModifyPrivateOwnerDeny = messages.getString(ConfigMessage.KEY_LIFTSIGN_MODIFY_PRIVATE_OWNER_DENY, ConfigMessage.DEFAULT_LIFTSIGN_MODIFY_PRIVATE_OWNER_DENY);
                ConfigMessage.liftsignModifyPrivateAdminTrue = messages.getString(ConfigMessage.KEY_LIFTSIGN_MODIFY_PRIVATE_ADMIN_TRUE, ConfigMessage.DEFAULT_LIFTSIGN_MODIFY_PRIVATE_ADMIN_TRUE);
                ConfigMessage.liftsignModifyPrivateAdminChange = messages.getString(ConfigMessage.KEY_LIFTSIGN_MODIFY_PRIVATE_ADMIN_CHANGE, ConfigMessage.DEFAULT_LIFTSIGN_MODIFY_PRIVATE_ADMIN_CHANGE);
                ConfigMessage.liftsignModifyPrivateAdminFalse = messages.getString(ConfigMessage.KEY_LIFTSIGN_MODIFY_PRIVATE_ADMIN_FALSE, ConfigMessage.DEFAULT_LIFTSIGN_MODIFY_PRIVATE_ADMIN_FALSE);
                ConfigMessage.liftsignModifyPrivateAdminDeny = messages.getString(ConfigMessage.KEY_LIFTSIGN_MODIFY_PRIVATE_ADMIN_DENY, ConfigMessage.DEFAULT_LIFTSIGN_MODIFY_PRIVATE_ADMIN_DENY);
                ConfigMessage.liftsignModifyPrivateMemberTrue = messages.getString(ConfigMessage.KEY_LIFTSIGN_MODIFY_PRIVATE_MEMBER_TRUE, ConfigMessage.DEFAULT_LIFTSIGN_MODIFY_PRIVATE_MEMBER_TRUE);
                ConfigMessage.liftsignModifyPrivateMemberChange = messages.getString(ConfigMessage.KEY_LIFTSIGN_MODIFY_PRIVATE_MEMBER_CHANGE, ConfigMessage.DEFAULT_LIFTSIGN_MODIFY_PRIVATE_MEMBER_CHANGE);
                ConfigMessage.liftsignModifyPrivateMemberFalse = messages.getString(ConfigMessage.KEY_LIFTSIGN_MODIFY_PRIVATE_MEMBER_FALSE, ConfigMessage.DEFAULT_LIFTSIGN_MODIFY_PRIVATE_MEMBER_FALSE);
                ConfigMessage.liftsignModifyPrivateMemberDeny = messages.getString(ConfigMessage.KEY_LIFTSIGN_MODIFY_PRIVATE_MEMBER_DENY, ConfigMessage.DEFAULT_LIFTSIGN_MODIFY_PRIVATE_MEMBER_DENY);
                ConfigMessage.liftsignModifyPrivateRemoveTrue = messages.getString(ConfigMessage.KEY_LIFTSIGN_MODIFY_PRIVATE_REMOVE_TRUE, ConfigMessage.DEFAULT_LIFTSIGN_MODIFY_PRIVATE_REMOVE_TRUE);
                ConfigMessage.liftsignModifyPrivateRemoveFalse = messages.getString(ConfigMessage.KEY_LIFTSIGN_MODIFY_PRIVATE_REMOVE_FALSE, ConfigMessage.DEFAULT_LIFTSIGN_MODIFY_PRIVATE_REMOVE_FALSE);
                ConfigMessage.liftsignModifyPrivateRemoveDeny = messages.getString(ConfigMessage.KEY_LIFTSIGN_MODIFY_PRIVATE_REMOVE_DENY, ConfigMessage.DEFAULT_LIFTSIGN_MODIFY_PRIVATE_REMOVE_DENY);
                ConfigMessage.liftsignModifyPrivateUnknownUnknown = messages.getString(ConfigMessage.KEY_LIFTSIGN_MODIFY_PRIVATE_UNKNOWN_UNKNOWN, ConfigMessage.DEFAULT_LIFTSIGN_MODIFY_PRIVATE_UNKNOWN_UNKNOWN);
                ConfigMessage.liftsignModifyPrivateUnknownDeny = messages.getString(ConfigMessage.KEY_LIFTSIGN_MODIFY_PRIVATE_UNKNOWN_DENY, ConfigMessage.DEFAULT_LIFTSIGN_MODIFY_PRIVATE_UNKNOWN_DENY);
                ConfigMessage.liftsignModifyOther = messages.getString(ConfigMessage.KEY_LIFTSIGN_MODIFY_OTHER, ConfigMessage.DEFAULT_LIFTSIGN_MODIFY_OTHER);
                ConfigMessage.liftsignRemovePublicAllow = messages.getString(ConfigMessage.KEY_LIFTSIGN_REMOVE_PUBLIC_ALLOW, ConfigMessage.DEFAULT_LIFTSIGN_REMOVE_PUBLIC_ALLOW);
                ConfigMessage.liftsignRemovePublicDeny = messages.getString(ConfigMessage.KEY_LIFTSIGN_REMOVE_PUBLIC_DENY, ConfigMessage.DEFAULT_LIFTSIGN_REMOVE_PUBLIC_DENY);
                ConfigMessage.liftsignRemovePrivateAllow = messages.getString(ConfigMessage.KEY_LIFTSIGN_REMOVE_PRIVATE_ALLOW, ConfigMessage.DEFAULT_LIFTSIGN_REMOVE_PRIVATE_ALLOW);
                ConfigMessage.liftsignRemovePrivateDeny = messages.getString(ConfigMessage.KEY_LIFTSIGN_REMOVE_PRIVATE_DENY, ConfigMessage.DEFAULT_LIFTSIGN_REMOVE_PRIVATE_DENY);
                ConfigMessage.liftsignRemoveAttachedAllow = messages.getString(ConfigMessage.KEY_LIFTSIGN_REMOVE_ATTACHED_ALLOW, ConfigMessage.DEFAULT_LIFTSIGN_REMOVE_ATTACHED_ALLOW);
                ConfigMessage.liftsignRemoveAttachedDeny = messages.getString(ConfigMessage.KEY_LIFTSIGN_REMOVE_ATTACHED_DENY, ConfigMessage.DEFAULT_LIFTSIGN_REMOVE_ATTACHED_DENY);
                ConfigMessage.liftsignUseNoneDefault = messages.getString(ConfigMessage.KEY_LIFTSIGN_USE_NONE_DEFAULT, ConfigMessage.DEFAULT_LIFTSIGN_USE_NONE_DEFAULT);
                ConfigMessage.liftsignUseUpDefault = messages.getString(ConfigMessage.KEY_LIFTSIGN_USE_UP_DEFAULT, ConfigMessage.DEFAULT_LIFTSIGN_USE_UP_DEFAULT);
                ConfigMessage.liftsignUseUpCustom = messages.getString(ConfigMessage.KEY_LIFTSIGN_USE_UP_CUSTOM, ConfigMessage.DEFAULT_LIFTSIGN_USE_UP_CUSTOM);
                ConfigMessage.liftsignUseDownDefault = messages.getString(ConfigMessage.KEY_LIFTSIGN_USE_DOWN_DEFAULT, ConfigMessage.DEFAULT_LIFTSIGN_USE_DOWN_DEFAULT);
                ConfigMessage.liftsignUseDownCustom = messages.getString(ConfigMessage.KEY_LIFTSIGN_USE_DOWN_CUSTOM, ConfigMessage.DEFAULT_LIFTSIGN_USE_DOWN_CUSTOM);
                ConfigMessage.liftsignUseDenyPublic = messages.getString(ConfigMessage.KEY_LIFTSIGN_USE_DENY_PUBLIC, ConfigMessage.DEFAULT_LIFTSIGN_USE_DENY_PUBLIC);
                ConfigMessage.liftsignUseDenyPrivate = messages.getString(ConfigMessage.KEY_LIFTSIGN_USE_DENY_PRIVATE, ConfigMessage.DEFAULT_LIFTSIGN_USE_DENY_PRIVATE);
                ConfigMessage.liftsignUseDisconnectedPublic = messages.getString(ConfigMessage.KEY_LIFTSIGN_USE_DISCONNECTED_PUBLIC, ConfigMessage.DEFAULT_LIFTSIGN_USE_DISCONNECTED_PUBLIC);
                ConfigMessage.liftsignUseDisconnectedPrivate = messages.getString(ConfigMessage.KEY_LIFTSIGN_USE_DISCONNECTED_PRIVATE, ConfigMessage.DEFAULT_LIFTSIGN_USE_DISCONNECTED_PRIVATE);
                ConfigMessage.liftsignUseBlockedPublic = messages.getString(ConfigMessage.KEY_LIFTSIGN_USE_BLOCKED_PUBLIC, ConfigMessage.DEFAULT_LIFTSIGN_USE_BLOCKED_PUBLIC);
                ConfigMessage.liftsignUseBlockedPrivate = messages.getString(ConfigMessage.KEY_LIFTSIGN_USE_BLOCKED_PRIVATE, ConfigMessage.DEFAULT_LIFTSIGN_USE_BLOCKED_PRIVATE);
                ConfigMessage.liftsignFileErrorSave = messages.getString(ConfigMessage.KEY_LIFTSIGN_FILE_ERROR_SAVE, ConfigMessage.DEFAULT_LIFTSIGN_FILE_ERROR_SAVE);
                ConfigMessage.liftsignFileErrorDelete = messages.getString(ConfigMessage.KEY_LIFTSIGN_FILE_ERROR_DELETE, ConfigMessage.DEFAULT_LIFTSIGN_FILE_ERROR_DELETE);
    
                if (command) {
                    sender.sendMessage("§r§aThe SignLift messages have been reloaded. Please verify that all LiftSign messages are working as intended.§r");
                }
            });
        });
    }
    
    /**
     * Sets the configuration items to their default values.
     */
    private static void setDefaults() {
        ConfigMessage.commandInfo = ConfigMessage.DEFAULT_COMMAND_INFO;
        ConfigMessage.commandModify = ConfigMessage.DEFAULT_COMMAND_MODIFY;
        ConfigMessage.commandDeny = ConfigMessage.DEFAULT_COMMAND_DENY;
        ConfigMessage.liftsignInfoPublic = ConfigMessage.DEFAULT_LIFTSIGN_INFO_PUBLIC;
        ConfigMessage.liftsignInfoPrivate = ConfigMessage.DEFAULT_LIFTSIGN_INFO_PRIVATE;
        ConfigMessage.liftsignInfoError = ConfigMessage.DEFAULT_LIFTSIGN_INFO_ERROR;
        ConfigMessage.liftsignInfoDeny = ConfigMessage.DEFAULT_LIFTSIGN_INFO_DENY;
        ConfigMessage.liftsignBuildDenyPublic = ConfigMessage.DEFAULT_LIFTSIGN_BUILD_DENY_PUBLIC;
        ConfigMessage.liftsignBuildDenyPrivate = ConfigMessage.DEFAULT_LIFTSIGN_BUILD_DENY_PRIVATE;
        ConfigMessage.liftsignCreatePublicAllow = ConfigMessage.DEFAULT_LIFTSIGN_CREATE_PUBLIC_ALLOW;
        ConfigMessage.liftsignCreatePublicDeny = ConfigMessage.DEFAULT_LIFTSIGN_CREATE_PUBLIC_DENY;
        ConfigMessage.liftsignCreatePublicError = ConfigMessage.DEFAULT_LIFTSIGN_CREATE_PUBLIC_ERROR;
        ConfigMessage.liftsignCreatePrivateAllow = ConfigMessage.DEFAULT_LIFTSIGN_CREATE_PRIVATE_ALLOW;
        ConfigMessage.liftsignCreatePrivateDeny = ConfigMessage.DEFAULT_LIFTSIGN_CREATE_PRIVATE_DENY;
        ConfigMessage.liftsignCreatePrivateError = ConfigMessage.DEFAULT_LIFTSIGN_CREATE_PRIVATE_ERROR;
        ConfigMessage.liftsignModifyPublic = ConfigMessage.DEFAULT_LIFTSIGN_MODIFY_PUBLIC;
        ConfigMessage.liftsignModifyPrivateOwnerAllow = ConfigMessage.DEFAULT_LIFTSIGN_MODIFY_PRIVATE_OWNER_ALLOW;
        ConfigMessage.liftsignModifyPrivateOwnerDeny = ConfigMessage.DEFAULT_LIFTSIGN_MODIFY_PRIVATE_OWNER_DENY;
        ConfigMessage.liftsignModifyPrivateAdminTrue = ConfigMessage.DEFAULT_LIFTSIGN_MODIFY_PRIVATE_ADMIN_TRUE;
        ConfigMessage.liftsignModifyPrivateAdminChange = ConfigMessage.DEFAULT_LIFTSIGN_MODIFY_PRIVATE_ADMIN_CHANGE;
        ConfigMessage.liftsignModifyPrivateAdminFalse = ConfigMessage.DEFAULT_LIFTSIGN_MODIFY_PRIVATE_ADMIN_FALSE;
        ConfigMessage.liftsignModifyPrivateAdminDeny = ConfigMessage.DEFAULT_LIFTSIGN_MODIFY_PRIVATE_ADMIN_DENY;
        ConfigMessage.liftsignModifyPrivateMemberTrue = ConfigMessage.DEFAULT_LIFTSIGN_MODIFY_PRIVATE_MEMBER_TRUE;
        ConfigMessage.liftsignModifyPrivateMemberChange = ConfigMessage.DEFAULT_LIFTSIGN_MODIFY_PRIVATE_MEMBER_CHANGE;
        ConfigMessage.liftsignModifyPrivateMemberFalse = ConfigMessage.DEFAULT_LIFTSIGN_MODIFY_PRIVATE_MEMBER_FALSE;
        ConfigMessage.liftsignModifyPrivateMemberDeny = ConfigMessage.DEFAULT_LIFTSIGN_MODIFY_PRIVATE_MEMBER_DENY;
        ConfigMessage.liftsignModifyPrivateRemoveTrue = ConfigMessage.DEFAULT_LIFTSIGN_MODIFY_PRIVATE_REMOVE_TRUE;
        ConfigMessage.liftsignModifyPrivateRemoveFalse = ConfigMessage.DEFAULT_LIFTSIGN_MODIFY_PRIVATE_REMOVE_FALSE;
        ConfigMessage.liftsignModifyPrivateRemoveDeny = ConfigMessage.DEFAULT_LIFTSIGN_MODIFY_PRIVATE_REMOVE_DENY;
        ConfigMessage.liftsignModifyPrivateUnknownUnknown = ConfigMessage.DEFAULT_LIFTSIGN_MODIFY_PRIVATE_UNKNOWN_UNKNOWN;
        ConfigMessage.liftsignModifyPrivateUnknownDeny = ConfigMessage.DEFAULT_LIFTSIGN_MODIFY_PRIVATE_UNKNOWN_DENY;
        ConfigMessage.liftsignModifyOther = ConfigMessage.DEFAULT_LIFTSIGN_MODIFY_OTHER;
        ConfigMessage.liftsignRemovePublicAllow = ConfigMessage.DEFAULT_LIFTSIGN_REMOVE_PUBLIC_ALLOW;
        ConfigMessage.liftsignRemovePublicDeny = ConfigMessage.DEFAULT_LIFTSIGN_REMOVE_PUBLIC_DENY;
        ConfigMessage.liftsignRemovePrivateAllow = ConfigMessage.DEFAULT_LIFTSIGN_REMOVE_PRIVATE_ALLOW;
        ConfigMessage.liftsignRemovePrivateDeny = ConfigMessage.DEFAULT_LIFTSIGN_REMOVE_PRIVATE_DENY;
        ConfigMessage.liftsignRemoveAttachedAllow = ConfigMessage.DEFAULT_LIFTSIGN_REMOVE_ATTACHED_ALLOW;
        ConfigMessage.liftsignRemoveAttachedDeny = ConfigMessage.DEFAULT_LIFTSIGN_REMOVE_ATTACHED_DENY;
        ConfigMessage.liftsignUseNoneDefault = ConfigMessage.DEFAULT_LIFTSIGN_USE_NONE_DEFAULT;
        ConfigMessage.liftsignUseUpDefault = ConfigMessage.DEFAULT_LIFTSIGN_USE_UP_DEFAULT;
        ConfigMessage.liftsignUseUpCustom = ConfigMessage.DEFAULT_LIFTSIGN_USE_UP_CUSTOM;
        ConfigMessage.liftsignUseDownDefault = ConfigMessage.DEFAULT_LIFTSIGN_USE_DOWN_DEFAULT;
        ConfigMessage.liftsignUseDownCustom = ConfigMessage.DEFAULT_LIFTSIGN_USE_DOWN_CUSTOM;
        ConfigMessage.liftsignUseDenyPublic = ConfigMessage.DEFAULT_LIFTSIGN_USE_DENY_PUBLIC;
        ConfigMessage.liftsignUseDenyPrivate = ConfigMessage.DEFAULT_LIFTSIGN_USE_DENY_PRIVATE;
        ConfigMessage.liftsignUseDisconnectedPublic = ConfigMessage.DEFAULT_LIFTSIGN_USE_DISCONNECTED_PUBLIC;
        ConfigMessage.liftsignUseDisconnectedPrivate = ConfigMessage.DEFAULT_LIFTSIGN_USE_DISCONNECTED_PRIVATE;
        ConfigMessage.liftsignUseBlockedPublic = ConfigMessage.DEFAULT_LIFTSIGN_USE_BLOCKED_PUBLIC;
        ConfigMessage.liftsignUseBlockedPrivate = ConfigMessage.DEFAULT_LIFTSIGN_USE_BLOCKED_PRIVATE;
        ConfigMessage.liftsignFileErrorSave = ConfigMessage.DEFAULT_LIFTSIGN_FILE_ERROR_SAVE;
        ConfigMessage.liftsignFileErrorDelete = ConfigMessage.DEFAULT_LIFTSIGN_FILE_ERROR_DELETE;
    }
    
    @NotNull
    public static String getCommandInfo() {
        return ConfigMessage.commandInfo;
    }
    
    @NotNull
    public static String getCommandModify() {
        return ConfigMessage.commandModify;
    }
    
    @NotNull
    public static String getCommandDeny() {
        return ConfigMessage.commandDeny;
    }
    
    @NotNull
    public static String getLiftsignInfoPublic() {
        return ConfigMessage.liftsignInfoPublic;
    }
    
    @NotNull
    public static String getLiftsignInfoPrivate() {
        return ConfigMessage.liftsignInfoPrivate;
    }
    
    @NotNull
    public static String getLiftsignInfoError() {
        return ConfigMessage.liftsignInfoError;
    }
    
    @NotNull
    public static String getLiftsignInfoDeny() {
        return ConfigMessage.liftsignInfoDeny;
    }
    
    @NotNull
    public static String getLiftsignBuildDenyPublic() {
        return ConfigMessage.liftsignBuildDenyPublic;
    }
    
    @NotNull
    public static String getLiftsignBuildDenyPrivate() {
        return ConfigMessage.liftsignBuildDenyPrivate;
    }
    
    @NotNull
    public static String getLiftsignCreatePublicAllow() {
        return ConfigMessage.liftsignCreatePublicAllow;
    }
    
    @NotNull
    public static String getLiftsignCreatePublicDeny() {
        return ConfigMessage.liftsignCreatePublicDeny;
    }
    
    @NotNull
    public static String getLiftsignCreatePublicError() {
        return ConfigMessage.liftsignCreatePublicError;
    }
    
    @NotNull
    public static String getLiftsignCreatePrivateAllow() {
        return ConfigMessage.liftsignCreatePrivateAllow;
    }
    
    @NotNull
    public static String getLiftsignCreatePrivateDeny() {
        return ConfigMessage.liftsignCreatePrivateDeny;
    }
    
    @NotNull
    public static String getLiftsignCreatePrivateError() {
        return ConfigMessage.liftsignCreatePrivateError;
    }
    
    @NotNull
    public static String getLiftsignModifyPublic() {
        return ConfigMessage.liftsignModifyPublic;
    }
    
    @NotNull
    public static String getLiftsignModifyPrivateOwnerAllow() {
        return ConfigMessage.liftsignModifyPrivateOwnerAllow;
    }
    
    @NotNull
    public static String getLiftsignModifyPrivateOwnerDeny() {
        return ConfigMessage.liftsignModifyPrivateOwnerDeny;
    }
    
    @NotNull
    public static String getLiftsignModifyPrivateAdminTrue() {
        return ConfigMessage.liftsignModifyPrivateAdminTrue;
    }
    
    @NotNull
    public static String getLiftsignModifyPrivateAdminChange() {
        return ConfigMessage.liftsignModifyPrivateAdminChange;
    }
    
    @NotNull
    public static String getLiftsignModifyPrivateAdminFalse() {
        return ConfigMessage.liftsignModifyPrivateAdminFalse;
    }
    
    @NotNull
    public static String getLiftsignModifyPrivateAdminDeny() {
        return ConfigMessage.liftsignModifyPrivateAdminDeny;
    }
    
    @NotNull
    public static String getLiftsignModifyPrivateMemberTrue() {
        return ConfigMessage.liftsignModifyPrivateMemberTrue;
    }
    
    @NotNull
    public static String getLiftsignModifyPrivateMemberChange() {
        return ConfigMessage.liftsignModifyPrivateMemberChange;
    }
    
    @NotNull
    public static String getLiftsignModifyPrivateMemberFalse() {
        return ConfigMessage.liftsignModifyPrivateMemberFalse;
    }
    
    @NotNull
    public static String getLiftsignModifyPrivateMemberDeny() {
        return ConfigMessage.liftsignModifyPrivateMemberDeny;
    }
    
    @NotNull
    public static String getLiftsignModifyPrivateRemoveTrue() {
        return ConfigMessage.liftsignModifyPrivateRemoveTrue;
    }
    
    @NotNull
    public static String getLiftsignModifyPrivateRemoveFalse() {
        return ConfigMessage.liftsignModifyPrivateRemoveFalse;
    }
    
    @NotNull
    public static String getLiftsignModifyPrivateRemoveDeny() {
        return ConfigMessage.liftsignModifyPrivateRemoveDeny;
    }
    
    @NotNull
    public static String getLiftsignModifyPrivateUnknownUnknown() {
        return ConfigMessage.liftsignModifyPrivateUnknownUnknown;
    }
    
    @NotNull
    public static String getLiftsignModifyPrivateUnknownDeny() {
        return ConfigMessage.liftsignModifyPrivateUnknownDeny;
    }
    
    @NotNull
    public static String getLiftsignModifyOther() {
        return ConfigMessage.liftsignModifyOther;
    }
    
    @NotNull
    public static String getLiftsignRemovePublicAllow() {
        return ConfigMessage.liftsignRemovePublicAllow;
    }
    
    @NotNull
    public static String getLiftsignRemovePublicDeny() {
        return ConfigMessage.liftsignRemovePublicDeny;
    }
    
    @NotNull
    public static String getLiftsignRemovePrivateAllow() {
        return ConfigMessage.liftsignRemovePrivateAllow;
    }
    
    @NotNull
    public static String getLiftsignRemovePrivateDeny() {
        return ConfigMessage.liftsignRemovePrivateDeny;
    }
    
    @NotNull
    public static String getLiftsignRemoveAttachedAllow() {
        return ConfigMessage.liftsignRemoveAttachedAllow;
    }
    
    @NotNull
    public static String getLiftsignRemoveAttachedDeny() {
        return ConfigMessage.liftsignRemoveAttachedDeny;
    }
    
    @NotNull
    public static String getLiftsignUseNoneDefault() {
        return ConfigMessage.liftsignUseNoneDefault;
    }
    
    @NotNull
    public static String getLiftsignUseUpDefault() {
        return ConfigMessage.liftsignUseUpDefault;
    }
    
    @NotNull
    public static String getLiftsignUseUpCustom() {
        return ConfigMessage.liftsignUseUpCustom;
    }
    
    @NotNull
    public static String getLiftsignUseDownDefault() {
        return ConfigMessage.liftsignUseDownDefault;
    }
    
    @NotNull
    public static String getLiftsignUseDownCustom() {
        return ConfigMessage.liftsignUseDownCustom;
    }
    
    @NotNull
    public static String getLiftsignUseDenyPublic() {
        return ConfigMessage.liftsignUseDenyPublic;
    }
    
    @NotNull
    public static String getLiftsignUseDenyPrivate() {
        return ConfigMessage.liftsignUseDenyPrivate;
    }
    
    @NotNull
    public static String getLiftsignUseDisconnectedPublic() {
        return ConfigMessage.liftsignUseDisconnectedPublic;
    }
    
    @NotNull
    public static String getLiftsignUseDisconnectedPrivate() {
        return ConfigMessage.liftsignUseDisconnectedPrivate;
    }
    
    @NotNull
    public static String getLiftsignUseBlockedPublic() {
        return ConfigMessage.liftsignUseBlockedPublic;
    }
    
    @NotNull
    public static String getLiftsignUseBlockedPrivate() {
        return ConfigMessage.liftsignUseBlockedPrivate;
    }
    
    @NotNull
    public static String getLiftsignFileErrorSave() {
        return ConfigMessage.liftsignFileErrorSave;
    }
    
    @NotNull
    public static String getLiftsignFileErrorDelete() {
        return ConfigMessage.liftsignFileErrorDelete;
    }
}
