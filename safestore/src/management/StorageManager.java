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
import java.lang.reflect.InvocationTargetException;
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

    public boolean storeFile(File file, StoreSafeFile ssf, ArrayList<StoreSafeSlice> slices, ArrayList<StoreSafeAccount> listAccounts) {
        try {
            IEncoderIDA ida = null;
            StorageOptions options = ssf.getOptions();

        //Get the upload streams for each driver        
        for (int i = 0; i < slices.size(); i++) {
            IDriver sliceDriver = null;
            StoreSafeSlice currentSlice = slices.get(i);
            StoreSafeAccount currentAccount = listAccounts.get(i);

                //Use Reflection to retrieve the Storage Driver required
                sliceDriver = (IDriver) Class.forName(currentAccount.getType()).getDeclaredConstructor(String.class, String.class).newInstance(currentAccount.getName(), currentAccount.getPath());
                
                //Add outputstream to list
                outputStreams.add(sliceDriver.getSliceUploadStream(currentSlice));

            }
            //Add outputstream to list
            outputStreams.add(sliceDriver.getSliceUploadStream(currentSlice));

            if (options.filePipeline.size() > 0) {

                //Implement Pipeline Filters using Aux File
                File tmpFile1 = new File(file.getAbsolutePath() + "-temp1");
                File tmpFile2 = new File(file.getAbsolutePath() + "-temp2");
                FileUtils.copyFile(file, tmpFile1);

                InputStream inputAux = new FileInputStream(tmpFile1);
                OutputStream outputAux = new FileOutputStream(tmpFile2);

                for (IPipeProcess pipe : options.filePipeline) {
                    pipe.process(inputAux, outputAux, options.additionalParameters);
                    FileUtils.copyFile(tmpFile2, tmpFile1);
                }

                //Close aux streams, delete tmp file 1 and redirect file to tmp2
                inputAux.close();
                outputAux.close();
                tmpFile1.delete();
                file = tmpFile2;

            }

            //Get the desired dispersal method
            if ("rabin".equals(ssf.getDispersalMethod())) {
                OutputStream[] aux = new OutputStream[ssf.getTotalParts()];
                ida = new EncoderRabinIDA(ssf.getTotalParts(), ssf.getReqParts(), file, outputStreams.toArray(aux));
            } else if ("rs".equals(ssf.getDispersalMethod())) {
                OutputStream[] aux = new OutputStream[ssf.getTotalParts()];
                ida = new EncoderRS(ssf.getTotalParts(), ssf.getReqParts(), file, outputStreams.toArray(aux));
            }
            Date start, end;
            start = new Date(System.currentTimeMillis());
            long sliceSize = ida.encode();
            //Finish and log everything
            end = new Date(System.currentTimeMillis());
            StoreSafeLogger.addLog("file", ssf.getId(), "Dispersal-" + ssf.getDispersalMethod() + "-" + ssf.getSize(), start, end);
            //Update important values
            ssf.setHash(ida.getFileHash());
            
            //TODO Pipeline slices
            
            String[] partsHash = ida.getPartsHash();
            for (int i = 0; i < slices.size(); i++) {
                StoreSafeSlice currentSlice = slices.get(i);
                currentSlice.setPath(currentSlice.getFile() + "-" + currentSlice.getPartIndex());
                currentSlice.setHash(partsHash[i]);
                currentSlice.setSize(sliceSize);

            }
            if (options.filePipeline.size() > 0) {
                file.delete();
            }
            return true;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (IOException ex) {
            Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (InstantiationException ex) {
            Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
        }
        return false;

        //Get the desired dispersal method
        if ("rabin".equals(ssf.getDispersalMethod())) {
            OutputStream[] aux = new OutputStream[ssf.getTotalParts()];
            ida = new EncoderRabinIDA(ssf.getTotalParts(), ssf.getReqParts(), file, outputStreams.toArray(aux));
        }
        else if ("rs".equals(ssf.getDispersalMethod())) {
            OutputStream[] aux = new OutputStream[ssf.getTotalParts()];
            ida = new EncoderRS(ssf.getTotalParts(), ssf.getReqParts(), file, outputStreams.toArray(aux));
        }

    public boolean downloadFile(File file, StoreSafeFile ssf, ArrayList<StoreSafeSlice> slices, ArrayList<StoreSafeAccount> listAccounts) {
        try {
            IDecoderIDA ida = null;
            ArrayList<InputStream> inputStreams = new ArrayList<>();
            OutputStream os = new FileOutputStream(file);
            StorageOptions options = ssf.getOptions();

            //Get the download streams for each driver
            for (int i = 0; i < slices.size(); i++) {
                IDriver sliceDriver = null;
                StoreSafeSlice currentSlice = slices.get(i);
                StoreSafeAccount currentAccount = listAccounts.get(i);

                sliceDriver = (IDriver) Class.forName(currentAccount.getType()).getDeclaredConstructor(String.class, String.class).newInstance(currentAccount.getName(), currentAccount.getPath());
                
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
            if (ssf.getHash().equals(ida.getFileHash())) {

                if (options.filePipeline.size() > 0) {

                    //Implement Pipeline Filters using Aux File
                    File tmpFile1 = new File(file.getAbsolutePath() + "-temp1");
                    File tmpFile2 = new File(file.getAbsolutePath() + "-temp2");
                    FileUtils.copyFile(file, tmpFile1);

                    InputStream inputAux = new FileInputStream(tmpFile1);
                    OutputStream outputAux = new FileOutputStream(tmpFile2);

                    for (IPipeProcess pipe : options.filePipeline) {
                        pipe.reverseProcess(inputAux, outputAux, options.additionalParameters);
                        FileUtils.copyFile(tmpFile2, tmpFile1);
                    }

                    //Close aux streams, delete tmp file 1 copy file2 to file and delete tmp2
                    inputAux.close();
                    outputAux.close();
                    tmpFile1.delete();
                    FileUtils.copyFile(tmpFile2, file);
                    tmpFile2.delete();

                }

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

    public boolean deleteSlice(StoreSafeSlice slice, StoreSafeAccount currentAccount) {
        
        try {
            IDriver sliceDriver = (IDriver) Class.forName(currentAccount.getType()).getDeclaredConstructor(String.class, String.class).newInstance(currentAccount.getName(), currentAccount.getPath());
           return sliceDriver.deleteSlice(slice);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;
    }
}
