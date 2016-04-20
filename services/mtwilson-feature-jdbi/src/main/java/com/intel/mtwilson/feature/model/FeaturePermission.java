/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.feature.model;

import com.intel.mtwilson.shiro.PermissionInfo;

/**
 *
 * @author jbuhacoff
 */
public class FeaturePermission {
        public String featureId = "00000000-0000-0000-0000-000000000000";
        public String featureName = "mtwilson-2.0";
        public String permitDomain;
        public String permitAction;
        public String permitSelection;
        public String comment;

        @Override
        public String toString() {
            PermissionInfo permissionInfo = new PermissionInfo(permitDomain, permitAction, permitSelection);
            return String.format("FeaturePermission[%s] %s %s", featureName, permissionInfo, (comment==null||comment.isEmpty()?"":String.format("(%s)", comment.replaceAll("\n","|"))));
        }
    
}
