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

package data;

import static org.hamcrest.core.IsEqual.equalTo;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author rafox
 */
public class StoreSafeDataUnitTest {
    
    public StoreSafeDataUnitTest() {
    }
    
    public StoreSafeDataUnit ssduRead;
    public StoreSafeDataUnit ssduWrite;
    public String filePathRead;
    public String filePathWrite;
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        this.filePathRead = "/home/rafox/NetBeansProjects/safestore/safestore/test/filesToTest/read/large.tgz";
        this.filePathWrite = "/home/rafox/NetBeansProjects/safestore/safestore/test/filesToTest/write/InStoreSafeDataUnitTest.txt";
        this.ssduRead = new StoreSafeDataUnit(this.filePathRead);
        this.ssduWrite = new StoreSafeDataUnit(this.filePathWrite);        
    }
    
    @After
    public void tearDown() {
        System.out.println("close");
        StoreSafeDataUnit instanceRead = this.ssduRead;
        StoreSafeDataUnit instanceWrite = this.ssduWrite;
        boolean expResult = false;
        boolean resultRead = instanceRead.close();
        boolean resultWrite = instanceWrite.close();
        //assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of read method, of class StoreSafeDataUnit.
     */
    @Test
    public void testRead() {
        System.out.println("read");
        byte[] byteVector = new byte[8];
        StoreSafeDataUnit instance = this.ssduRead;
        byte[] expResult = new byte[] {0x54, 0x65, 0x73, 0x74, 0x65}; //Hex to "Teste"
        while (instance.read(byteVector));
        assertThat(byteVector, equalTo(expResult));
    }

    /**
     * Test of write method, of class StoreSafeDataUnit.
     */
    @Test
    public void testWrite() {
        System.out.println("write");
        byte[] byteVector = new byte[] {0x54, 0x65, 0x73, 0x74, 0x65};
        StoreSafeDataUnit instance = this.ssduWrite;
        byte[] expResult = new byte[] {0x54, 0x65, 0x73, 0x74, 0x65};
        boolean result = instance.write(byteVector);
        //assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of close method, of class StoreSafeDataUnit.
     */
    @Test
    public void testClose() {
        
    }

    /**
     * Test of getFilePath method, of class StoreSafeDataUnit.
     */
    @Test
    public void testGetFilePath() {
        System.out.println("getFilePath");
        StoreSafeDataUnit instance = null;
        String expResult = "";
        String result = instance.getFilePath();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
