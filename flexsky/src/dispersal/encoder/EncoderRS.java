/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dispersal.encoder;

import com.google.common.primitives.Ints;
import dispersal.IEncoderIDA;
import dispersal.reedsolomon.RsEncode;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
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
        
        //super(((totalParts - reqParts) * 2) + reqParts, reqParts, fileToDisperse, dispersalStreams);
        super(totalParts, reqParts, fileToDisperse, dispersalStreams);
        this.parParts = (totalParts - reqParts);
        this.ida = new RsEncode(parParts);
    }
    
    public EncoderRS(int totalParts, int reqParts, InputStream fileToDisperse, OutputStream[] dispersalStreams, HashMap parameters)
    {
        
        //super(((totalParts - reqParts) * 2) + reqParts, reqParts, fileToDisperse, dispersalStreams);
        super(totalParts, reqParts, fileToDisperse, dispersalStreams, parameters);
        this.parParts = (totalParts - reqParts);
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

            int[] parity = new int[parParts];
            while (this.disFile.read(input, 0, reqParts) != -1) {
                int[] inputInt = Utils.bytesToInts(input);
                //System.arraycopy(inputInt, 0, parity, 0, inputInt.length);
                this.ida.encode(inputInt, parity);    
                                                               
                byte[] crypt = Utils.intsToBytes(Ints.concat(inputInt, parity));
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
            this.cleanUp();           

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return sliceCount;

    }
}
