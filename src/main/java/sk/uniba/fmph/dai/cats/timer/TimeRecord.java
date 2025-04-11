package sk.uniba.fmph.dai.cats.timer;

class TimeRecord {
    long id;
    long startUserTime;
    long endUserTime;

    public TimeRecord(long id, long startUserTime, long endUserTime) {
        this.id = id;
        this.startUserTime = startUserTime;
        this.endUserTime = endUserTime;
    }

    long getUserTime(){
        return endUserTime - startUserTime;
    }
}
