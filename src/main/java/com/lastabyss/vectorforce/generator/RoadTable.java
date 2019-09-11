package com.lastabyss.vectorforce.generator;

import com.lastabyss.vectorforce.map.MapManager;
import com.lastabyss.vectorforce.map.Road;
import com.lastabyss.vectorforce.map.Road.RoadType;
import com.lastabyss.vectorforce.map.RoadData;
import com.lastabyss.vectorforce.util.Util;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.Vector2D;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

/**
 *
 * @author Navid
 */
public class RoadTable {

    private MapManager manager;
    private Map<Vector2D, RoadData> roadTable = new HashMap<>();
    
    private Random random;
    
    public static void main(String[] args) {
        RoadTable table = new RoadTable(null);
        table.generateTable(10000);
        System.out.println(table.getRoadTable());
        
    }
    
    public RoadTable(MapManager manager) {
        this.manager = manager;
        random = Util.random;
    }
    
    /**
     * Generates the road table.
     * @param size
     */
    public void generateTable(int size) {
        int x = 0;
        int z = 0;
        RoadDirection prev = null;
        BlockVector2D prevVec = null;
        for (int i = 0; i < size; i++) {
            int index = random.nextInt(RoadDirection.values().length);
            RoadDirection dir = RoadDirection.values()[index];
            RoadType type = RoadType.STRAIGHT;
            if (prev != null) {
                if (prev == RoadDirection.WEST && dir == RoadDirection.EAST ||
                        prev == RoadDirection.EAST && dir == RoadDirection.WEST) {
                    dir = RoadDirection.NORTH;
                }
                if (prev == RoadDirection.NORTH && dir == RoadDirection.EAST) {
                    roadTable.put(prevVec, new RoadData(prevVec, random.nextInt(10) < 1 ? RoadType.INTERSECTION : RoadType.CORNER, 180));
                    int rand = random.nextInt(10);
                    type = rand < 6 ? RoadType.STRAIGHT : RoadType.TJUNCTION;
                } else if (prev == RoadDirection.NORTH && dir == RoadDirection.WEST) {
                    roadTable.put(prevVec, new RoadData(prevVec, random.nextInt(10) < 1 ? RoadType.INTERSECTION : RoadType.CORNER, 270));
                    int rand = random.nextInt(10);
                    type = rand < 6 ? RoadType.STRAIGHT : RoadType.TJUNCTION;
                } else if (prev == RoadDirection.WEST && dir == RoadDirection.NORTH) {
                    roadTable.put(prevVec, new RoadData(prevVec, random.nextInt(10) < 1 ? RoadType.INTERSECTION : RoadType.CORNER, 90));
                    int rand = random.nextInt(10);
                    type = rand < 6 ? RoadType.STRAIGHT : RoadType.TJUNCTION;
                }  else if (prev == RoadDirection.EAST && dir == RoadDirection.NORTH) {
                    roadTable.put(prevVec, new RoadData(prevVec, random.nextInt(10) < 1 ? RoadType.INTERSECTION : RoadType.CORNER, 0));
                    int rand = random.nextInt(10);
                    type = rand < 6 ? RoadType.STRAIGHT : RoadType.TJUNCTION;
                }
            }
            x += dir.getDx();
            z += dir.getDz();
            BlockVector2D vec = new BlockVector2D(x, z);
            int rot = determineRotation(type, dir);
            roadTable.put(vec, new RoadData(vec, type, rot));
            prev = dir;
            prevVec = vec;
        }
    }
    
    public int determineRotation(RoadType type, RoadDirection dir) {
        if (type == RoadType.STRAIGHT) {
            if (dir == RoadDirection.NORTH) {
                return random.nextInt(2) == 0 ? 0 : 180;
            } else {
                return random.nextInt(2) == 0 ? 90 : 270;
            }
        } else if (type == RoadType.TJUNCTION) {
                int rand = random.nextInt(2);
            if (dir == RoadDirection.NORTH) {
                return rand == 0 ? 90 : 270;
            } else {
                return rand == 0 ? 180 : 0; 
            }
        } else if (type == RoadType.INTERSECTION) {
            int rand = random.nextInt(4);
            switch (rand) {
                case 0:
                    return 0;
                case 1:
                    return 90;
                case 2:
                    return 180;
                case 3:
                    return 270;
            }
        }
        return 0;
    }
    
    /*
     * Calculates the road type based on the coordinates given.
     * @param cx
     * @param cz
     * @param theme
     * @return 
     */
    public Pair<Road, Integer> calculateType(int cx, int cz, String theme) {
        Road road = null;
        int rotate = 0;
        Road.RoadType nextType = null;
        if (cx == 0 && cz == 0) {
            nextType = RoadType.START;
        } else {
            RoadData roadData = checkForRoads(cx, cz);
            if (roadData != null) {
                Vector2D vec = roadData.loc;
                int rot = roadData.rotation;
                if (cx == vec.getBlockX() &&  cz == vec.getBlockZ()) {
                    nextType = roadData.type;
                    rotate = rot;
                }
            }
        }
        if (nextType != null) {
            road = manager.getRoadsByThemeAndType(nextType, theme);
        }
        return new ImmutablePair<>(road, rotate);
    }
    
    public RoadData checkForRoads(int cx, int cz) {
        BlockVector2D vec = new BlockVector2D(cx, cz);
        if (roadTable.containsKey(vec)) {
            RoadData data = roadTable.get(vec);
            return data;
        }
        return null;
    }

    public MapManager getManager() {
        return manager;
    }

    public Map<Vector2D, RoadData> getRoadTable() {
        return roadTable;
    }
}
