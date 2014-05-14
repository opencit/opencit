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
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <code>Users</code> is the class that allows creation , deletion  and updates to OEM objects.
 * 
 * @author ssbangal
 */
public class Users extends MtWilsonClient {
    
    Logger log = LoggerFactory.getLogger(getClass().getName());

    public Users(URL url) throws Exception{
        super(url);
    }

    /**
     * @example. This is testing
     */
    public Users(Properties properties) throws Exception {
        super(properties);
    }
    
     /**
     * Creates an User object with the specified parameters. This would be used to associate with BIOS MLE[Measured Launch Environment] during
     * the creation of BIOS MLE. Only Name is the required parameter. Description field is optional.
     * @param user - User object that needs to be created having the name and description. If the caller specifies the ID,
     * it has to be a valid UUID. If not provided, it would be automatically generated.
     * @return <code> User </code> that is created.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions users:create
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType POST
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/users
     * Input: {"name":"Intel","description":"Intel OEM"}
     * Output: {"id":"31741556-f5c7-4eb6-a713-338a23e43b93","name":"Intel","description":"Intel OEM"}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  Properties prop = My.configuration().getClientProperties();
     *  Users client = new Users(prop);
     *  User user = new User();
     *  user.setName("Intel");
     *  user.setDescription("Intel OEM");
     *  User createUser = client.createUser(user);
     * </pre>
     */
    public User createUser(User user) {
        log.debug("target: {}", getTarget().getUri().toString());
        User newUser = getTarget().path("users").request().accept(MediaType.APPLICATION_JSON).post(Entity.json(user), User.class);
        return newUser;
    }
    
    /**
     * Deletes the User with the specified UUID from the system. An OEM can be deleted only if it is not associated with any BIOS MLE.
     * @param uuid - UUID of the User that has to be deleted.
     * @return N/A
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions users:delete
     * @mtwContentTypeReturned N/A
     * @mtwMethodType DELETE
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/users/31741556-f5c7-4eb6-a713-338a23e43b93
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  Properties prop = My.configuration().getClientProperties(); 
     *  Users client = new Users(prop);
     *  client.deleteUser("31741556-f5c7-4eb6-a713-338a23e43b93");
     * </pre>
     */
    public void deleteUser(String uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("id", uuid);
        Response user = getTarget().path("users/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).delete();
    }

    /**
     * Updates the details of the User in the system. Only description of the User can be updated.
     * @param user - User that needs to be updated.
     * @return <code> User </code> that is updated.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions users:store
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType PUT
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/users/31741556-f5c7-4eb6-a713-338a23e43b93
     * Input: {"description":"Intel OEM updated"}
     * Output: {"id": "31741556-f5c7-4eb6-a713-338a23e43b93","description": "Intel OEM updated" }
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  Properties prop = My.configuration().getClientProperties();
     *  Users client = new Users(prop);  
     *  User user = new User();
     *  user.setId(UUID.valueOf("31741556-f5c7-4eb6-a713-338a23e43b93"));
     *  user.setDescription("Intel OEM updated");
     *  user = client.editUser(user);
     * </pre>
     */
    public User editUser(User user) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
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
     * https://server.com:8443/mtwilson/v2/users/31741556-f5c7-4eb6-a713-338a23e43b93
     * Output: {"id":"31741556-f5c7-4eb6-a713-338a23e43b93","name":"Intel","description":"Intel OEM"}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  Properties prop = My.configuration().getClientProperties();
     *  Users client = new Users(prop);
     *  User retrieveUser = client.retrieveUser("31741556-f5c7-4eb6-a713-338a23e43b93");
     * </pre>
     */
    public User retrieveUser(String uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("id", uuid);
        User user = getTarget().path("users/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(User.class);
        return user;
    }
    
    /**
     * Searches for the User's with the specified set of criteria
     * @param criteria - <code> UserFilterCriteria </code> expressing the filter criteria
     *      The possible search options include nameEqualTo and nameContains.
     * @return <code> UserCollection </code> with the Users that meet the specified filter criteria
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions users:search
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/users?nameContains=Intel
     * Output: {"users":[{"id":"f310b4e3-1f9c-4687-be60-90260262afd9","name":"Intel Corporation","description":"Intel Corporation"}]}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  Properties prop = My.configuration().getClientProperties();
     *  Users client = new Users(prop);
     *  UserFilterCriteria criteria = new UserFilterCriteria();
     *  criteria.nameContains = "intel";
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
