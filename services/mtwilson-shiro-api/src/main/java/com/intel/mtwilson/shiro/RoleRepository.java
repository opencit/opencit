/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro;

/**
 *
 * @author jbuhacoff
 */
public interface RoleRepository {
    SearchResults<Role> searchRoles(RoleSearchCriteria criteria);
}
