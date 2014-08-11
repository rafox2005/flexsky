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

import dispersal.IDecoderIDA;
import dispersal.IEncoderIDA;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import pipeline.IPipeProcess;

/**
 *
 * @author rafox
 */
public class StorageOptions implements Serializable
{    
    public HashMap<String,String> additionalParameters; 
    public ArrayList<IPipeProcess> filePipeline;
    public ArrayList<IPipeProcess> slicePipeline;
    
 
    public StorageOptions(ArrayList filePipeline, ArrayList slicePipeline, HashMap additionalParameters)
    {
        if (filePipeline == null) {
            filePipeline = new ArrayList();
        }
        if (slicePipeline == null) {
            slicePipeline = new ArrayList();
        }
        
        if (additionalParameters == null) {
            additionalParameters = new HashMap();
        }
        
        this.filePipeline = filePipeline;
        this.slicePipeline = slicePipeline;
        this.additionalParameters = additionalParameters;        
    }

    public StorageOptions() {    
    }
    
    
    
    
    
}
