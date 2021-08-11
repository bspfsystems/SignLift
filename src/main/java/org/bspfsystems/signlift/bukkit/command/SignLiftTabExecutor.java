package org.bspfsystems.signlift.bukkit.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
        final List<String> argsList = Arrays.asList(args);
    
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
                return this.modifyCommand(player, argsList);
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
            return this.modifyCommand(player, argsList);
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
            player.sendMessage("§r §f-§r §a" + allowedCommand + "§r");
        }
        
        player.sendMessage("§r§8================================§r");
        return true;
    }
    
    /**
     * Performs the main functionality of the info command.
     *
     * @param player The {@link Player} executing the command.
     * @return {@code true} if command execution was successful, {@code false}
     *         otherwise.
     */
    private boolean infoCommand(@NotNull final Player player) {
        player.sendMessage(ConfigMessage.getCommandInfo());
        this.signLiftPlugin.addPendingInformation(player.getUniqueId());
        return true;
    }
    
    /**
     * Performs the main functionality of the modify command.
     *
     * @param player The {@link Player} executing the command.
     * @param argsList The {@link List} of the command-line arguments (with the
     *                 already-parsed args removed).
     * @return {@code true} if command execution was successful, {@code false}
     *         otherwise.
     */
    private boolean modifyCommand(@NotNull final Player player, @NotNull final List<String> argsList) {
        
        final ChangeData changeData = new ChangeData();
        for (final String arg : argsList) {
            
            if (arg.startsWith("@")) {
                final String name = arg.substring(1);
                final UUID uniqueId = this.signLiftPlugin.getUniqueId(name);
                if (uniqueId == null) {
                    changeData.addUnknown(name);
                    continue;
                }
                
                try {
                    if (!changeData.addAdmin(uniqueId)) {
                        player.sendMessage("§r§6Warning: You have specified§r §b" + name + "§r §6multiple times. They will only be added as an admin once.§r");
                    }
                } catch (SignLiftException e) {
                    player.sendMessage("§r§c" + e.getMessage() + "§r");
                    return true;
                }
            } else if (arg.startsWith("-")) {
                final String name = arg.substring(1);
                final UUID uniqueId = this.signLiftPlugin.getUniqueId(name);
                if (uniqueId == null) {
                    changeData.addUnknown(name);
                    continue;
                }
                
                try {
                    if (!changeData.remove(uniqueId)) {
                        player.sendMessage("§r§6Warning: You have specified§r §b" + name + "§r §6multiple times. They will only be removed once.§r");
                    }
                } catch (SignLiftException e) {
                    player.sendMessage("§r§c" + e.getMessage() + "§r");
                    return true;
                }
            } else {
                final UUID uniqueId = this.signLiftPlugin.getUniqueId(arg);
                if (uniqueId == null) {
                    changeData.addUnknown(arg);
                    continue;
                }
                
                try {
                    if (!changeData.addMember(uniqueId)) {
                        player.sendMessage("§r§6Warning: You have specified§r §b" + arg + "§r §6multiple times. They will only be added as a member once.§r");
                    }
                } catch (SignLiftException e) {
                    player.sendMessage("§r§c" + e.getMessage() + "§r");
                    return true;
                }
            }
        }
        
        this.signLiftPlugin.addPendingModification(player.getUniqueId(), changeData);
        player.sendMessage(ConfigMessage.getCommandModify());
        return true;
    }
    
    /**
     * Performs the main functionality of the changeowner command.
     *
     * @param player The {@link Player} executing the command.
     * @param ownerName The name of the {@link Player} that is to be the new
     *                  owner.
     * @return {@code true} if command execution was successful, {@code false}
     *         otherwise.
     */
    private boolean changeOwnerCommand(@NotNull final Player player, @NotNull final String ownerName) {
        this.signLiftPlugin.addPendingModification(player.getUniqueId(), new ChangeData(this.signLiftPlugin.getUniqueId(ownerName)));
        player.sendMessage(ConfigMessage.getCommandModify());
        return true;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public List<String> onTabComplete(@NotNull final CommandSender sender, @NotNull final Command command, @NotNull final String label, @NotNull final String[] args) {
    
        final String commandName = command.getName();
        if (!this.signLiftPlugin.getDescription().getCommands().containsKey(commandName)) {
            this.logger.log(Level.WARNING, "The command " + commandName + " was triggered for tab completion in the SignLift plugin.");
            this.logger.log(Level.WARNING, "This command is not registered to SignLift.");
            return Collections.emptyList();
        }
    
        if (!(sender instanceof Player)) {
            sender.sendMessage(ConfigMessage.getCommandDeny());
            return Collections.emptyList();
        }
        
        final Player player = (Player) sender;
        final Server server = this.signLiftPlugin.getServer();
        final List<String> argsList = Arrays.asList(args);
        final List<String> completions = new ArrayList<String>();
        
        if (commandName.equalsIgnoreCase("signlift")) {
            boolean foundError = false;
    
            final PluginCommand sreloadCommand = server.getPluginCommand("sreload");
            if (sreloadCommand == null) {
                this.logger.log(Level.WARNING, "/sreload command not registered. Possible compilation/build issue with the plugin.");
                this.logger.log(Level.WARNING, "Cannot execute commandline: " + commandName + " : " + Arrays.toString(args));
                foundError = true;
            } else if (sreloadCommand.testPermissionSilent(player)) {
                completions.add("reload");
            }
    
            final PluginCommand shelpCommand = server.getPluginCommand("shelp");
            if (shelpCommand == null) {
                this.logger.log(Level.WARNING, "/shelp command not registered. Possible compilation/build issue with the plugin.");
                this.logger.log(Level.WARNING, "Cannot execute commandline: " + commandName + " : " + Arrays.toString(args));
                foundError = true;
            } else if (shelpCommand.testPermissionSilent(player)) {
                completions.add("help");
            }
    
            final PluginCommand sinfoCommand = server.getPluginCommand("sinfo");
            if (sinfoCommand == null) {
                this.logger.log(Level.WARNING, "/sinfo command not registered. Possible compilation/build issue with the plugin.");
                this.logger.log(Level.WARNING, "Cannot execute commandline: " + commandName + " : " + Arrays.toString(args));
                foundError = true;
            } else if (sinfoCommand.testPermissionSilent(player)) {
                completions.add("info");
            }
    
            final PluginCommand smodifyCommand = server.getPluginCommand("smodify");
            if (smodifyCommand == null) {
                this.logger.log(Level.WARNING, "/smodify command not registered. Possible compilation/build issue with the plugin.");
                this.logger.log(Level.WARNING, "Cannot execute commandline: " + commandName + " : " + Arrays.toString(args));
                foundError = true;
            } else if (smodifyCommand.testPermissionSilent(player)) {
                completions.add("modify");
            }
    
            final PluginCommand schangeownerCommand = server.getPluginCommand("schangeowner");
            if (schangeownerCommand == null) {
                this.logger.log(Level.WARNING, "/schangeowner command not registered. Possible compilation/build issue with the plugin.");
                this.logger.log(Level.WARNING, "Cannot execute commandline: " + commandName + " : " + Arrays.toString(args));
                foundError = true;
            } else if (schangeownerCommand.testPermissionSilent(player)) {
                completions.add("changeowner");
            }
            
            if (argsList.isEmpty()) {
                if (foundError) {
                    player.sendMessage(SignLiftTabExecutor.INTERNAL_ERROR);
                }
                return completions;
            }
            
            final String subCommand = argsList.remove(0);
            if (argsList.isEmpty()) {
                completions.removeIf(completion -> !completion.toLowerCase().startsWith(subCommand.toLowerCase()));
                return completions;
            }
            
            completions.clear();
            if (subCommand.equalsIgnoreCase("reload")) {
                return this.getReloadSuggestions(player, argsList);
            } else if (subCommand.equalsIgnoreCase("help")) {
                return Collections.emptyList();
            } else if (subCommand.equalsIgnoreCase("info")) {
                return Collections.emptyList();
            } else if (subCommand.equalsIgnoreCase("modify")) {
                return this.getPlayerSuggestions(player, argsList, false);
            } else if (subCommand.equalsIgnoreCase("changeowner")) {
                return this.getPlayerSuggestions(player, argsList, true);
            } else {
                return Collections.emptyList();
            }
        } else if (commandName.equalsIgnoreCase("sreload")) {
    
            final PluginCommand sreloadCommand = server.getPluginCommand("sreload");
            if (sreloadCommand == null) {
                this.logger.log(Level.WARNING, "/sreload command not registered. Possible compilation/build issue with the plugin.");
                this.logger.log(Level.WARNING, "Cannot execute commandline: " + commandName + " : " + Arrays.toString(args));
                player.sendMessage(SignLiftTabExecutor.INTERNAL_ERROR);
                return Collections.emptyList();
            }
            return this.getReloadSuggestions(player, argsList);
        } else if (commandName.equalsIgnoreCase("shelp")) {
            
            final PluginCommand shelpCommand = server.getPluginCommand("shelp");
            if (shelpCommand == null) {
                this.logger.log(Level.WARNING, "/shelp command not registered. Possible compilation/build issue with the plugin.");
                this.logger.log(Level.WARNING, "Cannot execute commandline: " + commandName + " : " + Arrays.toString(args));
                player.sendMessage(SignLiftTabExecutor.INTERNAL_ERROR);
            }
            return Collections.emptyList();
        } else if (commandName.equalsIgnoreCase("sinfo")) {
    
            final PluginCommand sinfoCommand = server.getPluginCommand("shelp");
            if (sinfoCommand == null) {
                this.logger.log(Level.WARNING, "/sinfo command not registered. Possible compilation/build issue with the plugin.");
                this.logger.log(Level.WARNING, "Cannot execute commandline: " + commandName + " : " + Arrays.toString(args));
                player.sendMessage(SignLiftTabExecutor.INTERNAL_ERROR);
            }
            return Collections.emptyList();
        } else if (commandName.equalsIgnoreCase("smodify")) {
    
            final PluginCommand smodifyCommand = server.getPluginCommand("smodify");
            if (smodifyCommand == null) {
                this.logger.log(Level.WARNING, "/smodify command not registered. Possible compilation/build issue with the plugin.");
                this.logger.log(Level.WARNING, "Cannot execute commandline: " + commandName + " : " + Arrays.toString(args));
                player.sendMessage(SignLiftTabExecutor.INTERNAL_ERROR);
                return Collections.emptyList();
            }
            return this.getPlayerSuggestions(player, argsList, false);
        } else if (commandName.equalsIgnoreCase("schangeowner")) {
    
            final PluginCommand schangeownerCommand = server.getPluginCommand("schangeowner");
            if (schangeownerCommand == null) {
                this.logger.log(Level.WARNING, "/schangeowner command not registered. Possible compilation/build issue with the plugin.");
                this.logger.log(Level.WARNING, "Cannot execute commandline: " + commandName + " : " + Arrays.toString(args));
                player.sendMessage(SignLiftTabExecutor.INTERNAL_ERROR);
                return Collections.emptyList();
            }
            return this.getPlayerSuggestions(player, argsList, true);
        } else {
            return Collections.emptyList();
        }
    }
    
    /**
     * Gets the tab-completion suggestions for the reload command.
     *
     * @param player The {@link Player} triggering the tab-completion.
     * @param argsList The {@link List} of arguments given, if any.
     * @return The {@link List} of tab-completions that will be given to the
     *         {@link Player}.
     */
    @NotNull
    private List<String> getReloadSuggestions(@NotNull final Player player, @NotNull final List<String> argsList) {
        
        final List<String> completions = new ArrayList<String>();
        
        if (player.hasPermission("signlift.command.signlift.reload.config")) {
            completions.add("config");
        }
        if (player.hasPermission("signlift.command.signlift.reload.messages")) {
            completions.add("messages");
        }
        
        if (argsList.isEmpty()) {
            return completions;
        }
        
        final String reloadType = argsList.remove(0);
        if (argsList.isEmpty()) {
            completions.removeIf(completion -> !completion.toLowerCase().startsWith(reloadType.toLowerCase()));
            return completions;
        }
        
        return Collections.emptyList();
    }
    
    /**
     * Gets the suggestions for players for the modify and changeowner commands.
     *
     * @param player The {@link Player} triggering the tab-completion.
     * @param argsList The command-line arguments, minus the (sub-)command(s).
     * @param onlyUseFirst If {@code true}, then only the 1st argument will be
     *                     considered, and all others will warrant no returned
     *                     tab-completions. If {@code false}, all args will
     *                     warrant possible tab-completions.
     * @return The {@link List} of possible {@link Player} names to use as
     *         tab-completions.
     */
    @NotNull
    private List<String> getPlayerSuggestions(@NotNull final Player player, @NotNull final List<String> argsList, final boolean onlyUseFirst) {
        
        final List<String> completions = new ArrayList<String>();
        for (final Player onlinePlayer : this.signLiftPlugin.getServer().getOnlinePlayers()) {
            completions.add(onlinePlayer.getName());
        }
        
        for (final String name : this.signLiftPlugin.getAllNames()) {
            if (!completions.contains(name)) {
                completions.add(name);
            }
        }
        
        if (argsList.isEmpty()) {
            return completions;
        }
        
        final String lastPlayer = argsList.remove(0);
        final String prefix;
        if (lastPlayer.startsWith("@")) {
            prefix = "@";
        } else if (lastPlayer.startsWith("-")) {
            prefix = "-";
        } else {
            prefix = "";
        }
        
        for (int index = 0; index < completions.size(); index++) {
            completions.set(index, prefix + completions.get(index));
        }
    
        completions.removeIf(s -> !s.toLowerCase().startsWith(lastPlayer.toLowerCase()));
        return completions;
    }
}
