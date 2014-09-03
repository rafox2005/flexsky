package util.monitor;

import java.io.OutputStream;
import java.io.FilterOutputStream;
import java.util.Date;
import java.io.IOException;

public class RTOutputStream extends FilterOutputStream {
  DataMonitor monitor;

  public RTOutputStream(OutputStream out) {
    super(out);
    monitor = new DataMonitor();
  }

  public void write(int b) throws IOException {
    Date start = new Date();
    super.write(b);
    monitor.addSample(1, start, new Date());
  }

  public void write(byte data[]) throws IOException {
    Date start = new Date();
    super.write(data);
    monitor.addSample(data.length, start, new Date());
  }

  public void write(byte data[], int off, int len)
    throws IOException {
    Date start = new Date();
    super.write(data, off, len);
    monitor.addSample(data.length, start, new Date());
  }

  public long averageRate() {
    return monitor.getAverageRate();
  }

  public long lastRate() {
    return monitor.getLastRate();
  }
  
   public long totalTime() {
    return monitor.getTotalTime();
  }
}
