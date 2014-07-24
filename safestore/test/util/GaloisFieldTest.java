/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import util.GaloisField;
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
public class GaloisFieldTest {
    
    public GaloisFieldTest() {
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
    public void testMultiplyMatrixGF() {
        GaloisField GF = GaloisField.getInstance();
        int[][] matrixA = { {1, 2}, {1, 3}, {1, 4}} ;
        int[][] matrixB = { {0}, {1} };
        int result[][] = GF.multiplyMatrix(matrixA, matrixB);
        int expresult[][] = { {2}, {3}, {4} };
        assertThat(result, equalTo(expresult));
        
    }
}