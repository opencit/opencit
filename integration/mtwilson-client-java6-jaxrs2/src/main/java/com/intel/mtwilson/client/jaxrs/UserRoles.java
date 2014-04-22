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
 * <code> UserRoles </code> is the class that represents the role of a User in Mount Wilson
 * @author ssbangal
 */
public class UserRoles extends MtWilsonClient {
    
    Logger log = LoggerFactory.getLogger(getClass().getName());

    public UserRoles(URL url)throws Exception {
        super(url);
    }

     /**
     * Initializes the <class>UserRoless</class> with the Mt.Wilson configuration properties.      * 
     * @param properties Properties associated with the Mt.Wilson configuration. 
     * Use the getClientProperties() of <class>MyConfiguration</class> for initializing.
     * @throws Exception 
     * 
     * <i><u>Sample Java API call :</u></i><br>
     * {@code 
     *  UserRoles client = new UserRoles(My.configuration().getClientProperties());
     * }
     */
    public UserRoles(Properties properties) throws Exception {
        super(properties);
    }
    
     /**
     * Retrieves the <code>UserRoles</code> object associated with a User.
     * @return <code> UserRoleCollection </code>, collection comprising the set of roles  associated with the User
     * <p>
     * <i><u>Roles Needed:</u></i> TO CHECK?
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: GET</i><br>
     * https://10.1.71.234:8443/mtwilson/v2/users/roles
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * {user_roles: [1]
     * 0:  
     *  {
     *   roles:[6]
     *   0:  "Security"
     *   1:  "Whitelist"
     *   2:  "Attestation"
     *   3:  "Report"
     *   4:  "Audit"
     *   5:  "AssetTagManagement"       
     *   }        
     * }
     * 
     *  
     * <i><u>Sample Java API call :</u></i><br>
     * {@code
     *  UserRoles client = new UserRoles(My.configuration().getClientProperties());
     *  UserRoleCollection roles = client.retrieveUserRoles();
     * }
     */
    public UserRoleCollection retrieveUserRoles() {
        log.debug("target: {}", getTarget().getUri().toString());
        UserRoleCollection objList = getTargetPath("users/roles").request(MediaType.APPLICATION_JSON).get(UserRoleCollection.class);
        return objList;
    }
    
}
