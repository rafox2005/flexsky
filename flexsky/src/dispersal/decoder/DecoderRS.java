/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dispersal.decoder;

import java.io.BufferedInputStream;
import dispersal.IDecoderIDA;
import java.io.BufferedOutputStream;
import dispersal.reedsolomon.RsDecode;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import org.apache.commons.io.output.ByteArrayOutputStream;
import util.Monitor;
import util.Utils;

/**
 *
 * @author rafox
 */
public class DecoderRS extends IDecoderIDA
{

    private int parParts;
    private RsDecode reed;

    public DecoderRS(int totalParts, int reqParts, InputStream[] parts, OutputStream file, HashMap additionalOptions)
    {
        super(totalParts, reqParts, parts, file, additionalOptions);
        this.parParts = totalParts - reqParts;
        this.reed = new RsDecode(parParts);

    }

    public DecoderRS(int totalParts, int reqParts, File[] parts, File file)
    {
        super(totalParts, reqParts, parts, file);
        this.parParts = totalParts - reqParts;
        this.reed = new RsDecode(parParts);

    }

    public DecoderRS(int totalParts, int reqParts, File[] parts, OutputStream fileOs, HashMap additionalOptions)
    {
        super(totalParts, reqParts, parts, fileOs, additionalOptions);
        this.parParts = totalParts - reqParts;
        this.reed = new RsDecode(parParts);

    }

    @Override
    protected int[] readPartsIndex()
    {
        try {
            int[] eachpart = new int[this.reqParts + this.parParts];
            for (int i = 0; i < this.reqParts + this.parParts; i++) {
                eachpart[i] = this.disParts[i].read();
            }
            return eachpart;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected int readParts(byte[] eachout)
    {
        try {
            byte[] eachpart = new byte[this.reqParts + this.parParts];
            for (int i = 0; i < this.reqParts + this.parParts; i++) {
                if (this.disParts[i].read(eachpart, i, 1) == -1) {
                    throw new EOFException();
                }
            }
            System.arraycopy(eachpart, 0, eachout, 0, eachpart.length);
            return this.disParts[0].available();
        } catch (Exception e) {
            return -1;
        }
    }

    @Override
    public void decode()
    {
        try {
            byte[] input = new byte[reqParts + this.parParts];
            byte[] finalInput = new byte[totalParts];
            int[] indexes = this.readPartsIndex();
            int remaining;

            while ((remaining = this.readParts(input)) != -1) {
                int j = 0;
                for (int i = 0; i < totalParts; i++) {

                    if (i == indexes[j]) {
                        finalInput[i] = input[indexes[i]];
                        if (j < indexes.length - 1) {
                            j++;
                        }
                    } else {
                        finalInput[i] = 0x00;
                    }

                }
                int[] inputInt = Utils.bytesToInts(finalInput);
                Monitor.getInstance().startTimeToDecode();
                int nerrors = this.reed.decode(inputInt);
                Monitor.getInstance().stopTimeToDecode();
                int msgindex[] = new int[reqParts];
                for (int k = 0; k < reqParts; k++) {
                    msgindex[k] = k;
                }
                int finalInt[] = Utils.partOfArray(inputInt, msgindex);
                byte[] decrypt = Utils.intsToBytes(finalInt);
                this.disFile.write(decrypt);
            }
            
            this.cleanUp();

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
