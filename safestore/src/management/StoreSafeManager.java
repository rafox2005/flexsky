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
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Date;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rafox
 */
public class StoreSafeManager {

    private static StoreSafeManager instance = null;
    private final DatabaseManager db;
    private final StorageManager storage;

    protected StoreSafeManager() {
        this.db = new DatabaseManager("/home/rlibardi/NetBeansProjects/safestore-leicester/safestore/db/safestore_test.db");
        this.storage = new StorageManager();

    }

    public static StoreSafeManager getInstance() {
        if (instance == null) {
            instance = new StoreSafeManager();
        }
        return instance;
    }

    public boolean storeFile(String path, String type, String dispersalMethod, int totalParts, int reqParts, int revision, ArrayList<StoreSafeAccount> listAccounts) {
        StoreSafeFile ssf = null;
        try {
            if (totalParts != listAccounts.size()) {
                throw new IllegalArgumentException("accounts list and total parts differ, please correct!");
            }

            File file = new File(path); //Get File object from the file
            BasicFileAttributes attrs = Files.readAttributes(file.toPath(), BasicFileAttributes.class); //Needed for lasAccess file variable
            ssf = new StoreSafeFile(file.getName(), file.length(), type, dispersalMethod, totalParts, reqParts, "0", new Date(attrs.lastAccessTime().toMillis()), new Date(attrs.lastModifiedTime().toMillis()), revision);
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

            return true;
        } catch (Exception ex) {
            Logger.getLogger(StoreSafeManager.class.getName()).log(Level.SEVERE, null, ex);
            this.db.deleteFile(ssf);
            return false;
        }

    }

    public ArrayList listFiles() {
        return this.db.listFiles();
    }

    public boolean downloadFile(String path, StoreSafeFile ssf) {
        try {
            ArrayList<StoreSafeSlice> slicesList = this.db.getFileSlices(ssf);
            ArrayList<StoreSafeAccount> accountList = this.db.getSlicesAccount(slicesList);
            File file = new File(path);

            this.storage.downloadFile(file, ssf, slicesList, accountList);

            //Update last_accessed info
            BasicFileAttributes attrs = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            ssf.setLastAccessed(new Date(attrs.lastAccessTime().toMillis()));

            this.db.updateFileLastAccessedDate(ssf);

            return true;
        } catch (IOException ex) {
            Logger.getLogger(StoreSafeManager.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

    }

    public boolean deleteFile() {
        return true;
    }

    public ArrayList listAccounts() {
        return this.db.listAccounts();
    }

}
