/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.user.management.rest.v2.repository;

import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.crypto.Sha256Digest;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginCertificate;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginCertificateCollection;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginCertificateFilterCriteria;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginCertificateLocator;
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
import com.intel.mtwilson.user.management.rest.v2.model.Status;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginCertificateRole;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginCertificateRoleCollection;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginCertificateRoleFilterCriteria;
import com.intel.mtwilson.shiro.jdbi.LoginDAO;
import com.intel.mtwilson.shiro.jdbi.MyJdbi;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.ws.rs.WebApplicationException;
import org.apache.shiro.authz.annotation.RequiresPermissions;


/**
 *
 * @author ssbangal
 */
public class UserLoginCertificateRepository implements DocumentRepository<UserLoginCertificate, UserLoginCertificateCollection, UserLoginCertificateFilterCriteria, UserLoginCertificateLocator> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UserLoginCertificateRepository.class);
    
    @Override
    @RequiresPermissions("user_login_certificates:search")        
    public UserLoginCertificateCollection search(UserLoginCertificateFilterCriteria criteria) {
        log.debug("UserLoginCertificate:Search - Got request to search for the users login certificates.");        
        UserLoginCertificateCollection objCollection = new UserLoginCertificateCollection();
        try (LoginDAO loginDAO = MyJdbi.authz()) {
            if (criteria.userUuid != null) {
                UserLoginCertificate obj = loginDAO.findUserLoginCertificateByUserId(criteria.userUuid);
                if (obj != null) {
                    if (criteria.filter == false) {
                        obj.setRoles(getAssociateRolesForLoginCertificateId(obj.getId()));
                        objCollection.getUserLoginCertificates().add(obj);                                                
                    } else if (criteria.id != null) {
                        if (obj.getId().equals(criteria.id)) {
                            obj.setRoles(getAssociateRolesForLoginCertificateId(obj.getId()));
                            objCollection.getUserLoginCertificates().add(obj);
                        }
                    } else if (criteria.enabled != null && criteria.status != null) {
                        if (obj.isEnabled() == criteria.enabled && obj.getStatus().equals(criteria.status)) {
                            obj.setRoles(getAssociateRolesForLoginCertificateId(obj.getId()));
                            objCollection.getUserLoginCertificates().add(obj);
                        }
                    } else if (criteria.enabled != null) {
                        if (obj.isEnabled() == criteria.enabled) {
                            obj.setRoles(getAssociateRolesForLoginCertificateId(obj.getId()));
                            objCollection.getUserLoginCertificates().add(obj);
                        }
                    } else if (criteria.status != null) {
                        if (obj.getStatus().equals(criteria.status)) {
                            obj.setRoles(getAssociateRolesForLoginCertificateId(obj.getId()));
                            objCollection.getUserLoginCertificates().add(obj);
                        }
                    } else if (criteria.sha1 != null) {
                        if (Arrays.equals(obj.getSha1Hash(), criteria.sha1)) {
                            obj.setRoles(getAssociateRolesForLoginCertificateId(obj.getId()));
                            objCollection.getUserLoginCertificates().add(obj);
                        }
                    } else if (criteria.sha256 != null) {
                        if (Arrays.equals(obj.getSha256Hash(), criteria.sha256)) {
                            obj.setRoles(getAssociateRolesForLoginCertificateId(obj.getId()));
                            objCollection.getUserLoginCertificates().add(obj);
                        }
                    } 
                }
            }
            
        } catch (Exception ex) {
            log.error("Error during user keystore search.", ex);
            throw new RepositorySearchException(ex, criteria);
        }
        log.debug("UserLoginCertificate:Search - Returning back {} of results.", objCollection.getUserLoginCertificates().size());                
        return objCollection;
    }

    @Override
    @RequiresPermissions("user_login_certificates:retrieve")        
    public UserLoginCertificate retrieve(UserLoginCertificateLocator locator) {
        if( locator == null || locator.id == null ) { return null; }
        log.debug("UserLoginCertificate:Retrieve - Got request to retrieve user login certificate with id {}.", locator.id);                
         try (LoginDAO loginDAO = MyJdbi.authz()) {
            UserLoginCertificate obj = loginDAO.findUserLoginCertificateById(locator.id);
            if (obj != null) {
                obj.setRoles(getAssociateRolesForLoginCertificateId(obj.getId()));
                return obj;
            }
        } catch (Exception ex) {
            log.error("Error during user login certificate search.", ex);
            throw new RepositoryRetrieveException(ex, locator);
        }
        return null;
    }

    @Override
    @RequiresPermissions("user_login_certificates:store")        
    public void store(UserLoginCertificate item) {
        log.debug("UserLoginCertificate:Store - Got request to update user login certificate with id {}.", item.getId().toString()); 
        UserLoginCertificateLocator locator = new UserLoginCertificateLocator();
        locator.id = item.getId();        
         try (LoginDAO loginDAO = MyJdbi.authz()) {
            UserLoginCertificate obj = loginDAO.findUserLoginCertificateById(item.getId());
            if (obj != null) {
                if (item.getComment() != null)
                    obj.setComment(item.getComment());
                
                obj.setEnabled(item.isEnabled());
                                
                if (item.getStatus() != null)
                    obj.setStatus(item.getStatus());
                
                loginDAO.updateUserLoginCertificateById(obj.getId(), obj.isEnabled(), obj.getStatus(), obj.getComment());
                log.debug("UserLoginCertificate:Store - Updated the user login certificate with id {} successfully.", obj.getId());

                // Before we add the roles we need to delete the existing ones
                UserLoginCertificateRoleRepository repo = new UserLoginCertificateRoleRepository();
                
                UserLoginCertificateRoleFilterCriteria criteria = new UserLoginCertificateRoleFilterCriteria();
                criteria.loginCertificateIdEqualTo = item.getId();
                repo.delete(criteria);
                
                // Now we need to add the roles requested by the user
                List<String> roles = item.getRoles();
                for (String role : roles) {
                    // Let us verify if the role exists, if it does, then we will map the role to the user login password entry
                    Role roleInSystem = loginDAO.findRoleByName(role);
                    if (roleInSystem != null) {
                        UserLoginCertificateRole userLoginCertificateRole = new UserLoginCertificateRole();
                        userLoginCertificateRole.setLoginCertificateId(item.getId());
                        userLoginCertificateRole.setRoleId(roleInSystem.getId());
                        repo.create(userLoginCertificateRole);
                    }
                }
                
            } else {
                log.error("UserLoginCertificate:Store - User login certificate will not be updated since it does not exist.");
                throw new RepositoryStoreConflictException(locator);
            }            
        } catch (WebApplicationException wex) {
            throw wex;
        } catch (Exception ex) {
            log.error("Error during user update.", ex);
            throw new RepositoryStoreException(ex, locator);
        }
        
    }

    /**
     * Creates a new user
     * @param item 
     */
    @Override
    public void create(UserLoginCertificate item) {
        log.debug("UserLoginCertificate:Create - Got request to create a new user keystore.");
        UserLoginCertificateLocator locator = new UserLoginCertificateLocator();
        locator.id = item.getId();  
        locator.userId = item.getUserId();
         try (LoginDAO loginDAO = MyJdbi.authz()) {
            UserLoginCertificate obj = loginDAO.findUserLoginCertificateByUserId(item.getUserId());
            if (obj == null) {
                obj = new UserLoginCertificate();
                obj.setId(item.getId());
                obj.setUserId(item.getUserId());
                obj.setCertificate(item.getCertificate());
                obj.setComment(item.getComment());
                obj.setEnabled(false);
                obj.setExpires(item.getX509Certificate().getNotAfter());
                obj.setSha1Hash(Sha1Digest.digestOf(item.getCertificate()).toByteArray());
                obj.setSha256Hash(Sha256Digest.digestOf(item.getCertificate()).toByteArray());
                obj.setStatus(Status.PENDING);
                loginDAO.insertUserLoginCertificate(obj.getId(), obj.getUserId(), obj.getCertificate(), obj.getSha1Hash(), obj.getSha256Hash(),
                        obj.getExpires(), obj.isEnabled(), obj.getStatus(), obj.getComment());
                log.debug("UserLoginCertificate:Create - Created the user login certificate for user with id {} successfully.", obj.getUserId());
            } else {
                log.error("UserLoginCertificate:Create - User login certificate for user with Id {} will not be created since a duplicate already exists.", obj.getUserId());
                throw new RepositoryCreateConflictException(locator);
            }            
        } catch (RepositoryException re) {
            throw re;
        } catch (Exception ex) {
            log.error("Error during user login certificate creation.", ex);
            throw new RepositoryCreateException(ex, locator);
        }
    }

    @Override
    @RequiresPermissions("user_login_certificates:delete")        
    public void delete(UserLoginCertificateLocator locator) {
        if( locator == null || locator.id == null ) { return; }
        log.debug("UserLoginCertificate:Delete - Got request to delete user login certificate with id {}.", locator.id.toString());        
        try (LoginDAO loginDAO = MyJdbi.authz()) {
            UserLoginCertificate obj = loginDAO.findUserLoginCertificateById(locator.id);
            if (obj != null ) {

                // First delete all the role mappings from the UserLoginCertificateRole table
                UserLoginCertificateRoleRepository repo = new UserLoginCertificateRoleRepository();
                UserLoginCertificateRoleFilterCriteria criteria = new UserLoginCertificateRoleFilterCriteria();
                criteria.loginCertificateIdEqualTo = locator.id;
                repo.delete(criteria);
                
                loginDAO.deleteUserLoginCertificateById(locator.id);
                log.debug("UserLoginCertificate:Delete - Deleted the user login certificate with id {} successfully.", locator.id);
            } else {
                log.info("UserLoginCertificate:Delete - User login certificate does not exist in the system.");
            }
        } catch (Exception ex) {
            log.error("Error during user deletion.", ex);
            throw new RepositoryDeleteException(ex, locator);
        }
    }
    
    @Override
    @RequiresPermissions("user_login_certificates:delete,search")        
    public void delete(UserLoginCertificateFilterCriteria criteria) {
        log.debug("UserLoginCertificate:Delete - Got request to delete login certificates by search criteria.");        
        try {
            UserLoginCertificateCollection objList = search(criteria);
            for (UserLoginCertificate obj : objList.getUserLoginCertificates()) {
                UserLoginCertificateLocator locator = new UserLoginCertificateLocator();
                locator.id = obj.getId();
                delete(locator);
            }
        } catch(RepositoryException re) {
            throw re;
        } catch (Exception ex) {
            log.error("Error during User login certificate deletion.", ex);
            throw new RepositoryDeleteException(ex);
        }
    }
    
    /**
     * Helper function to retrieve the roles associated with user login password. This would be called by
     * retrieve and search methods.
     * @param id
     * @return 
     */
    private List<String> getAssociateRolesForLoginCertificateId(UUID id) {
        List<String> associatedRoles = new ArrayList<>();

        UserLoginCertificateRoleRepository repo = new UserLoginCertificateRoleRepository();
        RoleRepository roleRepo = new RoleRepository();

        UserLoginCertificateRoleFilterCriteria criteria = new UserLoginCertificateRoleFilterCriteria();
        criteria.loginCertificateIdEqualTo = id;
        UserLoginCertificateRoleCollection roles = repo.search(criteria);
        if (roles != null && roles.getUserLoginCertificateRoles().size() > 0) {
            for (UserLoginCertificateRole role : roles.getUserLoginCertificateRoles()) {
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
