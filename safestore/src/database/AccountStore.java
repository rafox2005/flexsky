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

import data.StoreSafeAccount;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rafox
 */
public class AccountStore
{

    private final Connection conn;

    public AccountStore(Connection conn)
    {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(AccountStore.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.conn = conn;
    }

    public boolean insertAccount(StoreSafeAccount account)
    {
        try {
            PreparedStatement prepStatement = conn.prepareStatement("INSERT INTO accounts (name, type, path) VALUES(?, ?, ?)");
            prepStatement.setString(1, account.getName());
            prepStatement.setInt(2, account.getType());
            prepStatement.setString(3, account.getPath());
            prepStatement.executeUpdate();
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(AccountStore.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
    public boolean deleteAccountByName(String name)
    {
        try {            
            PreparedStatement prepStatement = conn.prepareStatement("DELETE FROM accounts WHERE name=?");
            prepStatement.setString(1, name);
            prepStatement.executeUpdate();
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(AccountStore.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
    public boolean deleteAccountByID(int id)
    {
        try {
            PreparedStatement prepStatement = conn.prepareStatement("DELETE FROM accounts WHERE id=?");
            prepStatement.setInt(1, id);
            prepStatement.executeUpdate();
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(AccountStore.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public ArrayList<StoreSafeAccount> getAccounts()
    {
        ResultSet rs;
        try {
            ArrayList<StoreSafeAccount> list = new ArrayList<>();
            PreparedStatement prepStatement = conn.prepareStatement("SELECT * FROM accounts");
            rs = prepStatement.executeQuery();
            while (rs.next()) {
                list.add(new StoreSafeAccount(rs.getString("name"), rs.getInt("type"), rs.getString("path")));
            }
            return list;
        } catch (SQLException ex) {
            Logger.getLogger(AccountStore.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }

    }    

}
