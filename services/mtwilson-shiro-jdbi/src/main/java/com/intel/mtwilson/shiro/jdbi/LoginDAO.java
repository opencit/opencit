/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.jdbi;

import com.intel.mtwilson.shiro.jdbi.model.*;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.jdbi.util.DateArgument;
import java.io.Closeable;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlBatch;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.BatchChunkSize;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterArgumentFactory;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.sqlobject.stringtemplate.UseStringTemplate3StatementLocator;
import org.skife.jdbi.v2.unstable.BindIn;
import com.intel.mtwilson.jdbi.util.UUIDArgument;
import org.skife.jdbi.v2.sqlobject.BindBean;

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
@UseStringTemplate3StatementLocator
@RegisterArgumentFactory({UUIDArgument.class, DateArgument.class, LocaleArgument.class, StatusArgument.class})
@RegisterMapper({UserResultMapper.class,UserKeystoreResultMapper.class,RoleResultMapper.class,RolePermissionResultMapper.class,UserLoginPasswordResultMapper.class,UserLoginPasswordRoleResultMapper.class,UserLoginHmacResultMapper.class,UserLoginHmacRoleResultMapper.class,UserLoginCertificateResultMapper.class,UserLoginCertificateRoleResultMapper.class})
public interface LoginDAO extends Closeable {
    // disabling create because it's different dependign on the database system used ... between the popular mysql and postgres there are enough differences to make this useless.  for example blob vs bytea.
    // use the .sql scripts in mtwilson-postgresql and mtwilson-mysql instead.  
    // TODO: check if JDBI has an update for database system dialects.
//    @SqlUpdate("create table certificate (id bigint primary key generated always as identity, uuid char(36), certificate blob, sha1 char(40), sha256 char(64), subject varchar(255), issuer varchar(255), notBefore timestamp, notAfter timestamp, revoked boolean)")
//    void create();
    
    /**
     * 
  id uuid NOT NULL,
  username character varying(255) NOT NULL,
  locale character varying(8) NULL,
  enabled boolean NOT NULL DEFAULT '0',
  status varchar(128) NOT NULL DEFAULT 'Pending',
  comment text DEFAULT NULL,
     */
    @SqlUpdate("insert into mw_user (id, username, locale, enabled, status, comment) values (:id, :username, :locale, :enabled, :status, :comment)")
    void insertUser(@Bind("id") UUID id, @Bind("username") String username, @Bind("locale") Locale locale, @Bind("enabled") boolean enabled, @Bind("status") Status status, @Bind("comment") String comment);

    @SqlUpdate("insert into mw_user (id, username, locale, enabled, status, comment) values (:id, :username, :locale, :enabled, :status, :comment)")
    void insertUser(@BindBean User user);
    
    @SqlUpdate("update mw_user set locale=:locale, comment=:comment WHERE id=:id")
    void updateUser(@Bind("id") UUID id, @Bind("locale") Locale locale, @Bind("comment") String comment);

    @SqlUpdate("update mw_user set enabled=:enabled, status=:status, comment=:comment WHERE id=:id")
    void enableUser(@Bind("id") UUID id, @Bind("enabled") boolean enabled, @Bind("status") Status status, @Bind("comment") String comment);

    @SqlQuery("select id,username,locale,enabled,status,comment from mw_user")
    List<User> findAllUsers();
    
    @SqlQuery("select id,username,locale,enabled,status,comment from mw_user where id=:id")
    User findUserById(@Bind("id") UUID id);
    
    @SqlQuery("select id,username,locale,enabled,status,comment from mw_user where username=:username")
    User findUserByName(@Bind("username") String username);
    
    @SqlUpdate("delete from mw_user where id=:id")
    void deleteUser(@Bind("id") UUID id);
    
    /**
  id uuid NOT NULL,
  user_id uuid NOT NULL,
  keystore bytea NOT NULL,
  keystore_format character varying(128) NOT NULL DEFAULT 'jks',
  comment text DEFAULT NULL,
     * 
     */
    @SqlUpdate("insert into mw_user_keystore (id, user_id, keystore, keystore_format, comment) values (:id, :user_id, :keystore, :keystore_format, :comment)")
    void insertUserKeystore(@Bind("id") UUID id, @Bind("user_id") UUID userId, @Bind("keystore") byte[] keystore, @Bind("keystore_format") String keystoreFormat, @Bind("comment") String comment);

    @SqlUpdate("update mw_user_keystore set keystore=:keystore, keystore_format=:keystore_format, comment=:comment WHERE id=:id")
    void updateUser(@Bind("id") UUID id, @Bind("keystore") byte[] keystore, @Bind("keystore_format") String keystoreFormat,  @Bind("comment") String comment);
    
    @SqlQuery("select id,user_id,keystore,keystore_format,comment from mw_user_keystore")
    List<UserKeystore> findAllUserKeystores();
    
    @SqlQuery("select id,user_id,keystore,keystore_format,comment from mw_user_keystore where id=:id")
    UserKeystore findUserKeystoreById(@Bind("id") UUID id);
    
    @SqlQuery("select id,user_id,keystore,keystore_format,comment from mw_user_keystore where user_id=:user_id")
    UserKeystore findUserKeystoreByUserId(@Bind("user_id") UUID userId);
    
    @SqlUpdate("delete from mw_user_keystore where id=:id")
    void deleteUserKeystoreById(@Bind("id") UUID id);
    
    /**
     * 
  id uuid NOT NULL,
  role_name character varying(200) NOT NULL,
  description text DEFAULT NULL,
     */    
    @SqlUpdate("insert into mw_role (id, role_name, description) values (:id, :role_name, :description)")
    void insertRole(@Bind("id") UUID id, @Bind("role_name") String roleName, @Bind("description") String description);

    @SqlUpdate("update mw_role set role_name=:role_name, description=:description where id=:id")
    void updateRole(@Bind("id") UUID id, @Bind("role_name") String roleName, @Bind("description") String description);

    @SqlQuery("select id,role_name,description from mw_role")
    List<Role> findAllRoles();
    
    @SqlQuery("select id,role_name,description from mw_role where id=:id")
    Role findRoleById(@Bind("id") UUID id);
    
    @SqlQuery("select id,role_name,description from mw_role where role_name=:role_name")
    Role findRoleByName(@Bind("role_name") String roleName);
    
    @SqlUpdate("delete from mw_role where id=:id")
    void deleteRole(@Bind("id") UUID id);

    
    /**
     * 
  role_id uuid NOT NULL,
  permit_domain character varying(200) DEFAULT NULL,
  permit_action character varying(200) DEFAULT NULL,
  permit_selection character varying(200) DEFAULT NULL,
     */
    @SqlUpdate("insert into mw_role_permission (role_id, permit_domain, permit_action, permit_selection) values (:role_id, :permit_domain, :permit_action, :permit_selection)")
    void insertRolePermission(@Bind("role_id") UUID roleId, @Bind("permit_domain") String permitDomain, @Bind("permit_action") String permitAction, @Bind("permit_selection") String permitSelection);

    @SqlUpdate("delete from mw_role_permission where role_id=:role_id, permit_domain=:permit_domain, permit_action=:permit_action, permit_selection=:permit_selection")
    void deleteRolePermission(@Bind("role_id") UUID roleId, @Bind("permit_domain") String permitDomain, @Bind("permit_action") String permitAction, @Bind("permit_selection") String permitSelection);
    

    /**
  id uuid DEFAULT NULL,
  user_id uuid DEFAULT NULL,
  password_hash bytea NOT NULL,
  salt bytea NOT NULL,
  iterations integer DEFAULT 1,
  algorithm character varying(128) NOT NULL,
  expires timestamp DEFAULT NULL,
  enabled boolean NOT NULL DEFAULT '0',
     * 
     */
    @SqlQuery("select id, user_id, password_hash, salt, iterations, algorithm, expires, enabled from mw_user_login_password where id=:id")
    UserLoginPassword findUserLoginPasswordById(@Bind("id") UUID id);

    @SqlQuery("select id, user_id, password_hash, salt, iterations, algorithm, expires, enabled from mw_user_login_password where user_id=:user_id")
    UserLoginPassword findUserLoginPasswordByUserId(@Bind("user_id") UUID userId);
    
    @SqlQuery("select mw_user_login_password.id as id, user_id, password_hash, salt, iterations, algorithm, expires, mw_user_login_password.enabled as enabled from mw_user join mw_user_login_password on mw_user.id=mw_user_login_password.user_id where mw_user.username=:username")
    UserLoginPassword findUserLoginPasswordByUsername(@Bind("username") String username);
    
    @SqlUpdate("insert into mw_user_login_password (id, user_id, password_hash, salt, iterations, algorithm, expires, enabled) values (:id, :user_id, :password_hash, :salt, :iterations, :algorithm, :expires, :enabled)")
    void insertUserLoginPassword(@Bind("id") UUID id, @Bind("user_id") UUID userId, @Bind("password_hash") byte[] password_hash, @Bind("salt") byte[] salt, @Bind("iterations") int iterations, @Bind("algorithm") String algorithm, @Bind("expires") Date expires, @Bind("enabled") boolean enabled);

    @SqlUpdate("update mw_user_login_password set password_hash=:password_hash, salt=:salt, iterations=:iterations, algorithm=:algorithm, expires=:expires, enabled=:enabled where id=:id")
    void updateUserLoginPassword(@Bind("password_hash") byte[] password_hash, @Bind("salt") byte[] salt, @Bind("iterations") int iterations, @Bind("algorithm") String algorithm, @Bind("expires") Date expires, @Bind("enabled") boolean enabled, @Bind("id") UUID id);

    @SqlUpdate("delete from mw_user_login_password where id=:id")
    void deleteUserLoginPasswordById(@Bind("id") UUID id);
    
    @SqlUpdate("insert into mw_user_login_password_role (login_password_id, role_id) values (:login_password_id, :role_id)")
    void insertUserLoginPasswordRole(@Bind("login_password_id") UUID loginPasswordId, @Bind("role_id") UUID roleId);

    @SqlUpdate("delete from mw_user_login_password_role where login_password_id=:login_password_id, role_id=:role_id")
    void deleteUserLoginPasswordRole(@Bind("login_password_id") UUID loginPasswordId, @Bind("role_id") UUID roleId);

    @SqlQuery("select id, role_name, description from mw_role join mw_user_login_password_role on mw_role.id = mw_user_login_password_role.role_id where mw_user_login_password_role.login_password_id=:login_password_id")
    List<Role> findRolesByUserLoginPasswordId(@Bind("login_password_id") UUID loginPasswordId);

    @SqlQuery("select role_id, permit_domain, permit_action, permit_selection from mw_role_permission where role_id in ( <role_ids> )")
    List<RolePermission> findRolePermissionsByPasswordRoleIds(@BindIn("role_ids") List<UUID> role_ids);
    
    
    /**
  id uuid DEFAULT NULL,
  user_id uuid DEFAULT NULL,
  certificate bytea NOT NULL,
  sha1_hash bytea NOT NULL,
  expires timestamp DEFAULT NULL,
  enabled boolean NOT NULL DEFAULT '0',
  status varchar(128) NOT NULL DEFAULT 'Pending',
  comment text,
     * 
     */
    @SqlQuery("select id, user_id, certificate, sha1_hash, sha256_hash, expires, enabled, status, comment from mw_user_login_certificate where id=:id")
    UserLoginCertificate findUserLoginCertificateById(@Bind("id") UUID id);

    @SqlQuery("select id, user_id, certificate, sha1_hash, sha256_hash, expires, enabled, status, comment from mw_user_login_certificate where user_id=:user_id")
    UserLoginCertificate findUserLoginCertificateByUserId(@Bind("user_id") UUID userId);
    
    @SqlQuery("select mw_user_login_certificate.id as id, user_id, certificate, sha1_hash, sha256_hash, mw_user_login_certificate.status as status, expires, mw_user_login_certificate.enabled as enabled,  mw_user_login_certificate.comment as comment from mw_user join mw_user_login_certificate on mw_user.id=mw_user_login_certificate.user_id where mw_user.username=:username")
    UserLoginCertificate findUserLoginCertificateByUsername(@Bind("username") String username);

    @SqlQuery("select mw_user_login_certificate.id as id, user_id, certificate, sha1_hash, sha256_hash, mw_user_login_certificate.status as status, expires, mw_user_login_certificate.enabled as enabled, mw_user_login_certificate.comment as comment from mw_user join mw_user_login_certificate on mw_user.id=mw_user_login_certificate.user_id where mw_user_login_certificate.sha1_hash=:fingerprint")
    UserLoginCertificate findUserLoginCertificateBySha1(@Bind("fingerprint") byte[] fingerprint);
    
    @SqlQuery("select mw_user_login_certificate.id as id, user_id, certificate, sha1_hash, sha256_hash, mw_user_login_certificate.status as status, expires, mw_user_login_certificate.enabled as enabled, mw_user_login_certificate.comment as comment from mw_user join mw_user_login_certificate on mw_user.id=mw_user_login_certificate.user_id where mw_user_login_certificate.sha256_hash=:fingerprint")
    UserLoginCertificate findUserLoginCertificateBySha256(@Bind("fingerprint") byte[] fingerprint);    
    
    @SqlUpdate("insert into mw_user_login_certificate (id, user_id, certificate, sha1_hash, sha256_hash, expires, enabled, status, comment) values (:id, :user_id, :certificate, :sha1_hash, :sha256_hash, :expires, :enabled, :status, :comment)")
    void insertUserLoginCertificate(@Bind("id") UUID id, @Bind("user_id") UUID userId, @Bind("certificate") byte[] certificate, @Bind("sha1_hash") byte[] sha1Hash, @Bind("sha256_hash") byte[] sha256Hash, @Bind("expires") Date expires, @Bind("enabled") boolean enabled, @Bind("status") Status status, @Bind("comment") String comment);
    
    @SqlUpdate("delete from mw_user_login_certificate where id=:id")
    void deleteUserLoginCertificateById(@Bind("id") UUID id);
        
    
    @SqlUpdate("insert into mw_user_login_certificate_role (login_certificate_id, role_id) values (:login_certificate_id, :role_id)")
    void insertUserLoginCertificateRole(@Bind("login_certificate_id") UUID loginCertificateId, @Bind("role_id") UUID roleId);

    @SqlUpdate("delete from mw_user_login_certificate_role where login_certificate_id=:login_certificate_id, role_id=:role_id")
    void deleteUserLoginCertificateRole(@Bind("login_certificate_id") UUID loginCertificateId, @Bind("role_id") UUID roleId);

    @SqlQuery("select id, role_name, description from mw_role join mw_user_login_certificate_role on mw_role.id = mw_user_login_certificate_role.role_id where mw_user_login_certificate_role.login_certificate_id=:login_certificate_id")
    List<Role> findRolesByUserLoginCertificateId(@Bind("login_certificate_id") UUID loginCertificateId);

    @SqlQuery("select role_id, permit_domain, permit_action, permit_selection from mw_role_permission where role_id in ( <role_ids> )")
    List<RolePermission> findRolePermissionsByCertificateRoleIds(@BindIn("role_ids") List<UUID> role_ids);
        
    
}
