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
package driver;

import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
import data.StoreSafeSlice;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rlibardi
 */
public class WebDavDriver implements IDriver {

    private final String name;
    private final String path;

    public WebDavDriver(String name, String path)
    {
        this.name = name;
        this.path = path;
        
    }  

    @Override
    public boolean deleteSlice(StoreSafeSlice slice, HashMap<String, String> additionalParameters) {
        try {
            Sardine sardine = SardineFactory.begin(additionalParameters.get("username"), additionalParameters.get("password"));
           
            sardine.delete(this.path + slice.getPath());
            return true;

        } catch (IOException ex) {
            Logger.getLogger(WebDavDriver.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    @Override
    public InputStream getSliceDownloadStream(StoreSafeSlice slice, HashMap<String, String> additionalParameters) {
        try {
            Sardine sardine = SardineFactory.begin(additionalParameters.get("username"), additionalParameters.get("password"));
            InputStream is = sardine.get(this.path + slice.getPath());
            return is;

        } catch (IOException ex) {
            Logger.getLogger(WebDavDriver.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public OutputStream getSliceUploadStream(StoreSafeSlice slice, HashMap<String, String> additionalParameters) {
        try {
            
            final String path = this.path;
            
            PipedInputStream in = new PipedInputStream();
            PipedOutputStream out = new PipedOutputStream(in);
            

            //New Thread to send what comes in the OutputStream
            new Thread(
                    new Runnable() {
                        public void run() {
                            try {
                                Sardine sardine = SardineFactory.begin(additionalParameters.get("username"), additionalParameters.get("password"));
                                URL url = new URL(path);
                                
                                sardine.enablePreemptiveAuthentication(url.getHost());
                                
                                String porra = path + slice.getFile() + "-" + String.valueOf(slice.getPartIndex());
                                sardine.put(porra, in);
                            } catch (IOException ex) {
                                Logger.getLogger(WebDavDriver.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
            ).start();

            return out;

        } catch (IOException ex) {
            Logger.getLogger(WebDavDriver.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

}
