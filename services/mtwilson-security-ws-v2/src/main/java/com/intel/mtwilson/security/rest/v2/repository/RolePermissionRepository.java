/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.security.rest.v2.repository;

import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.security.rest.v2.model.RolePermission;
import com.intel.mtwilson.security.rest.v2.model.RolePermissionCollection;
import com.intel.mtwilson.security.rest.v2.model.RolePermissionFilterCriteria;
import com.intel.mtwilson.security.rest.v2.model.RolePermissionLocator;
import com.intel.mtwilson.datatypes.ErrorCode;
import com.intel.mtwilson.jersey.resource.SimpleRepository;
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
public class RolePermissionRepository implements SimpleRepository<RolePermission, RolePermissionCollection, RolePermissionFilterCriteria, RolePermissionLocator> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RolePermissionRepository.class);
    
    @Override
    @RequiresPermissions("role_permissions:search")        
    public RolePermissionCollection search(RolePermissionFilterCriteria criteria) {
        log.debug("RolePermission:Search - Got request to search for the role permissions.");        
        RolePermissionCollection objCollection = new RolePermissionCollection();
        try (LoginDAO loginDAO = MyJdbi.authz()) {
            if (criteria.roleId != null) {
                if (!criteria.filter) {
                    // If the filter criteria is set to false, then all the results need to be returned back without filtering
                    List<RolePermission> roles = loginDAO.findAllRolePermissionsForRoleId(criteria.roleId);
                    if (roles != null && roles.size() > 0) {
                        for (RolePermission role : roles) {
                            objCollection.getRolePermissions().add(role);
                        }
                    }
                } else if (criteria.domainEqualTo != null && criteria.actionEqualTo == null) {
                    List<RolePermission> roles = loginDAO.findAllRolePermissionsForRoleIdAndDomain(criteria.roleId, criteria.domainEqualTo);
                    if (roles != null && roles.size() > 0) {
                        for (RolePermission role : roles) {
                            objCollection.getRolePermissions().add(role);
                        }
                    }
                } else if (criteria.actionEqualTo != null && criteria.domainEqualTo == null) {
                    List<RolePermission> roles = loginDAO.findAllRolePermissionsForRoleIdAndAction(criteria.roleId, criteria.actionEqualTo);
                    if (roles != null && roles.size() > 0) {
                        for (RolePermission role : roles) {
                            objCollection.getRolePermissions().add(role);
                        }
                    }
                } else if (criteria.actionEqualTo != null && criteria.domainEqualTo != null) {
                    List<RolePermission> roles = loginDAO.findAllRolePermissionsForRoleIdDomainAndAction(criteria.roleId, criteria.domainEqualTo, criteria.actionEqualTo);
                    if (roles != null && roles.size() > 0) {
                        for (RolePermission role : roles) {
                            objCollection.getRolePermissions().add(role);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Error during user role permissions search.", ex);
            throw new ASException(ErrorCode.MS_API_USER_SEARCH_ERROR, ex.getClass().getSimpleName());
        }
        log.debug("RolePermission:Search - Returning back {} of results.", objCollection.getRolePermissions().size());                
        return objCollection;
    }

    @Override
    @RequiresPermissions("role_permissions:retrieve")        
    public RolePermission retrieve(RolePermissionLocator locator) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    @RequiresPermissions("role_permissions:store")        
    public void store(RolePermission item) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.        
    }

    @Override
    @RequiresPermissions("role_permissions:create")        
    public void create(RolePermission item) {
        log.debug("RolePermission:Create - Got request to create a new role.");
         try (LoginDAO loginDAO = MyJdbi.authz()) {
             // Set the default for selection if not specified by the user.
            if (item.getPermitSelection() == null || item.getPermitSelection().isEmpty())
                item.setPermitSelection("*");
            RolePermission obj = loginDAO.findAllRolePermissionsForRoleIdDomainActionAndSelection(item.getRoleId(), item.getPermitDomain(), 
                    item.getPermitAction(), item.getPermitSelection());
            if (obj == null) {
                obj = new RolePermission();
                obj.setRoleId(item.getRoleId());
                obj.setPermitDomain(item.getPermitDomain());
                obj.setPermitAction(item.getPermitAction());
                obj.setPermitSelection(item.getPermitSelection());
                loginDAO.insertRolePermission(item.getRoleId(), item.getPermitDomain(), item.getPermitAction(), item.getPermitSelection());
                log.debug("RolePermission:Create - Created the role permission successfully.");
            } else {
                log.error("RolePermission:Create - RolePermission will not be created since a duplicate already exists.");
                throw new WebApplicationException(Response.Status.CONFLICT);
            }  
        } catch (WebApplicationException wex) {
            throw wex;
        } catch (Exception ex) {
            log.error("Error during role permission creation.", ex);
            throw new ASException(ErrorCode.MS_API_USER_REGISTRATION_ERROR, ex.getClass().getSimpleName());
        }
    }

    @Override
    @RequiresPermissions("role_permissions:delete")        
    public void delete(RolePermissionLocator locator) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    @RequiresPermissions("role_permissions:delete,search")        
    public void delete(RolePermissionFilterCriteria criteria) {
        log.debug("RolePermission:Delete - Got request to delete role permission by search criteria.");        
        RolePermissionCollection objCollection = search(criteria);
        try (LoginDAO loginDAO = MyJdbi.authz()) { 
            for (RolePermission obj : objCollection.getRolePermissions()) {
                loginDAO.deleteRolePermission(obj.getRoleId(), obj.getPermitDomain(), obj.getPermitAction(), obj.getPermitSelection());
            }
        } catch (Exception ex) {
            log.error("Error during user login password role deletion.", ex);
            throw new ASException(ErrorCode.MS_API_USER_REGISTRATION_ERROR, ex.getClass().getSimpleName());
        }
    }
    
}
