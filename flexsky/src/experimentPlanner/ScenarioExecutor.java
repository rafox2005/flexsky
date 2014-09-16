package experimentPlanner;

import data.StoreSafeAccount;
import data.StoreSafeFile;
import java.io.File;
import management.StoreSafeManager;

/**
 *
 * @author rlibardi
 */


public class ScenarioExecutor {
    private static ScenarioExecutor executor;
    
    public static ScenarioExecutor getInstance()
    {
        if (ScenarioExecutor.executor == null)
        {
            ScenarioExecutor.executor = new ScenarioExecutor();
        }
        return ScenarioExecutor.executor;
    }

    private ScenarioExecutor() {
    }
    
    public boolean execute(Scenario scenario)
    {
     
      StoreSafeManager instance = StoreSafeManager.getInstance(scenario.path_to_DB, scenario.path_to_logDB); 
      
      System.out.println("Running Scenario " + scenario.name + "...");
      
      System.out.println("Deleting accounts...");
      instance.delAllAccounts();
      
      //Adding accounts
      System.out.println("Adding accounts...");
        for (StoreSafeAccount ssa : scenario.providerList)
        {
            instance.addAccount(ssa);
        }
        
        //Repetitions
        for (int i = 0; i < scenario.repeatNo; i++)
        {
            System.out.println("Running repetition " + i + " of " + scenario.repeatNo);
            System.out.println("Deleting all files...");
            instance.deleteAllFiles();
            
            //Run the operations within a scenario
            for (ScenarioOperation operation : scenario.operationList)
            {
                System.out.println("Running operation " + scenario.operationList.indexOf(operation) + " of " + scenario.operationList.size() + ": " + operation.getAction());
                if (operation.getAction().equalsIgnoreCase("upload"))
                {                    
                    boolean result = instance.storeFile(operation.getPathForFile(), operation.getFile().getType(), operation.getIdaMethod(), operation.getTotalParts(), operation.getReqParts(), operation.getFile().getRevision(), scenario.providerList, operation.getFile().getOptions());
                    if (result == true) System.out.println("Ok...");
                    else {                        
                        System.out.println("Failed... Trying again...");
                        result = instance.storeFile(operation.getPathForFile(), operation.getFile().getType(), operation.getIdaMethod(), operation.getTotalParts(), operation.getReqParts(), operation.getFile().getRevision(), scenario.providerList, operation.getFile().getOptions());
                    }
                }
                
                else if (operation.getAction().equalsIgnoreCase("download"))
                {
                    
                    File filePath = new File(operation.getPathForFile());                   
                    
                    StoreSafeFile fileInfo = instance.getFileInfo(filePath.getName(), operation.getFile().getRevision());
                    boolean result = false;
                    if (fileInfo != null) result = instance.downloadFile(operation.getPathForFile(), fileInfo);
                    if (result == true) System.out.println("Ok...");
                    else {                        
                        System.out.println("Failed... Trying again...");
                        result = instance.downloadFile(operation.getPathForFile(), fileInfo);
                    }
                }
                
                else if (operation.getAction().equalsIgnoreCase("delete"))
                {
                    boolean result = instance.deleteFile(operation.getFile());
                    if (result == true) System.out.println("Ok...");
                    else {                        
                        System.out.println("Failed... Trying again...");
                        result = instance.deleteFile(operation.getFile());
                    }
                }
            }
            
            
        }      
       

      return true;
          
    }
    
    
}
