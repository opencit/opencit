/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.util;

import com.intel.dcsg.cpg.validation.Regex;
import com.intel.mtwilson.datatypes.ConnectionString;
import com.intel.mtwilson.datatypes.ErrorCode;
import com.intel.mtwilson.datatypes.ValidationRegEx;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
public class ValidationUtil {
    private static Logger log = LoggerFactory.getLogger(ValidationUtil.class);
    
    private static final HashMap<String,Pattern> patternMap = new HashMap<String,Pattern>();
    
    // Regular expressions for certain standard fields
    /*public static final String DEFAULT_PATTERN = "^[a-zA-Z0-9_-]*$";
    public static final String IPADDRESS_PATTERN = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$";
    public static final String FQDN_PATTERN = "^(([a-zA-Z]|[a-zA-Z][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z]|[A-Za-z][A-Za-z0-9\\-]*[A-Za-z0-9])$";
    public static final String IPADDR_FQDN_PATTERN = "(^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$)|(^(([a-zA-Z]|[a-zA-Z][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z]|[A-Za-z][A-Za-z0-9\\-]*[A-Za-z0-9])$)";
    public static final String EMAIL_PATTERN = "^([a-z0-9_\\.-]+)@([\\da-z\\.-]+)\\.([a-z\\.]{2,6})$";
    // This might need to be modified to allow more special characters. The rule currently says 1or more lower and upper case, one digit and one of the special characters and atleast 8 characters in length
    public static final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$";  
    public static final String PORT = "^[0-9]{1,5}";
    public static final String ADDON_CONNECTION_STRING = "ADDON_CONNECTION_STRING"; // We will internally use the other regular expressions to validate fields having this annotation
    */
    
    public static void validate(Object object) {
        validate(object, new ArrayList<Object>());
    }
    
    private static void validateStringMethod(Object object, Method method, String input) {
        Pattern pattern;
        if( method.isAnnotationPresent(Regex.class) ) {
            String regex = method.getAnnotation(Regex.class).value();
            log.debug("Regex annotation: {}", regex);
            pattern = getPattern(ValidationRegEx.getValidationRegEx(regex).getRegEx());
        }
        else {
            pattern = getPattern(ValidationRegEx.DEFAULT_PATTERN.getRegEx());
        }
        validateInput(input, pattern);        
    }

    private static void validateStringField(Object object, Field field, String input) {
        Pattern pattern;
        if( field.isAnnotationPresent(Regex.class) ) {
            String regex = field.getAnnotation(Regex.class).value();
            pattern = getPattern(ValidationRegEx.getValidationRegEx(regex).getRegEx());
        }
        else {
            pattern = getPattern(ValidationRegEx.DEFAULT_PATTERN.getRegEx());
        }
        validateInput(input, pattern);        
    }
    
    
    private static void validate(Object object, ArrayList<Object> stack) {
        // first check if the object being requested is already in the stack... if so we skip it to avoid infinite recursion
        for(Object item : stack) {
            if( object == item ) { return; }
        }
        // add the object to the stack so we don't try to validate it again if it has a self-referential property ...   unlike normal stacks we never really need to "pop" this one because it's just a record of where we've been,  and we don't use it to navigate.
        stack.add(object);

        // Validate the basic data types. This function validates the input such as Validate("Test") or Validate(new String{}..) or Validate(new ArrayList<String>....)
        boolean continueValidation = validateBasicDataType(object);
        if (!continueValidation)
            return;
        
        // Now validate the fields
        Set<Field> stringFields = getStringFields(object.getClass());
        for(Field field : stringFields) {
            log.debug("Verifying string field : " + field.getName());
            try {
                field.setAccessible(true);
                String value = (String)field.get(object); // throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
                log.debug("Verifying string value : " + value);
                validateStringField(object, field, value); // 20131012
            } catch (Exception e) {
                throw new MWException(ErrorCode.AS_INPUT_VALIDATION_ERROR);
            }
        }
        
        Set<Field> stringArrayFields = getStringArrayFields(object.getClass());
        for(Field field : stringArrayFields) {
            log.debug("Verifying string array field : " + field.getName());
            try {
                field.setAccessible(true);
                String[] collection = (String[])field.get(object); // throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
                for (String input : collection) {
                    log.debug("Verifying string array value : " + input);
                    validateStringField(object, field, input); // 20131012
                }
            } catch (Exception e) {
                // throw new MWException( ... failed to validate object so don't let it continue ...);
                throw new MWException(ErrorCode.AS_INPUT_VALIDATION_ERROR);
            }
        }

        Set<Field> stringCollectionFields = getStringCollectionFields(object.getClass());
        for(Field field : stringCollectionFields) {
            log.debug("Verifying string collection field : " + field.getName());
            try {
                field.setAccessible(true);
                List<String> collection = (List<String>) field.get(object); // throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
                for (String input : collection) {
                    log.debug("Verifying string collection value : " + input);
                    validateStringField(object, field, input); // 20131012
                }
            } catch (Exception e) {
                throw new MWException(ErrorCode.AS_INPUT_VALIDATION_ERROR);
            }
        }

        Set<Field> customObjectFields = getCustomObjectFields(object.getClass());
        for(Field field : customObjectFields) {
            log.debug("Verifying custom object field : " + field.getName());
            try {
                field.setAccessible(true);
                Object customObject = (Object)field.get(object); // throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
                validate(customObject, stack);
            } catch (Exception e) {
                throw new MWException(ErrorCode.AS_INPUT_VALIDATION_ERROR);
            }
        }
        
        Set<Field> customObjectArrayFields = getCustomObjectArrayFields(object.getClass());
        for(Field field : customObjectArrayFields) {
            log.debug("Verifying custom object array field : " + field.getName());
            try {
                field.setAccessible(true);
                Object[] collection = (Object[])field.get(object); // throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
                for (Object customObject : collection) {
                    validate(customObject, stack);
                }
            } catch (Exception e) {
                throw new MWException(ErrorCode.AS_INPUT_VALIDATION_ERROR);
            }
        }

        Set<Field> customObjectCollectionFields = getCustomObjectCollectionFields(object.getClass());
        for(Field field : customObjectCollectionFields) {
            log.debug("Verifying custom object collection field : " + field.getName());
            try {
                field.setAccessible(true);
                List<Object> collection = (List<Object>) field.get(object); // throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
                for (Object customObject : collection) {
                    validate(customObject, stack);
                }
            } catch (Exception e) {
                throw new MWException(ErrorCode.AS_INPUT_VALIDATION_ERROR);
            }
        }
        
        // now validate the object
        Set<Method> stringMethods = getStringMethods(object.getClass());
        for(Method method : stringMethods) {
            log.debug("Verifying string method : " + method.getName());
            try {
                String input = (String)method.invoke(object); // throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
                log.debug("Verifying method return value : " + input);
                validateStringMethod(object, method, input); // 20131012
            } catch (Exception e) {
                throw new MWException(ErrorCode.AS_INPUT_VALIDATION_ERROR);
            }
        }
        
        Set<Method> stringArrayMethods = getStringArrayMethods(object.getClass());
        for(Method method : stringArrayMethods) {
            log.debug("Verifying string array method : " + method.getName());
            try {
                String[] collection = (String[])method.invoke(object); // throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
                for (String input : collection) {
                    log.debug("Verifying string array method return value : " + input);
                    validateStringMethod(object, method, input); // 20131012
                }
            } catch (Exception e) {
                throw new MWException(ErrorCode.AS_INPUT_VALIDATION_ERROR);
            }
        }                

        Set<Method> stringCollectionMethods = getStringCollectionMethods(object.getClass());
        for(Method method : stringCollectionMethods) {
            log.debug("Verifying string collection method : " + method.getName());
            try {
                List<String> collection = (List<String>) method.invoke(object); // throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
                for(String input : collection) {
                    log.debug("Verifying string collection method return value : " + input);
                    validateStringMethod(object, method, input); // 20131012
                }
            } catch (Exception e) {
                throw new MWException(ErrorCode.AS_INPUT_VALIDATION_ERROR);
            }
        } 
        
        Set<Method> customMethods = getCustomObjectMethods(object.getClass());
        for(Method method : customMethods) {
            log.debug("Verifying custom object method : " + method.getName());
            try {
                Object customObject = method.invoke(object); // throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
                validate(customObject, stack);
            } catch (Exception e) {
                throw new MWException(ErrorCode.AS_INPUT_VALIDATION_ERROR);
            }
        } 
        
        Set<Method> customObjectArrayMethods = getCustomObjectArrayMethods(object.getClass());
        for(Method method : customObjectArrayMethods) {
            log.debug("Verifying custom object array method : " + method.getName());
            try {
                Object[] customObjectCollection = (Object[])method.invoke(object); // throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
                for (Object customObject : customObjectCollection) {                
                    validate(customObject, stack);
                }
            } catch (Exception e) {
                throw new MWException(ErrorCode.AS_INPUT_VALIDATION_ERROR);
            }
        } 
        
        Set<Method> customObjectCollectionMethods = getCustomObjectCollectionMethods(object.getClass());
        for(Method method : customObjectCollectionMethods) {
            log.debug("Verifying custom object collection method : " + method.getName());
            try {
                List<Object> customObjectCollection = (List<Object>) method.invoke(object); // throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
                for (Object customObject : customObjectCollection) {
                    validate(customObject, stack);
                }
            } catch (Exception e) {
                throw new MWException(ErrorCode.AS_INPUT_VALIDATION_ERROR);
            }
        }                
    }
        
    private static Pattern getPattern(String regex) {
        Pattern pattern = patternMap.get(regex);
        if( pattern == null ) {
            pattern = Pattern.compile(regex);
            patternMap.put(regex, pattern);
        }
        return pattern;
    }

    private static void validateInput(String input) {
        validateInput(input, getPattern(ValidationRegEx.DEFAULT_PATTERN.getRegEx()));    // 20131012
    }

    private static void validateInput(String input, Pattern pattern) {
        Matcher matcher = pattern.matcher(input);
        if (!matcher.matches()) {
            log.debug("Illegal characters found in : " + input);
            throw new IllegalArgumentException();
        }
    }

    private static void validateConnectionString(String input) {
        ConnectionString.VendorConnection cs = null;
        if (input != null && ! input.isEmpty()) {
            try {
                // Construct the connection string object so that we can extract the individual elements and validate them
                cs = ConnectionString.parseConnectionString(input);
            } catch (MalformedURLException ex) {
                log.error("Connection string specified is invalid. {}", ex.getMessage());
                throw new IllegalArgumentException();
            }
            // validate the management server name, port, host name
            validateInput(cs.url.getHost(), getPattern(ValidationRegEx.IPADDR_FQDN_PATTERN.getRegEx()));
            validateInput(Integer.toString(cs.url.getPort()), getPattern(ValidationRegEx.PORT.getRegEx()));
            if (!cs.options.isEmpty()) {
                validateInput(cs.options.getString(ConnectionString.OPT_HOSTNAME), getPattern(ValidationRegEx.IPADDR_FQDN_PATTERN.getRegEx()));
                validateInput(cs.options.getString(ConnectionString.OPT_USERNAME), getPattern(ValidationRegEx.DEFAULT_PATTERN.getRegEx()));
                validateInput(cs.options.getString(ConnectionString.OPT_PASSWORD), getPattern(ValidationRegEx.PASSWORD_PATTERN.getRegEx()));
            }
        }
    }

    /**
     * This function would be used to validate the basic data types. Ex: Validate("Test") or Validate(new String{}..) or Validate(new ArrayList<String>....)
     * 
     * @param object
     * @return 
     */
    private static boolean validateBasicDataType(Object object) {
        boolean continueValidation = true;
        boolean isBasic = isBuiltInType(object.getClass());
        if (isBasic) {
            if (object.getClass().equals(String.class)){
                validateInput((String)object);
                continueValidation = false;
            } else if (object.getClass().isArray() && object.getClass().equals(String[].class)) {
                continueValidation = false;
                Object[] objArray = (Object[]) object;
                for (Object obj : objArray){
                    validateInput((String)obj);
                }                
            } else {
                continueValidation = false; // since it is one of the basic types and is not a String[], we don't need to continue anyway
            }
        } else if (Collection.class.isAssignableFrom(object.getClass())) {
            List<Object> objList = (List<Object>) object;
            for (Object obj : objList){
                if (obj.getClass().equals(String.class)){
                    validateInput((String)obj);
                } else {
                    continueValidation = true;// This might be one of the custom objects
                    return continueValidation;
                }                    
            }
        }
        return continueValidation;
    }
    
    /**
     * This function verifies if the class is one of the built in datatypes or a custom class. This will also verify the arrays for
     * built-in data types.
     * @param clazz
     * @return 
     */
    private static boolean isBuiltInType(Class<?> clazz) {
        boolean isBuiltInType = clazz.isPrimitive() || 
                clazz.equals(Boolean.class) || clazz.equals(Boolean[].class) || clazz.equals(Number.class) || clazz.equals(Number[].class) ||
                clazz.equals(Float.class) || clazz.equals(Float[].class)|| clazz.equals(Integer.class) || clazz.equals(Integer[].class) ||
                clazz.equals(Byte.class) || clazz.equals(Byte[].class) || clazz.equals(Double.class) || clazz.equals(Double[].class) ||
                clazz.equals(Short.class) || clazz.equals(Short[].class) || clazz.equals(Long.class) || clazz.equals(Long[].class) || 
                clazz.equals(Character.class) || clazz.equals(Character[].class) || clazz.equals(String.class) || clazz.equals(String[].class);
        return isBuiltInType;
    }

    /**
     * 
     * @param field
     * @return 
     */
    private static boolean isStringField(Field field) {
        boolean isPublic = field.getModifiers() == Modifier.PUBLIC;
        boolean stringReturn =  field.getType().isAssignableFrom(String.class);// String.class.isAssignableFrom((Class<?>)field.getGenericType());            
        return isPublic && stringReturn;
    }
    
    /**
     * 
     * @param clazz
     * @return 
     */
    private static Set<Field> getStringFields(Class<?> clazz) {
        HashSet<Field> stringFields = new HashSet<Field>();
        Field[] declaredFields = clazz.getDeclaredFields();
        for(Field field : declaredFields ) {
            if (isStringField(field))
                stringFields.add(field);
        }
        return stringFields;
    }
    
    /**
     * 
     * @param field
     * @return 
     */
    private static boolean isStringArrayField(Field field) {
        boolean isPublic = field.getModifiers() == Modifier.PUBLIC;
        boolean isArray = field.getType().isArray();
        boolean stringReturn = field.getType().isAssignableFrom(String[].class); //String[].class.isAssignableFrom((Class<?>)field.getGenericType());            
        return isPublic && isArray && stringReturn;
    }

    /**
     * 
     * @param clazz
     * @return 
     */
    private static Set<Field> getStringArrayFields(Class<?> clazz) {
        HashSet<Field> stringArrayFields = new HashSet<Field>();
        Field[] declaredFields = clazz.getDeclaredFields();
        for(Field field : declaredFields ) {
            if (isStringArrayField(field))
                stringArrayFields.add(field);
        }
        return stringArrayFields;
    }

    /**
     * 
     * @param field
     * @return 
     */
    private static boolean isStringCollectionField(Field field) {
        boolean isPublic = field.getModifiers() == Modifier.PUBLIC;
        boolean isList = Collection.class.isAssignableFrom(field.getType()); // java.util.List.class.isAssignableFrom(field.getType());
        // boolean stringReturn = field.toGenericString().contains("java.util.List<java.lang.String>");            
        boolean stringReturn = false;
        if (isList) {
            ParameterizedType stringListType = (ParameterizedType) field.getGenericType();
            Class<?> stringListClass = (Class<?>) stringListType.getActualTypeArguments()[0];
            stringReturn = stringListClass.isAssignableFrom(String.class);
        }
        return isPublic && isList && stringReturn;
    }

    /**
     * 
     * @param clazz
     * @return 
     */
    private static Set<Field> getStringCollectionFields(Class<?> clazz) {
        HashSet<Field> stringCollectionFields = new HashSet<Field>();
        Field[] declaredFields = clazz.getDeclaredFields();
        for(Field field : declaredFields ) {
            if (isStringCollectionField(field))
                stringCollectionFields.add(field);
        }
        return stringCollectionFields;
    }

    /**
     * 
     * @param field
     * @return 
     */
    private static boolean isCustomObjectField(Field field) {
        boolean isPublic = field.getModifiers() == Modifier.PUBLIC;
        boolean isArrayOrCollection = field.getType().isArray() || Collection.class.isAssignableFrom(field.getType());
        boolean builtInObjectReturn = isBuiltInType(field.getType());
        boolean customObjectReturn = !builtInObjectReturn;
        return isPublic && !isArrayOrCollection && customObjectReturn;
    }

    /**
     * 
     * @param clazz
     * @return 
     */
    private static Set<Field> getCustomObjectFields(Class<?> clazz) {
        HashSet<Field> customObjectFields = new HashSet<Field>();
        Field[] declaredFields = clazz.getDeclaredFields();
        for(Field field : declaredFields ) {
            if (isCustomObjectField(field))
                customObjectFields.add(field);
        }
        return customObjectFields;
    }

    /**
     * 
     * @param field
     * @return 
     */
    private static boolean isCustomObjectArrayField(Field field) {
        boolean isPublic = field.getModifiers() == Modifier.PUBLIC; 
        boolean isArray = field.getType().isArray();
        boolean builtInObjectReturn = false;
        if (isArray) {
            builtInObjectReturn = isBuiltInType(field.getType());            
        }
        boolean customObjectReturn = !builtInObjectReturn;
        return isPublic && isArray && customObjectReturn;
    }

    /**
     * 
     * @param clazz
     * @return 
     */
    private static Set<Field> getCustomObjectArrayFields(Class<?> clazz) {
        HashSet<Field> customObjectArrayFields = new HashSet<Field>();
        Field[] declaredFields = clazz.getDeclaredFields();
        for(Field field : declaredFields ) {
            if (isCustomObjectArrayField(field))
                customObjectArrayFields.add(field);
        }
        return customObjectArrayFields;
    }
    
    /**
     * 
     * @param field
     * @return 
     */
    private static boolean isCustomObjectCollectionField(Field field) {
        boolean isPublic = field.getModifiers() == Modifier.PUBLIC; 
        boolean isCollection = Collection.class.isAssignableFrom(field.getType());
        boolean builtInObjectReturn = false;
        if (isCollection) {
            ParameterizedType stringListType = (ParameterizedType) field.getGenericType();
            Class<?> stringListClass = (Class<?>) stringListType.getActualTypeArguments()[0];
            builtInObjectReturn = isBuiltInType(stringListClass);            
        }
        boolean customObjectReturn = !builtInObjectReturn;
        return isPublic && isCollection && customObjectReturn;
    }

    /**
     * 
     * @param clazz
     * @return 
     */
    private static Set<Field> getCustomObjectCollectionFields(Class<?> clazz) {
        HashSet<Field> customObjectCollectionFields = new HashSet<Field>();
        Field[] declaredFields = clazz.getDeclaredFields();
        for(Field field : declaredFields ) {
            if (isCustomObjectCollectionField(field))
                customObjectCollectionFields.add(field);
        }
        return customObjectCollectionFields;
    }    
    
    /**
     * 
     * @param method
     * @return 
     */
    private static boolean isStringMethod(Method method) {
        boolean conventional = method.getName().startsWith("get") || method.getName().startsWith("to");
        boolean noArgs = method.getParameterTypes().length == 0;
        boolean stringReturn = method.getReturnType().isAssignableFrom(String.class);
        return conventional && noArgs && stringReturn;
    }
    
    /**
     * 
     * @param clazz
     * @return 
     */
    private static Set<Method> getStringMethods(Class<?> clazz) {
        HashSet<Method> stringMethods = new HashSet<Method>();
        Method[] methods =  clazz.getDeclaredMethods();                
        for(Method method : methods ) {
            if( isStringMethod(method) ) {
                stringMethods.add(method);
            }
        }
        return stringMethods;
    }
    
    /**
     * 
     * @param method
     * @return 
     */
    private static boolean isStringArrayMethod(Method method) {
        boolean conventional = method.getName().startsWith("get") || method.getName().startsWith("to");
        boolean noArgs = method.getParameterTypes().length == 0;
        boolean isArray = method.getReturnType().isArray();
        boolean stringReturn = String[].class.isAssignableFrom(method.getReturnType());
        return conventional && noArgs && stringReturn && isArray;
    }    

    /**
     * 
     * @param clazz
     * @return 
     */
    private static Set<Method> getStringArrayMethods(Class<?> clazz) {
        HashSet<Method> stringMethods = new HashSet<Method>();
        Method[] methods =  clazz.getDeclaredMethods();                
        for(Method method : methods ) {
            if( isStringArrayMethod(method) ) {
                stringMethods.add(method);
            }
        }
        return stringMethods;        
    }    

    /**
     * 
     * @param method
     * @return 
     */
    private static boolean isStringCollectionMethod(Method method) {
        boolean conventional = method.getName().startsWith("get") || method.getName().startsWith("to");
        boolean noArgs = method.getParameterTypes().length == 0;        
        //boolean isList = java.util.List.class.isAssignableFrom(method.getReturnType());
        boolean isList = Collection.class.isAssignableFrom(method.getReturnType());
        boolean stringReturn = false;
        if (isList) {
            ParameterizedType stringListType = (ParameterizedType) method.getGenericReturnType();
            Class<?> stringListClass = (Class<?>) stringListType.getActualTypeArguments()[0];
            stringReturn = stringListClass.isAssignableFrom(String.class);
        }
        return conventional && noArgs && isList && stringReturn;
    }

    /**
     * 
     * @param clazz
     * @return 
     */
    private static Set<Method> getStringCollectionMethods(Class<?> clazz) {
        HashSet<Method> stringMethods = new HashSet<Method>();
        Method[] methods =  clazz.getDeclaredMethods();                
        for(Method method : methods ) {
            if( isStringCollectionMethod(method) ) {
                stringMethods.add(method);
            }
        }
        return stringMethods;        
    }    

    /**
     * 
     * @param method
     * @return 
     */
    private static boolean isCustomObjectMethod(Method method) {
        boolean conventional = method.getName().startsWith("get") || method.getName().startsWith("to");
        boolean noArgs = method.getParameterTypes().length == 0;
        Class<?> returnType = method.getReturnType();
        // Since we will process the Array and Collection return types separately, we need to first check for that
        boolean isArrayOrCollection = returnType.isArray() || Collection.class.isAssignableFrom(returnType);
        boolean builtInObjectReturn = isBuiltInType(returnType);
        boolean customObjectReturn = !builtInObjectReturn;
        return conventional && noArgs && !isArrayOrCollection && customObjectReturn;
    }
    
    /**
     * 
     * @param clazz
     * @return 
     */
    private static Set<Method> getCustomObjectMethods(Class<?> clazz) {
        HashSet<Method> customMethods = new HashSet<Method>();
        Method[] methods =  clazz.getDeclaredMethods();                
        for(Method method : methods ) {
            if( isCustomObjectMethod(method) ) {
                customMethods.add(method);
            }
        }
        return customMethods;        
    }    

    /**
     * 
     * @param method
     * @return 
     */
    private static boolean isCustomObjectArrayMethod(Method method) {
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

    /**
     * 
     * @param clazz
     * @return 
     */
    private static Set<Method> getCustomObjectArrayMethods(Class<?> clazz) {
        HashSet<Method> customObjectArrayMethods = new HashSet<Method>();
        Method[] methods =  clazz.getDeclaredMethods();                
        for(Method method : methods ) {
            if( isCustomObjectArrayMethod(method)) {
                customObjectArrayMethods.add(method);
            }
        }
        return customObjectArrayMethods;        
    }        

    /**
     * 
     * @param method
     * @return 
     */
    private static boolean isCustomObjectCollectionMethod(Method method) {
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
        
    /**
     * 
     * @param clazz
     * @return 
     */
    private static Set<Method> getCustomObjectCollectionMethods(Class<?> clazz) {
        HashSet<Method> customObjectCollectionMethods = new HashSet<Method>();
        Method[] methods =  clazz.getDeclaredMethods();                
        for(Method method : methods ) {
            if( isCustomObjectCollectionMethod(method)) {
                customObjectCollectionMethods.add(method);
            }
        }
        return customObjectCollectionMethods;        
    }    
    
}
