/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.user.management.rest.v2.repository;

import com.intel.mtwilson.user.management.rest.v2.model.UserLoginHmac;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginHmacCollection;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginHmacFilterCriteria;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginHmacLocator;
import com.intel.mtwilson.i18n.ErrorCode;
import com.intel.mtwilson.jaxrs2.server.resource.DocumentRepository;
import org.apache.shiro.authz.annotation.RequiresPermissions;


/**
 *
 * @author ssbangal
 */
public class UserLoginHmacRepository implements DocumentRepository<UserLoginHmac, UserLoginHmacCollection, UserLoginHmacFilterCriteria, UserLoginHmacLocator> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UserLoginHmacRepository.class);
    
    @Override
    @RequiresPermissions("user_login_hmacs:search")        
    public UserLoginHmacCollection search(UserLoginHmacFilterCriteria criteria) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    @RequiresPermissions("user_login_hmacs:retrieve")        
    public UserLoginHmac retrieve(UserLoginHmacLocator locator) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    @RequiresPermissions("user_login_hmacs:store")        
    public void store(UserLoginHmac item) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    @RequiresPermissions("user_login_hmacs:create")        
    public void create(UserLoginHmac item) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    @RequiresPermissions("user_login_hmacs:delete")        
    public void delete(UserLoginHmacLocator locator) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    @RequiresPermissions("user_login_hmacs:delete,search")        
    public void delete(UserLoginHmacFilterCriteria criteria) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
