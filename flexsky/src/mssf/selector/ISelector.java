package mssf.selector;

import data.DataAccount;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author rlibardi
 */


public abstract class ISelector {
    public abstract DispersalSelection select(ArrayList<DataAccount> providers, ArrayList<PipeModule> modules, HashMap<String, Integer> userConstraints, HashMap<String, String> parameters);

}
