/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.user.management.client.jaxrs;

import com.intel.mtwilson.jaxrs2.client.MtWilsonClient;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginPasswordCollection;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginPasswordFilterCriteria;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginPassword;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;
import javax.ws.rs.WebApplicationException;
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
     * Stores the details of the user's password in the system for user to login using password mechanism. The caller
     * is expected to provide the hashed value along with the salt, algorithm and the iterations that were used
     * to calculate the hash.
     * @param UserLoginPassword object that needs to be created. 
     * @return Created UserLoginPassword object.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions user_login_passwords:create
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType POST
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/users/9116f3ed-5496-42b3-a9ee-4e89b1d533bc/login-passwords
     * Input: {"password_hash":"RZMrrSt/PvKvdqs1OgR0id0bDE0dvF4XbPKV7sF+oDg=","salt":"a9gDma0hUF8=","iterations":1,"algorithm":"SHA256",
     * "comment":"Access needed for development"}
     * Output: {"id":"610cc4fc-0148-4788-bc9c-633d61fbeb4e","user_id":"9116f3ed-5496-42b3-a9ee-4e89b1d533bc",
     * "password_hash":"RZMrrSt/PvKvdqs1OgR0id0bDE0dvF4XbPKV7sF+oDg=","salt":"a9gDma0hUF8=","iterations":1,
     * "algorithm":"SHA256","enabled":false,"comment":"Access needed for development"}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  UserLoginPasswords client = new UserLoginPasswords(My.configuration().getClientProperties());
     *  UserLoginPassword userLoginPassword = new UserLoginPassword();
     *  loginPasswordInfo.setUserId(UUID.valueOf("9116f3ed-5496-42b3-a9ee-4e89b1d533bc"));
     *  loginPasswordInfo.setAlgorithm("SHA256");
     *  loginPasswordInfo.setIterations(1);
     *  loginPasswordInfo.setSalt("salt".getBytes(Charset.forName("UTF-8")));
     *  loginPasswordInfo.setPasswordHash(hashedpassword); // here it is assumed that the user would pass in the password hashed using the algorithm, salt & iterations.
     *  loginPasswordInfo.setComment("Access needed for development");
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
     * Deletes the User's Login password entry with the specified UUID from the system. All the associated roles would
     * also be deleted.
     * @param uuid - UUID of the UserLoginPassword that has to be deleted.
     * @return N/A
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions user_login_passwords:delete
     * @mtwContentTypeReturned N/A
     * @mtwMethodType DELETE
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/users/9116f3ed-5496-42b3-a9ee-4e89b1d533bc/login-passwords/610cc4fc-0148-4788-bc9c-633d61fbeb4e
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  UserLoginPasswords client = new UserLoginPasswords(My.configuration().getClientProperties());
     *  client.deleteUserLoginPassword("9116f3ed-5496-42b3-a9ee-4e89b1d533bc","610cc4fc-0148-4788-bc9c-633d61fbeb4e");
     * </pre>
     */
    public void deleteUserLoginPassword(String userUuid, String uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("user_id", userUuid);
        map.put("id", uuid);
        Response obj = getTarget().path("/users/{user_id}/login-passwords/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).delete();
        if( !obj.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
            throw new WebApplicationException("Delete user login password failed");
        }
    }

    /**
     * Updates the details of the User's Login Password in the system. It is assumed that the caller
     * would verify the existing password before updating it with the new one using this method. 
     * Similar to the create method, the caller is expected to pass in the already hashed value 
     * of the password along with the details of the mechanism of hashing. Administrator can use
     * this method even just to approve the access with the specified roles.
     * @param UserLoginPassword object details that needs to be updated.
     * @return Updated userLoginPassword object.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions user_login_passwords:store
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType PUT
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/users/9116f3ed-5496-42b3-a9ee-4e89b1d533bc/login-passwords/610cc4fc-0148-4788-bc9c-633d61fbeb4e
     * Input: {"status":"APPROVED","enabled":true,"roles":["security","whitelist"]}
     * Output: {"id":"610cc4fc-0148-4788-bc9c-633d61fbeb4e","user_id":"9116f3ed-5496-42b3-a9ee-4e89b1d533bc",
     * "enabled":true,"status":"APPROVED","roles":["security","whitelist"]}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  UserLoginPasswords client = new UserLoginPasswords(My.configuration().getClientProperties());
     *  UserLoginPassword userLoginPassword = new UserLoginPassword();
     *  loginPasswordInfo.setUserId(UUID.valueOf("9116f3ed-5496-42b3-a9ee-4e89b1d533bc"));
     *  userLoginPassword.setId(UUID.valueOf("610cc4fc-0148-4788-bc9c-633d61fbeb4e"));
     *  loginPasswordInfo.setEnabled(true);
     *  loginPasswordInfo.setStatus(Status.APPROVED);
     *  List<String> roleSet = new ArrayList<>(Arrays.asList("administrator"));
     *  loginPasswordInfo.setRoles(roleSet);
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
     * Retrieves the User's Login Password details with the specified userUuid and id.
     * @param userUuid - UUID of the associated user
     * @param uuid - UUID of the UserLoginPassword
     * @return UserLoginPassword matching the specified UUID.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions user_login_passwords:retrieve
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/users/9116f3ed-5496-42b3-a9ee-4e89b1d533bc/login-passwords/610cc4fc-0148-4788-bc9c-633d61fbeb4e
     * Output: {"id":"610cc4fc-0148-4788-bc9c-633d61fbeb4e","user_id":"9116f3ed-5496-42b3-a9ee-4e89b1d533bc",
     * "password_hash":"i4bjqvom3KwEwAMpMpcAZRW8R8IUbi3apS0J9zCBl6c=",
     * "salt":"a9gDma0hUF8=","iterations":1,"algorithm":"SHA256","enabled":true,"status":"APPROVED","roles":["Security","Whitelist"]}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  UserLoginPasswords client = new UserLoginPasswords(My.configuration().getClientProperties());
     *  UserLoginPassword retrieveUserLoginPassword = client.retrieveUserLoginPassword("9116f3ed-5496-42b3-a9ee-4e89b1d533bc","610cc4fc-0148-4788-bc9c-633d61fbeb4e");
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
     * userUuid, id, status and enabled. If the user provides filter criteria and sets it to false, then the user login password entry
     * for that user_id would be retrieved.
     * @return UserLoginPasswordCollection with the UserLoginPasswords that meet the specified filter criteria
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions user_login_passwords:search
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/users/981d5993-d380-4623-9f8b-1c6131ee8234/login-passwords?filter=false
     * Output: {"user_login_passwords":[{"id":"db108831-96d7-4a3c-afd6-5521e2defcbf","user_id":"981d5993-d380-4623-9f8b-1c6131ee8234",
     * "password_hash":"RZMrrSt/PvKvdqs1OgR0id0bDE0dvF4XbPKV7sF+oDg=","salt":"a9gDma0hUF8=","iterations":1,"algorithm":"SHA256",
     * "enabled":true,"status":"APPROVED","comment":"Automatically created during setup.","roles":["admin","administrator"]}]}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  UserLoginPasswords client = new UserLoginPasswords(My.configuration().getClientProperties());
     *  UserLoginPasswordFilterCriteria criteria = new UserLoginPasswordFilterCriteria();
     *  criteria.userUuid = UUID.valueOf("981d5993-d380-4623-9f8b-1c6131ee8234");
     *  UserLoginPasswordCollection userLoginPasswords = client.searchUserLoginPasswords(criteria);
     * </pre>
     */
    public UserLoginPasswordCollection searchUserLoginPasswords(UserLoginPasswordFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("user_id", criteria.id);
        UserLoginPasswordCollection userLoginPasswords = getTargetPathWithQueryParams("/users/{user_id}/login-passwords", criteria)
                .resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(UserLoginPasswordCollection.class);
        return userLoginPasswords;
    }
}
