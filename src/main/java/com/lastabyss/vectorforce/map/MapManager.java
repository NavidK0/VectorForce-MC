package com.lastabyss.vectorforce.map;

import com.lastabyss.vectorforce.VectorForce;
import com.lastabyss.vectorforce.generator.VectorForceGenerator;
import com.lastabyss.vectorforce.map.Road.RoadType;
import com.lastabyss.vectorforce.util.Schematic;
import com.lastabyss.vectorforce.util.Util;
import com.sk89q.worldedit.world.registry.LegacyBlockRegistry;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author Navid
 */
@SuppressWarnings("ALL")
public class MapManager {
    
    private VectorForce plugin;
    private List<Road> roads;
    private List<List<Road>> roadByThemes;
    private List<Theme> themes;
    private Theme theme = null;
    
    public World world = null;
    public LegacyBlockRegistry legacyBlockRegistry = new LegacyBlockRegistry();
    
    public MapManager(VectorForce plugin) {
        this.plugin = plugin;
    }
    
    public void reload() {
        deleteWorld();
        roads = new ArrayList<>();
        roadByThemes = new ArrayList<>();
        themes = new ArrayList<>();
        File df = plugin.getDataFolder();
        File schematics = new File(df, "roads");
        List<File> list = getAllFiles(schematics.toPath());
        list.forEach((f) -> {
            if (f.getName().contains("_template")) return;
            try {
                if (f.getName().startsWith("road_")) {
                    String filename = FilenameUtils.removeExtension(f.getName());
                    String[] args = filename.split("_");
                    Road.RoadType type = Road.RoadType.valueOf(args[1].toUpperCase());
                    String t = args[2];
                    roads.add(new Road(Schematic.load(f), type, t));
                }
            } catch(ArrayIndexOutOfBoundsException ex) {}
        });
        sortAccordingly();
        ConfigurationSection cs = plugin.getConfig().getConfigurationSection("themes");
        Set<String> keys = cs.getKeys(false);
        keys.forEach(k -> {
            ConfigurationSection cfg = cs.getConfigurationSection(k);
            String name = k;
            String displayName = Util.colorize(cfg.getString("display-name", "Default"));
            long time = cfg.getLong("time", 6000);
            List<String> music = cfg.getStringList("music");
            ItemStack icon = cfg.getItemStack("icon", new ItemStack(Material.MAP, 0));
            List<String> description = new ArrayList<>();
            cfg.getStringList("description").forEach(u -> description.add(Util.colorize(u)));
            List<String> gamerules = cfg.getStringList("gamerules");
            boolean entitySpawning = cfg.getBoolean("entity-spawning", true);
            World.Environment environment = World.Environment.valueOf(cfg.getString("environment", World.Environment.NORMAL.toString()));
            boolean tokensEnabled = cfg.getBoolean("tokens", true);
            List<String> replaceRaw = cfg.getStringList("replace");
            Map<String, String> replaceData = new HashMap<>();
            replaceRaw.forEach(e -> {
                e = e.trim();
                String[] split = e.split(";");
                String original = split[0].toUpperCase();
                String newBlock = split[1].toUpperCase();
                replaceData.put(original, newBlock);
            });
            Theme theme = new Theme();
            theme.setName(name);
            theme.setDisplayName(displayName);
            theme.setTime(time);
            theme.setMusic(music);
            theme.setIcon(icon);
            theme.setDescription(description);
            theme.setGamerules(gamerules);
            theme.setEntitySpawningEnabled(entitySpawning);
            theme.setWorldType(environment);
            theme.setTokensEnabled(tokensEnabled);
            theme.setReplaceData(replaceData);
            plugin.getSQL().insertMapColor(name, displayName);
            themes.add(theme);
        });
        plugin.getLogger().log(Level.INFO, "Loaded {0} road schematics.", roads.size());
        theme = themes.get(0);
    }
    
    /**
     * Returns a list of roads sorted by theme.
     * @param theme
     * @return 
     */
    public List<Road> getRoadsByTheme(String theme) {
        return roadByThemes.stream()
                .filter(l -> l.get(0).getTheme().equals(theme))
                .findFirst()
                .get();
    }
    
    /**
     * Gets a random road by type.
     * @param t
     * @return 
     */
    public Road getRoadsByType(RoadType t) {
        return Util.getRandomObject(new ArrayList<>(roads.stream()
                .filter(r -> r.getType() == t)
                .collect(Collectors.toList())));
    }
    
    /**
     * Gets a random road by theme and type.
     * @param t
     * @param theme
     * @return 
     */
    public Road getRoadsByThemeAndType(RoadType t, String theme) {
        return Util.getRandomObject(new ArrayList<>(roads.stream()
                .filter(r -> r.getType() == t && r.getTheme().equals(theme))
                .collect(Collectors.toList())));
    }
    
    private void addByTheme(Road r) {
        String t = r.getTheme();
        roadByThemes.stream().forEach((l) -> {
            Road road = l.get(0);
            if (t.equals(road.getTheme())) {
                l.add(r);
            }
        });
    }

    /**
     * Returns a copy of all roads.
     * @return 
     */
    public List<Road> getRoads() {
        return new ArrayList<>(roads);
    }

    /**
     * Returns roads seperated into lists by theme.
     * @return 
     */
    public List<List<Road>> getRoadByThemes() {
        return roadByThemes;
    }

    private void sortAccordingly() {
        roads.stream().forEach(this::addByTheme);
    }
    
    public World getWorld() {
        return world;
    }

    public LegacyBlockRegistry getBlockRegistry() {
        return legacyBlockRegistry;
    }

    public void deleteWorld() {
        if (world != null) {
            world.setAutoSave(false);
            Location spawnLocation = Bukkit.getWorld("world").getSpawnLocation();
            world.getPlayers().stream()
                    .forEach(p -> p.teleport(spawnLocation));
            Bukkit.getScheduler().runTask(plugin, () -> {
                ((VectorForceGenerator) world.getGenerator()).end();
                Bukkit.unloadWorld(world, false);
                try {
                    FileUtils.deleteDirectory(new File(new File(".").getParentFile(), "VectorForce"));
                    world = null;
                } catch (IOException ex) {
                    Logger.getLogger(MapManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
        }
    }
    
    public void deleteWorldOnStop() {
        if (world != null) {
            world.setAutoSave(false);
            Location spawnLocation = Bukkit.getWorld("world").getSpawnLocation();
            world.getPlayers().stream()
                .forEach(p -> p.teleport(spawnLocation));
            ((VectorForceGenerator)world.getGenerator()).end();
            Bukkit.unloadWorld(world, false);
        }
        try {
            FileUtils.deleteDirectory(new File(new File(".").getParentFile(), "VectorForce"));
            world = null;
        } catch (IOException ex) {
            Logger.getLogger(MapManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public List<File> getAllFiles(Path dir) {
        List<File> list = new ArrayList<>();
        try {
            Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    list.add(file.toFile());
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    } 
    
    public void createWorld() {
        deleteWorld();
        if (world == null) {
            World w = Bukkit.createWorld(new WorldCreator("VectorForce")
                    .type(WorldType.CUSTOMIZED)
                    .environment(theme.getWorldType())
                    .generator(new VectorForceGenerator(plugin)));
            theme.getGamerules().forEach(s -> {
                String[] split = s.split(" ");
                String gamerule = split[0];
                String value = split[1];
                w.setGameRuleValue(gamerule, value);
            });
            w.setAutoSave(false);
            w.setSpawnLocation(7, 65, 7);
            world = w;
        }
    }

    public void setTheme(Theme theme) {
        this.theme = theme;
    }

    public Theme getTheme() {
        return theme;
    }

    /**
     * Returns a list of themes as defined in the
     * config.
     * @return 
     */
    public List<Theme> getThemes() {
        return themes;
    }
    
    public Theme getThemeFromName(String name) {
        Theme namedTheme = null;
        for (Theme t : themes) {
            if (t.getName().equalsIgnoreCase(name)) {
                namedTheme = t;
            }
        }
        return namedTheme;
    }

}
