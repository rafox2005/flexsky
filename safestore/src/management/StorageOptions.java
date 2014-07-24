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

import dispersal.IDecoderIDA;
import dispersal.IEncoderIDA;
import java.util.ArrayList;

/**
 *
 * @author rafox
 */
class StorageOptions
{    
    private int totalParts;
    private int reqParts;
    private ArrayList accounts;
    private ArrayList filePipeline;
    //TODO private ArrayList slicePipeline;
    private IEncoderIDA encoder;
    private IDecoderIDA decoder;

    public StorageOptions(int totalParts, int reqParts, ArrayList accounts, ArrayList filePipeline, IEncoderIDA encoder, IDecoderIDA decoder)
    {
        this.totalParts = totalParts;
        this.reqParts = reqParts;
        this.accounts = accounts;
        this.encoder = encoder;
        this.decoder = decoder;
    }
    
    
    
}
