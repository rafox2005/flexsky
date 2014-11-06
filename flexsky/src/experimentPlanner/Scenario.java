 package experimentPlanner;

import data.DataAccount;
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
    
    ArrayList<DataAccount> providerList;
    ArrayList<ScenarioOperation> operationList;

    public Scenario() {
        this.providerList = new ArrayList<DataAccount>();
        this.operationList = new ArrayList<>();
    }
    
    
   
    
    
}
