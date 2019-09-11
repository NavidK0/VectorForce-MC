package com.lastabyss.vectorforce.map;

import java.util.List;
import java.util.Map;

import lombok.Data;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Navid
 */
@Data
public class Theme {
    private String name;
    private String displayName;
    private long time;
    private List<String> music;
    private ItemStack icon;
    private List<String> description;
    private List<String> gamerules;
    private Map<String, String> replaceData;
    private boolean entitySpawningEnabled;
    private World.Environment worldType;
    private boolean tokensEnabled;

}
