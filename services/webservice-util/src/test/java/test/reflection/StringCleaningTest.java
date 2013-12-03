/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.reflection;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class StringCleaningTest {
    
    public static class Pet {
        public String getName() { return "sparky"; } 
    }
    public static class Person {
        public String getName() { return "bob"; }
        public int getAge() { return 40; }
        public Pet getPet() { return new Pet(); }
        // TODO  methods that return arraylist<string>  and string[] 
    }
    
    @Test
    public void testValidatePojo() {
        validate(new Person()); // throws an exception if person has invalid strings
    }
    
    // entry point:   call validate(TxtHost), validate(MLE), etc. 
    public static void validate(Object object) {
        validate(object, new ArrayList<Object>());
    }
    
    public static void validate(Object object, ArrayList<Object> stack) {
        // first check if the object being requested is already in the stack... if so we skip it to avoid infinite recursion
        for(Object item : stack) {
            if( object == item ) { return; }
        }
        // add the object to the stack so we don't try to validate it again if it has a self-referential property ...   unlike normal stacks we never really need to "pop" this one because it's just a record of where we've been,  and we don't use it to navigate.
        stack.add(object);
        
        // now validate the object
        Set<Method> stringMethods = getStringMethods(object.getClass());
        for(Method method : stringMethods) {
            try {
                String input = (String)method.invoke(object); // throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
                validateInput(input);
            } catch (Exception e) {
                // throw new ASException( ... failed to validate object so don't let it continue ...);
            }
        }
        // TODO  getStringCollectionMethods,  getStringArrayMethods
        // for the collection methods,  you would do  Collection<String> inputCollection = (Collection<String>)method.invoke(object) and then loop on the collection and validateInput on each item.   similar pattern for the arrays.
        
        // TODO getCustomObjectMethods , getCustomObjectCollectionMethods, getCustomObjectArrayMethods
        // for the object methods need to recurse into validate(object) for each one and each item in the collections and arrays
        
        // if we get to the end with no exceptions the object is validated.
        
    }
    

    public static void validateInput(String input) {
        // TODO check for illegal characters,  if present throw an IllegalArgumentException
    }

    // these are the reflection methods:
    
    public static boolean isStringMethod(Method method) {
        boolean conventional = method.getName().startsWith("get") || method.getName().startsWith("to");
        boolean noArgs = method.getParameterTypes().length == 0;
        boolean stringReturn = method.getReturnType().isAssignableFrom(String.class);
        return conventional && noArgs && stringReturn;
    }
    
    // TODO   isStringCollectionMethod and isStringArrayMethod ... because those strings need to be checked too .... should be similar to isStringMethod but check  Collection.class.isAssignableFrom(returnType) and isArray

    public static boolean isCustomObjectMethod(Method method) {
        boolean conventional = method.getName().startsWith("get") || method.getName().startsWith("to");
        boolean noArgs = method.getParameterTypes().length == 0;
        Class<?> returnType = method.getReturnType();
        boolean builtInObjectReturn = returnType.isPrimitive() || Number.class.isAssignableFrom(returnType) || Float.class.isAssignableFrom(returnType); // TODO  add Decimal, BigDecimal, Date, etc...  all the java built-ins that are safe.  
        boolean customObjectReturn = !builtInObjectReturn;
        return conventional && noArgs && customObjectReturn;
    }
    
    // TODO is isCustomObjectCollectionMethod  and isCustomObjectArrayMethod ...  because the contents of those would need to be checked too
    
    public static Set<Method> getStringMethods(Class<?> clazz) {
        HashSet<Method> stringMethods = new HashSet<Method>();
        Method[] methods =  clazz.getDeclaredMethods();                
        for(Method method : methods ) {
            if( isStringMethod(method) ) {
                stringMethods.add(method);
            }
        }
        return stringMethods;
    }
    
    // TODO  getStringCollectionMethods,  getStringArrayMethods,  getCustomObjectMethods , getCustomObjectCollectionMethods, getCustomObjectArrayMethods
    
}
