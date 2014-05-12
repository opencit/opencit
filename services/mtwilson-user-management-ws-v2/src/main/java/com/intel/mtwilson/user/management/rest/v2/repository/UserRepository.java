/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.user.management.rest.v2.repository;

import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.user.management.rest.v2.model.User;
import com.intel.mtwilson.user.management.rest.v2.model.UserCollection;
import com.intel.mtwilson.user.management.rest.v2.model.UserFilterCriteria;
import com.intel.mtwilson.user.management.rest.v2.model.UserLocator;
import com.intel.mtwilson.datatypes.ErrorCode;
import com.intel.mtwilson.jersey.resource.SimpleRepository;
import com.intel.mtwilson.shiro.jdbi.LoginDAO;
import com.intel.mtwilson.shiro.jdbi.MyJdbi;
import com.intel.mtwilson.user.management.rest.v2.model.Status;
import java.util.List;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.apache.shiro.authz.annotation.RequiresPermissions;


/**
 *
 * @author ssbangal
 */
public class UserRepository implements SimpleRepository<User, UserCollection, UserFilterCriteria, UserLocator> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UserRepository.class);
    
    @Override
    @RequiresPermissions("users:search")        
    public UserCollection search(UserFilterCriteria criteria) {
        log.debug("User:Search - Got request to search for the users.");        
        UserCollection userCollection = new UserCollection();
        try (LoginDAO loginDAO = MyJdbi.authz()) {
            if (!criteria.filter) {
                List<User> findAllUsers = loginDAO.findAllUsers();
                if (findAllUsers != null && findAllUsers.size() > 0) {
                    for (User user : findAllUsers) {
                        userCollection.getUsers().add(user);
                    }
                }                
            } else if (criteria.id != null) {
                User user = loginDAO.findUserById(criteria.id);
                if (user != null) {
                    userCollection.getUsers().add(user);
                }
            } else if (criteria.userNameEqualTo != null && !criteria.userNameEqualTo.isEmpty()) {
                User user = loginDAO.findUserByName(criteria.userNameEqualTo);
                if (user != null) {
                    userCollection.getUsers().add(user);
                }
            } 
        } catch (Exception ex) {
            log.error("Error during user search.", ex);
            throw new ASException(ErrorCode.MS_API_USER_SEARCH_ERROR, ex.getClass().getSimpleName());
        }
        log.debug("User:Search - Returning back {} of results.", userCollection.getUsers().size());                
        return userCollection;
    }

    @Override
    @RequiresPermissions("users:retrieve")        
    public User retrieve(UserLocator locator) {
        if( locator == null || locator.id == null ) { return null; }
        log.debug("User:Retrieve - Got request to retrieve user with id {}.", locator.id);                
         try (LoginDAO loginDAO = MyJdbi.authz()) {
            User user = loginDAO.findUserById(locator.id);
            if (user != null) {
                return user;
            }
        } catch (Exception ex) {
            log.error("Error during user search.", ex);
            throw new ASException(ErrorCode.MS_API_USER_SEARCH_ERROR, ex.getClass().getSimpleName());
        }
        return null;
    }

    @Override
    @RequiresPermissions("users:store")        
    public void store(User item) {
        log.debug("User:Create - Got request to update user with id {}.", item.getId().toString());        
         try (LoginDAO loginDAO = MyJdbi.authz()) {
            User user = loginDAO.findUserById(item.getId());
            if (user != null) {
                user.setComment(item.getComment());
                user.setEnabled(item.isEnabled());
                user.setStatus(item.getStatus());
                loginDAO.updateUser(user);
                log.debug("User:Store - Updated the user {} successfully.", user.getUsername());
            } else {
                log.error("User:Store - User {} will not be updated since it does not exist.");
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
    @RequiresPermissions("users:create")        
    public void create(User item) {
        log.debug("User:Create - Got request to create a new user {}.", item.getUsername());
         try (LoginDAO loginDAO = MyJdbi.authz()) {
            User user = loginDAO.findUserByName(item.getUsername());
            if (user == null) {
                user = new User();
                user.setId(item.getId());
                user.setUsername(item.getUsername());
                user.setComment(item.getComment());
                user.setEnabled(false);
                user.setStatus(Status.PENDING);
                loginDAO.insertUser(user.getId(), user.getUsername(), user.getLocale(), user.isEnabled(), user.getStatus(), user.getComment());
                log.debug("User:Create - Created the user {} successfully.", item.getUsername());
            } else {
                log.error("User:Create - User {} will not be created since a duplicate user already exists.", item.getUsername());
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
    @RequiresPermissions("users:delete")        
    public void delete(UserLocator locator) {
        if( locator == null || locator.id == null ) { return; }
        log.debug("User:Delete - Got request to delete user with id {}.", locator.id.toString());        
        try (LoginDAO loginDAO = MyJdbi.authz()) {
            User user = loginDAO.findUserById(locator.id);
            if (user != null ) {
                loginDAO.deleteUser(locator.id);
                log.debug("User:Delete - Deleted the user {} successfully.", user.getUsername());
            } else {
                log.info("User:Delete - User does not exist in the system.");
            }
        } catch (Exception ex) {
            log.error("Error during user deletion.", ex);
            throw new ASException(ErrorCode.MS_API_USER_DELETION_ERROR, ex.getClass().getSimpleName());
        }
    }
    
    @Override
    public void delete(UserFilterCriteria criteria) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
