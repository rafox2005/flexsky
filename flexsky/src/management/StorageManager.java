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
import data.StorageOptions;
import data.StoreSafeAccount;
import data.StoreSafeFile;
import data.StoreSafeSlice;
import dispersal.IDecoderIDA;
import dispersal.IEncoderIDA;
import dispersal.decoder.DecoderRS;
import dispersal.decoder.DecoderRabinIDA;
import dispersal.encoder.EncoderRS;
import dispersal.encoder.EncoderRabinIDA;
import storage.driver.DiskDriver;
import storage.IDriver;
import storage.driver.WebDavDriver;
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
import java.lang.management.ManagementFactory;
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
import util.monitor.RTInputStream;
import util.monitor.RTOutputStream;

/**
 *
 * @author rafox
 */
class StorageManager {

    public boolean storeFile(File file, StoreSafeFile ssf, ArrayList<StoreSafeSlice> slices, ArrayList<StoreSafeAccount> listAccounts) {
        IEncoderIDA ida = null;
        StorageOptions options = ssf.getOptions();
        RTInputStream fileInputStream = null;
        try {
            fileInputStream = new RTInputStream(new FileInputStream(file));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "STORAGE: not able to find the file to store", ex);
        }

        ArrayList<OutputStream> outputStreams = new ArrayList<>();
        ArrayList<RTOutputStream> outputStreamsOriginal = new ArrayList<>();
        
        //Get the upload streams for each driver
        for (int i = 0; i < slices.size(); i++) {
            IDriver sliceDriver = null;
            StoreSafeSlice currentSlice = slices.get(i);
            StoreSafeAccount currentAccount = listAccounts.get(i);

            try {
                sliceDriver = (IDriver) Class.forName(currentAccount.getType()).getDeclaredConstructor(String.class, String.class).newInstance(currentAccount.getName(), currentAccount.getPath());
            } catch (NoSuchMethodException ex) {
                Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "STORAGE: not able to find the constructor for the driver to store", ex);
            } catch (SecurityException ex) {
                Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "STORAGE: not able to find the constructor for the driver to store", ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "STORAGE: not able to find the driver class to store", ex);
            } catch (InstantiationException ex) {
                Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "STORAGE: not able to find the driver class to store", ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "STORAGE: not able to find the driver class to store", ex);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "STORAGE: not able to find the driver class to store", ex);
            } catch (InvocationTargetException ex) {
                Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "STORAGE: not able to find the driver class to store", ex);
            }

            try {

                if (options.slicePipeline.size() > 0) {
                    
                    //Add the
                    
                    //Implement Pipeline using Java Pipes
                    ArrayList<InputStream> sliceListPipesIn = new ArrayList<>();
                    ArrayList<OutputStream> sliceListPipesOut = new ArrayList<>();
                    

                    //Creating and connecting the pipes
                    for (int j = 0; j < options.slicePipeline.size(); j++) {
                        PipedOutputStream out = new PipedOutputStream();
                        PipedInputStream in = null;
                        try {
                            in = new PipedInputStream(out, StoreSafeManager.bufferSize);
                        } catch (IOException ex) {
                            Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "STORAGE: not able to create the PipedInputStream", ex);
                        }

                        sliceListPipesIn.add(in);
                        sliceListPipesOut.add(out);
                    }

                    //Add the last OutputStream for the driver
                    RTOutputStream driverOutput = new RTOutputStream(sliceDriver.getSliceUploadStream(currentSlice, currentAccount.getAdditionalParameters()));
                    sliceListPipesOut.add(driverOutput);
                    outputStreamsOriginal.add(driverOutput);
                    

                    //Now that we already have a pipeline, just need to send the right Streams for each PipeProcess
                    for (int j = 0; j < options.slicePipeline.size(); j++) {

                        //Get actual Pipe
                        IPipeProcess pipe = options.slicePipeline.get(j);

                        //Get the streams
                        InputStream inT = sliceListPipesIn.get(j);
                        RTOutputStream outT = new RTOutputStream(sliceListPipesOut.get(j + 1));
                        
                        final String slicePath = currentSlice.getFile() + "-" + currentSlice.getPartIndex();

                        //Run the process for the pipes in a new thread (parallel)
                        new Thread(
                                new Runnable() {
                                    public void run() {
                                        pipe.process(inT, outT, options.additionalParameters);
                                        try {
                                            outT.close();
                                            //Finish and log everything
                                            StoreSafeLogger.addSlicePipeLog(ssf, currentSlice, pipe.getClass().getName(), "UP", outT.totalTime(), outT.averageRate(), outT.totalBytes());

                                        } catch (IOException ex) {
                                            Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "Pipe problem", ex);
                                        }
                                    }
                                }
                        ).start();
                    }

                    //Add first OutputStream to list
                    outputStreams.add(new RTOutputStream(sliceListPipesOut.get(0)));
                }
                //If no slice pipeline
                else {
                    RTOutputStream driverOutput = new RTOutputStream(sliceDriver.getSliceUploadStream(currentSlice, currentAccount.getAdditionalParameters()));
                    outputStreams.add(driverOutput);
                    outputStreamsOriginal.add(driverOutput);
                }
                }catch (IOException ex) {
                    Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "STORAGE: not able to retrieve the upload stream to store", ex);
                }

            }

            if (options.filePipeline.size() > 0) {

                //Implement Pipeline using Java Pipes
                ArrayList<InputStream> listPipesIn = new ArrayList<>();
                ArrayList<OutputStream> listPipesOut = new ArrayList<>();
                
                try {
                    //Adding the first
                    listPipesIn.add(new FileInputStream(file));
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "STORAGE: not able to find the file to store", ex);
                }

                //Creating and connecting the pipes
                for (int i = 0; i < options.filePipeline.size(); i++) {
                    PipedOutputStream out = new PipedOutputStream();
                    PipedInputStream in = null;
                    try {
                        in = new PipedInputStream(out, StoreSafeManager.bufferSize);
                    } catch (IOException ex) {
                        Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "STORAGE: not able to create the PipedInputStream", ex);
                    }

                    listPipesIn.add(in);
                    listPipesOut.add(out);
                }

                //Now that we already have a pipeline, just need to send the right Streams for each PipeProcess
                for (int i = 0; i < options.filePipeline.size(); i++) {

                    //Get actual Pipe
                    IPipeProcess pipe = options.filePipeline.get(i);

                    //Get the streams
                    InputStream inT = listPipesIn.get(i);
                    RTOutputStream outT = new RTOutputStream(listPipesOut.get(i));

                    
                    //Run the process for the pipes in a new thread (parallel)
                    new Thread(
                            new Runnable() {
                                public void run() {
                                    pipe.process(inT, outT, options.additionalParameters);
                                    try {
                                        outT.close();
                                        //Finish and log everything                                     
                                        StoreSafeLogger.addFilePipeLog(ssf, pipe.getClass().getName(), "UP", outT.totalTime(), outT.averageRate(), outT.totalBytes());
                                    } catch (IOException ex) {
                                        Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "Pipe problem", ex);
                                    }
                                }
                            }
                    ).start();

                }

                //Get Last InputStream
                fileInputStream = new RTInputStream(listPipesIn.get(listPipesIn.size() - 1));

            }

            OutputStream[] aux = new OutputStream[ssf.getTotalParts()];

            try {
                //Get The Desired IDA Algorithm
                ida = (IEncoderIDA) Class.forName("dispersal.encoder.Encoder" + ssf.getDispersalMethod()).
                        getDeclaredConstructor(int.class, int.class, InputStream.class, OutputStream[].class, HashMap.class).
                        newInstance(ssf.getTotalParts(), ssf.getReqParts(), fileInputStream, outputStreams.toArray(aux), ssf.getOptions().additionalParameters);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "STORAGE: not able to retrieve the IDA to store", ex);
            } catch (NoSuchMethodException ex) {
                Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "STORAGE: not able to retrieve the IDA to store", ex);
            } catch (SecurityException ex) {
                Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "STORAGE: not able to retrieve the IDA to store", ex);
            } catch (InstantiationException ex) {
                Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "STORAGE: not able to retrieve the IDA to store", ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "STORAGE: not able to retrieve the IDA to store", ex);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "STORAGE: not able to retrieve the IDA to store", ex);
            } catch (InvocationTargetException ex) {
                Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "STORAGE: not able to retrieve the IDA to store", ex);
            }

            long sliceSize = ida.encode();
            //Finish and log everything
            StoreSafeLogger.addIDALog(ssf, "UP", fileInputStream.totalTime(), fileInputStream.averageRate(), fileInputStream.totalBytes());
            
            //Update important values
            ssf.setHash(ida.getFileHash());
            String[] partsHash = ida.getPartsHash();
            for (int i = 0; i < slices.size(); i++) {
                StoreSafeSlice currentSlice = slices.get(i);
                currentSlice.setPath(currentSlice.getFile() + "-" + currentSlice.getPartIndex());
                currentSlice.setHash(partsHash[i]);
                currentSlice.setSize(sliceSize);

            }
            
            //Log the slices outputs
        for (int i = 0; i < outputStreamsOriginal.size(); i++)
        {
            RTOutputStream os = outputStreamsOriginal.get(i);
            StoreSafeLogger.addSliceLog(ssf, slices.get(i), "UP", os.totalTime(), os.averageRate(), os.totalBytes()); 
        }

            return false;

        }

    

    

    public boolean downloadFile(File file, StoreSafeFile ssf, ArrayList<StoreSafeSlice> slices, ArrayList<StoreSafeAccount> listAccounts) {
            IDecoderIDA ida = null;
            ArrayList<InputStream> inputStreams = new ArrayList<>();
            ArrayList<RTInputStream> inputStreamsOriginal = new ArrayList<>();
            RTOutputStream os = null;
            try {
                os = new RTOutputStream(new FileOutputStream(file));
            } catch (FileNotFoundException ex) {
                Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "STORAGE: Not able to get file output", ex);
            }
            StorageOptions options = ssf.getOptions();

            //Get the download streams for each driver
            for (int i = 0; i < slices.size(); i++) {
                IDriver sliceDriver = null;
                StoreSafeSlice currentSlice = slices.get(i);
                StoreSafeAccount currentAccount = listAccounts.get(i);

                try {
                    sliceDriver = (IDriver) Class.forName(currentAccount.getType()).getDeclaredConstructor(String.class, String.class).newInstance(currentAccount.getName(), currentAccount.getPath());
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "STORAGE: Not able to find the driver required", ex);
                } catch (NoSuchMethodException ex) {
                    Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "STORAGE: Not able to find the driver required", ex);
                } catch (SecurityException ex) {
                    Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "STORAGE: Not able to find the driver required", ex);
                } catch (InstantiationException ex) {
                    Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "STORAGE: Not able to find the driver required", ex);
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "STORAGE: Not able to find the driver required", ex);
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "STORAGE: Not able to find the driver required", ex);
                } catch (InvocationTargetException ex) {
                    Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "STORAGE: Not able to find the driver required", ex);
                }

                //Add inputstream to list
                //Try to get the inputstreams
                InputStream input = null;
                try {
                    input = sliceDriver.getSliceDownloadStream(currentSlice, currentAccount.getAdditionalParameters());
                } catch (IOException ex) {
                    Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "Not able to retrieve the slice from the driver", ex);
                }
                
                RTInputStream sliceInputStream = new RTInputStream(input);
                    inputStreamsOriginal.add(sliceInputStream);

                if (input != null) {                 
                    
                    if (options.slicePipeline.size() > 0) {

                        //Implement Pipeline using Java Pipes
                        ArrayList<InputStream> sliceListPipesIn = new ArrayList<>();
                        ArrayList<OutputStream> sliceListPipesOut = new ArrayList<>();
                        
                        //Add the first InputStream from driver
                        sliceListPipesIn.add(input);

                        //Creating and connecting the pipes
                        for (int j = 0; j < options.slicePipeline.size(); j++) {
                            PipedOutputStream out = new PipedOutputStream();
                            PipedInputStream in = null;
                            try {
                                in = new PipedInputStream(out, StoreSafeManager.bufferSize);
                            } catch (IOException ex) {
                                Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "STORAGE: not able to create the PipedInputStream", ex);
                            }

                            sliceListPipesIn.add(in);
                            sliceListPipesOut.add(out);
                        }

                        //Now that we already have a pipeline, just need to send the right Streams for each PipeProcess
                        for (int j = 0; j < options.slicePipeline.size(); j++) {

                            //Get actual Pipe
                            IPipeProcess pipe = options.slicePipeline.get(j);

                            //Get the streams
                            InputStream inT = sliceListPipesIn.get(j);
                            RTOutputStream outT = new RTOutputStream(sliceListPipesOut.get(j));  

                            //Run the process for the pipes in a new thread (parallel)
                            new Thread(
                                    new Runnable() {
                                        public void run() {
                                            //Start time variables
                                            pipe.reverseProcess(inT, outT, options.additionalParameters);
                                            try {
                                                outT.close();
                                                //Finish and log everything
                                                StoreSafeLogger.addSlicePipeLog(ssf, currentSlice, pipe.getClass().getName(), "DOWN", outT.totalTime(), outT.averageRate(), outT.totalBytes());

                                            } catch (IOException ex) {
                                                Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "Pipe problem", ex);
                                            }
                                        }
                                    }
                            ).start();

                        }

                        //Get Last InputStream
                        sliceInputStream = new RTInputStream(sliceListPipesIn.get(sliceListPipesIn.size() - 1));

                    }
                    //Add the inputStream to the list of InputStreams
                    inputStreams.add(sliceInputStream);
                }            
                
            }        

        //Check if got minimum req. parts
        if (inputStreams.size() < ssf.getReqParts()) {
            throw new Error("ERROR: Not able to retrieve the minimum amount of parts required");
        }

        if (options.filePipeline.size() > 0) {

            //Implement Pipeline using Java Pipes
            ArrayList<InputStream> listPipesIn = new ArrayList<>();
            ArrayList<OutputStream> listPipesOut = new ArrayList<>();

            //Creating and connecting the pipes
            for (int i = 0; i < options.filePipeline.size(); i++) {

                PipedOutputStream out = new PipedOutputStream();
                PipedInputStream in = null;
                try {
                    in = new PipedInputStream(out, StoreSafeManager.bufferSize);
                } catch (IOException ex) {
                    Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "Storage: Problem creating the piped input stream", ex);
                }

                listPipesIn.add(in);
                listPipesOut.add(out);
            }

            //Replace the last Output for the file writer (FileOutputStream)
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(file);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "Storage: not found the output file", ex);
            }
            listPipesOut.add(fos);

            //Now that we already have a pipeline, just need to send the right Streams for each PipeProcess
            for (int i = 0; i < options.filePipeline.size(); i++) {

                //Get actual Pipe
                IPipeProcess pipe = options.filePipeline.get(i);

                //Get the streams
                InputStream inT = listPipesIn.get(i);
                RTOutputStream outT = new RTOutputStream(listPipesOut.get(i + 1));

                //Run the process for the pipes in a new thread (parallel)
                new Thread(
                        new Runnable() {
                            public void run() {
                                try {
                                    pipe.reverseProcess(inT, outT, options.additionalParameters);
                                    outT.close();                                    
                                    //Finish and log everything
                                    StoreSafeLogger.addFilePipeLog(ssf, pipe.getClass().getName(), "DOWN", outT.totalTime(), outT.averageRate(), outT.totalBytes());

                                } catch (IOException ex) {
                                    Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        }
                ).start();

            }

            //Get First OutputStream
            os = new RTOutputStream(listPipesOut.get(0));

        }

        //Get The Desired IDA Algorithm
        InputStream[] aux = new InputStream[ssf.getReqParts()];
        try {
            ida = (IDecoderIDA) Class.forName("dispersal.decoder.Decoder" + ssf.getDispersalMethod()).
                    getDeclaredConstructor(int.class, int.class, InputStream[].class, OutputStream.class, HashMap.class).
                    newInstance(ssf.getTotalParts(), ssf.getReqParts(), inputStreams.toArray(aux), os, ssf.getOptions().additionalParameters);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "Storage: Not able to retrieve the IDA", ex);
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "Storage: Not able to retrieve the IDA", ex);
        } catch (SecurityException ex) {
            Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "Storage: Not able to retrieve the IDA", ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "Storage: Not able to retrieve the IDA", ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "Storage: Not able to retrieve the IDA", ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "Storage: Not able to retrieve the IDA", ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "Storage: Not able to retrieve the IDA", ex);
        }

        //Decode
        ida.decode();

        //Finish and log everything
        StoreSafeLogger.addIDALog(ssf, "DOWN", os.totalTime(), os.averageRate(), os.totalBytes());
        String[] teste = ida.getPartsHash();
        
        for (int i = 0; i < teste.length; i++) {
            Logger.getLogger(StorageManager.class.getName()).log(Level.INFO, "RETRIEVAL: " + "P " + i + "H " + teste[i]);
        }
        
        for (int i = 0; i < inputStreamsOriginal.size(); i++)
        {
            RTInputStream is = inputStreamsOriginal.get(i);
            StoreSafeLogger.addSliceLog(ssf, slices.get(i), "DOWN", is.totalTime(), is.averageRate(), is.totalBytes()); 
        }

        
        //Check if file hash match
        if (ssf.getHash().equals(ida.getFileHash())) {
            return true;
        } else {
            file.deleteOnExit();
            throw new Error("ERROR: recovered file hash differs from the original");          
        }
}

public boolean deleteSlice(StoreSafeFile file, StoreSafeSlice slice, StoreSafeAccount currentAccount) {
        try {
            long start = System.currentTimeMillis();
            IDriver sliceDriver = (IDriver) Class.forName(currentAccount.getType()).getDeclaredConstructor(String.class, String.class).newInstance(currentAccount.getName(), currentAccount.getPath());
            boolean result = sliceDriver.deleteSlice (slice, currentAccount.getAdditionalParameters());
            
            //Finish and log everything
            double time = System.currentTimeMillis() - start;
            double rate = (slice.getSize() / 1024.0) / ( time/1000.0 );
            StoreSafeLogger.addSliceLog(file, slice, "DEL", time, rate, slice.getSize()/1024.0);
            
            return result;
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "STORAGE: Not able to retrieve the driver to delete the file", ex);
            return false;
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "STORAGE: Not able to retrieve the driver to delete the file", ex);
            return false;
        } catch (SecurityException ex) {
            Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "STORAGE: Not able to retrieve the driver to delete the file", ex);
            return false;
        } catch (InstantiationException ex) {
            Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "STORAGE: Not able to retrieve the driver to delete the file", ex);
            return false;
        } catch (IllegalAccessException ex) {
            Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "STORAGE: Not able to retrieve the driver to delete the file", ex);
            return false;
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "STORAGE: Not able to retrieve the driver to delete the file", ex);
            return false;
        } catch (InvocationTargetException ex) {
            Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "STORAGE: Not able to retrieve the driver to delete the file", ex);
            return false;
        } catch (IOException ex) {
            if (ex.getClass() == SardineException.class && ex.toString().contains("404")) {
                Logger.getLogger(StorageManager.class.getName()).log(Level.WARNING, "Slice already deleted", ex);
                return true;
            } else {
                Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "Error trying to delete file", ex);
                return false;
        }
    }
}
}
