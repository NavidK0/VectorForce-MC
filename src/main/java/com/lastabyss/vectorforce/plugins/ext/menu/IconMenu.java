package com.lastabyss.vectorforce.plugins.ext.menu;

import java.util.Arrays;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
 
/**
 * Found this class on Bukkit, very helpful
 * 
 */
public class IconMenu implements Listener {
 
    private String name;
    private int size;
    private OptionClickEventHandler handler;
    private Plugin plugin;
    private Player player;
    
 
    private String[] optionNames;
    private ItemStack[] optionIcons;
 
    public IconMenu(String name, int size, OptionClickEventHandler handler, Plugin plugin, Player player) {
        this.name = name;
        this.size = size;
        this.handler = handler;
        this.plugin = plugin;
        this.optionNames = new String[size];
        this.optionIcons = new ItemStack[size];
        this.player = player;
    }
    
    public IconMenu register() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        return this;
    }
 
    public IconMenu setOption(int position, ItemStack icon, String name, String... info) {
        optionNames[position] = name;
        optionIcons[position] = setItemNameAndLore(icon, name, info);
        return this;
    }
    public void open(Player player) {
        Inventory inventory = Bukkit.createInventory(player, size, name);
        for (int i = 0; i < optionIcons.length; i++) {
            if (optionIcons[i] != null) {
                inventory.setItem(i, optionIcons[i]);
            }
        }
        player.openInventory(inventory);
    }
 
    public void destroy() {
        HandlerList.unregisterAll(this);
        handler = null;
        plugin = null;
        optionNames = null;
        optionIcons = null;
    }
 
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getTitle().equals(name) && (event.getWhoClicked().equals(player))) {
            event.setCancelled(true);
            if (event.getClick() != ClickType.RIGHT && event.getClick() != ClickType.LEFT)
                return;
            int slot = event.getRawSlot();
            if (slot >= 0 && slot < size && optionNames[slot] != null) {
                Plugin pl = this.plugin;
                OptionClickEvent e = new OptionClickEvent(this, (Player) event.getWhoClicked(), slot, optionNames[slot], optionIcons[slot]);
                handler.onOptionClick(e);
                ((Player) event.getWhoClicked()).updateInventory();
                if (e.willClose()) {
                    final Player p = (Player) event.getWhoClicked();
                    Bukkit.getScheduler().runTask(pl, p::closeInventory);
                }
                if (e.willDestroy()) {
                    destroy();
                }
            }
        }
    }
    
    @EventHandler (priority = EventPriority.MONITOR)
    public void onClose(InventoryCloseEvent evt) {
        if (evt.getPlayer() instanceof Player) {
        if (evt.getInventory().getTitle().equals(name) && evt.getPlayer().equals(player)) {
            destroy();
        }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemDrop(PlayerDropItemEvent evt) {
        if (evt.getPlayer().equals(player)) {
            evt.setCancelled(true);
            evt.getPlayer().sendMessage(ChatColor.RED + "[MicroEvents] You cannot drop items while a menu is open!");
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public OptionClickEventHandler getHandler() {
        return handler;
    }

    public void setHandler(OptionClickEventHandler handler) {
        this.handler = handler;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public void setPlugin(Plugin plugin) {
        this.plugin = plugin;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public String[] getOptionNames() {
        return optionNames;
    }

    public void setOptionNames(String[] optionNames) {
        this.optionNames = optionNames;
    }

    public ItemStack[] getOptionIcons() {
        return optionIcons;
    }

    public void setOptionIcons(ItemStack[] optionIcons) {
        this.optionIcons = optionIcons;
    }
 
    public interface OptionClickEventHandler {
        void onOptionClick(OptionClickEvent event);
    }
 
    public class OptionClickEvent {
        private Player player;
        private int position;
        private String name;
        private boolean close;
        private boolean destroy;
        private ItemStack item;
        private IconMenu menu;
 
        public OptionClickEvent(IconMenu menu, Player player, int position, String name, ItemStack item) {
            this.menu = menu;
            this.player = player;
            this.position = position;
            this.name = name;
            this.close = true;
            this.destroy = true;
            this.item = item;
        }
 
        public Player getPlayer() {
            return player;
        }
 
        public int getPosition() {
            return position;
        }
 
        public String getName() {
            return name;
        }
 
        public boolean willClose() {
            return close;
        }
 
        public boolean willDestroy() {
            return destroy;
        }
 
        public void setWillClose(boolean close) {
            this.close = close;
        }
 
        public void setWillDestroy(boolean destroy) {
            this.destroy = destroy;
        }
 
        public ItemStack getItem() {
            return item;
        }

        public IconMenu getMenu() {
            return menu;
        }
    }
 
    private ItemStack setItemNameAndLore(ItemStack item, String name, String[] lore) {
        ItemMeta im = item.getItemMeta();
        im.setDisplayName(name);
        im.setLore(Arrays.asList(lore));
        item.setItemMeta(im);
        return item;
    }
 
}
