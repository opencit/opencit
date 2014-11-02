package com.intel.dcsg.cpg.validation;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff and ssbangal
 */
public class ValidationUtil {

    private static Logger log = LoggerFactory.getLogger(ValidationUtil.class);
    private static final HashMap<String, Pattern> patternMap = new HashMap<>();

    /**
     *
     * @param value
     * @param regex the surrounding ^ and $ are automatically added
     */
    public static boolean isValidWithRegex(String input, String regex) {
        Pattern pattern = getPattern(regex);
        if (input == null) {
            return false;
        }
        log.debug("Validating {} against regex {}", input, pattern.pattern());
        Matcher matcher = pattern.matcher(input);
        return matcher.matches();
    }

    /**
     * Validates the object
     * @param object 
     * @throws IllegalArgumentException if the object fails validation
     */
    public static void validate(Object object) {
        validate(object, new ArrayList<Object>());
    }

    /**
     * Validates each element in the array of objects
     * @param array
     * @throws IllegalArgumentException if any object fails validation
     */
    public static <T> void validate(T[] array) {
        for (int i = 0; i < array.length; i++) {
            validate(array[i]);
        }
    }

    /**
     * Validates each element in the collection of objects
     * @param collection
     * @throws IllegalArgumentException if any object fails validation
     */
    public static <T> void validate(Collection<T> collection) {
        for (Object object : collection) {
            validate(object);
        }
    }

    private static void validateWithValidatorClass(Object content, Class validatorClass) {
        log.debug("validateWithValidatorClass {}", validatorClass.getName());
        try {
            Object validatorObject = validatorClass.newInstance();
            InputValidator validator = (InputValidator) validatorObject;
            validator.setInput(content);
            if (!validator.isValid()) {
                List<Fault> faults = validator.getFaults();
                for (Fault fault : faults) {
                    log.error("Validation error: {}", fault.toString());
                }
                throw new IllegalArgumentException(); // XXX TODO maybe throw InputValidationException instead, with an argument new Report(validator) 
            }
        } catch (IllegalAccessException | InstantiationException e) {
            log.error("Error during custom validation", e);
            throw new IllegalArgumentException(validatorClass.getName(), e);
        }
    }

    private static void validateWithRegex(String content, String regex) {
        log.debug("Regex annotation: {}", regex);
        Pattern pattern = getPattern(regex);
        validateWithRegex(content, pattern);
    }

    private static Pattern getPattern(String regex) {
        Pattern pattern = patternMap.get(regex);
        if (pattern == null) {
            pattern = Pattern.compile("^" + regex + "$");
            patternMap.put(regex, pattern);
        }
        return pattern;
    }

    private static void validateWithRegex(String content, Pattern pattern) {
        if (content != null && !content.isEmpty()) {
            log.debug("Validating {} against regex {}", content, pattern.pattern());
            Matcher matcher = pattern.matcher(content);
            if (!matcher.matches()) {
                log.debug("Illegal characters found in : " + content);
                throw new IllegalArgumentException();
            }
        } else {
            log.debug("Skipping validating {} against {}", content, pattern.pattern());
        }
    }

    /**
     *
     * @param object instance to validate
     * @param context is the Field or Method from which we obtained the object
     * instance; we look for
     * @Unchecked and
     * @Validator annotations
     * @param contextName is the name of the Field or Method from which we
     * obtained the object instance
     * @param parent is the instance containing the Field or Method from which
     * we obtained the object instance
     */
    private static void validateObjectArray(Object[] array, AccessibleObject context, String contextName, Object parent, ArrayList<Object> visited) {
        if (context.isAnnotationPresent(Unchecked.class)) {
            log.debug("Object array of class {} in {} of {} is unchecked", array.getClass().getName(), contextName, parent.getClass().getName());
            return;
        }
        log.debug("validateObjectArray of length {}", array.length);
        // TODO:  if it's an array and annotated with a specific @ArrayValidator , use it directly
        /*
         if( context.isAnnotationPresent(Validator.class)) {
         Class validatorClass = context.getAnnotation(Validator.class).value();
         validateWithValidatorClass(object, validatorClass);
         return;
         }
         */
        // validate each item in the array
        for (int i = 0; i < array.length; i++) {
            Object object = array[i];
            validateObject(object, context, String.format("%s[%d]", contextName, i), parent, visited);
        }
    }
    
    /**
     * 
     * @param array of primitive type like byte[], char[], boolean[] etc.
     * @param context
     * @param contextName
     * @param parent
     * @param visited 
     */
    private static void validatePrimitiveArray(Object array, AccessibleObject context, String contextName, Object parent, ArrayList<Object> visited) {
        if (context.isAnnotationPresent(Unchecked.class)) {
            log.debug("Primitive array of class {} in {} of {} is unchecked", array.getClass().getComponentType().getName(), contextName, parent.getClass().getName());
            return;
        }
        log.debug("validatePrimitiveArray of class {}", array.getClass().getComponentType().getName());
        // TODO:  if it's an array and annotated with a specific @ArrayValidator , use it directly
        /*
         if( context.isAnnotationPresent(Validator.class)) {
         Class validatorClass = context.getAnnotation(Validator.class).value();
         validateWithValidatorClass(object, validatorClass);
         return;
         }
         */
        // because they are primitives we don't need to validate each object in the array
        // so the only validation is by a Validator or ArrayValidator
    }
    

    /**
     *
     * @param object instance to validate
     * @param context is the Field or Method from which we obtained the object
     * instance; we look for
     * @Unchecked and
     * @Validator annotations
     * @param contextName is the name of the Field or Method from which we
     * obtained the object instance
     * @param parent is the instance containing the Field or Method from which
     * we obtained the object instance
     */
    private static void validateObjectCollection(Collection<Object> collection, AccessibleObject context, String contextName, Object parent, ArrayList<Object> visited) {
        if (context.isAnnotationPresent(Unchecked.class)) {
            log.debug("Object collection of class {} in {} of {} is unchecked", collection.getClass().getName(), contextName, parent.getClass().getName());
            return;
        }
        log.debug("validateObjectCollection of size {}", collection.size());
        // TODO:  if it's an array and annotated with a specific @ArrayValidator , use it directly
        /*
         if( context.isAnnotationPresent(Validator.class)) {
         Class validatorClass = context.getAnnotation(Validator.class).value();
         validateWithValidatorClass(object, validatorClass);
         return;
         }
         */
        // validate each item in the collection
        /*
         ParameterizedType stringListType = (ParameterizedType) field.getGenericType();
         Class<?> stringListClass = (Class<?>) stringListType.getActualTypeArguments()[0];
         stringReturn = stringListClass.isAssignableFrom(String.class);
         * 
         */
        int i = 0;
        for (Object object : collection) {
            validateObject(object, context, String.format("%s[%d]", contextName, i), parent, visited);
            i++;
        }
    }

    /**
     *
     * @param object instance to validate, may be null
     * @param context is the Field or Method from which we obtained the object
     * instance; we look for
     * @Unchecked and
     * @Validator annotations
     * @param contextName is the name of the Field or Method from which we
     * obtained the object instance
     * @param parent is the instance containing the Field or Method from which
     * we obtained the object instance
     */
    private static void validateObject(Object object, AccessibleObject context, String contextName, Object parent, ArrayList<Object> visited) {
        String parentName = (parent == null ? "(no parent)" : parent.getClass().getName());
        if (context != null) {
            context.setAccessible(true);
            // if it's null, ignore it ;  TODO:  unless there is a non-null annotation which should cause a null value to fail!
            if (object == null) {
                // if context.isAnnotationPresent(Notnull.class) { throw new IllegalArgumentException("cannot be null"); }
                return;
            }
            if (context.isAnnotationPresent(Unchecked.class)) {
                log.debug("Object of class {} in {} of {} is unchecked", object.getClass().getName(), contextName, parentName);
                return;
            }
            // if it's annotated with a specific @Validator, use it directly    
            if (context.isAnnotationPresent(Validator.class)) {
                Class validatorClass = context.getAnnotation(Validator.class).value();
                log.debug("Object of class {} in {} of {} has validator {}", object.getClass().getName(), contextName, parentName, validatorClass.getName());
                validateWithValidatorClass(object, validatorClass);
                return;
            }
            // if it's any object annotated with a specific @Regex, use it directly  on the string representation 
            if (context.isAnnotationPresent(Regex.class)) {
                String content = object.toString();
                String regex = context.getAnnotation(Regex.class).value();
                log.debug("Object of class {} in {} of {} has regex {}", object.getClass().getName(), contextName, parentName, regex);
                validateWithRegex(content, regex);
                return;
            }
        }

        // at this point we either didn't have a field or method context to begin with,
        // or we did but there was no annotation on it to guide the validation.

        // nothing to do if the object is null
        if (object == null) {
            return;
        }

        // if it's a String, use the default regex (custom regex would have already been handled above via the annotation)
        if (String.class.isAssignableFrom(object.getClass())) {
            String content = (String) object;
            log.debug("Validating String in {} of class {} with default regex: {}", contextName, parentName, content);
            validateWithRegex(content, RegexPatterns.DEFAULT);
            return;
        }
        // if it's one of the 8 java primitives, ignore it
        if (isPrimitive(object.getClass())) {
            log.debug("Skipping primitive type {} in {} of class {}", object.getClass().getName(), contextName, parentName);
            return;
        }
        // recursive deep validation for anything else
        log.debug("Recursive validation for object of type {} in {} of class {}, already visited {}", object.getClass().getName(), contextName, parentName, visited.size());
        validateObjectRecursive(object, contextName, parent, visited);
    }

    /**
     *
     * @param object must not be null
     * @param contextName field or method name from which we got the object, or
     * empty string
     * @param parent object with the field or method name from which we got the
     * object; may be null if this is the initial object being validated
     * @param visited
     */
    private static void validateObjectRecursive(Object object, String contextName, Object parent, ArrayList<Object> visited) {
        String parentName = (parent == null ? "(no parent)" : parent.getClass().getName());
        // first check if the object being requested is already in the visited list... if so we skip it to avoid infinite recursion
        for (Object item : visited) {
            if (object.equals(item)) {
                return;
            } // object instance equality is intentional here to prevent infinite recursion
        }
        // add the object to the visited list so we don't try to validate it again if it has a self-referential property ... this is not a stack beacuse we never pop it 
        visited.add(object);

        log.debug("Starting recursive validation for object of type {} in {} of class {}", object.getClass().toString(), contextName, parentName);

        // validate all the public member fields
        Field[] fields = object.getClass().getFields();
        for (Field field : fields) {
            field.setAccessible(true);
            if (isPublicField(field)) {
                Object content;
                try {
                    content = field.get(object);
                } catch (SecurityException | IllegalAccessException e) {
                    log.warn("Cannot access field {} of class {}", field.getName(), object.getClass().getName(), e);
                    continue;
                }
                validateAnyType(content, field, String.format("%s.%s", contextName,field.getName()), object, visited);
            }
        }

        // validate all the public accessor methods
        Method[] methods = object.getClass().getMethods();
        for (Method method : methods) {
            method.setAccessible(true);
            if (isPublicMethod(method)) {
                Object content;
                try {
                    content = method.invoke(object);
                } catch (SecurityException | IllegalAccessException | InvocationTargetException e) {
                    log.warn("Cannot invoke method {} of class {}", method.getName(), object.getClass().getName(), e);
                    continue;
                }
                validateAnyType(content, method, String.format("%s.%s", contextName, method.getName()), object, visited);
            }
        }

    }

    // figure out if it's a single object, or an array, or a collection, and call the appropriate specific function
    private static void validateAnyType(Object object, AccessibleObject context, String contextName, Object parent, ArrayList<Object> visited) {
        if( object == null ) {
            validateObject(object, context, contextName, parent, visited); // validateObject allows null and may one day check for a non-null annotation
        }
        else if (object.getClass().isArray() && object.getClass().getComponentType().isPrimitive()) {
            validatePrimitiveArray(object, context, contextName, parent, visited);
        } 
        else if (object.getClass().isArray()) {
            Object[] array = (Object[]) object;
            validateObjectArray(array, context, contextName, parent, visited);
        } 
        else if (Collection.class.isAssignableFrom(object.getClass())) {
            Collection<Object> collection = (Collection<Object>) object;
            validateObjectCollection(collection, context, contextName, parent, visited);
        } 
        else {
            validateObject(object, context, contextName, parent, visited);
        }
    }

    private static boolean isPublicField(Field field) {
        boolean notStatic = !Modifier.isStatic(field.getModifiers());
        boolean isPublic = Modifier.isPublic(field.getModifiers());
        return notStatic && isPublic;
    }

    private static boolean isPublicMethod(Method method) {
        boolean notStatic = !Modifier.isStatic(method.getModifiers());
        boolean isPublic = Modifier.isPublic(method.getModifiers());
        boolean conventional = method.getName().startsWith("get") || method.getName().startsWith("to");
        boolean noArgs = method.getParameterTypes().length == 0;
        return notStatic && isPublic && conventional && noArgs;
    }

    private static boolean isPrimitive(Class<?> clazz) {
        return clazz.isPrimitive() || clazz.getName().startsWith("java.") || clazz.getName().startsWith("javax.");
    }

    private static void validate(Object object, ArrayList<Object> visited) {
        if (object == null) {
            return;
        }
        validateObject(object, null, "this", null, visited);
    }
}