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

import java.sql.Date;
import management.StorageOptions;

/**
 *
 * @author rafox
 */
public class StoreSafeFile
{

    private Date lastModified;
    private long size;
    private String dispersalMethod;
    private int totalParts;
    private Date lastAccessed;
    private int revision;
    private String hash;
    private int reqParts;
    private String type;
    private String name;
    private int id;
    private management.StorageOptions options;

    /**
     *
     * @param name the value of name
     * @param size the value of size
     * @param type the value of type
     * @param dispersalMethod the value of dispersalMethod
     * @param totalParts the value of totalParts
     * @param reqParts the value of reqParts
     * @param hash the value of hash
     * @param lastAccessed the value of lastAccessed
     * @param lastModified the value of lastModified
     * @param revision the value of revision
     */
    public StoreSafeFile(String name, long size, String type, String dispersalMethod, int totalParts, int reqParts, String hash, Date lastAccessed, Date lastModified, int revision)
    {
        this.lastModified = lastModified;
        this.size = size;
        this.dispersalMethod = dispersalMethod;
        this.totalParts = totalParts;
        this.lastAccessed = lastAccessed;
        this.revision = revision;
        this.hash = hash;
        this.reqParts = reqParts;
        this.type = type;
        this.name = name;
    }
    
    /**
     *
     * @param name the value of name
     * @param size the value of size
     * @param type the value of type
     * @param dispersalMethod the value of dispersalMethod
     * @param totalParts the value of totalParts
     * @param reqParts the value of reqParts
     * @param hash the value of hash
     * @param lastAccessed the value of lastAccessed
     * @param lastModified the value of lastModified
     * @param revision the value of revision
     */
    public StoreSafeFile(int id, String name, long size, String type, String dispersalMethod, int totalParts, int reqParts, String hash, Date lastAccessed, Date lastModified, int revision, StorageOptions options)
    {
        this.id = id;
        this.lastModified = lastModified;
        this.size = size;
        this.dispersalMethod = dispersalMethod;
        this.totalParts = totalParts;
        this.lastAccessed = lastAccessed;
        this.revision = revision;
        this.hash = hash;
        this.reqParts = reqParts;
        this.type = type;
        this.name = name;
        this.options = options;
    }

    public StoreSafeFile(String name, int revision) {
        this.revision = revision;
        this.name = name;
    }
    
    
    /**
     * Get the value of id
     *
     * @return the value of id
     */
    public int getId()
    {
        return id;
    }


    /**
     * Set the value of id
     *
     * @param id new value of id
     */
    public void setId(int id)
    {
        this.id = id;
    }

    /**
     * Get the value of lastModified
     *
     * @return the value of lastModified
     */
    public Date getLastModified()
    {
        return lastModified;
    }

    /**
     * Set the value of lastModified
     *
     * @param lastModified new value of lastModified
     */
    public void setLastModified(Date lastModified)
    {
        this.lastModified = lastModified;
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
     * Get the value of size
     *
     * @return the value of size
     */
    public long getSize()
    {
        return size;
    }

    /**
     * Set the value of size
     *
     * @param size new value of size
     */
    public void setSize(long size)
    {
        this.size = size;
    }

    /**
     * Get the value of dispersalMethod
     *
     * @return the value of dispersalMethod
     */
    public String getDispersalMethod()
    {
        return dispersalMethod;
    }

    /**
     * Set the value of dispersalMethod
     *
     * @param dispersalMethod new value of dispersalMethod
     */
    public void setDispersalMethod(String dispersalMethod)
    {
        this.dispersalMethod = dispersalMethod;
    }

    /**
     * Get the value of totalParts
     *
     * @return the value of totalParts
     */
    public int getTotalParts()
    {
        return totalParts;
    }

    /**
     * Get the value of lastAccessed
     *
     * @return the value of lastAccessed
     */
    public Date getLastAccessed()
    {
        return lastAccessed;
    }

    /**
     * Set the value of lastAccessed
     *
     * @param lastAccessed new value of lastAccessed
     */
    public void setLastAccessed(Date lastAccessed)
    {
        this.lastAccessed = lastAccessed;
    }

    /**
     * Get the value of revision
     *
     * @return the value of revision
     */
    public int getRevision()
    {
        return revision;
    }

    /**
     * Set the value of revision
     *
     * @param revision new value of revision
     */
    public void setRevision(int revision)
    {
        this.revision = revision;
    }

    /**
     * Get the value of hash
     *
     * @return the value of hash
     */
    public String getHash()
    {
        return hash;
    }

    /**
     * Set the value of hash
     *
     * @param hash new value of hash
     */
    public void setHash(String hash)
    {
        this.hash = hash;
    }

    /**
     * Get the value of reqParts
     *
     * @return the value of reqParts
     */
    public int getReqParts()
    {
        return reqParts;
    }

    /**
     * Set the value of reqParts
     *
     * @param reqParts new value of reqParts
     */
    public void setReqParts(int reqParts)
    {
        this.reqParts = reqParts;
    }

    /**
     * Set the value of totalParts
     *
     * @param totalParts new value of totalParts
     */
    public void setTotalParts(int totalParts)
    {
        this.totalParts = totalParts;
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

    public StorageOptions getOptions() {
        return options;
    }

    public void setOptions(StorageOptions options) {
        this.options = options;
    }

}
