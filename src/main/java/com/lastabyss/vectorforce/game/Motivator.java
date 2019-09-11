package com.lastabyss.vectorforce.game;

import com.lastabyss.vectorforce.VectorForce;
import com.lastabyss.vectorforce.entity.CraftSoulGatherer;
import com.lastabyss.vectorforce.entity.EntitySoulGatherer;
import com.lastabyss.vectorforce.entity.EntityTypes;
import com.lastabyss.vectorforce.map.MapManager;
import com.lastabyss.vectorforce.util.Util;
import net.minecraft.server.v1_9_R1.AttributeInstance;
import net.minecraft.server.v1_9_R1.AttributeModifier;
import net.minecraft.server.v1_9_R1.GenericAttributes;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftEnderDragon;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftEntity;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

/**
 *
 * @author Navid
 */
public class Motivator implements Listener {
    
    //Constants
    public static double SOUL_GATHERER_SPEED = 1;
    
    VectorForce plugin;
    MapManager manager;
    List<Player> players;
    VectorForceGame game;
    BukkitTask specialTask = null;
    Type motivator = null;
    
    private boolean dead = false;
    
    //Class globals
    CraftSoulGatherer cEntity;

    public Motivator(VectorForce plugin, MapManager manager, List<Player> players, VectorForceGame game) {
        this.plugin = plugin;
        this.manager = manager;
        this.players = players;
        this.game = game;
    }
    
    public enum Type {
        SOUL_GATHERER(ChatColor.DARK_GRAY + "The Soul Gatherer", CraftSoulGatherer.class);
        
        private String displayName;
        private Class<? extends CraftEntity> craftClass;

        Type(String name, Class<? extends CraftEntity> craftClass) {
            this.displayName = name;
            this.craftClass = craftClass;
        }

        public Class<? extends CraftEntity> getCraftClass() {
            return craftClass;
        }

        public String getDisplayName() {
            return displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    public String generateMotivator() {
        int nextInt = Util.random.nextInt(Type.values().length);
        Bukkit.getPluginManager().registerEvents(this, plugin);
        if (specialTask != null)
            specialTask.cancel();
        if (motivator == null) {
            motivator = Type.values()[nextInt];
        }
        switch (motivator) {
            case SOUL_GATHERER:
                World world = manager.getWorld();
                EntitySoulGatherer entity = new EntitySoulGatherer((world));
                cEntity = entity.getBukkitEntity();
                entity.noclip = true;
                AttributeInstance attributeInstance = entity.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED);
                AttributeModifier mod = new AttributeModifier("VectorForce speed", SOUL_GATHERER_SPEED, 1);
                attributeInstance.b(mod);
                attributeInstance.a(mod);
                EntityTypes.spawnEntity(entity, world.getSpawnLocation());
                specialTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                    if (game.getState() != VectorForceGame.State.RACING || !game.isMotivatorEnabled()) return;
                    Player p = game.getLastAlive();
                    if (p == null) return;
                    if (cEntity.isValid() && Util.sameWorld(cEntity.getLocation(), p.getLocation())) {
                        if (cEntity.getLocation().distanceSquared(p.getLocation()) < (120 * 120)) {
                            cEntity.setTarget(p);
                        } else {
                            cEntity.teleport(p.getLocation());
                            Bukkit.broadcastMessage(Util.PREFIX + ChatColor.BLUE + "The motivator teleported behind " + p.getDisplayName());
                        }
                        players.forEach(x -> x.playSound(x.getLocation(), "ambient.cave.cave", Integer.MAX_VALUE, 1));
                    } else if (!cEntity.isValid()) {
                        EntitySoulGatherer resummon = new EntitySoulGatherer((world));
                        cEntity = resummon.getBukkitEntity();
                        resummon.noclip = true;
                        AttributeInstance ai2 = resummon.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED);
                        AttributeModifier mod2 = new AttributeModifier("VectorForce speed", SOUL_GATHERER_SPEED, 1);
                        ai2.b(mod2);
                        ai2.a(mod2);
                        EntityTypes.spawnEntity(resummon, world.getSpawnLocation());
                    }
                }, 0, 20 * 5L);
                break;
        }
        cEntity.setMetadata("isMotivator", new FixedMetadataValue(plugin, true));
        return motivator.toString();
    }
    
    public void setDragonTarget(Location location, EnderDragon dragon) {
        //TODO: Fix these later
//    ((CraftEnderDragon)dragon).getHandle().getControllerMove().a() = location.getX();
//    ((CraftEnderDragon)dragon).getHandle().b = location.getY();
//    ((CraftEnderDragon)dragon).getHandle().c = location.getZ();
    }
    
    public void endAll() {
        if (specialTask != null) {
            specialTask.cancel();
            specialTask = null;
        }
        if (cEntity != null) {
            if (cEntity.isValid()) 
                cEntity.remove();
            cEntity = null;
        }
        HandlerList.unregisterAll(this);
    }

    public Type getType() {
        return motivator;
    }

    public void setMotivatorType(Type modifier) {
        this.motivator = modifier;
    }

    public Type getEntity() {
        return motivator;
    }

    public boolean isDead() {
        return dead;
    }
}
