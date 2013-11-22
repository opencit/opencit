/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jmod;

import com.intel.dcsg.cpg.util.Filter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * The ComponentHolder wraps Component classes and stores additional information about them such as a reference to the
 * activate and deactivate methods, a list of types for which they implement a connect method (either by convention or
 * with the
 *
 * @Connect annotation), a list of types they export (we look for
 * @Export annotations or methods that look like getters "get*" or factories "create*").
 *
 * @author jbuhacoff
 */
public class ComponentHolder {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ComponentHolder.class);

    private Object wrappedObject; // XXX  consider making this final and removing the setter
    private Module module; // XXX  consider making this final and removing the setter
    private Method activateMethod; // XXX  consider making this final and removing the setter
    private Method deactivateMethod; // XXX  consider making this final and removing the setter
    private Set<Class<?>> noticeTypes; // XXX  consider making this final and removing the setter ... and makign it a map of type -> method  so we don't have to search fo rthe method ...
    private Set<Class<?>> connectTypes; // XXX  consider making this final and removing the setter
    private boolean active = false;
    private Throwable error = null;
    private final HashSet<ExportHolder> exports = new HashSet<ExportHolder>();
    private final HashSet<ExportHolder> imports = new HashSet<ExportHolder>(); // these exports are from other components, connected this component via @Connect or connect(Object) during activation

    public ComponentHolder(Object wrappedObject, Module module) {
        this.wrappedObject = wrappedObject;
        this.module = module;
    }

    public Module getModule() {
        return module;
    }

    public void setModule(Module module) {
        this.module = module;
    }

    public Object getWrappedObject() {
        return wrappedObject;
    }

    public void setWrappedObject(Object wrappedObject) {
        this.wrappedObject = wrappedObject;
    }

    public boolean isActive() {
        return active;
    }

    public Throwable getError() {
        return error;
    }

    public void setError(Throwable error) {
        this.error = error;
    }

    public String getComponentName() {
        return wrappedObject.getClass().getName();
    }

    public HashSet<ExportHolder> getExports() {
        return exports;
    }

    public HashSet<ExportHolder> getImports() {
        return imports;
    }

    // NOTE:  if a component implements  connect(A) and connect(B) where A is a subclass of B,  only A (the best match) will be called for a component that exports A ... we only connect the best match.  
    public void connect(ExportHolder export) throws ComponentConnectionException {
        // first we find the most specific type we can connect to
        log.debug("Checking if {} accepts connection to {}", getComponentName(), export.getExportName());
        Class<?> bestConnect = ReflectionUtil.getMostSpecificType(export.getWrappedObject().getClass(), connectTypes);
        if (bestConnect == null) {
            return;
        } // no match
        // find the connect method - must be one since we made the connectTypes list by looking at available methods
        Method connectMethod = ReflectionUtil.getConnectMethodForType(wrappedObject.getClass(), bestConnect);
        if (connectMethod != null) {
            try {
                log.debug("Connecting {} to {}", getComponentName(), export.getExportName());
                connectMethod.invoke(wrappedObject, export.getWrappedObject()); // throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
            } catch (Exception e) {
                throw new ComponentConnectionException(e);
            }
        }
    }

    // NOTE:  if a component implements  connect(A) and connect(B) where A is a subclass of B,  only A (the best match) will be called for a component that exports A ... we only connect the best match.  
    public void disconnect(ExportHolder export) throws ComponentDisconnectionException {
        // first we find the most specific type we can connect to
        log.debug("Checking if {} accepts connection to {}", getComponentName(), export.getExportName());
        Class<?> bestConnect = ReflectionUtil.getMostSpecificType(export.getWrappedObject().getClass(), connectTypes);
        if (bestConnect == null) {
            return;
        } // no match
        // find the disconnect method - there may not be one because implementing disconnect() is optional... however if it's implemented its type should match the corresponding connect method. 
        Method disconnectMethod = ReflectionUtil.getDisconnectMethodForType(wrappedObject.getClass(), bestConnect);
        if (disconnectMethod != null) {
            try {
                log.debug("Disconnecting {} from {}", getComponentName(), export.getExportName());
                disconnectMethod.invoke(wrappedObject, export.getWrappedObject()); // throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
            } catch (Exception e) {
                throw new ComponentDisconnectionException(e);
            }
        }
    }
    
    // NOTE:  if a component implements  notice(A) and notice(B) where A is a subclass of B,  only A (the best match) will be called for a component that exports A ... we only notify the best match.      
    // XXX TODO do we need to create a NoticeHolder object and pass that around instead?
    public void notice(/*NoticeHolder*/Object message) throws ComponentNotificationException {
        // first we find the most specific type we can connect to
        log.debug("Checking if {} accepts notice for {}", getComponentName(), message.getClass().getName());
        Class<?> bestNotice = ReflectionUtil.getMostSpecificType(message/*.getWrappedObject()*/.getClass(), noticeTypes);
        if (bestNotice == null) {
            return;
        } // no match
        // find the notice method - must be one since we made the noticeTypes list by looking at available methods
        Method noticeMethod = ReflectionUtil.getConnectMethodForType(wrappedObject.getClass(), bestNotice);
        if (noticeMethod != null) {
            try {
                log.debug("Notifying {} about {}", getComponentName(), message.getClass().getName());
                noticeMethod.invoke(wrappedObject, message/*.getWrappedObject()*/); // throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
            } catch (Exception e) {
                throw new ComponentNotificationException(e);
            }
        }
    }

    public void export() throws ComponentExportException {
        if( !active ) { return; }
        exports.clear();
        Set<Method> exportMethods = ReflectionUtil.getExportMethods(wrappedObject.getClass());
        for (Method exportMethod : exportMethods) {
            try {
                error = null; // XXX TODO maybe we should be keeping a list instead of just one? then container can know all of them, and clear when it wants to...
                Object exportObject = exportMethod.invoke(wrappedObject);
                exports.add(new ExportHolder(exportObject, this));
            } catch (Exception e) {
                throw new ComponentExportException(e);// chain will be ComponentDeactivationException -> IllegalAccessException or IllegalArgumentException
            }
        }
    }

    /*
     public Method getActivateMethod() {
     return activateMethod;
     }
     */
    public void activate() throws ComponentActivationException {
        if (active) {
            return;
        }
        if (activateMethod == null) {
            active = true;
            return;
        }
        try {
            error = null; // XXX TODO maybe we should be keeping a list instead of just one? then container can know all of them, and clear when it wants to...
            activateMethod.invoke(wrappedObject);
            active = true;
        } catch (Exception e) {
            throw new ComponentActivationException(e);// chain will be ComponentDeactivationException -> IllegalAccessException or IllegalArgumentException
        }
    }

    public void setActivateMethod(Method activateMethod) {
        this.activateMethod = activateMethod;
    }

    /*
     public Method getDeactivateMethod() {
     return deactivateMethod;
     }
     */
    public void deactivate() throws ComponentDeactivationException {
        if (!active) {
            return;
        }
        if (deactivateMethod == null) {
            active = false;
            return;
        }
        try {
            error = null; // XXX TODO maybe we should be keeping a list instead of just one? then container can know all of them, and clear when it wants to...
            deactivateMethod.invoke(wrappedObject);
            active = false;
        } catch (Exception e) {
            throw new ComponentDeactivationException(e);// chain will be ComponentDeactivationException -> IllegalAccessException or IllegalArgumentException
        }
    }

    public void setDeactivateMethod(Method deactivateMethod) {
        this.deactivateMethod = deactivateMethod;
    }

    public Set<Class<?>> getNoticeTypes() {
        return noticeTypes;
    }

    public void setNoticeTypes(Set<Class<?>> noticeTypes) {
        this.noticeTypes = noticeTypes;
    }

    public Set<Class<?>> getConnectTypes() {
        return connectTypes;
    }

    public void setConnectTypes(Set<Class<?>> connectTypes) {
        this.connectTypes = connectTypes;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!getClass().equals(obj.getClass())) {
            return false;
        }
        return wrappedObject.equals(((ComponentHolder) obj).wrappedObject);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 43 * hash + (this.wrappedObject != null ? this.wrappedObject.hashCode() : 0);
        return hash;
    }
}
