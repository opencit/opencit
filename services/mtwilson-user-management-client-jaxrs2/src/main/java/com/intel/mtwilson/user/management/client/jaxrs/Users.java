/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.user.management.client.jaxrs;

import com.intel.mtwilson.jaxrs2.client.MtWilsonClient;
import com.intel.mtwilson.user.management.rest.v2.model.UserCollection;
import com.intel.mtwilson.user.management.rest.v2.model.UserFilterCriteria;
import com.intel.mtwilson.user.management.rest.v2.model.User;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Users extends MtWilsonClient {
    
    Logger log = LoggerFactory.getLogger(getClass().getName());

    public Users(URL url) throws Exception{
        super(url);
    }

    public Users(Properties properties) throws Exception {
        super(properties);
    }
    
     /**
     * Creates an User request with the specified parameters. The user can specify the purpose of the 
     * request in the comments section so that the administrator can provide access to appropriate roles during approval.
     * @param user - User object that needs to be created. Only the user name is required for creation of the user request. 
     * @return Created User object.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions users:create
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType POST
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/users
     * Input: {"username":"Developer1","locale":"en-US","comment":"Access needed for Project1"}
     * Output: {"id":"e6c9337c-e709-4b38-9f04-3b61b8a84667","username":"Developer1","locale":"en-us","comment":"Access needed for Project1"}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  Users client = new Users(My.configuration().getClientProperties());
     *  User user = new User();
     *  user.setUsername("Developer1");
     *  user.setLocale(Locale.US);
     *  user.setComment("Access needed for Project1");
     *  User createUser = client.createUser(user);
     * </pre>
     */
    public User createUser(User user) {
        log.debug("target: {}", getTarget().getUri().toString());
        User newUser = getTarget().path("users").request().accept(MediaType.APPLICATION_JSON).post(Entity.json(user), User.class);
        return newUser;
    }
    
    /**
     * Deletes the User with the specified UUID from the system. When the user is deleted, all the associated
     * certificate,hmac and password login information would also be deleted from the system.
     * @param uuid - UUID of the User that has to be deleted.
     * @return N/A
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions users:delete
     * @mtwContentTypeReturned N/A
     * @mtwMethodType DELETE
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/users/e6c9337c-e709-4b38-9f04-3b61b8a84667
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  Users client = new Users(My.configuration().getClientProperties());
     *  client.deleteUser("e6c9337c-e709-4b38-9f04-3b61b8a84667");
     * </pre>
     */
    public void deleteUser(String uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", uuid);
        Response user = getTarget().path("users/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).delete();
        if( !user.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
            throw new WebApplicationException("Delete OS failed");
        }
    }

    /**
     * Deletes the User(s) matching the specified search criteria. 
     * @param criteria UserFilterCriteria object specifying the search criteria. The search options include
     * id, nameEqualTo and nameContains.
     * @return N/A
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions roles:delete,search
     * @mtwContentTypeReturned N/A
     * @mtwMethodType DELETE
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/users?nameContains=admin
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  Users client = new Users(My.configuration().getClientProperties());
     *  UserFilterCriteria criteria = new UserFilterCriteria();
     *  criteria.nameContains = "admin";
     *  client.deleteUser(criteria);
     * </pre>
     */
    public void deleteUser(UserFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        Response obj = getTargetPathWithQueryParams("users", criteria).request(MediaType.APPLICATION_JSON).delete();
        if( !obj.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
            throw new WebApplicationException("Delete user failed");
        }
    }
    
    /**
     * Updates the details of the User in the system. The locale and the comments fields can be updated.
     * @param user - User object details that needs to be updated.
     * @return Updated user object.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions users:store
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType PUT
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/users/e6c9337c-e709-4b38-9f04-3b61b8a84667
     * Input: {"locale":"fr","comment":"Access granted for Project1"}
     * Output: {"id":"e6c9337c-e709-4b38-9f04-3b61b8a84667","locale":"fr","comment":"Access granted for Project1"}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  Users client = new Users(My.configuration().getClientProperties());
     *  User user = new User();
     *  user.setId(UUID.valueOf("e6c9337c-e709-4b38-9f04-3b61b8a84667"));
     *  user.setLocale(Locale.FRENCH);
     *  user.comment("Access granted for Project1");
     *  user = client.editUser(user);
     * </pre>
     */
    public User editUser(User user) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", user.getId().toString());
        User newUser = getTarget().path("users/{id}").resolveTemplates(map).request().accept(MediaType.APPLICATION_JSON).put(Entity.json(user), User.class);
        return newUser;
    }
    
     /**
     * Retrieves the User object with the specified UUID
     * @param uuid - UUID of the User to be retrieved
     * @return <code> User </code> matching the specified UUID.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions users:retrieve
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/users/e6c9337c-e709-4b38-9f04-3b61b8a84667
     * Output: {"id":"e6c9337c-e709-4b38-9f04-3b61b8a84667","username":"Developer1","locale":"en-us","comment":"Access needed for Project1"}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  Users client = new Users(My.configuration().getClientProperties());
     *  User retrieveUser = client.retrieveUser("e6c9337c-e709-4b38-9f04-3b61b8a84667");
     * </pre>
     */
    public User retrieveUser(String uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", uuid);
        User user = getTarget().path("users/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(User.class);
        return user;
    }
    
    /**
     * Searches for the User's with the specified set of criteria.
     * @param UserFilterCriteria object specifying the filter criteria. The search options include
     * id and userNameEqualTo. Also, if the caller wants to retrieve the list of all the registered
     * users, the filter option can be disabled by setting the filter criteria to false. By default
     * the filter criteria is true. [Ex: /v2/users?filter=false]
     * @return <code> UserCollection </code> with the Users that meet the specified filter criteria
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions users:search
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/users?filter=false
     * Output: {"users":[{"id":"3f2442cd-33d5-4d2b-8897-9c79a5dee0c4","username":"tagservice","locale":"en-us"},
     * {"id":"e6c9337c-e709-4b38-9f04-3b61b8a84667","username":"Developer1","locale":"fr","comment":"Access granted for Project1"}]}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  Users client = new Users(My.configuration().getClientProperties());
     *  UserFilterCriteria criteria = new UserFilterCriteria();
     *  criteria.filter = false;
     *  UserCollection users = client.searchUsers(criteria);
     * </pre>
     */
    public UserCollection searchUsers(UserFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        //UserCollection users = getTarget().path("users").queryParam("nameContains", name).request(MediaType.APPLICATION_JSON).get(UserCollection.class);
        UserCollection users = getTargetPathWithQueryParams("users", criteria).request(MediaType.APPLICATION_JSON).get(UserCollection.class);
        return users;
    }
}
