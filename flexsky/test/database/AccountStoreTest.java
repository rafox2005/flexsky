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

import data.DataAccount;
import storage.driver.WebDavDriver;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class AccountStoreTest
{
    public Connection conn;
    public AccountStore as;
    
    public AccountStoreTest()
    {
    }
    
    @BeforeClass
    public static void setUpClass()
    {
    }
    
    @AfterClass
    public static void tearDownClass()
    {
    }
    
    @Before
    public void setUp() throws ClassNotFoundException
    {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(AccountStoreTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            this.conn = DriverManager.getConnection("jdbc:sqlite:/home/mira/NetBeansProjects/flexsky/flexsky/db/SAC-experiments-files.db");
            as = new AccountStore(this.conn);
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

//    /**
//     * Test of insertAccount method, of class AccountStore.
//     */
//    @Test
//    public void testInsertAccount()
//    {
//        System.out.println("insertAccount");
//        String name = "teste3";
//        int type = 0;
//        String path = "/home/rafox/Desktop/localProviders/provider3";
//        AccountStore instance = this.as;
//        boolean expResult = true;
//        boolean result = instance.insertAccount(name, type, path);
//        name = "teste4";
//        result = instance.insertAccount(name, type, path);
//        assertEquals(expResult, result);
//    }

//    /**
//     * Test of getAccounts method, of class AccountStore.
//     */
//    @Test
//    public void testGetAccounts()
//    {
//        System.out.println("getAccounts");
//        AccountStore instance = this.as;
//        ArrayList<DBAccount> expResult = null;
//        ArrayList<DBAccount> result = instance.getAccounts();
//        assertTrue(result.size() == 3);
//    }

    /**
     * Test of deleteAccountByName method, of class AccountStore.
     */
//    @Test
//    public void testDeleteAccountByName()
//    {
//        System.out.println("deleteAccountByName");
//        String name = "teste3";
//        AccountStore instance = this.as;
//        boolean expResult = true;
//        boolean result = instance.deleteAccountByName(name);
//        assertEquals(expResult, result);
//    }
//
//    /**
//     * Test of deleteAccountByID method, of class AccountStore.
//     */
//    @Test
//    public void testDeleteAccountByID()
//    {
//        System.out.println("deleteAccountByID");
//        int ID = 4;
//        AccountStore instance = this.as;
//        boolean expResult = true;
//        boolean result = instance.deleteAccountByID(ID);
//        assertEquals(expResult, result);
//    }
    
    /**
     * Test some operations of class AccountStore.
     */
    @Test
    public void testOperations() throws SQLException
    {
        System.out.println("testOperations");
        AccountStore instance = this.as;
        HashMap parametros = new HashMap();
        HashMap selectparametros = new HashMap();
        parametros.put("username", "rafox2005@gmail.com");
        parametros.put("password", "ra281190");
        selectparametros.put("PROV_SEC", 50);
        selectparametros.put("PROV_PERF", 50);
        selectparametros.put("PROV_AVAIL", 50);
        selectparametros.put("PROV_DUR", 50);
        selectparametros.put("PROV_STORAGECOST", 50);
        selectparametros.put("PROV_BWCOST", 50);
        //StoreSafeAccount account1 = new DataAccount("teste5", 0, "path_teste");
        DataAccount account2 = new DataAccount("remote-box", WebDavDriver.class.getName(), "https://dav.box.com/dav", parametros, selectparametros);
        //instance.insertAccount(account1);
        instance.insertAccount(account2);
        ArrayList acc_list = instance.getAccounts();
        assertTrue(acc_list.size() == 4);
        //instance.deleteAccountByName("teste5");
        //instance.deleteAccountByName("teste6");
        acc_list = instance.getAccounts();
        assertTrue(acc_list.size() == 2);
        
    }
    
    @Test
    public void updateProvidersADDSELECTION() throws SQLException
    {
        System.out.println("updateProvidersADDSELECTION");
        AccountStore instance = this.as;
        ArrayList<DataAccount> list_accs = this.as.getAccounts();
        
        for (DataAccount acc : list_accs)
        {
            HashMap<String, Integer> selectparametros = new HashMap();     
            selectparametros.put("PROV_SEC", (int) Math.round(100*Math.random()));
            selectparametros.put("PROV_PERF", (int) Math.round(100*Math.random()));
            selectparametros.put("PROV_AVAIL", (int) Math.round(100*Math.random()));
            selectparametros.put("PROV_DUR", (int) Math.round(100*Math.random()));
            selectparametros.put("PROV_STORAGECOST", (int) Math.round(100*Math.random()));
            selectparametros.put("PROV_BWCOST", (int) Math.round(100*Math.random()));
            
            this.as.deleteAccountByName(acc.getName());
            acc.setSelectionParameters(selectparametros);
            this.as.insertAccount(acc);
        }
        
    }
}
