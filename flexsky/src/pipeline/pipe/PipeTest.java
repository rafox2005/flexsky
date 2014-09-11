/*
 * Copyright 2014 rlibardi.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pipeline.pipe;

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
import management.StoreSafeManager;
import pipeline.IPipeProcess;
import util.Utils;

/** Class to add a pipe module to FlexSky
 * This class just calculate the SHA1 hash of the streams and serve as an example
 *
 * @author rlibardi
 */
public class PipeTest implements IPipeProcess {

    /**
     * This method serve as an example to create other pipe processes.
     * This function read from the inputstream, do some operation and write on the outputstream
     * @param io - InputStream that will be read
     * @param os - OutputStream that will be write
     * @param parameters - Parameters that will be passed through all modules, its a hashmap so it is very flexible
     * just add the parameters as string when starting the application and use them here, casting if necessary
     */
    @Override
    public void process(InputStream io, OutputStream os, HashMap<String,String> parameters) {
        try {           
            
            //Create variables to handle the desired operation, in this case the MessageDigest objects for Hash
            byte[] buffer = new byte[StoreSafeManager.bufferSize];
            int len;
            MessageDigest mdFile = MessageDigest.getInstance("SHA1");
            DigestInputStream disFile = new DigestInputStream(io, mdFile);
            MessageDigest mdFileOS = MessageDigest.getInstance("SHA1");
            DigestOutputStream osFile =  new DigestOutputStream(os, mdFileOS);
            
            //Loop reading the InputStream until it finishes and then do some operation and write on the OutputStream
            //In this example reading and write from the DigestStream enables the hash calculation.
            while ((len = disFile.read(buffer)) != -1) {                
                osFile.write(buffer, 0, len);
            }
            
            //Do other wanted operations, in this case it logs for Debug Info
            Logger.getLogger(PipeTest.class.getName()).log(Level.INFO, "PROCESS: Input Hash: " + Utils.getStringFromMessageDigest(disFile.getMessageDigest()) + "Output Hash: " + Utils.getStringFromMessageDigest(osFile.getMessageDigest()));
            
            //Close OutputStream - Very important to close the Output stream after reading from it.
            osFile.flush();
            osFile.close();
            
        } catch (IOException ex) {
            Logger.getLogger(PipeTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(PipeTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Inverse function example for the pipe process.
     * If the process function alters the bytes from the original source, this function needs to revert
     * the process, getting the original bytes from the stream and writing in the outputstream.
     * 
     * The example just recalculates the hash for the streams.
     *
     * @param io - InputStream that will be read
     * @param os - OutputStream that will be write
     * @param parameters - Parameters that will be passed through all modules, its a hashmap so it is very flexible
     * just add the parameters as string when starting the application and use them here, casting if necessary
     */
    @Override
    public void reverseProcess(InputStream io, OutputStream os, HashMap<String,String> parameters) {
        try {
            //Create variables to handle the desired operation, in this case the MessageDigest objects for Hash
            byte[] buffer = new byte[StoreSafeManager.bufferSize];
            int len;
            MessageDigest mdFile = MessageDigest.getInstance("SHA1");
            DigestInputStream disFile = new DigestInputStream(io, mdFile);
            MessageDigest mdFileOS = MessageDigest.getInstance("SHA1");
            DigestOutputStream osFile =  new DigestOutputStream(os, mdFileOS);
            
            //Loop reading the InputStream until it finishes and then do some operation and write on the OutputStream
            //In this example reading and write from the DigestStream enables the hash calculation.
            while ((len = disFile.read(buffer)) != -1) {
                osFile.write(buffer, 0, len);
            }
            
            //Do other wanted operations, in this case it logs for Debug Info
            Logger.getLogger(PipeTest.class.getName()).log(Level.INFO, "RPROCESS: Input Hash: " + Utils.getStringFromMessageDigest(disFile.getMessageDigest()) + "Output Hash: " + Utils.getStringFromMessageDigest(osFile.getMessageDigest()));
            
            //Close inputStream - Very important to close the input stream after reading from it.
            osFile.flush();
            osFile.close();
            
        } catch (IOException ex) {
            Logger.getLogger(PipeTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(PipeTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
