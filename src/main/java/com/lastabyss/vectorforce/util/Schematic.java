package com.lastabyss.vectorforce.util;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.io.Closer;
import com.sk89q.worldedit.world.registry.LegacyWorldData;
import com.sk89q.worldedit.world.registry.WorldData;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;

@SuppressWarnings("deprecation")
/**
 * A collection of utilities for dealing with schematics files/
 * @author Navid
 */
public class Schematic {
    private static JavaPlugin plugin;
    public static final BaseBlock AIR = new BaseBlock(0);

    public static void initialize(JavaPlugin plugin) {
        Schematic.plugin = plugin;
    }

    public static Clipboard load(File schematic) {
        ClipboardFormat format = ClipboardFormat.findByFile(schematic);
        Closer closer = Closer.create();
        try {
            FileInputStream fis = closer.register(new FileInputStream(schematic));
            BufferedInputStream bis = closer.register(new BufferedInputStream(fis));
            ClipboardReader reader = format.getReader(bis);
            WorldData worldData = LegacyWorldData.getInstance();
            Clipboard clipboard = reader.read(worldData);
            Vector center = clipboard.getRegion().getCenter();
            clipboard.setOrigin(new Vector(center.getX(), 0, center.getZ()));
            return clipboard;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("Some problem arose while loading a schematic: " + schematic.getName());
        } finally {
            try {
                closer.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        return null;
    }

    public static BaseBlock[][][] getBlockArray(BlockArrayClipboard clipboard) {
        BaseBlock[][][] blocks;
        Field field = null;
        try {
            field = BlockArrayClipboard.class.getDeclaredField("blocks");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        field.setAccessible(true);
        try {
            return (BaseBlock[][][]) field.get(clipboard);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return new BaseBlock[clipboard.getRegion().getWidth()][clipboard.getRegion().getHeight()][clipboard.getRegion().getLength()];
        }
    }

    public static void setBlockArray(BlockArrayClipboard clipboard, BaseBlock[][][] blocks) {
        Field field = null;
        try {
            field = BlockArrayClipboard.class.getDeclaredField("blocks");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        field.setAccessible(true);
        try {
            field.set(clipboard, blocks);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Replaces the region in the BlockArrayClipboard.
     */
    public static void setRegion(BlockArrayClipboard clipboard, Region region) {
        Field field = null;
        try {
            field = BlockArrayClipboard.class.getDeclaredField("region");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        field.setAccessible(true);
        try {
            field.set(clipboard, region);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Expensive due to reflection usage!
     * It's better to use the BlockArray and grab data
     * from it in bulk directly than calling this method over and over again!
     * @param clipboard
     * @param location
     * @return
     */
    public static BaseBlock getBlock(BlockArrayClipboard clipboard, BlockVector location) {
        BaseBlock[][][] array = getBlockArray(clipboard);
        return array[location.getBlockX()][location.getBlockY()][location.getBlockZ()];
    }

    public static void fillCuboidClipboard(BlockArrayClipboard clipboard, CuboidClipboard cuboid) {
        BaseBlock[][][] array = getBlockArray(clipboard);
        for (int x = 0; x < array.length; x++) {
            for (int z = 0; z < array[0][0].length; z++) {
                for (int y = 0; y < array[0].length; y++) {
                    BaseBlock b = array[x][y][z];
                    if (b == null) b = AIR;
                    cuboid.setBlock(new BlockVector(x, y, z), b);
                }
            }
        }
    }
}
