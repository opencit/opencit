/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.performance;

import java.util.ArrayList;
import java.util.List;

/**
 * Use this observer when you need to group together multiple other observers to
 * receive the same updates from a Monitor.  Because a Monitor accepts only one
 * Observer instance, you can create a CompositeObserver that includes all the other
 * observers and it will relay observations to them in the order they were added.
 * 
 * Example:
<code>
// create all the observers you need
JdbcObserver<Long> jdbcObserver = new JdbcObserver<Long>(...);
LogObserver<Long> logObserver = new LogObserver<Long>(...);
// unify them with a single composite observer
CompositeObserver<Long> composite = new CompositeObserver(jdbcObserver, logObserver);
// then pass it to whatever needs an observer: all observations will pass through to
// the specified observers
ProgressMonitor monitor = new ProgressMonitor(composite,...);
</code>
 * @since 0.1
 * @author jbuhacoff
 */
public class CompositeObserver<T> implements Observer<T> {
    private List<Observer<T>> observerList;
    public CompositeObserver() {
        observerList = new ArrayList<Observer<T>>();
    }
    public CompositeObserver(List<Observer<T>> list) {
        observerList = list;
    }
    /*
    public CompositeObserver(Observer<T>[] array) {
        observerList = new ArrayList<Observer<T>>();
        for(Observer<T> observer : array) {
            observerList.add(observer);
        }
    }*/
    public CompositeObserver(Observer<T>... array) {
        observerList = new ArrayList<Observer<T>>(array.length);
        for(Observer<T> observer : array) {
            observerList.add(observer);
        }
    }
    
    @Override
    public void observe(T value) {
        for(Observer<T> observer : observerList) {
            observer.observe(value);
        }
    }
    
    /**
     * Does not check if the observer list already has this observer. 
     * @param observer 
     */
    public void add(Observer<T> observer) {
        observerList.add(observer);
    }
}
