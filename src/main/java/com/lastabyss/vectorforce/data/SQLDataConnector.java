package com.lastabyss.vectorforce.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Navid
 */
public class SQLDataConnector {
    
    private String user, password, host, database;
    private String url = "jdbc:mysql://";
    private Connection connection = null;
    
    public static PreparedStatement
            SQL_INSERT_SCORE,
            SQL_INSERT_NAMEHISTORY,
            SQL_INSERT_TOKENS,
            SQL_INSERT_WINS,
            SQL_INSERT_MAPCOLOR,
            SQL_SELECT_SCORES,
            SQL_SELECT_TOKENS,
            SQL_SELECT_WINS,
            SQL_SELECT_ALL_WINS,
            SQL_SELECT_ALL_TOKENS,
            SQL_SELECT_ALL_SCORES,
            SQL_SELECT_HIGHEST_SCORE,
            SQL_SELECT_NAMEHISTORY,
            SQL_SELECT_MAPCOLOR;

    public SQLDataConnector(String user, String password, String host, String database) {
        this.user = user;
        this.password = password;
        this.host = host + "/" + database;
        this.database = database;
        this.url += this.host;
    }
    
    public void checkTables() {
        updateMultiline(Queries.getQuery("createTables"));
    }
    
    public void insertHighScore(UUID uuid, int distance, String mapname) {
        update(SQL_INSERT_SCORE, uuid.toString(), distance, mapname, distance);
    }
    
    public void insertNameHistory(UUID uuid, String name) {
        update(SQL_INSERT_NAMEHISTORY, uuid.toString(), name, name);
    }
    
    public void insertWins(UUID uuid, int wins) {
        update(SQL_INSERT_WINS, uuid.toString(), wins, wins);
    }
    
    public void insertTokens(UUID uuid, int tokens) {
        update(SQL_INSERT_TOKENS, uuid.toString(), tokens, tokens);
    }
    
    public void insertMapColor(String mapname, String colored) {
        update(SQL_INSERT_MAPCOLOR, mapname, colored, colored);
    }
    
    public ResultSet getWins(UUID uuid) {
        return query(SQL_SELECT_WINS, uuid.toString());
    }
    
    public ResultSet getAllWins() {
        return query(SQL_SELECT_ALL_WINS);
    }
    
    public ResultSet getTokens(UUID uuid) {
        return query(SQL_SELECT_TOKENS, uuid.toString());
    }
    
    public ResultSet getAllTokens() {
        return query(SQL_SELECT_ALL_TOKENS);
    }
    
    public ResultSet getAllScores() {
        return query(SQL_SELECT_ALL_SCORES);
    }
    
    public ResultSet getNameHistory(UUID uuid) {
        return query(SQL_SELECT_NAMEHISTORY, uuid.toString());
    }
    
    public ResultSet getMapColor(String mapname) {
        return query(SQL_SELECT_MAPCOLOR, mapname);
    }
    
    /**
     * Returns a player's highest score in a map.
     * @param uuid
     * @param map
     * @return 
     */
    public ResultSet getScore(UUID uuid, String map) {
        return query(SQL_SELECT_SCORES, uuid.toString(), map);
    }
    
    /**
     * Returns the single highest score in a map.
     * @param map
     * @return 
     */
    public ResultSet getHighestScore(String map) {
        return query(SQL_SELECT_HIGHEST_SCORE, map);
    }
    
    public int update(PreparedStatement update, Object... args) {
        try {
            Queries.setValues(update, args);
            return update.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(SQLDataConnector.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }
    
    public ResultSet query(PreparedStatement query, Object... args) {
        try {
            Queries.setValues(query, args);
            return query.executeQuery();
        } catch (SQLException ex) {
            Logger.getLogger(SQLDataConnector.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    /**
     * This executes an update with a multilined script with a ";" delimiter.
     * @param script
     * @param params
     * @return 
     */
    public List<Integer> updateMultiline(String script, Object... params) {
        String[] statements = script.split(";");
        List<Integer> list = new ArrayList<>();
        for (String s : statements) {
            if (s.isEmpty()) continue;
            s = s.trim();
            try {
                PreparedStatement statement = connection.prepareStatement(s);
                Queries.setValues(statement, params);
                list.add(statement.executeUpdate());
            } catch (SQLException ex) {
                Logger.getLogger(SQLDataConnector.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return list;
    }
    
    public void connect() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(SQLDataConnector.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            connection = DriverManager.getConnection(url, user, password);
            SQL_INSERT_NAMEHISTORY = connection.prepareStatement(Queries.getQuery("insertNameHistory"));
            SQL_INSERT_SCORE = connection.prepareStatement(Queries.getQuery("insertHighScore"));
            SQL_INSERT_TOKENS = connection.prepareStatement(Queries.getQuery("insertTokens"));
            SQL_INSERT_WINS = connection.prepareStatement(Queries.getQuery("insertWins"));
            SQL_INSERT_MAPCOLOR = connection.prepareStatement(Queries.getQuery("insertMapColor"));
            SQL_SELECT_ALL_TOKENS = connection.prepareStatement(Queries.getQuery("selectAllTokens"));
            SQL_SELECT_ALL_WINS = connection.prepareStatement(Queries.getQuery("selectAllWins"));
            SQL_SELECT_ALL_SCORES = connection.prepareStatement(Queries.getQuery("selectAllScores"));
            SQL_SELECT_SCORES = connection.prepareStatement(Queries.getQuery("selectScores"));
            SQL_SELECT_TOKENS = connection.prepareStatement(Queries.getQuery("selectTokens"));
            SQL_SELECT_WINS = connection.prepareStatement(Queries.getQuery("selectWins"));
            SQL_SELECT_HIGHEST_SCORE = connection.prepareStatement(Queries.getQuery("selectHighestScore"));
            SQL_SELECT_NAMEHISTORY = connection.prepareStatement(Queries.getQuery("selectNameHistory"));
            SQL_SELECT_MAPCOLOR = connection.prepareStatement(Queries.getQuery("selectMapColor"));
            checkTables();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public String getUser() {
        return user;
    }

    public String getUrl() {
        return url;
    }

    public String getHost() {
        return host;
    }

    public String getDatabase() {
        return database;
    }

    public Connection getConnection() {
        return connection;
    }
    
    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException ex) {
            Logger.getLogger(SQLDataConnector.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
}
