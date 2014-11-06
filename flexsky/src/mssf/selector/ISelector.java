package mssf.selector;

import data.StoreSafeAccount;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author rlibardi
 */


public abstract class ISelector {
    public abstract void select(ArrayList<StoreSafeAccount> providers, ArrayList<String> idas, ArrayList<DispersalMethod> methods, HashMap<String, Integer> userProfile);

}
