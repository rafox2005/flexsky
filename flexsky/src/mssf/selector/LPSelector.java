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
import database.AccountStore;
import java.io.BufferedReader;
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
import util.FlexSkyLogger;
import util.Utils;

/**
 *
 * @author rlibardi
 */
public class LPSelector extends ISelector{

    @Override
    public DispersalSelection select(ArrayList<DataAccount> providers, ArrayList<Module> modules, HashMap<String, Number> userConstraints, HashMap<String, String> parameters)
    {
        try
        {
            //Create temp files and open file
            File fileTempData = File.createTempFile("flexsky-opt-data", ".tmp");
            File fileTempSolution = File.createTempFile("flexsky-opt-solution", ".tmp");
            File fileModel = new File(parameters.get("model_path"));
            
            //String pathToSolver = "C:\\Users\\Rafox\\Documents\\NetBeansProjects\\flexsky\\flexsky\\lib\\glpsol.exe";
            String pathToSolver = "glpsol";
            
            
            //WriteDataFile            
            writeDataFile(fileTempData, providers, modules, userConstraints);
            
            //Generate execution string
            String command = pathToSolver + " -m " + 
                    fileModel.getAbsolutePath() + 
                    " -d " + fileTempData.getAbsolutePath() + 
                    " -y " + fileTempSolution.getAbsolutePath();
            
            long start=System.currentTimeMillis();
            Utils.executeCommand(command);
            long time=System.currentTimeMillis() - start;
            
            
            //Read solution file and create dispersal selection object   TODO   
            
            DispersalMethod dm = new DispersalMethod();
            ArrayList<DataAccount> selectedProviders = new ArrayList();
            DispersalSelection ds = new DispersalSelection(dm, selectedProviders);
           
            BufferedReader br = new BufferedReader(new FileReader(fileTempSolution));
            String line;
            while ((line = br.readLine()) != null) {
            String[] result = line.split(";");
            
            //Get the providers
                if (result[0].equalsIgnoreCase("provider"))
                {                    
                    for (DataAccount provider : providers)
                    {
                        if (provider.getName().equalsIgnoreCase(result[1]))
                        {
                            selectedProviders.add(provider);
                        }
                    }
                }
                
                else if (result[0].equalsIgnoreCase("ida"))
                {                    
                    for (Module module : modules)
                    {
                        if (module.getName().equalsIgnoreCase(result[1]))
                        {
                            dm.getFile_pipeline().add(module);
                        }
                    }
                }
                
                else if (result[0].equalsIgnoreCase("comp"))
                {                    
                    for (Module module : modules)
                    {
                        if (module.getName().equalsIgnoreCase(result[1]))
                        {
                            dm.getFile_pipeline().add(module);
                        }
                    }
                }
                
                else if (result[0].equalsIgnoreCase("enc"))
                {                    
                    for (Module module : modules)
                    {
                        if (module.getName().equalsIgnoreCase(result[1]))
                        {
                            dm.getFile_pipeline().add(module);
                        }
                    }
                }
            
            }
            br.close(); 
            
            if (selectedProviders.isEmpty()) new Exception("GLPSOL Error in the selection.");
            
            //Calculate values
            
            int secParameter = 0;
            int perfParameter = 0;
            int availParameter = 0;
            int durParameter = 0;
            int stoCostParameter = 0;
            int bwCostParameter = 0;
            int stoParameter = 0;
            
            HashMap<String, Integer> resultSelectParameters = new HashMap<>();
            for (DataAccount selectedProvider : selectedProviders) {
                secParameter += selectedProvider.getSelectionParameters().get("PROV_SEC");
                perfParameter += (int) selectedProvider.getSelectionParameters().get("PROV_PERF");
                availParameter += selectedProvider.getSelectionParameters().get("PROV_AVAIL");
                durParameter += selectedProvider.getSelectionParameters().get("PROV_DUR");
                stoCostParameter += selectedProvider.getSelectionParameters().get("PROV_STORAGECOST");
                bwCostParameter += selectedProvider.getSelectionParameters().get("PROV_BWCOST");      
            }
            
            secParameter = secParameter/selectedProviders.size();
            perfParameter = perfParameter/selectedProviders.size();
            availParameter = availParameter/selectedProviders.size();
            durParameter = durParameter/selectedProviders.size();
            stoCostParameter = stoCostParameter/selectedProviders.size();
            bwCostParameter = bwCostParameter/selectedProviders.size();
            
            
            for (Module module : dm.getFile_pipeline()) {
                secParameter = (secParameter + module.getSelectionParameters().get("SEC"))/2;
                perfParameter = (perfParameter + module.getSelectionParameters().get("PERF"))/2;
                stoParameter = (stoParameter + module.getSelectionParameters().get("STO"))/2;
            }
            
            resultSelectParameters.put("SEC", secParameter);
            resultSelectParameters.put("PERF", perfParameter);
            resultSelectParameters.put("AVAIL", availParameter);
            resultSelectParameters.put("DUR", durParameter);
            resultSelectParameters.put("STORAGECOST", stoCostParameter);
            resultSelectParameters.put("BWCOST", bwCostParameter);
            resultSelectParameters.put("STO", stoParameter);
            
            dm.setSelectionParameters(resultSelectParameters);            
            
            
            FlexSkyLogger.addSelection(providers.size(), modules.size(), userConstraints, parameters, resultSelectParameters, time);
            
            
            return ds;
                                 
            
        } catch (IOException ex)
        {
            Logger.getLogger(LPSelector.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        
        
    
    }
            
    private void writeDataFile(File dataFile, ArrayList<DataAccount> providers, ArrayList<Module> modules, HashMap<String, Number> userConstraints)
    {
        
        
        try 
        {
            //Create FileWriter
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(dataFile)));
            
            //Write header
            out.println("data;");
            
            //Write IDAs
            out.println("param: Ida: IDA_SEC IDA_PERF IDA_STO :=");
            for (Module module : modules)
            {
                if (module.getType().equalsIgnoreCase("ida"))
                {
                    out.printf("%s %d %d %d\n",
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
            for (Module module : modules)
            {
                if (module.getType().equalsIgnoreCase("enc"))
                {
                    out.printf("%s %d %d %d\n",
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
            for (Module module : modules)
            {
                if (module.getType().equalsIgnoreCase("comp"))
                {
                    out.printf("%s %d %d %d\n",
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
                    out.printf("%s %d %d %d %d %d %d\n",
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
                out.printf("param %s:= %s;\n", param.getKey(), param.getValue().toString());
            }
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
