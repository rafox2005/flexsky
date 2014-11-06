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

package management;

import data.DataAccount;
import data.DataFile;
import data.DataSlice;
import database.AccountStore;
import database.FileStore;
import database.SliceStore;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rafox
 */
class DatabaseManager
{
    private AccountStore as;
    private FileStore fs;
    private SliceStore ss;
    private Connection conn;

    public DatabaseManager(String pathToDB)
    {
        try {
            Class.forName("org.sqlite.JDBC");
            
            this.conn = DriverManager.getConnection("jdbc:sqlite:" + pathToDB);
            
            this.as = new AccountStore(this.conn);
            this.fs = new FileStore("TestFS", this.conn);
            this.ss = new SliceStore("TestFS", this.conn);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DatabaseManager.class.getName()).log(Level.SEVERE, "DB: jdbc adapter driver not found", ex);
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseManager.class.getName()).log(Level.SEVERE, "DB: not able to connect with the DB", ex);
        }
    }
    
    public int insertFile(DataFile ssf)
    {
        try {
            this.fs.insertFile(ssf);
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseManager.class.getName()).log(Level.SEVERE, "DB: not able to insert file in the database", ex);
        }
        try {
            return this.fs.getFileID(ssf);
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseManager.class.getName()).log(Level.SEVERE, "DB: not able to retrieve file ID when inserting file", ex);
        }
        return 0;
    }
    
    public boolean insertSlice(DataSlice sss) 
    {
        return this.ss.insertSlice(sss);
        
        
    }
    
    public ArrayList listFiles()
    {
        try {
            return this.fs.getFiles();
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseManager.class.getName()).log(Level.SEVERE, "DB: SQL error when listing files", ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DatabaseManager.class.getName()).log(Level.SEVERE, "DB: pipeline class retrieval error when listing files DB", ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(DatabaseManager.class.getName()).log(Level.SEVERE, "DB: instatiation class error when retrieving files from DB", ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(DatabaseManager.class.getName()).log(Level.SEVERE, "DB: illegal access when listing files", ex);
        }
        return null;
    }
    
    public ArrayList listAccounts()
    {
        try {
            return this.as.getAccounts();
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseManager.class.getName()).log(Level.SEVERE, "DB: SQL error when listing accounts", ex);
        }
        return null;
    }
    
    public boolean deleteFile(DataFile file) {
        ArrayList<DataSlice> slicesToDelete = this.ss.getSlicesFromFile(file);
        for (DataSlice slice : slicesToDelete) {
            this.ss.deleteSlice(slice);
        }
        try {
            return this.fs.deleteFile(file);
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseManager.class.getName()).log(Level.SEVERE, "DB: SQL error when deleting file", ex);
        }
        return false;
        
    }
    
    public boolean updateFileHash(DataFile file)
    {
        try {
            return this.fs.updateHash(file);
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseManager.class.getName()).log(Level.SEVERE, "DB: SQL error when updating hash of the file", ex);
        }
        return false;
    }
    
    public ArrayList<DataSlice> getFileSlices(DataFile file)
    {
        try {
            this.fs.getFile(file);        
            return this.ss.getSlicesFromFile(file);
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseManager.class.getName()).log(Level.SEVERE, "DB: SQL error when getting file slices", ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(DatabaseManager.class.getName()).log(Level.SEVERE, "DB: Error instantiating pipeline classes from files", ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(DatabaseManager.class.getName()).log(Level.SEVERE, "DB: Illegal access error getting file slices", ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DatabaseManager.class.getName()).log(Level.SEVERE, "DB: Pipeline class not found while getting file slices", ex);
        }
        return null;
        
    }
    
    public ArrayList<DataAccount> getSlicesAccount(ArrayList<DataSlice> listSlices)
    {
        try {
            ArrayList<DataAccount> listAccounts = new ArrayList<>();
            
            //Get all Accounts
            ArrayList<DataAccount> listAccountsAux = this.as.getAccounts();
            
            //Get Slices Accounts
            for (DataSlice slice : listSlices)
            {
                for (DataAccount account : listAccountsAux)
                {
                    if (slice.getAccount().equals(account.getName())) listAccounts.add(account);
                }
                
            }
            
            return listAccounts;
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseManager.class.getName()).log(Level.SEVERE, "DB: SQL Error when retrieving slices account", ex);
        }
        return null;
    }
    
    public boolean updateFileLastAccessedDate(DataFile file)
    {
        try {
            return this.fs.updateHash(file);
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseManager.class.getName()).log(Level.SEVERE, "DB: SQL error trying to update last accessed file date", ex);
            return false;
        }
    }   

    public boolean insertAccount(DataAccount ssa) {
        try
        {
            this.as.insertAccount(ssa);
            return true;
        } catch (SQLException ex)
        {
            Logger.getLogger(DatabaseManager.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
    public boolean deleteAccount(DataAccount ssa) {
        try
        {
            this.as.deleteAccountByName(ssa.getName());
            return true;
        } catch (SQLException ex)
        {
            Logger.getLogger(DatabaseManager.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
}
