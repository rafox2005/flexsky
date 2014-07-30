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
import java.util.ArrayList;
import java.util.HashMap;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import pipeline.PipeTest;

/**
 *
 * @author rafox
 */
public class StoreSafeManagerTest
{
    
    public StoreSafeManagerTest()
    {
    }
    
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
    @Ignore @Test
    public void testGetInstance()
    {
        System.out.println("getInstance");
        StoreSafeManager expResult = null;
        StoreSafeManager result = StoreSafeManager.getInstance();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of storeFile method, of class StoreSafeManager.
     */
    @Ignore @Test
    public void testStoreFile()
    {
        System.out.println("storeFile");
        String path = "/home/rlibardi/NetBeansProjects/safestore-leicester/safestore/test/filesToTest/input/Kinowear-Bible [tahir99].pdf";
        String type = "music";
        String dispersalMethod = "rabin";
        int totalParts = 3;
        int reqParts = 2;
        int revision = 3;
        StoreSafeManager instance = StoreSafeManager.getInstance();    
        instance.deleteAllFiles();
        ArrayList<StoreSafeAccount> listAccounts = instance.listAccounts();    
        
        boolean expResult = true;
        boolean result = instance.storeFile(path, type, dispersalMethod, totalParts, reqParts, revision, listAccounts);
        assertEquals(expResult, result);
    }
    
    @Ignore @Test
    public void testDownloadFile()
    {
        StoreSafeManager instance = StoreSafeManager.getInstance();  
        //Download part test
        String pathDown = "/home/rlibardi/NetBeansProjects/safestore-leicester/safestore/test/filesToTest/output/Kinowear-Bible [tahir99].pdf";
        StoreSafeFile ssf =  new StoreSafeFile("Kinowear-Bible [tahir99].pdf", 3);
        instance.downloadFile(pathDown, ssf);        
     
    }
    
    @Ignore @Test
    public void testStoreAndDownloadFile()
    {
        //Store part test
        System.out.println("storeFileAndDownload");
        String path = "/home/rlibardi/NetBeansProjects/safestore-leicester/safestore/test/filesToTest/input/Kinowear-Bible [tahir99].pdf";
        String type = "book";
        String dispersalMethod = "rabin";
        int totalParts = 3;
        int reqParts = 2;
        int revision = 4;
        StoreSafeManager instance = StoreSafeManager.getInstance();    
        instance.deleteAllFiles();
        ArrayList<StoreSafeAccount> listAccounts = instance.listAccounts();    
        
        boolean expResult = true;
        boolean result = instance.storeFile(path, type, dispersalMethod, totalParts, reqParts, revision, listAccounts);
        
        //Download part test
        String pathDown = "/home/rlibardi/NetBeansProjects/safestore-leicester/safestore/test/filesToTest/output/Kinowear-Bible [tahir99].pdf";
        StoreSafeFile ssf =  new StoreSafeFile("Kinowear-Bible [tahir99].pdf", 4);
        instance.downloadFile(pathDown, ssf);        
     
    }
    
    @Test
    public void testStoreAndDownloadFileWithFilePipeline()
    {
        //Store part test
        System.out.println("storeFileAndDownload");
        String path = "/home/rlibardi/NetBeansProjects/safestore-leicester/safestore/test/filesToTest/input/sqlitestudio-2.1.5.bin";
        String type = "book";
        String dispersalMethod = "rabin";
        int totalParts = 3;
        int reqParts = 2;
        int revision = 17;
        StoreSafeManager instance = StoreSafeManager.getInstance();  
        instance.deleteAllFiles();
        ArrayList<StoreSafeAccount> listAccounts = instance.listAccounts();    
        
        PipeTest pt = new PipeTest();
        HashMap<String,String> param = new HashMap<String, String>();
        param.put("chave-geral", "ronaldo");
        param.put("metodoTeste", "chupetinha");
        
        ArrayList filePipeline = new ArrayList();
        filePipeline.add(pt);
        StorageOptions options = new StorageOptions(filePipeline, null, param);
        
        boolean expResult = true;
        boolean result = instance.storeFile(path, type, dispersalMethod, totalParts, reqParts, revision, listAccounts, options);
        
        //Download part test
        String pathDown = "/home/rlibardi/NetBeansProjects/safestore-leicester/safestore/test/filesToTest/output/sqlitestudio-2.1.5.bin";
        StoreSafeFile ssf =  new StoreSafeFile("sqlitestudio-2.1.5.bin", 17);
        instance.downloadFile(pathDown, ssf);        
     
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
