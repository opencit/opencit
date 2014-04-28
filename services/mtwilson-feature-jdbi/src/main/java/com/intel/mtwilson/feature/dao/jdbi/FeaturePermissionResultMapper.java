/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.feature.dao.jdbi;

import java.sql.ResultSet;
import java.sql.SQLException;
//import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.feature.model.FeaturePermission;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

/**
 * 
 * @author jbuhacoff
 */
public class FeaturePermissionResultMapper implements ResultSetMapper<FeaturePermission> {

    @Override
    public FeaturePermission map(int i, ResultSet rs, StatementContext sc) throws SQLException {
        FeaturePermission obj = new FeaturePermission();
//        obj.setId(UUID.valueOf(rs.getString("id")));
//        obj.setName(rs.getString("name"));
//        obj.setValue(rs.getString("value"));
        obj.featureId = rs.getString("feature_id");
        obj.featureName = rs.getString("feature_name");
        obj.permitDomain = rs.getString("permit_domain");
        obj.permitAction = rs.getString("permit_action");
        obj.permitSelection = rs.getString("permit_selection");
        obj.comment = rs.getString("comment");
        return obj;
    }
    
}
