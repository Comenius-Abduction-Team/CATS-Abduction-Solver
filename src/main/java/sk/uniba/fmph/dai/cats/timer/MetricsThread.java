package sk.uniba.fmph.dai.cats.timer;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MetricsThread extends Thread {

    private static final long BILLION = 1000000000;
    private final long timeInterval;
    private final long thisThreadId;
    private long startTime;
    private final Map<Long, TimeRecord> records = new ConcurrentHashMap<>();
    private int memorySum;
    private double memoryCount;
    private final Runtime runtime;

    public boolean running = true;

    /**
     * Create a polling thread to track times.
     */
    public MetricsThread(final long timeInterval) {
        super("Thread time monitor");
        this.timeInterval = timeInterval;
        thisThreadId = getId();
        setDaemon(true);

        runtime = Runtime.getRuntime();
    }

    @Override
    public void run() {

        updateTimeRecords();
        startTime = getTotalUserTime();

        while (running) {
            updateTimeRecords();
            measureMemory();
            try {
                sleep(timeInterval);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    /**
     * Update the hash table of thread times.
     */
    private void updateTimeRecords() {
        final ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        final long[] ids = bean.getAllThreadIds();

        for (long id : ids) {

            if (id == thisThreadId) {
                continue; // Exclude polling thread
            }

            final long userTime = bean.getThreadUserTime(id);

            if (userTime < 1) {
                continue; // Thread died
            }

            TimeRecord timeRecord = records.get(id);

            if (timeRecord == null) {
                timeRecord = new TimeRecord(id, userTime, userTime);
                records.put(id, timeRecord);
            } else {
                timeRecord.endUserTime = userTime;
            }
        }
    }

    private void measureMemory(){
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        double usedMemory = (totalMemory - freeMemory) / 1000000.0;
        memorySum += usedMemory;
        memoryCount += 1;
    }

    /**
     * Get total user time so far in nanoseconds.
     */
    long getTotalUserTime() {
        final Collection<TimeRecord> hist = records.values();
        long time = 0L;

        for (TimeRecord timeRecord : hist) {
            time += timeRecord.getUserTime();
        }

        return time - startTime;
    }

    double getTotalUserTimeInSec() {
        return (double) getTotalUserTime() / BILLION;
    }

    public void terminate(){
        running = false;
    }

    double getAverageMemory(){
        return memorySum / memoryCount;
    }

    void clearMemoryRecords(){
        memorySum = 0;
        memoryCount = 0;
    }

}
