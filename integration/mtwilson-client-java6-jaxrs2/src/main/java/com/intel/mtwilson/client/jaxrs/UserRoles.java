/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.client.jaxrs;

import com.intel.mtwilson.as.rest.v2.model.UserRole;
import com.intel.mtwilson.as.rest.v2.model.UserRoleCollection;
import java.net.URL;
import java.util.Properties;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
public class UserRoles extends MtWilsonClient {
    
    Logger log = LoggerFactory.getLogger(getClass().getName());

    public UserRoles(URL url) {
        super(url);
    }

    public UserRoles(Properties properties) throws Exception {
        super(properties);
    }
        
    public UserRoleCollection retrieveUserRoles() {
        log.debug("target: {}", getTarget().getUri().toString());
        UserRoleCollection objList = getTargetPath("users/roles").request(MediaType.APPLICATION_JSON).get(UserRoleCollection.class);
        return objList;
    }
    
}
