package sk.uniba.fmph.dai.cats.timer;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ThreadTimer extends Thread {

    private static final long BILLION = 1000000000;
    private final long interval;
    private final long thisThreadId;
    private final Map<Long, TimeRecord> records = new ConcurrentHashMap<>(8, 0.9f, 1);

    /**
     * Create a polling thread to track times.
     */
    public ThreadTimer(final long interval) {
        super("Thread time monitor");
        this.interval = interval;
        thisThreadId = getId();
        setDaemon(true);
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            update();
            try {
                sleep(interval);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    /**
     * Update the hash table of thread times.
     */
    private void update() {
        final ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        final long[] ids = bean.getAllThreadIds();

        for (long id : ids) {

            if (id == thisThreadId) {
                continue; // Exclude polling thread
            }

//            final long cpuTime = bean.getThreadCpuTime(id);
            final long userTime = bean.getThreadUserTime(id);

            if (/*cpuTime == -1 ||*/ userTime < 1) {
                continue; // Thread died
            }

            TimeRecord timeRecord = records.get(id);

            if (timeRecord == null) {
                timeRecord = new TimeRecord();
                timeRecord.id = id;
//                times.startCpuTime = cpuTime;
                timeRecord.startUserTime = userTime;
//                times.endCpuTime = cpuTime;
                timeRecord.endUserTime = userTime;
                records.put(id, timeRecord);
            } else {
//                times.endCpuTime = cpuTime;
                timeRecord.endUserTime = userTime;
            }
        }
    }

    /**
     * Get total CPU time so far in nanoseconds.
     */
//    public long getTotalCpuTime() {
//        final Collection<Times> hist = history.values();
//        long time = 0L;
//
//        for (Times times : hist) {
//            time += (times.endCpuTime - times.startCpuTime);
//        }
//
//        return time;
//    }
//
//    public double getTotalCpuTimeInSec() {
//        return (double) getTotalCpuTime() / BILLION;
//    }

    /**
     * Get total user time so far in nanoseconds.
     */
    long getTotalUserTime() {
        final Collection<TimeRecord> hist = records.values();
        long time = 0L;

        for (TimeRecord timeRecord : hist) {
            //System.out.println(times.id + " : " + (times.endUserTime - times.startUserTime));
            time += timeRecord.getUserTime();
        }

        return time;
    }

    double getTotalUserTimeInSec() {
        return (double) getTotalUserTime() / BILLION;
    }

    /**
     * Get total system time so far in nanoseconds.
     */
//    public long getTotalSystemTime() {
//        return getTotalCpuTime() - getTotalUserTime();
//    }
//
//    public double getTotalSystemTimeInSec() {
//        return (double) getTotalSystemTime() / BILLION;
//    }
}
