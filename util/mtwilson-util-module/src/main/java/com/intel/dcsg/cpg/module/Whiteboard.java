/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.module;

import com.intel.mtwilson.pipe.Filter;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Whiteboard class keeps track of service consumers and service providers, automatically notifying consumers when a
 * provider they requested is activated and deactivated.
 *
 * XXX TODO:
 * Consumers and providers must be automatically segregated by version - a consumer will not receive notifications for a
 * provider that implements an incompatible version of the interface.   (this might already be automatically handled
 * by the way we find the notification method, using java's "isAssignableFrom" method to check type compatibility)
 *
 * @author jbuhacoff
 */
public class Whiteboard {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Whiteboard.class);
    // XXX maybe instead of keeping our own global lists we just need to have access to the container... query for available modules, query each one for components...
    private final HashSet<ComponentHolder> components = new HashSet<ComponentHolder>(); // this is an application-global list, whereas each Module only keeps track of its own components
    private final HashSet<ExportHolder> exports = new HashSet<ExportHolder>(); // this is an application-global list, whereas each Component only keeps track of its own exports

    public Set<ExportHolder> getExports() {
        return exports;
    }
            
    
    public void register(ComponentHolder component) {
        components.add(component);
        
        // XXX see also the note next to WhiteboardProxy ... this is a special connection we're doing for the whiteboard proxy, since it's not provided by another component.... it's the same code as in register(exportholder) except we don't register this export of the whiteboard proxy
        try {
            component.connect(new ExportHolder(new WhiteboardProxy(this, component), component));
            // XXX TODO  should we check if the connection succeeded ... so we can maintain a map of connected objects? that would make it (maybe) faster to  know which components need to be deactivated when their dependencies are deactivaited... at the cost of a little memory to maintain the map
        }
        catch(ComponentConnectionException e) {
            log.error("Cannot connect {} with whiteboard proxy",component.getComponentName(), e);    
        }
        
    }

    public void unregister(ComponentHolder component) {
        components.remove(component);
    }

    /**
     * Note that objects that send a message will also receive it (bounce back) from the whiteboard if they
     * have a matching @Notice method.
     * @param event 
     */
    public void post(/*NoticeHolder*/Object event) {
        log.debug("Whiteboard event: {}", event/*.getWrappedObject()*/.getClass().getName());
        // notify everybody who is listening for an event like this
        for (ComponentHolder componentHolder : components) {
            try {
                componentHolder.notice(event);
            }
            catch(ComponentNotificationException e) {
                log.error("Cannot notify {} with message {}", componentHolder.getComponentName(), event/*.getWrappedObject()*/.getClass().getName(), e);                
            }
        }
    }
    
    // XXX TODO /// /  RIGHT NOW THERE IS NOTHING TO CALL REGISTER(EXPORT) AND UNREGISTER(EXPORT) .... SHOULD WE LET THE COMPONENTS DO THAT THEMSELVES? SO WE NEED TO GIVE THEM A WHITEBOARD COMPONENT AS A PROXY FOR THIS...
    
    // XXX TODO we wouldn't have to do the 2-step list&add if the componentholder itself would just do a no-exception connect... connect if possible, otherwise ignore it.  the list is not even useful to us after this method ebcause you can't assume that a connect() implies a notice().
    public void register(ExportHolder export) {
//        export.getComponent().getExports().add(export); // the component handles its own list; we just add it to the whiteboard
        exports.add(export);
        // XXX TODO post added export event to whiteboard?
        for(ComponentHolder component : components) {
            try {
                // NOTE: this is a post-activation connection for these components that are already activated
                // this is a best-effort basis... the component holder will check if the componen accepts the type of this export, and if it doesn't then it will be ignored
                component.connect(export);
                // XXX TODO  should we check if the connection succeeded ... so we can maintain a map of connected objects? that would make it (maybe) faster to  know which components need to be deactivated when their dependencies are deactivaited... at the cost of a little memory to maintain the map
            }
            catch(ComponentConnectionException e) {
                log.error("Cannot connect {} with export {}",component.getComponentName(), export.getExportName(), e);    
            }
        }        
    }
    
    public void unregister(ExportHolder export) throws ComponentActivationException, ComponentDeactivationException {
        // find all components that were connected to this export... make a list
        HashSet<ComponentHolder> connected = new HashSet<ComponentHolder>(); // XXX maybe during registration what we should be tracking is a map of export -> components  instead of just a global set; we can get the globals et through the container but nobody else would have this map
        for(ComponentHolder component : components) {
            if( component.getImports().contains(export)) {
                connected.add(component);
            }
        }
        for(ComponentHolder component : connected) {
            component.deactivate(); // throws ComponentDeactivationException ... XXX TODO should we catch this here and continue deactivating other components? by throwing the exception here we are giving up .... how will the container recover from this?
        }
        // now deactivate all components that were connected to this export 
        // XXX TODO post removed export event to whiteboard?
        exports.remove(export);
//        export.getComponent().getExports().remove(export); //  let the component handle its own list
        // now reactivate all components that were connected to this export... this time they wont' get the export we just unregistered
        for(ComponentHolder component : connected) {
            component.activate(); // throws ComponentActivationException ... XXX TODO should we catch this here and continue deactivating other components? by throwing the exception here we are giving up .... how will the container recover from this?
        }
    }
    
    // XXX right now the whiteboard proxy is special because we try to connect it to new components if they have a connector, but we don't register it as a component on the whiteboard (because we are teh whiteboard) - not sure if this should stay special or if we should register a factory like a regular component; the difference here is that we want to embed the component holder in the proxy so if we need to log debug info we know which component is using it...
    public static class WhiteboardProxy {
        private Whiteboard whiteboard;
        private ComponentHolder component;
        protected WhiteboardProxy(Whiteboard whiteboard, ComponentHolder component) {
            this.whiteboard = whiteboard;
            this.component = component;
        }
        /**
         * Export objects that should be available for connecting to other components as they are activated (these objects remain on the whiteboard)
         * @param object 
         */
        public void export(Object object) {
            ExportHolder exportHolder = new ExportHolder(object, component);
            whiteboard.register(exportHolder);
        }
        /**
         * Post messages that should be broadcast to interested components but will not be kept by the whiteboard after dissemination
         * @param message 
         */
        public void post(Object message) {
            /*NoticeHolder noticeHolder = new NoticeHolder(message, component);*/
            whiteboard.post(/*noticeHolder*/message);
        }
    }
}
