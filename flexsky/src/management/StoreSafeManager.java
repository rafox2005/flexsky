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

import data.StorageOptions;
import data.DataAccount;
import data.DataFile;
import data.DataSlice;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import util.FlexSkyLogger;

/**
 *
 * @author rafox
 */
public class StoreSafeManager {

    private static StoreSafeManager instance = null;
    private final DatabaseManager db;
    private final StorageManager storage;
    public static int bufferSize = 1024*1024*15;
    
   
    public final FlexSkyLogger logger;
    
    public static final ThreadMXBean tmx = ManagementFactory.getThreadMXBean(); 

    protected StoreSafeManager(String pathToDB, String pathToLogDB) {
        this.db = new DatabaseManager(pathToDB);
        this.storage = new StorageManager();
        this.logger = new FlexSkyLogger(pathToLogDB); 
        StoreSafeManager.tmx.setThreadContentionMonitoringEnabled(true);
        StoreSafeManager.tmx.setThreadCpuTimeEnabled(true);
    }


    public static StoreSafeManager getInstance(String pathToDB, String pathToLogDB) {
        if (instance == null) {
            instance = new StoreSafeManager(pathToDB, pathToLogDB);
        }
        return instance;
    }
    
    
    public boolean storeFile(String path, String type, String dispersalMethod, int totalParts, int reqParts, int revision, ArrayList<DataAccount> listAccounts, StorageOptions options) {
        long start, end;
        boolean result = false;
        //Buffer size must be compatible with reqParts for the Dispersal to work
        StoreSafeManager.bufferSize = StoreSafeManager.bufferSize - StoreSafeManager.bufferSize % reqParts;
        start = System.currentTimeMillis();
        DataFile ssf = null;
        try {
            if (totalParts != listAccounts.size()) {
                throw new IllegalArgumentException("accounts list and total parts differ, please correct!");
            }

            File file = new File(path); //Get File object from the file
            BasicFileAttributes attrs = Files.readAttributes(file.toPath(), BasicFileAttributes.class); //Needed for lasAccess file variable
            ssf = new DataFile(file.getName(), file.length(), type, dispersalMethod, totalParts, reqParts, "0", new Date(attrs.lastAccessTime().toMillis()), new Date(attrs.lastModifiedTime().toMillis()), revision);
            ssf.setOptions(options);
            
            int fileID = this.db.insertFile(ssf);
            
            if (fileID == 0) return false;
            
            ArrayList<DataSlice> slices = new ArrayList<>();

            for (int i = 0; i < totalParts; i++) {
                slices.add(new DataSlice(fileID, i, listAccounts.get(i).getName(), "", 0, "")); //Prepare a slice without some info
            }

            //Store the files and finish the parts to store
            result = this.storage.storeFile(file, ssf, slices, listAccounts);
            
            //If not able to store the file, delete file from db and send error message
            if (result == false) {
                this.db.deleteFile(ssf);            
                throw new Exception("IO Error, Aborting upload process");
            }
            else if (result == true) {
            //After parts are stored insert slices into the DB
            for (int i = 0; i < totalParts; i++) {
                this.db.insertSlice(slices.get(i));
            }

            //And update the hash of the file and the possible additional options
            this.db.updateFileHash(ssf);
            this.db.updateFileParameters(ssf);
            
            //Finish and log everything
            double time = System.currentTimeMillis() - start;
            double rate = (ssf.getSize() / 1024.0) / ( time/1000.0 );
            FlexSkyLogger.addFileLog(ssf, "UP", time, rate, ssf.getSize()/1000.0, (slices.get(0).getSize()*totalParts) / 1000.0);
            
            }
        } catch (Exception ex) {
            Logger.getLogger(StoreSafeManager.class.getName()).log(Level.SEVERE, null, ex);
            this.deleteFile(ssf);
            return false;
        }
        return true;

    }

    public boolean storeFile(String path, String type, String dispersalMethod, int totalParts, int reqParts, int revision, ArrayList<DataAccount> listAccounts) {
        StorageOptions options = new StorageOptions(null, null, null);
        return storeFile(path, type, dispersalMethod, totalParts, reqParts, revision, listAccounts, options);
    }
    
    public ArrayList listFiles() {
        return this.db.listFiles();
    }
    
    public DataFile getFileInfo(String name, int revision)
    {
        ArrayList<DataFile> filesStored = this.db.listFiles();
        
        for (DataFile file : filesStored)
        {
            if (file.getName().equalsIgnoreCase(name) && file.getRevision() == revision) return file;
        }
        
        return null;
    }

    public boolean downloadFile(String path, DataFile ssf) {
        try {
            StoreSafeManager.bufferSize = StoreSafeManager.bufferSize - StoreSafeManager.bufferSize%ssf.getReqParts();
            
            ArrayList<DataSlice> slicesList = this.db.getFileSlices(ssf);
            ArrayList<DataAccount> accountList = this.db.getSlicesAccount(slicesList);
            File file = new File(path);
            long start = System.currentTimeMillis();
            boolean result = false;
            
            while (result != true) result = this.storage.downloadFile(file, ssf, slicesList, accountList);
            
            //Wait for file download
            //StoreSafeManager.executor.shutdown();

            //Update last_accessed info
            BasicFileAttributes attrs = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            ssf.setLastAccessed(new Date(attrs.lastAccessTime().toMillis()));

            if (result == true) {
                this.db.updateFileLastAccessedDate(ssf);
                //Finish and log everything
                long time = System.currentTimeMillis() - start;
                double rate = (ssf.getSize() / 1000) / ( time/1000 );
            
                FlexSkyLogger.addFileLog(ssf, "DOWN", time, rate, (double) ssf.getSize()/1000, (slicesList.get(0).getSize()*ssf.getTotalParts()) / 1000.0);
            
                return true;
            }
            else return false;

            
        } catch (IOException ex) {
            Logger.getLogger(StoreSafeManager.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

    }

    public boolean deleteFile(DataFile ssf) {
        double start = System.currentTimeMillis();
        ArrayList<DataSlice> slices = this.db.getFileSlices(ssf);
        ArrayList<DataAccount> accounts = this.db.getSlicesAccount(slices);
        
        //Delete the slices
        for (int i = 0; i < slices.size(); i++) {
            this.storage.deleteSlice(ssf, slices.get(i), accounts.get(i));
        }
        //Delete the file from DB
        this.db.deleteFile(ssf);
        
        //Finish and log everything
            double time = System.currentTimeMillis() - start;
            double rate = (ssf.getSize() / 1000.0) / ( time/1000.0 );
            FlexSkyLogger.addFileLog(ssf, "DEL", time, rate, ssf.getSize()/1000.0, (slices.get(0).getSize()*ssf.getTotalParts()) / 1000.0);
        
        return true;
    }
    
    public boolean deleteAllFiles() {
        ArrayList<DataFile> files = this.listFiles();
        for (DataFile storeSafeFile : files) {
            this.deleteFile(storeSafeFile);
        }
        return true;
    }

    public ArrayList listAccounts() {
        return this.db.listAccounts();
    } 
      
       
    public Set getIDAList()
    {
        
        Set idaList = util.Utils.getClassesInPackage("dispersal.encoder");
        return idaList;
    }
    
    public Set getPipeList()
    {
        
        Set pipeList = util.Utils.getClassesInPackage("pipeline.pipe");
        return pipeList;
        
    }
    
    public Set getDriverList()
    {
        
        Set driverList = util.Utils.getClassesInPackage("storage.driver");
        return driverList;
        
    }
    
    public List getAccounts()
    {
        return this.db.listAccounts();
    }
    
    public boolean addAccount(DataAccount ssa)
    {
        return this.db.insertAccount(ssa);
    }
    
    public boolean delAccount(DataAccount ssa)
    {
        return this.db.deleteAccount(ssa);
    }
    
    public boolean delAllAccounts()
    {
        try {
        ArrayList<DataAccount> accs = this.db.listAccounts();
        for (DataAccount account : accs)
        {
            this.db.deleteAccount(account);
        }
        return true;
    }
        catch (Exception ex) {
            return false;
        }
    }
    
    public ArrayList listModules() {
        return this.db.listModules();
    } 
    
}
