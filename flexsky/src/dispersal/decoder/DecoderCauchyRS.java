/*
 * The MIT License
 *
 * Copyright 2015 Rafox.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package dispersal.decoder;

import dispersal.IDecoderIDA;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jigdfs.ida.base.InformationDispersalCodec;
import org.jigdfs.ida.base.InformationDispersalDecoder;
import org.jigdfs.ida.cauchyreedsolomon.CauchyDecode;
import org.jigdfs.ida.cauchyreedsolomon.CauchyInformationDispersalCodec;
import org.jigdfs.ida.exception.IDADecodeException;
import org.jigdfs.ida.exception.IDAInvalidParametersException;
import org.jigdfs.ida.exception.IDAInvalidSliceCountException;
import org.jigdfs.ida.exception.IDAInvalidSliceFormatException;
import org.jigdfs.ida.exception.IDANotInitializedException;

/**
 *
 * @author Rafox
 */
public class DecoderCauchyRS extends IDecoderIDA {
    private final InformationDispersalDecoder ida;
    
    public DecoderCauchyRS(int totalParts, int reqParts, InputStream[] parts, OutputStream file, HashMap additionalOptions) throws IDAInvalidParametersException, IDANotInitializedException {
        super(totalParts, reqParts, parts, file, additionalOptions);
        InformationDispersalCodec crsidacodec = new CauchyInformationDispersalCodec(this.totalParts, (this.totalParts-this.reqParts), 4096);
        ida = crsidacodec.getDecoder();
    }

    @Override
    public void decode() {
        try {
            ArrayList<byte[]> input = new ArrayList<>();
            int len;
            long segmentSize = Long.parseLong(this.additionalOptions.get("segmentSize"));
                     
                       
            while ((len = this.readParts(input, (int) segmentSize)) != -1) {
                byte[] decrypt = this.ida.process(input);            
                this.disFile.write(decrypt, 0, len);                
            }

            this.cleanUp();

            }catch (IOException e) {
            System.out.println(e.getMessage());
        } catch (IDADecodeException ex) {
            Logger.getLogger(DecoderCauchyRS.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IDANotInitializedException ex) {
            Logger.getLogger(DecoderCauchyRS.class.getName()).log(Level.SEVERE, null, ex);
        }
        }  
       
}
