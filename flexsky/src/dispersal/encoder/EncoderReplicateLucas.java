package dispersal.encoder;

import dispersal.IEncoderIDA;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class example to the Dispersal Module Encoder
 *
 *
 * @author lucasjadami
 */
public class EncoderReplicateLucas extends IEncoderIDA
{

    public EncoderReplicateLucas(int totalParts, int reqParts, InputStream inputStreamToDisperse, OutputStream[] dispersalStreams, HashMap additionalOptions)
    {
        super(totalParts, reqParts, inputStreamToDisperse, dispersalStreams, additionalOptions);
    }

    @Override
    public long encode()
    {

        byte[] buffer = new byte[1024];
        int len;
        int sliceSize = 0; //Variable to handle

        try
        {
            while ((len = this.disFile.read(buffer)) != -1)
            {
                for (int i = 0; i < this.disParts.length; i++)
                {
                    disParts[i].write(buffer, 0, len);
                }

                //Update the slice size 
                sliceSize += len;
            }
        } catch (IOException ex)
        {
            Logger.getLogger(EncoderReplicateLucas.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.cleanUp();

        return sliceSize;
    }
}
