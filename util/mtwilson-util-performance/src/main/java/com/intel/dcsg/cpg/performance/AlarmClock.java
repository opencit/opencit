/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.performance;

import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides an easy way to make a thread delay for a specified 
 * amount of time (measured in milliseconds by default).
 * 
 * Examples:
<code>
AlarmClock clock = new AlarmClock();
clock.sleep(); // default is 1 second = 1000 ms
clock.sleep(500); // sleep for 0.5 second = 500 ms
clock.snooze(); // default is 1 second = 1000 ms
(new AlarmClock()).sleep(2000); // as a one-liner
</code>
 * 
 * @author jbuhacoff
 */
public class AlarmClock {
    private Logger log = LoggerFactory.getLogger(getClass());
    private long alarm; // milliseconds
    
    /**
     * Uses the default duration of 1 second. Can be changed
     * by calling setAlarm(long) or setAlarm(long,TimeUnit).
     */
    public AlarmClock() {
        this(1000);
    }
    
    
    /**
     * Sets the default sleep or snooze duration.
     * @param duration in milliseconds
     */
    public AlarmClock(long duration) {
        alarm = duration;
    }
    
    /**
     * Sets the default sleep or snooze duration.
     * @param duration
     * @param unit 
     */
    public AlarmClock(long duration, TimeUnit unit) {
        alarm = TimeUnit.MILLISECONDS.convert(duration, unit);
    }
    
    /**
     * Sets the default sleep or snooze duration.
     * @param duration in milliseconds 
     */
    public void setAlarm(long duration) {
        alarm = duration;
    }
    
    public void setAlarm(long duration, TimeUnit unit) {
        alarm = TimeUnit.MILLISECONDS.convert(duration, unit);
    }
    
    public void sleep() {
        sleep(alarm);
    }
    
    public void sleep(long duration, TimeUnit unit) {
        sleep(TimeUnit.MILLISECONDS.convert(duration, unit));
    }
    
    /**
     * Sleeps for the specified amount of time (in milliseconds).
     * If the thread is interrupted before the specified duration has
     * elapsed, this method tries again and again until the specified
     * duration has elapsed.
     * @param ms 
     */
    public void sleep(long duration) {
        long startTime = System.currentTimeMillis();
        long currentTime = startTime;
        do {
            snooze(startTime + duration - currentTime);
            currentTime = System.currentTimeMillis();
        } while( currentTime <= startTime + duration);
//        log.debug("Slept for {} ms", currentTime - startTime);
    }
    
    public void snooze() {
        snooze(alarm);
    }
    
    public void snooze(long duration, TimeUnit unit) {
        snooze(TimeUnit.MILLISECONDS.convert(duration, unit));
    }
    
    /**
     * Tries to sleep for the specified amount of time. If the
     * thread is interrupted during the snooze, this method exits.
     * Therefore the duration is not guaranteed to elapse before
     * this method exits.
     * @param duration 
     */
    public void snooze(long duration) {
//        long startTime = System.currentTimeMillis();
        try {
            Thread.sleep(duration);
        }
        catch(InterruptedException ignored) {
            log.trace("Snooze interrupted", ignored);
        }
//        long currentTime = System.currentTimeMillis();
//        log.debug("Snoozed for {} ms", currentTime - startTime);
    }
}
