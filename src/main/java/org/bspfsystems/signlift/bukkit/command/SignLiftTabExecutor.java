package org.bspfsystems.signlift.bukkit.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bspfsystems.signlift.bukkit.ChangeData;
import org.bspfsystems.signlift.bukkit.SignLiftPlugin;
import org.bspfsystems.signlift.bukkit.config.ConfigMessage;
import org.bspfsystems.signlift.bukkit.exception.SignLiftException;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the {@link CommandExecutor} and {@link TabCompleter} for the
 * {@link SignLiftPlugin}'s commands.
 */
public final class SignLiftTabExecutor implements TabExecutor {
    
    private static final String INTERNAL_ERROR = "§r§cInternal error, please try again. If the issue persists, please contact a server administrator.§r";
    private static final String NO_PERMISSION = "§r§cI'm sorry, but you do not have permission to perform this command. Please contact the server administrators if you believe that this is in error.§r";
    
    private final SignLiftPlugin signLiftPlugin;
    private final Logger logger;
    
    /**
     * Constructs a new {@link SignLiftTabExecutor}.
     * 
     * @param signLiftPlugin The {@link SignLiftPlugin}.
     */
    public SignLiftTabExecutor(@NotNull final SignLiftPlugin signLiftPlugin) {
        this.signLiftPlugin = signLiftPlugin;
        this.logger = this.signLiftPlugin.getLogger();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command command, @NotNull final String label, @NotNull final String[] args) {
    
        final String commandName = command.getName();
        if (!this.signLiftPlugin.getDescription().getCommands().containsKey(commandName)) {
            this.logger.log(Level.WARNING, "The command " + commandName + " was triggered in the SignLift plugin.");
            this.logger.log(Level.WARNING, "This command is not registered to SignLift.");
            return true;
        }
    
        if (!(sender instanceof Player)) {
            sender.sendMessage(ConfigMessage.getCommandDeny());
            return true;
        }
    
        final Player player = (Player) sender;
        final Server server = this.signLiftPlugin.getServer();
        final ArrayList<String> argsList = new ArrayList<String>(Arrays.asList(args));
    
        if (commandName.equalsIgnoreCase("signlift")) {
            if (argsList.isEmpty()) {
                return false;
            }
    
            final String subCommand = argsList.remove(0);
            if (subCommand.equalsIgnoreCase("reload")) {
    
                final PluginCommand sreloadCommand = server.getPluginCommand("sreload");
                if (sreloadCommand == null) {
                    this.logger.log(Level.WARNING, "/sreload command not registered. Possible compilation/build issue with the plugin.");
                    this.logger.log(Level.WARNING, "Cannot execute commandline: " + commandName + " : " + Arrays.toString(args));
                    player.sendMessage(SignLiftTabExecutor.INTERNAL_ERROR);
                    return true;
                }
                if (!sreloadCommand.testPermission(player)) {
                    return true;
                }
                if (argsList.size() != 1) {
                    player.sendMessage(sreloadCommand.getUsage());
                    return true;
                }
                return this.reloadCommand(player, argsList.remove(0));
            } else if (subCommand.equalsIgnoreCase("help")) {
    
                final PluginCommand shelpCommand = server.getPluginCommand("shelp");
                if (shelpCommand == null) {
                    this.logger.log(Level.WARNING, "/shelp command is not registered. Possible compilation/build issue with the plugin.");
                    this.logger.log(Level.WARNING, "Cannot execute commandline: " + commandName + " : " + Arrays.toString(args));
                    player.sendMessage(SignLiftTabExecutor.INTERNAL_ERROR);
                    return true;
                }
                if (!shelpCommand.testPermission(player)) {
                    return true;
                }
                if (!argsList.isEmpty()) {
                    player.sendMessage(shelpCommand.getUsage());
                    return true;
                }
                return this.helpCommand(player);
            } else if (subCommand.equalsIgnoreCase("info")) {
    
                final PluginCommand sinfoCommand = server.getPluginCommand("sinfo");
                if (sinfoCommand == null) {
                    this.logger.log(Level.WARNING, "/sinfo command not registered. Possible compilation/build issue with the plugin.");
                    this.logger.log(Level.WARNING, "Cannot execute commandline: " + commandName + " : " + Arrays.toString(args));
                    player.sendMessage(SignLiftTabExecutor.INTERNAL_ERROR);
                    return true;
                }
                if (!sinfoCommand.testPermission(player)) {
                    return true;
                }
                if (!argsList.isEmpty()) {
                    player.sendMessage(sinfoCommand.getUsage());
                    return true;
                }
                return this.infoCommand(player);
            } else if (subCommand.equalsIgnoreCase("modify")) {
    
                final PluginCommand smodifyCommand = server.getPluginCommand("smodify");
                if (smodifyCommand == null) {
                    this.logger.log(Level.WARNING, "/smodify command not registered. Possible compilation/build issue with the plugin.");
                    this.logger.log(Level.WARNING, "Cannot execute commandline: " + commandName + " : " + Arrays.toString(args));
                    player.sendMessage(SignLiftTabExecutor.INTERNAL_ERROR);
                    return true;
                }
                if (!smodifyCommand.testPermission(player)) {
                    return true;
                }
                if (argsList.isEmpty()) {
                    player.sendMessage(smodifyCommand.getUsage());
                    return true;
                }
                return this.modifyCommand(player, argsList, true);
            } else if (subCommand.equalsIgnoreCase("changeowner")) {
    
                final PluginCommand schangeownerCommand = server.getPluginCommand("schangeowner");
                if (schangeownerCommand == null) {
                    this.logger.log(Level.WARNING, "/schangeowner command not registered. Possible compilation/build issue with the plugin.");
                    this.logger.log(Level.WARNING, "Cannot execute commandline: " + commandName + " : " + Arrays.toString(args));
                    player.sendMessage(SignLiftTabExecutor.INTERNAL_ERROR);
                    return true;
                }
                if (!schangeownerCommand.testPermission(player)) {
                    return true;
                }
                if (argsList.size() != 1) {
                    player.sendMessage(schangeownerCommand.getUsage());
                    return true;
                }
                return this.changeOwnerCommand(player, argsList.remove(0));
            } else {
                return false;
            }
        } else if (commandName.equalsIgnoreCase("sreload")) {
            
            if (!this.checkShortCommand(player, commandName, args)) {
                return true;
            }
            if (argsList.size() != 1) {
                return false;
            }
            return this.reloadCommand(player, argsList.remove(0));
        } else if (commandName.equalsIgnoreCase("shelp")) {
        
            if (!this.checkShortCommand(player, commandName, args)) {
                return true;
            }
            if (!argsList.isEmpty()) {
                return false;
            }
            return this.helpCommand(player);
        } else if (commandName.equalsIgnoreCase("sinfo")) {
            
            if (!this.checkShortCommand(player, commandName, args)) {
                return true;
            }
            if (!argsList.isEmpty()) {
                return false;
            }
            return this.infoCommand(player);
        } else if (commandName.equalsIgnoreCase("smodify")) {
            
            if (!this.checkShortCommand(player, commandName, args)) {
                return true;
            }
            if (argsList.isEmpty()) {
                return false;
            }
            return this.modifyCommand(player, argsList, false);
        } else if (commandName.equalsIgnoreCase("schangeowner")) {
            
            if (!this.checkShortCommand(player, commandName, args)) {
                return true;
            }
            if (argsList.size() != 1) {
                return false;
            }
            return this.changeOwnerCommand(player, argsList.remove(0));
        } else {
            this.logger.log(Level.WARNING, "The command " + commandName + " was triggered in the SignLift plugin.");
            this.logger.log(Level.WARNING, "This command is not registered to SignLift.");
            this.logger.log(Level.WARNING, "This command passed the earlier inspection undetected.");
            return true;
        }
    }
    
    /**
     * Performs common checks for the short versions of the {@code /signlift}
     * commands ({@code null} and permissions checks).
     * 
     * @param player The {@link Player} executing the command.
     * @param commandName The name of the actual command being executed (the
     *                    short command).
     * @param args The command-line arguments.
     * @return {@code true} if the checks passed, {@code false} otherwise.
     */
    private boolean checkShortCommand(@NotNull final Player player, @NotNull final String commandName, @NotNull final String[] args) {
        final PluginCommand signliftCommand = this.signLiftPlugin.getServer().getPluginCommand("signlift");
        if (signliftCommand == null) {
            this.logger.log(Level.WARNING, "/signlift command not registered. Possible compilation/build issue with the plugin.");
            this.logger.log(Level.WARNING, "Cannot execute commandline: " + commandName + " : " + Arrays.toString(args));
            player.sendMessage(SignLiftTabExecutor.INTERNAL_ERROR);
            return false;
        }
        return signliftCommand.testPermission(player);
    }
    
    /**
     * Performs the main functionality of the reload command.
     * 
     * @param player The {@link Player} executing the command.
     * @param resource The resource to reload.
     * @return {@code true} if command execution was successful, {@code false}
     *         otherwise.
     */
    private boolean reloadCommand(@NotNull final Player player, @NotNull final String resource) {
        
        if (resource.equalsIgnoreCase("config")) {
            this.signLiftPlugin.reloadConfig(player);
            return true;
        } else if (resource.equalsIgnoreCase("messages")) {
            this.signLiftPlugin.reloadMessages(player);
            return true;
        } else {
            player.sendMessage("§r§6" + resource + "§r §cis not a valid resource to reload.§r");
            return false;
        }
    }
    
    /**
     * Performs the main functionality of the help command.
     * 
     * @param player The {@link Player} executing the command.
     * @return {@code true} if command execution was successful, {@code false}
     *         otherwise.
     */
    private boolean helpCommand(@NotNull final Player player) {
        
        player.sendMessage("§r§8================================================§r");
        player.sendMessage("§r§6SignLift Help§r");
        player.sendMessage("§r§8------------------------------------------------§r");
        
        final Server server = this.signLiftPlugin.getServer();
        final ArrayList<String> allowedCommands = new ArrayList<String>();
        
        final PluginCommand sreloadCommand = server.getPluginCommand("sreload");
        if (sreloadCommand != null && sreloadCommand.testPermissionSilent(player)) {
            allowedCommands.add("§r§a/signlift reload§r §b<config|messages>§r");
            allowedCommands.add("§r§a/sreload§r §b<config|messages>§r");
        }
        
        final PluginCommand shelpCommand = server.getPluginCommand("shelp");
        if (shelpCommand != null && shelpCommand.testPermissionSilent(player)) {
            allowedCommands.add("§r§a/signlift help§r");
            allowedCommands.add("§r§a/shelp§r");
        }
        
        final PluginCommand sinfoCommand = server.getPluginCommand("sinfo");
        if (sinfoCommand != null && sinfoCommand.testPermissionSilent(player)) {
            allowedCommands.add("§r§a/signlift info§r");
            allowedCommands.add("§r§a/sinfo§r");
        }
        
        final PluginCommand smodifyCommand = server.getPluginCommand("smodify");
        if (smodifyCommand != null && smodifyCommand.testPermissionSilent(player)) {
            allowedCommands.add("§r§a/signlift modify§r §b§o<player> [player...]§r");
            allowedCommands.add("§r§a/smodify§r §b§o<player> [player...]§r");
        }
        
        final PluginCommand schangeownerCommand = server.getPluginCommand("schangeowner");
        if (schangeownerCommand != null && schangeownerCommand.testPermissionSilent(player)) {
            allowedCommands.add("§r§a/signlift changeowner§r §b§o<player>§r");
            allowedCommands.add("§r§a/schangeowner§r §b§o<player>§r");
        }
        
        if (allowedCommands.isEmpty()) {
            player.hasPermission("§r§cNo commands.§r");
            player.sendMessage("§r§8================================================§r");
            return true;
        }
        
        for (final String allowedCommand : allowedCommands) {
            player.sendMessage("§r§f -§r §a" + allowedCommand + "§r");
        }
        
        player.sendMessage("§r§8================================§r");
        return true;
    }
    
    private boolean infoCommand(final Player player) {
        
        player.sendMessage(ConfigMessage.getCommandInfo());
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
        
        final ChangeData changeData = new ChangeData();
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
        player.sendMessage(ConfigMessage.getCommandModify());
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
        player.sendMessage(ConfigMessage.getCommandModify());
        return true;
    }
}
