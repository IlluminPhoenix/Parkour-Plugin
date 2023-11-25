package me.phoenixstyle.parkour.utility;

public class tick_time {
    double time;

    public tick_time(double ticks) {
        time = ticks;
    }

    public String to_string() {
        double x = time;
        double millisec;
        short sec, min, hour;
        millisec = x % 20;
        millisec *= 50;
        x /= 20;
        sec = (short)(x %  60);
        x /= 60;
        min = (short)(x %  60);
        x /= 60;
        hour = (short)(x %  60);
        x /= 60;
        String result;
        if(hour > 0) {
            result = String.format("%02d:%02d:%02d.%03d", hour, min, sec, (int)millisec);
        }
        else {
            result = String.format("%02d:%02d.%03d", min, sec, (int)millisec);
        }
        return result;
    }
}
