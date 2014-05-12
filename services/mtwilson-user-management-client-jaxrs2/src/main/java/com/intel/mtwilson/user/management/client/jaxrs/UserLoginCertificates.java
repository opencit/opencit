/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.user.management.client.jaxrs;

import com.intel.mtwilson.client.jaxrs.common.MtWilsonClient;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginCertificateCollection;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginCertificateFilterCriteria;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginCertificate;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserLoginCertificates extends MtWilsonClient {
    
    Logger log = LoggerFactory.getLogger(getClass().getName());

    public UserLoginCertificates(URL url) throws Exception{
        super(url);
    }

    public UserLoginCertificates(Properties properties) throws Exception {
        super(properties);
    }
    
     /**
     * Stores the details of the user's password in the system for user to login using password mechanism. Using
     * the algorithm, salt and the iterations specified the password hash would be calculated and stored in the
     * system.
     * @param userLoginPassword - UserLoginCertificate object that needs to be created. 
     * @return Created UserLoginCertificate object.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions user_login_certificates:create
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
     *  UserLoginCertificates client = new UserLoginCertificates(My.configuration().getClientProperties());
     *  UserLoginCertificate userLoginPassword = new UserLoginCertificate();
     *  userLoginPassword.setName("Intel");
     *  userLoginPassword.setDescription("Intel OEM");
     *  UserLoginCertificate createUserLoginCertificate = client.createUserLoginCertificate(userLoginPassword);
     * </pre>
     */
    public UserLoginCertificate createUserLoginCertificate(UserLoginCertificate obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("user_id", obj.getUserId().toString());
        UserLoginCertificate newUserLoginCertificate = getTarget().path("/users/{user_id}/login-certificates").resolveTemplates(map)
                .request().accept(MediaType.APPLICATION_JSON).post(Entity.json(obj), UserLoginCertificate.class);
        return newUserLoginCertificate;
    }
    
    /**
     * Deletes the UserLoginCertificate with the specified UUID from the system. 
     * @param uuid - UUID of the UserLoginCertificate that has to be deleted.
     * @return N/A
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions user_login_certificates:delete
     * @mtwContentTypeReturned N/A
     * @mtwMethodType DELETE
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/users/{user_id}/login-passwords
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  UserLoginCertificates client = new UserLoginCertificates(My.configuration().getClientProperties());
     *  client.deleteUserLoginCertificate("31741556-f5c7-4eb6-a713-338a23e43b93");
     * </pre>
     */
    public void deleteUserLoginCertificate(String userUuid, String uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("user_id", userUuid);
        map.put("id", uuid);
        Response userLoginPassword = getTarget().path("/users/{user_id}/login-certificates/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).delete();
    }

    /**
     * Updates the details of the UserLoginCertificate in the system. Only the comments can be updated.
     * @param userLoginPassword - UserLoginCertificate object details that needs to be updated.
     * @return Updated userLoginPassword object.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions user_login_certificates:store
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType PUT
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/users/{user_id}/login-certificates
     * Input: {"comments":"Need access for development."}
     * Output: {"id": "31741556-f5c7-4eb6-a713-338a23e43b93","description": "Intel OEM updated" }
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  UserLoginCertificates client = new UserLoginCertificates(My.configuration().getClientProperties());
     *  UserLoginCertificate userLoginPassword = new UserLoginCertificate();
     *  userLoginPassword.setId(UUID.valueOf("31741556-f5c7-4eb6-a713-338a23e43b93"));
     *  userLoginPassword.setDescription("Intel OEM updated");
     *  userLoginPassword = client.editUserLoginCertificate(userLoginPassword);
     * </pre>
     */
    public UserLoginCertificate editUserLoginCertificate(UserLoginCertificate obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("user_id", obj.getUserId());
        map.put("id", obj.getId().toString());
        UserLoginCertificate newUserLoginCertificate = getTarget().path("/users/{user_id}/login-certificates/{id}").resolveTemplates(map)
                .request().accept(MediaType.APPLICATION_JSON).put(Entity.json(obj), UserLoginCertificate.class);
        return newUserLoginCertificate;
    }
    
     /**
     * Retrieves the UserLoginCertificate object with the specified UUID
     * @param uuid - UUID of the UserLoginCertificate to be retrieved
     * @return <code> UserLoginCertificate </code> matching the specified UUID.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions user_login_certificates:retrieve
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/users/{user_id}/login-certificates
     * Output: {"id":"31741556-f5c7-4eb6-a713-338a23e43b93","name":"Intel","description":"Intel OEM"}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  UserLoginCertificates client = new UserLoginCertificates(My.configuration().getClientProperties());
     *  UserLoginCertificate retrieveUserLoginCertificate = client.retrieveUserLoginCertificate("31741556-f5c7-4eb6-a713-338a23e43b93");
     * </pre>
     */
    public UserLoginCertificate retrieveUserLoginCertificate(String userUuid, String uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("user_id", userUuid);
        map.put("id", uuid);
        UserLoginCertificate userLoginPassword = getTarget().path("/users/{user_id}/login-certificates/{id}").resolveTemplates(map)
                .request(MediaType.APPLICATION_JSON).get(UserLoginCertificate.class);
        return userLoginPassword;
    }
    
        
    /**
     * Searches for the User's login certificates entries with the specified set of criteria.
     * @param UserLoginCertificateFilterCriteria object specifying the filter criteria. The search options include
     * id, sha1, sha256, status and enabled. If the user just provides the user_id without any filter criteria, then the user login certificate
     * for that user_id would be retrieved.
     * @return <code> UserLoginCertificateCollection </code> with the UserLoginCertificates that meet the specified filter criteria
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions user_login_certificates:search
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/users/{user_id}/login-passwords
     * Output: {"userLoginPasswords":[{"id":"f310b4e3-1f9c-4687-be60-90260262afd9","name":"Intel Corporation","description":"Intel Corporation"}]}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  UserLoginCertificates client = new UserLoginCertificates(My.configuration().getClientProperties());
     *  UserLoginCertificateFilterCriteria criteria = new UserLoginCertificateFilterCriteria();
     *  criteria.nameContains = "intel";
     *  UserLoginCertificateCollection userLoginPasswords = client.searchUserLoginCertificates(criteria);
     * </pre>
     */
    public UserLoginCertificateCollection searchUserLoginCertificates(UserLoginCertificateFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("user_id", criteria.id);
        UserLoginCertificateCollection userLoginPasswords = getTargetPathWithQueryParams("/users/{user_id}/login-certificates", criteria)
                .request(MediaType.APPLICATION_JSON).get(UserLoginCertificateCollection.class);
        return userLoginPasswords;
    }
}
