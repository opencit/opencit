/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jmod;

/**
 * 
 * @author jbuhacoff
 */
public class ComponentSearchConfiguration {
    private String[] names = null;
    private boolean annotations = true;
    private boolean conventions = true;
//    private boolean searchNone = false; // when true, it's a special override to prevent any search at all
    
    public ComponentSearchConfiguration withAnnotations() { annotations = true; return this; }
    public ComponentSearchConfiguration noAnnotations() { annotations = false; return this; }
    public ComponentSearchConfiguration withConventions() { conventions = true; return this; }
    public ComponentSearchConfiguration noConventions() { conventions = false; return this; }
    public ComponentSearchConfiguration withNames(String[] classNames) { names = classNames; return this; }
    public ComponentSearchConfiguration noNames() { names = null; return this; }
//    public ComponentSearchConfiguration none() { searchNone = true; return this; }
//    public ComponentSearchConfiguration any() { searchNone = false; searchAnnotations = true; searchConventions = true; return this; }

//    public boolean isNone() { return searchNone; }
    public String[] names() { return names; }
    public boolean annotations() { return annotations; }
    public boolean conventions() { return conventions; }
    
}
