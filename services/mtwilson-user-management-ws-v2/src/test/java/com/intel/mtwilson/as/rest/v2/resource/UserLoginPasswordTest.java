/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.resource;

import com.intel.dcsg.cpg.crypto.RandomUtil;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.security.rest.v2.model.Status;
import com.intel.mtwilson.security.rest.v2.model.User;
import com.intel.mtwilson.security.rest.v2.model.UserCollection;
import com.intel.mtwilson.security.rest.v2.model.UserFilterCriteria;
import com.intel.mtwilson.security.rest.v2.model.UserLocator;
import com.intel.mtwilson.security.rest.v2.model.UserLoginPassword;
import com.intel.mtwilson.security.rest.v2.model.UserLoginPasswordCollection;
import com.intel.mtwilson.security.rest.v2.model.UserLoginPasswordFilterCriteria;
import com.intel.mtwilson.security.rest.v2.model.UserLoginPasswordLocator;
import com.intel.mtwilson.user.management.rest.v2.repository.UserLoginPasswordRepository;
import com.intel.mtwilson.user.management.rest.v2.repository.UserRepository;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.junit.Test;

/**
 *
 * @author ssbangal
 */
public class UserLoginPasswordTest {
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UserLoginPasswordTest.class);
    
    @Test
    public void testUserLoginPassword() throws Exception {
        UserLoginPasswordRepository repo = new UserLoginPasswordRepository();
        UUID userLoginPwdId = new UUID();

        UserLoginPassword loginPasswordInfo = new UserLoginPassword();
        loginPasswordInfo.setId(userLoginPwdId);
        loginPasswordInfo.setUserId(UUID.valueOf("8d29aa87-386d-490d-9491-ab0be4f5e7f9"));
        loginPasswordInfo.setAlgorithm("SHA256");
        loginPasswordInfo.setIterations(1);
        loginPasswordInfo.setSalt(RandomUtil.randomByteArray(8));
        loginPasswordInfo.setPasswordHash(RandomUtil.randomByteArray(8)); //"password".getBytes(Charset.forName("UTF-8"));
        loginPasswordInfo.setEnabled(false);
        repo.create(loginPasswordInfo);
        
        UserLoginPasswordFilterCriteria criteria = new UserLoginPasswordFilterCriteria();
        criteria.id = userLoginPwdId;
        UserLoginPasswordCollection search = repo.search(criteria);
        for (UserLoginPassword obj : search.getUserLoginPasswords()) {
            log.debug("User login password retrieved has roles {}", obj.getRoles().toString());
        }

        loginPasswordInfo.setEnabled(true);
        Set<String> roleSet = new HashSet<>(Arrays.asList("administrator", "tagadmin"));
        loginPasswordInfo.setRoles(roleSet);
        repo.store(loginPasswordInfo);
        
        UserLoginPasswordLocator locator = new UserLoginPasswordLocator();
        locator.id = userLoginPwdId;
        UserLoginPassword retrieve = repo.retrieve(locator);
        log.debug("User login password retrieved has roles {}", retrieve.getRoles().toString());

        repo.delete(locator);
        
    }    
    
}
