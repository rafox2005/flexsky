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

package pipeline;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rlibardi
 */
public class PipeTest implements IPipeProcess {

    @Override
    public void process(InputStream io, OutputStream os, HashMap<String,String> parameters) {
        try {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = io.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            
            //Close inputStream
            io.close();
            
        } catch (IOException ex) {
            Logger.getLogger(PipeTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void reverseProcess(InputStream io, OutputStream os, HashMap<String,String> parameters) {
        try {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = io.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            
            //Close inputStream
            io.close();
            
        } catch (IOException ex) {
            Logger.getLogger(PipeTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
