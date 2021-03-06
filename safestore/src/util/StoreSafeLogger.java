/*
 * Copyright 2014 rlibardi.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package util;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rlibardi
 */
public class StoreSafeLogger {
    
    private static Connection conn;

    public StoreSafeLogger(String log_db_path) {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(StoreSafeLogger.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            StoreSafeLogger.conn = DriverManager.getConnection("jdbc:sqlite:" + log_db_path);
        } catch (SQLException ex) {
            Logger.getLogger(StoreSafeLogger.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void addLog(String table, String id, String action, Date start, Date end)
    {
        try {
            PreparedStatement prepStatement
                    = StoreSafeLogger.conn.prepareStatement("INSERT INTO " + table + "(id, action, requestTime)"
                            + " VALUES(?, ?, ?)");
            prepStatement.setString(1, id);
            prepStatement.setString(2, action);
            prepStatement.setDate(3, new Date(end.getTime()-start.getTime()));            
            prepStatement.executeUpdate();
            
        } catch (SQLException ex) {
            Logger.getLogger(StoreSafeLogger.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
