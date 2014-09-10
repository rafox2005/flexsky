/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dispersal.encoder;

import dispersal.IEncoderIDA;
import dispersal.rabin.RabinIDA;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestInputStream;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;

/**
 *  Class that take a file and code into multiple parts using Rabin IDA
 * @author rafox
 */
public class EncoderRabinIDA extends IEncoderIDA {
    private final RabinIDA ida;
    private String fileHash;
    private byte[] slicesHash;
    

    /**
     * Constructor for the Encoding process
     *
     * @param totalParts Number of total parts of the schemma used
     * @param reqParts  Number of required parts of the schemma used
     * @param fileToDisperse File to be dispersed
     * @param dispersalStreams Streams to Write the dispersed parts to
     */
    public EncoderRabinIDA(int totalParts, int reqParts, File fileToDisperse, OutputStream[] dispersalStreams) {
        super(totalParts, reqParts, fileToDisperse, dispersalStreams);
        this.ida = new RabinIDA(totalParts, reqParts);
    }
    
        public EncoderRabinIDA(int totalParts, int reqParts, InputStream fileToDisperse, OutputStream[] dispersalStreams, HashMap additionalOptions) {
        super(totalParts, reqParts, fileToDisperse, dispersalStreams, additionalOptions);
        this.ida = new RabinIDA(totalParts, reqParts);
    }

    /**
     * Encode the whole file and store the output streams in this object
     *
     */
    @Override
    public long encode() {
        
        long sliceCount = 0;     
        long size = 0;
        
        try {
            byte[] input = new byte[reqParts];
            int len = 0;            

            while (( len = disFile.read(input, 0, reqParts) ) != -1) {
                size += len;
                byte[] crypt = this.ida.encodeEach(input);
                for (int i=0; i < totalParts; i++) {
                    
                    this.disParts[i].write(crypt[i]);                    
                }
                sliceCount++;
                input = new byte[reqParts];
                
                //Check if the buffers are full to flush them
                //if (this.writeBufs[0].size() == this.bufSize) this.flush();
            }
            
            
            //Check if need to add 0 to the file
            if (size % reqParts != 0)
            {
            int padding = reqParts - (int) (size % reqParts);
            byte[] toPadding = new byte[padding];
            disFile.getMessageDigest().update(toPadding);
            }
            
            //Flush at the end
            this.cleanUp();           
                         


        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return sliceCount;
    }
    
}
