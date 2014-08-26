 package experimentPlanner;

import data.StoreSafeAccount;
import java.util.ArrayList;

/**
 *
 * @author rlibardi
 */


public class Scenario {
    public String name;
    public int repeatNo;
    
    public String path_to_DB;
    public String path_to_logDB;
    public boolean resetOnStartDB;
    
    ArrayList<StoreSafeAccount> providerList;
    ArrayList<ScenarioOperation> operationList;
   
    
    
}
