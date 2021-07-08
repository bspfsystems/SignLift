# SignLift

A plugin for Minecraft Bukkit servers that enables signs to become lifts (think instant elevators).

## Download

You can download the latest version of the plugin from [here](https://github.com/bspfsystems/SignLift/releases/latest/).

The latest release is 2.1.1.

## Build from Source

SignLift uses [Apache Maven](https://maven.apache.org/) to build and handle dependencies.

### Requirements

- Java Development Kit (JDK) 8 or higher
- Git
- Apache Maven

### Compile / Build

Run the following commands to build the plugin:
```
git clone https://github.com/bspfsystems/SignLift.git
cd SignLift/
mvn clean install
```

The `.jar` file will be located in the `target/` folder for the Bukkit plugin.

## Installation

Simply drop the appropriate file into the `plugins/` folder for your Bukkit installation, and then (re-)start the server.

The currently-supported versions of Bukkit are:
- 1.16.4
- 1.16.5

_Please Note: This plugin may work with other versions of Bukkit, but is not guaranteed to._

### Configuration

A default configuration file (`config.yml`) will be created in the plugin's data folder when you start the server for the first time after installing the plugin. You can then edit the configuration file as needed, and then run the reload command to reload the configuration file:
- `/signlift reload`

When new releases of the plugin are made available, the default configuration file may update; however, the configuration file in the plugin's data folder will not be updated. While we try not to change the configuration file, sometimes it is unavoidable. You may obtain an up-to-date version of the default file from [here](https://bspfsystems.org/config-files/signlift/). You can simply drop the updated file in place of the old one and update the values to reflect your requirements and/or previous settings. You can then run the reload command in-game to load the updated configuration.

The SignLift plugin will accept alternative names for its configuration file, if the default `config.yml` is confusing to keep track of (configuration files will be in the plugin's data folder). The plugin will also accept `signlift.yml` as a configuration file name. More information can be found at the top of the default configuration file (can be viewed [here](https://bspfsystems.org/config-files/signlift/)).

#### LiftSigns

There are 2 main types of LiftSigns: Public, and Private. Public lift signs will have no saved configuration. Private lift signs, on the other hand, will generate a configuration file with the following data:
- The Location (World, X, Y, Z, pitch, and yaw) of the LiftSign.
- The UUID of the owner (creator) of the LiftSign.
- A List of UUIDs of any Players that are allowed to use the LiftSign (members).
- An additional List of UUIDs of any Players that can add/remove members to/from the LiftSign (admins).

The Private LiftSign files will be stored in a folder named `PrivateLiftSigns` within the plugin's main data folder. The files will be named `<world>-<x coord>-<y coord>-<z coord>.yml`, where the coordinates are the respective integer coordinates of the Block that the LiftSign occupies. If the particular coordinate is negative, then `n` will come before the coordinate to differentiate between a negative coordinate and the separator `-`.

Examples:
- `world-4-64-4.yml` is for the Private LiftSign located in World `world` at (4,64,4).
- `testing-n84-73-328.yml` is for the Private LiftSign located in World `testing` at (-84,73,328).

#### PlayerData

PlayerData files hold Name-UUID mappings for any Player that has previously logged into the server since the plugin installation. These mappings do not update when a Player changes their name; they will only update when that Player next logs into the server.

The mappings allow for quick translations between a Player's name and their UUID, for when someone wants to add a Player to their LiftSign as a member or an admin. These configuration files will contain the following data:
- The UUID of the Player.
- The last-known name of the Player.

The PlayerData files will be stored in a folder named `PlayerData` within the plugin's main data folder. The files will be named `<uuid>.yml`, with `<uuid>` being the UUID of the Player.

## In-Game Usage / Commands & Permissions

The main purpose of SignLift is to provide a means of traversing builds vertically, much like how ladders would allow. However, SignLift allows this to be done much more quickly, with less resources used (no need to build a 100-block-high ladder).

While most of the time, no commands will be needed for LiftSigns (Public is the default), some functionality for Private LiftSigns require the use of commands:

**Base SignLift Command:** The base command for all SignLift commands. If this command has no arguments, a list of all subcommands that the sender has permission to use, and their respective syntax, will be displayed. **Please Note:** This permission **MUST** be granted to all that wish to use any SignLift subcommand (or any short commands).
- `/signlift` - `signlift.command.signlift`

**Reload Command:** Reloads the configuration file, updating any values for the in-game lift functionality **Please Note:** Changing these values can/will cause current LiftSigns to no longer function.
- `/signlift reload` - `signlift.command.signlift.reload`

**Help Command:** Displays all available SignLift subcommands that the Player has permission to use.
- `/signlift help` - `signlift.command.signlift.help`

**Info Command:** When used, a Player can then punch a LiftSign, and see detailed information about the LiftSign, such as Public or Private, and any owner, admins, or members, if applicable.
- `/signlift info` - `signlift.command.signlift.info`

**Modify Command:** When used, a Player can edit the members and admins on a Private LiftSign that they have permission to edit. Owners can add and remove members and admins, while admins can add and remove members.
- `/signlift modify <args...>` - `signlift.command.signlift.modify`

**ChangeOwner Command:** When used, a Player that owns a Private LiftSign may change the owner to be a different Player, thereby giving up any ownership rights.
- `/signlift changowner <player>` - `signlift.command.signlift.changeowner`

### Shortened Commands

These commands are the shorthand versions of the main commands. **Please Note:** The Base SignLift Command permission must be applied to use any of these commands.

**Short Reload Command:** Shortened version of the Reload Command, same permission.
- `/sreload`

**Short Help Command:** Shortened version of the Help Command, same permission.
- `/shelp`

**Short Info Command:** Shortened version of the Info Command, same permission.
- `/sinfo`

**Short Modify Command:** Shortened version of the Modify Command, same permission.
- `/smodify <args...>`

**Short ChangeOwner Command:** Shortened version of the ChangeOwner Command, same permission.
- `/schangeowner <player>`

### Permission Nodes

A full list of permissions can be viewed in the [plugin.yml](src/main/resources/plugin.yml) file. SignLift's permissions can work with or without a separate permission plugin installed.

## Contributing

### Pull Requests

Contributions to the project are welcome. SignLift is a free and open source software project, created in the hopes that the community would find ways to improve it. If you make any improvements or other enhancements to SignLift, we ask that you submit a Pull Request to merge the changes back upstream. We would enjoy the opportunity to give those improvements back to the wider community.

Various types of contributions are welcome, including (but not limited to):
- Security updates / patches
- Bug fixes
- Feature enhancements

We reserve the right to not include a contribution in the project if the contribution does not add anything substantive or otherwise reduces the functionality of SignLift in a non-desirable way. That said, the idea of having free and open source software was that contributions would be accepted, and discussions over a potential contribution are welcome.

For licensing questions, please see the Licensing section.

### Project Layout

SignLift somewhat follows the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html). This is not the definitive coding style of the project. Generally, it is best to try to copy the style of coding found in the class that you are editing.

## Support / Issues

Issues can be reported [here in GitHub](https://github.com/bspfsystems/SignLift/issues/).

### First Steps

Before creating an issue, please search to see if anyone else has reported the same issue. Don't forget to search the closed issues. It is much easier for us (and will get you a faster response) to handle a single issue that affects multiple users than it is to have to deal with duplicates.

There is also a chance that your issue has been resolved previously. In this case, you can (ideally) find the answer to your problem without having to ask (new version of SignLift, configuration update, etc).

### Creating an Issue

If no one has reported the issue previously, or the solution is not apparent, please open a new issue. When creating the issue, please give it a descriptive title (no "It's not working", please), and put as much detail into the description as possible. The more details you add, the easier it becomes for us to solve the issue. Helpful items may include:
- A descriptive title for the issue
- The version of SignLift you are using
- The version of Minecraft you are using
- The Bukkit implementation you are using (CraftBukkit / Spigot / Paper / etc.)
- Logs and/or stack traces
- Any steps to reproducing the issue
- Anything else that might be helpful in solving your issue.

_Note:_ Please redact any Personally-Identifiable Information (PII) when you create your issue. These may appear in logs or stack traces. Examples include (but are not limited to):
- Real names of players / server administrators
- Usernames of accounts on computers (may appear in logs or stack traces)
- IP addresses / hostnames
- etc.

If you are not sure, you can always redact or otherwise change the data.

### Non-Acceptable Issues

Issues such as "I need help" or "It doesn't work" will not be addressed and/or will be closed with no assistance given. These type of issues do not have any meaningful details to properly address the problem. Other issues that will not be addressed and/or closed without help include (but are not limited to):
- How to install SignLift (explained in README)
- How to configure SignLift (explained in README and default configuration)
- How to create plugins
- How to set up a development environment
- How to install plugins
- How to create a server
- Other issues of similar nature...

This is not a help forum for server administration or non-project-related coding issues. Other resources, such as [Google](https://www.google.com/), should have answers to most questions not related to SignLift.

## Licensing

SignLift uses the following licenses:
- [The GNU General Public License, Version 3](https://www.gnu.org/licenses/gpl-3.0.en.html)

### Contributions & Licensing

Contributions to the project will remain licensed under the GPLv3 license, as defined by this particular license. Copyright/ownership of the contributions shall be governed by the license. The use of an open source license in the hopes that contributions to the project will have better clarity on legal rights of those contributions.

_Please Note: This is not legal advice. If you are unsure on what your rights are, please consult a lawyer._