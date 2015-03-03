/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro;

import com.intel.dcsg.cpg.validation.Fault;
import com.intel.dcsg.cpg.validation.Faults;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jbuhacoff
 */
public class SearchResults<T> implements Faults {
        private ArrayList<Fault> faults = new ArrayList<>();
        private ArrayList<T> data = new ArrayList<>();

        @Override
        public List<Fault> getFaults() {
            return faults;
        }

        public List<T> getData() {
            return data;
        }
    
}
