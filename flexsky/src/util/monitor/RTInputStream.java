package util.monitor;
 
import java.io.InputStream;
import java.io.FilterInputStream;
import java.util.Date;
import java.io.IOException;

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
    byteCount++;
    timeCount += System.currentTimeMillis() - start;
    return b;
  }

  public int read(byte data[]) throws IOException {
    long start = System.currentTimeMillis();
    int cnt = super.read(data);
    byteCount += data.length;
    timeCount += System.currentTimeMillis() - start;
    return cnt;
  }

  public int read(byte data[], int off, int len)
    throws IOException {
    long start = System.currentTimeMillis();
    int cnt = super.read(data, off, len);
    byteCount += len;
    timeCount += System.currentTimeMillis() - start;
    return cnt;
  }

  //Kbyte/s
  public double averageRate() {      
    return (double) ( (byteCount/1024.0) / (timeCount/1000.0) );
  } 
  
  //ms
   public double totalTime() {
    return timeCount;
  }
   
   //k
   public double totalBytes() {
    return (byteCount/1024.0);
  }
}
