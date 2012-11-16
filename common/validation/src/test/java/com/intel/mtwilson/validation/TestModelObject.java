/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.validation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.junit.Test;
import test.model.Color;

/**
 * Test features of the aspect oriented model validation
 * @author jbuhacoff
 */
public class TestModelObject {
    
    @Test
    public void testUseNoArgConstructorValid() {
        Color color = new Color();
        color.setName("test");
        color.setRed(1);
        color.setGreen(2);
        color.setBlue(3);
        boolean valid = color.isValid();
        System.out.println(String.format("testUseNoArgConstructorValid: Valid? %s", String.valueOf(valid)));
    }

    @Test
    public void testUseNoArgConstructorInvalid() {
        Color color = new Color();
        color.setName(null);
        color.setRed(1);
        color.setGreen(2);
        color.setBlue(3);
        boolean valid = color.isValid();
        System.out.println(String.format("testUseNoArgConstructorInvalid: Valid? %s", String.valueOf(valid)));
    }

    @Test
    public void testConstructorValid() {
        Color color = new Color("test", 1, 2, 3);
        boolean valid = color.isValid();
        System.out.println(String.format("testConstructorValid: Valid? %s", String.valueOf(valid)));
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testConstructorInvalid() {
        Color color = new Color(null, 1, 2, 3); // IllegalArgumentException because of null name
        boolean valid = color.isValid();
        System.out.println(String.format("testConstructorInvalid: Valid? %s", String.valueOf(valid)));
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
    
    @Test(expected=IllegalArgumentException.class)
    public void testCheckedModelParameterInvalid() {
        Color color = new Color();
        printColor(color);
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
        System.out.println(String.format("printModel: %s", String.valueOf(model.isValid())));
    }
    private void printUncheckedModel(@Unchecked Model model) {
        System.out.println(String.format("printUncheckedModel: %s", String.valueOf(model.isValid())));
    }
    
    /**
     * Because Color is a @Model class, it will be automatically validated when passed as a parameter here
     * @param color 
     */
    private void printColor(Color color) {
        System.out.println(String.format("printColor: %s", color.getName()));
    }

    /**
     * Because this Color parameter is annotated @Unchecked, it will not be checked.
     * @param color 
     */
    private void printUncheckedColor(@Unchecked Color color) {
        System.out.println(String.format("printUncheckedColor: %s", color.getName()));
    }
    
    @Test
    public void testUseHashCode() {
        Color color = new Color();
        boolean valid = color.isValid();
        System.out.println(String.format("testUseHashCode: Valid? %s"+String.valueOf(valid)));
        System.out.println(String.format("testUseHashCode: Hashcode for this object is %d", hashCode()));
    }

    @Test
    public void testModelInterface() {
        Color color = new Color("blue", 0, 0, 255);
        boolean valid = color.isValid();
        System.out.println(String.format("testModelInterface: Valid? %s"+String.valueOf(valid)));
    }
    
    @Test
    public void testModelInterfaceWithErrors() {
        Color color = new Color();
        boolean valid = color.isValid();
        System.out.println(String.format("testModelInterfaceWithErrors: Valid? %s"+String.valueOf(valid)));
    }
    
    @Test
    public void testReflection() {
        try {
//            Color color = new Color();
            Color color = new Color("blue", 0, 0, 255);
            Method isValid = color.getClass().getMethod("isValid");
            Boolean isValidResult = (Boolean)isValid.invoke(color);
            System.out.println("testReflection: color isValid? "+isValidResult);
        } catch (IllegalAccessException ex) {
            System.out.println("Illegal access exception while invoking isValid on color");
        } catch (IllegalArgumentException ex) {
            System.out.println("Illegal argument exception while invoking isValid on color");
        } catch (InvocationTargetException ex) {
            System.out.println("Invocation target  exception while invoking isValid on color");
        } catch (NoSuchMethodException ex) {
            System.out.println("Color object does not have an isValid method");
        } catch (SecurityException ex) {
            System.out.println("Security exception while checking for isValid method in Color object");
        }
    }
    
}
