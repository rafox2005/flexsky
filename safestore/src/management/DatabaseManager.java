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

import data.StoreSafeAccount;
import data.StoreSafeFile;
import data.StoreSafeSlice;
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
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DatabaseManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            this.conn = DriverManager.getConnection("jdbc:sqlite:" + pathToDB);
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.as = new AccountStore(this.conn);
        this.fs = new FileStore("TestFS", this.conn);
        this.ss = new SliceStore("TestFS", this.conn);
    }
    
    public int insertFile(StoreSafeFile ssf)
    {
        this.fs.insertFile(ssf);
        return this.fs.getFileID(ssf);
    }
    
    public boolean insertSlice(StoreSafeSlice sss) 
    {
        return this.ss.insertSlice(sss);
        
        
    }
    
    public ArrayList listFiles()
    {
        return this.fs.getFiles();
    }
    
    public ArrayList listAccounts()
    {
        return this.as.getAccounts();
    }
    
    public boolean deleteFile(StoreSafeFile file) {
        ArrayList<StoreSafeSlice> slicesToDelete = this.ss.getSlicesFromFile(file);
        for (StoreSafeSlice slice : slicesToDelete) {
            this.ss.deleteSlice(slice);
        }
        return this.fs.deleteFile(file);
        
    }
    
    public boolean updateFileHash(StoreSafeFile file)
    {
        return this.fs.updateHash(file);
    }
    
    public ArrayList<StoreSafeSlice> getFileSlices(StoreSafeFile file)
    {
        this.fs.getFile(file);
        return this.ss.getSlicesFromFile(file);        
        
    }
    
    public ArrayList<StoreSafeAccount> getSlicesAccount(ArrayList<StoreSafeSlice> listSlices)
    {
        ArrayList<StoreSafeAccount> listAccounts = new ArrayList<>();
        
        //Get all Accounts
        ArrayList<StoreSafeAccount> listAccountsAux = this.as.getAccounts();
        
        //Get Slices Accounts
        for (StoreSafeSlice slice : listSlices)
        {
            for (StoreSafeAccount account : listAccountsAux)
            {
                if (slice.getAccount().equals(account.getName())) listAccounts.add(account);                
            }
                
        }
        
        return listAccounts;
    }
    
    public boolean updateFileLastAccessedDate(StoreSafeFile file)
    {
        return this.fs.updateHash(file);
    }
    
    public boolean deleteAllFiles()
    {
        return this.fs.deleteAllFiles();
    }
    
}
