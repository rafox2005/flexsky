package dispersal.decoder;

import dispersal.IDecoderIDA;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import util.Monitor;

/**
 *
 * @author rlibardi
 */
public class DecoderReplicate extends IDecoderIDA {

    public DecoderReplicate(int totalParts, int reqParts, InputStream[] parts, OutputStream file, HashMap additionalOptions) {
        super(totalParts, reqParts, parts, file, additionalOptions);
    }

    @Override
    public void decode() {
        int[] indexes = this.readPartsIndex();
        try {
            byte[] input = new byte[1024];
            int len = 0;
            int offset = 0;
            while ((len = this.readParts(input)) != -1) {                
                    this.disFile.write(input, 0, 1);
                    offset += len;
            }
            
            this.disFile.flush ();
            
            //Close the Input Buffers
            for (int i = 0; i < this.readBufs.length; i++) {

                this.readBufs[i].close();
            }
            
            //CLose the output buffer aswell
            this.disFile.close();
            
        } catch (IOException ex) {
            Logger.getLogger(DecoderReplicate.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    
}
