/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.user.management.client.jaxrs;

import com.intel.mtwilson.jaxrs2.client.MtWilsonClient;
import com.intel.mtwilson.user.management.rest.v2.model.RoleCollection;
import com.intel.mtwilson.user.management.rest.v2.model.RoleFilterCriteria;
import com.intel.mtwilson.user.management.rest.v2.model.Role;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Roles extends MtWilsonClient {
    
    Logger log = LoggerFactory.getLogger(getClass().getName());

    public Roles(URL url) throws Exception{
        super(url);
    }

    public Roles(Properties properties) throws Exception {
        super(properties);
    }
    
     /**
     * Creates an new Role in the system that could be assigned to the users. 
     * @param role - Role object that needs to be created. 
     * @return Created Role object.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions roles:create
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType POST
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/roles
     * Input: {"role_name":"developer"}
     * Output: {"id":"31741556-f5c7-4eb6-a713-338a23e43b93","name":"Intel","description":"Intel OEM"}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  Roles client = new Roles(My.configuration().getClientProperties());
     *  Role role = new Role();
     *  role.setName("Intel");
     *  role.setDescription("Intel OEM");
     *  Role createRole = client.createRole(role);
     * </pre>
     */
    public Role createRole(Role role) {
        log.debug("target: {}", getTarget().getUri().toString());
        Role newRole = getTarget().path("roles").request().accept(MediaType.APPLICATION_JSON).post(Entity.json(role), Role.class);
        return newRole;
    }
    
    /**
     * Deletes the Role with the specified UUID from the system. 
     * @param uuid - UUID of the Role that has to be deleted.
     * @return N/A
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions roles:delete
     * @mtwContentTypeReturned N/A
     * @mtwMethodType DELETE
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/roles/31741556-f5c7-4eb6-a713-338a23e43b93
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  Roles client = new Roles(My.configuration().getClientProperties());
     *  client.deleteRole("31741556-f5c7-4eb6-a713-338a23e43b93");
     * </pre>
     */
    public void deleteRole(String uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("id", uuid);
        Response role = getTarget().path("roles/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).delete();
    }

    /**
     * Updates the details of the Role in the system. Only the comments can be updated.
     * @param role - Role object details that needs to be updated.
     * @return Updated role object.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions roles:store
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType PUT
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/roles/31741556-f5c7-4eb6-a713-338a23e43b93
     * Input: {"comments":"Need access for development."}
     * Output: {"id": "31741556-f5c7-4eb6-a713-338a23e43b93","description": "Intel OEM updated" }
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  Roles client = new Roles(My.configuration().getClientProperties());
     *  Role role = new Role();
     *  role.setId(UUID.valueOf("31741556-f5c7-4eb6-a713-338a23e43b93"));
     *  role.setDescription("Intel OEM updated");
     *  role = client.editRole(role);
     * </pre>
     */
    public Role editRole(Role role) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("id", role.getId().toString());
        Role newRole = getTarget().path("roles/{id}").resolveTemplates(map).request().accept(MediaType.APPLICATION_JSON).put(Entity.json(role), Role.class);
        return newRole;
    }
    
     /**
     * Retrieves the Role object with the specified UUID
     * @param uuid - UUID of the Role to be retrieved
     * @return <code> Role </code> matching the specified UUID.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions roles:retrieve
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/roles/31741556-f5c7-4eb6-a713-338a23e43b93
     * Output: {"id":"31741556-f5c7-4eb6-a713-338a23e43b93","name":"Intel","description":"Intel OEM"}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  Roles client = new Roles(My.configuration().getClientProperties());
     *  Role retrieveRole = client.retrieveRole("31741556-f5c7-4eb6-a713-338a23e43b93");
     * </pre>
     */
    public Role retrieveRole(String uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("id", uuid);
        Role role = getTarget().path("roles/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(Role.class);
        return role;
    }
    
    /**
     * Searches for the Role's with the specified set of criteria.
     * @param RoleFilterCriteria object specifying the filter criteria. The search options include
     * id and roleNameEqualTo. Also, if the caller wants to retrieve the list of all the registered
     * roles, the filter option can be disabled by setting the filter criteria to false. By default
     * the filter criteria is true. [Ex: /v2/roles?filter=false retrieves the list of all the roles]
     * @return <code> RoleCollection </code> with the Roles that meet the specified filter criteria
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions roles:search
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/roles?roleNameEqualTo=admin
     * Output: {"roles":[{"id":"f310b4e3-1f9c-4687-be60-90260262afd9","name":"Intel Corporation","description":"Intel Corporation"}]}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  Roles client = new Roles(My.configuration().getClientProperties());
     *  RoleFilterCriteria criteria = new RoleFilterCriteria();
     *  criteria.nameContains = "intel";
     *  RoleCollection roles = client.searchRoles(criteria);
     * </pre>
     */
    public RoleCollection searchRoles(RoleFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        //RoleCollection roles = getTarget().path("roles").queryParam("nameContains", name).request(MediaType.APPLICATION_JSON).get(RoleCollection.class);
        RoleCollection roles = getTargetPathWithQueryParams("roles", criteria).request(MediaType.APPLICATION_JSON).get(RoleCollection.class);
        return roles;
    }
}
