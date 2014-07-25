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
package management;

import data.StoreSafeAccount;
import data.StoreSafeFile;
import data.StoreSafeSlice;
import dispersal.IDecoderIDA;
import dispersal.IEncoderIDA;
import dispersal.rabin.DecoderRabinIDA;
import dispersal.rabin.EncoderRabinIDA;
import dispersal.reedsolomon.DecoderRS;
import dispersal.reedsolomon.EncoderRS;
import driver.DiskDriver;
import driver.IDriver;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Date;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import util.StoreSafeLogger;

/**
 *
 * @author rafox
 */
class StorageManager
{

    public boolean storeFile(File file, StoreSafeFile ssf, ArrayList<StoreSafeSlice> slices, ArrayList<StoreSafeAccount> listAccounts)
    {
        IEncoderIDA ida = null;
        ArrayList<OutputStream> outputStreams = new ArrayList<>();

        //Get the upload streams for each driver        
        for (int i = 0; i < slices.size(); i++) {
            IDriver sliceDriver = null;
            StoreSafeSlice currentSlice = slices.get(i);
            StoreSafeAccount currentAccount = listAccounts.get(i);

            //Disk Driver
            if (currentAccount.getType() == 0) {
                sliceDriver = new DiskDriver(currentAccount.getName(), currentAccount.getPath());
            }
            //Add outputstream to list
            outputStreams.add(sliceDriver.getSliceUploadStream(currentSlice));

        }

        //Get the desired dispersal method
        if ("rabin".equals(ssf.getDispersalMethod())) {
            OutputStream[] aux = new OutputStream[ssf.getTotalParts()];
            ida = new EncoderRabinIDA(ssf.getTotalParts(), ssf.getReqParts(), file, outputStreams.toArray(aux));
        }
        else if ("rs".equals(ssf.getDispersalMethod())) {
            OutputStream[] aux = new OutputStream[ssf.getTotalParts()];
            ida = new EncoderRS(ssf.getTotalParts(), ssf.getReqParts(), file, outputStreams.toArray(aux));
        }

        //Encode
        Date start, end;
        start = new Date(System.currentTimeMillis());
        long sliceSize = ida.encode();
        
        //Finish and log everything
            end = new Date(System.currentTimeMillis());
            StoreSafeLogger.addLog("file", ssf.getId(), "Dispersal-" + ssf.getDispersalMethod() + "-" + ssf.getSize(), start, end);
           

        //Update important values
        ssf.setHash(ida.getFileHash());
        String[] partsHash = ida.getPartsHash();

        for (int i = 0; i < slices.size(); i++) {
            StoreSafeSlice currentSlice = slices.get(i);
            currentSlice.setPath(currentSlice.getFile() + "-" + currentSlice.getPartIndex());
            currentSlice.setHash(partsHash[i]);
            currentSlice.setSize(sliceSize);

        }
        
        return true;

    }
    
    public boolean downloadFile(File file, StoreSafeFile ssf, ArrayList<StoreSafeSlice> slices, ArrayList<StoreSafeAccount> listAccounts)
    {
        try {
            IDecoderIDA ida = null;
            ArrayList<InputStream> inputStreams = new ArrayList<>();
            OutputStream os = new FileOutputStream(file);
            
            //Get the download streams for each driver
            for (int i = 0; i < slices.size(); i++) {
                IDriver sliceDriver = null;
                StoreSafeSlice currentSlice = slices.get(i);
                StoreSafeAccount currentAccount = listAccounts.get(i);
                
                //Disk Driver
                if (currentAccount.getType() == 0) {
                    sliceDriver = new DiskDriver(currentAccount.getName(), currentAccount.getPath());
                }
                //Add inputstream to list
                //Try to get the inputstreams
                InputStream input = sliceDriver.getSliceDownloadStream(currentSlice);
                
                if (input != null) {
                    inputStreams.add(input);
                }           
                              
                
            }
            
            //Check if got minimum req. parts
                if (inputStreams.size() < ssf.getReqParts()) {
                    throw new Exception("ERROR: Not able to retrieve the minimum amount of parts required");                    
                }
            
            //Get the desired dispersal method
            if ("rabin".equals(ssf.getDispersalMethod())) {
                InputStream[] aux = new InputStream[ssf.getReqParts()];
                ida = new DecoderRabinIDA(ssf.getTotalParts(), ssf.getReqParts(), inputStreams.toArray(aux), os);
            }
            else if ("rs".equals(ssf.getDispersalMethod())) {
                InputStream[] aux = new InputStream[ssf.getReqParts()];
                ida = new DecoderRS(ssf.getTotalParts(), ssf.getReqParts(), inputStreams.toArray(aux), os);
            }
            
            Date start, end;
            start = new Date(System.currentTimeMillis());
            
            //Decode
            ida.decode();
            
            //Finish and log everything
            end = new Date(System.currentTimeMillis());
            StoreSafeLogger.addLog("file", ssf.getId(), "Retrieval-" + ssf.getDispersalMethod() + "-" + ssf.getSize(), start, end);
           
            
            //Check if file hash match
            if (ssf.getHash().equals(ida.getFileHash()))
                return true;
            else
                throw new Exception("ERROR: recovered file hash differs from the original");
        } catch (Exception ex) {
            Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, null, ex);
            //Delete output file
            file.deleteOnExit();
            return false;
        }
        
    }

}
