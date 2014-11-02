/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.performance;

/**
 * You can wrap any observer using this LazyObserver in order
 * to change its behavior so that if observe() is called multiple
 * times with the same value, the wrapped observer is notified
 * only the first time. So this is useful to create observers that
 * only react to changes in the value. 
 * 
 * Note that the comparison is done by calling hashCode() on the
 * observed value. 
 * 
 * Usage example: you have a JdbcObserver that logs progress updates
 * to the database, and you only want to execute an UPDATE statement
 * when the observed value actually changes.
 * 
 * Example:
<code>
JdbcObserver<Long> jdbcObserver = new JdbcObserver<Long>(...);
LazyObserver<Long> lazyObserver = new LazyObserver<Long>(jdbcObserver);
ProgressMonitor monitor = new ProgressMonitor(lazyObserver, ...);
</code>
 * 
 * @since 0.1
 * @author jbuhacoff
 */
public class LazyObserver<T> implements Observer<T> {
    private int previousValue = 0;
    private Observer<T> observer = null;
    
    public LazyObserver(Observer<T> observer) {
        this.observer = observer;
    }
    
    @Override
    public void observe(T value) {
        int currentValue = value.hashCode();
        if( currentValue == previousValue ) {
            return;
        }
        previousValue = currentValue;
        observer.observe(value);
    }
}
