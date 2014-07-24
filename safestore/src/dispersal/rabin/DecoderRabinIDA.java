/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dispersal.rabin;

import dispersal.IDecoderIDA;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import util.Monitor;

/**
 * Class that takes the parts and decode into one file using Rabin IDA
 *
 * @author rafox
 */
public class DecoderRabinIDA extends IDecoderIDA
{

    private RabinIDA rabin;

    /**
     * Constructor for the Decoding process
     *
     * @param totalParts Number of total parts of the schema used
     * @param reqParts Number of required parts of the schema used
     * @param parts File vector with the parts to create the read buffers needed
     */
    public DecoderRabinIDA(int totalParts, int reqParts, File[] parts, File fileToWrite)
    {
        super(totalParts, reqParts, parts, fileToWrite);
        this.rabin = new RabinIDA(totalParts, reqParts);
    }

    /**
     * Constructor for the Decoding process
     *
     * @param totalParts Number of total parts of the schema used
     * @param reqParts Number of required parts of the schema used
     * @param parts File vector with the parts to create the read buffers needed
     */
    public DecoderRabinIDA(int totalParts, int reqParts, File[] parts, OutputStream fileToWrite)
    {
        super(totalParts, reqParts, parts, fileToWrite);
        this.rabin = new RabinIDA(totalParts, reqParts);
    }

    /**
     * Constructor for the Decoding process
     *
     * @param totalParts Number of total parts of the schema used
     * @param reqParts Number of required parts of the schema used
     * @param parts BufferedInputStream vector with the parts to create the read
     * buffers needed
     * @param fileToWrite
     */
    public DecoderRabinIDA(int totalParts, int reqParts, InputStream[] parts, OutputStream fileToWrite)
    {
        super(totalParts, reqParts, parts, fileToWrite);
        this.rabin = new RabinIDA(totalParts, reqParts);

    }

    /**
     * Decode all the parts into the writeBuffer
     *
     */
    @Override
    public void decode()
    {
        try {
            byte[] input = new byte[reqParts];
            int[] indexes = this.readPartsIndex();
            int remaining;
            while ((remaining = this.readParts(input)) != -1) {
                int countBytes = reqParts;
                Monitor.getInstance().startTimeToDecode();
                byte[] decrypt = this.rabin.decodeEachEnough(input, indexes);
                Monitor.getInstance().stopTimeToDecode();
                if (remaining == 0) {
                    for (int i = decrypt.length - 1; i >= 0; i--) {
                        if (decrypt[i] == 0) {
                            countBytes--;
                        } else {
                            break;
                        }
                    }
                }

                this.disFile.write(decrypt, 0, countBytes);
            }
            this.disFile.flush();

            //Close the Input Buffers
            for (int i = 0; i < reqParts; i++) {

                this.readBufs[i].close();
            }

            }catch (IOException e) {
            System.out.println(e.getMessage());
        }
        }
    }
