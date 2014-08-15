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

import java.util.HashMap;

/**
 *
 * @author rafox
 */
public class StoreSafeAccount
{
    
    private String name;
    private String type;
    private String path;
    private HashMap<String,String> additionalParameters; 

    public StoreSafeAccount(String name, String type, String path)
    {
        this.name = name;
        this.type = type;
        this.path = path;
        this.additionalParameters = null;
    }
    
    public StoreSafeAccount(String name, String type, String path, HashMap<String,String> additionalParameters)
    {
        this.name = name;
        this.type = type;
        this.path = path;
        this.additionalParameters = additionalParameters;
    }
    
    @Override
    public String toString() {
        return this.name + " (" + this.type + ")";
    }

    /**
     * Get the value of path
     *
     * @return the value of path
     */
    public String getPath()
    {
        return path;
    }

    /**
     * Set the value of path
     *
     * @param path new value of path
     */
    public void setPath(String path)
    {
        this.path = path;
    }


    /**
     * Get the value of type
     *
     * @return the value of type
     */
    public String getType()
    {
        return type;
    }

    /**
     * Set the value of type
     *
     * @param type new value of type
     */
    public void setType(String type)
    {
        this.type = type;
    }


    /**
     * Get the value of name
     *
     * @return the value of name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Set the value of name
     *
     * @param name new value of name
     */
    public void setName(String name)
    {
        this.name = name;
    }

    public HashMap<String, String> getAdditionalParameters() {
        return additionalParameters;
    }

    public void setAdditionalParameters(HashMap<String, String> additionalParameters) {
        this.additionalParameters = additionalParameters;
    }
    
    

}
