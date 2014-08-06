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

import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
import com.github.sardine.impl.SardineException;
import data.StoreSafeAccount;
import data.StoreSafeFile;
import data.StoreSafeSlice;
import dispersal.IDecoderIDA;
import dispersal.IEncoderIDA;
import dispersal.decoder.DecoderRS;
import dispersal.decoder.DecoderRabinIDA;
import dispersal.encoder.EncoderRS;
import dispersal.encoder.EncoderRabinIDA;
import driver.DiskDriver;
import driver.IDriver;
import driver.WebDavDriver;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import pipeline.IPipeProcess;
import util.StoreSafeLogger;

/**
 *
 * @author rafox
 */
class StorageManager {

    public boolean storeFile(File file, StoreSafeFile ssf, ArrayList<StoreSafeSlice> slices, ArrayList<StoreSafeAccount> listAccounts) {
        try {
            IEncoderIDA ida = null;
            StorageOptions options = ssf.getOptions();
            InputStream fileInputStream = new FileInputStream(file);

            ArrayList<OutputStream> outputStreams = new ArrayList<>();
            //Get the upload streams for each driver
            for (int i = 0; i < slices.size(); i++) {
                IDriver sliceDriver = null;
                StoreSafeSlice currentSlice = slices.get(i);
                StoreSafeAccount currentAccount = listAccounts.get(i);

                sliceDriver = (IDriver) Class.forName(currentAccount.getType()).getDeclaredConstructor(String.class, String.class).newInstance(currentAccount.getName(), currentAccount.getPath());

                //Add outputstream to list
                outputStreams.add(sliceDriver.getSliceUploadStream(currentSlice, currentAccount.getAdditionalParameters()));

            }

            if (options.filePipeline.size() > 0) {

                //Implement Pipeline using Java Pipes
                ArrayList<InputStream> listPipesIn = new ArrayList<>();
                ArrayList<OutputStream> listPipesOut = new ArrayList<>();

                //Adding the first
                listPipesIn.add(new FileInputStream(file));

                //Creating and connecting the pipes
                for (int i = 0; i < options.filePipeline.size(); i++) {
                    PipedOutputStream out = new PipedOutputStream();
                    PipedInputStream in = new PipedInputStream(out);

                    listPipesIn.add(in);
                    listPipesOut.add(out);
                }

                //Now that we already have a pipeline, just need to send the right Streams for each PipeProcess
                for (int i = 0; i < options.filePipeline.size(); i++) {

                    //Get actual Pipe
                    IPipeProcess pipe = options.filePipeline.get(i);

                    //Get the streams
                    InputStream inT = listPipesIn.get(i);
                    OutputStream outT = listPipesOut.get(i);

                    //Run the process for the pipes in a new thread (parallel)
                    new Thread(
                            new Runnable() {
                                public void run() {
                                    try {
                                        pipe.process(inT, outT, options.additionalParameters);
                                        outT.close();
                                    } catch (IOException ex) {
                                        Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            }
                    ).start();

                }

                //Get Last InputStream
                fileInputStream = listPipesIn.get(listPipesIn.size() - 1);

            }

            OutputStream[] aux = new OutputStream[ssf.getTotalParts()];

            //Get The Desired IDA Algorithm
            ida = (IEncoderIDA) Class.forName("dispersal.encoder.Encoder" + ssf.getDispersalMethod()).
                    getDeclaredConstructor(int.class, int.class, InputStream.class, OutputStream[].class, HashMap.class).
                    newInstance(ssf.getTotalParts(), ssf.getReqParts(), fileInputStream, outputStreams.toArray(aux), ssf.getOptions().additionalParameters);

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
                InputStream input = sliceDriver.getSliceDownloadStream(currentSlice, currentAccount.getAdditionalParameters());

                if (input != null) {
                    inputStreams.add(input);
                }

            }

            //Check if got minimum req. parts
            if (inputStreams.size() < ssf.getReqParts()) {
                throw new IOException("ERROR: Not able to retrieve the minimum amount of parts required");
            }

            
            if (options.filePipeline.size() > 0) {

                    //Implement Pipeline using Java Pipes
                    ArrayList<InputStream> listPipesIn = new ArrayList<>();
                    ArrayList<OutputStream> listPipesOut = new ArrayList<>();    
                                                
                    //Creating and connecting the pipes
                    for (int i = 0; i < options.filePipeline.size(); i++) {
                        
                        PipedOutputStream out = new PipedOutputStream();
                        PipedInputStream in = new PipedInputStream(out);
                        

                        listPipesIn.add(in);
                        listPipesOut.add(out);
                    }              
                    
                    //Replace the last Output for the file writer (FileOutputStream)
                    FileOutputStream fos = new FileOutputStream(file);
                    listPipesOut.add(fos);                   
                    

                    //Now that we already have a pipeline, just need to send the right Streams for each PipeProcess
                    for (int i = 0; i < options.filePipeline.size(); i++) {

                        //Get actual Pipe
                        IPipeProcess pipe = options.filePipeline.get(i);

                        //Get the streams
                        InputStream inT = listPipesIn.get(i);
                        OutputStream outT = listPipesOut.get(i+1);

                        //Run the process for the pipes in a new thread (parallel)
                        new Thread(
                                new Runnable() {
                                    public void run() {
                                        try {
                                            pipe.reverseProcess(inT, outT, options.additionalParameters);
                                            outT.close();
                                        } catch (IOException ex) {
                                            Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, null, ex);
                                        }
                                    }
                                }
                        ).start();

                    }

                    //Get First OutputStream
                    os = listPipesOut.get(0);

                }
            
            //Get The Desired IDA Algorithm
            InputStream[] aux = new InputStream[ssf.getReqParts()];
            ida = (IDecoderIDA) Class.forName("dispersal.decoder.Decoder" + ssf.getDispersalMethod()).
                    getDeclaredConstructor(int.class, int.class, InputStream[].class, OutputStream.class, HashMap.class).
                    newInstance(ssf.getTotalParts(), ssf.getReqParts(), inputStreams.toArray(aux), os, ssf.getOptions().additionalParameters);

            
            
            Date start, end;
            start = new Date(System.currentTimeMillis());

            //Decode
            ida.decode();

            //Finish and log everything
            end = new Date(System.currentTimeMillis());
            StoreSafeLogger.addLog("file", ssf.getId(), "Retrieval-" + ssf.getDispersalMethod() + "-" + ssf.getSize(), start, end);

            //Check if file hash match
            if (ssf.getHash().equals(ida.getFileHash())) {              
                return true;
            } else {
                throw new Exception("ERROR: recovered file hash differs from the original");
            }
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
            return sliceDriver.deleteSlice(slice, currentAccount.getAdditionalParameters());
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (SecurityException ex) {
            Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (InstantiationException ex) {
            Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (IllegalAccessException ex) {
            Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (InvocationTargetException ex) {
            Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (IOException ex) {
            if (ex.getClass() == SardineException.class && ex.getLocalizedMessage().contains("404"))
            {                
            Logger.getLogger(StorageManager.class.getName()).log(Level.WARNING, "Slice already deleted", ex);
            return true;
            }
            else {
                Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "Error trying to delete file", ex);
                return false;
            }
        }
    }
}
