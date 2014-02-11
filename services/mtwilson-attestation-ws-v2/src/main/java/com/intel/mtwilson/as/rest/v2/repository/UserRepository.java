/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.repository;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.as.rest.v2.model.User;
import com.intel.mtwilson.as.rest.v2.model.UserCollection;
import com.intel.mtwilson.as.rest.v2.model.UserFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.UserLocator;
import com.intel.mtwilson.ms.controller.MwPortalUserJpaController;
import com.intel.mtwilson.My;
import com.intel.mtwilson.datatypes.ApiClientStatus;
import com.intel.mtwilson.datatypes.ErrorCode;
import com.intel.mtwilson.jersey.resource.SimpleRepository;
import com.intel.mtwilson.ms.data.MwPortalUser;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
public class UserRepository implements SimpleRepository<User, UserCollection, UserFilterCriteria, UserLocator> {

    private Logger log = LoggerFactory.getLogger(getClass().getName());
    
    @Override
    public UserCollection search(UserFilterCriteria criteria) {
        UserCollection userCollection = new UserCollection();
        try {
            MwPortalUserJpaController userJpaController = My.jpa().mwPortalUser();
            if (criteria.id != null) {
                MwPortalUser userObj = userJpaController.findMwPortalUserByUUID(criteria.id.toString());
                if (userObj != null) {
                    userCollection.getUsers().add(convert(userObj));
                }
            } else if (criteria.nameEqualTo != null && !criteria.nameEqualTo.isEmpty()) {
                MwPortalUser userObj = userJpaController.findMwPortalUserByUserName(criteria.nameContains);
                if (userObj != null) {
                    userCollection.getUsers().add(convert(userObj));
                }
            } else if (criteria.nameContains != null && !criteria.nameContains.isEmpty()) {
                List<MwPortalUser> userList = userJpaController.findMwPortalUsersMatchingName(criteria.nameContains);
                if (userList != null && !userList.isEmpty()) {
                    for(MwPortalUser userObj : userList) {
                        userCollection.getUsers().add(convert(userObj));
                    }
                }
            } else if (criteria.enabled != null) {
                List<MwPortalUser> userList = userJpaController.findMwPortalUsersWithEnabledStatus(criteria.enabled);
                if (userList != null && !userList.isEmpty()) {
                    for(MwPortalUser userObj : userList) {
                        userCollection.getUsers().add(convert(userObj));
                    }
                }
            }
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during user search.", ex);
            throw new ASException(ErrorCode.MS_API_USER_SEARCH_ERROR, ex.getClass().getSimpleName());
        }
        return userCollection;
    }

    @Override
    public User retrieve(UserLocator locator) {
        if( locator == null || locator.id == null ) { return null; }
        String id = locator.id.toString();
        try {
            MwPortalUserJpaController userJpaController = My.jpa().mwPortalUser();         
            MwPortalUser portalUser = userJpaController.findMwPortalUserByUUID(id);
            if (portalUser != null) {
                 User user = convert(portalUser);
                 return user;
            }
        } catch (ASException aex) {
            throw aex;            
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
    public void store(User item) {
        try {
            MwPortalUserJpaController userJpaController = My.jpa().mwPortalUser();
            MwPortalUser portalUser = userJpaController.findMwPortalUserByUUID(item.getId().toString());
            if (portalUser == null) {
                throw new ASException(ErrorCode.MS_USER_DOES_NOT_EXISTS, item.getId().toString());
            }
            
            if (item.getStatus() != null)
                portalUser.setStatus(item.getStatus()); 
            if (item.getEnabled() != null)
                portalUser.setEnabled(item.getEnabled()); 
            if (item.getKeystore() != null)
                portalUser.setKeystore(item.getKeystore());
            if (item.getLocale() != null)
                portalUser.setLocale(item.getLocale());
            userJpaController.edit(portalUser);

        } catch (ASException aex) {
            throw aex;            
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
    public void create(User item) {
        try {
            MwPortalUserJpaController userJpaController = My.jpa().mwPortalUser();
            MwPortalUser portalUser = userJpaController.findMwPortalUserByUUID(item.getId().toString());
            if (portalUser == null) {
                throw new ASException(ErrorCode.MS_USER_ALREADY_EXISTS, item.getId().toString());
            }
            
            portalUser.setUsername(item.getName());
            portalUser.setStatus(ApiClientStatus.PENDING.toString()); 
            portalUser.setEnabled(Boolean.FALSE); 
            portalUser.setKeystore(item.getKeystore());
            portalUser.setLocale(item.getLocale());
            portalUser.setUuid_hex(new UUID().toHexString());
            userJpaController.create(portalUser);
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during user creation.", ex);
            throw new ASException(ErrorCode.MS_API_USER_REGISTRATION_ERROR, ex.getClass().getSimpleName());
        }
    }

    @Override
    public void delete(UserLocator locator) {
        if( locator == null || locator.id == null ) { return; }
        String id = locator.id.toString();        
        try {
            MwPortalUserJpaController userJpaController = My.jpa().mwPortalUser();         
            MwPortalUser portalUser = userJpaController.findMwPortalUserByUUID(id);
            if (portalUser != null) {
                userJpaController.destroy(portalUser.getId());
            }
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during user deletion.", ex);
            throw new ASException(ErrorCode.MS_API_USER_DELETION_ERROR, ex.getClass().getSimpleName());
        }
    }
    
    @Override
    public void delete(UserFilterCriteria criteria) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private User convert(MwPortalUser portalUser) {
        User user = new User();
        user.setId(UUID.valueOf(portalUser.getUuid_hex()));
        user.setName(portalUser.getUsername());
        user.setStatus(portalUser.getStatus());
        user.setEnabled(portalUser.getEnabled());
        user.setLocale(portalUser.getLocale());
        user.setComments(portalUser.getComment());
        return user;
    }

}
