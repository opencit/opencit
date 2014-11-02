/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.performance.report;

import java.util.ArrayList;
import org.apache.commons.math3.stat.StatUtils;

/**
 *
 * @author jbuhacoff
 */
public class PerformanceInfo {

    private long[] values;
    private long min;
    private long max;

    public PerformanceInfo(ArrayList<Long> sample) {
        values = new long[sample.size()];
        for (int i = 0; i < values.length; i++) {
            values[i] = sample.get(i);
        }
    }
    
    public PerformanceInfo(long[] sample) {
        values = new long[sample.length];
        System.arraycopy(sample, 0, values, 0, sample.length);
    }
    
    public long[] getData() {
        return values;
    }

    public long getMin() {
        min = values[0];
        for (int i = 1; i < values.length; i++) {
            min = Math.min(min, values[i]);
        }
        return min;
    }

    public long getMax() {
        max = values[0];
        for (int i = 1; i < values.length; i++) {
            max = Math.max(max, values[i]);
        }
        return max;
    }

    public double getAverage() {
        int n = values.length;
        double[] fractions = new double[n];
        for (int i = 0; i < values.length; i++) {
            fractions[i] = values[i] * 1.0 / n;
        }
        return StatUtils.sum(fractions);
    }
}