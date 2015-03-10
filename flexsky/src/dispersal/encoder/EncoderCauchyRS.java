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
package dispersal.encoder;

import dispersal.IEncoderIDA;
import dispersal.rabin.RabinIDA;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import org.jigdfs.ida.base.InformationDispersalCodec;
import org.jigdfs.ida.base.InformationDispersalEncoder;
import org.jigdfs.ida.cauchyreedsolomon.CauchyInformationDispersalCodec;
import org.jigdfs.ida.exception.IDAInvalidParametersException;
import org.jigdfs.ida.exception.IDANotInitializedException;

/**
 *
 * @author Rafox
 */
public class EncoderCauchyRS extends IEncoderIDA {

    private final InformationDispersalEncoder ida;
    private String fileHash;
    private byte[] slicesHash;
    
    public EncoderCauchyRS(int totalParts, int reqParts, InputStream inputStreamToDisperse, OutputStream[] dispersalStreams, HashMap additionalOptions) throws IDAInvalidParametersException, IDANotInitializedException {
        super(totalParts, reqParts, inputStreamToDisperse, dispersalStreams, additionalOptions);
        InformationDispersalCodec crsidacodec = new CauchyInformationDispersalCodec(this.totalParts, (this.totalParts-this.reqParts), 4096);
        this.ida = crsidacodec.getEncoder();        
    }

    @Override
    public long encode() {
        
        long sliceCount = 0;     
        long size = 0;
        
        try {
            byte[] input = new byte[4096];
            int len = 0;            

            while (( len = disFile.read(input, 0, input.length) ) != -1) {
                size += len;
                
                List<byte[]> crypt = ida.process(input);

                for(byte[] b:crypt){
                    
                    this.disParts[crypt.indexOf(b)].write(b);                    
                }
                sliceCount+=4096;
                input = new byte[4096];
                
                
            }           
            
            
            //Flush at the end
            this.cleanUp();           
                         
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return sliceCount;
    }
    
}
