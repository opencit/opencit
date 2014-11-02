/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.extensions;

import com.intel.mtwilson.pipe.Filter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.ListUtils;

/**
 * An alternative for the ServiceLoader from Java 6.
 *
 * Differences from the Java 6 ServiceLoader:
 *
 * 1. ServiceLoader requires the developer to create text files under
 * META-INF/services/interface with the names of implementing classes;
 * Whiteboard scans the classpath to find implementations of a given interface
 * (this is slower but more convenient); Whiteboard can also read the same
 * META-INF/services/interface files but it will then use that information to
 * look for the mentioned classes and only handle classes that are found and
 * implement the interface that was being sought; 2. ServiceLoader will throw a
 * ServiceConfigurationError if the classes mentioned under
 * META-INF/services/interface are not found or do not implement the given
 * interface; Whiteboard only works with classes that exist and implement the
 * required interface, thus avoiding the need to catch these exceptions 3.
 * ServiceLoader always returns a lazy iterator to look through available
 * implementations one at a time; Whiteboard allows callers to request either
 * exactly one implementation (error if more than one is found), or request all
 * implementations, or request implementations that match specific criteria.
 * 
 * When using a criteria-based search, factories are still searched in order
 * of preference but if a preferred factory does not accept the criteria it
 * is skipped and the next one is checked.
 * 
 * When using a criteria-based search, factories that do not implement the
 * create(context) method are assumed to not accept it and are skipped.
 * 
 * !!! TODO !!! because of the requirement to find annotated classes by annotation
 * (which means we dont' know anything about their type) the number of methods
 * doubled to allow a String serviceInterfaceName as an alternative to Class
 * serviceInterface - the new calls always instantiate it as an Object and the
 * caller can figure out what to do with their annotated object. This makes
 * the Extensions interface bloated and makes it harder to decide which
 * function to use.  Similarly, providing the context argument to filter out
 * extensions based on some capability also blaoted the code and imposes a requirement
 * on supporting extensions to also provide a factory class that can evalute the
 * context. TO FIX IT:  1)  remove the duplicate functions that accept String 
 * serviceInterfaceName and that accept a context argument; 2)
 * create top-level findAllAnnotated and findAnnotated functions that accept
 * the annotation class as a parameter, call the regular findAll or find, and
 * at the end instantiate it as an object and return Object; 3) change the
 * order of work so that the create() gets called at the very end by find and
 * findAll instead of deep inside -- that will allow find and findAll to
 * decide whether to return <T> or Object, without having to duplicate functions
 * in the entire call stack; 4) implement some helper classes to filter the
 * results -- to be used with the output of findAll -- and let them have
 * many helper methods to filter for different kinds of things ; for example
 * given a Filter implementation let them apply it, and return the filtered 
 * results in the same order they were received (which is the preference order);
 * 5) for annotated classes the find and findAll method maybe instead of returning
 * Object would require a 3rd parameter which is the expected return type - and
 * the caller can either specify Object.class or something else like Runnable.class
 * and the resulting class would be checked if it's assignable to that and if so
 * that's the type that would be returned as <T> so that the caller doesn't have
 * to repeat the same work everywhere.
 * 
 * 
 * 
 * TODO: performance improvement: instead of keeping the available implementations
 * in a set and iterating through the preferences each time, we should just
 * use a List instead of a Set and order the classes in the list according to
 * the preferred order so that we don't need the "createPreferred" function
 * and we always just use "createFirst" - when a plugin is added it should
 * be placed in the list according to any applicable preference.  
 * This will work for static preferences.  
 * For the context-based approach we again would only have to scan until the
 * first context match because they will already be in preference order.
 * Change in preference and adding plugins should happen much less frequently
 * than finding plugins so it makes sense to do the sorting up front instead
 * of on each find request.
 * 
 * @author jbuhacoff
 */
public class Extensions {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Extensions.class);
//    private static final Whiteboard whiteboard = new Whiteboard();
    private static final HashMap<String, List<Class<?>>> whiteboard = new HashMap<>(); // complete set of available implementations for each interface, in preference order (sorted when preferences change and when plugins are added)
    private static final HashMap<String, List<String>> preferences = new HashMap<>(); // optional application-declared implementation preferences for each interface
//    private static final Filter<String> ANY = new AcceptAny();
//    private static final ArrayList<String> ANY = ListUtils.EMPTY_LIST;
    
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
    
    // returns an instance of class or null if not found
    public static <T> T find(Class<T> serviceInterface) {
        return find(serviceInterface, null);
    }
    
    public static Object findAnnotated(Class<? extends Annotation> annotationInterface) {
        return findAnnotated(annotationInterface, null);
    }
    
    /*
    public static Object find(String serviceName) {
        return find(serviceName, null);
    }
    */
    
    // same as find(class) but if not found throws exception
    public static <T> T require(Class<T> serviceInterface) {
        T instance = find(serviceInterface);
        if( instance == null ) {
            throw new ExtensionNotFoundException(serviceInterface.getName());
        }
        return instance;
    }
    public static Object requireAnnotated(Class<? extends Annotation> annotationInterface) {
        Object instance = findAnnotated(annotationInterface);
        if( instance == null ) {
            throw new ExtensionNotFoundException(annotationInterface.getName());
        }
        return instance;
    }
    

    // never returns null - but may return an empty set if no matches were found
    public static <T> List<T> findAll(Class<T> serviceInterface) {
        return findAll(serviceInterface, null);
    }
    public static List<Object> findAllAnnotated(Class<? extends Annotation> annotationInterface) {
        return findAllAnnotated(annotationInterface, null);
    }

    /*
    public static List<Object> findAll(String serviceName) {
        return findAll(serviceName, null);
    }
    */
    
    
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
    
    public static <T, C> T find(Class<T> serviceInterface, C context) {
        return find(serviceInterface, serviceInterface, context);
    }
    
    public static <T, C> T require(Class<T> serviceInterface, C context) {
        T instance = find(serviceInterface, context);
        if( instance == null ) {
            throw new ExtensionNotFoundException(serviceInterface.getName());
        }
        return instance;
    }
    
    public static <C> Object findAnnotated(Class<? extends Annotation> annotationInterface, C context) {
        return find(Object.class, annotationInterface, context);
    }

    public static <C> Object requireAnnotated(Class<? extends Annotation> annotationInterface, C context) {
        Object instance = findAnnotated(annotationInterface, context);
        if( instance == null ) {
            throw new ExtensionNotFoundException(annotationInterface.getName());
        }
        return instance;
    }
    
    /*
    public static <C> Object find(String serviceName, C context) {
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
        return createPreferred(serviceName, serviceImplementations, preferenceOrder, context);
    }
    */

    private static <T, C> List<T> findAll(Class<T> returnType, Class<?> serviceInterface, C context) {
        String serviceName = serviceInterface.getName();
        List<Class<?>> serviceImplementations = whiteboard.get(serviceName);
        if (serviceImplementations == null || serviceImplementations.isEmpty()) {
            log.debug("No registered implementations for {}", serviceName);
            serviceImplementations = ListUtils.EMPTY_LIST;
        }
        return createAll(returnType, serviceImplementations, context);
    }
    public static <T, C> List<T> findAll(Class<T> serviceInterface, C context) {
        return findAll(serviceInterface, serviceInterface, context);
    }
    public static <C> List<Object> findAllAnnotated(Class<? extends Annotation> annotationInterface, C context) {
        return findAll(Object.class, annotationInterface, context);
    }

    /*
    public static <C> List<Object> findAll(String serviceName, C context) {
        List<Class<?>> serviceImplementations = whiteboard.get(serviceName);
        if (serviceImplementations == null || serviceImplementations.isEmpty()) {
            log.debug("No registered implementations for {}", serviceName);
            serviceImplementations = ListUtils.EMPTY_LIST;
        }
        return createAll(serviceName, serviceImplementations, context);
    }
    */
    
    public static Map<String,List<String>> getPreferences() {
        return Collections.unmodifiableMap(preferences);
    }
    public static Map<String,List<Class<?>>> getWhiteboard() {
        return Collections.unmodifiableMap(whiteboard);
    }
}
