/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.security.rest.v2.repository;

import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.security.rest.v2.model.UserKeystore;
import com.intel.mtwilson.security.rest.v2.model.UserKeystoreCollection;
import com.intel.mtwilson.security.rest.v2.model.UserKeystoreFilterCriteria;
import com.intel.mtwilson.security.rest.v2.model.UserKeystoreLocator;
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
public class UserKeystoreRepository implements SimpleRepository<UserKeystore, UserKeystoreCollection, UserKeystoreFilterCriteria, UserKeystoreLocator> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UserKeystoreRepository.class);
    
    @Override
    @RequiresPermissions("user_keystores:search")        
    public UserKeystoreCollection search(UserKeystoreFilterCriteria criteria) {
        log.debug("UserKeystore:Search - Got request to search for the users.");        
        UserKeystoreCollection objCollection = new UserKeystoreCollection();
        try (LoginDAO loginDAO = MyJdbi.authz()) {
            if (criteria.id != null) {
                UserKeystore userKeystore = loginDAO.findUserKeystoreById(criteria.id);
                if (userKeystore != null) {
                    objCollection.getUserKeystores().add(userKeystore);
                }
            } else if (criteria.userIdEqualTo != null) {
                UserKeystore userKeystore = loginDAO.findUserKeystoreByUserId(criteria.userIdEqualTo);
                if (userKeystore != null) {
                    objCollection.getUserKeystores().add(userKeystore);
                }
            } else {
                List<UserKeystore> findAllUserKeystores = loginDAO.findAllUserKeystores();
                if (findAllUserKeystores != null && findAllUserKeystores.size() > 0) {
                    for (UserKeystore userKeystore : findAllUserKeystores) {
                        objCollection.getUserKeystores().add(userKeystore);
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Error during user keystore search.", ex);
            throw new ASException(ErrorCode.MS_API_USER_SEARCH_ERROR, ex.getClass().getSimpleName());
        }
        log.debug("UserKeystore:Search - Returning back {} of results.", objCollection.getUserKeystores().size());                
        return objCollection;
    }

    @Override
    @RequiresPermissions("user_keystores:retrieve")        
    public UserKeystore retrieve(UserKeystoreLocator locator) {
        if( locator == null || locator.id == null ) { return null; }
        log.debug("UserKeystore:Retrieve - Got request to retrieve user with id {}.", locator.id);                
         try (LoginDAO loginDAO = MyJdbi.authz()) {
            UserKeystore userKeystore = loginDAO.findUserKeystoreById(locator.id);
            if (userKeystore != null) {
                return userKeystore;
            }
        } catch (Exception ex) {
            log.error("Error during user search.", ex);
            throw new ASException(ErrorCode.MS_API_USER_SEARCH_ERROR, ex.getClass().getSimpleName());
        }
        return null;
    }

    /**
     * Used for updating the existing user
     * 
     * @param item 
     */
    @Override
    @RequiresPermissions("user_keystores:store")        
    public void store(UserKeystore item) {
        log.debug("UserKeystore:Store - Got request to update user with id {}.", item.getId().toString());        
         try (LoginDAO loginDAO = MyJdbi.authz()) {
            UserKeystore userKeystore = loginDAO.findUserKeystoreById(item.getId());
            if (userKeystore != null) {
                userKeystore.setComment(item.getComment());
                userKeystore.setKeystore(item.getKeystore());
                userKeystore.setKeystoreFormat(item.getKeystoreFormat());
                loginDAO.updateUserKeystore(userKeystore.getId(), userKeystore.getKeystore(), userKeystore.getKeystoreFormat(), userKeystore.getComment());
                log.debug("UserKeystore:Store - Updated the user keystore with id {} successfully.", userKeystore.getId());
            } else {
                log.error("UserKeystore:Store - UserKeystore {} will not be updated since it does not exist.");
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }            
        } catch (WebApplicationException wex) {
            throw wex;
        } catch (Exception ex) {
            log.error("Error during user update.", ex);
            throw new ASException(ErrorCode.MS_API_USER_UPDATE_ERROR, ex.getClass().getSimpleName());
        }
        
    }

    /**
     * Creates a new user
     * @param item 
     */
    @Override
    @RequiresPermissions("user_keystores:create")        
    public void create(UserKeystore item) {
        log.debug("UserKeystore:Create - Got request to create a new user keystore.");
         try (LoginDAO loginDAO = MyJdbi.authz()) {
            UserKeystore userKeystore = loginDAO.findUserKeystoreByUserId(item.getUserId());
            if (userKeystore == null) {
                userKeystore = new UserKeystore();
                userKeystore.setUserId(item.getUserId());
                userKeystore.setComment(item.getComment());
                userKeystore.setKeystore(item.getKeystore());
                userKeystore.setKeystoreFormat(item.getKeystoreFormat());
                loginDAO.insertUserKeystore(userKeystore.getId(), userKeystore.getUserId(), userKeystore.getKeystore(), userKeystore.getKeystoreFormat(), userKeystore.getComment());
                log.debug("UserKeystore:Create - Created the user keystore for user with id {} successfully.", userKeystore.getUserId());
            } else {
                log.error("UserKeystore:Create - UserKeystore for user with Id {} will not be created since a duplicate already exists.", userKeystore.getUserId());
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
    @RequiresPermissions("user_keystores:delete")        
    public void delete(UserKeystoreLocator locator) {
        if( locator == null || locator.id == null ) { return; }
        log.debug("UserKeystore:Delete - Got request to delete user keystore with id {}.", locator.id.toString());        
        try (LoginDAO loginDAO = MyJdbi.authz()) {
            UserKeystore userKeystore = loginDAO.findUserKeystoreById(locator.id);
            if (userKeystore != null ) {
                loginDAO.deleteUserKeystoreById(locator.id);
                log.debug("UserKeystore:Delete - Deleted the user keystore with id {} successfully.", locator.id);
            } else {
                log.info("UserKeystore:Delete - UserKeystore does not exist in the system.");
            }
        } catch (Exception ex) {
            log.error("Error during user deletion.", ex);
            throw new ASException(ErrorCode.MS_API_USER_DELETION_ERROR, ex.getClass().getSimpleName());
        }
    }
    
    @Override
    @RequiresPermissions("user_keystores:delete,search")        
    public void delete(UserKeystoreFilterCriteria criteria) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
