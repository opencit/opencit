/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.user.management.client.jaxrs;

import com.intel.mtwilson.jaxrs2.client.MtWilsonClient;
import com.intel.mtwilson.user.management.rest.v2.model.RolePermissionCollection;
import com.intel.mtwilson.user.management.rest.v2.model.RolePermissionFilterCriteria;
import com.intel.mtwilson.user.management.rest.v2.model.RolePermission;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RolePermissions extends MtWilsonClient {
    
    Logger log = LoggerFactory.getLogger(getClass().getName());

    public RolePermissions(URL url) throws Exception{
        super(url);
    }

    public RolePermissions(Properties properties) throws Exception {
        super(properties);
    }
    
     /**
     * Creates an new Role Permission mapping in the system. Permissions have 3 parts : Domain, Action and Selection.
     * Domains are basically resources on which the permissions would apply (Ex: Oems, Mles, etc). Action is basically
     * create, store, retrieve, search and delete. There can be sometimes special actions based on the resources like
     * import & export in case of certificates. Multiple actions for a single domain can be separated by comma.
     * Selection : This is currently not being used. By default it would be set to "*". This is for future purpose where
     * user's can specify certain conditions which if evaluates to true would get the required permissions.
     * User can provide "*" as the option for any combination of domain, action and selection. * indicates everything. 
     * Example: An administrator would have * for all the 3 options.
     * @param rolePermission - RolePermission object that needs to be created. 
     * @return Created RolePermission object.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions role_permissions:create
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType POST
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/roles/05f80052-2642-480a-8504-880e27ce8b57/permissions
     * Input: {"permit_domain":"user_mgmt","permit_action":"add,delete","permit_selection":"*"}
     * Output: {"id":"9b35b89c-c5f0-4ffb-8f94-a7f73eef8f76","role_id":"05f80052-2642-480a-8504-880e27ce8b57",
     * "permit_domain":"user_mgmt","permit_action":"add,delete","permit_selection":"*"}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  RolePermissions client = new RolePermissions(My.configuration().getClientProperties());
     *  RolePermission rolePermission = new RolePermission();
     *  rolePermission.setRoleId("05f80052-2642-480a-8504-880e27ce8b57");
     *  rolePermission.setPermitDomain("user_mgmt");
     *  rolePermission.setPermitAction("add,delete");
     *  rolePermission.setPermitSelection("*");
     *  RolePermission createRolePermission = client.createRolePermission(rolePermission);
     * </pre>
     */
    public RolePermission createRolePermission(RolePermission obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("role_id", obj.getRoleId().toString());
        RolePermission newRolePermission = getTarget().path("roles/{role_id}/permissions").resolveTemplates(map)
                .request().accept(MediaType.APPLICATION_JSON).post(Entity.json(obj), RolePermission.class);
        return newRolePermission;
    }
    
    /**
     * Deletes the RolePermission(s) matching the specified search criteria. 
     * @param RolePermissionFilterCriteria object specifying the search criteria.
     * @return N/A
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions role_permissions:delete
     * @mtwContentTypeReturned N/A
     * @mtwMethodType DELETE
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/roles/05f80052-2642-480a-8504-880e27ce8b57/permissions?actionEqualTo=*
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  RolePermissions client = new RolePermissions(My.configuration().getClientProperties());
     *  RolePermissionFilterCriteria criteria = new RolePermissionFilterCriteria();
     *  criteria.roleId = roleId;
     *  criteria.actionEqualTo = "*";
     *  client.deleteRolePermission(criteria);
     * </pre>
     */
    public void deleteRolePermission(RolePermissionFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("role_id", criteria.roleId);
        Response obj = getTargetPathWithQueryParams("roles/{role_id}/permissions", criteria).resolveTemplates(map)
                .request(MediaType.APPLICATION_JSON).delete(); //getTarget().path("roles/{role_id}/permissions").resolveTemplates(map).request(MediaType.APPLICATION_JSON).delete();
        if( !obj.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
            throw new WebApplicationException("Delete role permission failed");
        }
    }
        
    /**
     * Searches for the RolePermission's with the specified set of criteria.
     * @param RolePermissionFilterCriteria object specifying the filter criteria. The search options include
     * id and rolePermissionNameEqualTo. Also, if the caller wants to retrieve the list of all the registered
     * rolePermissions, the filter option can be disabled by setting the filter criteria to false. By default
     * the filter criteria is true. [Ex: /v2/roles/{role_id}/permissions?filter=false retrieves the list of all the 
     * for the specified role]
     * @return RolePermissionCollection with the list of RolePermissions that meet the specified filter criteria
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions role_permissions:search
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/roles/05f80052-2642-480a-8504-880e27ce8b57/permissions?actionEqualTo=*
     * Output: {"role_permissions":[{"role_id":"05f80052-2642-480a-8504-880e27ce8b57","permit_domain":"user_mgmt",
     * "permit_action":"*","permit_selection":"*"}]}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  RolePermissions client = new RolePermissions(My.configuration().getClientProperties());
     *  RolePermissionFilterCriteria criteria = new RolePermissionFilterCriteria();
     *  criteria.roleId = UUID.valueOf("05f80052-2642-480a-8504-880e27ce8b57");
     *  criteria.actionEqualTo = "*";
     *  RolePermissionCollection rolePermissions = client.searchRolePermissions(criteria);
     * </pre>
     */
    public RolePermissionCollection searchRolePermissions(RolePermissionFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("role_id", criteria.roleId);
        RolePermissionCollection rolePermissions = getTargetPathWithQueryParams("roles/{role_id}/permissions", criteria)
                .resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(RolePermissionCollection.class);
        return rolePermissions;
    }
}
