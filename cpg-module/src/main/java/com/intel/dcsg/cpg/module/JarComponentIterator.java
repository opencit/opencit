/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.module;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.jar.Manifest;
import java.util.regex.Pattern;

/**
 * Identifies classes following the Component convention, which
 * is class name ends with Component and class includes a no-arg constructor,
 * and either has void activate() and void deactivate() or has other no-return value
 * and no-arg methods with @Activate and @Deactivate annotations.
 * methods.
 * 
 * By default all classes in the Jar are checked for @Component annotations and 
 * Component conventions.
 * 
 * The search can be restricted to only classes having the @Component annotation
 * by calling the {@code noConventions} method.
 * 
 * The search can be expanded to classes without a @Component annotation and that
 * do not follow the Component naming convention (their methods might but not the class
 * name) by including specific class names to search for as well. If those classes
 * are found in the jar they will still be evaluated to ensure they have activation
 * and deactivation methods and a no-arg constructor. The by-name search requires the
 * fully qualified name (package and class name) like org.foo.Bar.
 * 
 * The search settings only apply to class selection while scanning the Jar file -
 * when a class is found that matches the search settings (either annotations or
 * conventions or both) the class is evaluated for having a no-arg constructor, and
 * an activation method, and a deactivation method, and the methods are found either
 * by annotations or conventions regardless of search settings.
 * 
 * @author jbuhacoff
 */
public class JarComponentIterator extends JarClassIterator implements Iterator<Class<?>> {
    private final Pattern namePattern = Pattern.compile(".*Component$");
    private ComponentSearchConfiguration search = null;
    
    /**
     * 
     * @param jar the file to scan for classes
     * @param classLoader to use for loading classes from the Jar; you can use new JarFileCLassLoader(jar)
     * @throws IOException 
     */
    public JarComponentIterator(File jar, ClassLoader classLoader) throws IOException {
        super(jar, classLoader);
        this.search = new ComponentSearchConfiguration(); // default is to search annotations and conventions and the manifest
    }

    public JarComponentIterator(File jar, ClassLoader classLoader, ComponentSearchConfiguration search) throws IOException {
        super(jar, classLoader);
        this.search = search;
    }
    
    @Override
    protected boolean accept(String name) { 
        // if by-name search is enabled and this name matches (must be the fully qualified name) then we accept the class for evaluation
        if( search.names() != null ) {
            for(String searchName : search.names()) {
                if( name.equals(searchName) ) { return true; }
            }
        }
        // if annotation search is turned on, we have to accept all names because we need to load the class to check for annotations
        if( search.annotations() ) { return true; }
        // we only check for the *Component name if search conventions is turned on, because an application might want to skip files that are not explicitly annotated
        if( search.conventions() ) {
            return namePattern == null || namePattern.matcher(name).matches();
        }
        return false;
    }
    
    @Override
    protected boolean accept(Class<?> clazz) {
        // short circuit if applications wants to search ONLY annotations and the class doesn't have it
        if( search.annotations() && !search.conventions() && !ReflectionUtil.hasComponentAnnotation(clazz) ) {
            return false;
        }
        // now check for no-arg constructor, activate, and deactivate; once we decide to check a class we'll accept either annotations or conventions (search settings are only for class selection)
        return ReflectionUtil.hasNoArgConstructor(clazz) 
                && ReflectionUtil.getActivateMethod(clazz) != null
                && ReflectionUtil.getDeactivateMethod(clazz) != null;
    }
    
}
