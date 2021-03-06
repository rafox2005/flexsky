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
package storage.driver;

import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
import data.DataSlice;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import management.StoreSafeManager;
import storage.IDriver;

/**
 *
 * @author rlibardi
 */
public class WebDavDriver implements IDriver {

    private final String name;
    private final String path;

    public WebDavDriver(String name, String path) {
        this.name = name;
        this.path = path;

    }

    @Override
    public boolean deleteSlice(DataSlice slice, HashMap<String, String> additionalParameters) throws IOException {
        Sardine sardine = SardineFactory.begin(additionalParameters.get("username"), additionalParameters.get("password"));
        sardine.delete(this.path + slice.getPath());
        return true;
    }

    @Override
    public InputStream getSliceDownloadStream(DataSlice slice, HashMap<String, String> additionalParameters) throws IOException {
        Sardine sardine = SardineFactory.begin(additionalParameters.get("username"), additionalParameters.get("password"));

        if (sardine.exists(this.path + slice.getPath()))
        { 
           InputStream is = sardine.get(this.path + slice.getPath());
           return is;
        }
        else new IOException("Slice not found at the provider");
        return null;
    }

    @Override
    public OutputStream getSliceUploadStream(DataSlice slice, HashMap<String, String> additionalParameters) throws IOException {
            final String pathT = this.path;

            PipedInputStream in = new PipedInputStream(StoreSafeManager.bufferSize);
            PipedOutputStream out = new PipedOutputStream(in);

            //New Thread to send what comes in the OutputStream
            new Thread(
                    new Runnable() {
                        public void run() {
                            try {
                                //Create Sardine Object and the URL parser
                                Sardine sardine = SardineFactory.begin(additionalParameters.get("username"), additionalParameters.get("password"));
                                URL url = new URL(pathT);

                                //Enable auth
                                sardine.enablePreemptiveAuthentication(url.getHost());
                                //Set the remotePath WebDav
                                String remotePath = pathT + slice.getFile() + "-" + String.valueOf(slice.getPartIndex());
                                //Send the Stream
                                sardine.exists(remotePath);
                                sardine.put(remotePath, in);
                            } catch (MalformedURLException ex) {
                                Logger.getLogger(WebDavDriver.class.getName()).log(Level.SEVERE, "WebDav URL incorrectly created " + pathT, ex);
                            } catch (IOException ex) {
                                Logger.getLogger(WebDavDriver.class.getName()).log(Level.SEVERE, "WebDav Upload IO error " + pathT, ex);
                            }
                        }
                    }
            ).start();

            return out;
    }

}
