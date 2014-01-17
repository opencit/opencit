/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.resource;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.as.rest.v2.model.User;
import com.intel.mtwilson.as.rest.v2.model.UserCollection;
import com.intel.mtwilson.as.rest.v2.model.UserFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.UserLinks;
import com.intel.mtwilson.jersey.resource.AbstractResource;
import com.intel.mtwilson.ms.controller.MwPortalUserJpaController;
import com.intel.mtwilson.My;
import com.intel.mtwilson.ms.controller.exceptions.MSDataException;
import com.intel.mtwilson.ms.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.ms.data.MwPortalUser;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.ws.rs.Path;

/**
 *
 * @author ssbangal
 */

@Stateless
@Path("/v2/users")
public class Users extends AbstractResource<User, UserCollection, UserFilterCriteria, UserLinks> {

    //TODO : Handling of the exceptions should be changed
    
    @Override
    protected UserCollection search(UserFilterCriteria criteria) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected User retrieve(String id) {
        User user = null;
        try {
            MwPortalUserJpaController userJpaController = My.jpa().mwPortalUser();         
            MwPortalUser portalUser = userJpaController.findMwPortalUserByUUID(id);
            if (portalUser != null) {
                 user = convert(portalUser);
            }
        } catch (IOException ex) {
            Logger.getLogger(Users.class.getName()).log(Level.SEVERE, null, ex);
        }
        return user;
    }

    /**
     * Used for updating the existing user
     * 
     * @param item 
     */
    @Override
    protected void store(User item) {
        try {
            MwPortalUserJpaController userJpaController = My.jpa().mwPortalUser();
            MwPortalUser portalUser = new MwPortalUser();
            portalUser.setUsername(item.getName());
            portalUser.setStatus(item.getStatus()); 
            portalUser.setEnabled(item.getEnabled()); 
            portalUser.setKeystore(item.getKeystore());
            portalUser.setLocale(item.getLocale());
            userJpaController.edit(portalUser);
        } catch (IOException ex) {
            Logger.getLogger(Users.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NonexistentEntityException ex) {
            Logger.getLogger(Users.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MSDataException ex) {
            Logger.getLogger(Users.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    /**
     * Creates a new user
     * @param item 
     */
    @Override
    protected void create(User item) {
        try {
            MwPortalUserJpaController userJpaController = My.jpa().mwPortalUser();
            MwPortalUser portalUser = new MwPortalUser();
            portalUser.setUsername(item.getName());
            // TODO: Do we want to create an enum for this ??
            portalUser.setStatus("PENDING"); 
            portalUser.setEnabled(Boolean.FALSE); 
            portalUser.setKeystore(item.getKeystore());
            portalUser.setLocale(item.getLocale());
            userJpaController.create(portalUser);
        } catch (IOException ex) {
            Logger.getLogger(Users.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void delete(String id) {
        try {
            MwPortalUserJpaController userJpaController = My.jpa().mwPortalUser();         
            MwPortalUser portalUser = userJpaController.findMwPortalUserByUUID(id);
            if (portalUser != null) {
                userJpaController.destroy(portalUser.getId());
            }
        } catch (IOException ex) {
            Logger.getLogger(Users.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NonexistentEntityException ex) {
            Logger.getLogger(Users.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }

    @Override
    protected UserFilterCriteria createFilterCriteriaWithId(String id) {
        UserFilterCriteria criteria = new UserFilterCriteria();
        criteria.id = UUID.valueOf(id);
        return criteria;
    }
    
    private User convert(MwPortalUser portalUser) {
        User user = new User();
        if (portalUser != null) {
            user.setId(UUID.valueOf(portalUser.getUuid_hex()));
            user.setName(portalUser.getUsername());
            user.setStatus(portalUser.getStatus());
            user.setEnabled(portalUser.getEnabled());
            user.setLocale(portalUser.getLocale());
            user.setComments(portalUser.getComment());
        } else {
            user = null;
        }            
        return user;
    }
}
