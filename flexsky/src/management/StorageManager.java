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

import com.github.sardine.impl.SardineException;
import data.StorageOptions;
import data.DataAccount;
import data.DataFile;
import data.DataSlice;
import dispersal.IDecoderIDA;
import dispersal.IEncoderIDA;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import pipeline.IPipeProcess;
import storage.IDriver;
import storage.driver.DiskDriver;
import util.FlexSkyLogger;
import util.monitor.RTInputStream;
import util.monitor.RTOutputStream;

/**
 *
 * @author rafox
 */
class StorageManager {

    public boolean storeFile(File file, DataFile ssf, ArrayList<DataSlice> slices, ArrayList<DataAccount> listAccounts) {
        IEncoderIDA ida = null;
        StorageOptions options = ssf.getOptions();
        RTInputStream fileInputStream = null;
        try {
            fileInputStream = new RTInputStream(new FileInputStream(file));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "STORAGE: not able to find the file to store", ex);
            return false;
        }

        ArrayList<OutputStream> outputStreams = new ArrayList<>();
        ArrayList<RTOutputStream> outputStreamsOriginal = new ArrayList<>();

        //Get the upload streams for each driver
        for (int i = 0; i < slices.size(); i++) {
            IDriver sliceDriver = null;
            DataSlice currentSlice = slices.get(i);
            DataAccount currentAccount = listAccounts.get(i);

            try {
                sliceDriver = (IDriver) Class.forName(currentAccount.getType()).getDeclaredConstructor(String.class, String.class).newInstance(currentAccount.getName(), currentAccount.getPath());
                OutputStream sliceOutputStream = sliceDriver.getSliceUploadStream(currentSlice, currentAccount.getAdditionalParameters());

                //Create the pipes to run the upload process in parallel
                PipedInputStream pipedInputStreamSlice = new PipedInputStream(StoreSafeManager.bufferSize);
                PipedOutputStream pipedOutputStreamSlice = new PipedOutputStream(pipedInputStreamSlice);

                new Thread(
                        new Runnable() {
                            public void run() {
                                try {
                                    byte[] buffer = new byte[StoreSafeManager.bufferSize];
                                    int len = 0;
                                    while ((len = pipedInputStreamSlice.read(buffer)) != -1) {
                                        sliceOutputStream.write(buffer, 0, len);
                                    }

                                    sliceOutputStream.close();

                                } catch (IOException ex) {
                                    Logger.getLogger(DiskDriver.class.getName()).log(Level.SEVERE, "STORAGE: not able to create the PipedInputStream for the slice", ex);
                                }

                            }
                        }).start();

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
                            Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "STORAGE: not able to create the PipedInputStream for the pipes", ex);
                        }

                        sliceListPipesIn.add(in);
                        sliceListPipesOut.add(out);
                    }

                    //Add the last OutputStream for the driver
                    RTOutputStream driverOutput = new RTOutputStream(pipedOutputStreamSlice);
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
                                        long start = StoreSafeManager.tmx.getCurrentThreadCpuTime();
                                        pipe.process(inT, outT, options.additionalParameters);
                                        double time = (StoreSafeManager.tmx.getCurrentThreadCpuTime() - start) / 1000000;

                                        try {
                                            inT.close();
                                            outT.flush();
                                            outT.close();
                                            //Finish and log everything
                                            FlexSkyLogger.addSlicePipeLog(ssf, currentSlice, pipe.getClass().getName(), "UP", time, outT.totalKbytes() / (time/1000), outT.totalKbytes());

                                        } catch (IOException ex) {
                                            Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "Pipe problem", ex);
                                        }
                                    }
                                }
                        ).start();
                    }

                    //Add first OutputStream to list
                    outputStreams.add(new RTOutputStream(sliceListPipesOut.get(0)));
                } //If no slice pipeline
                else {
                    RTOutputStream driverOutput = new RTOutputStream(pipedOutputStreamSlice);
                    outputStreams.add(driverOutput);
                    outputStreamsOriginal.add(driverOutput);
                }

            } catch (NoSuchMethodException | SecurityException ex) {
                Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "STORAGE: not able to find the constructor for the driver to store", ex);
                return false;
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "STORAGE: not able to find the driver class to store", ex);
                return false;
            } catch (IOException ex) {
                Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "STORAGE: not able to create the communication channel", ex);
                return false;
            }

        }

        if (options.filePipeline.size() > 0) {

            //Implement Pipeline using Java Pipes
            ArrayList<InputStream> listPipesIn = new ArrayList<>();
            ArrayList<OutputStream> listPipesOut = new ArrayList<>();

            listPipesIn.add(fileInputStream);

            //Creating and connecting the pipes
            for (int i = 0; i < options.filePipeline.size(); i++) {
                PipedOutputStream out = new PipedOutputStream();
                PipedInputStream in = null;
                try {
                    in = new PipedInputStream(out, StoreSafeManager.bufferSize);
                } catch (IOException ex) {
                    Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "STORAGE: not able to create the PipedInputStream", ex);
                    return false;
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
                                long start = StoreSafeManager.tmx.getCurrentThreadCpuTime();
                                pipe.process(inT, outT, options.additionalParameters);
                                double time = (StoreSafeManager.tmx.getCurrentThreadCpuTime() - start) / 1000000;

                                try {
                                    inT.close();
                                    outT.flush();
                                    outT.close();
                                    //Finish and log everything                                     
                                    FlexSkyLogger.addFilePipeLog(ssf, pipe.getClass().getName(), "UP", time, outT.totalKbytes() / (time / 1000), outT.totalKbytes());
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
            return false;
            
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "STORAGE: not able to retrieve the IDA to store", ex);
            return false;
        } catch (SecurityException ex) {
            Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "STORAGE: not able to retrieve the IDA to store", ex);
            return false;
        } catch (InstantiationException ex) {
            Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "STORAGE: not able to retrieve the IDA to store", ex);
            return false;
        } catch (IllegalAccessException ex) {
            Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "STORAGE: not able to retrieve the IDA to store", ex);
            return false;
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "STORAGE: not able to retrieve the IDA to store", ex);
            return false;
        } catch (InvocationTargetException ex) {
            Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "STORAGE: not able to retrieve the IDA to store", ex);
            return false;
        }

        long start = StoreSafeManager.tmx.getCurrentThreadCpuTime();
        long sliceSize = ida.encode();
        double time = (StoreSafeManager.tmx.getCurrentThreadCpuTime() - start) / 1000000;

        //Finish and log everything
        FlexSkyLogger.addIDALog(ssf, "UP", time, fileInputStream.totalKBytes() / (time / 1000), fileInputStream.totalKBytes() / 1000);

        //Update important values
        ssf.setHash(ida.getFileHash());
        String[] partsHash = ida.getPartsHash();
        for (int i = 0; i < slices.size(); i++) {
            DataSlice currentSlice = slices.get(i);
            currentSlice.setPath(currentSlice.getFile() + "-" + currentSlice.getPartIndex());
            currentSlice.setHash(partsHash[i]);
            currentSlice.setSize(sliceSize);

        }

        //Log the slices outputs
        while (StoreSafeManager.tmx.getThreadCount() - StoreSafeManager.tmx.getDaemonThreadCount() > 3);
        for (int i = 0; i < outputStreamsOriginal.size(); i++) {
            RTOutputStream os = outputStreamsOriginal.get(i);
            if (os.totalTime() > 0) {
                FlexSkyLogger.addSliceLog(ssf, slices.get(i), "UP", os.totalTime(), os.averageRate(), os.totalKbytes());
            }
        }
        
        

        return true;

    }

    public boolean downloadFile(File file, DataFile ssf, ArrayList<DataSlice> slices, ArrayList<DataAccount> listAccounts) {
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
            DataSlice currentSlice = slices.get(i);
            DataAccount currentAccount = listAccounts.get(i);

            try {
                sliceDriver = (IDriver) Class.forName(currentAccount.getType()).getDeclaredConstructor(String.class, String.class).newInstance(currentAccount.getName(), currentAccount.getPath());
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "STORAGE: Not able to find the driver required", ex);
                return false;
            } catch (NoSuchMethodException ex) {
                Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "STORAGE: Not able to find the driver required", ex);
                return false;
            } catch (SecurityException ex) {
                Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "STORAGE: Not able to find the driver required", ex);
                return false;
            } catch (InstantiationException ex) {
                Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "STORAGE: Not able to find the driver required", ex);
                return false;
            } catch (IllegalAccessException ex) {
                Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "STORAGE: Not able to find the driver required", ex);
                return false;
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "STORAGE: Not able to find the driver required", ex);
                return false;
            } catch (InvocationTargetException ex) {
                Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "STORAGE: Not able to find the driver required", ex);
                return false;
            }

            //Add inputstream to list
            //Try to get the inputstreams
            InputStream sliceInputStream = null;
            try {
                if (sliceDriver != null) {
                    sliceInputStream = sliceDriver.getSliceDownloadStream(currentSlice, currentAccount.getAdditionalParameters());
                }

            } catch (IOException ex) {
                Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "Not able to retrieve the slice from the driver " + currentAccount.getName(), ex);
                return false;
            }

            if (sliceInputStream != null && inputStreams.size() < ssf.getReqParts()) {

                try {
                    //Create the pipes to run the upload process in parallel
                    final InputStream sliceInputStreamThread = sliceInputStream;
                    PipedInputStream pipedInputStreamSlice = new PipedInputStream(StoreSafeManager.bufferSize);
                    PipedOutputStream pipedOutputStreamSlice = new PipedOutputStream(pipedInputStreamSlice);

                    new Thread(
                            new Runnable() {
                                public void run() {
                                    try {
                                        byte[] buffer = new byte[StoreSafeManager.bufferSize];
                                        int len = 0;
                                        while ((len = sliceInputStreamThread.read(buffer)) != -1) {
                                            pipedOutputStreamSlice.write(buffer, 0, len);
                                        }

                                        pipedOutputStreamSlice.close();

                                    } catch (IOException ex) {                                        
                                        Logger.getLogger(DiskDriver.class.getName()).log(Level.SEVERE, "STORAGE: not able to create the PipedInputStream for the slice - " + currentAccount.getPath() , ex);
                                        
                                        //Buffer problem - Increase buffer and restart
                                        if (ex.toString().contains("reset")) {
                                            StoreSafeManager.bufferSize = slices.size()+1;

                                        }

                                    }                                    

                                }
                            }).start();

                    RTInputStream sliceRTInputStream = new RTInputStream(pipedInputStreamSlice);
                    inputStreamsOriginal.add(sliceRTInputStream);

                    if (options.slicePipeline.size() > 0) {

                        //Implement Pipeline using Java Pipes
                        ArrayList<InputStream> sliceListPipesIn = new ArrayList<>();
                        ArrayList<OutputStream> sliceListPipesOut = new ArrayList<>();

                        //Add the first InputStream from driver
                        sliceListPipesIn.add(sliceRTInputStream);

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
                                            long start = StoreSafeManager.tmx.getCurrentThreadCpuTime();
                                            pipe.reverseProcess(inT, outT, options.additionalParameters);
                                            double time = (StoreSafeManager.tmx.getCurrentThreadCpuTime() - start) / 1000000;
                                            try {
                                                inT.close();
                                                outT.flush();
                                                outT.close();

                                                //Finish and log everything
                                                FlexSkyLogger.addSlicePipeLog(ssf, currentSlice, pipe.getClass().getName(), "DOWN", time, outT.totalKbytes() / (time / 1000), outT.totalKbytes());

                                            } catch (IOException ex) {
                                                Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "Pipe problem", ex);
                                            }
                                        }
                                    }
                            ).start();

                        }

                        //Get Last InputStream
                        sliceRTInputStream = new RTInputStream(sliceListPipesIn.get(sliceListPipesIn.size() - 1));

                    }   //Add the inputStream to the list of InputStreams
                    inputStreams.add(sliceRTInputStream);
                } catch (IOException ex) {
                    Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, null, ex);
                    return false;
                }
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
                    return false;
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
                return false;
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

                                    long start = StoreSafeManager.tmx.getCurrentThreadCpuTime();
                                    pipe.reverseProcess(inT, outT, options.additionalParameters);
                                    double time = (StoreSafeManager.tmx.getCurrentThreadCpuTime() - start) / 1000000;

                                    inT.close();
                                    outT.flush();
                                    outT.close();
                                    //Finish and log everything
                                    FlexSkyLogger.addFilePipeLog(ssf, pipe.getClass().getName(), "DOWN", time, outT.totalKbytes() / (time / 1000), outT.totalKbytes());

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
            return false;
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "Storage: Not able to retrieve the IDA", ex);
            return false;
        } catch (SecurityException ex) {
            Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "Storage: Not able to retrieve the IDA", ex);
            return false;
        } catch (InstantiationException ex) {
            Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "Storage: Not able to retrieve the IDA", ex);
            return false;
        } catch (IllegalAccessException ex) {
            Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "Storage: Not able to retrieve the IDA", ex);
            return false;
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "Storage: Not able to retrieve the IDA", ex);
            return false;
        } catch (InvocationTargetException ex) {
            Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, "Storage: Not able to retrieve the IDA", ex);
            return false;
        }

        long start = StoreSafeManager.tmx.getCurrentThreadCpuTime();

        //Decode
        ida.decode();
        double time = (StoreSafeManager.tmx.getCurrentThreadCpuTime() - start) / 1000000;

        //Finish and log everything
        FlexSkyLogger.addIDALog(ssf, "DOWN", time, os.totalKbytes() / (time / 1000), os.totalKbytes() / 1000);

//        String[] teste = ida.getPartsHash();
//
//        for (int i = 0; i < teste.length; i++)
//        {
//            Logger.getLogger(StorageManager.class.getName()).log(Level.INFO, "RETRIEVAL: " + "P " + i + "H " + teste[i]);
//        }
        while (StoreSafeManager.tmx.getThreadCount() - StoreSafeManager.tmx.getDaemonThreadCount() > 3);
        for (int i = 0; i < inputStreamsOriginal.size(); i++) {
            RTInputStream is = inputStreamsOriginal.get(i);
            if (is.totalTime() > 0) {
                FlexSkyLogger.addSliceLog(ssf, slices.get(i), "DOWN", is.totalTime(), is.averageRate(), is.totalKBytes());
            }
        }

        //Check if file hash match
        if (ssf.getHash().equals(ida.getFileHash())) {
            return true;
        } else {
            try {
                file.renameTo(new File(file.getAbsolutePath() + "-failed"));       
                throw new IOException("ERROR: recovered file hash differs from the original");
            } catch (IOException ex) {
                Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, null, ex);

                return false;
            }
        }

    }

    public boolean deleteSlice(DataFile file, DataSlice slice, DataAccount currentAccount) {
        try {
            long start = System.currentTimeMillis();
            IDriver sliceDriver = (IDriver) Class.forName(currentAccount.getType()).getDeclaredConstructor(String.class, String.class).newInstance(currentAccount.getName(), currentAccount.getPath());
            boolean result = sliceDriver.deleteSlice(slice, currentAccount.getAdditionalParameters());

            //Finish and log everything
            double time = System.currentTimeMillis() - start;
            double rate = (slice.getSize() / 1000.0) / (time / 1000.0);
            FlexSkyLogger.addSliceLog(file, slice, "DEL", time, rate, slice.getSize() / 1000.0);

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
