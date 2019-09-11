package com.lastabyss.vectorforce.util;

import net.minecraft.server.v1_9_R1.BlockPosition;
import net.minecraft.server.v1_9_R1.WorldServer;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_9_R1.CraftWorld;

public class LightUtils {

    public static void recalcLightLevelAt(Block block) {
        recalcLightLevelAt(block.getLocation());
    }

    public static void recalcLightLevelAt(Location l) {
        recalcLightLevelAt(l.getWorld(), l.getBlockX(), l.getBlockY(), l.getBlockZ());
    }

    public static void recalcLightLevelAt(World world, int x, int y, int z) {
        CraftWorld cworld = (CraftWorld) world;
        WorldServer worldServer = cworld.getHandle();
        BlockPosition blockPosition = new BlockPosition(x, y, z);
        worldServer.n(blockPosition); //TODO: Make sure this is the correct method now
    }

}