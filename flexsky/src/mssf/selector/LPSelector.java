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
package mssf.selector;

import data.DataAccount;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.SysexMessage;
import util.Utils;

/**
 *
 * @author rlibardi
 */
public class LPSelector extends ISelector{

    @Override
    public DispersalSelection select(ArrayList<DataAccount> providers, ArrayList<PipeModule> modules, HashMap<String, Integer> userConstraints, HashMap<String, String> parameters)
    {
        try
        {
            //Create temp files and open file
            File fileTempData = File.createTempFile("flexsky-opt-data", ".tmp");
            File fileTempSolution = File.createTempFile("flexsky-opt-solution", ".tmp");
            File fileModel = new File(parameters.get("model_path"));
            
            
            //WriteDataFile            
            writeDataFile(fileTempData, providers, modules, userConstraints);
            
            //Generate execution string
            String command = "glpsol -m " + 
                    fileModel.getAbsolutePath() + 
                    " -d " + fileTempData.getAbsolutePath() + 
                    " -y " + fileTempSolution.getAbsolutePath();
            
            
            Utils.executeCommand(command);
            
            //Read solution file and create dispersal selection object   TODO         
            FileReader reader = new FileReader(fileTempSolution);
            
            reader.
            
            
            
            
            
            
        } catch (IOException ex)
        {
            Logger.getLogger(LPSelector.class.getName()).log(Level.SEVERE, null, ex);
        }
    
    }
            
    private void writeDataFile(File dataFile, ArrayList<DataAccount> providers, ArrayList<PipeModule> modules, HashMap<String, Integer> userConstraints)
    {
        
        
        try 
        {
            //Create FileWriter
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(dataFile)));
            
            //Write header
            out.println("data;");
            
            //Write IDAs
            out.println("param: Ida: IDA_SEC IDA_PERF IDA_STO :=");
            for (PipeModule module : modules)
            {
                if (module.getType().equalsIgnoreCase("ida"))
                {
                    out.printf("%s %s %s %s\n",
                            module.getName(),
                            module.getSelectionParameters().get("SEC"),
                            module.getSelectionParameters().get("PERF"),
                            module.getSelectionParameters().get("STO"));                   
                }
            }
            out.println(";");
            out.println();
            
            //Write ENCs
            out.println("param: Enc: ENC_SEC ENC_PERF ENC_STO :=");
            for (PipeModule module : modules)
            {
                if (module.getType().equalsIgnoreCase("enc"))
                {
                    out.printf("%s %s %s %s\n",
                            module.getName(),
                            module.getSelectionParameters().get("SEC"),
                            module.getSelectionParameters().get("PERF"),
                            module.getSelectionParameters().get("STO"));                   
                }
            }
            out.println(";");
            out.println();
            
             //Write COMPs
            out.println("param: Comp: COMP_SEC COMP_PERF COMP_STO :=");
            for (PipeModule module : modules)
            {
                if (module.getType().equalsIgnoreCase("comp"))
                {
                    out.printf("%s %s %s %s\n",
                            module.getName(),
                            module.getSelectionParameters().get("SEC"),
                            module.getSelectionParameters().get("PERF"),
                            module.getSelectionParameters().get("STO"));                   
                }
            }
            out.println(";");
            out.println();
            
            //Write Providers
            out.println("param: Provider: PROV_SEC PROV_PERF PROV_DUR PROV_AVAIL PROV_STORAGECOST PROV_BWCOST:=");
            for (DataAccount provider : providers)
            {
                    out.printf("%s %s %s %s %s %s\n",
                            provider.getName(),
                            provider.getSelectionParameters().get("PROV_SEC"),
                            provider.getSelectionParameters().get("PROV_PERF"),
                            provider.getSelectionParameters().get("PROV_DUR"),
                            provider.getSelectionParameters().get("PROV_AVAIL"),
                            provider.getSelectionParameters().get("PROV_STORAGECOST"),
                            provider.getSelectionParameters().get("PROV_BWCOST"));                   
            }
            out.println(";");
            out.println();
            
            //Write User Parameters and Limits
            for (Map.Entry param : userConstraints.entrySet())
            {
                out.printf("param %s:= %d;\n", param.getKey(), param.getValue());
            }
            out.println(";");
            out.println();
            
            //End the file
            out.println("end;");
            
            out.close();
            
            
        } catch (IOException ex)
        {
            Logger.getLogger(LPSelector.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }    
        
    
    
}
