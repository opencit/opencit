/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.user.management.rest.v2.repository;

import com.intel.mtwilson.user.management.rest.v2.model.UserLoginPasswordRole;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginPasswordRoleCollection;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginPasswordRoleFilterCriteria;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginPasswordRoleLocator;
import com.intel.mtwilson.jaxrs2.server.resource.DocumentRepository;
import com.intel.mtwilson.repository.RepositoryCreateException;
import com.intel.mtwilson.repository.RepositoryDeleteException;
import com.intel.mtwilson.repository.RepositoryException;
import com.intel.mtwilson.repository.RepositorySearchException;
import com.intel.mtwilson.shiro.jdbi.LoginDAO;
import com.intel.mtwilson.shiro.jdbi.MyJdbi;
import java.util.List;
import org.apache.shiro.authz.annotation.RequiresPermissions;


/**
 *
 * @author ssbangal
 */
public class UserLoginPasswordRoleRepository implements DocumentRepository<UserLoginPasswordRole, UserLoginPasswordRoleCollection, UserLoginPasswordRoleFilterCriteria, UserLoginPasswordRoleLocator> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UserLoginPasswordRoleRepository.class);
    
    @Override
    @RequiresPermissions("user_login_password_roles:search")        
    public UserLoginPasswordRoleCollection search(UserLoginPasswordRoleFilterCriteria criteria) {
        log.debug("UserLoginPasswordRole:Search - Got request to search for the user login password roles.");        
        UserLoginPasswordRoleCollection objCollection = new UserLoginPasswordRoleCollection();
        try (LoginDAO loginDAO = MyJdbi.authz()) {
            if (criteria.loginPasswordIdEqualTo != null) {
                List<UserLoginPasswordRole> objList = loginDAO.findUserLoginPasswordRolesByUserLoginPasswordId(criteria.loginPasswordIdEqualTo);
                if (objList != null && objList.size() > 0) {
                    for (UserLoginPasswordRole obj : objList) {
                        objCollection.getUserLoginPasswordRoles().add(obj);
                    }
                }
            } else if (criteria.roleIdEqualTo != null) {
                List<UserLoginPasswordRole> objList = loginDAO.findUserLoginPasswordRolesByRoleId(criteria.roleIdEqualTo);
                if (objList != null && objList.size() > 0) {
                    for (UserLoginPasswordRole obj : objList) {
                        objCollection.getUserLoginPasswordRoles().add(obj);
                    }
                }
            }  
        } catch (Exception ex) {
            log.error("Error during user login password role search.", ex);
            throw new RepositorySearchException(ex, criteria);
        }
        log.debug("UserLoginPasswordRole:Search - Returning back {} of results.", objCollection.getUserLoginPasswordRoles().size());                
        return objCollection;
    }

    /**
     * Unlike other tables, for this table the primary key is a combination of the loginPasswordId and roleId. So, we cannot
     * use the locator object. Hence, store, retrieve and delete will not be supported. But users can get the list using search and
     * also delete by specifying the search criteria.
     * @param locator
     * @return 
     */
    @Override
    @RequiresPermissions("user_login_password_roles:retrieve")        
    public UserLoginPasswordRole retrieve(UserLoginPasswordRoleLocator locator) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.        
    }

    /**
     * Unlike other tables, for this table the primary key is a combination of the loginPasswordId and roleId. So, we cannot
     * use the locator object. Hence, store, retrieve and delete will not be supported. But users can get the list using search and
     * also delete by specifying the search criteria.
     * @param item 
     */
    @Override
    @RequiresPermissions("user_login_password_roles:store")        
    public void store(UserLoginPasswordRole item) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.        
    }

    @Override
    @RequiresPermissions("user_login_password_roles:create")        
    public void create(UserLoginPasswordRole item) {
        log.debug("UserLoginPasswordRole:Create - Got request to create a new user login password role.");
         try (LoginDAO loginDAO = MyJdbi.authz()) {
            UserLoginPasswordRole obj = loginDAO.findUserLoginPasswordRolesByUserLoginPasswordIdAndRoleId(item.getLoginPasswordId(), item.getRoleId());
            if (obj == null) {
                obj = new UserLoginPasswordRole();
                obj.setLoginPasswordId(item.getLoginPasswordId());
                obj.setRoleId(item.getRoleId());
                loginDAO.insertUserLoginPasswordRole(obj.getLoginPasswordId(), obj.getRoleId());
                log.debug("UserLoginPasswordRole:Create - Created the user login password role successfully.");
            } else {
                log.info("UserLoginPasswordRole:Create - User login password role specified already exists.");
            }            
        } catch (RepositoryException re) {
            throw re;
        } catch (Exception ex) {
            log.error("Error during user login password role creation.", ex);
            throw new RepositoryCreateException(ex);
        }
    }

    /**
     * Unlike other tables, for this table the primary key is a combination of the loginPasswordId and roleId. So, we cannot
     * use the locator object. Hence, store, retrieve and delete will not be supported. But users can get the list using search and
     * also delete by specifying the search criteria.
     * @param locator 
     */
    @Override
    @RequiresPermissions("user_login_password_roles:delete")        
    public void delete(UserLoginPasswordRoleLocator locator) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.        
    }
    
    @Override
    @RequiresPermissions("user_login_password_roles:delete,search")        
    public void delete(UserLoginPasswordRoleFilterCriteria criteria) {
        log.debug("UserLoginPasswordRole:Delete - Got request to delete login password role by search criteria.");        
        UserLoginPasswordRoleCollection objList = search(criteria);
        try (LoginDAO loginDAO = MyJdbi.authz()) { 
            for (UserLoginPasswordRole obj : objList.getUserLoginPasswordRoles()) {
                loginDAO.deleteUserLoginPasswordRole(obj.getLoginPasswordId(), obj.getRoleId());
            }
        } catch(RepositoryException re) {
            throw re;
        } catch (Exception ex) {
            log.error("Error during user login password role deletion.", ex);
            throw new RepositoryDeleteException(ex);
        }
    }
    
}
