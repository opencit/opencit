/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.user.management.rest.v2.repository;

import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginHmacRole;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginHmacRoleCollection;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginHmacRoleFilterCriteria;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginHmacRoleLocator;
import com.intel.mtwilson.datatypes.ErrorCode;
import com.intel.mtwilson.jersey.resource.SimpleRepository;
import com.intel.mtwilson.shiro.jdbi.LoginDAO;
import com.intel.mtwilson.shiro.jdbi.MyJdbi;
import java.util.List;
import javax.ws.rs.WebApplicationException;
import org.apache.shiro.authz.annotation.RequiresPermissions;


/**
 *
 * @author ssbangal
 */
public class UserLoginHmacRoleRepository implements SimpleRepository<UserLoginHmacRole, UserLoginHmacRoleCollection, UserLoginHmacRoleFilterCriteria, UserLoginHmacRoleLocator> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UserLoginHmacRoleRepository.class);
    
    @Override
    @RequiresPermissions("user_login_certificate_roles:search")        
    public UserLoginHmacRoleCollection search(UserLoginHmacRoleFilterCriteria criteria) {
        log.debug("UserLoginHmacRole:Search - Got request to search for the users login certificates.");        
        UserLoginHmacRoleCollection objCollection = new UserLoginHmacRoleCollection();
        try (LoginDAO loginDAO = MyJdbi.authz()) {
            if (criteria.loginHmacIdEqualTo != null) {
//                List<UserLoginHmacRole> objList = loginDAO.findUserLoginHmacRolesByUserLoginCertificateId(criteria.loginHmacIdEqualTo);
//                if (objList != null && objList.size() > 0) {
//                    for (UserLoginHmacRole obj : objList) {
//                        objCollection.getUserLoginHmacRoles().add(obj);
//                    }
//                }
            } else if (criteria.roleIdEqualTo != null) {
//                List<UserLoginHmacRole> objList = loginDAO.findUserLoginHmacRolesByUserLoginCertificateId(criteria.roleIdEqualTo);
//                if (objList != null && objList.size() > 0) {
//                    for (UserLoginHmacRole obj : objList) {
//                        objCollection.getUserLoginHmacRoles().add(obj);
//                    }
//                }
            }  
        } catch (Exception ex) {
            log.error("Error during user login hmac role search.", ex);
            throw new ASException(ErrorCode.MS_API_USER_SEARCH_ERROR, ex.getClass().getSimpleName());
        }
        log.debug("UserLoginHmacRole:Search - Returning back {} of results.", objCollection.getUserLoginHmacRoles().size());                
        return objCollection;
    }

    /**
     * Unlike other tables, for this table the primary key is a combination of the loginHmacId and roleId. So, we cannot
     * use the locator object. Hence, store, retrieve and delete will not be supported. But users can get the list using search and
     * also delete by specifying the search criteria.
     * @param locator
     * @return 
     */
    @Override
    @RequiresPermissions("user_login_certificate_roles:retrieve")        
    public UserLoginHmacRole retrieve(UserLoginHmacRoleLocator locator) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.        
    }

    /**
     * Unlike other tables, for this table the primary key is a combination of the loginHmacId and roleId. So, we cannot
     * use the locator object. Hence, store, retrieve and delete will not be supported. But users can get the list using search and
     * also delete by specifying the search criteria.
     * @param item 
     */
    @Override
    @RequiresPermissions("user_login_certificate_roles:store")        
    public void store(UserLoginHmacRole item) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.        
    }

    @Override
    @RequiresPermissions("user_login_certificate_roles:create")        
    public void create(UserLoginHmacRole item) {
        log.debug("UserLoginHmacRole:Create - Got request to create a new login certificate role.");
         try (LoginDAO loginDAO = MyJdbi.authz()) {
//            UserLoginHmacRole obj = loginDAO.findUserLoginHmacRolesByRoleIdAndUserLoginCertificateId(item.getLoginHmacId(), item.getRoleId());
//            if (obj == null) {
//                obj = new UserLoginHmacRole();
//                obj.setLoginHmacId(item.getLoginHmacId());
//                obj.setRoleId(item.getRoleId());
//                loginDAO.insertUserLoginHmacRole(obj.getLoginHmacId(), obj.getRoleId());
//                log.debug("UserLoginHmacRole:Create - Created the user login hmac role successfully.");
//            } else {
//                log.info("UserLoginHmacRole:Create - User login hmac role specified already exists.");
//                //throw new WebApplicationException(Response.Status.CONFLICT);
//            }            
        } catch (WebApplicationException wex) {
            throw wex;
        } catch (Exception ex) {
            log.error("Error during user creation.", ex);
            throw new ASException(ErrorCode.MS_API_USER_REGISTRATION_ERROR, ex.getClass().getSimpleName());
        }
    }

    /**
     * Unlike other tables, for this table the primary key is a combination of the loginCertificateId and roleId. So, we cannot
     * use the locator object. Hence, store, retrieve and delete will not be supported. But users can get the list using search and
     * also delete by specifying the search criteria.
     * @param locator 
     */
    @Override
    @RequiresPermissions("user_login_certificate_roles:delete")        
    public void delete(UserLoginHmacRoleLocator locator) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.        
    }
    
    @Override
    @RequiresPermissions("user_login_certificate_roles:delete,search")        
    public void delete(UserLoginHmacRoleFilterCriteria criteria) {
        UserLoginHmacRoleCollection objList = search(criteria);
        try (LoginDAO loginDAO = MyJdbi.authz()) { 
            for (UserLoginHmacRole obj : objList.getUserLoginHmacRoles()) {
                //loginDAO.deleteUserLoginHmacRole(obj.getLoginHmacId(), obj.getRoleId());
            }
        } catch (Exception ex) {
            log.error("Error during user login certificate role deletion.", ex);
            throw new ASException(ErrorCode.MS_API_USER_REGISTRATION_ERROR, ex.getClass().getSimpleName());
        }
    }
    
}
