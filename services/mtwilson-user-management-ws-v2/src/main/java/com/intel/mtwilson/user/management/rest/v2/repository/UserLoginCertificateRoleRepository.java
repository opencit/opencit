/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.user.management.rest.v2.repository;

import com.intel.mtwilson.user.management.rest.v2.model.UserLoginCertificateRole;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginCertificateRoleCollection;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginCertificateRoleFilterCriteria;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginCertificateRoleLocator;
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
public class UserLoginCertificateRoleRepository implements DocumentRepository<UserLoginCertificateRole, UserLoginCertificateRoleCollection, UserLoginCertificateRoleFilterCriteria, UserLoginCertificateRoleLocator> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UserLoginCertificateRoleRepository.class);
    
    @Override
//    @RequiresPermissions("user_login_certificate_roles:search")        
    public UserLoginCertificateRoleCollection search(UserLoginCertificateRoleFilterCriteria criteria) {
        log.debug("UserLoginCertificateRole:Search - Got request to search for the users login certificates.");        
        UserLoginCertificateRoleCollection objCollection = new UserLoginCertificateRoleCollection();
        try (LoginDAO loginDAO = MyJdbi.authz()) {
            if (criteria.loginCertificateIdEqualTo != null) {
                List<UserLoginCertificateRole> objList = loginDAO.findUserLoginCertificateRolesByUserLoginCertificateId(criteria.loginCertificateIdEqualTo);
                if (objList != null && objList.size() > 0) {
                    for (UserLoginCertificateRole obj : objList) {
                        objCollection.getUserLoginCertificateRoles().add(obj);
                    }
                }
            } else if (criteria.roleIdEqualTo != null) {
                List<UserLoginCertificateRole> objList = loginDAO.findUserLoginCertificateRolesByRoleId(criteria.roleIdEqualTo);
                if (objList != null && objList.size() > 0) {
                    for (UserLoginCertificateRole obj : objList) {
                        objCollection.getUserLoginCertificateRoles().add(obj);
                    }
                }
            }  
        } catch (Exception ex) {
            log.error("Error during user login certificate role search.", ex);
            throw new RepositorySearchException(ex, criteria);
        }
        log.debug("UserLoginCertificateRole:Search - Returning back {} of results.", objCollection.getUserLoginCertificateRoles().size());                
        return objCollection;
    }

    /**
     * Unlike other tables, for this table the primary key is a combination of the loginCertificateId and roleId. So, we cannot
     * use the locator object. Hence, store, retrieve and delete will not be supported. But users can get the list using search and
     * also delete by specifying the search criteria.
     * @param locator
     * @return 
     */
    @Override
    @RequiresPermissions("user_login_certificate_roles:retrieve")        
    public UserLoginCertificateRole retrieve(UserLoginCertificateRoleLocator locator) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.        
    }

    /**
     * Unlike other tables, for this table the primary key is a combination of the loginCertificateId and roleId. So, we cannot
     * use the locator object. Hence, store, retrieve and delete will not be supported. But users can get the list using search and
     * also delete by specifying the search criteria.
     * @param item 
     */
    @Override
    @RequiresPermissions("user_login_certificate_roles:store")        
    public void store(UserLoginCertificateRole item) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.        
    }

    @Override
//    @RequiresPermissions("user_login_certificate_roles:create")        
    public void create(UserLoginCertificateRole item) {
        log.debug("UserLoginCertificateRole:Create - Got request to create a new login certificate role.");
         try (LoginDAO loginDAO = MyJdbi.authz()) {
            UserLoginCertificateRole obj = loginDAO.findUserLoginCertificateRolesByRoleIdAndUserLoginCertificateId(item.getLoginCertificateId(), item.getRoleId());
            if (obj == null) {
                obj = new UserLoginCertificateRole();
                obj.setLoginCertificateId(item.getLoginCertificateId());
                obj.setRoleId(item.getRoleId());
                loginDAO.insertUserLoginCertificateRole(obj.getLoginCertificateId(), obj.getRoleId());
                log.debug("UserLoginCertificateRole:Create - Created the user login certificate role successfully.");
            } else {
                log.info("UserLoginCertificateRole:Create - User login certificate role specified already exists.");
            }            
        } catch (RepositoryException re) {
            throw re;
        } catch (Exception ex) {
            log.error("Error during User login certificate role creation.", ex);
            throw new RepositoryCreateException(ex);
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
    public void delete(UserLoginCertificateRoleLocator locator) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.        
    }
    
    @Override
//    @RequiresPermissions("user_login_certificate_roles:delete,search")        
    public void delete(UserLoginCertificateRoleFilterCriteria criteria) {
        log.debug("UserLoginCertificateRole:Delete - Got request to delete login certificate role by search criteria.");        
        UserLoginCertificateRoleCollection objList = search(criteria);
        try (LoginDAO loginDAO = MyJdbi.authz()) { 
            for (UserLoginCertificateRole obj : objList.getUserLoginCertificateRoles()) {
                loginDAO.deleteUserLoginCertificateRole(obj.getLoginCertificateId(), obj.getRoleId());
            }
        } catch(RepositoryException re) {
            throw re;
        } catch (Exception ex) {
            log.error("Error during user login certificate role deletion.", ex);
            throw new RepositoryDeleteException(ex);
        }
    }
    
}
