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
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
import data.StoreSafeAccount;
import data.StoreSafeFile;
import dispersal.IEncoderIDA;
import dispersal.rabin.RabinIDA;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import pipeline.pipe.PipeTest;

/**
 *
 * @author rafox
 */
public class StoreSafeManagerTest
{
    
    //public static final String pathToTestFile = "/home/rlibardi/rlibardi-local/safestore/filesToTest/input/ubuntu.iso";
    public static final String pathToTestFile = "/home/rlibardi/rlibardi-local/safestore/filesToTest/input/screen.png";
    //public static final String pathToTestFileOutput = "/home/rlibardi/rlibardi-local/safestore/filesToTest/output/ubuntu.iso";
    public static final String pathToTestFileOutput = "/home/rlibardi/rlibardi-local/safestore/filesToTest/output/screen.png";
    public static final String pathToDB = "/home/rlibardi/NetBeansProjects/safestore-leicester/safestore/db/safestore_test.db";
    public static final String pathToLogDB = "/home/rlibardi/NetBeansProjects/safestore-leicester/safestore/db/safestore_log.db";
    
    @Before
    public void setUp()
    {
        
    }
    
    @After
    public void tearDown()
    {
    }

    /**
     * Test of getInstance method, of class StoreSafeManager.
     */
    @Test
    public void testGetInstance()
    {
        System.out.println("getInstance");
        StoreSafeManager expResult = null;
        StoreSafeManager result = StoreSafeManager.getInstance(StoreSafeManagerTest.pathToDB, StoreSafeManagerTest.pathToLogDB);
        assertEquals(result.getClass(), StoreSafeManager.class);
    }

    /**
     * Test of storeFile method, of class StoreSafeManager.
     */
    @Test
    public void testStoreFile()
    {
        System.out.println("storeFile");
        String path = StoreSafeManagerTest.pathToTestFile;
        String type = "music";
        String dispersalMethod = "RabinIDA";
        int totalParts = 3;
        int reqParts = 2;
        int revision = 0;
        StoreSafeManager instance = StoreSafeManager.getInstance(StoreSafeManagerTest.pathToDB, StoreSafeManagerTest.pathToLogDB);    
        instance.deleteAllFiles();
        ArrayList<StoreSafeAccount> listAccounts = instance.listAccounts();    
        
        boolean expResult = true;
        boolean result = instance.storeFile(path, type, dispersalMethod, totalParts, reqParts, revision, listAccounts);
        assertEquals(expResult, result);
    }
    
    @Test
    public void testDownloadFile()
    {
        StoreSafeManager instance = StoreSafeManager.getInstance(StoreSafeManagerTest.pathToDB, StoreSafeManagerTest.pathToLogDB);  
        //Download part test
        String pathDown = StoreSafeManagerTest.pathToTestFileOutput;
        StoreSafeFile ssf =  new StoreSafeFile(new File(pathDown).getName(), 0);
        instance.downloadFile(pathDown, ssf);        
     
    }
    
    @Test
    public void testDeleteFile()
    {
        StoreSafeManager instance = StoreSafeManager.getInstance(StoreSafeManagerTest.pathToDB, StoreSafeManagerTest.pathToLogDB);  
        //Delete File       
        StoreSafeFile ssf =  new StoreSafeFile(new File(StoreSafeManagerTest.pathToTestFile).getName(), 0);
        instance.deleteFile(ssf);
     
    }
    
    @Test
    public void testStoreAndDownloadFile() throws IOException
    {
        //Store part test
        String path = StoreSafeManagerTest.pathToTestFile;
        String type = "book";
        String dispersalMethod = "RabinIDA";
        int totalParts = 3;
        int reqParts = 2;
        int revision = 1;
        StoreSafeManager instance = StoreSafeManager.getInstance(StoreSafeManagerTest.pathToDB, StoreSafeManagerTest.pathToLogDB);    
        instance.deleteAllFiles();
        ArrayList<StoreSafeAccount> listAccounts = instance.listAccounts();    
        
        boolean expResult = true;
        File start = new File(path);
        boolean result = instance.storeFile(path, type, dispersalMethod, totalParts, reqParts, revision, listAccounts);
        
        //Download part test
        String pathDown = StoreSafeManagerTest.pathToTestFileOutput;
        StoreSafeFile ssf =  new StoreSafeFile(new File(pathDown).getName(), 1);
        instance.downloadFile(pathDown, ssf);
        
        File end = new File(pathDown);
        
        assertTrue(FileUtils.contentEquals(start, end));
        
        //After Success, delete the file
        instance.deleteFile(ssf);
        
                
     
    }
    
   @Test
    public void testStoreAndDownloadFileWithFilePipeline() throws IOException, InterruptedException
    {
        //Store part test
        System.out.println("storeFileAndDownload");
        String path = StoreSafeManagerTest.pathToTestFile;
        String type = "book";
        String dispersalMethod = "RabinIDA";
        int totalParts = 3;
        int reqParts = 2;
        int revision = 2;
        StoreSafeManager instance = StoreSafeManager.getInstance(StoreSafeManagerTest.pathToDB, StoreSafeManagerTest.pathToLogDB);  
        instance.deleteAllFiles();
        ArrayList<StoreSafeAccount> listAccounts = instance.listAccounts();    
        
        PipeTest pt = new PipeTest();
        PipeTest pt2 = new PipeTest();
        HashMap<String,String> param = new HashMap<String, String>();
        param.put("chave-geral", "ronaldo");
        param.put("metodoTeste", "chupetinha");
        
        ArrayList filePipeline = new ArrayList();
        filePipeline.add(pt);
        
        ArrayList slicePipeline = new ArrayList();
        slicePipeline.add(pt2);
        
        StorageOptions options = new StorageOptions(filePipeline, slicePipeline, param);
        
        boolean expResult = true;
        File start = new File(path);
        boolean result = instance.storeFile(path, type, dispersalMethod, totalParts, reqParts, revision, listAccounts, options);
        
        Thread.sleep(8000);
        
        //Download part test
        String pathDown = StoreSafeManagerTest.pathToTestFileOutput;
        StoreSafeFile ssf =  new StoreSafeFile(new File(pathDown).getName(), 2);
        instance.downloadFile(pathDown, ssf);        
     
        File end = new File(pathDown);
        
        assertTrue(FileUtils.contentEquals(start, end));
        
        //After Success, delete the file
        //instance.deleteFile(ssf);
        
    }   

        
    
    

//    /**
//     * Test of listFiles method, of class StoreSafeManager.
//     */
//    @Test
//    public void testListFiles()
//    {
//        System.out.println("listFiles");
//        StoreSafeManager instance = new StoreSafeManager();
//        ArrayList expResult = null;
//        ArrayList result = instance.listFiles();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of downloadFile method, of class StoreSafeManager.
//     */
//    @Test
//    public void testDownloadFile()
//    {
//        System.out.println("downloadFile");
//        String path = "";
//        StoreSafeFile file = null;
//        StoreSafeManager instance = new StoreSafeManager();
//        boolean expResult = false;
//        boolean result = instance.downloadFile(path, file);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of deleteFile method, of class StoreSafeManager.
//     */
//    @Test
//    public void testDeleteFile()
//    {
//        System.out.println("deleteFile");
//        StoreSafeManager instance = new StoreSafeManager();
//        boolean expResult = false;
//        boolean result = instance.deleteFile();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
    
}
