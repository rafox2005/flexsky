package dispersal.encoder;

import dispersal.IEncoderIDA;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import management.StoreSafeManager;

/** Class example to the Dispersal Module Encoder
 *
 *
 * @author rlibardi
 */
public class EncoderReplicate extends IEncoderIDA {

    /**
     * Create the Encoder Dispersal, passing the main parameters for the dispersal process
     *
     * @param totalParts - Total parts to split the file
     * @param reqParts - Minimum amount of parts to retrieve the file correctly
     * @param inputStreamToDisperse - InputStream that will be dispersed and splitted
     * @param dispersalStreams - OutputStream vector that will be written
     * @param additionalOptions - Flexible parameters file to add additional parameters to the class
     */
    public EncoderReplicate(int totalParts, int reqParts, InputStream inputStreamToDisperse, OutputStream[] dispersalStreams, HashMap additionalOptions) {
        super(totalParts, reqParts, inputStreamToDisperse, dispersalStreams, additionalOptions);
    }

    /**
     * Example encoder method. Reads from the this.disFile (DigestInputStream created by the superclass)
     * perform the desired dispersal operation and write in the DigestOutputStreams vector the parts
     *
     * 
     */
    @Override
    public long encode() {
        
        //Create variables to handle the encoding process.
        byte[] buffer = new byte[StoreSafeManager.bufferSize];
        int len = 0;
        int sliceSize = 0; //Variable to handle
        
        //Read from the DigestInputStream and perform the desired dispersal operation and
        //then write to the DigestOutputStream vector the parts.
        try {
            //Read from the inputstream
            while ((len = this.disFile.read(buffer)) != -1) {
                //Do some desired operations
                
                
                
                //Loop trough the slices vector writing the slices
                for (int i = 0; i < this.disParts.length; i++) {                    
                    this.disParts[i].write(buffer, 0, len);
                }
                
                //Update the slice size 
                sliceSize += len;
            }
        } catch (IOException ex) {
            Logger.getLogger(EncoderReplicate.class.getName()).log(Level.SEVERE, null, ex);
        }

        //Flush at the end - Very important to make sure the buffers are writen
        this.cleanUp(); 

        return sliceSize;
    }

}
