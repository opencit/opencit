/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package test.model;

import com.intel.dcsg.cpg.validation.Fault;
import com.intel.dcsg.cpg.validation.Model;
import java.util.List;

/**
 * A model object for testing purposes
 * @author jbuhacoff
 */
public class Widget implements Model {
    private String name;
    private Color color;
    private Flavor flavor;
    
    /**
     * No-arg constructor to facilitate creation via factory
     */
    public Widget() {
        
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getName() { return name; }
    
    public void setColor(Color color) {
        this.color = color;
    }
    
    public Color getColor() { return color; }
    
    public void setFlavor(Flavor flavor) {
        this.flavor = flavor;
    }
    
    public Flavor getFlavor() { return flavor; }
    
    @Override
    public boolean isValid() {
        return true;
    }
    
    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        return true;
    }

    @Override
    public List<Fault> getFaults() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    
}
