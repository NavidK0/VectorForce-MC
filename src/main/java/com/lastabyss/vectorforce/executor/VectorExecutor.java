package com.lastabyss.vectorforce.executor;

import com.lastabyss.vectorforce.VectorForce;
import com.lastabyss.vectorforce.game.Motivator;
import com.lastabyss.vectorforce.game.SpecialRound;
import com.lastabyss.vectorforce.game.VectorForceGame;
import com.lastabyss.vectorforce.generator.RoadTable;
import com.lastabyss.vectorforce.generator.VectorForceGenerator;
import com.lastabyss.vectorforce.map.Road;
import com.lastabyss.vectorforce.util.Util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Navid
 */
public class VectorExecutor implements CommandExecutor {

    private VectorForce plugin;

    public VectorExecutor(VectorForce plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("vectorforce")) {
            if (args.length == 0) {
                Util.sendHelpMenu(sender);
            } else {
                if (args.length > 0) {
                    if (args[0].equalsIgnoreCase("playSound")) {
                        if (!sender.hasPermission("vf.admin")) {
                            sender.sendMessage(ChatColor.RED + "You don't have the permissions for that!");
                            return true;
                        }
                        if (args.length == 2) {
                            if (!(sender instanceof Player)) {
                                sender.sendMessage(Util.colorize("You must be a player to use these commands!"));
                            }
                            plugin.getAudioSystem().playMusic((Player) sender, args[1]);
                        } else {
                            Player p = Bukkit.getPlayer(args[2]);
                            if (p != null) {
                                plugin.getAudioSystem().playMusic(Bukkit.getPlayer(args[2]), args[1]);
                            } else {
                                sender.sendMessage(Util.colorize("Player is offline!"));
                            }
                        }
                        return true;
                    } else if (args[0].equalsIgnoreCase("stopSounds")) {
                        if (!sender.hasPermission("vf.admin")) {
                            sender.sendMessage(ChatColor.RED + "You don't have the permissions for that!");
                            return true;
                        }
                        if (args.length == 2) {
                            if (!(sender instanceof Player)) {
                                sender.sendMessage(Util.colorize("You must be a player to use these commands!"));
                            }
                            plugin.getAudioSystem().stopSounds((Player) sender);
                        } else {
                            Player p = Bukkit.getPlayer(args[2]);
                            if (p != null) {
                                plugin.getAudioSystem().stopSounds(Bukkit.getPlayer(args[2]));
                            } else {
                                sender.sendMessage(Util.colorize("Player is offline!"));
                            }
                        }
                        return true;
                    } else if (args[0].equalsIgnoreCase("rotinfo") || args[0].equalsIgnoreCase("ri")) {
                        if (!sender.hasPermission("vf.admin")) {
                            sender.sendMessage(ChatColor.RED + "You don't have the permissions for that!");
                            return true;
                        }
                        if (!(sender instanceof Player)) {
                            sender.sendMessage(Util.colorize("You must be a player to use these commands!"));
                        }
                        if (plugin.getMapManager().getWorld() == null) {
                            sender.sendMessage(Util.colorize("&4Not available, world isn't loaded!"));
                            return true;
                        }
                        Player player = (Player) sender;
                        if (!player.getWorld().getName().equals(plugin.getMapManager().getWorld().getName())) {
                            sender.sendMessage(Util.colorize("&4You're not in the VF world!"));
                            return true;
                        }
                        VectorForceGenerator generator = (VectorForceGenerator) plugin.getMapManager().getWorld().getGenerator();
                        RoadTable roadTable = generator.getRoadTable();
                        Chunk chunk = player.getLocation().getChunk();
                        Pair<Road, Integer> pair = roadTable.calculateType(chunk.getX(), chunk.getZ(), plugin.getMapManager().getTheme().getName());
                        Road road = pair.getLeft();
                        int rotate = pair.getRight();
                        if (road == null) {
                            sender.sendMessage(ChatColor.GREEN + "There is no road in this chunk!");
                            return true;
                        }
                        sender.sendMessage(ChatColor.GREEN + "Road: " + road.getType().toString() + " Rotation: " + rotate);
                        return true;
                    }  else if (args[0].equalsIgnoreCase("specialround") | args[0].equalsIgnoreCase("sr")) {
                        if (!sender.hasPermission("vf.admin")) {
                            sender.sendMessage(ChatColor.RED + "You don't have the permissions for that!");
                            return true;
                        }
                        if (plugin.getHandler().getGame() != null && args.length == 2) {
                            plugin.getHandler().getGame().setSpecialRoundType(SpecialRound.Type.valueOf(args[1].toUpperCase()));
                        } else if (plugin.getHandler().getGame() != null) {
                            plugin.getHandler().getGame().setSpecialRoundEnabled(true);
                        }
                        sender.sendMessage(Util.PREFIX + ChatColor.GREEN + "Set the specialround!");
                    } else if (args[0].equalsIgnoreCase("motivator") | args[0].equalsIgnoreCase("m")) {
                        if (!sender.hasPermission("vf.admin")) {
                            sender.sendMessage(ChatColor.RED + "You don't have the permissions for that!");
                            return true;
                        }
                        if (plugin.getHandler().getGame() != null && args.length == 2) {
                            plugin.getHandler().getGame().setMotivatorType(Motivator.Type.valueOf(args[1].toUpperCase()));
                        } else if (plugin.getHandler().getGame() != null) {
                            plugin.getHandler().getGame().setMotivatorEnabled(true);
                        }
                        sender.sendMessage(Util.PREFIX + ChatColor.GREEN + "Set the motivator!");
                    } else if (args[0].equalsIgnoreCase("vote")) {
                        if (!sender.hasPermission("vf.user")) {
                            sender.sendMessage(ChatColor.RED + "You don't have the permissions for that!");
                            return true;
                        }
                        if (plugin.getHandler().getGame() != null && plugin.getHandler().getGame().getState() == VectorForceGame.State.WAITING) {
                            if (sender instanceof Player) {
                                Util.showVoteMenu(((Player) sender));
                            } else {
                                sender.sendMessage(Util.PREFIX + ChatColor.RED + "You must be a player in order to use this command!");
                            }
                        } else {
                            sender.sendMessage(Util.PREFIX + ChatColor.RED + "Sorry, you can't vote right now!");
                        }
                    } else if (args[0].equalsIgnoreCase("tokens")) {
                        if (!sender.hasPermission("vf.user")) {
                            sender.sendMessage(ChatColor.RED + "You don't have the permissions for that!");
                            return true;
                        }
                        if (args.length == 1) {
                            if (sender instanceof Player) {
                                Player p = (Player) sender;
                                Util.async(() -> {
                                    try {
                                        ResultSet tokens = plugin.getSQL().getTokens(p.getUniqueId());
                                        int bling = tokens.next() ? tokens.getInt("tokens") : 0;
                                        sender.sendMessage(ChatColor.AQUA + "You currently have " + ChatColor.BLUE + bling + ChatColor.AQUA + (bling == 1 ? " token!" : " tokens!"));
                                    } catch (SQLException ex) {
                                        Logger.getLogger(VectorExecutor.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                });
                            } else {
                                sender.sendMessage(Util.PREFIX + ChatColor.RED + "You must be a player in order to use this command, or specify a player name!");
                            }
                        } else if (args.length > 1) {
                            if (sender instanceof Player) {
                                Util.async(() -> {
                                    try {
                                        ResultSet tokens = plugin.getSQL().getTokens(Bukkit.getOfflinePlayer(args[1]).getUniqueId());
                                        int bling = tokens.next() ? tokens.getInt("tokens") : 0;
                                        sender.sendMessage(ChatColor.AQUA + args[1] + " currently has " + ChatColor.BLUE + bling + ChatColor.AQUA + (bling == 1 ? " token!" : " tokens!"));
                                    } catch (SQLException ex) {
                                        Logger.getLogger(VectorExecutor.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                });
                            } else {
                                sender.sendMessage(Util.PREFIX + ChatColor.RED + "You must be a player in order to use this command, or specify a player name!");
                            }
                        }
                    } else if (args[0].equalsIgnoreCase("wins")) {
                        if (!sender.hasPermission("vf.user")) {
                            sender.sendMessage(ChatColor.RED + "You don't have the permissions for that!");
                            return true;
                        }
                        if (args.length == 1) {
                            if (sender instanceof Player) {
                                Player p = (Player) sender;
                                Util.async(() -> {
                                    try {
                                        ResultSet wins = plugin.getSQL().getWins(p.getUniqueId());
                                        int bling = wins.next() ? wins.getInt("wins") : 0;
                                        sender.sendMessage(ChatColor.AQUA + "You currently have " + ChatColor.BLUE + bling + ChatColor.AQUA + (bling == 1 ? " win!" : " wins!"));
                                    } catch (SQLException ex) {
                                        Logger.getLogger(VectorExecutor.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                });
                            } else {
                                sender.sendMessage(Util.PREFIX + ChatColor.RED + "You must be a player in order to use this command, or specify a player name!");
                            }
                        } else if (args.length > 1) {
                            if (sender instanceof Player) {
                                Util.async(() -> {
                                    try {
                                        ResultSet wins = plugin.getSQL().getWins(Bukkit.getOfflinePlayer(args[1]).getUniqueId());
                                        int bling = wins.next() ? wins.getInt("wins") : 0;
                                        sender.sendMessage(ChatColor.AQUA + args[1] + " currently has " + ChatColor.BLUE + bling + ChatColor.AQUA + (bling == 1 ? " win!" : " wins!"));
                                    } catch (SQLException ex) {
                                        Logger.getLogger(VectorExecutor.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                });
                            } else {
                                sender.sendMessage(Util.PREFIX + ChatColor.RED + "You must be a player in order to use this command, or specify a player name!");
                            }
                        }
                    } else if (args[0].equalsIgnoreCase("forcestart")) {
                        if (!sender.hasPermission("vf.admin")) {
                            sender.sendMessage(ChatColor.RED + "You don't have the permissions for that!");
                            return true;
                        }
                        try {
                            if (plugin.getHandler().getGame() != null && plugin.getHandler().getGame().getState() == VectorForceGame.State.WAITING) {
                                plugin.getHandler().getGame().generateWorld();
                                plugin.getHandler().getGame().setCounter(0);
                            }
                        } catch (Exception e) {
                            sender.sendMessage(Util.PREFIX + "Invalid arguments!");
                        }
                    } else if (args[0].equalsIgnoreCase("seticon")) {
                        if (!sender.hasPermission("vf.admin")) {
                            sender.sendMessage(ChatColor.RED + "You don't have the permissions for that!");
                            return true;
                        }
                        if (sender instanceof Player) {
                            Player p = (Player) sender;
                            try {
                                String theme = args[1];
                                plugin.getConfig().set("themes." + theme + ".icon", p.getItemInHand());
                                plugin.saveConfig();
                                plugin.reload();
                                p.sendMessage(Util.PREFIX + ChatColor.GREEN + "Saved the icon as themes." + theme + ".icon in the config.");
                            } catch (Exception e) {
                                sender.sendMessage(Util.PREFIX + ChatColor.RED + "Failed to set the theme's icon.");
                            }
                        } else {
                            sender.sendMessage(Util.PREFIX + ChatColor.RED + "You must be a player in order to use this command!");
                        }
                    } else if (args[0].equalsIgnoreCase("setmap")) {
                        if (!sender.hasPermission("vf.admin")) {
                            sender.sendMessage(ChatColor.RED + "You don't have the permissions for that!");
                            return true;
                        }
                        if (plugin.getHandler().getGame() != null && plugin.getHandler().getGame().getState() == VectorForceGame.State.WAITING) {
                            plugin.getHandler().getGame().setForceMap(plugin.getMapManager().getThemeFromName(args[1]));
                            sender.sendMessage(Util.PREFIX + ChatColor.GREEN + "Set the map!");
                        }
                    } else if (args[0].equalsIgnoreCase("reload")) {
                        if (!sender.hasPermission("vf.admin")) {
                            sender.sendMessage(ChatColor.RED + "You don't have the permissions for that!");
                            return true;
                        }
                        plugin.reload();
                        sender.sendMessage(Util.PREFIX + ChatColor.GRAY + "Reloaded the config!");
                    } else if (args[0].equalsIgnoreCase("debug-generate") || args[0].equalsIgnoreCase("dg")) {
                        if (!sender.hasPermission("vf.admin")) {
                            sender.sendMessage(ChatColor.RED + "You don't have the permissions for that!");
                            return true;
                        }
                        sender.sendMessage(Util.PREFIX + ChatColor.GRAY + "[Debug] Generated a new world");
                        plugin.getMapManager().setTheme(plugin.getMapManager().getThemeFromName(args[1]));
                        plugin.getMapManager().createWorld();
                    }
                }
            }
            return true;
        }
        if (command.getName().equalsIgnoreCase("spectate")) {
            if (args.length == 0) {
                if (!sender.hasPermission("vf.user")) {
                    sender.sendMessage(ChatColor.RED + "You don't have the permissions for that!");
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "You must be a player to use this command!");
                    return true;
                } else {
                    Player spec = (Player) sender;
                    switch (spec.getGameMode()) {
                        case SPECTATOR:
                            spec.setGameMode(GameMode.ADVENTURE);
                            break;
                        default:
                            spec.setGameMode(GameMode.SPECTATOR);
                    }
                    sender.sendMessage(Util.PREFIX + "Toggled your spectator mode!");
                }
            } else {
                if (!sender.hasPermission("vf.admin")) {
                    sender.sendMessage(ChatColor.RED + "You don't have the permissions for that!");
                    return true;
                }
                Player spec = Bukkit.getPlayer(args[1]);
                if (spec != null) {
                    switch (spec.getGameMode()) {
                        case SPECTATOR:
                            spec.setGameMode(GameMode.ADVENTURE);
                            break;
                        default:
                            spec.setGameMode(GameMode.SPECTATOR);
                    }
                    sender.sendMessage(Util.PREFIX + "Toggled " + spec.getDisplayName() + "'s spectator mode!");
                } else {
                    sender.sendMessage(ChatColor.RED + "This player is offline!");
                }
            }
            return true;
        }
        return true;
    }
}
