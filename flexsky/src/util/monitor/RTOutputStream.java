package util.monitor;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class RTOutputStream extends FilterOutputStream {
  long byteCount = 0;
  long timeCount = 0;

  public RTOutputStream(OutputStream out) {
    super(out);
  }

  public void write(int b) throws IOException {
    long start = System.currentTimeMillis();
    super.write(b);
    timeCount += System.currentTimeMillis() - start;
    byteCount++;
    
  }

  public void write(byte data[]) throws IOException {
    super.write(data);
  }

  public void write(byte data[], int off, int len)
    throws IOException {
    super.write(data, off, len);
  }

  //Kbyte/s
  public double averageRate() {
    return ( (byteCount/1000.0) / (timeCount/1000.0) );
  } 
  
  //ms
   public double totalTime() {
    return timeCount;
  }
   
   //ms
   public double totalBytes() {
    return (byteCount/1000.0);
  }
 
 
}
