/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dispersal;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.util.HashMap;
import util.Utils;

/**
 *
 * @author rafox
 */
public abstract class IDecoderIDA
{

    protected InputStream[] readBufs;
    protected BufferedOutputStream writeBuffer;
    protected int totalParts;
    protected int reqParts;
    protected final int bufSize = 8 * 1024;
    protected MessageDigest[] mdParts;
    protected MessageDigest mdFile;
    protected DigestInputStream[] disParts;
    protected DigestOutputStream disFile;
    protected HashMap<String, String> additionalOptions;

    /**
     * Constructor for the Decoding proccess
     *
     * @param totalParts Number of total parts of the schemma used
     * @param reqParts Number of required parts of the schemma used
     * @param parts BufferedInputStream vector with the parts to create the read
     * buffers needed
     * @param file
     */
    public IDecoderIDA(int totalParts, int reqParts, InputStream[] parts, OutputStream file, HashMap additionalOptions)
    {
        this.totalParts = totalParts;
        this.reqParts = reqParts;
        this.mdParts = new MessageDigest[totalParts];
        this.disParts = new DigestInputStream[totalParts];
        this.additionalOptions = additionalOptions;
        try {
            this.readBufs = parts;
            for (int i = 0; i < parts.length; i++) {
                //Create Hashes for the parts
                this.mdParts[i] = MessageDigest.getInstance("SHA1");
                this.disParts[i] = new DigestInputStream(this.readBufs[i], this.mdParts[i]);
            }
            this.writeBuffer = new BufferedOutputStream(file, this.bufSize);
            //Create Hash for the file
            this.mdFile = MessageDigest.getInstance("SHA1");
            this.disFile = new DigestOutputStream(this.writeBuffer, this.mdFile);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Constructor for the Decoding proccess
     *
     * @param totalParts Number of total parts of the schemma used
     * @param reqParts Number of required parts of the schemma used
     * @param parts File vector with the parts to create the read buffers needed
     * @param file File to write to
     */
    public IDecoderIDA(int totalParts, int reqParts, File[] parts, File file)
    {
        try {
            InputStream[] in = new InputStream[parts.length];
            BufferedInputStream[] readBuffers = new BufferedInputStream[parts.length];
            BufferedOutputStream fileBos = new BufferedOutputStream(new FileOutputStream(file), this.bufSize);

            for (int i = 0; i < parts.length; i++) {
                in[i] = new FileInputStream(parts[i]);
                readBuffers[i] = new BufferedInputStream(in[i], this.bufSize);
            }

            this.totalParts = totalParts;
            this.reqParts = reqParts;
            this.mdParts = new MessageDigest[totalParts];
            this.disParts = new DigestInputStream[totalParts];
            this.readBufs = readBuffers;
            for (int i = 0; i < parts.length; i++) {
                //Create Hashes for the parts
                this.mdParts[i] = MessageDigest.getInstance("SHA1");
                this.disParts[i] = new DigestInputStream(this.readBufs[i], this.mdParts[i]);
            }
            this.writeBuffer = fileBos;
            //Create Hash for the file
            this.mdFile = MessageDigest.getInstance("SHA1");
            this.disFile = new DigestOutputStream(this.writeBuffer, this.mdFile);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Constructor for the Decoding proccess
     *
     * @param totalParts Number of total parts of the schemma used
     * @param reqParts Number of required parts of the schemma used
     * @param parts File vector with the parts to create the read buffers needed
     * @param file File to write to
     */
    public IDecoderIDA(int totalParts, int reqParts, File[] parts, OutputStream fileOs, HashMap additionalOptions)
    {
        try {
            InputStream[] in = new InputStream[parts.length];
            BufferedInputStream[] readBuffers = new BufferedInputStream[parts.length];
            BufferedOutputStream fileBos = new BufferedOutputStream(fileOs, this.bufSize);
            this.additionalOptions = additionalOptions;

            for (int i = 0; i < parts.length; i++) {
                in[i] = new FileInputStream(parts[i]);
                readBuffers[i] = new BufferedInputStream(in[i], this.bufSize);
            }

            this.totalParts = totalParts;
            this.reqParts = reqParts;
            this.mdParts = new MessageDigest[totalParts];
            this.disParts = new DigestInputStream[totalParts];
            this.readBufs = readBuffers;
            for (int i = 0; i < parts.length; i++) {
                //Create Hashes for the parts
                this.mdParts[i] = MessageDigest.getInstance("SHA1");
                this.disParts[i] = new DigestInputStream(this.readBufs[i], this.mdParts[i]);
            }
            this.writeBuffer = fileBos;
            //Create Hash for the file
            this.mdFile = MessageDigest.getInstance("SHA1");
            this.disFile = new DigestOutputStream(this.writeBuffer, this.mdFile);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Decode all the parts into the writeBuffer
     *
     */
    abstract public void decode();

    /**
     * Get the writeBuffer
     *
     * @return Write buffer of the file complete
     */
    public BufferedOutputStream getWriteBuffer()
    {
        return writeBuffer;
    }

    /**
     * Read the parts into the readBuffers and get each (one) byte from the parts to
     * be multiplied later
     *
     * @param eachout Byte vector to be written into with each byte from the
     * parts
     * @return 1 for success, -1 for failure
     */
    protected int readParts(byte[] eachout)
    {
        try {
            byte[] eachpart = new byte[this.reqParts];
            int len = 0;
            int read = 0;
            for (int i = 0; i < this.reqParts; i++) {
                if ((len = this.disParts[i].read(eachpart, i, 1)) == -1) {
                    throw new EOFException();
                }
                read++;
            }
            System.arraycopy(eachpart, 0, eachout, 0, eachpart.length);
            return read;
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Read the parts index to know what parts are recovered
     *
     * @return An index int vector or null if not succeed
     */
    protected int[] readPartsIndex()
    {
        try {
            int[] eachpart = new int[this.reqParts];
            for (int i = 0; i < this.reqParts; i++) {
                eachpart[i] = this.disParts[i].read();
            }
            return eachpart;
        } catch (IOException e) {
            return null;
        }
    }
    
    public String getFileHash()
    {
        return Utils.getStringFromMessageDigest(mdFile);
    }
    
    public String[] getPartsHash()
    {
        String[] hashStringArray = new String[reqParts];
        for (int i=0; i < reqParts; i++)
        {
            hashStringArray[i] = Utils.getStringFromMessageDigest(mdParts[i]);
        }
        return hashStringArray;
    }

}
