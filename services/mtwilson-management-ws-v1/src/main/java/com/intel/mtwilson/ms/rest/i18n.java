/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.ms.rest;

import com.intel.dcsg.cpg.i18n.LocaleUtil;
import com.intel.mtwilson.My;
import com.intel.mtwilson.datatypes.PortalUserLocale;
import com.intel.mtwilson.launcher.ws.ext.V1;
import com.intel.mtwilson.ms.controller.MwPortalUserJpaController;
import com.intel.mtwilson.ms.controller.exceptions.MSDataException;
import com.intel.mtwilson.ms.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.ms.data.MwPortalUser;
import com.intel.mtwilson.security.annotations.RolesAllowed;
import com.intel.mtwilson.shiro.jdbi.LoginDAO;
import com.intel.mtwilson.shiro.jdbi.MyJdbi;
import com.intel.mtwilson.user.management.rest.v2.model.User;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Locale;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * REST Web Service
 *
 * @author rksavinx
 */
@V1
//@Stateless
@Path("/ManagementService/resources/i18n")
public class i18n {

    private Logger log = LoggerFactory.getLogger(getClass());

    public i18n() {
    }

    /**
     * Returns locale for specified user.
     * 
     * @param username
     * @return
     * @throws IOException 
     */
    @GET
    @Consumes("application/json")
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/locale")
    //@RolesAllowed({"Security"})
    @RequiresPermissions("users:retrieve")
    public String getLocaleForUser(
            @QueryParam("username") String username) throws IOException, SQLException {
        log.debug("Retrieving information from database for user: {}", username);
        LoginDAO loginDAO = MyJdbi.authz();
        User user = loginDAO.findUserByName(username);
        log.debug("Retrieving locale for user: {}", user.getUsername());
        log.debug("Locale for {}: {}", user.getUsername(), user.getLocale());
        if (user.getLocale() != null) {
            return user.getLocale().toString();
        } else {
            return "en-US";
        }
    }
    
    /**
     * Sets the user defined locale.
     * 
     * @param username
     * @param locale
     * @return
     * @throws IOException
     * @throws NonexistentEntityException
     * @throws MSDataException 
     */
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/locale")
    //@RolesAllowed({"Security"})
    @RequiresPermissions("users:store")
    public String setLocaleForUser(PortalUserLocale pul) throws IOException, NonexistentEntityException, MSDataException, SQLException {
        log.debug("Retrieving user [{}] from database.", pul.getUser());
        LoginDAO loginDAO = MyJdbi.authz();
        User user = loginDAO.findUserByName(pul.getUser());
        log.debug("Retrieved user [{}] from database.", user.getUsername());
        log.debug("Setting locale [{}] for user [{}] in database.", pul.getLocale(), user.getUsername());
        loginDAO.updateUser(user.getId(), pul.getLocale(), user.getComment());
        
        log.debug("Retrieving portal user [{}] from database.", pul.getUser());
        MwPortalUserJpaController mwPortalUserJpaController = My.jpa().mwPortalUser(); //new MwPortalUserJpaController(getMSEntityManagerFactory());
        MwPortalUser portalUser = mwPortalUserJpaController.findMwPortalUserByUserName(pul.getUser());
        log.debug("Retrieved portal user [{}] from database.", portalUser.getUsername());
        log.debug("Setting locale [{}] for portal user [{}] in database.", pul.getLocale(), portalUser.getUsername());
        portalUser.setLocale(pul.getLocale());
        mwPortalUserJpaController.edit(portalUser);
        
        return "OK";
    }
    
    /**
     * Retrieves list of available locales
     *
     * @return
     * @throws IOException
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/locales")
    //@RolesAllowed({"Security"})
    //@RequiresPermissions("users:retrieve")
    public String getLocales() throws IOException {
        log.debug("Retrieving available locales: {}", Arrays.toString(My.configuration().getAvailableLocales()));
        return Arrays.toString(My.configuration().getAvailableLocales());
    }
}
