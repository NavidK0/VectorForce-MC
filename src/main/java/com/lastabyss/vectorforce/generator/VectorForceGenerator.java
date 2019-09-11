package com.lastabyss.vectorforce.generator;

import com.lastabyss.vectorforce.VectorForce;
import com.lastabyss.vectorforce.map.MapManager;
import com.lastabyss.vectorforce.map.Road;
import com.lastabyss.vectorforce.util.LightUtils;
import com.lastabyss.vectorforce.util.NBTConverter;
import com.lastabyss.vectorforce.util.Util;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.transform.BlockTransformExtent;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.regions.Region;
import net.minecraft.server.v1_9_R1.BlockPosition;
import net.minecraft.server.v1_9_R1.NBTTagCompound;
import net.minecraft.server.v1_9_R1.TileEntity;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_9_R1.CraftWorld;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Random;

public class VectorForceGenerator extends ChunkGenerator {

    VectorForce plugin;
    MapManager manager;
    RoadTable roadTable;
    BukkitTask fillTask;
    World worldRef = null;

    Queue<List<Triple<BlockVector, BaseBlock, CompoundTag>>> dataQueue = new ArrayDeque<>();

    public VectorForceGenerator(VectorForce plugin) {
        this.plugin = plugin;
        this.manager = plugin.getMapManager();
        this.roadTable = new RoadTable(manager);
        plugin.getLogger().info("Generating road tables...");
        this.roadTable.generateTable(10000);
        plugin.getLogger().info("Done generating road tables.");

        fillTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            if (manager != null && worldRef != null) {
                 if (!dataQueue.isEmpty()) {
                    List<Triple<BlockVector, BaseBlock, CompoundTag>> list = dataQueue.poll();
                    list.forEach(p -> {
                        Util.sync(() -> {
                            BlockVector vec = p.getLeft();
                            BaseBlock bb = p.getMiddle();
                            CompoundTag tag = p.getRight();
                            BlockPosition pos = new BlockPosition(vec.getBlockX(), vec.getBlockY(), vec.getBlockZ());
                            Block b = worldRef.getBlockAt(vec.getBlockX(), vec.getBlockY(), vec.getBlockZ());
                            b.setData((byte) bb.getData(), false);
                            if (tag != null) {
                                NBTTagCompound nbt = NBTConverter.toNative(tag);
                                TileEntity tile = ((CraftWorld) worldRef).getHandle().getTileEntity(pos);
                                tile.a(nbt);
                                tile.update();
                            }
                            LightUtils.recalcLightLevelAt(b);
                        });
                    });
                }
            }
        }, 0L, 1L);
    }

    public void end() {
        if (fillTask != null)
            fillTask.cancel();
    }

    @Override
    public short[][] generateExtBlockSections(World world, Random random, int cx, int cz, ChunkGenerator.BiomeGrid biomes) {
        if (this.worldRef == null)
            this.worldRef = world;
        short[][] result = new short[world.getMaxHeight() / 16][];
        List<Road> roads = manager.getRoads();
        if (roads.size() < 1) {
            plugin.getLogger().severe("There are no road schematics in the VectorForce/roads folder! World cannot generate anything!");
            return result;
        }
        Pair<Road, Integer> pair = roadTable.calculateType(cx, cz, manager.getTheme().getName());
        if (pair.getLeft() != null) {
            Road road = pair.getLeft();
            int rotate = pair.getRight();
            if (road == null) return result;
            Clipboard clipboard = transform(road, rotate);
            fillChunk(result, cx, cz, clipboard);
        }
        return result;
    }

    private void fillChunk(short[][] result, int cx, int cz, Clipboard clipboard) {
        List<Triple<BlockVector, BaseBlock, CompoundTag>> blockList = new ArrayList<>();
        Region region = clipboard.getRegion();
        Iterator<BlockVector> iter = region.iterator();
        while(iter.hasNext()) {
            BlockVector vec = iter.next();
            BaseBlock block = clipboard.getBlock(vec);
            Material mat = Material.getMaterial(block.getId());
            if (manager.getTheme().getReplaceData().containsKey(mat.name())) {
                String replacement = manager.getTheme().getReplaceData().get(mat.name());
                Material material = Material.valueOf(replacement);
                int id = material.getId();
                block.setId(id);
            }
            Vector abs = vec.subtract(region.getMinimumPoint());
            int x = abs.getBlockX();
            int y = abs.getBlockY();
            int z = abs.getBlockZ();
            int mx = (cx << 4) + x;
            int mz = (cz << 4) + z;
            ImmutableTriple<BlockVector, BaseBlock, CompoundTag> mod = modBlock(result, block, new int[]{x, z, mx, y, mz});
            if (mod != null) {
                blockList.add(mod);
            }
        }
//        entityQueue.add(clipboard);
        dataQueue.add(blockList);
    }

    private ImmutableTriple<BlockVector, BaseBlock, CompoundTag> modBlock(short[][] result, BaseBlock block, int[] pos) {
        int x = pos[0];
        int z = pos[1];
        int mx = pos[2];
        int y = pos[3];
        int mz = pos[4];
        try {
            setBlock(result, x, y, z, (short) block.getId());
            if (block.isAir()) return null;
            if (block.hasNbtData() || block.getData() != 0)
                return new ImmutableTriple<>(new BlockVector(mx, y, mz), block, block.getNbtData());
        } catch (Exception e) {
            setBlock(result, x, y, z, (byte) 0);
            e.printStackTrace();
        }
        return null;
    }

    private Clipboard transform(Road road, int rotate) {
        Clipboard clipboard = road.getClipboard();
        Clipboard cbCopy = new BlockArrayClipboard(clipboard.getRegion());
        Vector center = cbCopy.getRegion().getCenter();
        cbCopy.setOrigin(new Vector(center.getX(), 0, center.getZ()));
        AffineTransform transform = new AffineTransform().rotateY(-rotate);
        BlockTransformExtent bte = new BlockTransformExtent(clipboard, transform, manager.getBlockRegistry());
        Vector to = cbCopy.getOrigin();
        ForwardExtentCopy copy = new ForwardExtentCopy(
                bte,
                clipboard.getRegion(),
                clipboard.getOrigin(),
                cbCopy,
                to
        );
        copy.setTransform(transform);
        Operations.completeBlindly(copy);
        return cbCopy;
    }

    private void setBlock(short[][] result, int x, int y, int z, short blockid) {
        if (result[y >> 4] == null) {
            result[y >> 4] = new short[4096];
        }
        result[y >> 4][((y & 0xF) << 8) | (z << 4) | x] = blockid;
    }

    @Override
    public boolean canSpawn(World world, int x, int z) {
        return true;
    }

    public BukkitTask getFillTask() {
        return fillTask;
    }

    public MapManager getManager() {
        return manager;
    }

    public Queue<List<Triple<BlockVector, BaseBlock, CompoundTag>>> getDataQueue() {
        return dataQueue;
    }

    public RoadTable getRoadTable() {
        return roadTable;
    }

    public VectorForce getPlugin() {
        return plugin;
    }
}
