/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.feature.dao.jdbi;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.feature.model.FeaturePermission;
import java.io.Closeable;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterArgumentFactory;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import com.intel.mtwilson.jdbi.util.UUIDArgument;
import java.util.List;
import org.skife.jdbi.v2.sqlobject.BindBean;

@RegisterArgumentFactory(UUIDArgument.class)
@RegisterMapper(FeaturePermissionResultMapper.class)
public interface FeaturePermissionDAO extends Closeable {
//    @SqlUpdate("create table mw_feature_permission (feature_id char(36) not null, feature_name , name varchar(255), value varchar(255))")
//    @SqlUpdate("create table mw_tag_kvattribute (id char(16) for bit data primary key, name varchar(100), value varchar(255))")
//    void create();
    
    @SqlUpdate("insert into mw_feature_permission (feature_id, feature_name, permit_domain, permit_action, permit_selection, comment) values (:feature_id, :feature_name, :permit_domain, :permit_action, :permit_selection, :comment)")
    void insert(@Bind("feature_id") UUID featureId, @Bind("feature_name") String featureName, @Bind("permit_domain") String permitDomain, @Bind("permit_action") String permitAction, @Bind("permit_selection") String permitSelection, @Bind("comment") String comment);
    @SqlUpdate("insert into mw_feature_permission (feature_id, feature_name, permit_domain, permit_action, permit_selection, comment) values (:featureId, :featureName, :permitDomain, :permitAction, :permitSelection)")
    void insert(@BindBean FeaturePermission featurePermission);

    @SqlUpdate("delete from mw_feature_permission where feature_id=:feature_id")
    void delete(@Bind("feature_id") UUID featureId);

    @SqlQuery("select feature_id, feature_name, permit_domain, permit_action, permit_selection, comment from mw_feature_permission where feature_id=:feature_id")
    List<FeaturePermission> findByFeatureId(@Bind("feature_id") UUID featureId);
    
    @SqlQuery("select feature_id, feature_name, permit_domain, permit_action, permit_selection, comment from mw_feature_permission where feature_name=:feature_name")
    List<FeaturePermission> findByFeatureName(@Bind("feature_name") String featureName);

    @Override
    void close();
}
