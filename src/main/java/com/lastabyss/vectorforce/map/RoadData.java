package com.lastabyss.vectorforce.map;

import com.sk89q.worldedit.Vector2D;

/**
 *
 * @author Navid
 */
public class RoadData {

    public Vector2D loc;
    public Road.RoadType type;
    public int rotation;

    public RoadData(Vector2D loc, Road.RoadType type, int rotation) {
        this.loc = loc;
        this.type = type;
        this.rotation = rotation;
    }

    public RoadData() {
    }

    @Override
    public String toString() {
       return "loc: " + loc.toString()
               + " type: " + type
               + " rotation: " + rotation + "\n";
    }
}
