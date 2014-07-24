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

package data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rafox
 */
public class StoreSafeDataUnit {
    private final String filePath;
    private long fileSize;
    private MappedByteBuffer streamBuffer;
    private RandomAccessFile file;
    private FileChannel fileCh;
    private static long bufferSize = 8*1024;
    private long lastBufferReadPosition;

    public StoreSafeDataUnit(String filePath) {
        this.lastBufferReadPosition = 0;
        this.filePath = filePath;
        this.openFile(filePath);
    }
    
    private void openFile(String filePath) {
        if (this.streamBuffer == null) {
            try {
                long buffer;
                this.file = new RandomAccessFile(filePath, "rw");                
                this.fileCh = file.getChannel();
                this.fileSize = fileCh.size();
                
                if (this.fileSize <= this.bufferSize) buffer=this.fileSize;
                else buffer=this.bufferSize;
                
                this.streamBuffer = fileCh.map(FileChannel.MapMode.READ_WRITE, 0, buffer);                
            } catch (IOException ex) {
                Logger.getLogger(StoreSafeDataUnit.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public boolean read(byte[] byteVector) {
        try {
            long bufferToUse = 0;
            //If the buffer has nothing more to retrieve and there is still file to map, remap
            if (!this.streamBuffer.hasRemaining()) {
                this.streamBuffer = fileCh.map(FileChannel.MapMode.READ_WRITE, this.lastBufferReadPosition, Math.min(this.bufferSize, this.fileCh.size() - this.lastBufferReadPosition)); 
                this.lastBufferReadPosition += this.bufferSize;
            }
        this.streamBuffer.get(byteVector);
        return true;
        } catch (Exception ex) {
            Logger.getLogger(StoreSafeDataUnit.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
    public boolean write(byte[] byteVector) {
        try {
        this.streamBuffer.clear();
        this.streamBuffer.put(byteVector);
        return true;
        } catch (Exception ex) {
            Logger.getLogger(StoreSafeDataUnit.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
    public boolean close() {
        try {
            this.streamBuffer.force();
            this.file.getFD().sync();
            this.file.close();
            return true;
        } catch (IOException ex) {
            Logger.getLogger(StoreSafeDataUnit.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public String getFilePath() {
        return filePath;
    }
    
    
    
}
