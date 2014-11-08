/*
 * The MIT License
 *
 * Copyright 2014 rlibardi.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import mssf.selector.Module;
import org.apache.commons.lang3.SerializationUtils;

/**
 *
 * @author rlibardi
 */
public class ModuleStore {
    private String name;
    private final Connection conn;

    public ModuleStore(Connection conn) throws ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        this.conn = conn;
    }

    public ModuleStore(String testFS, Connection conn) throws ClassNotFoundException {
        this(conn);
        this.name = testFS;        
    }
    
    public boolean insertModule(Module module) throws SQLException {
        //Get File Pipe Info            
        byte[] parametersBlob = SerializationUtils.serialize(module.getParameters());
        byte[] selectionParametersBlob = SerializationUtils.serialize(module.getSelectionParameters());
        try (PreparedStatement prepStatement = conn.prepareStatement("INSERT INTO modules (name, type, pipe_name, parameters, selection_parameters) VALUES(?, ?, ?, ?, ?)")) {
            prepStatement.setString(1, module.getName());
            prepStatement.setString(2, module.getType());
            prepStatement.setString(3, module.getPipe_name());
            prepStatement.setBytes(4, parametersBlob);
            prepStatement.setBytes(5, selectionParametersBlob);
            prepStatement.executeUpdate();
        }
        return true;
    }
    
    public boolean deleteModuleByName(String name) throws SQLException {
        try (PreparedStatement prepStatement = conn.prepareStatement("DELETE FROM modules WHERE name=?")) {
            prepStatement.setString(1, name);
            prepStatement.executeUpdate();
        }
        return true;
    }
    
    public ArrayList<Module> getModules() throws SQLException {
        ResultSet rs;
        ArrayList<Module> list = new ArrayList<>();
        try (PreparedStatement prepStatement = conn.prepareStatement("SELECT * FROM modules")) {
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

                list.add(new Module(rs.getString("name"), rs.getString("type"), rs.getString("pipe_name"), additionalParameters, selectionParameters));
            }
        }
        return list;

    }

    
}
