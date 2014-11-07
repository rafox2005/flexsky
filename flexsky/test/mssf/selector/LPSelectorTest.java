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

import management.StoreSafeManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
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
        ssm = StoreSafeManager.getInstance("/home/rlibardi/NetBeansProjects/flexsky/flexsky/db/SAC-experiments-files.db", "/home/rlibardi/NetBeansProjects/flexsky/flexsky/db/safestore_log.db");
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testSomeMethod() {
        
        //User parameters - TODO

        
        LPSelector lps = new LPSelector();
        lps.select(ssm.getAccounts(), ssm.listModules(), null, null)
        ssm.getAccounts()
    }
    
}
