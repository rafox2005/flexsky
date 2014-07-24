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
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author rafox
 */
public class FileStoreTest
{
    
    public Connection conn;
    public FileStore fs;
    
    public FileStoreTest()
    {
    }
    
    @Before
    public void setUp()
    {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(AccountStoreTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            this.conn = DriverManager.getConnection("jdbc:sqlite:/var/autofs/home/home/rlibardi/NetBeansProjects/safestore-leicester/safestore/db/safestore_test.db");
            this.fs = new FileStore("FSTeste", this.conn);
        } catch (SQLException ex) {
            Logger.getLogger(AccountStoreTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @After
    public void tearDown()
    {
        try {
            this.conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(AccountStoreTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Test
    public void testOperations()
    {
        System.out.println("testOperations");
        FileStore instance = this.fs;
        int sizeBegin = this.fs.getFiles().size();
        StoreSafeFile file1 = new StoreSafeFile("teste5",
                                                new Long("812331230023").longValue(),
                                                "type/text",
                                                "rabin",
                                                10,
                                                8,
                                                "HASHEXAMPLE",
                                                new Date(System.currentTimeMillis()),
                                                new Date(System.currentTimeMillis()),
                                                1);
        StoreSafeFile file2 = new StoreSafeFile("teste5",
                                                new Long("212331230025").longValue(),
                                                "type/text",
                                                "rabin",
                                                10,
                                                8,
                                                "HASHEXAMPLE2",
                                                new Date(System.currentTimeMillis()),
                                                new Date(System.currentTimeMillis()),
                                                2);
        instance.insertFile(file1);
        instance.insertFile(file2);
        ArrayList acc_list = instance.getFiles();
        assertTrue(acc_list.size() == sizeBegin+2);
        instance.deleteFile(file1);
        instance.deleteFile(file2);
        acc_list = instance.getFiles();
        assertEquals(acc_list.size(), sizeBegin);
    }
    
    @Test
    public void testInsertSameFiles()
    {
        FileStore instance = this.fs;
        StoreSafeFile file1 = new StoreSafeFile("teste5",
                                                new Long("812331230023").longValue(),
                                                "type/text",
                                                "rabin",
                                                10,
                                                8,
                                                "HASHEXAMPLE",
                                                new Date(System.currentTimeMillis()),
                                                new Date(System.currentTimeMillis()),
                                                1);
        instance.insertFile(file1);        
        Assert.assertFalse(instance.insertFile(file1));
        instance.deleteFile(file1);
        
    }
    
    @Test
    public void testGetFile()
    {
        FileStore instance = this.fs;
        StoreSafeFile file1 = new StoreSafeFile("book.pdf", 0);
        instance.getFile(file1);
        
        Assert.assertEquals(file1.getSize(), 8493855);
        
    }
}
