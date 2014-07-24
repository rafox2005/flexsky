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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

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
    @Test
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
    @Test
    public void testStoreFile()
    {
        System.out.println("storeFile");
        String path = "/home/rafox/NetBeansProjects/safestore/safestore/test/filesToTest/video2.3gp";
        String type = "music";
        String dispersalMethod = "rabin";
        int totalParts = 3;
        int reqParts = 2;
        int revision = 4;
        StoreSafeManager instance = StoreSafeManager.getInstance();      
        ArrayList<StoreSafeAccount> listAccounts = instance.listAccounts();    
        
        boolean expResult = true;
        boolean result = instance.storeFile(path, type, dispersalMethod, totalParts, reqParts, revision, listAccounts);
        assertEquals(expResult, result);
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
