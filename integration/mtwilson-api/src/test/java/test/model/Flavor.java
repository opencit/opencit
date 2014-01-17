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
public class Flavor implements Model {
    private String name;
    
    /**
     * No-arg constructor to facilitate creation via factory
     */
    public Flavor() {
        
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getName() { return name; }
    
    
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
