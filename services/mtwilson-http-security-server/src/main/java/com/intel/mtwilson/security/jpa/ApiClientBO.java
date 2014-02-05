package com.intel.mtwilson.security.jpa;

import com.intel.mtwilson.ms.controller.TblApiClientJpaController;
import com.intel.mtwilson.ms.data.TblApiClient;
import com.intel.mtwilson.security.core.SecretKeyFinder;
import javax.persistence.EntityManagerFactory;

/**
 * XXX This class has a compile-time dependency on ManagementService because
 * that application defines the JPA configuration for accessing the security
 * database. In maven this is annotated as "provided" because when this
 * project is deployed it will be as a JAR component of a web service that
 * also depends on the ManagementService data layer.
 * 
 * It was labeled business logic but it's very closely tied to the JPA layer.
 * 
 * TODO: Separate JPA from Business Objects by formalizing the required
 * data interfaces and creating JPA objects that implement those interfaces.
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
