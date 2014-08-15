/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dispersal.rabin;

import dispersal.decoder.DecoderRabinIDA;
import dispersal.encoder.EncoderRabinIDA;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import management.StoreSafeManagerTest;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author rafox
 */
public class EncoderRabinIDATest
{

    public File file = new File(StoreSafeManagerTest.pathToTestFile);
    
    public EncoderRabinIDATest()
    {

        //this.file = new File(tmpdir, "video2.3gp");
        //this.file = new File(tmpdir, "book.pdf");
        //this.file = new File(tmpdir, "test.txt");

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
    public void setUp()
    {
    }

    @After
    public void tearDown()
    {
        
    }

    @Ignore @Test
    public void testEncodeAndDecode()
    {
        OutputStream[] outStreams = new OutputStream[5];
        for (int i = 0; i < 5; i++) {
            try {
                outStreams[i] = new FileOutputStream(new File(this.file, "videoRA" + i));
                //outStreams[i] = new FileOutputStream(new File(tmpdir, "bookRA" + i));
                //outStreams[i] = new FileOutputStream(new File(tmpdir, "testRA" + i));
            } catch (Exception e) {
            }

        }

        EncoderRabinIDA ida = new EncoderRabinIDA(5, 3, this.file, outStreams);

        ida.encode();
        String teste[] = ida.getPartsHash();
        BufferedOutputStream[] partesSaida = ida.getWriteBufs();

        File[] partesEntrada = new File[3];

        for (int i = 0; i < 3; i++) {
            try {
                partesEntrada[i] = new File(this.file, "videoRA" + (i+1));
                //partesEntrada[i] = new File(tmpdir, "bookRA" + i);
                //partesEntrada[i] = new File(tmpdir, "testRA" + i);
            } catch (Exception e) {
            }

        }

        try {
            //OutputStream outputStream2 = new FileOutputStream(new File(tmpdir, "book2RA.pdf"));
            OutputStream outputStream2 = new FileOutputStream(new File(file, "video2RA.3gp"));
            //OutputStream outputStream2 = new FileOutputStream(new File(tmpdir, "test2RA.txt"));
            //dida.getWriteBuffer().writeTo(outputStream2);
            DecoderRabinIDA dida = new DecoderRabinIDA(5, 3, partesEntrada, outputStream2, null);
            dida.decode();
            String testee = dida.getFileHash();
            String teste2[] = dida.getPartsHash();
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(EncoderRabinIDATest.class.getName()).log(Level.SEVERE, null, ex);
        }

        File testeFinal = new File(file, "video2RA.3gp");
        //File testeFinal = new File(tmpdir, "book2RA.pdf");
        //File testeFinal = new File(tmpdir, "test2RA.txt");
        try {

            System.out.println(FileUtils.contentEquals(this.file, testeFinal));
        } catch (IOException ex) {
            Logger.getLogger(EncoderRabinIDATest.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
