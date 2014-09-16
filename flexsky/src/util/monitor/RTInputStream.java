package util.monitor;
 
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author rlibardi
 */




public class RTInputStream extends FilterInputStream {
  long byteCount = 0;
  long timeCount = 0;

  public RTInputStream(InputStream in) {
    super(in);
  }

  public int read() throws IOException {
    long start = System.currentTimeMillis();
    int b = super.read();
    timeCount += System.currentTimeMillis() - start;
    byteCount++;    
    return b;
  }

  public int read(byte data[]) throws IOException {
    long start = System.currentTimeMillis();
    int cnt = super.read(data);
    timeCount += System.currentTimeMillis() - start;
    if (cnt > 0) byteCount += cnt;    
    return cnt;
  }

  public int read(byte data[], int off, int len)
    throws IOException {
    long start = System.currentTimeMillis();
    int cnt = super.read(data, off, len);
    timeCount += System.currentTimeMillis() - start;
    if (cnt > 0) byteCount += cnt;    
    return cnt;
  }

  //Kbyte/s
  public double averageRate() {      
    return (double) ( (byteCount/1000.0) / (timeCount/1000.0) );
  } 
  
  //ms
   public double totalTime() {
    return timeCount;
  }
   
   //k
   public double totalKBytes() {
    return (byteCount/1000.0);
  }

}
