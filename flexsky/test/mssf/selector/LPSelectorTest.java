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

import java.util.ArrayList;
import java.util.HashMap;
import management.StoreSafeManager;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import util.FlexSkyLogger;

/**
 *
 * @author rlibardi
 */
public class LPSelectorTest {
    
    StoreSafeManager ssm;
    
    public LPSelectorTest() {
    }
    
    @Before
    public void setUp() {
        //ssm = StoreSafeManager.getInstance("/home/rlibardi/NetBeansProjects/flexsky/flexsky/db/SAC-experiments-files.db", "/home/rlibardi/NetBeansProjects/flexsky/flexsky/db/safestore_log.db");
        ssm = StoreSafeManager.getInstance("C:\\Users\\Rafox\\Documents\\NetBeansProjects\\flexsky\\flexsky\\db\\SAC-experiments-files10000.db", "C:\\Users\\Rafox\\Documents\\NetBeansProjects\\flexsky\\flexsky\\db\\safestore_log.db");
   
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testSelection() {
        for (int i = 0; i < 15; i++) {
                
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("model_path", "C:\\Users\\Rafox\\Documents\\NetBeansProjects\\flexsky\\flexsky\\optimization.mod");
        
        //User parameters - TODO
        HashMap<String, Number> userParam = new HashMap<>();
        userParam.put("MIN_SEC", -20);
        userParam.put("MIN_PERF", -20);
        userParam.put("MIN_STO", -20);
        
        userParam.put("WEIGHT_SEC", 0.8);
        userParam.put("WEIGHT_PERF", 0);
        userParam.put("WEIGHT_STO", 0);
        userParam.put("WEIGHT_STOCOST", 0.1);
        userParam.put("WEIGHT_BWCOST", 0.1);
        userParam.put("WEIGHT_AVAIL", 0);
        userParam.put("WEIGHT_DUR", 0);
        
        userParam.put("PROV_REQ", 7);
        
        
        
        LPSelector lps = new LPSelector();
        DispersalSelection ds = lps.select(new ArrayList(ssm.getAccounts()), ssm.listModules(), userParam, parameters);
        
        }
        
    }
    
}
