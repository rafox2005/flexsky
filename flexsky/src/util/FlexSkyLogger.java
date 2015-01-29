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

import data.DataFile;
import data.DataSlice;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.SerializationUtils;

/**
 *
 * @author rlibardi
 */
public class FlexSkyLogger {
    
    private static Connection conn;

    public static void addSelection(int nProviders, int nModules, HashMap<String, Number> userConstraints, HashMap<String, String> parameters, HashMap<String, Integer> selectParameters, long time) {
        try {
            byte[] userConstraintsBlob = SerializationUtils.serialize(userConstraints);
            byte[] parametersBlob = SerializationUtils.serialize(parameters);
            byte[] selectParametersBlob = SerializationUtils.serialize(selectParameters);
            
            PreparedStatement prepStatement
                    = FlexSkyLogger.conn.prepareStatement("INSERT INTO mssf" + "(log_time, nmodules, nproviders, action, user_constraints, parameters, selection_parameters, time)"
                            + " VALUES(?, ?, ?, ?, ?, ?, ?, ?)");
            prepStatement.setDate(1, new Date(System.currentTimeMillis()));
            prepStatement.setInt(2, nModules);
            prepStatement.setInt(3, nProviders);            
           prepStatement.setString(4, "SELECT");
           prepStatement.setBytes(5, userConstraintsBlob);
           prepStatement.setBytes(6, parametersBlob);
           prepStatement.setBytes(7, selectParametersBlob);
           prepStatement.setLong(8, time);       
            prepStatement.executeUpdate();
            
        } catch (SQLException ex) {
            Logger.getLogger(FlexSkyLogger.class.getName()).log(Level.SEVERE, null, ex);
        }    
    }

    public FlexSkyLogger(String log_db_path) {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(FlexSkyLogger.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            FlexSkyLogger.conn = DriverManager.getConnection("jdbc:sqlite:" + log_db_path);
        } catch (SQLException ex) {
            Logger.getLogger(FlexSkyLogger.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void addFileLog(DataFile file, String action, double time, double throughput, double size, double size_dispersed)
    {
        try {
            PreparedStatement prepStatement
                    = FlexSkyLogger.conn.prepareStatement("INSERT INTO file" + "(file, revision, ida, action, time, throughput, size, log_time, size_dispersed)"
                            + " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)");
           prepStatement.setString(1, file.getName());
            prepStatement.setInt(2, file.getRevision());
            prepStatement.setString(3, file.getDispersalMethod());
            prepStatement.setString(4, action);
            prepStatement.setDouble(5, time); 
            prepStatement.setDouble(6, throughput);
            prepStatement.setDouble(7, size);
            prepStatement.setDate(8, new Date(System.currentTimeMillis()));
            prepStatement.setDouble(9, size_dispersed);
            prepStatement.executeUpdate();
            
        } catch (SQLException ex) {
            Logger.getLogger(FlexSkyLogger.class.getName()).log(Level.SEVERE, null, ex);
        }    
    }
    
    public static void addIDALog(DataFile file, String action, double time, double throughput, double size)
    {
        try {
            PreparedStatement prepStatement
                    = FlexSkyLogger.conn.prepareStatement("INSERT INTO ida" + "(file, revision, ida, action, time, throughput, size, log_time)"
                            + " VALUES(?, ?, ?, ?, ?, ?, ?, ?)");
           prepStatement.setString(1, file.getName());
            prepStatement.setInt(2, file.getRevision());
            prepStatement.setString(3, file.getDispersalMethod());
            prepStatement.setString(4, action);
            prepStatement.setDouble(5, time); 
            prepStatement.setDouble(6, throughput);
            prepStatement.setDouble(7, size);
            prepStatement.setDate(8, new Date(System.currentTimeMillis()));
            prepStatement.executeUpdate();
            
        } catch (SQLException ex) {
            Logger.getLogger(FlexSkyLogger.class.getName()).log(Level.SEVERE, null, ex);
        }    
    }  
 
    
    public static void addSliceLog(DataFile file, DataSlice slice, String action, double time, double throughput, double size)
    {
        try {
            PreparedStatement prepStatement
                    = FlexSkyLogger.conn.prepareStatement("INSERT INTO slice" + "(file, revision, slice_index, account, ida, action, time, throughput, size, log_time)"
                            + " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            prepStatement.setString(1, file.getName());
            prepStatement.setInt(2, file.getRevision());
            prepStatement.setInt(3, slice.getPartIndex());
            prepStatement.setString(4, slice.getAccount());
            prepStatement.setString(5, file.getDispersalMethod());
            prepStatement.setString(6, action);
            prepStatement.setDouble(7, time); 
            prepStatement.setDouble(8, throughput);
            prepStatement.setDouble(9, size);
            prepStatement.setDate(10, new Date(System.currentTimeMillis()));
            prepStatement.executeUpdate();
            
        } catch (SQLException ex) {
            Logger.getLogger(FlexSkyLogger.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void addFilePipeLog(DataFile file, String pipe, String action, double time, double throughput, double size)
    {
        try {
            PreparedStatement prepStatement
                    = FlexSkyLogger.conn.prepareStatement("INSERT INTO file_pipe" + "(file, revision, ida, pipe, action, time, throughput, size, log_time)"
                            + " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)");
            prepStatement.setString(1, file.getName());
            prepStatement.setInt(2, file.getRevision());
            prepStatement.setString(3, file.getDispersalMethod());
            prepStatement.setString(4, pipe);
            prepStatement.setString(5, action);
            prepStatement.setDouble(6, time); 
            prepStatement.setDouble(7, throughput);
            prepStatement.setDouble(8, size);
            prepStatement.setDate(9, new Date(System.currentTimeMillis()));
            prepStatement.executeUpdate();
            
        } catch (SQLException ex) {
            Logger.getLogger(FlexSkyLogger.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void addSlicePipeLog(DataFile file, DataSlice slice, String pipe, String action, double time, double throughput, double size)
    {
        try {
            PreparedStatement prepStatement
                    = FlexSkyLogger.conn.prepareStatement("INSERT INTO slice_pipe" + "(file, revision, slice_index, ida, pipe, action, time, throughput, size, log_time)"
                            + " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            prepStatement.setString(1, file.getName());
            prepStatement.setInt(2, file.getRevision());
            prepStatement.setInt(3, slice.getPartIndex());
            prepStatement.setString(4, file.getDispersalMethod());
            prepStatement.setString(5, pipe);
            prepStatement.setString(6, action);
            prepStatement.setDouble(7, time); 
            prepStatement.setDouble(8, throughput);
            prepStatement.setDouble(9, size);
            prepStatement.setDate(10, new Date(System.currentTimeMillis()));
            prepStatement.executeUpdate();
            
        } catch (SQLException ex) {
            Logger.getLogger(FlexSkyLogger.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
