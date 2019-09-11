package com.lastabyss.vectorforce.entity;

import net.minecraft.server.v1_9_R1.EntityMonster;
import org.bukkit.craftbukkit.v1_9_R1.CraftServer;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftMonster;
import org.bukkit.entity.Wither;

/**
 *
 * @author Navid
 */
public class CraftSoulGatherer extends CraftMonster implements Wither {
    public CraftSoulGatherer(CraftServer server, EntityMonster entity) {
        super(server, entity);
    }
}
