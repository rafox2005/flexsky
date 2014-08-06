/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dispersal.reedsolomon;

import dispersal.reedsolomon.RsEncode;
import dispersal.decoder.DecoderRS;
import dispersal.reedsolomon.RsDecode;
import dispersal.encoder.EncoderRS;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import util.Monitor;

/**
 *
 * @author rafox
 */
public class EncoderRSTest {
    
    public File file;
    File tmpdir = new File("/home/rafox/NetBeansProjects/safestore/safestore/test/filesToTest");
    
    public EncoderRSTest() {
        this.file = new File(tmpdir, "book.pdf");
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
    public void teste() {
        final int[] ref = new int[] {32, 65, 205, 69, 41, 220, 46, 128};

		// Encoding QR-CODE
		int[] qrData = new int[ref.length + 4];
		System.arraycopy(ref, 0, qrData, 0, ref.length);
		RsEncode enc = new RsEncode(4);
		enc.encode(qrData);
		System.out.println("qrData=" + java.util.Arrays.toString(qrData));
		// parity = 42, 159, 74, 221, 244, 169, 239, 150, 138, 70, 237, 85, 224, 96, 74, 219, 61

		// force add errors
		qrData[0] = 0x00;
		qrData[1] = 0x00;
                

		// Decoding QR-CODE
		RsDecode dec = new RsDecode(4);
		int r = dec.decode(qrData);
		System.out.println("r=" + r);
		System.out.println("qrData=" + java.util.Arrays.toString(qrData));
    }

    /**
     * Test of encode method, of class EncoderRS.
     **/
    @Test
    public void testEncode() {
        OutputStream[] outStreams = new OutputStream[8];
        for (int i = 0; i<7; i++)
        {
            try {
                outStreams[i] = new FileOutputStream(new File(tmpdir, "bigRS" + i));
            } catch (Exception e) {
            }
            
        } 
        
        EncoderRS reed = new EncoderRS(5, 3, this.file, outStreams);
        
        reed.encode();
        String[] teste = reed.getPartsHash();
        BufferedOutputStream[] partesSaida = reed.getWriteBufs();
        
        File[] partesEntrada = new File[7];
        
       
        for (int i = 0; i<7; i++)
        {
            try {
                partesEntrada[i] = new File(tmpdir, "bigRS" + i);
            } catch (Exception e) {
            }
            
        } 
        
        try {            
            OutputStream outputStream2 = new FileOutputStream(new File(tmpdir, "big2RS.pdf"));
            DecoderRS dreed = new DecoderRS(5, 3, partesEntrada, outputStream2);
            dreed.decode();
            String teste2[] = dreed.getPartsHash();
            String testee = dreed.getFileHash();
            
            String rola;
        } catch (Exception e) {
        }
        
        
        
        
        System.out.println("E: " + Monitor.getInstance().getTimeToEncode() + " D: " + Monitor.getInstance().getTimeToDecode());
    }   
}