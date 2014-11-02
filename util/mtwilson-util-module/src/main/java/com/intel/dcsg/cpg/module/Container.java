/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.module;

import com.intel.dcsg.cpg.module.annotations.Activate;
import com.intel.dcsg.cpg.module.annotations.Deactivate;
import com.intel.dcsg.cpg.module.annotations.Notice;
import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * The container is responsible for tracking modules, activating them, and deactivating them.
 *
 * @author jbuhacoff
 */
public class Container {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Container.class);
    private final HashSet<Module> modules = new HashSet<>();
    private final Whiteboard whiteboard = new Whiteboard();
//    private final HashMap<Object,ComponentHolder> components = new HashMap<Object,ComponentHolder>();
    
    public Set<Module> getModules() {
        return modules;
    }
    /*
    public ComponentHolder getComponent(Object object) {
        return components.get(object);
    }
*/

    public boolean isActive(Module module) {
        return module != null && modules.contains(module) && module.isActive();
    }
    
    public void register(Module module) {
        // adds module to our set of known modules, but does NOT automatically activate it
        modules.add(module);
    }
    public void unregister(Module module) {
        // removes module from the list of known modules ... must already be deactivated. we don't try to 
        // deactivate automatically.
        if( module.isActive() ) {
            throw new IllegalStateException("Module must be deactivated before unregistering");
        }
        modules.remove(module);
    }
    
    public void activate(Module module) throws ModuleActivationException {
        try {
            if( !modules.contains(module) ) {
                throw new IllegalStateException("Module must be registered before activating"); // XXX or we can just  modules.add(module) if it's not already there
            }
            if (module.isActive()) {
                return;
            }
            log.debug("Activating module {}", module.getImplementationTitle());
            // initialize the classloader for the module
            // module.setClassLoader(getModuleClassLoader(module)); // XXX TODO this was working for direcotyr-based deployment, neeed to move it into a DirectoryLauncher/DirectoryResolver like I did for MavenResolver and MavenLauncher
            // find and activate components in the  module...
            module.loadComponents();
            Collection<ComponentHolder> componentHolders = module.getComponentHolders();
            for (ComponentHolder componentHolder : componentHolders) {
                log.debug("Component: {}", componentHolder.getComponentName());
                activateComponent(module, componentHolder);
            }
        } catch (Exception e) {
            log.debug("Cannot activate module: {}", module.getImplementationTitle(), e);
            throw new ModuleActivationException("Cannot activate module: " + module.getImplementationTitle() + "-" + module.getImplementationVersion(), e);
        }
    }

    protected void activateComponent(Module module, ComponentHolder component) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
//        Object object = component.getWrappedObject();
        // 1) make connections to other activated components 
        // to keep things simple for now, we simply take our entire list of exports and try to connect all of them to this component.  anything it doesn't accept will be ignored.  later we might do something with component.getConnectTypes to maybe filter the list first, there might be a performance gain there but don't know right now if this is a hot spot
        connectExistingExportsToComponent(component);
        // 2) activate this component
        // XXX TODO  maybe  activating should just go inside the component holder ... nobody else needs a reference to that method!
        try {
            component.activate();
            component.export();
            // 3) get this components exports (tentative - right now the component has to accept a whiteboard connection and post by itself... or we should look for getters or @Export annotations ??)
            for(ExportHolder export : component.getExports()) {  // getExports() should be popualted by export() in the component holder, which we call immediately above this line
                whiteboard.register(export);
            }
            // 4) register this component with the whiteboard so it can start receiving events
            whiteboard.register(component);
            // 5) try to connect this component's exports to other components that accept them
            connectNewExportsToAllComponents(component.getExports());
            // 6) post an activation event indictating the component is now activated ... XXX TODO maybe we also want to post a ModulePreactivationEvent before we start... (step 0)
            whiteboard.post(new ModuleActivationEvent(module, component));
        }
        catch(Throwable e) {
            log.debug("Failed to activate component {} in module {}", component.getComponentName(), module.getImplementationTitle(), e);
            // when a component fails to activate, it should consider itself "deactivated" automatically with no further action by the container
            //whiteboard.post(new ComponentActivationFailureEvent(module, component));  // XXX tentative ...
            component.setError(e);            
        }
    }
    
    private void connectExistingExportsToComponent(ComponentHolder component) {
        for( ExportHolder exportHolder : whiteboard.getExports() ) {
            try {
                component.connect(exportHolder);
            }
            catch(ComponentConnectionException e) {
                // this lets us know that the component has a connect method matching thsi import but it threw an exception
                // we can either ignore that export/conencetion or abort the loading of the component or abort the loading of the modules.
                log.error("Cannot connect {} to {}", exportHolder.getExportName(), component.getComponentName());
            }
        }        
    }
    private void connectNewExportsToAllComponents(Set<ExportHolder> exports) {
            for(ExportHolder export : exports) {  // getExports() should be popualted by activate() in the component
                for(Module targetModule : modules) {
                    for(ComponentHolder targetComponentHolder : targetModule.getComponentHolders()) {
                        try {
                            targetComponentHolder.connect(export);
                        }
                        catch(ComponentConnectionException e) {
                            // this lets us know that the component has a connect method matching thsi import but it threw an exception
                            // we can either ignore that export/conencetion or abort the loading of the component or abort the loading of the modules.
                            log.error("Cannot connect {} to {}", export.getExportName(), targetComponentHolder.getComponentName());
                        }
                    }
                }
            }        
    }
    private void disconnectExportsFromAllComponents(Set<ExportHolder> exports) {
            for(ExportHolder export : exports) {  // getExports() should be popualted by activate() in the component
                for(Module targetModule : modules) {
                    for(ComponentHolder targetComponentHolder : targetModule.getComponentHolders()) {
                        try {
                            targetComponentHolder.disconnect(export);
                        }
                        catch(ComponentDisconnectionException e) {
                            // this lets us know that the component has a connect method matching thsi import but it threw an exception
                            // we can either ignore that export/conencetion or abort the loading of the component or abort the loading of the modules.
                            log.error("Cannot disconnect {} from {}", export.getExportName(), targetComponentHolder.getComponentName());
                        }
                    }
                }
            }        
    }

    public void deactivate(Module module) throws ModuleDeactivationException {
        try {
            if (!modules.contains(module)) {
                throw new IllegalArgumentException("Module is not registered");
            }
            if (!module.isActive()) {
                return;
            }
            log.debug("Deactivating module: {}", module.getImplementationTitle());
            // find and deactivate components in the  module
            Set<ComponentHolder> moduleComponents = module.getComponentHolders();
            for (ComponentHolder component : moduleComponents) {
                log.debug("Component: {} isActive? {}", component.getComponentName(), component.isActive());
                if( component.isActive() ) {
                    deactivateComponent(module, component);
                }
                // XXX TODO do we need to unlink things?  set module, wrappedObject, and filter = null ?
            }
            moduleComponents.clear();
            // release the classloader for the module
            module.setClassLoader(null);

        } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            throw new ModuleDeactivationException("Cannot deactivate module: " + module.getImplementationTitle() + "-" + module.getImplementationVersion(), e);

        }
    }

    protected void deactivateComponent(Module module, ComponentHolder component) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
//        Object object = component.getWrappedObject();
        // 6) post a deactivation event to notify other components this  component is about to be deactivated ... XXX maybe this should be ModulePredeactivationEvent, and post ModuleDeactivationEvent when done... or maybe leave this one as-is and post ModulePostDeactivationEvent when done... whereas the rule for that one is you can't do anything with the reference because its deactivated)
        whiteboard.post(new ModuleDeactivationEvent(module, component));

        // 5) disconnect this compoennts exports from all other components
        // before deactivating the component, disconnect its exports from all other components
        disconnectExportsFromAllComponents(component.getExports());

        
        // 4) unregister this component with the whiteboard so it will stop receiving events
        whiteboard.unregister(component);
        try {
        // 3) get this components exports (tentative - right now the component has to accept a whiteboard connection and post by itself... or we should look for getters or @Export annotations ??)
            for(ExportHolder export : component.getExports()) {  // getExports() should be popualted by activate() in the component
                whiteboard.unregister(export);
            }
            
        // 2) deactivate this component... must happen AFTER everything else because at this point the component may reset its references to null so it would be dangerous to try passing it to any other objects after this.
            component.deactivate();
            
            // 1) there is no deactivation analog to  making connections, because it's not necessary... we're already disconnected this component from other components above (step 5),  no need tod isconnect them from this one because it's deactivated.
        }
        catch(Throwable e) {
            log.debug("Failed to deactivate component {} in module {}", component.getComponentName(), module.getImplementationTitle(), e);
            component.setError(e);
        }
    }

    
    /**
     * activates all registered modules
     * 
     * @throws Exception 
     */
    public void start() throws ContainerException {
        for(Module module : modules) {
            activate(module);
        }
    }
    
    /**
     * deactivates all registered modules
     * 
     * @throws Exception 
     */
    public void stop() throws ContainerException {
        for(Module module : modules) {
            if( module.isActive() ) {
                deactivate(module);
            }
        }
    }
}
