/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package test.model.impl;

import com.intel.dcsg.cpg.validation.Fault;
import com.intel.dcsg.cpg.validation.InvalidModelException;
import com.intel.dcsg.cpg.validation.Model;
import com.intel.dcsg.cpg.validation.Unchecked;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.Test;
import test.model.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test features of the aspect oriented model validation
 * @author jbuhacoff
 */
public class TestModelObject {
    private static Logger log = LoggerFactory.getLogger(TestModelObject.class);
    
    @Test
    public void testUseNoArgConstructorValid() {
        Color color = new Color();
        color.setName("test");
        color.setRed(1);
        color.setGreen(2);
        color.setBlue(3);
        boolean valid = color.isValid();
        log.debug(String.format("testUseNoArgConstructorValid: Valid? %s", String.valueOf(valid)));
    }

    @Test
    public void testUseNoArgConstructorInvalid() {
        Color color = new Color();
        color.setName(null);
        color.setRed(1);
        color.setGreen(2);
        color.setBlue(3);
        boolean valid = color.isValid();
        log.debug(String.format("testUseNoArgConstructorInvalid: Valid? %s", String.valueOf(valid)));
    }

    @Test
    public void testConstructorValid() {
        Color color = new Color("test", 1, 2, 3);
        boolean valid = color.isValid();
        log.debug(String.format("testConstructorValid: Valid? %s", String.valueOf(valid)));
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testConstructorInvalid() {
        Color color = new Color(null, 1, 2, 3); // IllegalArgumentException because of null name
        boolean valid = color.isValid();
        log.debug(String.format("testConstructorInvalid: Valid? %s", String.valueOf(valid)));
    }
    
    @Test
    public void testCheckedModelParameterValid() {
        Color color = new Color("test", 1, 2, 3);
        printColor(color);
    }

    @Test
    public void testCheckedModelParameterValid2() {
        Color color = new Color("test", 1, 2, 3);
        printModel(color);
    }
    
    @Test(expected=InvalidModelException.class)
    public void testCheckedModelParameterInvalid() {
        Color color = new Color();
        printColor(color); // InvalidModelException thrown when we pass a model that isn't valid
    }

    @Test
    public void testUnheckedModelParameterValid() {
        Color color = new Color("test", 1, 2, 3);
        printUncheckedColor(color);
    }

    @Test
    public void testUnheckedModelParameterValid2() {
        Color color = new Color();
        printUncheckedModel(color);
    }
    

    @Test
    public void testUnheckedModelParameterInvalid() {
        Color color = new Color();
        printUncheckedColor(color);
    }

    
    private void printModel(Model model) {
        log.debug(String.format("printModel: %s", String.valueOf(model.isValid())));
    }
    private void printUncheckedModel(@Unchecked Model model) {
        log.debug(String.format("printUncheckedModel: %s", String.valueOf(model.isValid())));
    }
    
    /**
     * Because Color is a @Model class, it will be automatically validated when passed as a parameter here
     * @param color 
     */
    private void printColor(Color color) {
        log.debug(String.format("printColor: %s", color.getName()));
    }

    /**
     * Because this Color parameter is annotated @Unchecked, it will not be checked.
     * @param color 
     */
    private void printUncheckedColor(@Unchecked Color color) {
        log.debug(String.format("printUncheckedColor: %s", color.getName()));
    }
    
    @Test
    public void testUseHashCode() {
        Color color = new Color();
        boolean valid = color.isValid();
        log.debug(String.format("testUseHashCode: Valid? %s", String.valueOf(valid)));
        log.debug(String.format("testUseHashCode: Hashcode for this object is %d", hashCode()));
    }

    @Test
    public void testModelInterface() {
        Color color = new Color("blue", 0, 0, 255);
        boolean valid = color.isValid();
        log.debug(String.format("testModelInterface: Valid? %s", String.valueOf(valid)));
    }
    
    @Test
    public void testModelInterfaceWithErrors() {
        Color color = new Color();
        boolean valid = color.isValid();
        log.debug(String.format("testModelInterfaceWithErrors: Valid? %s", String.valueOf(valid)));
        List<Fault> faults = color.getFaults();
        for(Fault fault : faults) {
            log.debug("Fault: {}", fault);
        }
    }
    
    @Test
    public void testReflection() {
        try {
//            Color color = new Color();
            Color color = new Color("blue", 0, 0, 255);
            Method isValid = color.getClass().getMethod("isValid");
            Boolean isValidResult = (Boolean)isValid.invoke(color);
            log.debug("testReflection: color isValid? "+isValidResult);
        } catch (IllegalAccessException ex) {
            log.debug("Illegal access exception while invoking isValid on color");
        } catch (IllegalArgumentException ex) {
            log.debug("Illegal argument exception while invoking isValid on color");
        } catch (InvocationTargetException ex) {
            log.debug("Invocation target  exception while invoking isValid on color");
        } catch (NoSuchMethodException ex) {
            log.debug("Color object does not have an isValid method");
        } catch (SecurityException ex) {
            log.debug("Security exception while checking for isValid method in Color object");
        }
    }
    
}
