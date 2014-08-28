package experimentPlanner;

import data.StorageOptions;
import data.StoreSafeAccount;
import data.StoreSafeFile;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import management.StoreSafeManager;
import pipeline.pipe.PipeTest;

/**
 *
 * @author rlibardi
 */


public class ScenarioExecutor {
    private ScenarioExecutor executor;
    
    ScenarioExecutor getInstance()
    {
        if (this.executor == null)
        {
            this.executor = new ScenarioExecutor();
        }
        return this.executor;
    }

    private ScenarioExecutor() {
    }
    
    public boolean execute(Scenario scenario)
    {
     
      StoreSafeManager instance = StoreSafeManager.getInstance(scenario.path_to_DB, scenario.path_to_logDB); 
      
      System.out.println("Running Scenario " + scenario.name + "...");
      
      
      //Adding accounts
      System.out.println("Adding accounts...");
        for (StoreSafeAccount ssa : scenario.providerList)
        {
            instance.addProvider(ssa);
        }
        
        //Repetitions
        for (int i = 0; i < scenario.repeatNo; i++)
        {
            System.out.println("Running repetition " + i + " of " + scenario.repeatNo);
            if (scenario.resetOnStartDB) instance.deleteAllFiles();
            
            //Run the operations within a scenario
            for (ScenarioOperation operation : scenario.operationList)
            {
                System.out.println("Running operation " + scenario.operationList.indexOf(operation) + "of " + scenario.operationList.size() + ": " + operation.getAction());
                if (operation.getAction() == "upload")
                {                    
                    boolean result = instance.storeFile(operation.getPathForFile(), operation.getFile().getType(), operation.getIdaMethod(), operation.getTotalParts(), operation.getReqParts(), operation.getFile().getRevision(), scenario.providerList, operation.getFile().getOptions());
                    System.out.println(result);
                }
                
                else if (operation.getAction() == "download")
                {
                            boolean result = instance.downloadFile(operation.getPathForFile(), operation.getFile());
                            System.out.println(result);
                }
                
                else if (operation.getAction() == "delete")
                {
                    boolean result = instance.deleteFile(operation.getFile());
                    System.out.println(result);
                }
            }
            
            
        }      
       

      return true;
          
    }
    
    
}
