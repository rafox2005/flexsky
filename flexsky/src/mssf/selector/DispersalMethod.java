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
    private ArrayList<PipeModule> filePipeline;
    private int totalParts;
    private int reqParts;
    private String ida;    

    public HashMap<String, Double> getSelectionParameters() {
        return selectionParameters;
    }

    public void setSelectionParameters(HashMap<String, Double> selectionParameters) {
        this.selectionParameters = selectionParameters;
    }

    public ArrayList<PipeModule> getFile_pipeline() {
        return filePipeline;
    }

    public void setFile_pipeline(ArrayList<PipeModule> file_pipeline) {
        this.filePipeline = file_pipeline;
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

    public String getIda() {
        return ida;
    }

    public void setIda(String ida) {
        this.ida = ida;
    }
    
    
}
