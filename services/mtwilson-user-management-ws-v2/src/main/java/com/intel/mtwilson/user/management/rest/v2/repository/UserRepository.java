/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.user.management.rest.v2.repository;

import com.intel.dcsg.cpg.i18n.LocaleUtil;
import com.intel.mtwilson.user.management.rest.v2.model.User;
import com.intel.mtwilson.user.management.rest.v2.model.UserCollection;
import com.intel.mtwilson.user.management.rest.v2.model.UserFilterCriteria;
import com.intel.mtwilson.user.management.rest.v2.model.UserLocator;
import com.intel.mtwilson.jaxrs2.server.resource.DocumentRepository;
import com.intel.mtwilson.repository.RepositoryCreateConflictException;
import com.intel.mtwilson.repository.RepositoryCreateException;
import com.intel.mtwilson.repository.RepositoryDeleteException;
import com.intel.mtwilson.repository.RepositoryException;
import com.intel.mtwilson.repository.RepositoryRetrieveException;
import com.intel.mtwilson.repository.RepositorySearchException;
import com.intel.mtwilson.repository.RepositoryStoreConflictException;
import com.intel.mtwilson.repository.RepositoryStoreException;
import com.intel.mtwilson.shiro.jdbi.LoginDAO;
import com.intel.mtwilson.shiro.jdbi.MyJdbi;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginCertificateFilterCriteria;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginPasswordFilterCriteria;
import java.util.List;
import org.apache.shiro.authz.annotation.RequiresPermissions;


/**
 *
 * @author ssbangal
 */
public class UserRepository implements DocumentRepository<User, UserCollection, UserFilterCriteria, UserLocator> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UserRepository.class);
    
    @Override
    @RequiresPermissions("users:search")        
    public UserCollection search(UserFilterCriteria criteria) {
        log.debug("User:Search - Got request to search for the users. Filter criteria is {}", criteria.filter);        
        UserCollection userCollection = new UserCollection();
        try (LoginDAO loginDAO = MyJdbi.authz()) {
            if (criteria.filter) {
                if (criteria.id != null) {
                    User user = loginDAO.findUserById(criteria.id);
                    if (user != null) {
                        userCollection.getUsers().add(user);
                    }
                } else if (criteria.nameEqualTo != null && !criteria.nameEqualTo.isEmpty()) {
                    User user = loginDAO.findUserByName(criteria.nameEqualTo);
                    if (user != null) {
                        userCollection.getUsers().add(user);
                    }
                } else if (criteria.nameContains != null && !criteria.nameContains.isEmpty()) {
                    List<User> users = loginDAO.findUserByNameLike("%"+criteria.nameContains+"%");
                    if (users != null && users.size() > 0) {
                        for (User user : users) {
                            userCollection.getUsers().add(user);
                        }
                    }                
                }
            } else {
                List<User> findAllUsers = loginDAO.findAllUsers();
                if (findAllUsers != null && findAllUsers.size() > 0) {
                    for (User user : findAllUsers) {
                        userCollection.getUsers().add(user);
                    }
                }                
            }
        } catch (Exception ex) {
            log.error("User:Search - Error during user search.", ex);
            throw new RepositorySearchException(ex, criteria);
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
            log.error("User:Retrieve - Error during user search.", ex);
            throw new RepositoryRetrieveException(ex, locator);
        }
        return null;
    }

    @Override
    @RequiresPermissions("users:store")        
    public void store(User item) {
        log.debug("User:Store - Got request to update user with id {}.", item.getId().toString());        
        UserLocator locator = new UserLocator(); // will be used if we need to throw an exception
        locator.id = item.getId();
         try (LoginDAO loginDAO = MyJdbi.authz()) {
            User user = loginDAO.findUserById(item.getId());
            if (user != null) {
                user.setComment(item.getComment());
                if (item.getLocale() != null)
                    user.setLocale(item.getLocale());
                loginDAO.updateUser(user.getId(), LocaleUtil.toLanguageTag(user.getLocale()), user.getComment());
                log.debug("User:Store - Updated the user {} successfully.", user.getUsername());
            } else {
                log.error("User:Store - User will not be updated since it does not exist.");
                throw new RepositoryStoreConflictException(locator);
            }
         } catch(RepositoryException re) { 
             throw re; 
        } catch (Exception ex) {
            log.error("User:Store - Error during user update.", ex);
            throw new RepositoryStoreException(ex, locator);
        }
        
    }

    @Override
    public void create(User item) {
        log.debug("User:Create - Got request to create a new user {}.", item.getUsername());
        UserLocator locator = new UserLocator(); // will be used if we need to throw an exception
        locator.id = item.getId();
         try (LoginDAO loginDAO = MyJdbi.authz()) {
            User user = loginDAO.findUserByName(item.getUsername());
            if (user == null) {
                user = new User();
                user.setId(item.getId());
                user.setUsername(item.getUsername());
                user.setComment(item.getComment());
                String localeTag = null;
                if (item.getLocale() != null)
                    localeTag = LocaleUtil.toLanguageTag(item.getLocale());
                loginDAO.insertUser(user.getId(), user.getUsername(), localeTag, user.getComment());
                log.debug("User:Create - Created the user {} successfully.", item.getUsername());
            } else {
                log.error("User:Create - User {} will not be created since a duplicate user already exists.", item.getUsername());
                throw new RepositoryCreateConflictException(locator);
            }            
        } catch (RepositoryException re) {
            throw re;
        } catch (Exception ex) {
            log.error("User:Create - Error during user creation.", ex);
            throw new RepositoryCreateException(ex, locator);
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
                // First we need to delete the user's associated logins (certificate/password/hmac)
                UserLoginCertificateRepository certRepo = new UserLoginCertificateRepository();
                UserLoginCertificateFilterCriteria certCriteria = new UserLoginCertificateFilterCriteria();
                certCriteria.userUuid = user.getId();
                certRepo.delete(certCriteria);
                log.debug("User:Delete - Deleted the user {} login certificate entries successfully.", user.getUsername());
                
                UserLoginPasswordRepository passwordRepo = new UserLoginPasswordRepository();
                UserLoginPasswordFilterCriteria passwordCriteria = new UserLoginPasswordFilterCriteria();
                passwordCriteria.userUuid = user.getId();
                passwordRepo.delete(passwordCriteria);
                log.debug("User:Delete - Deleted the user {} login password entries successfully.", user.getUsername());
                
                loginDAO.deleteUser(locator.id);
                log.debug("User:Delete - Deleted the user {} successfully.", user.getUsername());
            } else {
                log.info("User:Delete - User does not exist in the system.");
            }
        } catch (Exception ex) {
            log.error("User:Delete - Error during user deletion.", ex);
            throw new RepositoryDeleteException(ex, locator);
        }
    }
    
    @Override
    public void delete(UserFilterCriteria criteria) {
        log.debug("User:Delete - Got request to delete user by search criteria.");        
        UserCollection objCollection = search(criteria);
        try { 
            for (User obj : objCollection.getUsers()) {
                UserLocator locator = new UserLocator();
                locator.id = obj.getId();
                delete(locator);
            }
        } catch(RepositoryException re) {
            throw re;
        } catch (Exception ex) {
            log.error("User:Delete - Error during User deletion.", ex);
            throw new RepositoryDeleteException(ex);
        }
    }
    
}
