package mssf.selector;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author rlibardi
 */


public abstract class ISelector {
    public abstract void select(ArrayList<DispersalMethod> methods, HashMap<String, Integer> userProfile);

}
