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
import javax.ws.rs.WebApplicationException;
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
     * https://server.com:8181/mtwilson/v2/roles
     * Input: {"role_name":"MTW_Admin"}
     * Output: {"id":"17dbfd48-12a4-4328-85af-43b0d5adfee3","role_name":"MTW_Admin"}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  Roles client = new Roles(My.configuration().getClientProperties());
     *  Role role = new Role();
     *  role.setRoleName("MTW_Admin");
     *  Role createRole = client.createRole(role);
     * </pre>
     */
    public Role createRole(Role role) {
        log.debug("target: {}", getTarget().getUri().toString());
        Role newRole = getTarget().path("roles").request().accept(MediaType.APPLICATION_JSON).post(Entity.json(role), Role.class);
        return newRole;
    }
    
    /**
     * Deletes the Role with the specified UUID from the system. All the associated permissions would also
     * be deleted.
     * @param uuid - UUID of the Role that has to be deleted.
     * @return N/A
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions roles:delete
     * @mtwContentTypeReturned N/A
     * @mtwMethodType DELETE
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/roles/17dbfd48-12a4-4328-85af-43b0d5adfee3
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  Roles client = new Roles(My.configuration().getClientProperties());
     *  client.deleteRole("17dbfd48-12a4-4328-85af-43b0d5adfee3");
     * </pre>
     */
    public void deleteRole(String uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", uuid);
        Response obj = getTarget().path("roles/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).delete();
        if( !obj.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
            throw new WebApplicationException("Delete role failed");
        }
    }

    /**
     * Deletes the Role(s) matching the specified search criteria. 
     * @param criteria RoleFilterCriteria object specifying the search criteria. The search options include
     * id, nameEqualTo and nameContains.
     * @return N/A
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions roles:delete,search
     * @mtwContentTypeReturned N/A
     * @mtwMethodType DELETE
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/roles?nameContains=admin
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  Roles client = new Roles(My.configuration().getClientProperties());
     *  RoleFilterCriteria criteria = new RoleFilterCriteria();
     *  criteria.nameContains = "admin";
     *  client.deleteRole(criteria);
     * </pre>
     */
    public void deleteRole(RoleFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        Response obj = getTargetPathWithQueryParams("roles", criteria).request(MediaType.APPLICATION_JSON).delete();
        if( !obj.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
            throw new WebApplicationException("Delete role failed");
        }
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
     * https://server.com:8181/mtwilson/v2/roles/17dbfd48-12a4-4328-85af-43b0d5adfee3
     * Input: {"description":"MTW Admin role"}
     * Output: {"id":"17dbfd48-12a4-4328-85af-43b0d5adfee3","description":"MTW Admin role"}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  Roles client = new Roles(My.configuration().getClientProperties());
     *  Role role = new Role();
     *  role.setId(UUID.valueOf("17dbfd48-12a4-4328-85af-43b0d5adfee3"));
     *  role.setDescription("MTW Admin role");
     *  role = client.editRole(role);
     * </pre>
     */
    public Role editRole(Role role) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", role.getId().toString());
        Role newRole = getTarget().path("roles/{id}").resolveTemplates(map).request().accept(MediaType.APPLICATION_JSON).put(Entity.json(role), Role.class);
        return newRole;
    }
    
     /**
     * Retrieves the Role object with the specified UUID
     * @param uuid - UUID of the Role to be retrieved
     * @return Role object matching the specified UUID.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions roles:retrieve
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/roles/17dbfd48-12a4-4328-85af-43b0d5adfee3
     * Output: {"id":"17dbfd48-12a4-4328-85af-43b0d5adfee3","role_name":"MTW_Admin","description":"MTW Admin role"}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  Roles client = new Roles(My.configuration().getClientProperties());
     *  Role retrieveRole = client.retrieveRole("17dbfd48-12a4-4328-85af-43b0d5adfee3");
     * </pre>
     */
    public Role retrieveRole(String uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", uuid);
        Role role = getTarget().path("roles/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(Role.class);
        return role;
    }
    
    /**
     * Searches for the Role's with the specified set of criteria.
     * @param RoleFilterCriteria object specifying the filter criteria. The search options include
     * id, nameEqualTo and nameContains. Also, if the caller wants to retrieve the list of all the registered
     * roles, the filter option can be disabled by setting the filter criteria to false. By default
     * the filter criteria is true. [Ex: /v2/roles?filter=false retrieves the list of all the roles]
     * @return RoleCollection with the Roles that meet the specified filter criteria
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions roles:search
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/roles?filter=false
     * Output: {"roles":[{"id":"0199a936-9a49-482a-8c63-cfe7a9412d7e","role_name":"server_manager"},
     * {"id":"177b1d3c-b0aa-4543-8509-92fde907a4a9","role_name":"admin","description":"user created role"}]}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  Roles client = new Roles(My.configuration().getClientProperties());
     *  RoleFilterCriteria criteria = new RoleFilterCriteria();
     *  criteria.filter = false;
     *  RoleCollection roles = client.searchRoles(criteria);
     * </pre>
     */
    public RoleCollection searchRoles(RoleFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        RoleCollection roles = getTargetPathWithQueryParams("roles", criteria).request(MediaType.APPLICATION_JSON).get(RoleCollection.class);
        return roles;
    }
}
