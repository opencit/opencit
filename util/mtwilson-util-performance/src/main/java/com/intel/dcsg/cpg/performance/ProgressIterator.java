/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.performance;

import java.util.Iterator;

/**
 * This class wraps any Iterator and provides a built-in progress monitor.
 * For example, code that looks like this:
<code>
CountingIterator it = new CountingIterator<Task>(tasks.iterator());
ProgressMonitor monitor = new ProgressMonitor(it, observer);
monitor.start();
while(it.hasNext()) {
  Task task = it.next();
  task.run();
}
monitor.stop();
</code>
* Can now be written like this instead:
<code>
ProgressIterator<Task> it = new ProgressIterator<Task>(tasks, observer);
while(it.hasNext()) {
  Task task = it.next();
  task.run();
}
</code>
 * 
 * 
 * @since 0.1
 * @author jbuhacoff
 */
public class ProgressIterator<T> implements Iterator<T>,Value<Long> {
    protected CountingIterator<T> iterator = null;
    protected Monitor<Long> monitor = new Monitor<>();
    protected boolean enabled = false;
    public ProgressIterator(Iterable<T> iterable) {
        this(iterable.iterator(),new LogObserver());
    }
    public ProgressIterator(Iterator<T> iterator) {
        this(iterator,new LogObserver());
    }
    public ProgressIterator(Iterable<T> iterable, Observer<Long> observer) {
        this(iterable.iterator(),observer);
    }
    public ProgressIterator(Iterator<T> iterator, Observer<Long> observer) {
        this.iterator = new CountingIterator<>(iterator);
        monitor.setValue(this.iterator);
        monitor.setObserver(observer);
    }
    public void setObserver(Observer<Long> observer) {
        monitor.setObserver(observer);
    }
    @Override
    public T next() {
        return iterator.next();
    }
    @Override
    public boolean hasNext() {
        if(!enabled) {
            monitor.start();
            enabled = true;
        }
        if(!iterator.hasNext()) {
            monitor.stop();
            enabled = false;
        }
        return iterator.hasNext();
    }
    @Override
    public void remove() {
        iterator.remove();
    }
    @Override
    public Long getValue() { return iterator.getValue(); }
}
