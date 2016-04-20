/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.shiro;

import com.intel.mtwilson.jaxrs2.server.resource.AbstractResource;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.jaxrs2.NoLinks;
import javax.ws.rs.Path;
import org.apache.shiro.authz.annotation.RequiresPermissions;

/**
 * NOTE:  this is just a test class;  in mtwilson 2.0 domains must be plural like "hosts" and "user_passwords"
 * 
 * Just a sample resource for testing
 * 
 * @author jbuhacoff
 */
//@Stateless
@Path("/user-passwords")
public class UserPasswords extends AbstractResource<UserPassword,UserPasswordCollection,UserPasswordFilterCriteria,NoLinks<UserPassword>> {

    @RequiresPermissions("user_password:read")
    @Override
    protected UserPasswordCollection search(UserPasswordFilterCriteria criteria) {
        UserPasswordCollection userPasswords = new UserPasswordCollection();
        UserPassword userPassword = new UserPassword();
        userPassword.setId(new UUID()); // id
        userPassword.setName("userPasswordabc"); // name
        userPassword.setPassword("babsdf"); // description
        userPasswords.getUserPasswords().add(userPassword);
        return userPasswords;
    }

    @RequiresPermissions("user_password:read")
    @Override
    protected UserPassword retrieve(String id) {
        UserPassword tmp = new UserPassword();
        tmp.setId(new UUID());
        tmp.setName("userPasswordxyz");
        tmp.setPassword("test userPassword");
        return tmp;
    }

    @RequiresPermissions("user_password:write")
    @Override
    protected void store(UserPassword item) {
        // store it...
    }
    @RequiresPermissions("user_password:write")
    @Override
    protected void create(UserPassword item) {
        // store it...
    }

    @RequiresPermissions("user_password:delete")
    @Override
    protected void delete(String id) {
    }

    @Override
    protected UserPasswordCollection createEmptyCollection() {
        return new UserPasswordCollection();
    }
    
}
