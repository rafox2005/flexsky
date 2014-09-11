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
 * @author lucasjadami
 */
public class DecoderReplicateLucas extends IDecoderIDA
{
    public DecoderReplicateLucas(int totalParts, int reqParts, InputStream[] parts, OutputStream file, HashMap additionalOptions)
    {
        super(totalParts, reqParts, parts, file, additionalOptions);
    }

    @Override
    public void decode()
    {
        int[] indexes = this.readPartsIndex();
        try
        {
            byte[] buffer = new byte[1024];
            int len = 0;
            
            while ((len = this.readParts(buffer)) != -1)
            {
                disFile.write(buffer[0]);
            }

            this.cleanUp();
        } 
        catch (IOException ex)
        {
            Logger.getLogger(DecoderReplicateLucas.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
