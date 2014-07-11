/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.user.management.rest.v2.repository;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginPassword;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginPasswordCollection;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginPasswordFilterCriteria;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginPasswordLocator;
import com.intel.mtwilson.jaxrs2.server.resource.DocumentRepository;
import com.intel.mtwilson.repository.RepositoryCreateConflictException;
import com.intel.mtwilson.repository.RepositoryCreateException;
import com.intel.mtwilson.repository.RepositoryDeleteException;
import com.intel.mtwilson.repository.RepositoryException;
import com.intel.mtwilson.repository.RepositoryRetrieveException;
import com.intel.mtwilson.repository.RepositorySearchException;
import com.intel.mtwilson.repository.RepositoryStoreConflictException;
import com.intel.mtwilson.repository.RepositoryStoreException;
import com.intel.mtwilson.user.management.rest.v2.model.Role;
import com.intel.mtwilson.user.management.rest.v2.model.RoleLocator;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginPasswordRole;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginPasswordRoleCollection;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginPasswordRoleFilterCriteria;
import com.intel.mtwilson.shiro.jdbi.LoginDAO;
import com.intel.mtwilson.shiro.jdbi.MyJdbi;
import com.intel.mtwilson.user.management.rest.v2.model.Status;
import java.util.ArrayList;
import java.util.List;
import org.apache.shiro.authz.annotation.RequiresPermissions;


/**
 *
 * @author ssbangal
 */
public class UserLoginPasswordRepository implements DocumentRepository<UserLoginPassword, UserLoginPasswordCollection, UserLoginPasswordFilterCriteria, UserLoginPasswordLocator> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UserLoginPasswordRepository.class);
    
    @Override
    @RequiresPermissions("user_login_passwords:search")        
    public UserLoginPasswordCollection search(UserLoginPasswordFilterCriteria criteria) {
        log.debug("UserLoginPassword:Search - Got request to search for the user login passwords.");        
        UserLoginPasswordCollection objCollection = new UserLoginPasswordCollection();
        try (LoginDAO loginDAO = MyJdbi.authz()) {
            if (criteria.userUuid != null) {
                UserLoginPassword obj = loginDAO.findUserLoginPasswordByUserId(criteria.userUuid);
                if (obj != null) {
                    if (criteria.filter == false) {
                        obj.setRoles(getAssociateRolesForLoginPasswordId(obj.getId()));
                        objCollection.getUserLoginPasswords().add(obj);                                                
                    } else if (criteria.id != null) {
                        if (obj.getId().equals(criteria.id)) {
                            obj.setRoles(getAssociateRolesForLoginPasswordId(obj.getId()));
                            objCollection.getUserLoginPasswords().add(obj);
                        }
                    } else if (criteria.enabled != null && criteria.status != null) {
                        if (obj.isEnabled() == criteria.enabled && obj.getStatus().equals(criteria.status)) {
                            obj.setRoles(getAssociateRolesForLoginPasswordId(obj.getId()));
                            objCollection.getUserLoginPasswords().add(obj);
                        }
                    } else if (criteria.enabled != null) {
                        if (obj.isEnabled() == criteria.enabled) {
                            obj.setRoles(getAssociateRolesForLoginPasswordId(obj.getId()));
                            objCollection.getUserLoginPasswords().add(obj);
                        }
                    } else if (criteria.status != null) {
                        if (obj.getStatus().equals(criteria.status)) {
                            obj.setRoles(getAssociateRolesForLoginPasswordId(obj.getId()));
                            objCollection.getUserLoginPasswords().add(obj);
                        }
                    } 
                }
            }
        } catch (Exception ex) {
            log.error("Error during user login password search.", ex);
            throw new RepositorySearchException(ex, criteria);
        }
        log.debug("UserLoginPassword:Search - Returning back {} of results.", objCollection.getUserLoginPasswords().size());                
        return objCollection;
    }

    @Override
    @RequiresPermissions("user_login_passwords:retrieve")        
    public UserLoginPassword retrieve(UserLoginPasswordLocator locator) {
        if( locator == null || locator.id == null ) { return null; }
        log.debug("UserLoginPassword:Retrieve - Got request to retrieve user login password with id {}.", locator.id);                
         try (LoginDAO loginDAO = MyJdbi.authz()) {
            UserLoginPassword obj = loginDAO.findUserLoginPasswordById(locator.id);
            if (obj != null) {
                obj.setRoles(getAssociateRolesForLoginPasswordId(obj.getId()));
                return obj;
            }
        } catch (Exception ex) {
            log.error("Error during user login password retrieve.", ex);
            throw new RepositoryRetrieveException(ex, locator);
        }
        return null;
    }

    @Override
    @RequiresPermissions("user_login_passwords:store")        
    public void store(UserLoginPassword item) {
        log.debug("UserLoginPassword:Store - Got request to update user login password with id {}.", item.getId().toString());     
        UserLoginPasswordLocator locator = new UserLoginPasswordLocator();
        locator.id = item.getId();        
         try (LoginDAO loginDAO = MyJdbi.authz()) {
            UserLoginPassword obj = loginDAO.findUserLoginPasswordById(item.getId());
            if (obj != null) {
                if (item.getPasswordHash()!= null)
                    obj.setPasswordHash(item.getPasswordHash());
                if (item.getAlgorithm() != null)
                    obj.setAlgorithm(item.getAlgorithm());
                if (item.getExpires() != null)
                    obj.setExpires(item.getExpires());
                if (item.getIterations() > 0)
                    obj.setIterations(item.getIterations());
                if (item.getSalt() != null)
                    obj.setSalt(item.getSalt());
                obj.setEnabled(item.isEnabled());
                obj.setStatus(item.getStatus());
                obj.setComment(item.getComment());
                loginDAO.updateUserLoginPassword(obj.getPasswordHash(), obj.getSalt(), obj.getIterations(), obj.getAlgorithm(), obj.getExpires(), 
                        obj.isEnabled(), obj.getId(), obj.getStatus(), obj.getComment());
                log.debug("UserLoginPassword:Store - Updated the user login password with id {} successfully.", obj.getId());

                // We need not check for the length here since the admin might want to delete all the roles
                if (item.getRoles() != null){
                    // Before we add the roles we need to delete the existing ones
                    UserLoginPasswordRoleRepository repo = new UserLoginPasswordRoleRepository();

                    UserLoginPasswordRoleFilterCriteria criteria = new UserLoginPasswordRoleFilterCriteria();
                    criteria.loginPasswordIdEqualTo = item.getId();
                    repo.delete(criteria);

                    // Now we need to add the roles requested by the user
                    List<String> roles = item.getRoles();
                    if (roles != null && roles.size() > 0) {
                        for (String role : roles) {
                            // Let us verify if the role exists, if it does, then we will map the role to the user login password entry
                            Role roleInSystem = loginDAO.findRoleByName(role);
                            if (roleInSystem != null) {
                                UserLoginPasswordRole userLoginPasswordRole = new UserLoginPasswordRole();
                                userLoginPasswordRole.setLoginPasswordId(item.getId());
                                userLoginPasswordRole.setRoleId(roleInSystem.getId());
                                repo.create(userLoginPasswordRole);
                            }
                        }
                    }
                }                
            } else {
                log.error("UserLoginPassword:Store - User login password will not be updated since it does not exist.");
                throw new RepositoryStoreConflictException(locator);
            }
            
        } catch (RepositoryException re) {
            throw re;
        } catch (Exception ex) {
            log.error("Error during user login password update.", ex);
            throw new RepositoryStoreException(ex, locator);
        }
        
    }

    @Override
    @RequiresPermissions("user_login_passwords:create")        
    public void create(UserLoginPassword item) {
        log.debug("UserLoginPassword:Create - Got request to create a new user login password.");
        UserLoginPasswordLocator locator = new UserLoginPasswordLocator();
        locator.id = item.getId();
        locator.userId = item.getUserId();
         try (LoginDAO loginDAO = MyJdbi.authz()) {
            UserLoginPassword obj = loginDAO.findUserLoginPasswordByUserId(item.getUserId());
            if (obj == null) {
                obj = new UserLoginPassword();
                obj.setId(item.getId());
                obj.setUserId(item.getUserId());
                obj.setPasswordHash(item.getPasswordHash());
                obj.setAlgorithm(item.getAlgorithm());
                obj.setExpires(item.getExpires());
                obj.setIterations(item.getIterations());
                obj.setSalt(item.getSalt());
                obj.setEnabled(false);
                obj.setStatus(Status.PENDING);
                obj.setComment(item.getComment());
                loginDAO.insertUserLoginPassword(obj.getId(), obj.getUserId(), obj.getPasswordHash(), obj.getSalt(), obj.getIterations(), obj.getAlgorithm(),
                        obj.getExpires(), obj.isEnabled(), obj.getStatus(), obj.getComment());
                log.debug("UserLoginPassword:Create - Created the user login password for user with id {} successfully.", obj.getUserId());
            } else {
                log.error("UserLoginPassword:Create - User login password for user with Id {} will not be created since a duplicate already exists.", obj.getUserId());
                throw new RepositoryCreateConflictException(locator);
            }  
        } catch (RepositoryException re) {
            throw re;
        } catch (Exception ex) {
            log.error("Error during user login password creation.", ex);
            throw new RepositoryCreateException(ex, locator);
        }
    }

    @Override
    @RequiresPermissions("user_login_passwords:delete")        
    public void delete(UserLoginPasswordLocator locator) {
        if( locator == null || locator.id == null ) { return; }
        log.debug("UserLoginPassword:Delete - Got request to delete user login password with id {}.", locator.id.toString());        
        try (LoginDAO loginDAO = MyJdbi.authz()) {
            UserLoginPassword obj = loginDAO.findUserLoginPasswordById(locator.id);
            if (obj != null ) {
                
                // First delete all the role mappings from the UserLoginPasswordRole table
                UserLoginPasswordRoleRepository repo = new UserLoginPasswordRoleRepository();
                UserLoginPasswordRoleFilterCriteria criteria = new UserLoginPasswordRoleFilterCriteria();
                criteria.loginPasswordIdEqualTo = locator.id;
                repo.delete(criteria);
                
                loginDAO.deleteUserLoginPasswordById(locator.id);
                log.debug("UserLoginPassword:Delete - Deleted the user login password with id {} successfully.", locator.id);
            } else {
                log.info("UserLoginPassword:Delete - User login password does not exist in the system.");
            }
        } catch (Exception ex) {
            log.error("Error during user login password deletion.", ex);
            throw new RepositoryDeleteException(ex, locator);
        }
    }
    
    @Override
    @RequiresPermissions("user_login_passwords:delete,search")        
    public void delete(UserLoginPasswordFilterCriteria criteria) {
        log.debug("UserLoginPassword:Delete - Got request to delete login passwords by search criteria.");        
        try {
            UserLoginPasswordCollection objList = search(criteria);
            for (UserLoginPassword obj : objList.getUserLoginPasswords()) {
                UserLoginPasswordLocator locator = new UserLoginPasswordLocator();
                locator.id = obj.getId();
                delete(locator);
            }
        } catch(RepositoryException re) {
            throw re;
        } catch (Exception ex) {
            log.error("Error during User login password deletion.", ex);
            throw new RepositoryDeleteException(ex);
        }
    }
    
    /**
     * Helper function to retrieve the roles associated with user login password. This would be called by
     * retrieve and search methods.
     * @param id
     * @return 
     */
    private List<String> getAssociateRolesForLoginPasswordId(UUID id) {
        List<String> associatedRoles = new ArrayList<>();

        UserLoginPasswordRoleRepository repo = new UserLoginPasswordRoleRepository();
        RoleRepository roleRepo = new RoleRepository();

        UserLoginPasswordRoleFilterCriteria criteria = new UserLoginPasswordRoleFilterCriteria();
        criteria.loginPasswordIdEqualTo = id;
        UserLoginPasswordRoleCollection roles = repo.search(criteria);
        if (roles != null && roles.getUserLoginPasswordRoles().size() > 0) {
            for (UserLoginPasswordRole role : roles.getUserLoginPasswordRoles()) {
                RoleLocator roleLocator = new RoleLocator();
                roleLocator.id = role.getRoleId();
                Role retrieve = roleRepo.retrieve(roleLocator);
                if (retrieve == null) {
                    throw new IllegalStateException(String.format("Unable to retrieve role with ID: %s", role.getId()));
                }
                associatedRoles.add(retrieve.getRoleName());
            }            
        }                 
        return associatedRoles;
    }
}
