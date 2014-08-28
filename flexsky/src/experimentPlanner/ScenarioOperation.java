package experimentPlanner;

import data.StorageOptions;
import data.StoreSafeFile;
import java.io.File;

/**
 *
 * @author rlibardi
 */


public class ScenarioOperation {
    private String action;
    private StoreSafeFile file;
    private String pathForFile;
    private StorageOptions options;
    private int totalParts;
    private int reqParts;
    private String idaMethod; 

    public ScenarioOperation() {
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public StoreSafeFile getFile() {
        return file;
    }

    public void setFile(StoreSafeFile file) {
        this.file = file;
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

    public String getIdaMethod() {
        return idaMethod;
    }

    public void setIdaMethod(String idaMethod) {
        this.idaMethod = idaMethod;
    }

    public StorageOptions getOptions() {
        return options;
    }

    public void setOptions(StorageOptions options) {
        this.options = options;
    }

    public String getPathForFile() {
        return pathForFile;
    }

    public void setPathForFile(String pathForFile) {
        this.pathForFile = pathForFile;
    }
    
    
    
}
