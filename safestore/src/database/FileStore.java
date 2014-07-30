/*
 * Copyright 2014 rafox.
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
package database;

import data.StoreSafeFile;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import management.StorageOptions;
import org.apache.commons.lang3.SerializationUtils;
import pipeline.IPipeProcess;

/**
 * Class to deal with File Store Database Access
 *
 * @author rafox
 */
public class FileStore
{

    private final Connection conn;
    private String name;

    /**
     * Default constructor for a FileStore, passing the DB connection object
     *
     * @param conn Connection object from the Database
     */
    public FileStore(String name, Connection conn)
    {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(AccountStore.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.conn = conn;
        this.name = name;
    }

    /**
     * Get the value of name
     *
     * @return the value of name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Set the value of name
     *
     * @param name new value of name
     */
    public void setName(String name)
    {
        this.name = name;
    }

    
    private String serializePipeline(ArrayList<IPipeProcess> list)
    {
        String pipelineString = new String();
        for (Iterator<IPipeProcess> it = list.iterator(); it.hasNext();) {
            IPipeProcess pipe = it.next();
            pipelineString += pipe.getClass().getName(); 
            
            if (it.hasNext()) {
                pipelineString += "-";
            }
            
        }
        
        return pipelineString;
    }
    
    private ArrayList<IPipeProcess> deSerializePipeline(String pipeString)
    {
        ArrayList<IPipeProcess> list = new ArrayList();
        String[] pipeStringList = pipeString.split("-");
        
        for (String stringPipe : pipeStringList) {
            try {            
                list.add((IPipeProcess) Class.forName(stringPipe).newInstance());
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(FileStore.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InstantiationException ex) {
                Logger.getLogger(FileStore.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(FileStore.class.getName()).log(Level.SEVERE, null, ex);
            }
        }       
        
        return list;
    }
    
    /**
     *
     * @param file
     * @return
     */
    public boolean insertFile(StoreSafeFile file)
    {
        try {
            //Get File Pipe Info            
            byte[] parametersBlob = SerializationUtils.serialize(file.getOptions().additionalParameters);
            String pipeString = this.serializePipeline(file.getOptions().filePipeline);
            
            PreparedStatement prepStatement
                    = this.conn.prepareStatement("INSERT INTO files(name, size, type, dispersal_method, total_parts, req_parts, hash, last_accessed, last_modified, revision, pipeline, parameters)"
                            + " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            prepStatement.setString(1, file.getName());
            prepStatement.setLong(2, file.getSize());
            prepStatement.setString(3, file.getType());
            prepStatement.setString(4, file.getDispersalMethod());
            prepStatement.setInt(5, file.getTotalParts());
            prepStatement.setInt(6, file.getReqParts());
            prepStatement.setString(7, file.getHash());
            prepStatement.setDate(8, file.getLastAccessed());
            prepStatement.setDate(9, file.getLastModified());
            prepStatement.setInt(10, file.getRevision());
            prepStatement.setString(11, pipeString);
            prepStatement.setBytes(12, parametersBlob);
            prepStatement.executeUpdate();

            file.setId(this.getFileID(file));

            return true;
        } catch (SQLException ex) {
            Logger.getLogger(AccountStore.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    /**
     *
     * @return
     */
    public ArrayList<StoreSafeFile> getFiles()
    {
        ResultSet rs;
        try {
            ArrayList<StoreSafeFile> list = new ArrayList<>();
            PreparedStatement prepStatement = conn.prepareStatement("SELECT * FROM files");
            rs = prepStatement.executeQuery();
            while (rs.next()) {
                
            //Pipeline Stuff
            StorageOptions options = new StorageOptions();    
            ArrayList pipeline = this.deSerializePipeline(rs.getString("pipeline"));
            options.additionalParameters = SerializationUtils.deserialize(rs.getBytes("parameters"));
            options.filePipeline = pipeline;                
                
                StoreSafeFile file = new StoreSafeFile(rs.getInt("id"),
                        rs.getString("name"),
                        rs.getLong("size"),
                        rs.getString("type"),
                        rs.getString("dispersal_method"),
                        rs.getInt("total_parts"),
                        rs.getInt("req_parts"),
                        rs.getString("hash"),
                        rs.getDate("last_accessed"),
                        rs.getDate("last_modified"),
                        rs.getInt("revision"),
                        options);
                list.add(file);
            }
            return list;
        } catch (SQLException ex) {
            Logger.getLogger(AccountStore.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /**
     *
     * @param file
     * @return
     */
    public boolean deleteFile(StoreSafeFile file)
    {
        try {
            PreparedStatement prepStatement = conn.prepareStatement("DELETE FROM files WHERE name=? AND revision=?");
            prepStatement.setString(1, file.getName());
            prepStatement.setInt(2, file.getRevision());
            prepStatement.executeUpdate();
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(AccountStore.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public int getFileID(StoreSafeFile file)
    {
        try {
            ResultSet rs;
            PreparedStatement prepStatement = conn.prepareStatement("SELECT id FROM files WHERE name=? AND revision=?");
            prepStatement.setString(1, file.getName());
            prepStatement.setInt(2, file.getRevision());
            rs = prepStatement.executeQuery();
            return rs.getInt("id");
        } catch (SQLException ex) {
            Logger.getLogger(FileStore.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }

    }
    
    public boolean getFile(StoreSafeFile file)
    {
        try {
            
            ResultSet rs;
            PreparedStatement prepStatement = conn.prepareStatement("SELECT * FROM files WHERE name=? AND revision=?");
            prepStatement.setString(1, file.getName());
            prepStatement.setInt(2, file.getRevision());
            rs = prepStatement.executeQuery();
            if (rs.getInt("id") < 0) new SQLException("file not found");
            
            //Pipeline Stuff
            StorageOptions options = new StorageOptions();    
            ArrayList pipeline = this.deSerializePipeline(rs.getString("pipeline"));
            options.additionalParameters = SerializationUtils.deserialize(rs.getBytes("parameters"));
            options.filePipeline = pipeline;
            
            file.setDispersalMethod(rs.getString("dispersal_method"));
            file.setHash(rs.getString("hash"));
            file.setId(rs.getInt("id"));
            file.setLastAccessed(rs.getDate("last_accessed"));
            file.setLastModified(rs.getDate("last_modified"));
            file.setReqParts(rs.getInt("req_parts"));
            file.setSize(rs.getLong("size"));
            file.setTotalParts(rs.getInt("total_parts"));
            file.setType(rs.getString("type"));           
            
            file.setOptions(options);
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(FileStore.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

    }

    public boolean updateHash(StoreSafeFile file)
    {
        try {
            PreparedStatement prepStatement = conn.prepareStatement("UPDATE files SET hash=? WHERE name=? AND revision=?");
            prepStatement.setString(1, file.getHash());
            prepStatement.setString(2, file.getName());
            prepStatement.setInt(3, file.getRevision());
            prepStatement.executeUpdate();
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(AccountStore.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
    public boolean updateLastAccessedDate(StoreSafeFile file)
    {
        try {
            PreparedStatement prepStatement = conn.prepareStatement("UPDATE files SET last_accessed=? WHERE name=? AND revision=?");
            prepStatement.setDate(1, file.getLastAccessed());
            prepStatement.setString(2, file.getName());
            prepStatement.setInt(3, file.getRevision());
            prepStatement.executeUpdate();
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(AccountStore.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
}
