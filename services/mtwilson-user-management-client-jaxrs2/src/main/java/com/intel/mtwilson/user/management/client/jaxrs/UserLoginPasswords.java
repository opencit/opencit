/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.user.management.client.jaxrs;

import com.intel.mtwilson.client.jaxrs.common.MtWilsonClient;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginPasswordCollection;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginPasswordFilterCriteria;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginPassword;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserLoginPasswords extends MtWilsonClient {
    
    Logger log = LoggerFactory.getLogger(getClass().getName());

    public UserLoginPasswords(URL url) throws Exception{
        super(url);
    }

    public UserLoginPasswords(Properties properties) throws Exception {
        super(properties);
    }
    
     /**
     * Stores the details of the user's password in the system for user to login using password mechanism. Using
     * the algorithm, salt and the iterations specified the password hash would be calculated and stored in the
     * system.
     * @param userLoginPassword - UserLoginPassword object that needs to be created. 
     * @return Created UserLoginPassword object.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions user_login_passwords:create
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType POST
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/users/{user_id}/login-passwords
     * Input: {"userLoginPassword_name":"developer"}
     * Output: {"id":"31741556-f5c7-4eb6-a713-338a23e43b93","name":"Intel","description":"Intel OEM"}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  UserLoginPasswords client = new UserLoginPasswords(My.configuration().getClientProperties());
     *  UserLoginPassword userLoginPassword = new UserLoginPassword();
     *  userLoginPassword.setName("Intel");
     *  userLoginPassword.setDescription("Intel OEM");
     *  UserLoginPassword createUserLoginPassword = client.createUserLoginPassword(userLoginPassword);
     * </pre>
     */
    public UserLoginPassword createUserLoginPassword(UserLoginPassword obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("user_id", obj.getUserId().toString());
        UserLoginPassword newUserLoginPassword = getTarget().path("/users/{user_id}/login-passwords").resolveTemplates(map)
                .request().accept(MediaType.APPLICATION_JSON).post(Entity.json(obj), UserLoginPassword.class);
        return newUserLoginPassword;
    }
    
    /**
     * Deletes the UserLoginPassword with the specified UUID from the system. 
     * @param uuid - UUID of the UserLoginPassword that has to be deleted.
     * @return N/A
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions user_login_passwords:delete
     * @mtwContentTypeReturned N/A
     * @mtwMethodType DELETE
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/users/{user_id}/login-passwords
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  UserLoginPasswords client = new UserLoginPasswords(My.configuration().getClientProperties());
     *  client.deleteUserLoginPassword("31741556-f5c7-4eb6-a713-338a23e43b93");
     * </pre>
     */
    public void deleteUserLoginPassword(String userUuid, String uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("user_id", userUuid);
        map.put("id", uuid);
        Response userLoginPassword = getTarget().path("/users/{user_id}/login-passwords/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).delete();
    }

    /**
     * Updates the details of the UserLoginPassword in the system. Only the comments can be updated.
     * @param userLoginPassword - UserLoginPassword object details that needs to be updated.
     * @return Updated userLoginPassword object.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions user_login_passwords:store
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType PUT
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/users/{user_id}/login-passwords
     * Input: {"comments":"Need access for development."}
     * Output: {"id": "31741556-f5c7-4eb6-a713-338a23e43b93","description": "Intel OEM updated" }
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  UserLoginPasswords client = new UserLoginPasswords(My.configuration().getClientProperties());
     *  UserLoginPassword userLoginPassword = new UserLoginPassword();
     *  userLoginPassword.setId(UUID.valueOf("31741556-f5c7-4eb6-a713-338a23e43b93"));
     *  userLoginPassword.setDescription("Intel OEM updated");
     *  userLoginPassword = client.editUserLoginPassword(userLoginPassword);
     * </pre>
     */
    public UserLoginPassword editUserLoginPassword(UserLoginPassword obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("user_id", obj.getUserId());
        map.put("id", obj.getId().toString());
        UserLoginPassword newUserLoginPassword = getTarget().path("/users/{user_id}/login-passwords/{id}").resolveTemplates(map)
                .request().accept(MediaType.APPLICATION_JSON).put(Entity.json(obj), UserLoginPassword.class);
        return newUserLoginPassword;
    }
    
     /**
     * Retrieves the UserLoginPassword object with the specified UUID
     * @param uuid - UUID of the UserLoginPassword to be retrieved
     * @return <code> UserLoginPassword </code> matching the specified UUID.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions user_login_passwords:retrieve
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/users/{user_id}/login-passwords
     * Output: {"id":"31741556-f5c7-4eb6-a713-338a23e43b93","name":"Intel","description":"Intel OEM"}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  UserLoginPasswords client = new UserLoginPasswords(My.configuration().getClientProperties());
     *  UserLoginPassword retrieveUserLoginPassword = client.retrieveUserLoginPassword("31741556-f5c7-4eb6-a713-338a23e43b93");
     * </pre>
     */
    public UserLoginPassword retrieveUserLoginPassword(String userUuid, String uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("user_id", userUuid);
        map.put("id", uuid);
        UserLoginPassword userLoginPassword = getTarget().path("/users/{user_id}/login-passwords/{id}").resolveTemplates(map)
                .request(MediaType.APPLICATION_JSON).get(UserLoginPassword.class);
        return userLoginPassword;
    }
    
        
    /**
     * Searches for the User's login password entries with the specified set of criteria.
     * @param UserLoginPasswordFilterCriteria object specifying the filter criteria. The search options include
     * id, status and enabled. If the user just provides the user_id without any filter criteria, then the user login password entry
     * for that user_id would be retrieved.
     * @return <code> UserLoginPasswordCollection </code> with the UserLoginPasswords that meet the specified filter criteria
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions user_login_passwords:search
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/users/{user_id}/login-passwords
     * Output: {"userLoginPasswords":[{"id":"f310b4e3-1f9c-4687-be60-90260262afd9","name":"Intel Corporation","description":"Intel Corporation"}]}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  UserLoginPasswords client = new UserLoginPasswords(My.configuration().getClientProperties());
     *  UserLoginPasswordFilterCriteria criteria = new UserLoginPasswordFilterCriteria();
     *  criteria.nameContains = "intel";
     *  UserLoginPasswordCollection userLoginPasswords = client.searchUserLoginPasswords(criteria);
     * </pre>
     */
    public UserLoginPasswordCollection searchUserLoginPasswords(UserLoginPasswordFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("user_id", criteria.id);
        UserLoginPasswordCollection userLoginPasswords = getTargetPathWithQueryParams("/users/{user_id}/login-passwords", criteria)
                .request(MediaType.APPLICATION_JSON).get(UserLoginPasswordCollection.class);
        return userLoginPasswords;
    }
}
