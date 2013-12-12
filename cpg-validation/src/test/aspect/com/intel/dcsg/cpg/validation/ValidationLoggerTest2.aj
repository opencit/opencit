package com.intel.dcsg.cpg.validation;

import java.lang.reflect.Method;
import org.aspectj.lang.reflect.MethodSignature;
import org.aspectj.lang.Signature;
import java.lang.annotation.Annotation;

public aspect ValidationLoggerTest2 {
/* TODO: need to define annotation such as @AutoHashCode for automatic creation of hashcode? 
    pointcut logHashcode() :
        @within(Model) && call(* hashCode());

    before(): logHashcode() {
        System.out.println("entering hashcode");
    }
  */  


/*
    // this works but only on methods that have just ONE Model parameter... if there are two then it doesn't even execute the advice for either one

    pointcut logModelParameter(Model model) :
        execution(* *.*(.., !@Unchecked (Model+), ..)) && !within(ValidationLogger) && args(model);

    before(Model model): logModelParameter(model)  {
        System.out.println("entering method with checked model parameter.  isValid? "+String.valueOf(model.isValid()));
    }

    pointcut logUncheckedModelParameter(Model model) :
        execution(* *.*(.., @Unchecked (Model+), ..)) && !within(ValidationLogger) && args(model);

    before(Model model): logUncheckedModelParameter(model) {
        System.out.println("entering method with unchecked model parameter.  isValid? "+String.valueOf(model.isValid()));
    }
*/

    pointcut logModelParameter() :
        execution(* *.*(.., !@Unchecked (Model+), ..)) && !within(ValidationLogger);

    before(): logModelParameter()  {
        final Object[] args = thisJoinPoint.getArgs();
        final boolean[] isUnchecked = new boolean[args.length];
        System.out.println("entering method with checked model parameter.  # args:  "+String.valueOf(args.length));
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
                System.out.println("   arg "+String.valueOf(i+1)+" is a Model and valid = "+String.valueOf(model.isValid()));
            }
        }
    }

/*
    pointcut logUncheckedModelParameter() :
        execution(* *.*(.., @Unchecked (Model+), ..)) && !within(ValidationLogger);

    before(Model model): logUncheckedModelParameter() {
        System.out.println("entering method with unchecked model parameter.  isValid? "+String.valueOf(model.isValid()));
    }
*/

}
