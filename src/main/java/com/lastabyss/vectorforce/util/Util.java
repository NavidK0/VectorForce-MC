package com.lastabyss.vectorforce.util;

import com.lastabyss.vectorforce.VectorForce;
import com.lastabyss.vectorforce.game.VectorForceGame;
import com.lastabyss.vectorforce.game.VectorForceHandler;
import com.lastabyss.vectorforce.map.Theme;
import com.lastabyss.vectorforce.plugins.ext.menu.IconMenu;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import net.minecraft.server.v1_9_R1.IChatBaseComponent;
import net.minecraft.server.v1_9_R1.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_9_R1.PacketPlayInClientCommand;
import net.minecraft.server.v1_9_R1.PacketPlayOutChat;
import net.minecraft.server.v1_9_R1.PacketPlayOutTitle;
import net.minecraft.server.v1_9_R1.PacketPlayOutTitle.EnumTitleAction;
import net.minecraft.server.v1_9_R1.PlayerConnection;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.InvocationTargetException;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

/**
 * @author Navid
 */
public class Util {
    private static VectorForce plugin;
    public final static Random random = new SecureRandom();
    public final static String PREFIX = ChatColor.translateAlternateColorCodes('&', "&f[&b&l>>&f] ");
    public final static String UNAVAILABLE = ChatColor.GRAY + "" + ChatColor.ITALIC + "Unavailable";
    public static WorldEditPlugin wePlugin = null;

    private Util() {
        throw new UnsupportedOperationException("Noice try m8");
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        super.clone();
        throw new CloneNotSupportedException("Noice try m8");
    }

    public static void initialize(VectorForce plugin) {
        Util.plugin = plugin;
        wePlugin = ((WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit"));
    }

    public static void sendHelpMenu(CommandSender sender) {
        sender.sendMessage(ChatColor.AQUA + "----- " + ChatColor.WHITE + "VectorForce v." + plugin.getDescription().getVersion() + ChatColor.AQUA + " -----");
        sender.sendMessage(ChatColor.BLUE + "Aliases: /vf, /vectorforce, /vector, /force, /vforce, /vectorf");
        sender.sendMessage(ChatColor.GREEN + "[] = Optional <> = Required");
        if (sender.hasPermission("vf.user")) {
            sender.sendMessage(ChatColor.AQUA
                            + "/vf ready: Vote ready to skip the wait timer. \n"
                            + "/vf vote: Vote for the next theme! \n"
            );
        }
        if (sender.hasPermission("vf.admin")) {
            sender.sendMessage(ChatColor.AQUA
                            + "/vf playsound [player] : Play music using VectorForce's audio system to a user. \n"
                            + "/vf stopsounds [player] : Stop any music playing. The resourcepack must be downloaded in order for this to work. \n"
                            + "/vf specialround [specialround]: Forces specialround. \n"
                            + "/vf motivator [motivator]: Forces a motivator.\n"
                            + "/vf forcestart: Forces the game to start.\n"
                            + "/vf seticon <theme>: Sets an icon for the vote menu for a specific map.\n"
                            + "/vf reload: Reloads the config from disk.\n"
            );
        }
    }

    /**
     * Returns true if player was successfully respawned, otherwise
     * returns false.
     *
     * @param player
     * @return
     */
    public static boolean respawnPlayer(Player player) {
        Player p = player;
        try {
            Object nmsPlayer = p.getClass().getMethod("getHandle").invoke(p);
            PacketPlayInClientCommand packet = new PacketPlayInClientCommand(PacketPlayInClientCommand.EnumClientCommand.PERFORM_RESPAWN);
            PlayerConnection con = (PlayerConnection) nmsPlayer.getClass().getField("playerConnection").get(nmsPlayer);
            con.getClass().getMethod("a", packet.getClass()).invoke(con, packet);
            return true;
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchFieldException t) {
            VectorForce.getInstance().getLogger().log(Level.WARNING, "Failed to respawn player {0}", p.getName());
            t.printStackTrace(System.out);
            return false;
        }
    }

    /**
     * Colors a text.
     * This is just shorthand for ChatColor.translateAlternateColorCodes
     *
     * @param text
     * @return
     */
    public static String colorize(String text) {
        if (text == null) return null;
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static void sendActionBar(Player player, String message) {
        CraftPlayer p = (CraftPlayer) player;
        IChatBaseComponent cbc = ChatSerializer.a("{\"text\": \"" + message + "\"}");
        PacketPlayOutChat ppoc = new PacketPlayOutChat(cbc, (byte) 2);
        p.getHandle().playerConnection.sendPacket(ppoc);
    }

    public static void sendTitle(Player player, Integer fadeInTicks, Integer stayTicks, Integer fadeOutTicks, String title, String subtitle) {
        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;

        PacketPlayOutTitle packetPlayOutTimes = new PacketPlayOutTitle(EnumTitleAction.TIMES, null, fadeInTicks, stayTicks, fadeOutTicks);
        connection.sendPacket(packetPlayOutTimes);
        if (title == null) title = "";

        if (subtitle != null) {
            subtitle = subtitle.replaceAll("%player%", player.getDisplayName());
            subtitle = ChatColor.translateAlternateColorCodes('&', subtitle);
            IChatBaseComponent titleSub = ChatSerializer.a("{\"text\": \"" + subtitle + "\"}");
            PacketPlayOutTitle packetPlayOutSubTitle = new PacketPlayOutTitle(EnumTitleAction.SUBTITLE, titleSub);
            connection.sendPacket(packetPlayOutSubTitle);
        }

        title = title.replaceAll("%player%", player.getDisplayName());
        title = ChatColor.translateAlternateColorCodes('&', title);
        IChatBaseComponent titleMain = ChatSerializer.a("{\"text\": \"" + title + "\"}");
        PacketPlayOutTitle packetPlayOutTitle = new PacketPlayOutTitle(EnumTitleAction.TITLE, titleMain);
        connection.sendPacket(packetPlayOutTitle);
    }

    public static String formatHealth(String hf, double health, double maxHealth) {
        hf = hf.replace("%n", String.valueOf(Math.round(health)));
        hf = hf.replace("%d", String.valueOf(Math.round(maxHealth)));
        return hf;
    }

    public static boolean isStairs(Material mat) {
        return mat == Material.WOOD_STAIRS ||
                mat == Material.COBBLESTONE_STAIRS || mat == Material.NETHER_BRICK_STAIRS ||
                mat == Material.QUARTZ_STAIRS || mat == Material.RED_SANDSTONE_STAIRS ||
                mat == Material.BRICK_STAIRS || mat == Material.SANDSTONE_STAIRS || mat == Material.SMOOTH_STAIRS;
    }

    /**
     * NOT MINE
     * <p>
     * Credit: Rprrr
     *
     * @param player
     */
    public void spawnFirework(Player player) {
        //Spawn the Firework, get the FireworkMeta.
        Firework fw = (Firework) player.getWorld().spawnEntity(player.getLocation(), EntityType.FIREWORK);
        FireworkMeta fwm = fw.getFireworkMeta();

        //Our random generator
        Random r = new Random();

        //Get the type
        int rt = r.nextInt(5) + 1;
        FireworkEffect.Type fType = FireworkEffect.Type.BALL;
        if (rt == 1) fType = FireworkEffect.Type.BALL;
        if (rt == 2) fType = FireworkEffect.Type.BALL_LARGE;
        if (rt == 3) fType = FireworkEffect.Type.BURST;
        if (rt == 4) fType = FireworkEffect.Type.CREEPER;
        if (rt == 5) fType = FireworkEffect.Type.STAR;

        //Get our random colours
        int r1i = r.nextInt(10);
        int r2i = r.nextInt(10);
        Color c1 = getColor(r1i);
        Color c2 = getColor(r2i);

        //Create our effect with this
        FireworkEffect effect = FireworkEffect.builder().flicker(r.nextBoolean()).withColor(c1).withFade(c2).with(fType).trail(r.nextBoolean()).build();

        //Then apply the effect to the meta
        fwm.addEffect(effect);

        //Generate some random power and set it
        int rp = r.nextInt(2) + 1;
        fwm.setPower(rp);

        //Then apply this to our rocket
        fw.setFireworkMeta(fwm);
    }

    private Color getColor(int i) {
        Color c = null;
        if (i == 0) {
            c = Color.AQUA;
        }
        if (i == 1) {
            c = Color.BLACK;
        }
        if (i == 2) {
            c = Color.BLUE;
        }
        if (i == 3) {
            c = Color.FUCHSIA;
        }
        if (i == 4) {
            c = Color.GRAY;
        }
        if (i == 5) {
            c = Color.GREEN;
        }
        if (i == 6) {
            c = Color.LIME;
        }
        if (i == 7) {
            c = Color.MAROON;
        }
        if (i == 8) {
            c = Color.NAVY;
        }
        if (i == 9) {
            c = Color.OLIVE;
        }
        return c;
    }

    /**
     * Converts minutes and seconds to just seconds.
     *
     * @param minutes
     * @param seconds
     * @return
     */
    public static int convertToSeconds(int minutes, int seconds) {
        return (minutes * 60) + seconds;
    }

    /**
     * Seconds must be 0 or greater.
     *
     * @param seconds
     * @return
     */
    public static String formatTime(int seconds) {
        String formatted = "N/A";
        int hours = seconds / 3600,
                remainder = seconds % 3600,
                minutes = remainder / 60,
                sec = remainder % 60;

        if (hours > 0) {
            formatted = String.valueOf(hours + " hour(s), " + minutes + " min(s), " + sec + " second(s)");
        } else if (minutes > 0) {
            formatted = String.valueOf(minutes + " min(s), " + sec + " second(s)");
        } else if (sec > 0) {
            formatted = String.valueOf(sec + " second(s)");
        } else if (sec == 0) {
            formatted = " 0 second(s)";
        }
        return formatted;
    }

    public static String formatShortenedTime(int seconds) {
        int m = seconds / 60;
        int s = seconds % 60;
        String str = String.format("%02d:%02d", m, s);
        return str;
    }

    public static <T> T getRandomObject(List<? extends T> list) {
        Collections.shuffle(list, random);
        return list.get(0);
    }

    /**
     * round n down to nearest multiple of m
     *
     * @param n
     * @param m
     * @return
     */
    public static long roundDown(long n, long m) {
        return n >= 0 ? (n / m) * m : ((n - m + 1) / m) * m;
    }

    /**
     * round n up to nearest multiple of m
     *
     * @param n
     * @param m
     * @return
     */
    public static long roundUp(long n, long m) {
        return n >= 0 ? ((n + m - 1) / m) * m : (n / m) * m;
    }

    public static void showVoteMenu(Player player) {
        if (plugin.getHandler().getGame() == null) return;
        List<Theme> themes = plugin.getMapManager().getThemes();
        VectorForceGame game = plugin.getHandler().getGame();
        IconMenu menu = new IconMenu("Vote the next round's map!", (int) roundUp(themes.size(), 9),
                (IconMenu.OptionClickEvent event) -> {
                    Player evtPlayer = event.getPlayer();
                    if (!evtPlayer.hasPermission("vf.admin")) {
                        if (event.getItem().hasItemMeta() &&
                                event.getItem().getItemMeta().hasLore() &&
                                event.getItem().getItemMeta().getLore().get(0)
                                        .equals(UNAVAILABLE)) {
                            event.setWillClose(false);
                            event.setWillDestroy(false);
                            player.sendMessage(Util.PREFIX + ChatColor.RED + "You cannot vote for a map that is cooling down!");
                            return;
                        }
                    } else {
                        if (event.getItem().hasItemMeta() &&
                                event.getItem().getItemMeta().hasLore() &&
                                event.getItem().getItemMeta().getLore().get(0)
                                        .equals(UNAVAILABLE)) {
                            player.sendMessage(Util.PREFIX + ChatColor.BLUE + "Bypassed cooldown with admin perms.");
                        }
                    }
                    int slot = event.getPosition();
                    Theme t = themes.get(slot);
                    if (!game.getVotes().containsKey(evtPlayer))
                        Bukkit.broadcastMessage(Util.PREFIX + ChatColor.GREEN + event.getPlayer().getDisplayName() + " voted for the next map with /vote!");
                    else
                        Bukkit.broadcastMessage(Util.PREFIX + ChatColor.GREEN + evtPlayer.getDisplayName() + " changed their vote with /vote!");
                    game.getVotes().put(evtPlayer, t);
                    if (!game.getReadied().contains(evtPlayer)) {
                        game.getReadied().add(evtPlayer);
                        int counter = game.getCounter();
                        if (counter > 30) {
                            game.setCounter(counter - 10);
                        }
                    }
                    event.getMenu().destroy();
                }, plugin, player);
        int pos = 0;
        for (Theme t : themes) {
            if (VectorForceHandler.previouslyVoted.containsKey(t.getName())) {
                String[] info = {
                        UNAVAILABLE,
                        ChatColor.RED + "This map is cooling down!",
                        ChatColor.RED + "It will be ready in: ",
                        ChatColor.RED + "{x}"
                };
                Integer integer = VectorForceHandler.previouslyVoted.get(t.getName());
                int roundsLeft = 0;
                if (integer != null)
                    roundsLeft = integer.intValue();
                info[3] = info[3].replace("{x}", roundsLeft + roundsLeft != 1 ? roundsLeft + " rounds!" : roundsLeft + " round!");
                ItemStack icon = new ItemStack(Material.BARRIER, 0);
                menu.setOption(pos, icon, t.getDisplayName(), info);
                pos++;
                continue;
            }
            List<String> description = t.getDescription();
            String[] info = description.toArray(new String[description.size()]);
            ItemStack icon = t.getIcon();
            icon.setAmount(0);
            menu.setOption(pos, icon, t.getDisplayName(), info);
            pos++;
        }
        menu.register();
        menu.open(player);
    }

    /**
     * Shorthand for Bukkit.getScheduler().runTaskAsync...
     *
     * @param r
     * @return
     */
    public static BukkitTask async(Runnable r) {
        return Bukkit.getScheduler().runTaskAsynchronously(plugin, r);
    }

    /**
     * Shorthand for Bukkit.getScheduler().runTaskSync...
     *
     * @param r
     * @return
     */
    public static BukkitTask sync(Runnable r) {
        return Bukkit.getScheduler().runTask(plugin, r);
    }

    public static boolean isTileEntity(Block block) {
        return (!block.getState().getClass().getName().endsWith("CraftBlockState"));
    }

    public static boolean sameWorld(Location l1, Location l2) {
        return l1.getWorld().getName().equals(l2.getWorld().getName());
    }

    public static float clamp(float val, float min, float max) {
        return Math.max(min, Math.min(max, val));
    }

    public static int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }

    public static double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }

    public static long clamp(long val, long min, long max) {
        return Math.max(min, Math.min(max, val));
    }

    public static int reverseInt(int num, int min, int max) {
        return (max + min) - num;
    }
}
