package com.intel.mtwilson.datatypes;

import org.apache.commons.lang3.Validate;

/**
 * Representation of a Measured Launch Environment (MLE) comprised
 * of a Bios and Vmm.
 * 
 * XXX TODO need to change the name of this class because throughout the codebase
 * we refer to Bios and Vmm separately as Mle's, so a pair (Bios,Vmm) should
 * be called something else to avoid confusion. Also an Os may be an Mle separate
 * from a Vmm so that adds to the confusion.
 * 
 * @since 0.5.1
 * @author jbuhacoff
 */
public class MLE {
    private Bios bios = null;
    private Vmm vmm = null;
    
    public MLE(Bios bios, Vmm vmm) {
        setBios(bios);
        setVmm(vmm);
    }
    
    public final void setBios(Bios bios) {
        Validate.notNull(bios);
        this.bios = bios;
    }

    public final void setVmm(Vmm vmm) {
        Validate.notNull(vmm);
        this.vmm = vmm;
    }
    
    public Bios getBios() { return bios; }
    public Vmm getVmm() { return vmm; }
    
    @Override
    public String toString() {
        return String.format("BIOS:%s,VMM:%s", bios.toString(), vmm.toString());
    }
    
    
}
