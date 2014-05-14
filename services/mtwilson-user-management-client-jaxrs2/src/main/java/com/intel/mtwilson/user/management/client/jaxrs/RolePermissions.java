/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.user.management.client.jaxrs;

import com.intel.mtwilson.client.jaxrs.common.MtWilsonClient;
import com.intel.mtwilson.user.management.rest.v2.model.RolePermissionCollection;
import com.intel.mtwilson.user.management.rest.v2.model.RolePermissionFilterCriteria;
import com.intel.mtwilson.user.management.rest.v2.model.RolePermission;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;
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
     * Creates an new RolePermission mapping in the system. Permissions have 3 parts : Domain, Action and Selection.
     * Domains are basically resources on which the permissions would apply (Ex: Oems, Mles, etc). Action is basically
     * create, store, retrieve, search and delete. There can be sometimes special actions based on the resources like
     * import & export in case of certificates. Selection : TODO
     * @param rolePermission - RolePermission object that needs to be created. 
     * @return Created RolePermission object.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions role_permissions:create
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType POST
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/roles/{role_id}/permissions
     * Input: {"rolePermission_name":"developer"}
     * Output: {"id":"31741556-f5c7-4eb6-a713-338a23e43b93","name":"Intel","description":"Intel OEM"}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  RolePermissions client = new RolePermissions(My.configuration().getClientProperties());
     *  RolePermission rolePermission = new RolePermission();
     *  rolePermission.setName("Intel");
     *  rolePermission.setDescription("Intel OEM");
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
     * Deletes the RolePermission(s) with the specified search criteria. 
     * @param RolePermissionFilterCriteria object specifying the search criteria.
     * @return N/A
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions role_permissions:delete
     * @mtwContentTypeReturned N/A
     * @mtwMethodType DELETE
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/roles/{role_id}/permissions
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  RolePermissions client = new RolePermissions(My.configuration().getClientProperties());
     *  client.deleteRolePermission("31741556-f5c7-4eb6-a713-338a23e43b93");
     * </pre>
     */
    public void deleteRolePermission(RolePermissionFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("role_id", criteria.roleId);
        Response rolePermission = getTarget().path("roles/{role_id}/permissions").resolveTemplates(map).request(MediaType.APPLICATION_JSON).delete();
    }
        
    /**
     * Searches for the RolePermission's with the specified set of criteria.
     * @param RolePermissionFilterCriteria object specifying the filter criteria. The search options include
     * id and rolePermissionNameEqualTo. Also, if the caller wants to retrieve the list of all the registered
     * rolePermissions, the filter option can be disabled by setting the filter criteria to false. By default
     * the filter criteria is true. [Ex: /v2/rolePermissions?filter=false retrieves the list of all the rolePermissions]
     * @return <code> RolePermissionCollection </code> with the RolePermissions that meet the specified filter criteria
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions role_permissions:search
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/roles/{role_id}/permissions
     * Output: {"rolePermissions":[{"id":"f310b4e3-1f9c-4687-be60-90260262afd9","name":"Intel Corporation","description":"Intel Corporation"}]}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  RolePermissions client = new RolePermissions(My.configuration().getClientProperties());
     *  RolePermissionFilterCriteria criteria = new RolePermissionFilterCriteria();
     *  criteria.nameContains = "intel";
     *  RolePermissionCollection rolePermissions = client.searchRolePermissions(criteria);
     * </pre>
     */
    public RolePermissionCollection searchRolePermissions(RolePermissionFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("role_id", criteria.roleId);
        RolePermissionCollection rolePermissions = getTargetPathWithQueryParams("roles/{role_id}/permissions", criteria).request(MediaType.APPLICATION_JSON).get(RolePermissionCollection.class);
        return rolePermissions;
    }
}
