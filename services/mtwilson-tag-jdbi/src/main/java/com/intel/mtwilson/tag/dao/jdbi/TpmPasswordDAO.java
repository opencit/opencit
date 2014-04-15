/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.intel.mtwilson.tag.dao.jdbi;



import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.jdbi.util.DateArgument;
import com.intel.mtwilson.jdbi.util.UUIDArgument;
import com.intel.mtwilson.tag.model.TpmPassword;
import java.io.Closeable;
import java.util.Date;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterArgumentFactory;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;


/**
 *
 * References:
 * http://www.jdbi.org/five_minute_intro/
 * http://jdbi.org/sql_object_api_argument_binding/
 * http://skife.org/jdbi/java/library/sql/2011/03/16/jdbi-sql-objects.html  (map result set to object)
 * http://www.cowtowncoder.com/blog/archives/2010/04/entry_391.html
 * http://jdbi.org/sql_object_api_batching/   (batching)
 * 
 * @author stdalex
 */
@RegisterMapper(TpmPasswordResultMapper.class)
@RegisterArgumentFactory({UUIDArgument.class,DateArgument.class})
public interface TpmPasswordDAO extends Closeable {
 
    @SqlUpdate("create table mw_host_tpm_password (id char(36) primary key,password varchar(255), modifiedOn timestamp)")
    void create();
    
    @SqlUpdate("insert into mw_host_tpm_password (id, password, modifiedOn) values (:id, :password, :modifiedOn)")
    void insert(@Bind("id") UUID id, @Bind("password") String password, @Bind("modifiedOn") Date modifiedOn);
       
    @SqlUpdate("update mw_host_tpm_password set password=:password, modifiedOn=:modifiedOn where id=:id")
    void update(@Bind("id") UUID id, @Bind("password") String password, @Bind("modifiedOn") Date modifiedOn);

    @SqlQuery("select id, password, modifiedOn from mw_host_tpm_password where id=:id")
    TpmPassword findById(@Bind("id") UUID id);

//    @SqlQuery("select id,uuid,password from mw_host_tpm_password where uuid=:uuid")
//    TpmPassword findByUuid(@Bind("uuid") String uuid);
    
    @SqlUpdate("delete from mw_host_tpm_password where id=:id")
    void delete(@Bind("id") UUID id);
    
    @Override
    void close();
    
}
