/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro;

import java.util.ArrayList;

/**
 *
 * @author jbuhacoff
 */
public class Role {
    private String name;
    private ArrayList<String> permissions = new ArrayList<>();

    public String getName() {
        return name;
    }

    public ArrayList<String> getPermissions() {
        return permissions;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPermissions(ArrayList<String> permissions) {
        this.permissions = permissions;
    }
    
    
}
