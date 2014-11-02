/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.extensions;

import java.util.Collection;
import java.util.Iterator;

/**
 *
 * @author jbuhacoff
 */
public class Scanner {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Scanner.class);
    private Registrar[] registrars;
    private boolean throwExceptions = false; // by default we'll skip any ClassNotFound and continue scanning for what is available
    private boolean throwErrors = false; // by default we'll skip any NoClassDefFoundError and continue scanning for what is available

    public Scanner() {
    }

    public Scanner(Registrar... registrars) {
        this.registrars = registrars;
    }

    private void process(Class<?> clazz) {
        // ignore interfaces because they cannot be instantiated and therefore cannot be extensions themselves
        if( clazz.isInterface() ) {
            return;
        }
        for (int i = 0; i < registrars.length; i++) {
            Registrar registrar = registrars[i];
            log.debug("Processing {} with registrar {}", clazz.getName(), registrar.getClass().getName());
            try {
                if (registrar.accept(clazz)) {
                    log.debug("Auto-registered {} with {}", clazz.getName(), registrar.getClass().getName());
                }
            } catch (RuntimeException e) { // could be ClassNotFoundException or NoClassDefFoundError
                log.debug("Cannot evaluate class {}: {}", clazz.getName(), e.getClass().getName());
                if (throwExceptions) {
                    throw e;
                }
            } catch (Error e) {
                log.debug("Cannot evaluate class {}: {}", clazz.getName(), e.getClass().getName());
                if (throwErrors) {
                    throw e;
                }
            }
        }
    }

    public void scan(Class<?>... clazzes) {
        for (Class<?> clazz : clazzes) {
            process(clazz);
        }
    }

    public void scan(Collection<Class<?>> clazzes) {
        for (Class<?> clazz : clazzes) {
            process(clazz);
        }
    }

    public void scan(Iterator<Class<?>> clazzes) {
        while (clazzes.hasNext()) {
            Class<?> clazz = clazzes.next();
            process(clazz);
        }
    }

    public Registrar[] getRegistrars() {
        return registrars;
    }

    public void setRegistrars(Registrar[] registrars) {
        this.registrars = registrars;
    }

    public boolean isThrowExceptions() {
        return throwExceptions;
    }

    public void setThrowExceptions(boolean throwExceptions) {
        this.throwExceptions = throwExceptions;
    }

    public boolean isThrowErrors() {
        return throwErrors;
    }

    public void setThrowErrors(boolean throwErrors) {
        this.throwErrors = throwErrors;
    }
}
