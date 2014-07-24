/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import util.Utils;
import static org.hamcrest.CoreMatchers.equalTo;
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
public class UtilsTest {
    
    public UtilsTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }
    
    @Test
    public void testBytesIntConversion(){    
     
        int[] teste = Utils.bytesToInts("Tes".getBytes());
        byte[] teste2 = Utils.intsToBytes(teste);
        assertThat("Tes".getBytes(), equalTo(teste2));
    }
    
    @Test
    public void testIntBytesConversion(){    
        int[] intArray = {1, 2, 3};
        byte[] teste = Utils.intsToBytes(intArray);
        int[] teste2 = Utils.bytesToInts(teste);
        assertThat(intArray, equalTo(teste2));
    }
    
    @Test
    public void testPartOfArray(){
        int[] expected = new int[]{1, 4, 6};
        
        int[] part = Utils.partOfArray(new int[]{1, 2, 3, 4, 5, 6}, new int[]{0, 3, 5});
        assertThat(part, equalTo(expected));
    }


}