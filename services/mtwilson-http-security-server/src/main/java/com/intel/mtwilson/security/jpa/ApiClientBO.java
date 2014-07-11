package com.intel.mtwilson.security.jpa;

import com.intel.mtwilson.ms.controller.TblApiClientJpaController;
import com.intel.mtwilson.ms.data.TblApiClient;
import com.intel.mtwilson.security.core.SecretKeyFinder;
import javax.persistence.EntityManagerFactory;

/**
 * It was labeled business logic but it's very closely tied to the JPA layer.
 * 
 * Secret key lookup provider for the authentication filter that secures
 * the REST API.
 * 
 * @since 0.5.1
 * @author jbuhacoff
 */
public class ApiClientBO implements SecretKeyFinder {
    private TblApiClientJpaController controller;
    
    public ApiClientBO(EntityManagerFactory factory) {
        controller = new TblApiClientJpaController(factory);
    }

    @Override
    public String getSecretKeyForUserId(String userId) {
        TblApiClient user = controller.findTblApiClientByClientId(userId);
        return user.getSecretKey();
    }

}
