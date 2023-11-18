package me.phoenixstyle.parkour.utility;

public class tick_time {
    long time;

    public tick_time(long ticks) {
        time = ticks;
    }

    public String to_string() {
        long x = time;
        short millisec, sec, min, hour;
        millisec = (short)(time % 20);
        millisec *= 50;
        time /= 20;
        sec = (short)(time %  60);
        time /= 60;
        min = (short)(time %  60);
        time /= 60;
        hour = (short)(time %  60);
        time /= 60;
        String result;
        if(hour > 0) {
            result = String.format("%02d:%02d:%02d.%03d", hour, min, sec, millisec);
        }
        else {
            result = String.format("%02d:%02d.%03d", min, sec, millisec);
        }
        return result;
    }
}
