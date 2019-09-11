package com.lastabyss.vectorforce.game;

import com.lastabyss.vectorforce.VectorForce;
import com.lastabyss.vectorforce.map.MapManager;
import com.lastabyss.vectorforce.map.Theme;
import com.lastabyss.vectorforce.util.Util;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.selections.Selection;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitTask;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Navid
 */
public final class VectorForceHandler implements Listener, PluginMessageListener {

    public final static int MAP_COOLDOWN = 3; //Amount of rounds to wait before unlocking the map again

    private VectorForce plugin;
    private List<Player> players = new ArrayList<>();
    private BukkitTask gameTask;
    private BukkitTask gameFinder;
    private BukkitTask entityKiller;
    private VectorForceGame game = null;
    private float speed = 1.0f;
    private Selection selection;
    private EditSession session;
    private int minPlayers;

    public static Map<String, Integer> previouslyVoted = new HashMap<>();

    @SuppressWarnings("LeakingThisInConstructor")
    public VectorForceHandler(VectorForce plugin) {
        this.plugin = plugin;
        Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getPluginManager().registerEvents(this, plugin));
        minPlayers = plugin.getConfig().getInt("min-players", 5);
        restartGameFinder();
    }

    public void start() {
        if (game == null) {
            game = new VectorForceGame(this, players);
            gameTask = Bukkit.getScheduler().runTaskTimer(plugin, game, 0, 20L);
            entityKiller = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                if (game != null && plugin.getMapManager().getWorld() != null) {
                    MapManager map = plugin.getMapManager();
                    Theme theme = map.getTheme();
                    Iterator<Map.Entry<String, Integer>> iterator = previouslyVoted.entrySet().iterator();
                    Map<String, Integer> pv = new HashMap<>();
                    while (iterator.hasNext()) {
                        Map.Entry<String, Integer> e = iterator.next();
                        String k = e.getKey();
                        int v = e.getValue();
                        int left = v - 1;
                        if (left > 0) {
                            pv.put(k, left);
                        }
                    }
                    previouslyVoted = pv;
                    if (!theme.isEntitySpawningEnabled()) {
                        map.getWorld().getEntities()
                                .stream()
                                .filter(e -> e.getType() != EntityType.PLAYER)
                                .filter(e -> !e.hasMetadata("isMotivator") || !e.getMetadata("isMotivator").get(0).asBoolean()).forEach(Entity::remove);
                    } else {
                        entityKiller.cancel();
                    }
                }
            }, 0, 1L);
            gameFinder.cancel();
        }
    }

    public void stop(boolean permStop) {
        if (game != null) {
            entityKiller.cancel();
            entityKiller = null;
            game.terminate();
            game = null;
            if (!permStop)
                Bukkit.broadcastMessage(Util.PREFIX + ChatColor.AQUA + "Game is resetting, it will be ready again in 10 seconds.");
        }
    }

    public void onDisable() {
        stop(true);
        if (gameFinder != null)
            gameFinder.cancel();
    }

    @EventHandler
    public void onPlayerGamemode(PlayerGameModeChangeEvent evt) {
        if (game == null || (game.getState() == VectorForceGame.State.WAITING)) {
            boolean join;
            switch (evt.getNewGameMode()) {
                case ADVENTURE:
                    join = true;
                    break;
                case CREATIVE:
                    join = false;
                    break;
                case SPECTATOR:
                    join = false;
                    break;
                case SURVIVAL:
                    join = true;
                    break;
                default:
                    join = false;
            }
            if (join) {
                add(evt.getPlayer());
                evt.getPlayer().sendMessage(Util.PREFIX + ChatColor.GREEN + "You've joined back into the game!");
            } else {
                remove(evt.getPlayer());
                evt.getPlayer().sendMessage(Util.PREFIX + ChatColor.GREEN + "You've become a spectator!");
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent evt) {
        if (game != null) {
            if (has(evt.getEntity()) && evt.getEntity().getGameMode() == GameMode.SURVIVAL && game.getState() == VectorForceGame.State.RACING) {
                game.onDeath(evt.getEntity(), evt.getEntity().getLocation());
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (plugin.getMapManager().getWorld() != null)
                        evt.getEntity().teleport(plugin.getMapManager().getWorld().getSpawnLocation());
                    evt.getEntity().setGameMode(GameMode.SPECTATOR);
                    evt.getEntity().setFlying(true);
                }, 20L);
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent evt) {
        Util.sendTitle(evt.getPlayer(), 1, 20 * 5, 1, ChatColor.YELLOW + "The sound client", ChatColor.YELLOW + "is REQUIRED!");
        evt.getPlayer().sendMessage(ChatColor.RED + "Make sure to click on the " + ChatColor.BLUE + "Sound Client" + ChatColor.RED + " link " +
                "when it pops up! Otherwise you won't be added to the game.");
        Bukkit.getScheduler().runTaskLater(plugin, () -> Bukkit.getScheduler().runTask(plugin, () -> add(evt.getPlayer())), 5 * 20L);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent evt) {
        if (evt.isCancelled() || evt.getPlayer().hasPermission("vf.admin")) return;
        if (has(evt.getPlayer())) {
            if (game != null && !game.isBlockExplosionsEnabled() && game.getState() != VectorForceGame.State.RACING) {
                if (evt.getBlock().getWorld().getName().equals(plugin.getMapManager().getWorld().getName()))
                    evt.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent evt) {
        if (evt.isCancelled()) return;
        if (evt.getPlayer().getLocation().getBlockY() < 0 && !evt.getPlayer().isDead()) {
            if (game != null && game.getState() == VectorForceGame.State.RACING) {
                evt.getPlayer().setHealth(0);
                Util.respawnPlayer(evt.getPlayer());
            } else if (game != null && game.getState() == VectorForceGame.State.WAITING) {
                evt.getPlayer().setHealth(0);
                Util.respawnPlayer(evt.getPlayer());
            } else if (game != null && (game.getState() == VectorForceGame.State.STARTING || game.getState() == VectorForceGame.State.SPECIAL)) {
                evt.getPlayer().setHealth(0);
                Util.respawnPlayer(evt.getPlayer());
                Bukkit.getScheduler().runTaskLater(plugin, () -> evt.getPlayer().teleport(plugin.getMapManager().getWorld().getSpawnLocation()), 2L);
            }
        }
        if (game != null && game.getState() == VectorForceGame.State.RACING) {
            if (game.getAlive().contains(evt.getPlayer()))
                game.getScoreboardUpdater().updateDistances(evt.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent evt) {
        remove(evt.getPlayer());
        if (players.size() < 1) {
            restartGameFinder();
        }
    }

    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent evt) {
        if (evt.isCancelled()) return;
        if (evt.getEntity() instanceof Player) {
            if (game != null && game.getState() == VectorForceGame.State.RACING && !game.isPvpEnabled()) {
                if (evt.getDamager() instanceof Player) {
                    if (has((Player) evt.getDamager()))
                        evt.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent evt) {
        if (evt.isCancelled()) return;
        if (has((Player) evt.getEntity())) {
            evt.setFoodLevel(20);
            evt.setCancelled(true);
        }
    }

    @Override
    public void onPluginMessageReceived(String channel, Player sender, byte[] bytes) {
        if (channel.equals("OzoneMusic")) {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));
            try {
                String header = in.readUTF().toLowerCase();
                switch (header) {
                    case "onclientconnect": {
                        add(in);
                        break;
                    }
                    case "onmusicclientserverswitch": {
                        add(in);
                        break;
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void add(DataInputStream in) throws IOException {
        String playerName = in.readUTF();
        Player p = Bukkit.getPlayer(playerName);
        if (!has(p)) {
            players.add(p);
            Util.sendTitle(p, 20, 5 * 20, 20, ChatColor.AQUA + ">>" + ChatColor.WHITE + "VectorForce" + ChatColor.AQUA + "<<", ChatColor.AQUA + ""
                    + ChatColor.ITALIC + ChatColor.GREEN + "v. " + plugin.getDescription().getVersion());
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                p.setGameMode(GameMode.ADVENTURE);
                plugin.getAudioSystem().playMusic(p, plugin.getConfig().getString("intro-music"), 10);
                plugin.getAudioSystem().playSound(p, "announcer/welcome", p.getLocation(), 1, 35);
                if (game != null) {
                    game.onJoin(p);
                }
            }, 20L);
        }
    }

    public void reload() {
        if (gameTask != null) gameTask.cancel();
    }

    public void cleanup() {
        gameTask.cancel();
    }

    public List<Player> getPrepared() {
        return players;
    }

    public void add(Player p) {
        if (!has(p)) {
            plugin.getAudioSystem().sendRequired(p.getName());
            Util.async(() -> plugin.getSQL().insertNameHistory(p.getUniqueId(), p.getName()));
        }
    }

    public void remove(Player p) {
        if (has(p)) {
            players.remove(p);
            if (game != null) {
                game.onQuit(p);
            }
        }
    }

    public boolean has(Player p) {
        return players.contains(p);
    }

    public boolean has(UUID uuid) {
        return players.stream().anyMatch((player) -> (player.getUniqueId().equals(uuid)));
    }

    public void sendGameMessage(String s) {
        players.stream().forEach(p -> p.sendMessage(s));
    }

    public void sendRaceTitle(String s, String subtitle) {
        players.stream().forEach(p -> Util.sendTitle(p, 0, 3 * 20, 0, s, subtitle));
    }

    public void sendRaceTitle(String s, String subtitle, int time) {
        players.stream().forEach(p -> Util.sendTitle(p, 0, time * 20, 0, s, subtitle));
    }

    public VectorForceGame getGame() {
        return game;
    }

    public BukkitTask getGameTask() {
        return gameTask;
    }

    public VectorForce getPlugin() {
        return plugin;
    }

    public void setGameTask(BukkitTask gameTask) {
        this.gameTask = gameTask;
    }

    public void setGameFinder(BukkitTask gameFinder) {
        this.gameFinder = gameFinder;
    }

    public BukkitTask getGameFinder() {
        return gameFinder;
    }

    public void lockSpawnChunk(boolean bool) {
        if (selection == null || session == null) return;
        if (bool) {
            try {
                session.makeCuboidWalls(selection.getRegionSelector().getRegion(), new BaseBlock(Material.BARRIER.getId(), 0));
            } catch (IncompleteRegionException | MaxChangedBlocksException ex) {
                Logger.getLogger(VectorForceHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            session.undo(session);
        }

    }

    public void setMinPlayers(int minPlayers) {
        this.minPlayers = minPlayers;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
        if (game != null) {
            game.getAlive().forEach(p -> p.setWalkSpeed(speed));
        }
    }

    public float getSpeed() {
        return speed;
    }

    public void setGame(VectorForceGame game) {
        this.game = game;
    }

    public void setSelection(Selection selection) {
        this.selection = selection;
    }

    public void setSession(EditSession session) {
        this.session = session;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public EditSession getSession() {
        return session;
    }

    public Selection getSelection() {
        return selection;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void restartGameFinder() {
        if (game != null) {
            game.setState(VectorForceGame.State.FINISH);
        }
        setGameFinder(Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (players.size() > 0) {
                start();
            }
        }, 20 * 10L, 20L));
        setGame(null);
    }

    public void setEntityKiller(BukkitTask entityKiller) {
        this.entityKiller = entityKiller;
    }

    public BukkitTask getEntityKiller() {
        return entityKiller;
    }
}
