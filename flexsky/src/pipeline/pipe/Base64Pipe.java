package pipeline.pipe;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import pipeline.IPipeProcess;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * BASE64 pipe.
 *
 * @author lucasjadami
 */
public class Base64Pipe implements IPipeProcess
{
    /**
     * 
     * @param io
     * @param os
     * @param parameters 
     */
    @Override
    public void process(InputStream io, OutputStream os, HashMap<String, String> parameters)
    {
        try
        {
            // Use hash param.
            boolean useHash = parameters.get("useHash").equals("true");
            
            InputStream inputStream;
            OutputStream outputStream;
            
            if (useHash)
            {
                MessageDigest mdFile = MessageDigest.getInstance("SHA1");
                inputStream = new DigestInputStream(io, mdFile);
                MessageDigest mdFileOut = MessageDigest.getInstance("SHA1");
                outputStream = new DigestOutputStream(os, mdFileOut);
            }
            else
            {
                inputStream = new BufferedInputStream(io);
                outputStream = new BufferedOutputStream(os);
            }

            BASE64Encoder encoder = new BASE64Encoder();
            encoder.encode(inputStream, outputStream);

            io.close();
        } 
        catch (IOException ex)
        {
            Logger.getLogger(Base64Pipe.class.getName()).log(Level.SEVERE, null, ex);
        } 
        catch (NoSuchAlgorithmException ex)
        {
            Logger.getLogger(Base64Pipe.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * 
     * @param io
     * @param os
     * @param parameters 
     */
    @Override
    public void reverseProcess(InputStream io, OutputStream os, HashMap<String, String> parameters)
    {
        try
        {
            // Use hash param.
            boolean useHash = parameters.get("useHash").equals("true");
            
            InputStream inputStream;
            OutputStream outputStream;
            
            if (useHash)
            {
                MessageDigest mdFile = MessageDigest.getInstance("SHA1");
                inputStream = new DigestInputStream(io, mdFile);
                MessageDigest mdFileOut = MessageDigest.getInstance("SHA1");
                outputStream = new DigestOutputStream(os, mdFileOut);
            }
            else
            {
                inputStream = new BufferedInputStream(io);
                outputStream = new BufferedOutputStream(os);
            }

            BASE64Decoder decoder = new BASE64Decoder();
            decoder.decodeBuffer(inputStream, outputStream);
            
            outputStream.flush();
            outputStream.close();

        } 
        catch (IOException ex)
        {
            Logger.getLogger(Base64Pipe.class.getName()).log(Level.SEVERE, null, ex);
        } 
        catch (NoSuchAlgorithmException ex)
        {
            Logger.getLogger(Base64Pipe.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
