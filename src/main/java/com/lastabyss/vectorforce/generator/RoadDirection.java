package com.lastabyss.vectorforce.generator;

/**
 *
 * @author Navid
 */
public enum RoadDirection {
    NORTH(0, -1),
    EAST(1, 0),
    WEST(-1, 0);
    
    private int dx, dz;

    RoadDirection(int dx, int dz) {
        this.dx = dx;
        this.dz = dz;
    }

    public int getDx() {
        return dx;
    }

    public int getDz() {
        return dz;
    }
}
