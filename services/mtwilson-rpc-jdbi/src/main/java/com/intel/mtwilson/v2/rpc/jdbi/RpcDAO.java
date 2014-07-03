/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.v2.rpc.jdbi;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.rpc.v2.model.Rpc;
import com.intel.mtwilson.rpc.v2.model.RpcPriv;
import java.util.Date;
import java.util.List;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlBatch;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.BatchChunkSize;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterArgumentFactory;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import com.intel.mtwilson.jdbi.util.UUIDArgument;

/**
 * References:
 * http://www.jdbi.org/five_minute_intro/
 * http://jdbi.org/sql_object_api_argument_binding/
 * http://skife.org/jdbi/java/library/sql/2011/03/16/jdbi-sql-objects.html  (map result set to object)
 * http://www.cowtowncoder.com/blog/archives/2010/04/entry_391.html
 * http://jdbi.org/sql_object_api_batching/   (batching)
 * 
 * @author jbuhacoff
 */
@RegisterArgumentFactory(UUIDArgument.class)
public interface RpcDAO {
    // disabling create because it's different dependign on the database system used ... between the popular mysql and postgres there are enough differences to make this useless.  for example blob vs bytea.
    // use the .sql scripts in mtwilson-postgresql and mtwilson-mysql instead.  
//    @SqlUpdate("create table certificate (id bigint primary key generated always as identity, uuid char(36), certificate blob, sha1 char(40), sha256 char(64), subject varchar(255), issuer varchar(255), notBefore timestamp, notAfter timestamp, revoked boolean)")
//    void create();
    
    @SqlUpdate("insert into mw_rpc (ID, Name, Input, Output, Status, ProgressCurrent, ProgressMax) values (:id, :name, :input, :output, :status, :progressCurrent, :progressMax)")
    void insert(@Bind("id") UUID id, @Bind("name") String name, @Bind("input") byte[] input, @Bind("output") byte[] output, @Bind("status") String status, @Bind("progressCurrent") Long progressCurrent, @Bind("progressMax") Long progressMax);

    @SqlUpdate("update mw_rpc set Name=:name, Input=:input, Output=:output, Status=:status, ProgressCurrent=:progressCurrent, ProgressMax=:progressMax WHERE ID=:id")
    void update(@Bind("id") UUID id, @Bind("name") String name, @Bind("input") byte[] input, @Bind("output") byte[] output, @Bind("status") String status, @Bind("progressCurrent") Long progressCurrent, @Bind("progressMax") Long progressMax);
    
    @SqlUpdate("update mw_rpc set Status=:status where ID=:id")
    void updateStatus(@Bind("id") UUID id, @Bind("status") String status);
    
    @SqlUpdate("update mw_rpc set ProgressCurrent=:progressCurrent,ProgressMax=:progressMax where ID=:id")
    void updateProgress(@Bind("id") UUID id, @Bind("progressCurrent") Long progressCurrent, @Bind("progressMax") Long progressMax);
    
    @RegisterMapper(RpcPrivResultMapper.class)
    @SqlQuery("select ID,Name,Input,Output,Status,ProgressCurrent,ProgressMax from mw_rpc where ID=:id")
    RpcPriv findById(@Bind("id") UUID id);

    @RegisterMapper(RpcResultMapper.class)
    @SqlQuery("select ID,Name,Status,ProgressCurrent,ProgressMax from mw_rpc where ID=:id")
    Rpc findStatusById(@Bind("id") UUID id);
    
    @RegisterMapper(RpcResultMapper.class)
    @SqlQuery("select ID,Name,Status,ProgressCurrent,ProgressMax from mw_rpc where Name=:name")
    List<Rpc> findStatusByName(@Bind("name") String name);
        
    @RegisterMapper(RpcResultMapper.class)
    @SqlQuery("select ID,Name,Status,ProgressCurrent,ProgressMax from mw_rpc where Status=:status")
    List<Rpc> findStatusByStatus(@Bind("status") String status);

    @SqlUpdate("delete from mw_rpc where ID=:id")
    void delete(@Bind("id") UUID id);
    
    void close();
}
