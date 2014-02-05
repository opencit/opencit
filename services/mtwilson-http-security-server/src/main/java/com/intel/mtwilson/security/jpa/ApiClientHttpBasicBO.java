/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.security.jpa;

import com.intel.mtwilson.as.controller.MwApiClientHttpBasicJpaController;
import com.intel.mtwilson.as.data.MwApiClientHttpBasic;
import com.intel.mtwilson.security.core.HttpBasicUserFinder;
import javax.persistence.EntityManagerFactory;


/**
 *
 * @author ssbangal
 */
public class ApiClientHttpBasicBO implements HttpBasicUserFinder{
    private MwApiClientHttpBasicJpaController controller;

    public ApiClientHttpBasicBO(EntityManagerFactory factory) {
        controller = new MwApiClientHttpBasicJpaController(factory);
    }

    @Override
    public String getPasswordForUser(String userName) {
        MwApiClientHttpBasic user = controller.findByUserName(userName);
        return user.getPassword();
    }
    
}
