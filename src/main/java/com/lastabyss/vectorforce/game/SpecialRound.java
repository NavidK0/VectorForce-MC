package com.lastabyss.vectorforce.game;

import com.lastabyss.vectorforce.VectorForce;
import com.lastabyss.vectorforce.map.MapManager;
import com.lastabyss.vectorforce.util.Util;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

/**
 *
 * @author Navid
 */
public class SpecialRound implements Listener {
    
    VectorForce plugin;
    MapManager manager;
    List<Player> players;
    VectorForceGame game;
    BukkitTask specialTask = null;
    Type modifier = null;
    
    private boolean redLight = false;
    private boolean flooding = false;

    public SpecialRound(VectorForce plugin, MapManager manager, List<Player> players, VectorForceGame game) {
        this.plugin = plugin;
        this.manager = manager;
        this.players = players;
        this.game = game;
    }
    
    public enum Type {
        NIGHT_RUN(ChatColor.BLUE + "Night Run"),
        STORMY_RUN(ChatColor.AQUA + "Stormy Run"),
        GRIEF_RUN(ChatColor.GRAY + "Griefing Enabled"),
        TNT_RUN(ChatColor.DARK_RED + "TNT RUN"),
        FASTER_SPEEDUP(ChatColor.WHITE + "Faster Speedup"),
        RED_LIGHT_GREEN_LIGHT(ChatColor.RED + "Red Light, " + ChatColor.GREEN + "Green Light"),
        EARLY_HARDMODE(ChatColor.RED + "Early Hard Mode"),
        EARLY_MOTIVATOR(ChatColor.DARK_GRAY + "Early Motivator..."),
        POISON_RUN(ChatColor.DARK_GREEN + "Poison Run");
//        NOAHS_ARK(ChatColor.AQUA + "Noah's Ark");
        
        private String name;

        Type(String name) {
            this.name = name;
        }
        
        @Override        
        public String toString() {
            return ChatColor.translateAlternateColorCodes('&', name);
        }
    }

    public String getSpecialRound() {
        int nextInt = Util.random.nextInt(Type.values().length);
        Bukkit.getPluginManager().registerEvents(this, plugin);
        if (specialTask != null)
            specialTask.cancel();
        if (modifier == null) {
            modifier = Type.values()[nextInt];
        }
        switch (modifier) {
            case NIGHT_RUN:
                manager.getWorld().setTime(13000);
                players.forEach(x -> x.playSound(x.getLocation(), "random.anvil_land", Integer.MAX_VALUE, 1));
                break;
            case STORMY_RUN:
                manager.getWorld().setThundering(true);
                manager.getWorld().setStorm(true);
                manager.getWorld().setWeatherDuration(99999);
                manager.getWorld().setThunderDuration(99999);
                players.forEach(p -> p.playSound(p.getLocation(), "ambient.weather.thunder", 1, 1));
                break;
            case GRIEF_RUN:
                players.forEach(p -> p.playSound(p.getLocation(), "mob.zombiepig.zpigangry", 1, 1));
                ItemStack shears = new ItemStack(Material.SHEARS, 1);
                shears.addUnsafeEnchantment(Enchantment.DURABILITY, 10);
                shears.addUnsafeEnchantment(Enchantment.DIG_SPEED, 10);
                ItemStack pick = new ItemStack(Material.DIAMOND_PICKAXE, 1);
                pick.addUnsafeEnchantment(Enchantment.DURABILITY, 10);
                pick.addUnsafeEnchantment(Enchantment.DIG_SPEED, 10);
                ItemStack shovel = new ItemStack(Material.DIAMOND_SPADE, 1);
                shovel.addUnsafeEnchantment(Enchantment.DURABILITY, 10);
                shovel.addUnsafeEnchantment(Enchantment.DIG_SPEED, 10);
                ItemStack axe = new ItemStack(Material.DIAMOND_AXE, 1);
                axe.addUnsafeEnchantment(Enchantment.DURABILITY, 10);
                axe.addUnsafeEnchantment(Enchantment.DIG_SPEED, 10);
                players.forEach(p -> {
                    p.getInventory().addItem(shears);
                    p.getInventory().addItem(pick);
                    p.getInventory().addItem(shovel);
                    p.getInventory().addItem(axe);
                    p.updateInventory();
                });
                break;
            case TNT_RUN:
                specialTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                    if (game.getState() != VectorForceGame.State.RACING) return;
                    for (Player player : players) {
                        if (player == null) {
                                continue;
                        }
                        final Location loc = player.getLocation();
                        final World world = loc.getWorld();
                        for (int x = -10; x <= 10; x += 5) {
                                for (int z = -10; z <= 10; z += 5) {
                                        final Location tntloc = new Location(world, loc.getBlockX() + x, world.getHighestBlockYAt(loc) + 64, loc.getBlockZ() + z);
                                        final TNTPrimed tnt = world.spawn(tntloc, TNTPrimed.class);
                                }
                        }
                    }
                }, 0, 20L * 10L);
                players.forEach(p -> p.playSound(p.getLocation(), "game.tnt.primed", 1, 1));
                break;
            case FASTER_SPEEDUP:
                players.forEach(x -> x.playSound(x.getLocation(), "tile.piston.in", Integer.MAX_VALUE, 1));
                VectorForceGame.SPEEDUP_INTERVAL = VectorForceGame.SPEEDUP_INTERVAL / 2;
                break;
            case RED_LIGHT_GREEN_LIGHT:
                players.forEach(x -> x.playSound(x.getLocation(), "tile.piston.in", Integer.MAX_VALUE, 0));
                specialTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                    if (game.getState() != VectorForceGame.State.RACING) return;
                    VectorForceHandler handler = game.getHandler();
                    if (redLight) {
                        handler.sendGameMessage(ChatColor.RED + "Red light!");
                        handler.setSpeed(0.0f);
                        players.forEach(p -> game.getAudio().stopSounds(p));
                        game.getAudio().broadcastSound("disc-scratch", players, 0.50);
                    } else {
                        game.getAudio().broadcastSong(players);
                        handler.sendGameMessage(ChatColor.GREEN + "Green light!");
                        handler.setSpeed(game.getSpeed());
                    }
                    redLight = !redLight;
                }, 0, 20L * 10L);
                players.forEach(p -> p.playSound(p.getLocation(), "fireworks.launch", 1, 1));
                break;
            case EARLY_HARDMODE:
                game.setSpeed(1);
                players.forEach(p -> p.playSound(p.getLocation(), "mob.guardian.curse", 1, 1));
                break;
            case EARLY_MOTIVATOR:
                game.setMotivatorEnabled(true);
                players.forEach(x -> x.playSound(x.getLocation(), "mob.ghast.scream", Integer.MAX_VALUE, 1));
                break;
            case POISON_RUN:
                specialTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                    if (game.getState() != VectorForceGame.State.RACING) return;
                    players.forEach(p -> {
                        p.addPotionEffect(new PotionEffect(PotionEffectType.POISON, Integer.MAX_VALUE, 0), true);
                    });
                }, 0, 20L * 10L);
                players.forEach(x -> x.playSound(x.getLocation(), "mob.ghast.scream", Integer.MAX_VALUE, 1));
                break;
//            case NOAHS_ARK:
//                int waterId = Material.WATER.getId();
//                World w = plugin.getMapManager().getWorld();
//                List<Block> bList = new ArrayList<>();
//                int maxXZ = 10000;
//                int center = maxXZ / 2;
//                int maxY = 256;
//                int centerY = maxY / 2;
//                specialTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
//                    flooding = !flooding;
//                    if (flooding) Bukkit.broadcastMessage(ChatColor.RED + "The flood is coming!");
//                    else Bukkit.broadcastMessage(ChatColor.RED + "The flood is fading...");
//                }, 20L * 5L, 20L * 30L);
//                game.getAudio().broadcastSound("ambient.weather.rain", players, Integer.MAX_VALUE, 0);
//                break;
        }
        return modifier.toString();
    }
    
    @EventHandler
    public void onChunkLoad(ChunkLoadEvent evt) {
//        if (modifier != null && modifier == Type.NOAHS_ARK) {
//            if (evt.getWorld().getName().equals(plugin.getMapManager().getWorld().getName())) {
//                World w = plugin.getMapManager().getWorld();
//                int waterId = Material.WATER.getId();
//                Chunk chunk = evt.getChunk();
//                for (int x = 1; x < 16; x++) {
//                    for (int z = 1; z < 256; z++) {
//                        for (int y = 1; y < 16; y++) {
//                            if (flooding)
//                                BlockUtils.setBlockFast(w, x, y, z, waterId, (byte)0);
//                            else
//                                BlockUtils.setBlockFast(w, x, y, z, 0, (byte)0);
//                        }
//                    }
//                }
//                players.forEach(p -> BlockUtils.queueChunkForUpdate(p, chunk.getX(), chunk.getZ()));
//            }
//        }
    }
    
    @EventHandler
    public void onTNT(EntityExplodeEvent evt) {
        if (modifier != null) {
            evt.blockList().clear();
        }
    }
    
    public void endAll() {
        if (specialTask != null) {
            specialTask.cancel();
            specialTask = null;
        }
        HandlerList.unregisterAll(this);
    }

    public Type getModifier() {
        return modifier;
    }

    public void setModifier(Type modifier) {
        this.modifier = modifier;
    }
    
}
