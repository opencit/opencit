/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.user.management.rest.v2.repository;

import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.user.management.rest.v2.model.Role;
import com.intel.mtwilson.user.management.rest.v2.model.RoleCollection;
import com.intel.mtwilson.user.management.rest.v2.model.RoleFilterCriteria;
import com.intel.mtwilson.user.management.rest.v2.model.RoleLocator;
import com.intel.mtwilson.i18n.ErrorCode;
import com.intel.mtwilson.jaxrs2.server.resource.DocumentRepository;
import com.intel.mtwilson.user.management.rest.v2.model.RolePermissionFilterCriteria;
import com.intel.mtwilson.shiro.jdbi.LoginDAO;
import com.intel.mtwilson.shiro.jdbi.MyJdbi;
import java.util.List;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.apache.shiro.authz.annotation.RequiresPermissions;


/**
 *
 * @author ssbangal
 */
public class RoleRepository implements DocumentRepository<Role, RoleCollection, RoleFilterCriteria, RoleLocator> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RoleRepository.class);
    
    @Override
    @RequiresPermissions("roles:search")        
    public RoleCollection search(RoleFilterCriteria criteria) {
        log.debug("Role:Search - Got request to search for the roles.");        
        RoleCollection objCollection = new RoleCollection();
        try (LoginDAO loginDAO = MyJdbi.authz()) {
            if (!criteria.filter) {
                // If the filter criteria is set to false, then all the results need to be returned back without filtering
                List<Role> roles = loginDAO.findAllRoles();
                if (roles != null && roles.size() > 0) {
                    for (Role role : roles) {
                        objCollection.getRoles().add(role);
                    }
                }
            } else if (criteria.id != null) {
                Role obj = loginDAO.findRoleById(criteria.id);
                if (obj != null) {
                    objCollection.getRoles().add(obj);
                }
            } else if (criteria.nameEqualTo != null && !criteria.nameEqualTo.isEmpty()) {
                Role obj = loginDAO.findRoleByName(criteria.nameEqualTo);
                if (obj != null) {
                    objCollection.getRoles().add(obj);
                }
            } else if (criteria.nameContains != null && !criteria.nameContains.isEmpty()) {
                List<Role> roles = loginDAO.findRoleByNameLike("%"+criteria.nameContains+"%");
                if (roles != null && roles.size() > 0) {
                    for (Role role : roles) {
                        objCollection.getRoles().add(role);
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Error during user role search.", ex);
            throw new ASException(ErrorCode.MS_API_USER_SEARCH_ERROR, ex.getClass().getSimpleName());
        }
        log.debug("Role:Search - Returning back {} of results.", objCollection.getRoles().size());                
        return objCollection;
    }

    @Override
    @RequiresPermissions("roles:retrieve")        
    public Role retrieve(RoleLocator locator) {
        if( locator == null || locator.id == null ) { return null; }
        log.debug("Role:Retrieve - Got request to retrieve role with id {}.", locator.id);                
         try (LoginDAO loginDAO = MyJdbi.authz()) {
            Role obj = loginDAO.findRoleById(locator.id);
            if (obj != null) {
                return obj;
            }
        } catch (Exception ex) {
            log.error("Error during role search.", ex);
            throw new ASException(ErrorCode.MS_API_USER_SEARCH_ERROR, ex.getClass().getSimpleName());
        }
        return null;
    }

    @Override
    @RequiresPermissions("roles:store")        
    public void store(Role item) {
        log.debug("Role:Store - Got request to update role with id {}.", item.getId().toString());        
         try (LoginDAO loginDAO = MyJdbi.authz()) {
            Role obj = loginDAO.findRoleById(item.getId());
            if (obj != null) {
                if (item.getDescription()!= null && !item.getDescription().isEmpty())
                    obj.setDescription(item.getDescription());
                
                loginDAO.updateRole(obj.getId(), obj.getRoleName(), obj.getDescription());
                log.debug("Role:Store - Updated the role with id {} successfully.", obj.getId());
                
            } else {
                log.error("Role:Store - Role will not be updated since it does not exist.");
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }
            
        } catch (WebApplicationException wex) {
            throw wex;
        } catch (Exception ex) {
            log.error("Error during user update.", ex);
            throw new ASException(ErrorCode.MS_API_USER_UPDATE_ERROR, ex.getClass().getSimpleName());
        }
        
    }

    @Override
    @RequiresPermissions("roles:create")        
    public void create(Role item) {
        log.debug("Role:Create - Got request to create a new role.");
         try (LoginDAO loginDAO = MyJdbi.authz()) {
            Role obj = loginDAO.findRoleByName(item.getRoleName());
            if (obj == null) {
                obj = new Role();
                obj.setId(item.getId());
                obj.setRoleName(item.getRoleName());
                obj.setDescription(item.getDescription());
                loginDAO.insertRole(obj.getId(), obj.getRoleName(), obj.getDescription());
                log.debug("Role:Create - Created the role with name {} successfully.", item.getRoleName());
            } else {
                log.error("Role:Create - Role with name {} will not be created since a duplicate already exists.", item.getRoleName());
                throw new WebApplicationException(Response.Status.CONFLICT);
            }  
        } catch (WebApplicationException wex) {
            throw wex;
        } catch (Exception ex) {
            log.error("Error during role creation.", ex);
            throw new ASException(ErrorCode.MS_API_USER_REGISTRATION_ERROR, ex.getClass().getSimpleName());
        }
    }

    @Override
    @RequiresPermissions("roles:delete")        
    public void delete(RoleLocator locator) {
        if( locator == null || locator.id == null ) { return; }
        log.debug("Role:Delete - Got request to delete role with id {}.", locator.id.toString());        
        try (LoginDAO loginDAO = MyJdbi.authz()) {
            Role obj = loginDAO.findRoleById(locator.id);
            if (obj != null ) {
                // Before the role object is deleted, we need to see if there are any reference to this role in Certificate, Password and Hmac role repositories
                if ((loginDAO.findUserLoginCertificateRolesByRoleId(obj.getId()).size() > 0) ||
                        //(loginDAO.findUserLoginHmacRolesByRoleId(obj.getId()).size() > 0) ||
                        (loginDAO.findUserLoginPasswordRolesByRoleId(obj.getId()).size() > 0)) {
                    log.error("Role with id {} cannot be deleted since it is associated to users.");
                    throw new WebApplicationException("Role cannot be deleted as it is being assigned to users.", Response.Status.PRECONDITION_FAILED);
                }
                // Since no user has been assigned this role, it can be deleted. But before deleting the role, the associated permission entries need to be cleared.
                RolePermissionRepository rpRepo = new RolePermissionRepository();
                RolePermissionFilterCriteria rpCriteria = new RolePermissionFilterCriteria();
                rpCriteria.roleId = obj.getId();
                rpCriteria.filter = false; // this filter condition will ensure that all the entries for the role would be retrieved.
                rpRepo.delete(rpCriteria);
                
                loginDAO.deleteRole(obj.getId());
                log.debug("Role:Delete - Deleted the role with id {} successfully.", locator.id);
            } else {
                log.info("Role:Delete - Role does not exist in the system.");
            }
        } catch (WebApplicationException wex) {
            throw wex;
        } catch (Exception ex) {
            log.error("Error during role deletion.", ex);
            throw new ASException(ErrorCode.MS_API_USER_DELETION_ERROR, ex.getClass().getSimpleName());
        }
    }
    
    @Override
    @RequiresPermissions("roles:delete,search")        
    public void delete(RoleFilterCriteria criteria) {
        log.debug("Role:Delete - Got request to delete role permission by search criteria.");        
        RoleCollection objCollection = search(criteria);
        try { 
            for (Role obj : objCollection.getRoles()) {
                RoleLocator locator = new RoleLocator();
                locator.id = obj.getId();
                delete(locator);
            }
        } catch (Exception ex) {
            log.error("Error during role deletion.", ex);
            throw new ASException(ErrorCode.MS_API_USER_REGISTRATION_ERROR, ex.getClass().getSimpleName());
        }
    }
    
}
