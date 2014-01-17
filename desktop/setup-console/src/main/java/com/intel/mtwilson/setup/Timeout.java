/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup;

import com.intel.dcsg.cpg.validation.ObjectModel;
import java.util.concurrent.TimeUnit;

/**
 * Convenience class for passing around timeouts.
 * @author jbuhacoff
 */
public class Timeout extends ObjectModel {
    private final long duration;
    private final TimeUnit units;
    public Timeout(long duration, TimeUnit units) {
        this.duration = duration;
        this.units = units;
    }
    public long toNanoseconds() { return TimeUnit.NANOSECONDS.convert(duration, units); }
    public long toMicroseconds() { return TimeUnit.MICROSECONDS.convert(duration, units); }
    public long toMilliseconds() { return TimeUnit.MILLISECONDS.convert(duration, units); }
    public long toSeconds() { return TimeUnit.SECONDS.convert(duration, units); }
    public long toMinutes() { return TimeUnit.MINUTES.convert(duration, units); }
    public long toHours() { return TimeUnit.HOURS.convert(duration, units); }
    public long toDays() { return TimeUnit.DAYS.convert(duration, units); }

    @Override
    protected void validate() {
        if( units == null ) { fault("TimeUnit is null"); }
    }
}
