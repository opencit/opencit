/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.ms.rest;

import com.intel.mtwilson.My;
import com.intel.mtwilson.launcher.ws.ext.V1;
import com.intel.mtwilson.ms.controller.MwPortalUserJpaController;
import com.intel.mtwilson.ms.controller.exceptions.MSDataException;
import com.intel.mtwilson.ms.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.ms.data.MwPortalUser;
import com.intel.mtwilson.security.annotations.RolesAllowed;
import java.io.IOException;
import java.util.Arrays;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
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
    @RolesAllowed({"Security"})
    public String getLocaleForUser(
            @QueryParam("username") String username) throws IOException {
        MwPortalUserJpaController mwPortalUserJpaController = My.jpa().mwPortalUser(); //new MwPortalUserJpaController(getMSEntityManagerFactory());
        MwPortalUser portalUser = mwPortalUserJpaController.findMwPortalUserByUserName(username);
        log.debug("Retrieving locale for portal user: {}", portalUser.getUsername());
            if(portalUser != null) {
                log.debug("Locale for {}: {}", portalUser.getUsername(), portalUser.getLocale());
                return portalUser.getLocale();
            } else {
                log.debug("Portal user not found.");
                return "Portal user not found.";
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
    @Consumes("application/json")
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/locale")
    @RolesAllowed({"Security"})
    public String setLocaleForUser(
            //@QueryParam("api") Boolean api,
            @QueryParam("username") String username,
            @QueryParam("locale") String locale) throws IOException, NonexistentEntityException, MSDataException {
        //ValidationUtil.validate(apiClientRequest);
        MwPortalUserJpaController mwPortalUserJpaController = My.jpa().mwPortalUser(); //new MwPortalUserJpaController(getMSEntityManagerFactory());
        MwPortalUser portalUser = mwPortalUserJpaController.findMwPortalUserByUserName(username);
        if (portalUser != null) {
            portalUser.setLocale(locale);
            mwPortalUserJpaController.edit(portalUser);
        } else { return "Portal user not found."; }
        
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
    @RolesAllowed({"Security"})
    public String getLocales() throws IOException {
        log.debug("Retrieving available locales: {}", Arrays.toString(My.configuration().getAvailableLocales()));
        return Arrays.toString(My.configuration().getAvailableLocales());
    }
}
