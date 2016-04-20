/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package validation;

import com.intel.dcsg.cpg.validation.Model;
import com.intel.dcsg.cpg.validation.Unchecked;
import com.intel.mtwilson.model.PcrIndex;
import org.junit.Test;

/**
 * Test features of the aspect oriented model validation
 * @author jbuhacoff
 */
public class TestModelObject {
    
    @Test
    public void testUseOneArgConstructorValid() {
        PcrIndex pcr = new PcrIndex(0);
        boolean valid = pcr.isValid();
        System.out.println(String.format("testUseNoArgConstructorValid: Valid? %s    value? %d", String.valueOf(valid), pcr.toInteger()));
    }

    @Test
    public void testUseOneArgConstructorInvalid() {
        PcrIndex pcr = new PcrIndex(-1);
        boolean valid = pcr.isValid();
        System.out.println(String.format("testUseNoArgConstructorInvalid: Valid? %s", String.valueOf(valid)));
    }
    
    @Test
    public void testCheckedModelParameterValid() {
        PcrIndex pcr = new PcrIndex(1);
        printPcr(pcr);
        printModel(pcr);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testCheckedModelParameterInvalid() {
        PcrIndex pcr = new PcrIndex(-1);
        printPcr(pcr);
        printModel(pcr);
    }

    @Test
    public void testUnheckedModelParameterValid() {
        PcrIndex pcr = new PcrIndex(2);
        printUncheckedPcr(pcr);
        printUncheckedModel(pcr);
    }

    @Test
    public void testUnheckedModelParameterInvalid() {
        PcrIndex pcr = new PcrIndex(-2);
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
    private void printPcr(PcrIndex pcr) {
        System.out.println(String.format("printPcr: %d", pcr.toInteger()));
    }

    /**
     * Because this Pcr parameter is annotated @Unchecked, it will not be checked.
     * @param pcr 
     */
    private void printUncheckedPcr(@Unchecked PcrIndex pcr) {
        System.out.println(String.format("printUncheckedPcr: %d", pcr.toInteger()));
    }
    
    
}
