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
import dispersal.IEncoderIDA;
import dispersal.rabin.EncoderRabinIDA;
import dispersal.reedsolomon.EncoderRS;
import driver.DiskDriver;
import driver.IDriver;
import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;

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
        long sliceSize = ida.encode();

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

}
