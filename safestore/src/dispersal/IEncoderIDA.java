/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dispersal;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
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
import org.apache.commons.io.output.ByteArrayOutputStream;
import util.Utils;

/**
 *
 * @author rafox
 */
public abstract class IEncoderIDA
{

    protected final int bufSize = 8 * 1024;
    protected OutputStream[] filesWriteBufs;
    protected BufferedInputStream readBuffer;
    protected int reqParts;
    protected int totalParts;
    protected BufferedOutputStream[] writeBufs;
    protected MessageDigest[] mdParts;
    protected MessageDigest mdFile;
    protected DigestOutputStream[] disParts;
    protected DigestInputStream disFile;
    protected HashMap<String,String> additionalOptions;
    
    public IEncoderIDA(int totalParts, int reqParts, File fileToDisperse, OutputStream[] dispersalStreams)
    {
        this.totalParts = totalParts;
        this.reqParts = reqParts;
        this.filesWriteBufs = dispersalStreams;
        try {
            InputStream in = new FileInputStream(fileToDisperse);
            this.readBuffer = new BufferedInputStream(in, this.bufSize);
            this.writeBufs = new BufferedOutputStream[totalParts];
            this.mdParts = new MessageDigest[totalParts];
            this.disParts = new DigestOutputStream[totalParts];
            //Create Hash for the file
            this.mdFile = MessageDigest.getInstance("SHA1");
            this.disFile = new DigestInputStream(this.readBuffer, this.mdFile);
            for (int i = 0; i < totalParts; i++) {
                writeBufs[i] = new BufferedOutputStream(filesWriteBufs[i], this.bufSize);
                //Create Hashes for the parts
                this.mdParts[i] = MessageDigest.getInstance("SHA1");
                this.disParts[i] = new DigestOutputStream(writeBufs[i], this.mdParts[i]);
                this.disParts[i].write(i); //Write Part Number
            }
        } catch (IOException | NoSuchAlgorithmException e) {
            System.out.println(e.getMessage());
        }
    }
    
        public IEncoderIDA(int totalParts, int reqParts, InputStream inputStreamToDisperse, OutputStream[] dispersalStreams, HashMap additionalOptions)
    {
        this.totalParts = totalParts;
        this.reqParts = reqParts;
        this.filesWriteBufs = dispersalStreams;
        this.additionalOptions = additionalOptions;
        try {
            InputStream in = inputStreamToDisperse;
            this.readBuffer = new BufferedInputStream(in, this.bufSize);
            this.writeBufs = new BufferedOutputStream[totalParts];
            this.mdParts = new MessageDigest[totalParts];
            this.disParts = new DigestOutputStream[totalParts];
            //Create Hash for the file
            this.mdFile = MessageDigest.getInstance("SHA1");
            this.disFile = new DigestInputStream(this.readBuffer, this.mdFile);
            for (int i = 0; i < totalParts; i++) {
                writeBufs[i] = new BufferedOutputStream(filesWriteBufs[i], this.bufSize);
                //Create Hashes for the parts
                this.mdParts[i] = MessageDigest.getInstance("SHA1");
                this.disParts[i] = new DigestOutputStream(writeBufs[i], this.mdParts[i]);
                this.disParts[i].write(i); //Write Part Number
            }
        } catch (IOException | NoSuchAlgorithmException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Encode the whole file and store the output streams in this object
     *
     */
    abstract public long encode();

     protected void flush()
    {
        for (int i = 0; i < totalParts; i++) {
            try {
                this.writeBufs[i].flush();
            } catch (IOException ex) {
                Logger.getLogger(IEncoderIDA.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
     
    /**
     * Get the writeBuffer vector with the parts
     *
     * @return Write buffer of the file complete
     */
    public BufferedOutputStream[] getWriteBufs()
    {
        return writeBufs;
    }
    
    public String getFileHash()
    {
        return Utils.getStringFromMessageDigest(mdFile);
    }
    
    public String[] getPartsHash()
    {

        String[] hashStringArray = new String[totalParts];
        for (int i=0; i < totalParts; i++)
        {
            hashStringArray[i] = Utils.getStringFromMessageDigest(mdParts[i]);
        }
        return hashStringArray;
    }

}
