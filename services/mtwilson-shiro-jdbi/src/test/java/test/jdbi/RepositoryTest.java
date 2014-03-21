/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.jdbi;

import com.intel.dcsg.cpg.crypto.RandomUtil;
import com.intel.dcsg.cpg.crypto.Sha256Digest;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.util.ByteArray;
import com.intel.mtwilson.My;
import com.intel.mtwilson.shiro.authc.password.PasswordCredentialsMatcher;
import com.intel.mtwilson.shiro.jdbi.*;
import com.intel.mtwilson.shiro.jdbi.model.*;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * If you are setting up a test environment you should run the unit tests
 * in this order:
 * 
 * testCreateRole
 * testCreateUser
 * 
 * 
 * References:
 * Validation queries: http://stackoverflow.com/questions/3668506/efficient-sql-test-query-or-validation-query-that-will-work-across-all-or-most
 * 
 * @author jbuhacoff
 */
public class RepositoryTest {
    private static Logger log = LoggerFactory.getLogger(RepositoryTest.class);

    @Test
    public void testCreateRole() throws Exception {
        LoginDAO dao = MyJdbi.authz();
        
        //create a new role
        Role role = new Role();
        role.setId(new UUID());
        role.setRoleName("root");
        role.setDescription("for testing only");
        dao.insertRole(role.getId(), role.getRoleName(), role.getDescription());

        log.debug("Created role {} with id {}", role.getRoleName(), role.getId()); // for example: Created role root with id 6a382588-ab03-4973-b751-0ae927e1a639

        // add permissions to this role
        RolePermission rolePermission = new RolePermission();
        rolePermission.setRoleId(role.getId());
        rolePermission.setPermitDomain("*");
        rolePermission.setPermitAction("*");
        rolePermission.setPermitSelection("*");
        dao.insertRolePermission(rolePermission.getRoleId(), rolePermission.getPermitDomain(), rolePermission.getPermitAction(), rolePermission.getPermitSelection());
        
        dao.close();
    }
    
    @Test
    public void testCreateUser() throws Exception {
        LoginDAO dao = MyJdbi.authz();
        
        //create a new user
        User user = new User();
        user.setId(new UUID());
        user.setUsername(My.configuration().getKeystoreUsername());
        user.setStatus(Status.APPROVED);
        user.setEnabled(true);
        user.setComment("test");
        dao.insertUser(user.getId(), user.getUsername(), user.getLocale(), user.isEnabled(), user.getStatus(), user.getComment());

        log.debug("Created username {} with id {}", user.getUsername(), user.getId()); // for example: Created username jonathan with id 84ff12f4-6a68-495c-a70d-174cb07e45ce

        // set a password for the user
        UserLoginPassword userLoginPassword = new UserLoginPassword();
        userLoginPassword.setId(new UUID());
        userLoginPassword.setUserId(user.getId());
//        userLoginPassword.setUserId(UUID.valueOf("84ff12f4-6a68-495c-a70d-174cb07e45ce"));
        userLoginPassword.setSalt(RandomUtil.randomByteArray(8));
        userLoginPassword.setIterations(1000);
        userLoginPassword.setAlgorithm("SHA256");
        userLoginPassword.setEnabled(true);
        userLoginPassword.setPasswordHash(PasswordCredentialsMatcher.passwordHash(My.configuration().getKeystorePassword().getBytes(), userLoginPassword));
        dao.insertUserLoginPassword(userLoginPassword.getId(), userLoginPassword.getUserId(), userLoginPassword.getPasswordHash(), userLoginPassword.getSalt(), userLoginPassword.getIterations(), userLoginPassword.getAlgorithm(), userLoginPassword.getExpires(), userLoginPassword.isEnabled());
        
        // add a role for the user
        Role root = dao.findRoleByName("root");
        dao.insertUserLoginPasswordRole(userLoginPassword.getId(), root.getId());
        
        dao.close();
    }
    
    /**
     * uses  mtwilson.api.username and mtwilson.api.password from your local
     * mtwilson.properties  as the username and password 
     * 
     * @throws Exception 
     */
    @Test
    public void testResetPassword() throws Exception {
        LoginDAO dao = MyJdbi.authz();
        UserLoginPassword userLoginPassword = dao.findUserLoginPasswordByUsername(My.configuration().getKeystoreUsername());
        if( userLoginPassword == null ) {
            throw new IllegalArgumentException("No such user: "+My.configuration().getKeystoreUsername());
        }
        userLoginPassword.setSalt(RandomUtil.randomByteArray(8));
        userLoginPassword.setPasswordHash(PasswordCredentialsMatcher.passwordHash(My.configuration().getKeystorePassword().getBytes(), userLoginPassword));
        userLoginPassword.setEnabled(true);
        dao.updateUserLoginPassword(userLoginPassword.getPasswordHash(), userLoginPassword.getSalt(), userLoginPassword.getIterations(), userLoginPassword.getAlgorithm(), userLoginPassword.getExpires(), userLoginPassword.isEnabled(), userLoginPassword.getId());
        dao.close();
    }
    
    @Test
    public void testGetRolePermissions() throws Exception {
        LoginDAO dao = MyJdbi.authz();
        
        List<Role> roles = dao.findRolesByUserLoginPasswordId(UUID.valueOf("39bee0f8-9284-4d55-9abe-cf372e200e79")); //df10be6f-7d67-4e86-a7e4-a13169d9ce23"));
        ArrayList<UUID> roleIds = new ArrayList<>();
        for(Role role : roles) {
            log.debug("role: {}", role.getRoleName());
            roleIds.add(role.getId());
        }
        List<RolePermission> permissions = dao.findRolePermissionsByPasswordRoleIds(roleIds);
        for(RolePermission permission : permissions) {
            log.debug("permission: {} {} {}", permission.getPermitDomain(), permission.getPermitAction(), permission.getPermitSelection());
                    }
        dao.close();
    }
    
}
