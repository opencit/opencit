/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.security.rest.v2.repository;

import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.security.rest.v2.model.UserLoginHmac;
import com.intel.mtwilson.security.rest.v2.model.UserLoginHmacCollection;
import com.intel.mtwilson.security.rest.v2.model.UserLoginHmacFilterCriteria;
import com.intel.mtwilson.security.rest.v2.model.UserLoginHmacLocator;
import com.intel.mtwilson.datatypes.ErrorCode;
import com.intel.mtwilson.jersey.resource.SimpleRepository;
import com.intel.mtwilson.security.rest.v2.model.Status;
import com.intel.mtwilson.shiro.jdbi.LoginDAO;
import com.intel.mtwilson.shiro.jdbi.MyJdbi;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.apache.shiro.authz.annotation.RequiresPermissions;


/**
 *
 * @author ssbangal
 */
public class UserLoginHmacRepository implements SimpleRepository<UserLoginHmac, UserLoginHmacCollection, UserLoginHmacFilterCriteria, UserLoginHmacLocator> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UserLoginHmacRepository.class);
    
    @Override
    @RequiresPermissions("user_login_hmacs:search")        
    public UserLoginHmacCollection search(UserLoginHmacFilterCriteria criteria) {
        log.debug("UserLoginHmac:Search - Got request to search for the users login hmacs.");        
        UserLoginHmacCollection objCollection = new UserLoginHmacCollection();
        try (LoginDAO loginDAO = MyJdbi.authz()) {
            if (criteria.id != null) {
//                UserLoginHmac obj = loginDAO.findUserLoginHmacById(criteria.id);
//                if (obj != null) {
//                    objCollection.getUserLoginHmacs().add(obj);
//                }
            } else if (criteria.userIdEqualTo != null) {
//                UserLoginHmac obj = loginDAO.findUserLoginHmacByUserId(criteria.userIdEqualTo);
//                if (obj != null) {
//                    objCollection.getUserLoginHmacs().add(obj);
//                }
            } 
        } catch (Exception ex) {
            log.error("Error during user keystore search.", ex);
            throw new ASException(ErrorCode.MS_API_USER_SEARCH_ERROR, ex.getClass().getSimpleName());
        }
        log.debug("UserLoginHmac:Search - Returning back {} of results.", objCollection.getUserLoginHmacs().size());                
        return objCollection;
    }

    @Override
    @RequiresPermissions("user_login_hmacs:retrieve")        
    public UserLoginHmac retrieve(UserLoginHmacLocator locator) {
        if( locator == null || locator.id == null ) { return null; }
        log.debug("UserLoginHmac:Retrieve - Got request to retrieve user login hmac with id {}.", locator.id);                
         try (LoginDAO loginDAO = MyJdbi.authz()) {
//            UserLoginHmac obj = loginDAO.findUserLoginHmacById(locator.id);
//            if (obj != null) {
//                return obj;
//            }
        } catch (Exception ex) {
            log.error("Error during user login hmac search.", ex);
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
    @RequiresPermissions("user_login_hmacs:store")        
    public void store(UserLoginHmac item) {
        log.debug("UserLoginHmac:Store - Got request to update user login hmac with id {}.", item.getId().toString());        
         try (LoginDAO loginDAO = MyJdbi.authz()) {
//            UserLoginHmac obj = loginDAO.findUserLoginHmacById(item.getId());
//            if (obj != null) {
//                if (item.getComment() != null)
//                    obj.setComment(item.getComment());
//                
//                obj.setEnabled(item.isEnabled());
//                                
//                if (item.getStatus() != null)
//                    obj.setStatus(item.getStatus());
//                
//                loginDAO.updateUserLoginHmacById(obj.getId(), obj.isEnabled(), obj.getStatus(), obj.getComment());
//                log.debug("UserLoginHmac:Store - Updated the user login hmac with id {} successfully.", obj.getId());
//            } else {
//                log.error("UserLoginHmac:Store - User login hmac will not be updated since it does not exist.");
//                throw new WebApplicationException(Response.Status.NOT_FOUND);
//            }            
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
    @RequiresPermissions("user_login_hmacs:create")        
    public void create(UserLoginHmac item) {
        log.debug("UserLoginHmac:Create - Got request to create a new user keystore.");
         try (LoginDAO loginDAO = MyJdbi.authz()) {
//            UserLoginHmac obj = loginDAO.findUserLoginHmacByUserId(item.getUserId());
//            if (obj == null) {
//                obj = new UserLoginHmac();
//                obj.setUserId(item.getUserId());
//                obj.setHmac(item.getHmac());
//                obj.setComment(item.getComment());
//                obj.setEnabled(false);
//                obj.setExpires(item.getExpires());
//                obj.setSha1Hash(item.getSha1Hash());
//                obj.setSha256Hash(item.getSha256Hash());
//                obj.setStatus(Status.PENDING);
//                loginDAO.insertUserLoginHmac(obj.getId(), obj.getUserId(), obj.getHmac(), obj.getSha1Hash(), obj.getSha256Hash(),
//                        obj.getExpires(), obj.isEnabled(), obj.getStatus(), obj.getComment());
//                log.debug("UserLoginHmac:Create - Created the user login hmac for user with id {} successfully.", obj.getUserId());
//            } else {
//                log.error("UserLoginHmac:Create - User login hmac for user with Id {} will not be created since a duplicate already exists.", obj.getUserId());
//                throw new WebApplicationException(Response.Status.CONFLICT);
//            }            
        } catch (WebApplicationException wex) {
            throw wex;
        } catch (Exception ex) {
            log.error("Error during user creation.", ex);
            throw new ASException(ErrorCode.MS_API_USER_REGISTRATION_ERROR, ex.getClass().getSimpleName());
        }
    }

    @Override
    @RequiresPermissions("user_login_hmacs:delete")        
    public void delete(UserLoginHmacLocator locator) {
        if( locator == null || locator.id == null ) { return; }
        log.debug("UserLoginHmac:Delete - Got request to delete user login hmac with id {}.", locator.id.toString());        
        try (LoginDAO loginDAO = MyJdbi.authz()) {
//            UserLoginHmac obj = loginDAO.findUserLoginHmacById(locator.id);
//            if (obj != null ) {
//                loginDAO.deleteUserLoginHmacById(locator.id);
//                log.debug("UserLoginHmac:Delete - Deleted the user login hmac with id {} successfully.", locator.id);
//            } else {
//                log.info("UserLoginHmac:Delete - User login hmac does not exist in the system.");
//            }
        } catch (Exception ex) {
            log.error("Error during user deletion.", ex);
            throw new ASException(ErrorCode.MS_API_USER_DELETION_ERROR, ex.getClass().getSimpleName());
        }
    }
    
    @Override
    @RequiresPermissions("user_login_hmacs:delete,search")        
    public void delete(UserLoginHmacFilterCriteria criteria) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
