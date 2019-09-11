package com.lastabyss.vectorforce.entity;

import net.minecraft.server.v1_9_R1.EntityWither;
import net.minecraft.server.v1_9_R1.IRangedEntity;
import net.minecraft.server.v1_9_R1.MinecraftServer;
import net.minecraft.server.v1_9_R1.World;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_9_R1.CraftWorld;

/**
 *
 * @author Navid
 */
public class EntitySoulGatherer extends EntityWither implements IRangedEntity {

    public EntitySoulGatherer(org.bukkit.World world) {
        this(((CraftWorld)world).getHandle());
    }

    public EntitySoulGatherer(World world) {
        super(world);
        noclip = true;
    }

    @Override
    public CraftSoulGatherer getBukkitEntity() {
        if (bukkitEntity == null) {
            bukkitEntity = new CraftSoulGatherer(MinecraftServer.getServer().server, this);
            bukkitEntity.setCustomNameVisible(false);
            bukkitEntity.setCustomName(ChatColor.GRAY + "Soul Gatherer");
        }
        return (CraftSoulGatherer) bukkitEntity;
    }

}
