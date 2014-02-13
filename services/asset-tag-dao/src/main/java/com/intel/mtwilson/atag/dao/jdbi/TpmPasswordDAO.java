/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.intel.mtwilson.atag.dao.jdbi;



import com.intel.mtwilson.atag.model.TpmPassword;
import java.util.List;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlBatch;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
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
public interface TpmPasswordDAO {
 
    @SqlUpdate("create table tpm_password (id bigint primary key generated always as identity, uuid varchar(255), password varchar(255))")
    void create();
    
    @SqlUpdate("insert into tpm_password (uuid,password) values (:uuid, :password)")
    @GetGeneratedKeys
    long insert(@Bind("uuid") String uuid, @Bind("password") String password);
       
    @SqlQuery("select id,uuid,password from tpm_password where id=:id")
    TpmPassword findById(@Bind("id") long id);

    @SqlQuery("select id,uuid,password from tpm_password where uuid=:uuid")
    TpmPassword findByUuid(@Bind("uuid") String uuid);
    
    @SqlQuery("delete from tpm_password where uuid=:uuid")
    void deleteByUuid(@Bind("uuid") String uuid);
    
    void close();
    
}
