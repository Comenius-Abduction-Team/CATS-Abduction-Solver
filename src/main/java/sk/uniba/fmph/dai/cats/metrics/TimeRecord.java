package sk.uniba.fmph.dai.cats.metrics;

class TimeRecord {
    long id;
    long startUserTime;
    long endUserTime;
    long startCpuTime;
    long endCpuTime;

    public TimeRecord(long id, long startUserTime, long endUserTime, long startCpuTime, long endCpuTime) {
        this.id = id;
        this.startUserTime = startUserTime;
        this.endUserTime = endUserTime;
        this.startCpuTime = startCpuTime;
        this.endCpuTime = endCpuTime;
    }

    long getUserTime(){
        return endUserTime - startUserTime;
    }

    long getCpuTime(){
        return endCpuTime - startCpuTime;
    }
}
