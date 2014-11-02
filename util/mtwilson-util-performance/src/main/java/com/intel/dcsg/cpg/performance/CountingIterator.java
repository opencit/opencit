/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.performance;

import java.util.Iterator;

/**
 * This class wraps any Iterator and increments a counter every time next() is 
 * called. You can use this to monitor the progress of any loop in which you use
 * an Iterator.
 * 
 * Example:
<code>
CountingIterator<String> it = new CountingIterator<String>(list.iterator());
ProgressMonitor monitor = new ProgressMonitor();
monitor.setCounter(it);
monitor.start();
while(it.hasNext()) {
  String text = it.next(); // counter automatically incremented here
  log.debug(text);
}
monitor.stop();
</code>
 * @since 0.1
 * @author jbuhacoff
 */
public class CountingIterator<T> implements Iterator<T>, Value<Long> {
    protected Iterator<T> iterator = null;
    protected LongCounter counter = new LongCounter();
    public CountingIterator(Iterator<T> iterator) {
        this.iterator = iterator;
    }
    public CountingIterator(Iterable<T> iterable) {
        this.iterator = iterable.iterator();
    }
    @Override
    public T next() {
        counter.increment();
        return iterator.next();
    }
    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }
    @Override
    public void remove() {
        iterator.remove();
    }
    @Override
    public Long getValue() {
        return counter.getValue();
    }
}
