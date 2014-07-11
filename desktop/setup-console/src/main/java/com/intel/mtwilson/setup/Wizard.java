/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup;

import java.util.Stack;

/**
 * just a draft
 * 
 * This is a driver for the setup requirements. It may be used by any user
 * interface in order to ensure that adequate input is gathered from the 
 * operator. 
 * 
 * The procedure is that we have a stack of items to configure and we always
 * work with the top item on the stack. Sometimes items may add sub-items to
 * the stack so those have to be done before the original items can be popped
 * off. Each configuration item may return a list of sub-items
 * to configure (for simplicity - in the order they should be configured), and
 * indicate when its ready to be popped off by returning "true" from its 
 * isDone() method.
 * 
 * @author jbuhacoff
 */
public class Wizard {
    private boolean confirm = false;
    private Stack<Panel> stack = new Stack<Panel>();
    
    /**
     * 
     * @param root the main panel that defines the configuration steps
     * @param confirm if every screen should be confirmed even if it has valid data (set to true when opening an existing configuration file to edit)
     */
    public Wizard(Panel root, boolean confirm) {
        stack.push(root);
    }
    
    // intent:  whatever panel we return from here is what needs to be shown to the user to complete.
    public Panel next() {
        if( stack.isEmpty() ) { return null; }
        Panel current = stack.pop();
        while( current.isComplete() ) {
            current = stack.pop();
        }
//        stack.push();
        return stack.pop();
    }
    
    public Panel previous() {
        return null;
    }
}
