/*
 * Copyright 2014 rafox.
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

import data.StoreSafeSlice;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rafox
 */
public class DiskDriver implements IDriver
{
    private String name;
    private File path;

    public DiskDriver(String name, String path)
    {
        this.name = name;
        this.path = new File(path);
        
    }       
                
    @Override
    public OutputStream getSliceUploadStream(StoreSafeSlice slice, HashMap<String, String> additionalParameters) {
        OutputStream os = null;
        try {
            os = new FileOutputStream(new File(this.path, String.valueOf(slice.getFile()) + "-" + String.valueOf(slice.getPartIndex())));
            return os;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DiskDriver.class.getName()).log(Level.SEVERE, null, ex);
        } 
        return null;
    }
    
    @Override
    public InputStream getSliceDownloadStream(StoreSafeSlice slice, HashMap<String, String> additionalParameters) {
        InputStream is = null;
        try {
            is = new FileInputStream(new File(this.path, String.valueOf(slice.getFile()) + "-" + String.valueOf(slice.getPartIndex())));
            return is;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DiskDriver.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } 
    }
    
    @Override
    public boolean deleteSlice(StoreSafeSlice slice, HashMap<String, String> additionalParameters) {
        File file = new File(this.path, String.valueOf(slice.getFile()) + "-" + String.valueOf(slice.getPartIndex()));
        return file.delete();
    }
    
}
