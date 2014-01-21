/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.resource;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.rest.v2.model.UserCertificate;
import com.intel.mtwilson.as.rest.v2.model.UserCertificateCollection;
import com.intel.mtwilson.as.rest.v2.model.UserCertificateFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.UserCertificateLinks;
import com.intel.mtwilson.jersey.resource.AbstractResource;
import com.intel.mtwilson.ms.controller.ApiClientX509JpaController;
import com.intel.mtwilson.ms.controller.exceptions.IllegalOrphanException;
import com.intel.mtwilson.ms.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.ms.data.ApiClientX509;
import com.intel.mtwilson.ms.data.ApiRoleX509;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ssbangal
 */
public class UserCertificates extends AbstractResource<UserCertificate, UserCertificateCollection, UserCertificateFilterCriteria, UserCertificateLinks> {

    @Override
    protected UserCertificateCollection search(UserCertificateFilterCriteria criteria) {
        UserCertificateCollection userCertCollection = null;
        try {
            ApiClientX509JpaController userCertJpaController = My.jpa().mwApiClientX509();
            if (criteria.id != null) {
                UserCertificate userCert = convert(userCertJpaController.findApiClientX509ByUUID(criteria.id.toString()));            
                userCertCollection.getUserCertificates().add(userCert);
            } else if (criteria.nameEqualTo != null && !criteria.nameEqualTo.isEmpty()) {
                List<ApiClientX509> userList = userCertJpaController.findApiClientX509ByName(criteria.nameEqualTo);
                if (userList != null && !userList.isEmpty()) {
                    for(ApiClientX509 userObj : userList) {
                        userCertCollection.getUserCertificates().add(convert(userObj));
                    }
                }                
            } else if (criteria.nameContains != null && !criteria.nameContains.isEmpty()) {
                List<ApiClientX509> userList = userCertJpaController.findApiClientX509ByNameLike(criteria.nameContains);
                if (userList != null && !userList.isEmpty()) {
                    for(ApiClientX509 userObj : userList) {
                        userCertCollection.getUserCertificates().add(convert(userObj));
                    }
                }                
            } else if (criteria.fingerprint != null) {
                UserCertificate userCert = convert(userCertJpaController.findApiClientX509ByFingerprint(criteria.fingerprint));
                userCertCollection.getUserCertificates().add(userCert);                
            } else if (criteria.expiresAfter != null) {
                List<ApiClientX509> userList = userCertJpaController.findApiClientX509ByExpiresAfter(criteria.expiresAfter);
                if (userList != null && !userList.isEmpty()) {
                    for(ApiClientX509 userObj : userList) {
                        userCertCollection.getUserCertificates().add(convert(userObj));
                    }
                }                
            } else if (criteria.expiresBefore != null) {
                List<ApiClientX509> userList = userCertJpaController.findApiClientX509ByExpiresBefore(criteria.expiresBefore);
                if (userList != null && !userList.isEmpty()) {
                    for(ApiClientX509 userObj : userList) {
                        userCertCollection.getUserCertificates().add(convert(userObj));
                    }
                }                
            } else if (criteria.enabled != null) {
                List<ApiClientX509> userList = userCertJpaController.findApiClientX509ByEnabled(criteria.enabled);
                if (userList != null && !userList.isEmpty()) {
                    for(ApiClientX509 userObj : userList) {
                        userCertCollection.getUserCertificates().add(convert(userObj));
                    }
                }                
            } else if (criteria.status != null) {
                List<ApiClientX509> userList = userCertJpaController.findApiClientX509ByStatus(criteria.status);
                if (userList != null && !userList.isEmpty()) {
                    for(ApiClientX509 userObj : userList) {
                        userCertCollection.getUserCertificates().add(convert(userObj));
                    }
                }                
            }
        } catch (IOException ex) {
            Logger.getLogger(UserCertificates.class.getName()).log(Level.SEVERE, null, ex);
        }
        return userCertCollection;
    }

    @Override
    protected UserCertificate retrieve(String id) {
        UserCertificate userCert = null;
        try {
            ApiClientX509JpaController userCertJpaController = My.jpa().mwApiClientX509();
            if (id != null) {
                ApiClientX509 user = userCertJpaController.findApiClientX509ByUUID(id);            
                if (user != null)
                    userCert = convert(user);
            }
        } catch (IOException ex) {
            Logger.getLogger(UserCertificates.class.getName()).log(Level.SEVERE, null, ex);
        }
        return userCert;
    }

    @Override
    protected void store(UserCertificate item) {
        // Need to see how we can call into the Business object directly
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void create(UserCertificate item) {
        // Need to see how we can call into the Business object directly
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void delete(String id) {
        try {
            ApiClientX509JpaController userCertJpaController = My.jpa().mwApiClientX509();
            if (id != null) {
                ApiClientX509 user = userCertJpaController.findApiClientX509ByUUID(id);            
                if (user != null)
                    userCertJpaController.destroy(user.getId());
            }
        } catch (IOException ex) {
            Logger.getLogger(UserCertificates.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalOrphanException ex) {
            Logger.getLogger(UserCertificates.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NonexistentEntityException ex) {
            Logger.getLogger(UserCertificates.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected UserCertificateFilterCriteria createFilterCriteriaWithId(String id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private UserCertificate convert(ApiClientX509 apiObj) {
        UserCertificate userCert = new UserCertificate();
        if (apiObj != null) {
            userCert.setId(UUID.valueOf(apiObj.getUuid_hex()));
            userCert.setName(apiObj.getName());
            userCert.setEnabled(apiObj.getEnabled());
            userCert.setCertificate(apiObj.getCertificate());
            userCert.setComment(apiObj.getComment());
            userCert.setExpires(apiObj.getExpires());
            userCert.setFingerprint(apiObj.getFingerprint());
            userCert.setIssuer(apiObj.getIssuer());
            userCert.setSerialNumber(apiObj.getSerialNumber());
            userCert.setStatus(apiObj.getStatus());
            Collection<ApiRoleX509> apiRoleX509Collection = apiObj.getApiRoleX509Collection();
            userCert.setRoles(apiRoleX509Collection.toArray(new String[apiRoleX509Collection.size()]));
        } else {
            apiObj = null;
        }
        return userCert;
    }
}
