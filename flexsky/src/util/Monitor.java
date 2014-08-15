/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

/**
 * Singleton Monitor
 * @author rafox
 */
public class Monitor {
    
    private static Monitor instance;
    
    private long timeToEncode = 0;
    private long timeToDecode = 0;
    private long timeToRead = 0;
    private long timeToWrite = 0;
    
    private long startTime = 0;
    private boolean usingStartTime = false;
    
    public static Monitor getInstance() {
      if (instance == null)
         instance = new Monitor();
      return instance;
   }
    
    private boolean setStartTime() {
        if (!this.usingStartTime)
        {
           this.usingStartTime = true;
           this.startTime = System.currentTimeMillis();
           return true;
        }
        else return false;
    }
    
    private long getElapsedTime() {
        long now = System.currentTimeMillis();
        if (this.usingStartTime)
        {
           this.usingStartTime = false;
           return (now-this.startTime);
        }
        else return 0;
    }

    public double getTimeToEncode() {
        return timeToEncode;
    }

    public void startTimeToEncode() {
        this.setStartTime();
    }
    
    public void stopTimeToEncode() {
           this.timeToEncode += this.getElapsedTime();
           
    }

    public double getTimeToDecode() {
        return timeToDecode;
    }

    public void startTimeToDecode() {
        this.setStartTime();
    }
    
    public void stopTimeToDecode() {
        this.timeToDecode += this.getElapsedTime();
    }

    public double getTimeToRead() {
        return timeToRead;
    }

    public void startTimeToRead() {
        this.setStartTime();
    }
    
    public void stopTimeToRead() {
        this.timeToRead += this.getElapsedTime();
    }

    public double getTimeToWrite() {
        return timeToWrite;
    }

    public void startTimeToWrite() {
        this.setStartTime();
    }
    
    public void stopTimeToWrite() {
        this.timeToWrite += this.getElapsedTime();
    }

    public long getTimeTotal() {
        return (this.timeToDecode + this.timeToEncode + this.timeToRead + this.timeToWrite);
    }   
    
}
