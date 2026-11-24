package sk.uniba.fmph.dai.cats.metrics;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ConcurrentHashMap;

public class MetricsThread extends Thread {

    private static final long BILLION = 1000000000;
    private long timeInterval;
    private final long thisThreadId;
    private final ThreadMXBean threadBean;
    private final boolean cpuTimeSupported;
    private final CountDownLatch initialized = new CountDownLatch(1);
    private long startUserTime;
    private long startCpuTime;
    private final Map<Long, TimeRecord> records = new ConcurrentHashMap<>();
    private double memorySum;
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

        threadBean = ManagementFactory.getThreadMXBean();
        boolean supported = threadBean.isThreadCpuTimeSupported();
        if (supported && !threadBean.isThreadCpuTimeEnabled()) {
            try {
                threadBean.setThreadCpuTimeEnabled(true);
            } catch (SecurityException | UnsupportedOperationException e) {
                supported = false;
            }
        }
        cpuTimeSupported = supported;

        runtime = Runtime.getRuntime();
    }

    @Override
    public void run() {

        updateTimeRecords();
        startUserTime = getAccumulatedUserTime();
        startCpuTime = getAccumulatedCpuTime();
        initialized.countDown();

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
        final long[] ids = threadBean.getAllThreadIds();

        for (long id : ids) {

            if (id == thisThreadId) {
                continue; // Exclude polling thread
            }

            final long userTime = threadBean.getThreadUserTime(id);

            if (userTime < 1) {
                continue; // Thread died
            }

            final long cpuTime = cpuTimeSupported ? threadBean.getThreadCpuTime(id) : userTime;
            if (cpuTimeSupported && cpuTime < 0) {
                continue;
            }

            TimeRecord timeRecord = records.get(id);

            if (timeRecord == null) {
                timeRecord = new TimeRecord(id, userTime, userTime, cpuTime, cpuTime);
                records.put(id, timeRecord);
            } else {
                timeRecord.endUserTime = userTime;
                timeRecord.endCpuTime = cpuTime;
            }
        }
    }

    void refreshTimeRecords() {
        updateTimeRecords();
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
    private long getAccumulatedUserTime() {
        final Collection<TimeRecord> hist = records.values();
        long time = 0L;

        for (TimeRecord timeRecord : hist) {
            time += timeRecord.getUserTime();
        }

        return time;
    }

    private long getAccumulatedCpuTime() {
        if (!cpuTimeSupported) {
            return -1L;
        }

        final Collection<TimeRecord> hist = records.values();
        long time = 0L;

        for (TimeRecord timeRecord : hist) {
            time += timeRecord.getCpuTime();
        }

        return time;
    }

    long getTotalUserTime() {
        return getAccumulatedUserTime() - startUserTime;
    }

    long getTotalCpuTime() {
        if (!cpuTimeSupported) {
            return -1L;
        }
        return getAccumulatedCpuTime() - startCpuTime;
    }

    double getTotalUserTimeInSec() {
        return (double) getTotalUserTime() / BILLION;
    }

    double getTotalCpuTimeInSec() {
        long cpuTime = getTotalCpuTime();
        if (cpuTime < 0) {
            return Double.NaN;
        }
        return (double) cpuTime / BILLION;
    }

    void awaitInitialization() {
        try {
            initialized.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void terminate(){
        running = false;
        interrupt();
    }

    double getAverageMemory(){
        return memorySum / memoryCount;
    }

    void setTimeInterval(){
        this.timeInterval = 10;
    }

    void clearMemoryRecords(){
        memorySum = 0;
        memoryCount = 0;
    }

}
