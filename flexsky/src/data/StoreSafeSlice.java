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

/**
 *
 * @author rafox
 */
public class StoreSafeSlice
{
    
    private int file;
    private String account;
    private String path;
    private long size;
    private String hash;
    private int partIndex;

    /**
     *
     * @param file the value of file
     * @param partIndex the value of partIndex
     * @param account the value of account
     * @param path the value of path (AUTO)
     * @param size the value of size
     * @param hash the value of hash
     */
    public StoreSafeSlice(int file, int partIndex, String account, String path, long size, String hash)
    {
        this.file = file;
        this.account = account;        
        this.size = size;
        this.hash = hash;
        this.partIndex = partIndex;
        this.path = path;
    }

    /**
     * Get the value of file
     *
     * @return the value of file
     */
    public int getFile()
    {
        return file;
    }

    /**
     * Get the value of account
     *
     * @return the value of account
     */
    public String getAccount()
    {
        return account;
    }

    /**
     * Set the value of account
     *
     * @param account new value of account
     */
    public void setAccount(String account)
    {
        this.account = account;
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
     * Set the value of file
     *
     * @param file new value of file
     */
    public void setFile(int file)
    {
        this.file = file;
    }

    /**
     * Get the value of partIndex
     *
     * @return the value of partIndex
     */
    public int getPartIndex()
    {
        return partIndex;
    }

    /**
     * Set the value of partIndex
     *
     * @param partIndex new value of partIndex
     */
    public void setPartIndex(int partIndex)
    {
        this.partIndex = partIndex;
    }

}
