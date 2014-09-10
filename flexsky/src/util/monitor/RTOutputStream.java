package util.monitor;

import java.io.OutputStream;
import java.io.FilterOutputStream;
import java.util.Date;
import java.io.IOException;

public class RTOutputStream extends FilterOutputStream {
  long byteCount = 0;
  long timeCount = 0;
    private boolean closed = false;

  public RTOutputStream(OutputStream out) {
    super(out);
  }

  public void write(int b) throws IOException {
    long start = System.currentTimeMillis();
    super.write(b);
    byteCount++;
    timeCount += System.currentTimeMillis() - start;
  }

  public void write(byte data[]) throws IOException {
    super.write(data);
  }

  public void write(byte data[], int off, int len)
    throws IOException {
    super.write(data, off, len);
  }
  
  public void close() throws IOException
  {
      this.closed = true;
      super.close();
  }

  //Kbyte/s
  public double averageRate() {
    return ( (byteCount/1024.0) / (timeCount/1000.0) );
  } 
  
  //ms
   public double totalTime() {
    return timeCount;
  }
   
   //ms
   public double totalBytes() {
    return (byteCount/1024.0);
  }
   
   public boolean isClosed()
   {
       return this.closed;
   }
 
}
