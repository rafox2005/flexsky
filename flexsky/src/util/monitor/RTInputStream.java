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
  DataMonitor monitor;

  public RTInputStream(InputStream in) {
    super(in);
    monitor = new DataMonitor();
  }

  public int read() throws IOException {
    Date start = new Date();
    int b = super.read();
    monitor.addSample(1, start, new Date());
    return b;
  }

  public int read(byte data[]) throws IOException {
    Date start = new Date();
    int cnt = super.read(data);
    monitor.addSample(cnt, start, new Date());
    return cnt;
  }

  public int read(byte data[], int off, int len)
    throws IOException {
    Date start = new Date();
    int cnt = super.read(data, off, len);
    monitor.addSample(cnt, start, new Date());
    return cnt;
  }

  public long averageRate() {
    return monitor.getAverageRate();
  }
  
  public long totalTime() {
    return monitor.getTotalTime();
  }

  public long lastRate() {
    return monitor.getLastRate();
  }
}
