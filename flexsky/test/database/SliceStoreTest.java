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

import data.DataSlice;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author rafox
 */
public class SliceStoreTest
{
    public Connection conn;
    public SliceStore ss;
    
    public SliceStoreTest()
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
            this.conn = DriverManager.getConnection("jdbc:sqlite:/home/rafox/NetBeansProjects/safestore/safestore/db/safestore_test.db");
            this.ss = new SliceStore("FSTeste", this.conn);
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
        SliceStore instance = this.ss;
        int sizeBegin = instance.getSlicesFromFile(23).size();
        DataSlice slice1 = new DataSlice(23,
                                                   1,
                                                   "",
                                                   "teste",
                                                   new Long(123312331).longValue(),
                                                   "HASHTESTE");
        DataSlice slice2 = new DataSlice(23,
                                                   2,
                                                   "",
                                                   "teste",
                                                   new Long(123312331).longValue(),
                                                   "HASHTESTE");
        instance.insertSlice(slice1);
        instance.insertSlice(slice2);
        assertTrue(instance.getSlicesFromFile(23).size() == sizeBegin+2);
        instance.deleteSlice(slice1);
        instance.deleteSlice(slice2);
        assertEquals(instance.getSlicesFromFile(23).size(), sizeBegin);

    }
    
    @Test
    public void testInsertSameSlices()
    {
        System.out.println("testInsertSameSlices");
        SliceStore instance = this.ss;
        DataSlice slice1 = new DataSlice(23,
                                                   1,
                                                   "",
                                                   "teste",
                                                   new Long(123312331).longValue(),
                                                   "HASHTESTE");
        
        instance.insertSlice(slice1);
        assertFalse(instance.insertSlice(slice1));
        instance.deleteSlice(slice1);
        
    }
    
}
