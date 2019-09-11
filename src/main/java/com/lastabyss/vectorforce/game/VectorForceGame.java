package com.lastabyss.vectorforce.game;

import com.lastabyss.vectorforce.VectorForce;
import com.lastabyss.vectorforce.audio.AudioSystem;
import com.lastabyss.vectorforce.map.Theme;
import com.lastabyss.vectorforce.util.Util;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.BlockVector;

/**
 *
 * @author Navid
 */
public final class VectorForceGame implements Runnable {
    
    public static float STARTING_SPEED = 0.4f;
    public static float SPEEDUP = 0.1f;
    public static float SPEEDUP_INTERVAL = 20;
    public static int TIME = 301;
    public static int FIRST_TOKENS = 10;
    public static int SECOND_TOKENS = 6;
    public static int THIRD_TOKENS = 3;
    public static int OTHER_TOKENS = 1;
    public static int HIGHSCORE_TOKENS = 20;
    
    private VectorForce plugin;
    private VectorForceHandler handler;
    private AudioSystem audio;
    private List<Player> players;
    private List<Player> alive = new ArrayList<>();
    private int waitTime = 180;
    private int counter = waitTime;
    private State state = State.WAITING;
    private Map<Integer, Player> distances = new TreeMap<>();
    private List<Player> readied = new ArrayList<>();
    private Map<Player, Theme> ballotMap = new HashMap<>();
    private boolean specialRoundEnabled = false;
    private boolean blockBreakingEnabled = false;
    private boolean blockExplosionsEnabled = false;
    private SpecialRound specialRound = null;
    private SpecialRound.Type specialRoundType = null;
    private Motivator motivator = null;
    private Motivator.Type motivatorType = null;
    private float speed = STARTING_SPEED;
    private int difficulty = 1;
    private int potionDifficulty = 0;
    private boolean potionMode = false;
    private int timeElapsed = -1;
    private boolean announceHardMode = true;
    private boolean motivatorEnabled = false;
    private boolean motivatorFlag = false;
    private boolean pvpEnabled = false;
    private boolean worldGenerated = false;
    private Theme forceMap;
    private int highscore = 0;
    
    private Scoreboard scoreboard;
    private Objective objective;
    
    private BukkitTask scoreboardUpdaterTask;
    private ScoreboardUpdater scoreboardUpdater;
    
    @SuppressWarnings("BroadCatchBlock")
    public VectorForceGame(VectorForceHandler handler, List<Player> players) {
        this.handler = handler;
        this.players = players;
        this.plugin = handler.getPlugin();
        this.audio = plugin.getAudioSystem();
        setDefaults();
        Bukkit.broadcastMessage(Util.PREFIX + ChatColor.BLUE + "Voting has begun! Vote for the next map with /vote!");
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        try {
            scoreboard.getObjective("vectorforce").unregister();
        } catch (Exception e){}
        objective = scoreboard.registerNewObjective("vectorforce", "dummy");
        objective.setDisplayName(ChatColor.GREEN + "/vote for the next map!");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        scoreboardUpdater = new ScoreboardUpdater(this);
        scoreboardUpdaterTask = Bukkit.getScheduler().runTaskTimer(plugin, scoreboardUpdater, 0, 5);
        players.forEach(p -> p.setScoreboard(scoreboard));
    }

    public enum State {
        WAITING,
        STARTING,
        RACING,
        FINISH,
        SPECIAL
    }
    
    /**
     * Set the default constants
     */
    public void setDefaults() {
        STARTING_SPEED = 0.4f;
        SPEEDUP = 0.1f;
        SPEEDUP_INTERVAL = 20;
        TIME = 301;
        FIRST_TOKENS = plugin.getConfig().getInt("first", 10);
        SECOND_TOKENS = plugin.getConfig().getInt("second", 6);
        THIRD_TOKENS = plugin.getConfig().getInt("third", 3);
        OTHER_TOKENS = plugin.getConfig().getInt("other", 1);
        HIGHSCORE_TOKENS = plugin.getConfig().getInt("highscore-tokens", 20);
    }
    
    @Override
    public void run() {
        switch(state) {
            case WAITING:
                if (counter == waitTime) {
                    pre();
                } else if (counter == waitTime / 2) {
                    handler.sendGameMessage(Util.PREFIX + ChatColor.AQUA + "Prepare for speed!");
                    waiting();
                    handler.sendGameMessage(Util.PREFIX + ChatColor.AQUA + "The game is starting in " + Util.formatShortenedTime(counter) + "!");
                } else if (counter == 30) {
                    handler.sendGameMessage(Util.PREFIX + ChatColor.AQUA + "The game is starting in " + Util.formatShortenedTime(counter) + "!");
                } else if (counter == 10) {
                    generateWorld();
                    handler.sendGameMessage(Util.PREFIX + ChatColor.AQUA + "The game is starting in " + Util.formatShortenedTime(counter) + "!");
                } else if (counter == 3) {
                    handler.sendGameMessage(Util.PREFIX + ChatColor.AQUA + "The game is starting in " + Util.formatShortenedTime(counter) + "!");
                } else if (counter == 2) {
                    handler.sendGameMessage(Util.PREFIX + ChatColor.AQUA + "The game is starting in " + Util.formatShortenedTime(counter) + "!");
                } else if (counter == 1) {
                    handler.sendGameMessage(Util.PREFIX + ChatColor.AQUA + "The game is starting in " + Util.formatShortenedTime(counter) + "!");
                }
                if (counter < 1) {
                    if (!specialRoundEnabled) {
                        state = State.STARTING;
                        counter = 11;
                    } else {
                        state = State.SPECIAL;
                        counter = 30;
                    }
                }
                break;
            case SPECIAL:
                if (counter == 29) {
                    starting();
                    players.forEach(audio::stopSounds);
                    handler.sendRaceTitle(ChatColor.RED + "Special Round!", ChatColor.DARK_RED + "" +  ChatColor.MAGIC + "!.......................!", 18);
                    players.forEach(p -> alive.add(p));
                    handler.lockSpawnChunk(true);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> starting(), 30L);
                    audio.broadcastMusic("specialround", players);
                    audio.broadcastSound("announcer/specialround", players, 1.0);
                } else if (counter == 14) {
                    players.forEach(audio::stopSounds);
                    specialRound = new SpecialRound(plugin, plugin.getMapManager(), alive, this);
                    if (specialRoundType != null) {
                        specialRound.setModifier(specialRoundType);
                    }
                    handler.sendRaceTitle(ChatColor.RED + "Special Round!", specialRound.getSpecialRound());
                } else if (counter == 10) {
                    handler.sendRaceTitle(ChatColor.GOLD + "Race is about to start!", ChatColor.BLUE + "Get ready!");
                    audio.broadcastMusic("announcer/starting2", players);
                } else if (counter <= 3 && counter >= 1) {
                    handler.sendRaceTitle(ChatColor.GOLD + "Race starts in...", ChatColor.BLUE.toString() + counter);
                    audio.broadcastSound("beep", players, 1);
                } else if (counter < 1) {
                    preGo();
                }
                break;
            case STARTING:
                if (counter == 10) {
                    starting();
                    players.forEach(audio::stopSounds);
                    handler.sendRaceTitle(ChatColor.GOLD + "Race is about to start!", ChatColor.BLUE + "Get ready!");
                    audio.broadcastMusic("announcer/starting", players);
                    players.forEach(p -> alive.add(p));
                    handler.lockSpawnChunk(true);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> starting(), 30L);
                } else if (counter == 3) {
                    handler.sendRaceTitle(ChatColor.GOLD + "Race starts in...", ChatColor.BLUE + "3");
                    audio.broadcastSound("beep", players, 1);
                } else if (counter == 2) {
                    handler.sendRaceTitle(ChatColor.GOLD + "Race starts in...", ChatColor.BLUE + "2");
                    audio.broadcastSound("beep", players, 1);
                } else if (counter == 1) {
                    handler.sendRaceTitle(ChatColor.GOLD + "Race starts in...", ChatColor.BLUE + "1");
                    audio.broadcastSound("beep", players, 1);
                } else if (counter < 1) {
                    preGo();
                }
                break;
            case RACING:
                if (counter < 1) {
                    state = State.FINISH;
                }
                break;
            case FINISH:
                finish();
                break;
        }
        if (counter > 0) {
            if (!readied.isEmpty() && readied.size() >= players.size()) {
                Bukkit.broadcastMessage(Util.PREFIX + ChatColor.GREEN + "All players have readied up!\n" + Util.PREFIX + ChatColor.GREEN + "The game will start in 10 seconds!");
                generateWorld();
                counter = 10;
                readied.clear();
            }
            if (state == State.RACING) {
                if (motivatorEnabled && !motivatorFlag) {
                    motivatorFlag = true;
                    motivator = new Motivator(plugin, plugin.getMapManager(), alive, this);
                    if (motivatorType != null) {
                        motivator.setMotivatorType(motivatorType);
                    }
                    handler.sendRaceTitle("", ChatColor.GRAY + "Motivator: " + ChatColor.RESET + motivator.generateMotivator(), 5);
                    players.forEach(x -> x.playSound(x.getLocation(), "ambient.cave.cave", Integer.MAX_VALUE, 1));
                }
                if (timeElapsed >= SPEEDUP_INTERVAL && timeElapsed % SPEEDUP_INTERVAL == 0) {
                    if (handler.getSpeed() < 1.0f) {
                        difficulty++;
                        speed += SPEEDUP;
                        if (speed >= 1.0f) speed = 1.0f;
                        blockExplosionsEnabled = true;
                        handler.setSpeed(speed);
                        handler.sendRaceTitle(null, ChatColor.GREEN + "Speed up!", 2);
                        players.forEach(x -> x.playSound(x.getLocation(), "note.pling", Integer.MAX_VALUE, 1));
                    } else {
                        if (announceHardMode) {
                            audio.broadcastMusic("hardmode", players);
                            audio.broadcastMusic("announcer/ready", players, 91L);
                            audio.broadcastSong(players, 141);
                            announceHardMode = false;
                            potionMode = true;
                            Bukkit.getScheduler().runTaskLater(plugin, () -> motivatorEnabled = true, 20 * 6);
                            PotionEffect effect = new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, potionDifficulty, false, false);
                            handler.sendRaceTitle(null, ChatColor.DARK_RED + "Hard Mode Activated!", 2);
                            alive.forEach(p -> p.addPotionEffect(effect, true));
                            potionDifficulty++;
                        } else {
                            potionMode = true;
                            PotionEffect effect = new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, potionDifficulty, false, false);
                            handler.sendRaceTitle(null, ChatColor.RED + "Speed up!", 2);
                            alive.forEach(p -> {
                                p.addPotionEffect(effect, true);
                            });
                            players.forEach(x -> x.playSound(x.getLocation(), "game.potion.smash", Integer.MAX_VALUE, 1));
                            potionDifficulty++;
                        }
                    }
                }
                if (!potionMode) {
                        players.forEach(p -> Util.sendActionBar(p, ChatColor.GREEN + "Level: " +  ChatColor.WHITE + difficulty +
                                ChatColor.GOLD + " Time Left: " + ChatColor.AQUA + Util.formatShortenedTime(counter)));
                } else {
                    players.forEach(p -> Util.sendActionBar(p, ChatColor.RED + "Hard Mode: " +  ChatColor.RED + potionDifficulty +
                     ChatColor.GOLD + " Time Left: " + ChatColor.AQUA + Util.formatShortenedTime(counter)));
                }
                timeElapsed++;
            } else {
                players.forEach(p -> Util.sendActionBar(p, ChatColor.GOLD + " Time Left: " + ChatColor.AQUA + Util.formatShortenedTime(counter)));
            }
            counter--;
        }
        if ((alive.isEmpty() && (state == State.RACING)) || (counter < 1 && state == State.RACING) || players.isEmpty()) {
            state = State.FINISH;
        }
    }

    private void preGo() {
        handler.sendRaceTitle(ChatColor.GOLD + "Race has started!", "GO!");
        audio.broadcastSong(players, 20);
        audio.broadcastSound("highbeep", players, 1);
        handler.lockSpawnChunk(false);
        go();
        state = State.RACING;
        counter = TIME;
    }
    
    public void onJoin(Player p) {
        p.setFlying(false);
        p.setWalkSpeed(1f);
        p.setGameMode(GameMode.ADVENTURE);
        p.setMaxHealth(20);
        p.setHealth(20);
        p.setSaturation(20);
        p.setFoodLevel(20);
        p.getActivePotionEffects().stream().forEach((effect) -> {
            p.removePotionEffect(effect.getType());
        });
        p.setScoreboard(scoreboard);
    }
    
    public void onQuit(Player p) {
        alive.remove(p);
        p.setScoreboard(scoreboard);
    }
    
    
    public void pre() {
        players.forEach(this::onJoin);
    }
    
    public void onDeath(Player p, Location deathLoc) {
        if (state != State.RACING) return;
        alive.remove(p);
        int z = Math.abs(deathLoc.getBlockZ());
        p.sendMessage(Util.PREFIX + ChatColor.AQUA + "You finished at " + ChatColor.RED + z + ChatColor.AQUA + " blocks!");
        if (!distances.containsValue(p)) {
            distances.put(z, p);
            Util.async(() -> plugin.getSQL().insertHighScore(p.getUniqueId(), z, ChatColor.stripColor(plugin.getMapManager().getTheme().getName())));
        }
        Util.respawnPlayer(p);
        if (alive.size() > 0)
            audio.playSound(p, "announcer/finished", p.getLocation(), 1, 40);
        if (alive.size() == 1) {
            audio.playSound(alive.get(0), "announcer/lastone", p.getLocation(), 1);
        }
    }
    
    private void go() {
        handler.setSpeed(speed);
    }
    
    private void waiting() {
        Bukkit.broadcastMessage(ChatColor.GREEN + "Vote for the next map! /vote to change your mind later!");
        players.forEach(p -> {
            p.setFlying(false);
            p.setWalkSpeed(1.0f);
            Util.showVoteMenu(p);
        });
    }
    
    private void starting() {
        World world = plugin.getMapManager().getWorld();
        Location loc = world.getSpawnLocation().setDirection(new BlockVector(0, 0, -1));
        players.forEach(p -> {
            Location clone = loc.clone();
            p.teleport(clone);
            p.setGameMode(GameMode.SURVIVAL);
            p.setFlying(false);
            p.setMaxHealth(20);
            p.setHealth(20);
            p.setSaturation(20);
            p.setFoodLevel(20);
            p.getActivePotionEffects().clear();
        });
    }
    
    private void finish() {
        alive.forEach(p -> {
            distances.put(Math.abs(p.getLocation().getBlockZ()), p);
        });
        if (specialRound != null) {
            specialRound.endAll();
            specialRound = null;
        }
        try {
            objective.unregister();
        } catch (Exception ignored) {}
        scoreboardUpdaterTask.cancel();
        scoreboard.clearSlot(DisplaySlot.SIDEBAR);
        audio.resetAudioTimer();
        World world = Bukkit.getWorld("world");
        players.forEach(p -> {
            p.setFlying(false);
            p.setGameMode(GameMode.ADVENTURE);
            p.getActivePotionEffects().stream().forEach((activePotionEffect) -> {
                p.removePotionEffect(activePotionEffect.getType());
            });
            if (!p.isDead()) {
                p.teleport(world.getSpawnLocation());
            } else {
                Util.respawnPlayer(p);
            }
        });
        handler.setSpeed(0.2f);
        Bukkit.getScheduler().runTask(plugin, () -> {
            handler.getGameTask().cancel();
            handler.setGameTask(null);
            Bukkit.broadcastMessage(Util.PREFIX + ChatColor.BLUE + "The game has ended!");
            int c = 0;
            Set<Entry<Integer, Player>> set = distances.entrySet();
            List<Entry<Integer, Player>> list = new ArrayList<>(set);
            Collections.reverse(list);
            for (Entry<Integer, Player> entry : list) {
                int distance = Math.abs(entry.getKey());
                Player p = entry.getValue();
                if (c == 0) {
                    Bukkit.broadcastMessage(ChatColor.RED + "1st: " + p.getDisplayName() + " with " + distance + " blocks traveled!");
                    audio.playMusic(p, "first");
                    audio.playSound(p, "announcer/first", p.getLocation(), 1, 20);
                    if (distance > highscore) {
                        Bukkit.broadcastMessage(p.getDisplayName() + " has broken the previous highscore of " + highscore + "!");
                        p.sendMessage(ChatColor.AQUA + "You got " + HIGHSCORE_TOKENS + " additional token(s) for breaking the highscore!");
                    }
                } else if (c == 1) {
                    Bukkit.broadcastMessage(ChatColor.RED + "2nd: " + p.getDisplayName() + " with " + distance + " blocks traveled!");
                    audio.playMusic(p, "second");
                    audio.playSound(p, "announcer/second", p.getLocation(), 1, 20);
                } else if (c == 2) {
                    Bukkit.broadcastMessage(ChatColor.RED + "3rd: " + p.getDisplayName() + " with " + distance + " blocks traveled!");
                    audio.playMusic(p, "third");
                    audio.playSound(p, "announcer/third", p.getLocation(), 1, 20);
                } else {
                    p.sendMessage(ChatColor.RED + "You got " + distance + " blocks!");
                    audio.playSound(p, "announcer/betterlucknexttime", p.getLocation(), 1, 20);
                }
                int sw = c;
                if (isEnoughPlayers() && plugin.getMapManager().getTheme().isTokensEnabled()) {
                    Util.async(() -> {
                        ResultSet wins = plugin.getSQL().getWins(p.getUniqueId());
                        ResultSet tokens = plugin.getSQL().getTokens(p.getUniqueId());
                        try {
                            int wCount = 0;
                            int tCount = tokens.next() ? tokens.getInt("tokens") : 0;
                            switch (sw) {
                                case 0:
                                    int firstTokens = FIRST_TOKENS;
                                    if (distance > highscore)
                                        firstTokens += HIGHSCORE_TOKENS;
                                    if (firstTokens > 0)
                                        p.sendMessage(ChatColor.AQUA + "You've earned " + ChatColor.BLUE + FIRST_TOKENS + " token(s)!");
                                    wCount = wins.next() ? wins.getInt("wins") : 0;
                                    plugin.getSQL().insertWins(p.getUniqueId(), wCount + 1);
                                    plugin.getSQL().insertTokens(p.getUniqueId(), tCount + FIRST_TOKENS);
                                    break;
                                case 1:
                                    if (SECOND_TOKENS > 0)
                                        p.sendMessage(ChatColor.AQUA + "You've earned " + ChatColor.BLUE + SECOND_TOKENS + " token(s)!");
                                    wCount = wins.next() ? wins.getInt("wins") : 0;
                                    plugin.getSQL().insertWins(p.getUniqueId(), wCount + 1);
                                    plugin.getSQL().insertTokens(p.getUniqueId(), tCount + SECOND_TOKENS);
                                    break;
                                case 2:
                                    if (THIRD_TOKENS > 0)
                                        p.sendMessage(ChatColor.AQUA + "You've earned " + ChatColor.BLUE + THIRD_TOKENS + " token(s)!");
                                    wCount = wins.next() ? wins.getInt("wins") : 0;
                                    plugin.getSQL().insertWins(p.getUniqueId(), wCount + 1);
                                    plugin.getSQL().insertTokens(p.getUniqueId(), tCount + THIRD_TOKENS);
                                    break;
                                default:
                                    if (OTHER_TOKENS > 0)
                                        p.sendMessage(ChatColor.AQUA + "You've earned " + ChatColor.BLUE + OTHER_TOKENS + " token(s)!");
                                    plugin.getSQL().insertTokens(p.getUniqueId(), tCount + OTHER_TOKENS);
                                    break;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                } else if (!plugin.getMapManager().getTheme().isTokensEnabled()) {
                    p.sendMessage(Util.PREFIX + ChatColor.RED + "Tokens are disabled for this map!");
                } else if (!isEnoughPlayers()) {
                    Util.async(() -> {
                        try {
                            p.sendMessage(Util.PREFIX + ChatColor.RED + "There weren't enough players (" + handler.getMinPlayers() + " min); wins and tokens were not given.");
                            ResultSet tokens = plugin.getSQL().getTokens(p.getUniqueId());
                            int tCount = tokens.next() ? tokens.getInt("tokens") : 0;
                            if (distance > highscore) {
                                p.sendMessage(ChatColor.AQUA + "You've earned " + ChatColor.BLUE + HIGHSCORE_TOKENS + ChatColor.AQUA + " token(s) for breaking the highscore, however!");
                                plugin.getSQL().insertTokens(p.getUniqueId(), tCount + HIGHSCORE_TOKENS);
                            }
                        } catch (SQLException ex) {
                            Logger.getLogger(VectorForceGame.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    });
                }
                c++;
            }
            plugin.getMapManager().deleteWorld();
            Bukkit.broadcastMessage(Util.PREFIX + ChatColor.GOLD + "The next game will start in 10 seconds.");
            handler.restartGameFinder();
        });
    }
    
    public Player getFirstAlive() {
        Player first = null;
        for (Player p : alive) {
            if (first == null) {
                first = p;
                continue;
            }
            int z = Math.abs(p.getLocation().getBlockZ());
            if (z > Math.abs(first.getLocation().getBlockZ())) {
                first = p;
            }
        }
        return first;
    }
    
    public Player getLastAlive() {
        Player last = null;
        for (Player p : alive) {
            if (last == null) {
                last = p;
                continue;
            }
            int z = Math.abs(p.getLocation().getBlockZ());
            if (z < Math.abs(last.getLocation().getBlockZ())) {
                last = p;
            }
        }
        return last;
    }
    
    public void terminate() {
        counter = 0;
        state = State.FINISH;
    }
    
    public int getWaitTime() {
        return waitTime;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public int getCounter() {
        return counter;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }
    
    public AudioSystem getAudio() {
        return audio;
    }

    public List<Player> getAlive() {
        return alive;
    }

    public boolean isSpecialRoundEnabled() {
        return specialRoundEnabled;
    }
    
    public void setSpecialRoundEnabled(boolean specialRound) {
        this.specialRoundEnabled = specialRound;
    }

    public boolean isBlockExplosionsEnabled() {
        return blockExplosionsEnabled;
    }

    public void setBlockExplosionsEnabled(boolean blockExplosionsEnabled) {
        this.blockExplosionsEnabled = blockExplosionsEnabled;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setPotionDifficulty(int potionDifficulty) {
        this.potionDifficulty = potionDifficulty;
    }

    public int getPotionDifficulty() {
        return potionDifficulty;
    }

    public boolean isPotionMode() {
        return potionMode;
    }

    public void setPotionMode(boolean potionMode) {
        this.potionMode = potionMode;
    }

    public VectorForceHandler getHandler() {
        return handler;
    }

    public SpecialRound.Type getSpecialRoundType() {
        return specialRoundType;
    }

    public void setSpecialRoundType(SpecialRound.Type type) {
        specialRoundEnabled = true;
        this.specialRoundType = type;
    }

    public boolean isMotivatorEnabled() {
        return motivatorEnabled;
    }

    public void setMotivatorEnabled(boolean motivatorEnabled) {
        this.motivatorEnabled = motivatorEnabled;
    }

    public void setMotivatorType(Motivator.Type type) {
        this.motivatorType = type;
    }

    public Motivator.Type getMotivatorType() {
        return motivatorType;
    }

    public Motivator getMotivator() {
        return motivator;
    }
    
    /**
     * Returns true if there's enough players for win tokens.
     * @return 
     */
    public boolean isEnoughPlayers() {
        return distances.size() >= handler.getMinPlayers();
    }
    
    public void generateWorld() {
        Theme nextmap;
        if (!ballotMap.isEmpty() && forceMap == null) {
            Map<Theme, Integer> v = new HashMap<>();
            ballotMap.entrySet().stream().map((e) -> e.getValue()).forEach((t) -> {
                int votes = v.get(t) != null ? v.get(t) + 1 : 1;
                v.put(t, votes);
            });
            Bukkit.broadcastMessage(ChatColor.GREEN + "Votes: ");
            v.entrySet().stream().forEach((e) -> {
                Bukkit.broadcastMessage("Theme: " + e.getKey().getDisplayName() + " Votes: " + e.getValue());
            });
            nextmap = v.keySet().iterator().next();
            int highest = 0;
            for (Entry<Theme, Integer> e : v.entrySet()) {
                Theme key = e.getKey();
                int votes = e.getValue();
                if (votes > highest) {
                    nextmap = key;
                    highest = votes;
                }
            }
        } else if (forceMap != null) {
            nextmap = forceMap;
        } else {
            List<Theme> copy = new ArrayList<>(plugin.getMapManager().getThemes());
            Collections.shuffle(copy);
            nextmap = copy.get(0);
        }
        worldGenerated = true;
        Bukkit.broadcastMessage(ChatColor.GREEN + "The next map is: " + nextmap.getDisplayName());
        VectorForceHandler.previouslyVoted.put(nextmap.getName(), VectorForceHandler.MAP_COOLDOWN);
        plugin.getMapManager().setTheme(nextmap);
        final Theme t = nextmap;
        if (!t.isTokensEnabled()) {
            Bukkit.broadcastMessage(ChatColor.AQUA + "Tokens are disabled for this map!");
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            ResultSet highestScore = plugin.getSQL().getHighestScore(t.getName());
            try {
                if (highestScore != null && highestScore.next()) {
                    String str = highestScore.getString("uuid");
                    if (str != null) {
                        UUID uuid = UUID.fromString(str);
                        String user = "by N/A";
                        ResultSet history = plugin.getSQL().getNameHistory(uuid);
                        if (history.next()) {
                            String username = history.getString("username");
                            user = user.replace("N/A", ChatColor.RED + username);
                        }
                        highscore = highestScore.getInt("distance");
                        Bukkit.broadcastMessage(Util.PREFIX + ChatColor.AQUA + "The highscore of this map is " + ChatColor.RED + highscore + " blocks "
                                + ChatColor.AQUA + user + ".");
                    }
                } else {
                    Bukkit.broadcastMessage(Util.PREFIX + ChatColor.AQUA + "There is no highscore for this map... yet.");
                }
            } catch (SQLException ex) {
                Logger.getLogger(VectorForceGame.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Bukkit.broadcastMessage(Util.PREFIX + ChatColor.AQUA + "The VectorForce world is regenerating, you may experience some slight lag.");
            plugin.getMapManager().createWorld();
            Bukkit.broadcastMessage(Util.PREFIX + ChatColor.AQUA + "All done!");
            float chance = Util.random.nextFloat();
            if (chance < 0.3) {
                specialRoundEnabled = true;
            }
            Bukkit.broadcastMessage(Util.PREFIX + ChatColor.AQUA + "VectorForce will begin soon! Get ready!");
            handler.setSelection(new CuboidSelection(
                    plugin.getMapManager().getWorld(),
                    new com.sk89q.worldedit.BlockVector(0, 0, 15),
                    new com.sk89q.worldedit.BlockVector(15, 256, 0))
            );
            handler.setSession(Util.wePlugin.getWorldEdit().getEditSessionFactory().getEditSession(new BukkitWorld(
                                    plugin.getMapManager().getWorld()),
                            -1)
            );
        }, 20L);
    }

    /**
     * Gets the current walking speed level in the game.
     * This value is different from VectorForceHandler's speed,
     * as that speed reflects all the racer's CURRENT speed, where as
     * this value reflects what the speed should be based on level.
     * @return 
     */
    public float getSpeed() {
        return speed;
    }

    /**
     * Sets the current walking speed level in the game.
     * This value is different from VectorForceHandler's speed,
     * as that speed reflects all the racer's CURRENT speed, where as
     * this value reflects what the speed should be based on level.
     * @param speed 
     */
    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public void setPvpEnabled(boolean pvpEnabled) {
        this.pvpEnabled = pvpEnabled;
    }

    public boolean isPvpEnabled() {
        return pvpEnabled;
    }

    public Map<Player, Theme> getVotes() {
        return ballotMap;
    }

    /**
     * Returns the list of players who have typed
     * /ready.
     * @return 
     */
    public List<Player> getReadied() {
        return readied;
    }

    public void setForceMap(Theme forceMap) {
        this.forceMap = forceMap;
    }

    public Theme getForceMap() {
        return forceMap;
    }

    public Scoreboard getScoreboard() {
        return scoreboard;
    }

    public Objective getObjective() {
        return objective;
    }

    public BukkitTask getScoreboardUpdaterTask() {
        return scoreboardUpdaterTask;
    }

    public ScoreboardUpdater getScoreboardUpdater() {
        return scoreboardUpdater;
    }

    public boolean isWorldGenerated() {
        return worldGenerated;
    }

    public class ScoreboardUpdater implements Runnable {
        VectorForceGame game;
        Scoreboard scoreboard;
        Objective sideBar;
        List<Player> players;
        List<Player> readied;
        List<Player> alive;
        boolean flag = true;

        public ScoreboardUpdater(VectorForceGame game) {
            this.scoreboard = game.getScoreboard();
            this.sideBar = game.getObjective();
            this.game = game;
            this.readied = game.getReadied();
            this.alive = game.getAlive();
            this.players = game.getHandler().getPlayers();
        }

        @Override
        public void run() {
            if (game.getState() == State.WAITING) {
                players.forEach(p -> {
                    if (readied.contains(p)) {
                        sideBar.getScore(p.getName()).setScore(1);
                    } else {
                        sideBar.getScore(p.getName()).setScore(0);
                    }
                });
            } else if (game.getState() == State.SPECIAL) {
                sideBar.setDisplayName(ChatColor.GREEN + "Starting...");
            } else if (game.getState() == State.STARTING) {
                sideBar.setDisplayName(ChatColor.GREEN + "Starting...");
            } else if (game.getState() == State.RACING) {
                if (flag) {
                    scoreboard.clearSlot(DisplaySlot.SIDEBAR);
                    sideBar.unregister();
                    sideBar = scoreboard.registerNewObjective("VFDistances", "dummy");
                    sideBar.setDisplaySlot(DisplaySlot.SIDEBAR);
                    flag = false;
                }
                sideBar.setDisplayName(ChatColor.GREEN + "Racers | Song Time: " + audio.getCounter());
            }
        }

        public void updateDistances(Player p) {
            sideBar.getScore(p.getName()).setScore(Math.abs(p.getLocation().getBlockZ()));
        }

        public void setBoard(Scoreboard board) {
            this.scoreboard = board;
        }

        public void setObjective(Objective objective) {
            this.sideBar = objective;
        }

        public Scoreboard getBoard() {
            return scoreboard;
        }

        public Objective getObjective() {
            return sideBar;
        }

        public VectorForceGame getGame() {
            return game;
        }
    }
}
