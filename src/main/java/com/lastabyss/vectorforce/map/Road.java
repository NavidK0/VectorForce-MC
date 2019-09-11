package com.lastabyss.vectorforce.map;

import com.sk89q.worldedit.extent.clipboard.Clipboard;

/**
 *
 * @author Navid
 */
@SuppressWarnings("deprecation")
public class Road {
    
    public enum RoadType {
        /**
         * Starting platform
         */
        START,
        /**
         * Straight road, rotates two ways
         */
        STRAIGHT,
        /**
         * Corner road, rotates four ways
         */
        CORNER,
        /**
         * Intersection, doesn't rotate
         */
        INTERSECTION,
        /**
         * TJunction, Rotates 4 ways
         */
        TJUNCTION
    }

    private Clipboard board;
    private RoadType type;
    private String theme;

    public Road(Clipboard board, RoadType type, String theme) {
        this.board = board;
        this.type = type;
        this.theme = theme;
    }

    public Clipboard getClipboard() {
        return board;
    }

    public String getTheme() {
        return theme;
    }

    public RoadType getType() {
        return type;
    }

    public void setBoard(Clipboard board) {
        this.board = board;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public void setType(RoadType type) {
        this.type = type;
    }
}
