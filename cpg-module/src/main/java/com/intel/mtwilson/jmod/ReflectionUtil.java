/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jmod;

import com.intel.mtwilson.jmod.annotations.Activate;
import com.intel.mtwilson.jmod.annotations.Component;
import com.intel.mtwilson.jmod.annotations.Connect;
import com.intel.mtwilson.jmod.annotations.Deactivate;
import com.intel.mtwilson.jmod.annotations.Disconnect;
import com.intel.mtwilson.jmod.annotations.Export;
import com.intel.mtwilson.jmod.annotations.Notice;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 * @author jbuhacoff
 */
public class ReflectionUtil {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ReflectionUtil.class);
        
    public static boolean hasNoArgConstructor(Class<?> clazz) {
        Constructor<?>[] constructors = clazz.getConstructors();
        for(Constructor<?> constructor : constructors) {
            if( constructor.getParameterTypes().length == 0 ) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean hasComponentAnnotation(Class<?> clazz) {
        return clazz.isAnnotationPresent(Component.class);
    }
    
    public static boolean isActivateMethod(Method method) {
        boolean annotated = method.isAnnotationPresent(Activate.class) && !method.isAnnotationPresent(Deactivate.class);
        boolean conventional = method.getName().equals("activate");
        boolean noArgs = method.getParameterTypes().length == 0;
        boolean noReturn = method.getReturnType().getName().equals("void");
        return (annotated || conventional) && noArgs && noReturn;
    }
    
    public static boolean isDeactivateMethod(Method method) {
        boolean annotated = method.isAnnotationPresent(Deactivate.class) && !method.isAnnotationPresent(Activate.class);
        boolean conventional = method.getName().equals("deactivate");
        boolean noArgs = method.getParameterTypes().length == 0;
        boolean noReturn = method.getReturnType().getName().equals("void");
        return (annotated || conventional) && noArgs && noReturn;
    }

    public static boolean isNoticeMethod(Method method) {
        boolean annotated = method.isAnnotationPresent(Notice.class) && !method.isAnnotationPresent(Connect.class);
        boolean conventional = method.getName().equals("notice");
        boolean oneArg = method.getParameterTypes().length == 1;
        boolean notPrimitive = oneArg && !method.getParameterTypes()[0].isPrimitive();
        boolean noReturn = method.getReturnType().getName().equals("void");
        return (annotated || conventional) && oneArg && notPrimitive && noReturn;
    }

    public static boolean isConnectMethod(Method method) {
        boolean annotated = method.isAnnotationPresent(Connect.class) && !method.isAnnotationPresent(Notice.class);
        boolean conventional = method.getName().equals("connect");
        boolean oneArg = method.getParameterTypes().length == 1;
        boolean notPrimitive = oneArg && !method.getParameterTypes()[0].isPrimitive();
        boolean noReturn = method.getReturnType().getName().equals("void");
        return (annotated || conventional) && oneArg && notPrimitive && noReturn;
    }

    public static boolean isDisconnectMethod(Method method) {
        boolean annotated = method.isAnnotationPresent(Disconnect.class) && !method.isAnnotationPresent(Notice.class);
        boolean conventional = method.getName().equals("disconnect");
        boolean oneArg = method.getParameterTypes().length == 1;
        boolean notPrimitive = oneArg && !method.getParameterTypes()[0].isPrimitive();
        boolean noReturn = method.getReturnType().getName().equals("void");
        return (annotated || conventional) && oneArg && notPrimitive && noReturn;
    }
    
    public static boolean isExportMethod(Method method) {
        boolean annotated = method.isAnnotationPresent(Export.class);
        boolean conventional = method.getName().startsWith("get") || method.getName().startsWith("export");
        boolean noArgs = method.getParameterTypes().length == 0;
        boolean notPrimitiveReturn = !method.getReturnType().isPrimitive();
        return (annotated || conventional) && noArgs && notPrimitiveReturn;
    }
    
    public static Method getActivateMethod(Class<?> clazz) {
        Method[] methods =  clazz.getDeclaredMethods();                
        // look for activation annotation first
        for(Method method : methods) {
            if( method.isAnnotationPresent(Activate.class) ) {
                if( isActivateMethod(method) ) {
                    return method;
                }
                else {
                    log.error("Method {} in class {} is annotated @Activate but does not meet criteria", method.getName(), clazz.getName());
                }
            }
        }
        // then look for a method matching the convention
        for(Method method : methods ) {
            if( isActivateMethod(method) ) {
                return method;
            }
        }
        return null;
    }
    
    public static Method getDeactivateMethod(Class<?> clazz) {
        Method[] methods =  clazz.getDeclaredMethods();                
        // look for activation annotation first
        for(Method method : methods) {
            if( method.isAnnotationPresent(Deactivate.class) ) {
                if( isDeactivateMethod(method) ) {
                    return method;
                }
                else {
                    log.error("Method {} in class {} is annotated @Deactivate but does not meet criteria",  method.getName(), clazz.getName() );
                }
            }
        }
        // then look for a method matching the convention
        for(Method method : methods ) {
            if( isDeactivateMethod(method) ) {
                return method;
            }
        }
        return null;
    }

    public static Set<Method> getExportMethods(Class<?> clazz) {
        HashSet<Method> exportMethods = new HashSet<Method>();
        Method[] methods =  clazz.getDeclaredMethods();                
        // look for export annotation first
        for(Method method : methods) {
            if( method.isAnnotationPresent(Export.class) ) {
                if( isExportMethod(method) ) {
                    exportMethods.add(method);
                }
                else {
                    log.error("Method {} in class {} is annotated @Export but does not meet criteria",  method.getName(), clazz.getName() );
                }
            }
        }
        // then look for a method matching the convention
        for(Method method : methods ) {
            if( isExportMethod(method) ) {
                exportMethods.add(method);
            }
        }
        return exportMethods;
    }
    
    
    public static Set<Class<?>> getNoticeTypes(Class<?> clazz) {
        HashSet<Class<?>> typeset = new HashSet<Class<?>>();
        Method[] methods =  clazz.getDeclaredMethods();                
        // look for notice annotation first
        for(Method method : methods) {
            if( method.isAnnotationPresent(Notice.class) ) {
                if( isNoticeMethod(method) ) {
                    typeset.add(method.getParameterTypes()[0]); // guaranteed to have exactly one element because we check with isNoticeMethod
                }
                else {
                    log.error("Method {} in class {} is annotated @Notice but does not meet criteria",method.getName(), clazz.getName());
                }
            }
        }
        // then look for a method matching the convention
        for(Method method : methods ) {
            if( isNoticeMethod(method) ) {
                typeset.add(method.getParameterTypes()[0]); // guaranteed to have exactly one element because we check with isNoticeMethod
            }
        }
        return typeset;
    }
    
    /**
     * Note there is no disconnect types... a component can only implement disconnection for
     * types  for which it accepts a connection.
     * @param clazz
     * @return 
     */
    public static Set<Class<?>> getConnectTypes(Class<?> clazz) {
        HashSet<Class<?>> typeset = new HashSet<Class<?>>();
        Method[] methods =  clazz.getDeclaredMethods();                
        // look for connect annotation first
        for(Method method : methods) {
            if( method.isAnnotationPresent(Connect.class) ) {
                if( isConnectMethod(method) ) {
                    typeset.add(method.getParameterTypes()[0]); // guaranteed to have exactly one element because we check with isConnectMethod
                }
                else {
                    log.error("Method {} in class {} is annotated @Connect but does not meet criteria",  method.getName(), clazz.getName());
                }
            }
        }
        // then look for a method matching the convention
        for(Method method : methods ) {
            if( isConnectMethod(method) ) {
                typeset.add(method.getParameterTypes()[0]); // guaranteed to have exactly one element because we check with isConnectMethod
            }
        }
        return typeset;
    }
    
    public static Method getNoticeMethodForType(Class<?> clazz, Class<?> arg) {
        Method[] methods =  clazz.getDeclaredMethods();                
        // look for notification annotation first
        for(Method method : methods) {
            if( method.isAnnotationPresent(Notice.class) ) {
                if( isNoticeMethod(method) && method.getParameterTypes()[0].isAssignableFrom(arg) ) {
                    return method;
                }
            }
        }
        // then look for a method matching the convention
        for(Method method : methods ) {
            if( isNoticeMethod(method) && method.getParameterTypes()[0].isAssignableFrom(arg) ) {
                return method;
            }
        }
        return null;
        
    }

    public static Method getConnectMethodForType(Class<?> clazz, Class<?> arg) {
        Method[] methods =  clazz.getDeclaredMethods();                
        // look for connection annotation first
        for(Method method : methods) {
            if( method.isAnnotationPresent(Connect.class) ) {
                if( isConnectMethod(method) && method.getParameterTypes()[0].isAssignableFrom(arg) ) {
                    return method;
                }
            }
        }
        // then look for a method matching the convention
        for(Method method : methods ) {
            if( isConnectMethod(method) && method.getParameterTypes()[0].isAssignableFrom(arg) ) {
                return method;
            }
        }
        return null;
        
    }

    public static Method getDisconnectMethodForType(Class<?> clazz, Class<?> arg) {
        Method[] methods =  clazz.getDeclaredMethods();                
        // look for disconnection annotation first
        for(Method method : methods) {
            if( method.isAnnotationPresent(Disconnect.class) ) {
                if( isDisconnectMethod(method) && method.getParameterTypes()[0].isAssignableFrom(arg) ) {
                    return method;
                }
            }
        }
        // then look for a method matching the convention
        for(Method method : methods ) {
            if( isDisconnectMethod(method) && method.getParameterTypes()[0].isAssignableFrom(arg) ) {
                return method;
            }
        }
        return null;
        
    }
    
    /**
     * Use case for this method: we have a component that implements notice(A), notice(B), notice(C_extends_A) and
     * notice(D_extends_C), we want to find the best method to call for a given message.  If the message type is
     * D then we should call notice(D_extends_C).  If the message type is E_extends_C then we should call 
     * notice(C_extends_A) because there is no method for notice(E_extends_C) and C is the nearest type available.
     * If the object type is F_extends_G, then we would not be able call any of the notice methods because none of
     * them match. 
     * @param given
     * @param fromCollection
     * @return the type in the collection that is most specific to the given type, or null if a match was not found at all
     */
    public static Class<?> getMostSpecificType(Class<?> given, Collection<Class<?>> fromCollection) {
        Class<?> bestConnect = null;
        for(Class<?> type : fromCollection )  {
            // find any match at all
            if( type.isAssignableFrom(given) ) {
                // is it the best match?
                if( bestConnect == null || bestConnect.isAssignableFrom(type) ) {
                    bestConnect = type;
                }
            }
        }
        return bestConnect;
    }    
    
    
}
