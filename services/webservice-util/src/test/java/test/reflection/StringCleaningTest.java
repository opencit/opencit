/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.reflection;

import com.intel.dcsg.cpg.validation.Regex; // 20131012
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class StringCleaningTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StringCleaningTest.class);
    
    public static class Pet {
        public String getName() { return "sparky@"; } 
    }
    public static class Person {
        public String description = "Test";
        private String desc2 = "Decription";
        public String getName() { return "bob"; }
        @Regex("^[a-zA-z0-9_-]+@[a-zA-z0-9-]+\\.(?:com|org|net)$") // not a real email regex,  just for quick testing      // 20131012
        public String getEmail() { return "bob^@example.com"; }    // 20131012
        public int getAge() { return 40; }
        public Pet getPet() { return new Pet(); }
        public String[] getPetNames() { return new String[] {"doga","dogb","dogc"}; }
        public Integer[] getPetIDs() { return new Integer[] {1,2,3};}
        public List<Integer> getPetIDList() { return new ArrayList<Integer>(Arrays.asList(new Integer[] {1,2,3}));} 
        public List<String> getPetList() { return new ArrayList<String>(Arrays.asList(new String[] {"dogA","dogB","dogC"}));} //{{ add("dogA");  add("dogB");  add("dogC");}};}
        public List<Pet> getPetList2() {return new ArrayList<Pet>();}     
        public Pet[] getPetList3() {return new Pet[4];}
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
    
    // 20131012
    // this is used for String getName(),  String[] getNames(), and Collection<String> getNames()  but caller is responsible for iterating over the values
    public static void validateStringMethod(Object object, Method method, String input) {
        Pattern pattern;
        if( method.isAnnotationPresent(Regex.class) ) {
            String regex = method.getAnnotation(Regex.class).value();
            log.debug("Regex annotation: {}", regex);
            pattern = getPattern(regex);
        }
        else {
            pattern = getPattern(DEFAULT_PATTERN);
        }
        validateInput(input, pattern);        
    }
    // 20131012
    // this is used for String name,  String[] names(), and Collection<String> names()  but caller is responsible for iterating over the values
    public static void validateStringField(Object object, Field field, String input) {
        Pattern pattern;
        if( field.isAnnotationPresent(Regex.class) ) {
            String regex = field.getAnnotation(Regex.class).value();
            pattern = getPattern(regex);
        }
        else {
            pattern = getPattern(DEFAULT_PATTERN);
        }
        validateInput(input, pattern);        
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
            log.debug("Verifying method : " + method.getName());
            try {
                String input = (String)method.invoke(object); // throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
                log.debug("Verifying value : " + input);
                validateStringMethod(object, method, input); // 20131012
            } catch (Exception e) {
                // throw new ASException( ... failed to validate object so don't let it continue ...);
            }
        }
        
        // Now validate the fields
        Set<Field> stringFields = getStringFields(object.getClass());
        for(Field field : stringFields) {
            log.debug("Verifying field : " + field.getName());
            try {
                field.setAccessible(true);
                String value = (String)field.get(object); // throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
                log.debug("Verifying value : " + value);
                validateStringField(object, field, value); // 20131012
            } catch (Exception e) {
                // throw new ASException( ... failed to validate object so don't let it continue ...);
            }
        }
        
        Set<Method> stringArrayMethods = getStringArrayMethods(object.getClass());
        for(Method method : stringArrayMethods) {
            log.debug("Verifying method : " + method.getName());
            try {
                String[] collection = (String[])method.invoke(object); // throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
                for (String input : collection) {
                    log.debug("Verifying value : " + input);
                    validateStringMethod(object, method, input); // 20131012
                }
            } catch (Exception e) {
                // throw new ASException( ... failed to validate object so don't let it continue ...);
            }
        }                

        Set<Method> stringCollectionMethods = getStringCollectionMethods(object.getClass());
        for(Method method : stringCollectionMethods) {
            log.debug("Verifying method : " + method.getName());
            try {
                List<String> collection = (List<String>) method.invoke(object); // throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
                for(String input : collection) {
                    log.debug("Verifying value : " + input);
                    validateStringMethod(object, method, input); // 20131012
                }
            } catch (Exception e) {
                // throw new ASException( ... failed to validate object so don't let it continue ...);
            }
        } 
        
        Set<Method> customMethods = getCustomObjectMethods(object.getClass());
        for(Method method : customMethods) {
            log.debug("Verifying method : " + method.getName());
            try {
                Object customObject = method.invoke(object); // throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
                validate(customObject, stack);
            } catch (Exception e) {
                // throw new ASException( ... failed to validate object so don't let it continue ...);
            }
        } 
        
        Set<Method> customObjectCollectionMethods = getCustomObjectCollectionMethods(object.getClass());
        for(Method method : customObjectCollectionMethods) {
            log.debug("Verifying method : " + method.getName());
            try {
                List<Object> customObjectCollection = (List<Object>) method.invoke(object); // throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
                for (Object customObject : customObjectCollection) {
                    validate(customObject, stack);
                }
            } catch (Exception e) {
                // throw new ASException( ... failed to validate object so don't let it continue ...);
            }
        }
        
        Set<Method> customObjectArrayMethods = getCustomObjectArrayMethods(object.getClass());
        for(Method method : customObjectArrayMethods) {
            log.debug("Verifying method : " + method.getName());
            try {
                Object[] customObjectCollection = (Object[])method.invoke(object); // throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
                for (Object customObject : customObjectCollection) {                
                    validate(customObject, stack);
                }
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
    
    // 20131012
    public static final HashMap<String,Pattern> patternMap = new HashMap<String,Pattern>();
    public static final String DEFAULT_PATTERN = "^[a-zA-Z0-9_-]*$";
    
    // 20131012
    public static Pattern getPattern(String regex) {
        Pattern pattern = patternMap.get(regex);
        if( pattern == null ) {
            pattern = Pattern.compile(regex);
            patternMap.put(regex, pattern);
        }
        return pattern;
    }

    public static void validateInput(String input) {
        validateInput(input, getPattern(DEFAULT_PATTERN));    // 20131012
    }

    // 20131012
    public static void validateInput(String input, Pattern pattern) {
        Matcher matcher = pattern.matcher(input);
        if (!matcher.matches()) {
            log.debug("Illegal characters found in : " + input);
            throw new IllegalArgumentException();
        }
    }
    
    /**
     * This function verifies if the class is one of the built in datatypes or a custom class. This will also verify the arrays for
     * built-in data types.
     * @param clazz
     * @return 
     */
    public static boolean isBuiltInType(Class<?> clazz) {
        boolean isBuiltInType = clazz.isPrimitive() || 
                clazz.equals(Boolean.class) || clazz.equals(Boolean[].class) || clazz.equals(Number.class) || clazz.equals(Number[].class) ||
                clazz.equals(Float.class) || clazz.equals(Float[].class)|| clazz.equals(Integer.class) || clazz.equals(Integer[].class) ||
                clazz.equals(Byte.class) || clazz.equals(Byte[].class) || clazz.equals(Double.class) || clazz.equals(Double[].class) ||
                clazz.equals(Short.class) || clazz.equals(Short[].class) || clazz.equals(Long.class) || clazz.equals(Long[].class) || 
                clazz.equals(Character.class) || clazz.equals(Character[].class) || clazz.equals(String.class) || clazz.equals(String[].class);
        return isBuiltInType;
    }

    public static boolean isStringMethod(Method method) {
        boolean conventional = method.getName().startsWith("get") || method.getName().startsWith("to");
        boolean noArgs = method.getParameterTypes().length == 0;
        boolean stringReturn = method.getReturnType().isAssignableFrom(String.class);
        return conventional && noArgs && stringReturn;
    }
    
    public static boolean isStringArrayMethod(Method method) {
        boolean conventional = method.getName().startsWith("get") || method.getName().startsWith("to");
        boolean noArgs = method.getParameterTypes().length == 0;
        boolean isArray = method.getReturnType().isArray();
        boolean stringReturn = String[].class.isAssignableFrom(method.getReturnType());
        return conventional && noArgs && stringReturn && isArray;
    }    

    public static boolean isStringCollectionMethod(Method method) {
        boolean conventional = method.getName().startsWith("get") || method.getName().startsWith("to");
        boolean noArgs = method.getParameterTypes().length == 0;        
        boolean isList = java.util.List.class.isAssignableFrom(method.getReturnType());
        boolean stringReturn = method.toGenericString().contains("java.util.List<java.lang.String>");
        return conventional && noArgs && isList && stringReturn;
    }
    // TODO   isStringCollectionMethod and isStringArrayMethod ... because those strings need to be checked too .... should be similar to isStringMethod but check  Collection.class.isAssignableFrom(returnType) and isArray

    public static boolean isCustomObjectMethod(Method method) {
        boolean conventional = method.getName().startsWith("get") || method.getName().startsWith("to");
        boolean noArgs = method.getParameterTypes().length == 0;
        Class<?> returnType = method.getReturnType();
        // Since we will process the Array and Collection return types separately, we need to first check for that
        boolean isArrayOrCollection = returnType.isArray() || Collection.class.isAssignableFrom(returnType);
        boolean builtInObjectReturn = isBuiltInType(returnType);
        boolean customObjectReturn = !builtInObjectReturn;
        return conventional && noArgs && !isArrayOrCollection && customObjectReturn;
    }
    
    public static boolean isCustomObjectCollectionMethod(Method method) {
        boolean conventional = method.getName().startsWith("get") || method.getName().startsWith("to");
        boolean noArgs = method.getParameterTypes().length == 0;
        Class<?> returnType = method.getReturnType();
        boolean isCollection = Collection.class.isAssignableFrom(returnType);
        // We need to check if it is a collection of built in data types or a custom object.
        boolean builtInObjectReturn = false;
        if (isCollection) {
            ParameterizedType stringListType = (ParameterizedType) method.getGenericReturnType();
            Class<?> stringListClass = (Class<?>) stringListType.getActualTypeArguments()[0];
            builtInObjectReturn = isBuiltInType(stringListClass);
        }        
        boolean customObjectReturn = !builtInObjectReturn;
        return conventional && noArgs && isCollection && customObjectReturn;
    }

    public static boolean isCustomObjectArrayMethod(Method method) {
        boolean conventional = method.getName().startsWith("get") || method.getName().startsWith("to");
        boolean noArgs = method.getParameterTypes().length == 0;
        Class<?> returnType = method.getReturnType();
        boolean isArray = returnType.isArray();
        // We need to check if the array is of built in data types or a custom object.
        boolean builtInObjectReturn = false;
        if (isArray) {
            builtInObjectReturn = isBuiltInType(returnType);
        }        
        boolean customObjectReturn = !builtInObjectReturn;
        return conventional && noArgs && isArray && customObjectReturn;
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
    
    public static Set<Field> getStringFields(Class<?> clazz) {
        HashSet<Field> stringFields = new HashSet<Field>();
        Field[] declaredFields = clazz.getDeclaredFields();
        if (declaredFields != null && declaredFields.length > 0)
        for(Field field : declaredFields ) {
            int modifiers = field.getModifiers();
            if( (field.getModifiers() == Modifier.PUBLIC) &&  (String.class.isAssignableFrom(field.getGenericType().getClass()))) {  //(field.getGenericType().toString().equalsIgnoreCase("class java.lang.String")) ) {
                stringFields.add(field);
            }
        }
        return stringFields;
    }
    
    public static Set<Method> getStringArrayMethods(Class<?> clazz) {
        HashSet<Method> stringMethods = new HashSet<Method>();
        Method[] methods =  clazz.getDeclaredMethods();                
        for(Method method : methods ) {
            if( isStringArrayMethod(method) ) {
                stringMethods.add(method);
            }
        }
        return stringMethods;        
    }    

    public static Set<Method> getStringCollectionMethods(Class<?> clazz) {
        HashSet<Method> stringMethods = new HashSet<Method>();
        Method[] methods =  clazz.getDeclaredMethods();                
        for(Method method : methods ) {
            if( isStringCollectionMethod(method) ) {
                stringMethods.add(method);
            }
        }
        return stringMethods;        
    }    
    
    public static Set<Method> getCustomObjectMethods(Class<?> clazz) {
        HashSet<Method> customMethods = new HashSet<Method>();
        Method[] methods =  clazz.getDeclaredMethods();                
        for(Method method : methods ) {
            if( isCustomObjectMethod(method) ) {
                customMethods.add(method);
            }
        }
        return customMethods;        
    }    

    public static Set<Method> getCustomObjectCollectionMethods(Class<?> clazz) {
        HashSet<Method> customObjectCollectionMethods = new HashSet<Method>();
        Method[] methods =  clazz.getDeclaredMethods();                
        for(Method method : methods ) {
            if( isCustomObjectCollectionMethod(method)) {
                customObjectCollectionMethods.add(method);
            }
        }
        return customObjectCollectionMethods;        
    }    
    
    public static Set<Method> getCustomObjectArrayMethods(Class<?> clazz) {
        HashSet<Method> customObjectArrayMethods = new HashSet<Method>();
        Method[] methods =  clazz.getDeclaredMethods();                
        for(Method method : methods ) {
            if( isCustomObjectArrayMethod(method)) {
                customObjectArrayMethods.add(method);
            }
        }
        return customObjectArrayMethods;        
    }    
    // TODO  getStringCollectionMethods,  getStringArrayMethods,  getCustomObjectMethods , getCustomObjectCollectionMethods, getCustomObjectArrayMethods
    
    
}
