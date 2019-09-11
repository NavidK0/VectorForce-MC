package com.lastabyss.vectorforce;

import com.lastabyss.vectorforce.audio.AudioSystem;
import com.lastabyss.vectorforce.data.SQLDataConnector;
import com.lastabyss.vectorforce.executor.VectorExecutor;
import com.lastabyss.vectorforce.game.VectorForceHandler;
import com.lastabyss.vectorforce.map.MapManager;
import com.lastabyss.vectorforce.util.Schematic;
import com.lastabyss.vectorforce.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VectorForce extends JavaPlugin {
    public static YamlConfiguration musicFile;

    private static VectorForce instance;
    private AudioSystem audioSystem;
    private VectorForceHandler handler;
    private VectorExecutor executor;
    private MapManager mapManager;
    private SQLDataConnector connector;

    @Override
    public void onEnable() {
        instance = this;
        this.executor = new VectorExecutor(this);
        this.handler = new VectorForceHandler(this);
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "OzoneMusic");
        Bukkit.getMessenger().registerIncomingPluginChannel(this, "OzoneMusic", handler);
        Util.initialize(this);
        try {
            saveResources();
        } catch (IOException | URISyntaxException ex) {
            Logger.getLogger(VectorForce.class.getName()).log(Level.SEVERE, null, ex);
        }
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            connector = new SQLDataConnector(getConfig().getString("database.user"),
                    getConfig().getString("database.password"),
                    getConfig().getString("database.host"),
                    getConfig().getString("database.db"));
            this.getLogger().info("Connecting to MySQL database...");
            connector.connect();
            if (connector.isConnected()) {
                this.getLogger().info("Connected to database successfully!");
                Util.sync(() -> {
                    this.mapManager = new MapManager(this);
                    if (Bukkit.getPluginManager().getPlugin("WorldGuard") == null) {
                        this.getLogger().severe("WorldGuard is REQUIRED to run this plugin! Please install it!");
                        Bukkit.getPluginManager().disablePlugin(this);
                        return;
                    }
                    if (Bukkit.getPluginManager().getPlugin("ResourcePackApi") == null) {
                        this.getLogger().severe("ResourcePackAPI is REQUIRED to run this plugin! Please install it!");
                        Bukkit.getPluginManager().disablePlugin(this);
                        return;
                    }
                    Schematic.initialize(this);
                    reload();
                    this.audioSystem = new AudioSystem(this);
                    getCommand("vectorforce").setExecutor(executor);
                    getCommand("spectate").setExecutor(executor);
                });
            } else {
                this.getLogger().warning("Failed to connect to database! Scores cannot be recorded, disabling plugin!");
                Bukkit.getPluginManager().disablePlugin(this);
            }
        });
    }

    @Override
    public void onDisable() {
        mapManager.deleteWorldOnStop();
        handler.onDisable();
    }

    public void reload() {
        musicFile = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "music.yml"));
        reloadConfig();
        mapManager.reload();
        handler.reload();
        handler.restartGameFinder();
    }

    public void saveResources() throws IOException, URISyntaxException {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
            getLogger().info("Saved new data folder.");
        }
        saveDefaultConfig();
        if (!new File(getDataFolder(), "music.yml").exists()) {
            saveResource("music.yml", false);
        }
        File urbanrundown = new File(getDataFolder(), "roads/default/");
        File icy = new File(getDataFolder(), "roads/icy/");
        File tnt = new File(getDataFolder(), "roads/tnt/");
        File oldgold = new File(getDataFolder(), "roads/oldgold/");
        File slimeally = new File(getDataFolder(), "roads/slimeally/");
        if (!urbanrundown.exists()) {
            urbanrundown.mkdirs();
            URL url = VectorForce.class.getResource("VectorForce.class");
            String scheme = url.getProtocol();
            if (!"jar".equals(scheme))
                throw new IllegalArgumentException("Unsupported scheme: " + scheme);
            JarURLConnection con = (JarURLConnection) url.openConnection();
            JarFile archive = con.getJarFile();
            Enumeration<JarEntry> entries = archive.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().startsWith("roads/default/")) {
                    saveResource(entry.getName(), false);
                }
            }
        }

        if (!icy.exists()) {
            icy.mkdirs();
            URL url = VectorForce.class.getResource("VectorForce.class");
            String scheme = url.getProtocol();
            if (!"jar".equals(scheme))
                throw new IllegalArgumentException("Unsupported scheme: " + scheme);
            JarURLConnection con = (JarURLConnection) url.openConnection();
            JarFile archive = con.getJarFile();
            Enumeration<JarEntry> entries = archive.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().startsWith("roads/icy/")) {
                    saveResource(entry.getName(), false);
                }
            }
        }
        if (!tnt.exists()) {
            tnt.mkdirs();
            URL url = VectorForce.class.getResource("VectorForce.class");
            String scheme = url.getProtocol();
            if (!"jar".equals(scheme))
                throw new IllegalArgumentException("Unsupported scheme: " + scheme);
            JarURLConnection con = (JarURLConnection) url.openConnection();
            JarFile archive = con.getJarFile();
            Enumeration<JarEntry> entries = archive.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().startsWith("roads/tnt/")) {
                    saveResource(entry.getName(), false);
                }
            }
        }
        if (!oldgold.exists()) {
            tnt.mkdirs();
            URL url = VectorForce.class.getResource("VectorForce.class");
            String scheme = url.getProtocol();
            if (!"jar".equals(scheme))
                throw new IllegalArgumentException("Unsupported scheme: " + scheme);
            JarURLConnection con = (JarURLConnection) url.openConnection();
            JarFile archive = con.getJarFile();
            Enumeration<JarEntry> entries = archive.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().startsWith("roads/oldgold/")) {
                    saveResource(entry.getName(), false);
                }
            }
        }
        if (!slimeally.exists()) {
            tnt.mkdirs();
            URL url = VectorForce.class.getResource("VectorForce.class");
            String scheme = url.getProtocol();
            if (!"jar".equals(scheme))
                throw new IllegalArgumentException("Unsupported scheme: " + scheme);
            JarURLConnection con = (JarURLConnection) url.openConnection();
            JarFile archive = con.getJarFile();
            Enumeration<JarEntry> entries = archive.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().startsWith("roads/slimeally/")) {
                    saveResource(entry.getName(), false);
                }
            }
        }
        saveResource("roads/road_template.schematic", false);
    }

    public MapManager getMapManager() {
        return mapManager;
    }

    public AudioSystem getAudioSystem() {
        return audioSystem;
    }

    public VectorForceHandler getHandler() {
        return handler;
    }

    public SQLDataConnector getSQL() {
        return connector;
    }

    public static VectorForce getInstance() {
        return instance;
    }
}
