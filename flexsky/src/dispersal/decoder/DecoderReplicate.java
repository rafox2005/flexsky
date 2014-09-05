package dispersal.decoder;

import dispersal.IDecoderIDA;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rlibardi
 */
public class DecoderReplicate extends IDecoderIDA {

    /**
     *
     * @param totalParts - Total parts to split the file
     * @param reqParts - Minimum amount of parts to retrieve the file correctly
     * @param parts - InputStream of the parts to read
     * @param file - OutputStream to write the file
     * @param additionalOptions - Flexible parameters file to add additional parameters to the class
     */
    public DecoderReplicate(int totalParts, int reqParts, InputStream[] parts, OutputStream file, HashMap additionalOptions) {
        super(totalParts, reqParts, parts, file, additionalOptions);
    }

    /**
     * Example decoder method. Reads from the parts using the aux function readParts() offered by the superclass
     * perform the desired joining operation and write in the DigestInputStream for the file
     *
     * 
     */
    @Override
    public void decode() {
        //Read the parts indexes, to know which parts were sucessfully retrieved
        int[] indexes = this.readPartsIndex();
        try {
            //Create variables to handle the desired operations
            byte[] input = new byte[1024];
            int len = 0;
            
            //Read from the parts using the readParts function from the superclass, which reads the bytes from the parts
            while ((len = this.readParts(input)) != -1) {
                //Do the desired operations to join the file
                
                
                
                //Write to the file DigestOutputStream the bytes resulting from the joining operation
                    this.disFile.write(input, 0, 1);
            }
            
            this.cleanUp();
            
        } catch (IOException ex) {
            Logger.getLogger(DecoderReplicate.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    
}
