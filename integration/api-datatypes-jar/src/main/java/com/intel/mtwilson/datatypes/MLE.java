package com.intel.mtwilson.datatypes;

import org.apache.commons.lang3.Validate;

/**
 * Representation of a Measured Launch Environment (MLE) comprised
 * of a Bios and Vmm.
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
