/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package validation;

import com.intel.mtwilson.datatypes.Pcr;
import com.intel.mtwilson.validation.*;
import org.junit.Test;

/**
 * Test features of the aspect oriented model validation
 * @author jbuhacoff
 */
public class TestModelObject {
    
    @Test
    public void testUseOneArgConstructorValid() {
        Pcr pcr = new Pcr(0);
        boolean valid = pcr.isValid();
        System.out.println(String.format("testUseNoArgConstructorValid: Valid? %s    value? %d", String.valueOf(valid), pcr.toInteger()));
    }

    @Test
    public void testUseOneArgConstructorInvalid() {
        Pcr pcr = new Pcr(-1);
        boolean valid = pcr.isValid();
        System.out.println(String.format("testUseNoArgConstructorInvalid: Valid? %s", String.valueOf(valid)));
    }
    
    @Test
    public void testCheckedModelParameterValid() {
        Pcr pcr = new Pcr(1);
        printPcr(pcr);
        printModel(pcr);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testCheckedModelParameterInvalid() {
        Pcr pcr = new Pcr(-1);
        printPcr(pcr);
        printModel(pcr);
    }

    @Test
    public void testUnheckedModelParameterValid() {
        Pcr pcr = new Pcr(2);
        printUncheckedPcr(pcr);
        printUncheckedModel(pcr);
    }

    @Test
    public void testUnheckedModelParameterInvalid() {
        Pcr pcr = new Pcr(-2);
        printUncheckedPcr(pcr);
        printUncheckedModel(pcr);
    }

    
    private void printModel(Model model) {
        System.out.println(String.format("printModel: %s", String.valueOf(model.isValid())));
    }
    private void printUncheckedModel(@Unchecked Model model) {
        System.out.println(String.format("printUncheckedModel: %s", String.valueOf(model.isValid())));
    }
    
    /**
     * Because Pcr is a @Model class, it will be automatically validated when passed as a parameter here
     * @param pcr 
     */
    private void printPcr(Pcr pcr) {
        System.out.println(String.format("printPcr: %d", pcr.toInteger()));
    }

    /**
     * Because this Pcr parameter is annotated @Unchecked, it will not be checked.
     * @param pcr 
     */
    private void printUncheckedPcr(@Unchecked Pcr pcr) {
        System.out.println(String.format("printUncheckedPcr: %d", pcr.toInteger()));
    }
    
    
}
