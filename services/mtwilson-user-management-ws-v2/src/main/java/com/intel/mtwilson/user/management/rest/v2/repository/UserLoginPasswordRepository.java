/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.user.management.rest.v2.repository;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginPassword;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginPasswordCollection;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginPasswordFilterCriteria;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginPasswordLocator;
import com.intel.mtwilson.datatypes.ErrorCode;
import com.intel.mtwilson.jersey.resource.SimpleRepository;
import com.intel.mtwilson.user.management.rest.v2.model.Role;
import com.intel.mtwilson.user.management.rest.v2.model.RoleLocator;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginPasswordRole;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginPasswordRoleCollection;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginPasswordRoleFilterCriteria;
import com.intel.mtwilson.shiro.authc.password.PasswordCredentialsMatcher;
import com.intel.mtwilson.shiro.jdbi.LoginDAO;
import com.intel.mtwilson.shiro.jdbi.MyJdbi;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.apache.shiro.authz.annotation.RequiresPermissions;


/**
 *
 * @author ssbangal
 */
public class UserLoginPasswordRepository implements SimpleRepository<UserLoginPassword, UserLoginPasswordCollection, UserLoginPasswordFilterCriteria, UserLoginPasswordLocator> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UserLoginPasswordRepository.class);
    
    @Override
    @RequiresPermissions("user_login_passwords:search")        
    public UserLoginPasswordCollection search(UserLoginPasswordFilterCriteria criteria) {
        log.debug("UserLoginPassword:Search - Got request to search for the users login passwords.");        
        UserLoginPasswordCollection objCollection = new UserLoginPasswordCollection();
        try (LoginDAO loginDAO = MyJdbi.authz()) {
            if (criteria.id != null) {
                UserLoginPassword obj = loginDAO.findUserLoginPasswordById(criteria.id);
                if (obj != null) {
                    obj.setRoles(getAssociateRolesForLoginPasswordId(obj.getId()));
                    objCollection.getUserLoginPasswords().add(obj);
                }
            } else if (criteria.userIdEqualTo != null) {
                UserLoginPassword obj = loginDAO.findUserLoginPasswordByUserId(criteria.userIdEqualTo);
                if (obj != null) {
                    obj.setRoles(getAssociateRolesForLoginPasswordId(obj.getId()));
                    objCollection.getUserLoginPasswords().add(obj);
                }
            } else if (criteria.userNameEqualTo != null && !criteria.userNameEqualTo.isEmpty()) {
                UserLoginPassword obj = loginDAO.findUserLoginPasswordByUsername(criteria.userNameEqualTo);
                if (obj != null) {
                    obj.setRoles(getAssociateRolesForLoginPasswordId(obj.getId()));
                    objCollection.getUserLoginPasswords().add(obj);
                }
            }
        } catch (Exception ex) {
            log.error("Error during user login password search.", ex);
            throw new ASException(ErrorCode.MS_API_USER_SEARCH_ERROR, ex.getClass().getSimpleName());
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
            throw new ASException(ErrorCode.MS_API_USER_SEARCH_ERROR, ex.getClass().getSimpleName());
        }
        return null;
    }

    @Override
    @RequiresPermissions("user_login_passwords:store")        
    public void store(UserLoginPassword item) {
        log.debug("UserLoginPassword:Store - Got request to update user login password with id {}.", item.getId().toString());        
         try (LoginDAO loginDAO = MyJdbi.authz()) {
            UserLoginPassword obj = loginDAO.findUserLoginPasswordById(item.getId());
            if (obj != null) {
                if (item.getPasswordHash()!= null)
                    obj.setPasswordHash(item.getPasswordHash());
                if (item.getAlgorithm() != null)
                    obj.setAlgorithm(item.getAlgorithm());
                if (item.getExpires() != null)
                    obj.setExpires(item.getExpires());
                if (item.getIterations() != 0)
                    obj.setIterations(item.getIterations());
                if (item.getSalt() != null)
                    obj.setSalt(item.getSalt());
                obj.setEnabled(item.isEnabled());
                                
                loginDAO.updateUserLoginPassword(obj.getPasswordHash(), obj.getSalt(), obj.getIterations(), obj.getAlgorithm(), obj.getExpires(), obj.isEnabled(), obj.getId());
                log.debug("UserLoginPassword:Store - Updated the user login password with id {} successfully.", obj.getId());

                // Before we add the roles we need to delete the existing ones
                UserLoginPasswordRoleRepository repo = new UserLoginPasswordRoleRepository();
                
                UserLoginPasswordRoleFilterCriteria criteria = new UserLoginPasswordRoleFilterCriteria();
                criteria.loginPasswordIdEqualTo = item.getId();
                repo.delete(criteria);
                
                // Now we need to add the roles requested by the user
                Set<String> roles = item.getRoles();
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
                
            } else {
                log.error("UserLoginPassword:Store - User login password will not be updated since it does not exist.");
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
    @RequiresPermissions("user_login_passwords:create")        
    public void create(UserLoginPassword item) {
        log.debug("UserLoginPassword:Create - Got request to create a new user keystore.");
         try (LoginDAO loginDAO = MyJdbi.authz()) {
            UserLoginPassword obj = loginDAO.findUserLoginPasswordByUserId(item.getUserId());
            if (obj == null) {
                obj = new UserLoginPassword();
                obj.setId(item.getId());
                obj.setUserId(item.getUserId());
                obj.setPasswordHash(PasswordCredentialsMatcher.passwordHash(item.getPasswordHash(), item));
                obj.setAlgorithm(item.getAlgorithm());
                obj.setExpires(item.getExpires());
                obj.setIterations(item.getIterations());
                obj.setSalt(item.getSalt());
                obj.setEnabled(item.isEnabled());
                loginDAO.insertUserLoginPassword(obj.getId(), obj.getUserId(), obj.getPasswordHash(), obj.getSalt(), obj.getIterations(), obj.getAlgorithm(),
                        obj.getExpires(), obj.isEnabled());
                log.debug("UserLoginPassword:Create - Created the user login password for user with id {} successfully.", obj.getUserId());
            } else {
                log.error("UserLoginPassword:Create - User login password for user with Id {} will not be created since a duplicate already exists.", obj.getUserId());
                throw new WebApplicationException(Response.Status.CONFLICT);
            }  
        } catch (WebApplicationException wex) {
            throw wex;
        } catch (Exception ex) {
            log.error("Error during user creation.", ex);
            throw new ASException(ErrorCode.MS_API_USER_REGISTRATION_ERROR, ex.getClass().getSimpleName());
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
            log.error("Error during user deletion.", ex);
            throw new ASException(ErrorCode.MS_API_USER_DELETION_ERROR, ex.getClass().getSimpleName());
        }
    }
    
    @Override
    @RequiresPermissions("user_login_passwords:delete,search")        
    public void delete(UserLoginPasswordFilterCriteria criteria) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    /**
     * Helper function to retrieve the roles associated with user login password. This would be called by
     * retrieve and search methods.
     * @param id
     * @return 
     */
    private Set<String> getAssociateRolesForLoginPasswordId(UUID id) {
        Set<String> associatedRoles = new HashSet<>();

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
                associatedRoles.add(retrieve.getRoleName());
            }            
        }                 
        return associatedRoles;
    }
}
