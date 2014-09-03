package util.monitor;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

class DataSample {
  long byteCount;
  Date start;
  Date end;

  DataSample(long bc, Date ts, Date tf) {
    byteCount = bc;
    start = ts;
    end = tf;
  }
}

public class DataMonitor {
  protected ArrayList<DataSample> samples;
  protected Date epoch;

  public DataMonitor() {
    samples = new ArrayList();
    epoch = new Date();
  }

  // Add a sample with a start and finish time.
  public void addSample(long bcount, Date ts, Date tf) {
    samples.add(new DataSample(bcount, ts, tf));
  }

  // Get the data rate of a given sample.
  public long getRateFor(int sidx) {
      long rate = 0;
    int scnt = samples.size();
    if (scnt > sidx && sidx >= 0) {
      DataSample s = samples.get(sidx);
      Date start = s.start;
      Date end = s.end;
      if (start == null && sidx >= 1) {
        DataSample prev = samples.get(sidx - 1);
        start = prev.end;
      }

      if (start != null && end != null) {
        long msec = end.getTime() - start.getTime();
        rate = 1000 * (long)s.byteCount / (long)msec;
      }
    }

    return rate;
  }

  // Get the rate of the last sample
  public long getLastRate() {
    int scnt = samples.size();
    return getRateFor(scnt - 1);
  }

  // Get the average rate over all samples.
  public long getAverageRate() {
    long msCount = 0;
    long byteCount = 0;
    Date start;
    Date finish;
    int scnt = samples.size();
    for (int i = 0; i < scnt; i++) {
      DataSample ds = (DataSample)samples.get(i);

      if (ds.start != null)
        start = ds.start;
      else if (i > 0) {
        DataSample prev = (DataSample)samples.get(i-1);
        start = ds.end;
      }
      else
        start = epoch;

      if (ds.end != null)
        finish = ds.end;
      else if (i < scnt - 1) {
        DataSample next = (DataSample)samples.get(i+1);
        finish = ds.start;
      }
      else
        finish = new Date();

      // Only include this sample if we could figure out a start
      // and finish time for it.
      if (start != null && finish != null) {
        byteCount += ds.byteCount;
        msCount += finish.getTime() - start.getTime();
      }
    }

    long rate = -1;
    if (msCount > 0) {
      rate = 1000 * (long)byteCount / (long)msCount;
    }

    return rate;
  }
  
  // Get the average rate over all samples.
  public long getTotalTime() {
    long msCount = 0;
    long byteCount = 0;
    Date start;
    Date finish;
    int scnt = samples.size();
    for (int i = 0; i < scnt; i++) {
      DataSample ds = (DataSample)samples.get(i);

      if (ds.start != null)
        start = ds.start;
      else if (i > 0) {
        DataSample prev = (DataSample)samples.get(i-1);
        start = ds.end;
      }
      else
        start = epoch;

      if (ds.end != null)
        finish = ds.end;
      else if (i < scnt - 1) {
        DataSample next = (DataSample)samples.get(i+1);
        finish = ds.start;
      }
      else
        finish = new Date();

      // Only include this sample if we could figure out a start
      // and finish time for it.
      if (start != null && finish != null) {
        byteCount += ds.byteCount;
        msCount += finish.getTime() - start.getTime();
      }
    }

    return msCount;
  }
}
