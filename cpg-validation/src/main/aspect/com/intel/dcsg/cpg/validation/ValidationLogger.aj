package com.intel.dcsg.cpg.validation;

import java.lang.reflect.Method;
import org.aspectj.lang.reflect.MethodSignature;
import org.aspectj.lang.Signature;
import java.lang.annotation.Annotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

public aspect ValidationLogger {
    private Logger log = LoggerFactory.getLogger(ValidationLogger.class);

    /*
     * Identify all methods that have one or more implementations of Model as
     * parameters. 
     * The search includes all methods in all packages. 
     * The @Unchecked annotation indicates that the annotated parameter should
     * not be checked for validity. Therefore the method must have at least one
     * not-@Unchecked Model (or implementation of Model) parameter in order for
     * the advice to be applied.
     */
    pointcut logModelParameter() :
        execution(* *.*(.., !@Unchecked (Model+), ..)) && !within(ValidationLogger);

    /*
     * This advice applies to methods that have at least one parameter that implements
     * the Model interface. All parameters are checked (in case there is more than one
     * that implements Model) except for those marked with @Unchecked. The checked
     * parameters are logged if they return false from isValid(). 
     * In the future we may automatically throw an InvalidModelException.
     */
    before(): logModelParameter()  {
        final Object[] args = thisJoinPoint.getArgs();
        final boolean[] isUnchecked = new boolean[args.length];
        //System.out.println("entering method with checked model parameter.  # args:  "+String.valueOf(args.length));
        // we need the method signature so that we can check if any given Model parameter is @Unchecked
        final Signature signature = thisJoinPoint.getSignature();
        if( signature instanceof MethodSignature ) {
            final Method method = ((MethodSignature)signature).getMethod();
            final Annotation[][] paramAnnotations = method.getParameterAnnotations();
            // iterate over the parameters of the method
            for(int i=0; i<paramAnnotations.length; i++) {
                // iterate over the annotations for each parameter to see if it is unchecked
                for(Annotation a : paramAnnotations[i]) {
                    isUnchecked[i] = a instanceof Unchecked;
                }
            }
        }
        for(int i=0; i<args.length; i++) {
            final Object arg = args[i];
            if( arg instanceof Model && !isUnchecked[i]) {
                Model model = (Model)arg;
                if( !model.isValid() ) {
                    log.warn(String.format("Invalid model %s", arg.getClass().getName()));
                    List<Fault> faults = model.getFaults();
                    for(Fault fault : faults) {
                        log.warn(String.format("- %s", fault.toString()));
                    }
                    throw new InvalidModelException(model);
                }
            }
        }
    }

}
