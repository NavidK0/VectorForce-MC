package com.lastabyss.vectorforce.audio;

import com.google.common.collect.Iterables;
import com.lastabyss.vectorforce.VectorForce;
import com.lastabyss.vectorforce.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * This class connects to the Ozone Sound Client.
 *
 * @author Navid
 */
public class AudioSystem implements Listener {

    private VectorForce plugin;
    private BukkitTask audioTimer = null;
    /**
     * A map of all the songs available, loaded from the
     * config.
     * This map contains the song event name and the length of it.
     */
    private Map<String, Integer> songs = new HashMap<>();
    int counter = 0;
    private boolean paused = false;

    public AudioSystem(VectorForce plugin) {
        this.plugin = plugin;
        List<String> music = VectorForce.musicFile.getStringList("music");
        music.forEach(s -> {
            String[] data = s.split(";");
            String song = data[0];
            String minutes = data[1];
            String seconds = null;
            if (data.length == 3) {
                seconds = data[2];
            }
            int time;
            if (seconds != null) {
                time = Util.convertToSeconds(Integer.valueOf(minutes), Integer.valueOf(seconds));
            } else {
                time = Util.convertToSeconds(Integer.valueOf(minutes), 0);
            }
            plugin.getLogger().log(Level.INFO, "Found song: {0} duration: {1}", new Object[]{song, time});
            songs.put(song, time);
        });

        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                Location loc = player.getLocation();
                Vector dir = loc.getDirection();
                updatePlayer(player.getName(), new double[]{dir.getX(), dir.getY(), dir.getZ()});
            }
        }, 0, 1);
    }

    /**
     * Plays music that has been properly loaded with
     * times. At the end of the song, it will loop again.
     * If the song isn't in the list, it won't load.
     *
     * @param players
     */
    public void broadcastSong(List<Player> players) {
        resetAudioTimer();
        audioTimer = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            updateBroadcast(players);
            counter--;
        }, 0, 20L);
    }

    /**
     * Plays music that has been properly loaded with
     * times. At the end of the song, it will loop again.
     * If the song isn't in the list, it won't load.
     *
     * @param players
     * @param later
     */
    public void broadcastSong(List<Player> players, long later) {
        resetAudioTimer();
        audioTimer = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            if (!paused) {
                updateBroadcast(players);
                counter--;
            }
        }, later, 20L);
    }

    private void updateBroadcast(List<Player> players) {
        if (counter <= 0) {
            String r = chooseRandomSong(plugin.getMapManager().getTheme().getName());
            if (r == null) return;
            counter = songs.get(r) + 1;
            players.forEach(p -> playMusic(p, r));
        }
    }

    public void broadcastMusic(String sound, List<Player> players) {
        players.forEach(p -> playMusic(p, sound));
    }

    public void broadcastMusic(String sound, List<Player> players, long later) {
        players.forEach(p -> Bukkit.getScheduler().runTaskLater(plugin, () -> playMusic(p, sound, 1), later));
    }

    public void broadcastSound(String sound, Location soundLoc, double volume, List<Player> players) {
        players.forEach(p -> playSound(p, sound, soundLoc, volume));
    }

    public void broadcastSound(String sound, List<Player> players, double volume) {
        players.forEach(p -> playSound(p, sound, p.getLocation(), volume));
    }

    public void broadcastSound(String sound, List<Player> players, double volume, long later) {
        players.forEach(p -> Bukkit.getScheduler().runTaskLater(plugin, () -> playSound(p, sound, p.getLocation(), volume), later));
    }

    public void playMusic(Player p, String sound, long later) {
        if (sound == null) return;
        if (sound.isEmpty()) return;
        Bukkit.getScheduler().runTaskLater(plugin, () -> playMusic(p, sound), later);
    }

    public void playMusic(Player p, String sound) {
        if (sound == null) return;
        if (sound.isEmpty()) return;
        stopSounds(p);
        playMusic(p, sound, false);
    }

    public void playMusic(Player p, String sound, boolean loop) {
        if (sound == null) return;
        if (sound.isEmpty()) return;
        stopSounds(p);
        playMusic(p.getName(), sound, 1, loop);
    }

    public void playSound(Player p, String sound, Location soundLocation, double volume, long later) {
        if (sound == null) return;
        if (sound.isEmpty()) return;
        Bukkit.getScheduler().runTaskLater(plugin, () -> playSound(p, sound, soundLocation, volume), later);
    }

    public void playSound(Player p, String sound, Location soundLocation, double volume) {
        if (sound == null) return;
        if (sound.isEmpty()) return;
        double[] userPos = new double[3], soundPos = new double[3];
        Location loc = p.getLocation();
        userPos[0] = loc.getX();
        userPos[1] = loc.getY();
        userPos[2] = loc.getZ();
        soundPos[0] = soundLocation.getX();
        soundPos[1] = soundLocation.getY();
        soundPos[2] = soundLocation.getZ();
        playSound(p.getName(), sound, volume, userPos, soundPos);
    }

    public void stopSounds(Player p) {
        stopMusic(p.getName());
        stopSounds(p.getName());
    }

    public void resetAudioTimer() {
        if (audioTimer != null) {
            audioTimer.cancel();
            audioTimer = null;
        }
        counter = 0;
    }

    public String chooseRandomSong(String theme) {
        List<String> list = plugin.getConfig().getStringList("themes." + theme + ".music");
        if (list == null) return "";
        String rand = Util.getRandomObject(list);
        return rand;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public void updatePlayer(String player, double[] orientation) {
        if (Bukkit.getOnlinePlayers().isEmpty()) return;
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(byteOut);
        try {
            dataOut.writeUTF("updatePlayer");
            dataOut.writeUTF(player);
            dataOut.writeDouble(orientation[0]);
            dataOut.writeDouble(orientation[1]);
            dataOut.writeDouble(orientation[2]);
            dataOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Iterables.getFirst(Bukkit.getOnlinePlayers(), null).sendPluginMessage(VectorForce.getInstance(), "OzoneMusic", byteOut.toByteArray());
    }

    public void playSound(String player, String sound, double volume, double[] userPos, double[] soundPos) {
        if (Bukkit.getOnlinePlayers().isEmpty()) return;
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(byteOut);
        try {
            dataOut.writeUTF("playSound");
            dataOut.writeUTF(player);
            dataOut.writeUTF("vectorforce/" + sound);
            dataOut.writeDouble(volume);

            dataOut.writeDouble(userPos[0]);
            dataOut.writeDouble(userPos[1]);
            dataOut.writeDouble(userPos[2]);

            dataOut.writeDouble(soundPos[0]);
            dataOut.writeDouble(soundPos[1]);
            dataOut.writeDouble(soundPos[2]);
            dataOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Iterables.getFirst(Bukkit.getOnlinePlayers(), null).sendPluginMessage(VectorForce.getInstance(), "OzoneMusic", byteOut.toByteArray());
    }

    public void playMusic(String player, String sound, double volume, boolean loop) {
        paused = false;
        if (Bukkit.getOnlinePlayers().isEmpty()) return;
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(byteOut);
        try {
            dataOut.writeUTF("playMusic");
            dataOut.writeUTF(player);
            dataOut.writeUTF("vectorforce/" + sound);
            dataOut.writeDouble(volume);
            dataOut.writeBoolean(loop);
            dataOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Iterables.getFirst(Bukkit.getOnlinePlayers(), null).sendPluginMessage(VectorForce.getInstance(), "OzoneMusic", byteOut.toByteArray());
    }

    public void stopSounds(String player) {
        if (Bukkit.getOnlinePlayers().isEmpty()) return;
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(byteOut);
        try {
            dataOut.writeUTF("stopSounds");
            dataOut.writeUTF(player);
            dataOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Iterables.getFirst(Bukkit.getOnlinePlayers(), null).sendPluginMessage(VectorForce.getInstance(), "OzoneMusic", byteOut.toByteArray());
    }

    public void stopMusic(String player) {
        paused = false;
        if (Bukkit.getOnlinePlayers().isEmpty()) return;
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(byteOut);
        try {
            dataOut.writeUTF("stopMusic");
            dataOut.writeUTF(player);
            dataOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Iterables.getFirst(Bukkit.getOnlinePlayers(), null).sendPluginMessage(VectorForce.getInstance(), "OzoneMusic", byteOut.toByteArray());
    }

    public void resumeSounds(String player) {
        if (Bukkit.getOnlinePlayers().isEmpty()) return;
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(byteOut);
        try {
            dataOut.writeUTF("resumeSounds");
            dataOut.writeUTF(player);
            dataOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Iterables.getFirst(Bukkit.getOnlinePlayers(), null).sendPluginMessage(VectorForce.getInstance(), "OzoneMusic", byteOut.toByteArray());
    }

    public void resumeMusic(String player) {
        paused = false;
        if (Bukkit.getOnlinePlayers().isEmpty()) return;
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(byteOut);
        try {
            dataOut.writeUTF("resumeMusic");
            dataOut.writeUTF(player);
            dataOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Iterables.getFirst(Bukkit.getOnlinePlayers(), null).sendPluginMessage(VectorForce.getInstance(), "OzoneMusic", byteOut.toByteArray());
    }

    public void pauseSounds(String player) {
        if (Bukkit.getOnlinePlayers().isEmpty()) return;
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(byteOut);
        try {
            dataOut.writeUTF("pauseSounds");
            dataOut.writeUTF(player);
            dataOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Iterables.getFirst(Bukkit.getOnlinePlayers(), null).sendPluginMessage(VectorForce.getInstance(), "OzoneMusic", byteOut.toByteArray());
    }

    public void pauseMusic(String player) {
        paused = true;
        if (Bukkit.getOnlinePlayers().isEmpty()) return;
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(byteOut);
        try {
            dataOut.writeUTF("pauseMusic");
            dataOut.writeUTF(player);
            dataOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Iterables.getFirst(Bukkit.getOnlinePlayers(), null).sendPluginMessage(VectorForce.getInstance(), "OzoneMusic", byteOut.toByteArray());
    }

    public void sendRequired(String player) {
        if (Bukkit.getOnlinePlayers().isEmpty()) return;
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(byteOut);
        try {
            dataOut.writeUTF("sendRequired");
            dataOut.writeUTF(player);
            dataOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Iterables.getFirst(Bukkit.getOnlinePlayers(), null).sendPluginMessage(VectorForce.getInstance(), "OzoneMusic", byteOut.toByteArray());
    }

    public void sendRecommended(String player) {
        if (Bukkit.getOnlinePlayers().isEmpty()) return;
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(byteOut);
        try {
            dataOut.writeUTF("sendRecommended");
            dataOut.writeUTF(player);
            dataOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Iterables.getFirst(Bukkit.getOnlinePlayers(), null).sendPluginMessage(VectorForce.getInstance(), "OzoneMusic", byteOut.toByteArray());
    }

}
