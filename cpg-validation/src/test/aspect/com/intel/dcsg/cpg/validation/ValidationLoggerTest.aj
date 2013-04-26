package com.intel.dcsg.cpg.validation;

public aspect ValidationLoggerTest {
/* TODO: need to define annotation such as @AutoHashCode for automatic creation of hashcode? 
    pointcut logHashcode() :
        @within(Model) && call(* hashCode());

    before(): logHashcode() {
        System.out.println("entering hashcode");
    }
  */  

/*
    pointcut isValidMethod() : execution(public boolean *.isValid());
    after() returning : isValidMethod() {
        System.out.println("Hello from AspectJ");
      }


    pointcut logAnyModelMethod() :
        execution(* Model+.*()) ; // && within(Model+)


    before(): logAnyModelMethod() {
        System.out.println("calling method parameter "); // isValid? "+String.valueOf(model.isValid()));
    }
*/

/* // hmm... causes NoSuchMethodError when the advice is executed...
    pointcut logAllCallsWithSingleModelParameter(Model model) :
        execution(* *.*(Model+)) && !within(ValidationLogger) && args(model);

    before(Model model): logAllCallsWithSingleModelParameter(model) {
        System.out.println("logAllCallsWithSingleModelParameter: entering method with model parameter. ");// isValid? "+String.valueOf(model.isValid()));
    }
*/

/*

// works for methods with single parameter Color that implements interface Model

    pointcut logModelParameterSingle(Model model) :
        execution(* *.*(!@Unchecked (Model+))) && !within(ValidationLogger) && args(model);

    before(Model model): logModelParameterSingle(model) {
        System.out.println("entering method with single checked model parameter.  isValid? "+String.valueOf(model.isValid()));
    }


    pointcut logUncheckedModelParameterSingle(Model model) :
        execution(* *.*(@Unchecked (Model+))) && !within(ValidationLogger) && args(model);


    before(Model model): logUncheckedModelParameterSingle(model) {
        System.out.println("entering method with single unchecked model parameter.  isValid? "+String.valueOf(model.isValid()));
    }

*/

/*
    pointcut logUncheckedObjectParameterSingle(Object model) :
        execution(* *.*(@Unchecked (*))) && !within(ValidationLogger) && args(model);


    before(Object model): logUncheckedObjectParameterSingle(model) {
        System.out.println("entering method with single unchecked object parameter. "); // isValid? "+String.valueOf(model.isValid()));
    }
*/


    pointcut logModelParameter(Model model) :
        execution(* *.*(.., !@Unchecked (Model+), ..)) && !within(ValidationLoggerTest) && args(model);

    before(Model model): logModelParameter(model) {
        System.out.println("entering method with checked model parameter.  isValid? "+String.valueOf(model.isValid()));
    }

    pointcut logUncheckedModelParameter(Model model) :
        execution(* *.*(.., @Unchecked (Model+), ..)) && !within(ValidationLoggerTest) && args(model);

    before(Model model): logUncheckedModelParameter(model) {
        System.out.println("entering method with unchecked model parameter.  isValid? "+String.valueOf(model.isValid()));
    }

}
