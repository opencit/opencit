/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.client.jaxrs;

import com.intel.mtwilson.as.rest.v2.model.User;
import com.intel.mtwilson.as.rest.v2.model.UserCollection;
import com.intel.mtwilson.as.rest.v2.model.UserFilterCriteria;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <code> Users </code> is the class that lets creation, updation and deletion of Users 
 * in the Mt.Wilson system.
 * @author ssbangal
 */
public class Users extends MtWilsonClient {
    
    Logger log = LoggerFactory.getLogger(getClass().getName());

    public Users(URL url)throws Exception {
        super(url);
    }

    /**
     * Initializes the <class>Users</class> with the Mt.Wilson configuration properties.      * 
     * @param properties Properties associated with the Mt.Wilson configuration. 
     * Use the getClientProperties() of <class>MyConfiguration</class> for initializing.
     * @throws Exception 
     * 
     * <i><u>Sample Java API call :</u></i><br>
     *{@code
     *  Properties prop = My.configuration().getClientProperties();
     *  Users client = new Users(prop);
     * }
     */
    public Users(Properties properties) throws Exception {
        super(properties);
    }
    
     /**
     * Gets the list of users satisfying a mentioned set of search criteria
     * @param criteria - FilterCriteria to be used for searching the users.
     *  The possible search options include nameEqualTo, nameContains and enabled.
     * @return <code> Usercollection </code>, a list of users that satisfy the search criteria
     * * <p>
     * <i><u>Roles Needed:</u></i> TOCHECK?
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML<br>
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: GET</i><br>
     * https://10.1.71.234:8443/mtwilson/v2/users?nameContains=superadmin
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * {"users":[{
     *  id: "1b3ea885-7696-442a-8ae5-89404edce224"
     *  name: "superadmin"
     *  enabled: false
     *  status: "PENDING"
     *  locale: "en"
     * }]}
     * 
     * <i><u>Sample Java API call :</u></i><br>
     * {@code
     *  Properties prop = My.configuration().getClientProperties();
     *  Users client = new Users(prop);
     *  UserFilterCriteria criteria = new UserFilterCriteria();
     *  criteria.nameContains = "d";
     *  UserCollection objCollection = client.searchUsers(criteria);
     * }
     */
    public UserCollection searchUsers(UserFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        UserCollection objCollection = getTargetPathWithQueryParams("users", criteria).request(MediaType.APPLICATION_JSON).get(UserCollection.class);
        return objCollection;
    }
    
     /**
     * Retrieves the <code>User</code> object associated with the uuid.
     * @param uuid UUID of the User to be retrieved/fetched from the system.
     * @return <code> User </code> associated with the uuid
      * <p>
     * <i><u>Roles Needed:</u></i> TO CHECK?
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: GET</i><br>
     * https://10.1.71.234:8181/mtwilson/v2/users/1b3ea885-7696-442a-8ae5-89404edce224
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * {
     *  id: "1b3ea885-7696-442a-8ae5-89404edce224"
     *  name: "superadmin"
     *  enabled: false
     *  status: "PENDING"
     *  locale: "en"
     * }
     * 
     * <i><u>Sample Java API call :</u></i><br>
     * {@code
     *  Properties prop = My.configuration().getClientProperties();
     *  Users client = new Users(prop);
     *  User retrieveUser = client.retrieveUser("56bb5400-64ea-47c7-99c4-958c6f721717");
     * }
     */
    public User retrieveUser(String uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("id", uuid);
        User obj = getTarget().path("users/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(User.class);
        return obj;
    }

      /**
     * Creates <code> User </code> with the provided attributes
     * @param obj <code> User </code> object that has to be created
     * @return  <code> User </code> that has been created in the Mt.Wilson system.
     *  * <p>
     * <i><u>Roles Needed:</u></i> TO CHECK
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: POST</i><br>
     * https://10.1.71.234:8181/mtwilson/v2/users
     * <p>
     * <i>Sample Input</i><br>
     *	{"name":"superadmin","locale":"en"}
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * {
     *  id: "1b3ea885-7696-442a-8ae5-89404edce224"
     *  name: "superadmin"
     *  locale: "en"
     * } 
     * 
     * <i><u>Sample Java API call :</u></i><br>
     * {@code
     *  Properties prop = My.configuration().getClientProperties();
     *  Users client = new Users(prop);
     *  User obj = new User();
     *  obj.setName("ApiUser1");
     *  obj.setLocale("en");
     *  obj.setComments("Api client testing");
     *  User createUser = client.createUser(obj);
     * }
     */
    public User createUser(User obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        User newObj = getTarget().path("users").request().accept(MediaType.APPLICATION_JSON).post(Entity.json(obj), User.class);
        return newObj;
    }

     /**
     * Edits the User with the provided credentials.
     * @param obj - User to be edited. Id of the User should be non-null.
     * @return the updated <code>User</code> object from the system.
     * <p>
     * <i><u>Roles Needed:</u></i> TO CHECK
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: PUT</i><br>
     * https://10.1.71.234:8181/mtwilson/v2/users/1b3ea885-7696-442a-8ae5-89404edce224
     * <p>
     * <i>Sample Input</i><br>
     *	    {"enabled":"true","status":"APPROVED"}
     * <p>
     * <i><u>Sample Output:</u></i><br>
     *  {
     *      id: "1b3ea885-7696-442a-8ae5-89404edce224"
     *      enabled: true
     *      status: "APPROVED"
     *  }
     *   
     * <i><u>Sample Java API call :</u></i><br>
     * {@code
     *  Properties prop = My.configuration().getClientProperties();
     *  Users client = new Users(prop);
     *  User obj = new User();
     *  obj.setId(UUID.valueOf("56bb5400-64ea-47c7-99c4-958c6f721717"));
     *  obj.setEnabled(Boolean.TRUE);
     *  obj.setStatus("APPROVED");
     *  obj = client.editUser(obj);
     * }
     */
    public User editUser(User obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("id", obj.getId().toString());
        User newObj = getTarget().path("users/{id}").resolveTemplates(map).request().accept(MediaType.APPLICATION_JSON).put(Entity.json(obj), User.class);
        return newObj;
    }

     /**
     * Deletes the specified user identified by the UUID.
     * @param uuid UUID of the user to be deleted. UUID Should not be null.
     * <p>
     * <i><u>Roles Needed:</u></i> TO CHECK
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: DELETE</i><br>
     * https://10.1.71.234:8181/mtwilson/v2/users/1b3ea885-7696-442a-8ae5-89404edce224
     * <p>
     * <i><u>Sample Output: NA </u></i><br>
     *  
     * <i><u>Sample Java API call :</u></i><br>
     * {@code
     *  Properties prop = My.configuration().getClientProperties();
     *  Users client = new Users(prop);
     *  client.deleteUser("56bb5400-64ea-47c7-99c4-958c6f721717");
     * }
     */
    public void deleteUser(String uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("id", uuid);
        Response obj = getTarget().path("users/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).delete();
        log.debug(obj.toString());
    }
    
}
