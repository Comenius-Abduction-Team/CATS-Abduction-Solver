package sk.uniba.fmph.dai.cats.timer;

class TimeRecord {
    long id;
    long startCpuTime;
    long startUserTime;
    long endCpuTime;
    long endUserTime;

    long getUserTime(){
        return endUserTime - startUserTime;
    }
}
