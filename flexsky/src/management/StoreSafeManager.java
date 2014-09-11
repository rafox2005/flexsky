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
import data.StoreSafeAccount;
import data.StoreSafeFile;
import data.StoreSafeSlice;
import dispersal.IEncoderIDA;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import pipeline.IPipeProcess;
import util.FlexSkyLogger;

/**
 *
 * @author rafox
 */
public class StoreSafeManager {

    private static StoreSafeManager instance = null;
    private final DatabaseManager db;
    private final StorageManager storage;
    public static int bufferSize = 1024*8;
    
    private static ExecutorService executor = null;
    public static final List<FutureTask<Integer>> taskList = new ArrayList<FutureTask<Integer>>();
    
    public final FlexSkyLogger logger;
    

    protected StoreSafeManager(String pathToDB, String pathToLogDB) {
        this.db = new DatabaseManager(pathToDB);
        this.storage = new StorageManager();
        this.logger = new FlexSkyLogger(pathToLogDB); 
    }


    public static StoreSafeManager getInstance(String pathToDB, String pathToLogDB) {
        if (instance == null) {
            instance = new StoreSafeManager(pathToDB, pathToLogDB);
        }
        return instance;
    }
    
    
    public boolean storeFile(String path, String type, String dispersalMethod, int totalParts, int reqParts, int revision, ArrayList<StoreSafeAccount> listAccounts, StorageOptions options) {
        long start, end;
        //Buffer size must be compatible with reqParts for the Dispersal to work
        StoreSafeManager.bufferSize = StoreSafeManager.bufferSize + (reqParts - StoreSafeManager.bufferSize % reqParts);
        start = System.currentTimeMillis();
        StoreSafeFile ssf = null;
        try {
            if (totalParts != listAccounts.size()) {
                throw new IllegalArgumentException("accounts list and total parts differ, please correct!");
            }

            File file = new File(path); //Get File object from the file
            BasicFileAttributes attrs = Files.readAttributes(file.toPath(), BasicFileAttributes.class); //Needed for lasAccess file variable
            ssf = new StoreSafeFile(file.getName(), file.length(), type, dispersalMethod, totalParts, reqParts, "0", new Date(attrs.lastAccessTime().toMillis()), new Date(attrs.lastModifiedTime().toMillis()), revision);
            ssf.setOptions(options);
            
            int fileID = this.db.insertFile(ssf);
            
            
            ArrayList<StoreSafeSlice> slices = new ArrayList<>();

            for (int i = 0; i < totalParts; i++) {
                slices.add(new StoreSafeSlice(fileID, i, listAccounts.get(i).getName(), "", 0, "")); //Prepare a slice without some info
            }

            //Store the files and finish the parts to store
            this.storage.storeFile(file, ssf, slices, listAccounts);
            
            //After parts are stored insert slices into the DB
            for (int i = 0; i < totalParts; i++) {
                this.db.insertSlice(slices.get(i));
            }

            //And update the hash of the file            
            this.db.updateFileHash(ssf);
            
            //Finish and log everything
            double time = System.currentTimeMillis() - start;
            double rate = (ssf.getSize() / 1024.0) / ( time/1000.0 );
            FlexSkyLogger.addFileLog(ssf, "UP", time, rate, ssf.getSize()/1000.0);
            
           
            return true;
        } catch (Exception ex) {
            Logger.getLogger(StoreSafeManager.class.getName()).log(Level.SEVERE, null, ex);
            this.db.deleteFile(ssf);
            return false;
        }

    }

    public boolean storeFile(String path, String type, String dispersalMethod, int totalParts, int reqParts, int revision, ArrayList<StoreSafeAccount> listAccounts) {
        StorageOptions options = new StorageOptions(null, null, null);
        return storeFile(path, type, dispersalMethod, totalParts, reqParts, revision, listAccounts, options);
    }
    
    public ArrayList listFiles() {
        return this.db.listFiles();
    }

    public boolean downloadFile(String path, StoreSafeFile ssf) {
        try {
            StoreSafeManager.bufferSize = StoreSafeManager.bufferSize - (ssf.getReqParts() - StoreSafeManager.bufferSize%ssf.getReqParts());
            long start = System.currentTimeMillis();
            ArrayList<StoreSafeSlice> slicesList = this.db.getFileSlices(ssf);
            ArrayList<StoreSafeAccount> accountList = this.db.getSlicesAccount(slicesList);
            File file = new File(path);

            this.storage.downloadFile(file, ssf, slicesList, accountList);
            
            //Wait for file download
            //StoreSafeManager.executor.shutdown();

            //Update last_accessed info
            BasicFileAttributes attrs = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            ssf.setLastAccessed(new Date(attrs.lastAccessTime().toMillis()));

            this.db.updateFileLastAccessedDate(ssf);

            //Finish and log everything
            long time = System.currentTimeMillis() - start;
            long rate = (ssf.getSize() / 1024) / ( time/1000 );
            
            FlexSkyLogger.addFileLog(ssf, "DOWN", time, rate, ssf.getSize());
            
            return true;
        } catch (IOException ex) {
            Logger.getLogger(StoreSafeManager.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

    }

    public boolean deleteFile(StoreSafeFile ssf) {
        double start = System.currentTimeMillis();
        ArrayList<StoreSafeSlice> slices = this.db.getFileSlices(ssf);
        ArrayList<StoreSafeAccount> accounts = this.db.getSlicesAccount(slices);
        
        //Delete the slices
        for (int i = 0; i < slices.size(); i++) {
            this.storage.deleteSlice(ssf, slices.get(i), accounts.get(i));
        }
        //Delete the file from DB
        this.db.deleteFile(ssf);
        
        //Finish and log everything
            double time = System.currentTimeMillis() - start;
            double rate = (ssf.getSize() / 1024.0) / ( time/1000.0 );
            FlexSkyLogger.addFileLog(ssf, "DEL", time, rate, ssf.getSize()/1024.0);
        
        return true;
    }
    
    public boolean deleteAllFiles() {
        ArrayList<StoreSafeFile> files = this.listFiles();
        for (StoreSafeFile storeSafeFile : files) {
            this.deleteFile(storeSafeFile);
        }
        return true;
    }

    public ArrayList listAccounts() {
        return this.db.listAccounts();
    } 
    
    public static ExecutorService getExecutor()
    {
        if (StoreSafeManager.executor == null || StoreSafeManager.executor.isShutdown()) {
            StoreSafeManager.executor = Executors.newCachedThreadPool();
        }
        
        return StoreSafeManager.executor;
        
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
    
    public boolean addAccount(StoreSafeAccount ssa)
    {
        return this.db.insertAccount(ssa);
    }
    
    public boolean delAccount(StoreSafeAccount ssa)
    {
        return this.db.deleteAccount(ssa);
    }

}
