package dispersal.encoder;

import dispersal.IEncoderIDA;
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
public class EncoderReplicate extends IEncoderIDA {

    public EncoderReplicate(int totalParts, int reqParts, InputStream inputStreamToDisperse, OutputStream[] dispersalStreams, HashMap additionalOptions) {
        super(totalParts, reqParts, inputStreamToDisperse, dispersalStreams, additionalOptions);
    }

    @Override
    public long encode() {
        byte[] buffer = new byte[1024];
        int len = 0;
        int offset = 0;
        try {
            while ((len = this.disFile.read(buffer)) != -1) {
                for (int i = 0; i < this.disParts.length; i++) {
                    this.disParts[i].write(buffer, 0, len);
                }
                offset += len;
            }
        } catch (IOException ex) {
            Logger.getLogger(EncoderReplicate.class.getName()).log(Level.SEVERE, null, ex);
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

        return offset;
    }

}
