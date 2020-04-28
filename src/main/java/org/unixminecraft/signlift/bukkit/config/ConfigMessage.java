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

package org.unixminecraft.signlift.bukkit.config;

import org.bukkit.configuration.file.FileConfiguration;

public final class ConfigMessage {
	
	private static String commandInfoDefault;
	private static String commandModifyDefault;
	private static String commandDenyPermission;
	private static String commandDenyPlayer;
	private static String liftsignInfoPublic;
	private static String liftsignInfoPrivate;
	private static String liftsignInfoError;
	private static String liftsignInfoDeny;
	private static String liftsignMaintainBuildDenyPublic;
	private static String liftsignMaintainBuildDenyPrivate;
	private static String liftsignMaintainCreatePublicAllow;
	private static String liftsignMaintainCreatePublicDeny;
	private static String liftsignMaintainCreatePrivateAllow;
	private static String liftsignMaintainCreatePrivateDeny;
	private static String liftsignMaintainModifyPublicDeny;
	private static String liftsignMaintainModifyPrivateOwnerAllow;
	private static String liftsignMaintainModifyPrivateOwnerDeny;
	private static String liftsignMaintainModifyPrivateAdminTrue;
	private static String liftsignMaintainModifyPrivateAdminChange;
	private static String liftsignMaintainModifyPrivateAdminFalse;
	private static String liftsignMaintainModifyPrivateAdminDeny;
	private static String liftsignMaintainModifyPrivateMemberTrue;
	private static String liftsignMaintainModifyPrivateMemberChange;
	private static String liftsignMaintainModifyPrivateMemberFalse;
	private static String liftsignMaintainModifyPrivateMemberDeny;
	private static String liftsignMaintainModifyPrivateRemoveTrue;
	private static String liftsignMaintainModifyPrivateRemoveFalse;
	private static String liftsignMaintainModifyPrivateRemoveDeny;
	private static String liftsignMaintainModifyPrivateUnknownUnknown;
	private static String liftsignMaintainModifyPrivateUnknownDeny;
	private static String liftsignMaintainModifyOtherDeny;
	private static String liftsignMaintainRemovePublicAllow;
	private static String liftsignMaintainRemovePublicDeny;
	private static String liftsignMaintainRemovePrivateAllow;
	private static String liftsignMaintainRemovePrivateDeny;
	private static String liftsignMaintainRemoveMultipleAllow;
	private static String liftsignMaintainRemoveMultipleDeny;
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
	
	private ConfigMessage() {
		
	}
	
	public static void loadMessages(final FileConfiguration messages) {
		
		commandInfoDefault = messages.getString("command.info.default", "§r§3Right-click on a lift sign to see information about it.§r");
		commandModifyDefault = messages.getString("command.modify.default", "§r§3Left-click on a private lift sign to make your changes.§r");
		commandDenyPermission = messages.getString("command.deny.permission", "§r§cNo permission.§r");
		commandDenyPlayer = messages.getString("command.deny.player", "§r§cOnly players may use this command.§r");
		liftsignInfoPublic = messages.getString("liftsign.info.public", "§r§aThis is a public lift sign.§r");
		liftsignInfoPrivate = messages.getString("liftsign.info.private", "§r§aThis is a private lift sign.§r");
		liftsignInfoError = messages.getString("liftsign.info.error", "§r§cInternal error, please try again. If the issue persists, please contact a server administrator.§r");
		liftsignInfoDeny = messages.getString("liftsign.info.deny", "§r§cThat is not a liftsign.§r");
		liftsignMaintainBuildDenyPublic = messages.getString("liftsign.maintain.build.deny.public", "§r§cYou cannot build on a lift sign.§r");
		liftsignMaintainBuildDenyPrivate = messages.getString("liftsign.maintain.build.deny.private", "§r§cYou cannot build on a private lift sign.§r");
		liftsignMaintainCreatePublicAllow = messages.getString("liftsign.maintain.create.public.allow", "§r§aLift sign created.§r");
		liftsignMaintainCreatePublicDeny = messages.getString("liftsign.maintain.create.public.deny", "§r§cYou do not have permission to create a lift sign.§r");
		liftsignMaintainCreatePrivateAllow = messages.getString("liftsign.maintain.create.private.allow", "§r§aPrivate lift sign created.§r");
		liftsignMaintainCreatePrivateDeny = messages.getString("liftsign.maintain.create.private.deny", "§r§cYou do not have permission to create a private lift sign.§r");
		liftsignMaintainModifyPublicDeny = messages.getString("liftsign.maintain.modify.public.deny", "§r§cPublic lift signs cannot be modified.§r");
		liftsignMaintainModifyPrivateOwnerAllow = messages.getString("liftsign.maintain.modify.private.owner.allow", "§r§6%%player%%§r §bhas been made owner.§r");
		liftsignMaintainModifyPrivateOwnerDeny = messages.getString("liftsign.maintain.modify.private.owner.deny", "§r§cYou cannot change the owner of this private lift sign.§r");
		liftsignMaintainModifyPrivateAdminTrue = messages.getString("liftsign.maintain.modify.private.admin.true", "§r§a%%player%%§r §bhas been added as an admin.§r");
		liftsignMaintainModifyPrivateAdminChange = messages.getString("liftsign.maintain.modify.private.admin.change", "§r§a%%player%%§r §bhas been changed to an admin.§r");
		liftsignMaintainModifyPrivateAdminFalse = messages.getString("liftsign.maintain.modify.private.admin.false", "§r§6%%player%%§r §bis already an admin.§r");
		liftsignMaintainModifyPrivateAdminDeny = messages.getString("liftsign.maintain.modify.private.admin.deny", "§r§cYou cannot change the admins of this private lift sign.§r");
		liftsignMaintainModifyPrivateMemberTrue = messages.getString("liftsign.maintain.modify.private.member.true", "§r§a%%player%%§r §bhas been added as a member.§r");
		liftsignMaintainModifyPrivateMemberChange = messages.getString("liftsign.maintain.modify.private.member.change", "§r§a%%player%%§r §bhas been changed to a member.§r");
		liftsignMaintainModifyPrivateMemberFalse = messages.getString("liftsign.maintain.modify.private.member.false", "§r§6%%player%%§r §bis already a member.§r");
		liftsignMaintainModifyPrivateMemberDeny = messages.getString("liftsign.maintain.modify.private.member.deny", "§r§cYou do not have permission to modify this private lift sign.§r");
		liftsignMaintainModifyPrivateRemoveTrue = messages.getString("liftsign.maintain.modify.private.remove.true", "§r§a%%player%%§r §bhas been removed.§r");
		liftsignMaintainModifyPrivateRemoveFalse = messages.getString("liftsign.maintain.modify.private.remove.false", "§r§6%%player%%§r §bis not a member or admin.§r");
		liftsignMaintainModifyPrivateRemoveDeny = messages.getString("liftsign.maintain.modify.private.remove.deny", "deny: §r§cYou do not have permission to modify this private lift sign.§r");
		liftsignMaintainModifyPrivateUnknownUnknown = messages.getString("liftsign.maintain.modify.private.unknown.unknown", "§r§cUnknown player %%player%%.§r");
		liftsignMaintainModifyPrivateUnknownDeny = messages.getString("liftsign.maintain.modify.private.unknown.deny", "§r§cYou do not have permission to modify this private lift sign.§r");
		liftsignMaintainModifyOtherDeny = messages.getString("liftsign.maintain.modify.other.deny", "§r§cThis is not a lift sign.§r");
		liftsignMaintainRemovePublicAllow = messages.getString("liftsign.maintain.remove.public.allow", "§r§aLift sign removed.§r");
		liftsignMaintainRemovePublicDeny = messages.getString("liftsign.maintain.remove.public.deny", "§r§cYou do not have permission to remove this lift sign.§r");
		liftsignMaintainRemovePrivateAllow = messages.getString("liftsign.maintain.remove.private.allow", "§r§aPrivate lift sign removed.§r");
		liftsignMaintainRemovePrivateDeny = messages.getString("liftsign.maintain.remove.private.deny", "§r§cYou do not have permission to remove this private lift sign.§r");
		liftsignMaintainRemoveMultipleAllow = messages.getString("liftsign.maintain.remove.multiple.allow", "§r§aLift sign(s) removed.§r");
		liftsignMaintainRemoveMultipleDeny = messages.getString("liftsign.maintain.remove.multiple.deny", "§r§cYou do not have permission to remove these lift sign(s).§r");
		liftsignUseNoneDefault = messages.getString("liftsign.use.none.default", "§r§cThis lift sign does not go anywhere.§r");
		liftsignUseUpDefault = messages.getString("liftsign.use.up.default", "§r§fGoing Up§r");
		liftsignUseUpCustom = messages.getString("liftsign.use.up.custom", "§r§fGoing to§r %%destination%%§r");
		liftsignUseDownDefault = messages.getString("liftsign.use.down.default", "§r§fGoing Down§r");
		liftsignUseDownCustom = messages.getString("liftsign.use.down.custom", "§r§fGoing to§r %%destination%%§r");
		liftsignUseDenyPublic = messages.getString("liftsign.use.deny.public", "§r§cYou do not have permission to use this lift sign.§r");
		liftsignUseDenyPrivate = messages.getString("liftsign.use.deny.private", "§r§cYou do not have permission to use this private lift sign.§r");
		liftsignUseDisconnectedPublic = messages.getString("liftsign.use.disconnected.public", "§r§6This lift sign is not connected to another lift sign.§r");
		liftsignUseDisconnectedPrivate = messages.getString("liftsign.use.disconnected.private", "§r§6This private lift sign is not connected to another lift sign.§r");
		liftsignUseBlockedPublic = messages.getString("liftsign.use.blocked.public", "§r§6The destination for this lift sign is blocked.§r");
		liftsignUseBlockedPrivate = messages.getString("liftsign.use.blocked.private", "§r§6The destination for this private lift sign is blocked.§r");
		liftsignFileErrorSave = messages.getString("liftsign.file.error.save", "§r§cInternal error saving private lift file data. Please contact a server administrator with your location, and where the sign is.§r");
		liftsignFileErrorDelete = messages.getString("liftsign.file.error.delete", "§r§cInternal error deleting private lift file data. Please contact a server administrator with your location, and where the sign was.§r");
	}
	
	public static String getCommandInfoDefault() {
		return commandInfoDefault;
	}
	
	public static String getCommandModifyDefault() {
		return commandModifyDefault;
	}
	
	public static String getCommandDenyPermission() {
		return commandDenyPermission;
	}
	
	public static String getCommandDenyPlayer() {
		return commandDenyPlayer;
	}
	
	public static String getLiftsignInfoPublic() {
		return liftsignInfoPublic;
	}
	
	public static String getLiftsignInfoPrivate() {
		return liftsignInfoPrivate;
	}
	
	public static String getLiftsignInfoError() {
		return liftsignInfoError;
	}
	
	public static String getLiftsignInfoDeny() {
		return liftsignInfoDeny;
	}
	
	public static String getLiftsignMaintainBuildDenyPublic() {
		return liftsignMaintainBuildDenyPublic;
	}
	
	public static String getLiftsignMaintainBuildDenyPrivate() {
		return liftsignMaintainBuildDenyPrivate;
	}
	
	public static String getLiftsignMaintainCreatePublicAllow() {
		return liftsignMaintainCreatePublicAllow;
	}
	
	public static String getLiftsignMaintainCreatePublicDeny() {
		return liftsignMaintainCreatePublicDeny;
	}
	
	public static String getLiftsignMaintainCreatePrivateAllow() {
		return liftsignMaintainCreatePrivateAllow;
	}
	
	public static String getLiftsignMaintainCreatePrivateDeny() {
		return liftsignMaintainCreatePrivateDeny;
	}
	
	public static String getLiftsignMaintainModifyPublicDeny() {
		return liftsignMaintainModifyPublicDeny;
	}
	
	public static String getLiftsignMaintainModifyPrivateOwnerAllow() {
		return liftsignMaintainModifyPrivateOwnerAllow;
	}
	
	public static String getLiftsignMaintainModifyPrivateOwnerDeny() {
		return liftsignMaintainModifyPrivateOwnerDeny;
	}
	
	public static String getLiftsignMaintainModifyPrivateAdminTrue() {
		return liftsignMaintainModifyPrivateAdminTrue;
	}
	
	public static String getLiftsignMaintainModifyPrivateAdminChange() {
		return liftsignMaintainModifyPrivateAdminChange;
	}
	
	public static String getLiftsignMaintainModifyPrivateAdminFalse() {
		return liftsignMaintainModifyPrivateAdminFalse;
	}
	
	public static String getLiftsignMaintainModifyPrivateAdminDeny() {
		return liftsignMaintainModifyPrivateAdminDeny;
	}
	
	public static String getLiftsignMaintainModifyPrivateMemberTrue() {
		return liftsignMaintainModifyPrivateMemberTrue;
	}
	
	public static String getLiftsignMaintainModifyPrivateMemberChange() {
		return liftsignMaintainModifyPrivateMemberChange;
	}
	
	public static String getLiftsignMaintainModifyPrivateMemberFalse() {
		return liftsignMaintainModifyPrivateMemberFalse;
	}
	
	public static String getLiftsignMaintainModifyPrivateMemberDeny() {
		return liftsignMaintainModifyPrivateMemberDeny;
	}
	
	public static String getLiftsignMaintainModifyPrivateRemoveTrue() {
		return liftsignMaintainModifyPrivateRemoveTrue;
	}
	
	public static String getLiftsignMaintainModifyPrivateRemoveFalse() {
		return liftsignMaintainModifyPrivateRemoveFalse;
	}
	
	public static String getLiftsignMaintainModifyPrivateRemoveDeny() {
		return liftsignMaintainModifyPrivateRemoveDeny;
	}
	
	public static String getLiftsignMaintainModifyPrivateUnknownUnknown() {
		return liftsignMaintainModifyPrivateUnknownUnknown;
	}
	
	public static String getLiftsignMaintainModifyPrivateUnknownDeny() {
		return liftsignMaintainModifyPrivateUnknownDeny;
	}
	
	public static String getLiftsignMaintainModifyOtherDeny() {
		return liftsignMaintainModifyOtherDeny;
	}
	
	public static String getLiftsignMaintainRemovePublicAllow() {
		return liftsignMaintainRemovePublicAllow;
	}
	
	public static String getLiftsignMaintainRemovePublicDeny() {
		return liftsignMaintainRemovePublicDeny;
	}
	
	public static String getLiftsignMaintainRemovePrivateAllow() {
		return liftsignMaintainRemovePrivateAllow;
	}
	
	public static String getLiftsignMaintainRemovePrivateDeny() {
		return liftsignMaintainRemovePrivateDeny;
	}
	
	public static String getLiftsignMaintainRemoveMultipleAllow() {
		return liftsignMaintainRemoveMultipleAllow;
	}
	
	public static String getLiftsignMaintainRemoveMultipleDeny() {
		return liftsignMaintainRemoveMultipleDeny;
	}
	
	public static String getLiftsignUseNoneDefault() {
		return liftsignUseNoneDefault;
	}
	
	public static String getLiftsignUseUpDefault() {
		return liftsignUseUpDefault;
	}
	
	public static String getLiftsignUseUpCustom() {
		return liftsignUseUpCustom;
	}
	
	public static String getLiftsignUseDownDefault() {
		return liftsignUseDownDefault;
	}
	
	public static String getLiftsignUseDownCustom() {
		return liftsignUseDownCustom;
	}
	
	public static String getLiftsignUseDenyPublic() {
		return liftsignUseDenyPublic;
	}
	
	public static String getLiftsignUseDenyPrivate() {
		return liftsignUseDenyPrivate;
	}
	
	public static String getLiftsignUseDisconnectedPublic() {
		return liftsignUseDisconnectedPublic;
	}
	
	public static String getLiftsignUseDisconnectedPrivate() {
		return liftsignUseDisconnectedPrivate;
	}
	
	public static String getLiftsignUseBlockedPublic() {
		return liftsignUseBlockedPublic;
	}
	
	public static String getLiftsignUseBlockedPrivate() {
		return liftsignUseBlockedPrivate;
	}
	
	public static String getLiftsignFileErrorSave() {
		return liftsignFileErrorSave;
	}
	
	public static String getLiftsignFileErrorDelete() {
		return liftsignFileErrorDelete;
	}
}
