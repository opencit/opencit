/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.extensions;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author jbuhacoff
 */
public class Scanner {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Scanner.class);
    private Registrar[] registrars;
    private boolean throwExceptions = false; // by default we'll skip any ClassNotFound and continue scanning for what is available
    private boolean throwErrors = false; // by default we'll skip any NoClassDefFoundError and continue scanning for what is available
    private List<String> includePackages = null; // when null, will accept implementations in any package; when set, will only accept implementations in specified packages
    private List<String> excludePackages = null; // when null, will accept implementations in any package; when set, will exclude implementations in specified packages (overrides includePackages so can be used to exclude specific portion of included package)

    public Scanner() {
    }

    public Scanner(Registrar... registrars) {
        this.registrars = registrars;
    }

    public void setIncludePackages(List<String> includePackages) {
        this.includePackages = includePackages;
    }

    public void setExcludePackages(List<String> excludePackages) {
        this.excludePackages = excludePackages;
    }

    private void process(Class<?> clazz) {
        // ignore interfaces and abstract classes because they cannot be instantiated and therefore cannot be extensions themselves
        if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
            return;
        }
        if (includePackages == null || startsWithAny(clazz.getName(), toPackagePrefixes(includePackages))) {
            if (excludePackages == null || !startsWithAny(clazz.getName(), toPackagePrefixes(excludePackages))) {

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

    private boolean startsWithAny(String test, List<String> prefixes) {
        for (String prefix : prefixes) {
            if (test.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    // the prefixes are like "java", "javax", "com.intel", etc.
    // we return the same prefixes with a "." at the end so they become
    // "java.", "javax.", "com.intel.", etc. 
    private List<String> toPackagePrefixes(List<String> prefixes) {
        ArrayList<String> packagePrefixes = new ArrayList<>();
        for (String prefix : prefixes) {
            packagePrefixes.add(prefix + ".");
        }
        return packagePrefixes;
    }
}
