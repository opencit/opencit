/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.user.management.client.jaxrs;

import com.intel.mtwilson.jaxrs2.client.MtWilsonClient;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginCertificateCollection;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginCertificateFilterCriteria;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginCertificate;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;
import javax.ws.rs.WebApplicationException;
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
     * Stores the details of the user's certificate in the system for allowing the user to login using certificate mechanism. The 
     * request would be created in the disabled and pending state. Once the access is approved with the roles, users would be able
     * to login.
     * @param UserLoginCertificate object that needs to be created. 
     * @return Created UserLoginCertificate object.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions user_login_certificates:create
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType POST
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/users/cdec55c3-206d-4abb-8ba3-83b819e0b256/login-certificates
     * Input: {"certificate":"MIICrzCCAZegAwIB.....LX+ukqAKQDdqfiSkV+Bw==","comment":"Need to manage user accounts."}
     * Output: {"id":"574874bd-2d5c-4190-b724-d69f2b4c89b4",
     * "certificate":"MIICrzCCAZegAwIBAgIJAJ9cWj....LX+ukqAKQDdqfiSkV+Bw==","enabled":false,"comment":"Need to manage user accounts."}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  UserLoginCertificates client = new UserLoginCertificates(My.configuration().getClientProperties());
     *  UserLoginCertificate userLoginCertificate = new UserLoginCertificate();
     *  userLoginCertificate.setUserId(UUID.valueOf("cdec55c3-206d-4abb-8ba3-83b819e0b256");
     *  userLoginCertificate.setComment("Need to manage user accounts.");
     *  userLoginCertificate.setCertificate(certificate.getEncoded()); // assuming the user has created a x509Certificate
     *  UserLoginCertificate createUserLoginCertificate = client.createUserLoginCertificate(userLoginCertificate);
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
     * Deletes the User's Login Certificate with the specified UUID from the system. All the associated roles would
     * also be deleted
     * @param userUuid - UUID of the User with which the certificate request is associated
     * @param uuid - UUID of the UserLoginCertificate that has to be deleted.
     * @return N/A
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions user_login_certificates:delete
     * @mtwContentTypeReturned N/A
     * @mtwMethodType DELETE
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/users/cdec55c3-206d-4abb-8ba3-83b819e0b256/login-certificates/574874bd-2d5c-4190-b724-d69f2b4c89b4
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  UserLoginCertificates client = new UserLoginCertificates(My.configuration().getClientProperties());
     *  client.deleteUserLoginCertificate("cdec55c3-206d-4abb-8ba3-83b819e0b256","574874bd-2d5c-4190-b724-d69f2b4c89b4");
     * </pre>
     */
    public void deleteUserLoginCertificate(String userUuid, String uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("user_id", userUuid);
        map.put("id", uuid);
        Response obj = getTarget().path("/users/{user_id}/login-certificates/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).delete();
        if( !obj.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
            throw new WebApplicationException("Delete user login certificate failed");
        }
    }

    /**
     * Updates the details of the User's Login Certificate in the system. Only the roles and comments can be updated.
     * Note that during access request, user would just specify the reason for access as part of the comments section.
     * It is up to the administrator approving the access to identify the roles required for the user.
     * @param userLoginPassword - UserLoginCertificate object details that needs to be updated.
     * @return Updated userLoginPassword object.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions user_login_certificates:store
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType PUT
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/users/cdec55c3-206d-4abb-8ba3-83b819e0b256/login-certificates/574874bd-2d5c-4190-b724-d69f2b4c89b4
     * Input: {"enabled":"true","status":"APPROVED","roles":["security","whitelist"]}
     * Output: {"id":"574874bd-2d5c-4190-b724-d69f2b4c89b4","enabled":true,"status":"APPROVED","roles":["security","whitelist"]}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  UserLoginCertificates client = new UserLoginCertificates(My.configuration().getClientProperties());
     *  UserLoginCertificate userLoginCertificate = new UserLoginCertificate();
     *  userLoginCertificate.setId(UUID.valueOf("574874bd-2d5c-4190-b724-d69f2b4c89b4");
     *  userLoginCertificate.setUserId(UUID.valueOf("cdec55c3-206d-4abb-8ba3-83b819e0b256");
     *  userLoginCertificate.setEnabled(true);
     *  userLoginCertificate.setStatus(Status.APPROVED);
     *  List<String> roleSet = new ArrayList<>(Arrays.asList("security", "whitelist"));
     *  userLoginCertificate.setRoles(roleSet);
     *  userLoginCertificate = client.editUserLoginCertificate(userLoginPassword);
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
     * Retrieves the User's Login Certificate details with the specified userUuid and id.
     * @param userUuid - UUID of the associated user
     * @param uuid - UUID of the UserLoginCertificate to be retrieved
     * @return UserLoginCertificate object matching the specified UUID.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions user_login_certificates:retrieve
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/users/cdec55c3-206d-4abb-8ba3-83b819e0b256/login-certificates/574874bd-2d5c-4190-b724-d69f2b4c89b4
     * Output: {"user_login_certificates":[{"id":"574874bd-2d5c-4190-b724-d69f2b4c89b4","user_id":"cdec55c3-206d-4abb-8ba3-83b819e0b256",
     * "certificate":"MIICrzCCAZegAwIB....==","sha1_hash":"5vv7fVyDVD6fGdi/AfAmoieTRfo=","sha256_hash":"b5v2UPacu4zkDnmxXCXrbFBsmHOiUhwES5Olrd+TKC4=",
     * "expires":1432106266000,"enabled":false,"status":"PENDING","comment":"Need to manage user accounts."}]}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  UserLoginCertificates client = new UserLoginCertificates(My.configuration().getClientProperties());
     *  UserLoginCertificate retrieveUserLoginCertificate = client.retrieveUserLoginCertificate("cdec55c3-206d-4abb-8ba3-83b819e0b256",
     * "574874bd-2d5c-4190-b724-d69f2b4c89b4");
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
     * id, sha1, sha256, status and enabled. If the user specifies the filter=false criteria, then the user login certificate
     * for that user_id would be retrieved.
     * @return UserLoginCertificateCollection with the UserLoginCertificates that meet the specified filter criteria
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions user_login_certificates:search
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/users/cdec55c3-206d-4abb-8ba3-83b819e0b256/login-certificates?filter=false
     * Output: {"user_login_certificates":[{"id":"574874bd-2d5c-4190-b724-d69f2b4c89b4","user_id":"cdec55c3-206d-4abb-8ba3-83b819e0b256",
     * "certificate":"MIICrzCCAZegAwIBAgIJAJ9cWj/....LX+ukqAKQDdqfiSkV+Bw==","sha1_hash":"5vv7fVyDVD6fGdi/AfAmoieTRfo=",
     * "sha256_hash":"b5v2UPacu4zkDnmxXCXrbFBsmHOiUhwES5Olrd+TKC4=","expires":1432106266000,"enabled":true,
     * "status":"APPROVED","comment":"Need to manage user accounts.","roles":["Security","Whitelist"]}]}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  UserLoginCertificates client = new UserLoginCertificates(My.configuration().getClientProperties());
     *  UserLoginCertificateFilterCriteria criteria = new UserLoginCertificateFilterCriteria();
     *  criteria.userUuid = UUID.valueOf("cdec55c3-206d-4abb-8ba3-83b819e0b256");
     *  UserLoginCertificateCollection userLoginCertificates = client.searchUserLoginCertificates(criteria);
     * </pre>
     */
    public UserLoginCertificateCollection searchUserLoginCertificates(UserLoginCertificateFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("user_id", criteria.userUuid);
        UserLoginCertificateCollection userLoginPasswords = getTargetPathWithQueryParams("/users/{user_id}/login-certificates", criteria)
                .resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(UserLoginCertificateCollection.class);
        return userLoginPasswords;
    }
}
