/*
 * The MIT License
 *
 * Copyright 2014 rlibardi.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package mssf.selector;

import com.sun.jmx.mbeanserver.ModifiableClassLoaderRepository;
import data.DataAccount;
import database.AccountStore;
import database.AccountStoreTest;
import database.ModuleStore;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author rlibardi
 */
public class ModuleTest {
    
    public Connection conn;
    public ModuleStore ms;
    
    public ModuleTest() {
    }
    
    @Before
    public void setUp() throws ClassNotFoundException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(AccountStoreTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            this.conn = DriverManager.getConnection("jdbc:sqlite:/home/rlibardi/NetBeansProjects/flexsky/flexsky/db/SAC-experiments-files.db");
            ms = new ModuleStore(this.conn);
        } catch (SQLException ex) {
            Logger.getLogger(AccountStoreTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @After
    public void tearDown() {        
        try {
            this.conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(AccountStoreTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void AddModules() {
        System.out.println("addModules");
        ModuleStore ms = this.ms;
        
        
       
        for (int i=0; i<10;i++)
        {            
            try
            {
                HashMap parametros = new HashMap();
                HashMap selectparametros = new HashMap();
                selectparametros.put("SEC", Math.round(100*Math.random()));
                selectparametros.put("PERF", Math.round(100*Math.random()));
                selectparametros.put("STO", Math.round(100*Math.random()));
                
                Module pm1 = new Module("testeCOMP" + i, "comp", "pipeline.pipe.Base64Pipe", parametros, selectparametros);
                Module pm2 = new Module("testeENC" + i, "enc", "pipeline.pipe.PipeTest", parametros, selectparametros);
                Module pm3 = new Module("testeIDA" + i, "ida", "RabinIDA", parametros, selectparametros);
                
                ms.insertModule(pm1);
                ms.insertModule(pm2);
                ms.insertModule(pm3);
            } catch (SQLException ex)
            {
                Logger.getLogger(ModuleTest.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
    }

    /**
     * Test of getName method, of class Module.
     */
    @Test
    public void testGetName() {
    }

    /**
     * Test of getPipe_name method, of class Module.
     */
    @Test
    public void testGetPipe_name() {
    }

    /**
     * Test of getType method, of class Module.
     */
    @Test
    public void testGetType() {
    }

    /**
     * Test of getParameters method, of class Module.
     */
    @Test
    public void testGetParameters() {
    }

    /**
     * Test of getSelectionParameters method, of class Module.
     */
    @Test
    public void testGetSelectionParameters() {
    }
    
}
