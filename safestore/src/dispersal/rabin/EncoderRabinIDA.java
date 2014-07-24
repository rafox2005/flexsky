/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dispersal.rabin;

import dispersal.IEncoderIDA;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import util.Monitor;

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

    /**
     * Encode the whole file and store the output streams in this object
     *
     */
    @Override
    public long encode() {
        long sliceCount = 0;
        try {

            byte[] input = new byte[reqParts];
            while (disFile.read(input, 0, reqParts) != -1) {
                Monitor.getInstance().startTimeToEncode();
                byte[] crypt = this.ida.encodeEach(input);
                Monitor.getInstance().stopTimeToEncode();
                for (int i=0; i < totalParts; i++) {
                    this.disParts[i].write(crypt[i]);                    
                }
                sliceCount++;
                input = new byte[reqParts];
                
                //Check if the buffers are full to flush them
                //if (this.writeBufs[0].size() == this.bufSize) this.flush();
            }
            
            //Flush at the end
            this.flush();
            
            //Close the Output Buffers
            for (int i = 0; i < totalParts; i++) {
                try {
                    this.writeBufs[i].close();
                } catch (IOException ex) {
                    Logger.getLogger(EncoderRabinIDA.class.getName()).log(Level.SEVERE, null, ex);
                }
            }      
            


        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        
        return sliceCount;
    }
    
}
