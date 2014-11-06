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

import data.DataAccount;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.commons.lang3.SerializationUtils;

/**
 *
 * @author rafox
 */
public class AccountStore {

    private final Connection conn;

    public AccountStore(Connection conn) throws ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        this.conn = conn;
    }

    public boolean insertAccount(DataAccount account) throws SQLException {
        //Get File Pipe Info            
        byte[] parametersBlob = SerializationUtils.serialize(account.getAdditionalParameters());
        byte[] selectionParametersBlob = SerializationUtils.serialize(account.getSelectionParameters());
        try (PreparedStatement prepStatement = conn.prepareStatement("INSERT INTO accounts (name, type, path, parameters, selection_parameters) VALUES(?, ?, ?, ?, ?)")) {
            prepStatement.setString(1, account.getName());
            prepStatement.setString(2, account.getType());
            prepStatement.setString(3, account.getPath());
            prepStatement.setBytes(4, parametersBlob);
            prepStatement.setBytes(5, selectionParametersBlob);
            prepStatement.executeUpdate();
        }
        return true;
    }

    public boolean deleteAccountByName(String name) throws SQLException {
        try (PreparedStatement prepStatement = conn.prepareStatement("DELETE FROM accounts WHERE name=?")) {
            prepStatement.setString(1, name);
            prepStatement.executeUpdate();
        }
        return true;
    }

    public boolean deleteAccountByID(int id) throws SQLException {
        try (PreparedStatement prepStatement = conn.prepareStatement("DELETE FROM accounts WHERE id=?")) {
            prepStatement.setInt(1, id);
            prepStatement.executeUpdate();
        }
        return true;
    }

    public ArrayList<DataAccount> getAccounts() throws SQLException {
        ResultSet rs;
        ArrayList<DataAccount> list = new ArrayList<>();
        try (PreparedStatement prepStatement = conn.prepareStatement("SELECT * FROM accounts")) {
            rs = prepStatement.executeQuery();
            while (rs.next()) {
                //Get add parameters
                HashMap additionalParameters = null;
                byte[] param = rs.getBytes("parameters");
                if (param != null) {
                    additionalParameters = SerializationUtils.deserialize(param);
                }
                //Get Selection Parameters
                 HashMap selectionParameters = null;
                byte[] select_param = rs.getBytes("selection_parameters");
                if (select_param != null) {
                    selectionParameters = SerializationUtils.deserialize(select_param);
                }

                list.add(new DataAccount(rs.getString("name"), rs.getString("type"), rs.getString("path"), additionalParameters, selectionParameters));
            }
        }
        return list;

    }

}
