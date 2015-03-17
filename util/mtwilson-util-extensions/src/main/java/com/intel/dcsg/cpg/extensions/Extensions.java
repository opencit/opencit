/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.extensions;

import com.intel.mtwilson.pipe.Filter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
 * When using a criteria-based search, factories are still searched in order of
 * preference but if a preferred factory does not accept the criteria it is
 * skipped and the next one is checked.
 *
 * When using a criteria-based search, factories that do not implement the
 * create(context) method are assumed to not accept it and are skipped.
 *
 * !!! TODO !!! because of the requirement to find annotated classes by
 * annotation (which means we dont' know anything about their type) the number
 * of methods doubled to allow a String serviceInterfaceName as an alternative
 * to Class serviceInterface - the new calls always instantiate it as an Object
 * and the caller can figure out what to do with their annotated object. This
 * makes the Extensions interface bloated and makes it harder to decide which
 * function to use. Similarly, providing the context argument to filter out
 * extensions based on some capability also blaoted the code and imposes a
 * requirement on supporting extensions to also provide a factory class that can
 * evalute the context. TO FIX IT: 1) remove the duplicate functions that accept
 * String serviceInterfaceName and that accept a context argument; 2) create
 * top-level findAllAnnotated and findAnnotated functions that accept the
 * annotation class as a parameter, call the regular findAll or find, and at the
 * end instantiate it as an object and return Object; 3) change the order of
 * work so that the create() gets called at the very end by find and findAll
 * instead of deep inside -- that will allow find and findAll to decide whether
 * to return <T> or Object, without having to duplicate functions in the entire
 * call stack; 4) implement some helper classes to filter the results -- to be
 * used with the output of findAll -- and let them have many helper methods to
 * filter for different kinds of things ; for example given a Filter
 * implementation let them apply it, and return the filtered results in the same
 * order they were received (which is the preference order); 5) for annotated
 * classes the find and findAll method maybe instead of returning Object would
 * require a 3rd parameter which is the expected return type - and the caller
 * can either specify Object.class or something else like Runnable.class and the
 * resulting class would be checked if it's assignable to that and if so that's
 * the type that would be returned as <T> so that the caller doesn't have to
 * repeat the same work everywhere.
 *
 *
 *
 * TODO: performance improvement: instead of keeping the available
 * implementations in a set and iterating through the preferences each time, we
 * should just use a List instead of a Set and order the classes in the list
 * according to the preferred order so that we don't need the "createPreferred"
 * function and we always just use "createFirst" - when a plugin is added it
 * should be placed in the list according to any applicable preference. This
 * will work for static preferences. For the context-based approach we again
 * would only have to scan until the first context match because they will
 * already be in preference order. Change in preference and adding plugins
 * should happen much less frequently than finding plugins so it makes sense to
 * do the sorting up front instead of on each find request.
 *
 * @author jbuhacoff
 */
public class Extensions {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Extensions.class);
//    private static final Whiteboard whiteboard = new Whiteboard();
//    private static final Filter<String> ANY = new AcceptAny();
//    private static final ArrayList<String> ANY = ListUtils.EMPTY_LIST;
    private static final Collection<ExtensionProvider> providers = new ArrayList<>();

    static {
        // we use an instance of ServiceLoaderExtensionProvider to leverage
        // Java's ServiceLoader interface to find all available ExtensionProvider
        // implementations on the classpath
        ServiceLoaderExtensionProvider sl = new ServiceLoaderExtensionProvider();
        Iterator<String> providerNamesIterator = sl.find(ExtensionProvider.class);
        while (providerNamesIterator.hasNext()) {
            String providerName = providerNamesIterator.next();
            log.debug("Found extension provider: {}", providerName);
            try {
                ExtensionProvider provider = sl.create(ExtensionProvider.class, providerName);
                providers.add(provider);
            } catch (ExtensionNotFoundException e) {
                log.error("Cannot use extension provider: {}", providerName, e);
            }
        }
    }

//    public static Collection<ExtensionProvider> getProviders() { return providers; }
    public static class Extension {

        public ExtensionProvider provider;
        public String name;

        public Extension(ExtensionProvider provider, String name) {
            this.provider = provider;
            this.name = name;
        }
    }

    /**
     * Providers are in priority order, so if multiple providers return the
     * SAME IMPLEMENTATION class name of an extension interface, 
     * only the first provider's implementation will be
     * used. 
     * @param extension
     * @return 
     */
    private static List<Extension> list(Class<?> extension) {
        HashMap<String,Extension> unique = new HashMap<>(); // implementation class name -> extension descriptor
        ArrayList<Extension> list = new ArrayList<>(); // we use the map to prevent duplicates but the list stores the results in order: in provider order and then in the order returned by each provider
        for (ExtensionProvider provider : providers) {
            log.debug("Looking for extension {} in provider {}", extension.getName(), provider.getClass().getName());
            Iterator<String> implementationNamesIterator = provider.find(extension);
            while (implementationNamesIterator.hasNext()) {
                String name = implementationNamesIterator.next();
                Extension alreadyListed = unique.get(name);
                if( alreadyListed == null ) {
                    Extension e = new Extension(provider, name);
                    unique.put(name, e);
                    list.add(e);
                    log.debug("Extension {} implementation {} provider {}", extension.getName(), name, provider.getClass().getName());
                }
                else {
                    log.debug("Extension {} ignoring implementation {} from provider {} because already registered from provider {}", extension.getName(), name, provider.getClass().getName(), alreadyListed.provider.getClass().getName());
                }
            }
        }
        return list;
    }

    private static List<Extension> listAnnotated(Class<? extends Annotation> annotation) {
        ArrayList<Extension> list = new ArrayList<>();
        for (ExtensionProvider provider : providers) {
            Iterator<String> implementationNamesIterator = provider.findAnnotated(annotation);
            while (implementationNamesIterator.hasNext()) {
                String name = implementationNamesIterator.next();
                list.add(new Extension(provider, name));
                log.debug("Extension annotation {} implementation {} provider {}", annotation.getName(), name, provider.getClass().getName());
            }
        }
        return list;
    }

    public static void reload() {
        for (ExtensionProvider provider : providers) {
            provider.reload();
        }
    }

    // returns an instance of class or null if not found
    public static <T> T find(Class<T> extension) {
        List<Extension> list = list(extension);
        if (list.isEmpty()) {
            return null;
        }
        Extension first = list.get(0);
        T implementation = first.provider.create(extension, first.name);
        return implementation;
    }

    public static Object findAnnotated(Class<? extends Annotation> annotation) {
        List<Extension> list = listAnnotated(annotation);
        if (list.isEmpty()) {
            return null;
        }
        Extension first = list.get(0);
        Object implementation = first.provider.createAnnotated(annotation, first.name);
        return implementation;
    }

    /*
     public static Object find(String serviceName) {
     return find(serviceName, null);
     }
     */
    // same as find(class) but if not found throws exception
    public static <T> T require(Class<T> serviceInterface) {
        T instance = find(serviceInterface);
        if (instance == null) {
            throw new ExtensionNotFoundException(serviceInterface.getName());
        }
        return instance;
    }

    public static Object requireAnnotated(Class<? extends Annotation> annotationInterface) {
        Object instance = findAnnotated(annotationInterface);
        if (instance == null) {
            throw new ExtensionNotFoundException(annotationInterface.getName());
        }
        return instance;
    }

    // never returns null - but may return an empty set if no matches were found
    public static <T> List<T> findAll(Class<T> extension) {
        log.debug("findAll extension {}", extension.getName());
        ArrayList<T> result = new ArrayList<>();
        List<Extension> list = list(extension);
        for (Extension item : list) {
            try {
                Class<?> clazz = Class.forName(item.name);
                Constructor constructor = ReflectionUtil.getNoArgConstructor(clazz);
                Object instance = constructor.newInstance();
                T implementation = (T)instance;
                result.add(implementation);
            } catch (InstantiationException | InvocationTargetException | IllegalAccessException | ClassNotFoundException | ClassCastException e) {
                log.debug("Cannot instantiate implementation class {}", item.name, e);
                continue;
            }

        }
        return result;
    }
    
    public static interface Factory<T> {
        /**
         * Example content of create using a no-arg constructor:
         * <pre>
                Constructor constructor = ReflectionUtil.getNoArgConstructor(clazz);
                Object instance = constructor.newInstance();
                T implementation = (T)instance;
         * </pre>
         * 
         * @param clazz
         * @return
         * @throws ReflectiveOperationException
         * @throws ClassCastException 
         */
        T create(Class<?> clazz) throws ReflectiveOperationException, ClassCastException;
    }
    
    public static class NoArgFactory<T> implements Factory<T> {

        @Override
        public T create(Class<?> clazz) throws ReflectiveOperationException, ClassCastException {
            Constructor constructor = ReflectionUtil.getNoArgConstructor(clazz);
            Object instance = constructor.newInstance();
            T implementation = (T)instance;
            return implementation;
        }        
    }
    
    public static class OneArgFactory<T> implements Factory<T> {
        private Object arg;
        public OneArgFactory(Object arg) {
            this.arg = arg;
        }
        
        @Override
        public T create(Class<?> clazz) throws ReflectiveOperationException, ClassCastException {
            Constructor constructor = ReflectionUtil.getOneArgConstructor(clazz, arg.getClass());
            Object instance = constructor.newInstance(arg);
            T implementation = (T) instance;
            return implementation;
        }        
    }
    
    public static class OneArgFilter implements Filter<Class<?>> {
        private Object arg;

        public OneArgFilter(Object arg) {
            this.arg = arg;
        }
        
        @Override
        public boolean accept(Class<?> item) {
            Constructor constructor = ReflectionUtil.getOneArgConstructor(item, arg.getClass());
            return constructor != null;
        }
        
    }
    
    public static <T> List<T> findAll(Class<T> extension, Filter<Class<?>> filter, Factory<T> factory) {
        log.debug("findAll extension {} filter {} factory {}", extension.getName(), filter.getClass().getName(), factory.getClass().getName());
        ArrayList<T> result = new ArrayList<>();
        List<Extension> list = list(extension);
        for (Extension item : list) {
            try {
                log.debug("findAll trying {} from provider {}", item.name, item.provider.getClass().getName());
                Class<?> clazz = Class.forName(item.name);
                if( filter.accept(clazz)) {
                    result.add(factory.create(clazz));
                }
            } catch (ReflectiveOperationException | ClassCastException e) {
                log.debug("Cannot instantiate implementation class {}", item.name, e);
                continue;
            }

        }
        return result;
    }

    public static List<Object> findAllAnnotated(Class<? extends Annotation> annotationInterface) {
        return findAllAnnotated(annotationInterface, null);
    }

    /*
     public static List<Object> findAll(String serviceName) {
     return findAll(serviceName, null);
     }
     */
    @Deprecated
    public static <T, C> T find(Class<T> serviceInterface, C context) {
//        return find(serviceInterface, serviceInterface, context);
//        throw new UnsupportedOperationException();
        List<T> items = findAll(serviceInterface, context);
        if( items.isEmpty() ) { return null; }
        T first = items.get(0);
        return first;
    }

    @Deprecated
    public static <T, C> T require(Class<T> serviceInterface, C context) {
        T instance = find(serviceInterface, context);
        if (instance == null) {
            throw new ExtensionNotFoundException(serviceInterface.getName());
        }
        return instance;
    }

    @Deprecated
    public static <C> Object findAnnotated(Class<? extends Annotation> annotationInterface, C context) {
//        return find(Object.class, annotationInterface, context);
//        throw new UnsupportedOperationException();
        List<Object> items = findAllAnnotated(annotationInterface, context);
        if( items.isEmpty() ) { return null; }
        Object first = items.get(0);
        return first;
    }

    @Deprecated
    public static <C> Object requireAnnotated(Class<? extends Annotation> annotationInterface, C context) {
        Object instance = findAnnotated(annotationInterface, context);
        if (instance == null) {
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
    @Deprecated
    public static <T, C> List<T> findAll(Class<T> extension, C context) {
//        return findAll(serviceInterface, serviceInterface, context);
//        throw new UnsupportedOperationException();
        log.debug("Extensions findAll interface {} context {}", extension.getName(), (context==null?"null":context.getClass().getName()));
        ArrayList<T> result = new ArrayList<>();
        List<Extension> list = list(extension);
        for (Extension item : list) {
            try {
                Class<?> clazz = Class.forName(item.name);
                if( context == null ) {
                    Constructor constructor = ReflectionUtil.getNoArgConstructor(clazz);
                    Object instance = constructor.newInstance();
                    T implementation = (T)instance;
                    result.add(implementation);
                }
                else {
                    Constructor constructor = ReflectionUtil.getOneArgConstructor(clazz, context.getClass());
                    if (constructor == null) {
                        log.debug("Implementation {} does not support context constructor {}", clazz.getName(), context.getClass().getName());
                        if( Filter.class.isAssignableFrom(clazz)) {
                            log.debug("Implementation {} is a filter", clazz.getName());
                            constructor = ReflectionUtil.getNoArgConstructor(clazz);
                            Object instance = constructor.newInstance();
                            Filter filter = (Filter)instance;
                            if( filter.accept(context) ) {
                                T implementation = (T)instance;
                                result.add(implementation);
                            }
                        }
                        continue; // implementation does not have a constructor for the given context  
                    }
                    else {
                        Object instance = constructor.newInstance(context);
                        T implementation = (T) instance;
                        result.add(implementation);
                    }
                }
            } catch (InstantiationException | InvocationTargetException | IllegalAccessException | ClassNotFoundException | ClassCastException e) {
                log.debug("Cannot instantiate implementation class {}", item.name, e);
                continue;
            }

        }
        return result;
    }

    @Deprecated
    public static <C> List<Object> findAllAnnotated(Class<? extends Annotation> annotation, C context) {
//        return findAll(Object.class, annotationInterface, context);
//        throw new UnsupportedOperationException();
        ArrayList<Object> result = new ArrayList<>();
        List<Extension> list = listAnnotated(annotation);
        for (Extension item : list) {
            try {
                Class<?> clazz = Class.forName(item.name);
                if( context == null ) {
                    Constructor constructor = ReflectionUtil.getNoArgConstructor(clazz);
                    Object instance = constructor.newInstance();
                    result.add(instance);
                }
                else {
                    Constructor constructor = ReflectionUtil.getOneArgConstructor(clazz, context.getClass());
                    if (constructor == null) {
                        log.debug("Implementation {} does not support context {}", clazz.getName(), context.getClass().getName());
                         if( Filter.class.isAssignableFrom(clazz)) {
                            log.debug("Implementation {} is a filter", clazz.getName());
                            constructor = ReflectionUtil.getNoArgConstructor(clazz);
                            Object instance = constructor.newInstance();
                            Filter filter = (Filter)instance;
                            if( filter.accept(context) ) {
                                result.add(instance);
                            }
                        }
                       continue; // implementation does not have a constructor for the given context  
                    }
                    else {
                        Object instance = constructor.newInstance(context);
                        result.add(instance);
                    }
                }
            } catch (InstantiationException | InvocationTargetException | IllegalAccessException | ClassNotFoundException | ClassCastException e) {
                log.debug("Cannot instantiate implementation class {}", item.name, e);
                continue;
            }
        }
        return result;
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
    
    
    ////////// deprecated functions delegated to WhiteboardExtensionProvider ///////////
@Deprecated
public static void clearAll() { WhiteboardExtensionProvider.clearAll(); }
@Deprecated
public static void clear(Class<?> serviceInterface) {WhiteboardExtensionProvider.clear(serviceInterface); }
@Deprecated
public static void clearAnnotated(Class<? extends Annotation> annotationInterface) {WhiteboardExtensionProvider.clearAnnotated(annotationInterface); }
@Deprecated
public static <T> void register(Class<T> serviceInterface, Class<?> serviceImplementation) {WhiteboardExtensionProvider.register(serviceInterface,serviceImplementation); }
@Deprecated
public static void registerAnnotated(Class<? extends Annotation> annotationInterface, Class<?> serviceImplementation) {WhiteboardExtensionProvider.registerAnnotated(annotationInterface,serviceImplementation); }
@Deprecated
public static void unregister(Class<?> serviceInterface, Class<?> serviceImplementation) {WhiteboardExtensionProvider.unregister(serviceInterface,serviceImplementation); }
@Deprecated
public static void unregisterAnnotated(Class<? extends Annotation> annotationInterface, Class<?> serviceImplementation) {WhiteboardExtensionProvider.unregisterAnnotated(annotationInterface,serviceImplementation); }
@Deprecated
public static <T> void prefer(Class<T> serviceInterface, List<String> preferenceOrder) {WhiteboardExtensionProvider.prefer(serviceInterface,preferenceOrder); }
@Deprecated
public static <T> void prefer(Class<T> serviceInterface, String[] preferenceOrder) {WhiteboardExtensionProvider.prefer(serviceInterface,preferenceOrder); }
@Deprecated
public static Map<String,List<String>> getPreferences() {return WhiteboardExtensionProvider.getPreferences(); }
@Deprecated
public static Map<String,List<Class<?>>> getWhiteboard() {return WhiteboardExtensionProvider.getWhiteboard(); }
    
    
}
