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
package database;

import data.StoreSafeFile;
import data.StoreSafeSlice;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rafox
 */
public class SliceStore
{

    private final Connection conn;
    private String name;

    public SliceStore(String name, Connection conn)
    {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(AccountStore.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.conn = conn;
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public boolean insertSlice(StoreSafeSlice slice)
    {
        try {
            PreparedStatement prepStatement
                    = this.conn.prepareStatement("INSERT INTO slices(file, part_index, account, path, size, hash)"
                            + " VALUES(?, ?, ?, ?, ?, ?)");
            prepStatement.setInt(1, slice.getFile());
            prepStatement.setInt(2, slice.getPartIndex());
            prepStatement.setString(3, slice.getAccount());
            prepStatement.setString(4, slice.getPath());
            prepStatement.setLong(5, slice.getSize());
            prepStatement.setString(6, slice.getHash());
            prepStatement.executeUpdate();
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(SliceStore.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public ArrayList<StoreSafeSlice> getSlicesFromFile(StoreSafeFile file)
    {
        try {
            ResultSet rs;
            ArrayList<StoreSafeSlice> list = new ArrayList<>();
            PreparedStatement prepStatement = conn.prepareStatement("SELECT * FROM slices WHERE file=?");
            prepStatement.setInt(1, file.getId());
            rs = prepStatement.executeQuery();
            while (rs.next()) {
                list.add(new StoreSafeSlice(rs.getInt("file"),
                        rs.getInt("part_index"),
                        rs.getString("account"),
                        rs.getString("path"),
                        rs.getLong("size"),
                        rs.getString("hash")));
            }
            return list;
        } catch (SQLException ex) {
            Logger.getLogger(SliceStore.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
     public ArrayList<StoreSafeSlice> getSlicesFromFile(int fileId)
    {
        try {
            ResultSet rs;
            ArrayList<StoreSafeSlice> list = new ArrayList<>();
            PreparedStatement prepStatement = conn.prepareStatement("SELECT * FROM slices WHERE file=?");
            prepStatement.setInt(1, fileId);
            rs = prepStatement.executeQuery();
            while (rs.next()) {
                list.add(new StoreSafeSlice(rs.getInt("file"),
                        rs.getInt("part_index"),
                        rs.getString("account"),
                        rs.getString("path"),
                        rs.getLong("size"),
                        rs.getString("hash")));
            }
            return list;
        } catch (SQLException ex) {
            Logger.getLogger(SliceStore.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public boolean deleteSlice(StoreSafeSlice slice)
    {
        try {
            PreparedStatement prepStatement = conn.prepareStatement("DELETE FROM slices WHERE file=? AND part_index=?");
            prepStatement.setInt(1, slice.getFile());
            prepStatement.setInt(2, slice.getPartIndex());
            prepStatement.executeUpdate();
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(AccountStore.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
}
