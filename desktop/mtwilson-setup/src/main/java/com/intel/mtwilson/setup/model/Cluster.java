/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.model;

/**
 *
 * @author jbuhacoff
 */
public class Cluster {
    private String[] endpoints; // public hostnames or IP addresses (for example the load balancers); for non-load-balanced clusters this is the same as members
    private String[] members; // each hostnames or IP addresses of each member instance of the cluster
}
