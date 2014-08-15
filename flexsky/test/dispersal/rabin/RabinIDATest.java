/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dispersal.rabin;

import java.nio.ByteBuffer;
import java.util.Arrays;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import dispersal.rabin.RabinIDA;

/**
 *
 * @author mira
 */
public class RabinIDATest {
    
    public RabinIDATest() {
    }
    public int reqParts = 3;
    public int totParts = 5;
    public RabinIDA rabin = new RabinIDA(totParts, reqParts);
    
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
    public void testSomeMethod() {
    }
    
    @Test
    public void testGenerateVandermondeMatrix() {
        int[][] vandermonde = this.rabin.getVandermondeMatrix();
        int[][] vandermondeTest =  { {1, 2, 4}, {1, 3, 5}, {1, 4, 16}, {1, 5, 17}, {1, 6, 20} };
        assertThat(vandermonde, equalTo(vandermondeTest));  
        
    }
         
    @Test
    public void testEncodeDecodePart() {
        byte[] origin = "Te\n".getBytes();
        byte[] crypt = this.rabin.encodeEach(origin);   
        byte[] msg = this.rabin.decodeEach(crypt, new int[]{4, 3, 1});
        assertThat(origin, equalTo(msg));
        
    }    

    
    
}