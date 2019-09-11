package com.lastabyss.vectorforce.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author Navid
 */
public class Queries {

    /**
     * Gets a query from an sql file
     * located in /sql/<i>name</i>.sql
     * 
     * Returns null if the .sql file doesn't exist.
     * @param name
     * @return 
     */
    public static String getQuery(String name) {
        try {
            InputStream in = Queries.class.getResourceAsStream("/sql/" + name + ".sql");
            String s = IOUtils.toString(in, "UTF-8");
            IOUtils.closeQuietly(in);
            return s.trim();
        } catch (IOException ex) {
            Logger.getLogger(Queries.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public static void setValues(PreparedStatement preparedStatement, Object... values) throws SQLException {
        for (int i = 0; i < values.length; i++) {
            preparedStatement.setObject(i + 1, values[i]);
        }
    }
    
    public static Reader stringToReader(String string) {
        return new BufferedReader(new StringReader(string));
    }
}
