/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dispersal.encoder;

import dispersal.IEncoderIDA;
import dispersal.rabin.RabinIDA;
import dispersal.reedsolomon.RsEncode;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import util.Monitor;
import util.Utils;

/**
 *
 * @author rafox
 */
public class EncoderRS extends IEncoderIDA
{

    private int parParts;
    private RsEncode ida;

    public EncoderRS(int totalParts, int reqParts, File fileToDisperse, OutputStream[] dispersalStreams)
    {
        
        super(((totalParts - reqParts) * 2) + reqParts, reqParts, fileToDisperse, dispersalStreams);
        this.parParts = (totalParts - reqParts) * 2;
        this.ida = new RsEncode(parParts);
    }

    /**
     * Encode the whole file and store the output streams in this object
     *
     * @param cryptmsg encrypted message to be decoded
     * @param vandermondeIndexes index of the parts
     * @return message
     */
    @Override
    public long encode()
    {
        long sliceCount = 0;
        try {
            byte[] input = new byte[reqParts];

            int[] parity = new int[reqParts + parParts];
            while (this.disFile.read(input, 0, reqParts) != -1) {
                int[] inputInt = Utils.bytesToInts(input);
                System.arraycopy(inputInt, 0, parity, 0, inputInt.length);
                Monitor.getInstance().startTimeToEncode();
                this.ida.encode(parity);
                Monitor.getInstance().stopTimeToEncode();
                byte[] crypt = Utils.intsToBytes(parity);
                for (int i = 0; i < reqParts + parParts; i++) {
                    this.disParts[i].write(crypt[i]);
                }
                sliceCount++;
                /*
                //Check if the buffers are full to flush them
                if (this.writeBufs[0].size() == this.bufSize) {
                    this.flush();
                }
                        */
            }
            //Flush at the end
            this.flush();
            
            //Close the Output Buffers
            for (int i = 0; i < totalParts; i++) {
                try {
                    this.writeBufs[i].close();
                } catch (IOException ex) {
                    Logger.getLogger(EncoderRS.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                //Close the input buffer
                this.readBuffer.close();
            }            

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return sliceCount;

    }
}
