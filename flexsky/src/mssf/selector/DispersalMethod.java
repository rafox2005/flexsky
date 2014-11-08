package mssf.selector;

import data.StorageOptions;
import data.DataAccount;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author rlibardi
 */


public class DispersalMethod {
    private HashMap<String, Double> selectionParameters;    
    private ArrayList<Module> modules;
    private int totalParts;
    private int reqParts;

    public DispersalMethod() {
        this.selectionParameters = new HashMap<>();
        this.modules = new ArrayList<>();        
    }
    
    

    public HashMap<String, Double> getSelectionParameters() {
        return selectionParameters;
    }

    public void setSelectionParameters(HashMap<String, Double> selectionParameters) {
        this.selectionParameters = selectionParameters;
    }

    public ArrayList<Module> getFile_pipeline() {
        return modules;
    }

    public void setFile_pipeline(ArrayList<Module> file_pipeline) {
        this.modules = file_pipeline;
    }
        
    public int getTotalParts() {
        return totalParts;
    }

    public void setTotalParts(int totalParts) {
        this.totalParts = totalParts;
    }
    
    public int getReqParts() {
        return reqParts;
    }

    public void setReqParts(int reqParts) {
        this.reqParts = reqParts;
    }    
    
}
