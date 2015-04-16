/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.extensions;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.ListUtils;

/**
 * This class is maintains a singleton whiteboard with registered
 * extensions. 
 * 
 * @author jbuhacoff
 */
public class WhiteboardExtensionProvider implements ExtensionProvider {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WhiteboardExtensionProvider.class);
    private static final HashMap<String, List<Class<?>>> whiteboard = new HashMap<>(); // complete list of available implementations for each interface, in preference order (sorted when preferences change and when plugins are added)
    private static final HashMap<String, List<String>> preferences = new HashMap<>(); // optional application-declared implementation preferences for each interface

    /**
     * Clears all registrations
     */
    public static void clearAll() {
        whiteboard.clear();
        log.info("Cleared all implementations for all interfaces");
    }
    
    private static void clear(String serviceName) {
        List<Class<?>> serviceImplementations = whiteboard.get(serviceName);
        if( serviceImplementations != null ) {
            serviceImplementations.clear();
        }
    }    
    
    /**
     * Clears registrations for the specified interface class, abstract class,
     * or annotation.
     * @param serviceInterface 
     */
    public static void clear(Class<?> serviceInterface) {
        clear(serviceInterface.getName());
        log.info("Cleared implementations for interface {}", serviceInterface.getName());
    }
    public static void clearAnnotated(Class<? extends Annotation> annotationInterface) {
        clear(annotationInterface.getName());
        log.info("Cleared implementations for annotation {}", annotationInterface.getName());
    }
    
    /**
     * Registers serviceImplementation as an implementation of serviceInterface.
     * 
     * @param serviceName can be an interface class name, abstract class name, or annotation name
     * @param serviceImplementation any class that implements or extends serviceInterface or is annotated with serviceInterface
     */
    private static void register(String serviceName, Class<?> serviceImplementation) {
        log.debug("Registering implementation {} for interface {}", serviceImplementation.getName(), serviceName);
        // first, unregister the implementation to ensure we're not registering a duplicate
        synchronized(whiteboard) {
            List<Class<?>> serviceImplementations = whiteboard.get(serviceName);
            if( serviceImplementations == null ) {
                serviceImplementations = new ArrayList<Class<?>>();
                whiteboard.put(serviceName, serviceImplementations);
            }
            // check if the implementation is already registered
            if( serviceImplementations.contains(serviceImplementation) ) {
                log.debug("Implementation {} is already registered for interface {}", serviceImplementation.getName(), serviceName);
            }
            else {
                serviceImplementations.add(serviceImplementation); // no preferences at all or no preference for this implementation, so just add it at the end
            }
            // now we need to find the best index to add this implementation - if optional preferences are defined it needs to match them
            final List<String> preferenceOrder = preferences.get(serviceName);
            if( preferenceOrder != null && preferenceOrder.contains(serviceImplementation.getName())) {
                // we have preferences for this interface, so place it accordingly - first we add it to the end then we sort based on preferences and bubble it up as needed
                Collections.sort(serviceImplementations, new PreferenceComparator(preferenceOrder));
            }
        }
        log.debug("Registered implementation {} for interface {}", serviceImplementation.getName(), serviceName);
    }
    
    /**
     * Registers serviceImplementation as an implementation of serviceInterface.
     * @param <T>
     * @param serviceInterface can be an interface class or abstract class
     * @param serviceImplementation any class that implements or extends serviceInterface
     */
    public static <T> void register(Class<T> serviceInterface, Class<?> serviceImplementation) {
        if( !serviceInterface.isAssignableFrom(serviceImplementation)) {
            throw new IllegalArgumentException(String.format("Class %s does not implement %s", serviceImplementation.getName(), serviceInterface.getName()));
        }
        register(serviceInterface.getName(), serviceImplementation);
    }

    public static void registerAnnotated(Class<? extends Annotation> annotationInterface, Class<?> serviceImplementation) {
        if( !ReflectionUtil.isAnnotatedClass(serviceImplementation, annotationInterface)) {
            throw new IllegalArgumentException(String.format("Class %s is not annotated with %s", serviceImplementation.getName(), annotationInterface.getName()));
        }
        register(annotationInterface.getName(), serviceImplementation);
    }

    private static void unregister(String serviceName, Class<?> serviceImplementation) {
        log.debug("Unregistering implementation {} for interface {}", serviceImplementation.getName(), serviceName);
        synchronized(whiteboard) {
            List<Class<?>> serviceImplementations = whiteboard.get(serviceName);
            if( serviceImplementations == null ) {
                return;
            }
            Iterator<Class<?>> it = serviceImplementations.iterator();
            while(it.hasNext()) {
                Class<?> clazz = it.next();
                if( clazz == serviceImplementation ) {
                    it.remove();
                    log.debug("Unregistered implementation {} for interface {}", serviceImplementation.getName(), serviceName);
                }
            }
        }
    }
    
    public static void unregister(Class<?> serviceInterface, Class<?> serviceImplementation) {
        if( !serviceInterface.isAssignableFrom(serviceImplementation)) {
            throw new IllegalArgumentException(String.format("Class %s does not implement %s", serviceImplementation.getName(), serviceInterface.getName()));
        }
        unregister(serviceInterface.getName(), serviceImplementation);
    }

    public static void unregisterAnnotated(Class<? extends Annotation> annotationInterface, Class<?> serviceImplementation) {
        if( !ReflectionUtil.isAnnotatedClass(serviceImplementation, annotationInterface)) {
            throw new IllegalArgumentException(String.format("Class %s is not annotated with %s", serviceImplementation.getName(), annotationInterface.getName()));
        }
        unregister(annotationInterface.getName(), serviceImplementation);
    }

    // returns an instance of the given implementation class or null if it could not be created
    private static Object createInstance(Class<?> serviceImplementation) {
        Constructor constructor = ReflectionUtil.getNoArgConstructor(serviceImplementation);
        if( constructor == null ) {
            log.debug("Implementation {} does not have no-arg constructor", serviceImplementation.getName());
            return null; // implementation does not have a constructor for the given context  
        }
        try {
            Object instance = constructor.newInstance();
            return instance;
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
            log.debug("Cannot instantiate implementation class {}", serviceImplementation.getName(), e);
            return null;
        }
    }
    
    @Deprecated
    private static <C> Object createInstanceWithContext(Class<?> serviceImplementation, C context) {
        if( context == null ) { throw new NullPointerException(); }
        Constructor constructor = ReflectionUtil.getOneArgConstructor(serviceImplementation, context.getClass());
        if( constructor == null ) {
            log.debug("Implementation {} does not support context {}", serviceImplementation.getName(), context.getClass().getName());
            return null; // implementation does not have a constructor for the given context  
        }
        try {
            Object instance = constructor.newInstance(context);
            return instance;
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
            log.debug("Cannot instantiate implementation class {}", serviceImplementation.getName(), e);
            return null;
        }
    }

    // returns an instance of the given implementation class cast to the given interface, or null if it could not be created or cast
    @Deprecated
    private static <T,C> T create(Class<T> returnType, Class<?> serviceImplementation, C context) {
        Object instance;
        if( context == null ) {
            instance = createInstance(serviceImplementation);
        }
        else {
            instance = createInstanceWithContext(serviceImplementation, context);
        }
        try {
            T castInstance = (T) instance;
            return castInstance;
        } catch (ClassCastException e) {
            log.debug("Cannot cast service class {} to {}", serviceImplementation.getName(), returnType.getName(), e);
            return null;
        }
    }
    /*
    private static <T> T createFirst(Class<T> serviceInterface, List<Class<?>> serviceImplementations) {
        for(Class<?> item : serviceImplementations) {
            T instance = create(serviceInterface, item);
            if( instance != null ) {
                return instance;
            }
        }
        return null;
    }
    */
    
    @Deprecated
    private static <T,C> T createFirst(Class<T> returnType, List<Class<?>> serviceImplementations, C context) {
        for(Class<?> item : serviceImplementations) {
            T instance = create(returnType, item, context);
            if( instance != null ) {
                return instance;
            }
        }
        return null;
    }
    
    /*
    private static <T> List<T> createAll(Class<T> serviceInterface, List<Class<?>> serviceImplementations) {
        ArrayList<T> instances = new ArrayList<T>();
        for(Class<?> item : serviceImplementations) {
            T instance = create(serviceInterface, item);
            if( instance != null ) {
                instances.add(instance);
            }
        }
        return instances;
    }
    */
    
    @Deprecated
    private static <T,C> List<T> createAll(Class<T> returnType, List<Class<?>> serviceImplementations, C context) {
        ArrayList<T> instances = new ArrayList<>();
        for(Class<?> item : serviceImplementations) {
            T instance = create(returnType, item, context);
            if( instance != null ) {
                instances.add(instance);
            }
        }
        return instances;
    }
    
    /*
    private static <T> T createPreferred(Class<T> serviceInterface, List<Class<?>> serviceImplementations, List<String> preferenceOrder) {
        return createPreferred(serviceInterface, serviceImplementations, preferenceOrder, null);
    }
    */

   

    private static <T> void prefer(String serviceName, List<String> preferenceOrder) {
        preferences.put(serviceName, preferenceOrder);
    }
//    //unused, can be added back later: klocwork 87
//    private static <T> void prefer(String serviceName, String[] preferenceOrder) {
//        preferences.put(serviceName, Arrays.asList(preferenceOrder));
//    }
    public static <T> void prefer(Class<T> serviceInterface, List<String> preferenceOrder) {
        prefer(serviceInterface.getName(), preferenceOrder);
    }
    public static <T> void prefer(Class<T> serviceInterface, String[] preferenceOrder) {
        prefer(serviceInterface.getName(), Arrays.asList(preferenceOrder));
    }

    
    
    @Deprecated
    private static <T,C> T createPreferred(Class<T> returnType, List<Class<?>> serviceImplementations, List<String> preferenceOrder, C context) {
        for(String preference : preferenceOrder) {
            log.debug("Looking for implementation with preference {}", preference);
            for(Class<?> serviceImplementation : serviceImplementations) {
                log.debug("Checking implementation {}", serviceImplementation.getName());
                if( preference.equals(serviceImplementation.getName())) {
                    log.debug("Found implementation {} for interface {}", serviceImplementation.getName(), returnType.getName());
                    T instance = create(returnType, serviceImplementation, context);
                    if( instance != null ) {
                        return instance;
                    }
                }
            }
        }
        // did not find one matching the preference ;so pick any one
        return createFirst(returnType, serviceImplementations, context);
    }

    
    @Deprecated
    private static <T,C> T find(Class<T> returnType, Class<?> serviceInterface, C context) {
        String serviceName = serviceInterface.getName();
        List<Class<?>> serviceImplementations = whiteboard.get(serviceName);
        if (serviceImplementations == null || serviceImplementations.isEmpty()) {
            return null;
        }
        List<String> preferenceOrder = preferences.get(serviceName);
        if (preferenceOrder == null || preferenceOrder.isEmpty() ) {
            // no preference as to which implementation is desirable
            log.debug("No preference for service {}", serviceName);
//            return createFirst(serviceClass, matches);
            preferenceOrder = ListUtils.EMPTY_LIST;
        }
        log.debug("There are {} preferences for service {}", preferenceOrder.size(), serviceName);
        return createPreferred(returnType, serviceImplementations, preferenceOrder, context);
    }
    @Deprecated
    private static <T, C> List<T> findAll(Class<T> returnType, Class<?> serviceInterface, C context) {
        String serviceName = serviceInterface.getName();
        List<Class<?>> serviceImplementations = whiteboard.get(serviceName);
        if (serviceImplementations == null || serviceImplementations.isEmpty()) {
            log.debug("No registered implementations for {}", serviceName);
            serviceImplementations = ListUtils.EMPTY_LIST;
        }
        return createAll(returnType, serviceImplementations, context);
    }

    private static List<String> findAll(Class<?> serviceInterface) {
        String serviceName = serviceInterface.getName();
        List<Class<?>> serviceImplementations = whiteboard.get(serviceName);
        if (serviceImplementations == null || serviceImplementations.isEmpty()) {
            log.debug("No registered implementations for {}", serviceName);
            serviceImplementations = ListUtils.EMPTY_LIST;
        }
        //return createAll(returnType, serviceImplementations, context);
        ArrayList<String> classNames = new ArrayList<>();
        for(Class<?> clazz : serviceImplementations) {
            classNames.add(clazz.getName());
        }
        return classNames;
    }
    
    public static Map<String,List<String>> getPreferences() {
        return Collections.unmodifiableMap(preferences);
    }
    public static Map<String,List<Class<?>>> getWhiteboard() {
        return Collections.unmodifiableMap(whiteboard);
    }

    @Override
    public void reload() {
//        throw new UnsupportedOperationException("Not supported yet.");
        /**
         * Because the whiteboard is passive, and any component can register
         * extensions on the whiteboard, what we need here is a mechanism
         * for those other components to opt-in and register themselves
         * as well in a separate list of Runnables maybe that we can call
         * on reload in order to do whatever they do again and re-register
         * all the extensions (in case there's a change - because reload
         * would be triggered by a feature/extensions manager probably or
         * manually by the user after adding/removing/upgrading somthing)
         */
    }

    @Override
    public Iterator<String> find(Class<?> extension) {
//        Collection<?> all = findAll(extension, extension, null);
        Collection<String> all = findAll(extension);
        log.debug("find extension {} returning {} items", extension.getName(), all.size());
//        ServiceLoaderExtensionProvider.ClassNameIterator it = new ServiceLoaderExtensionProvider.ClassNameIterator(all.iterator());
        return all.iterator();
    }

    @Override
    public Iterator<String> findAnnotated(Class<? extends Annotation> annotation) {
        Collection<?> all = findAll(Object.class, annotation, null);
        ServiceLoaderExtensionProvider.ClassNameIterator it = new ServiceLoaderExtensionProvider.ClassNameIterator(all.iterator());
        return it;
    }

    @Override
    public <T> T create(Class<T> extension, String name) throws ExtensionNotFoundException {
        try {
            Class<?> clazz = Class.forName(name);
            T instance = (T) createInstance(clazz);
            return instance;
        }
        catch(Exception e) {
            throw new ExtensionNotFoundException(name, e);
        }
    }

    @Override
    public Object createAnnotated(Class<? extends Annotation> annotation, String name) throws ExtensionNotFoundException {
        try {
            Class<?> clazz = Class.forName(name);
            Object instance = createInstance(clazz);
            return instance;
        }
        catch(Exception e) {
            throw new ExtensionNotFoundException(name, e);
        }
    }
    
}
