package mssf.selector;

import experimentPlanner.ScenarioOperation;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author rlibardi
 */


public class ISelector {
    public abstract DispersalMethod select(ArrayList<DispersalMethod> methods, HashMap<String, int> userProfile);

}
