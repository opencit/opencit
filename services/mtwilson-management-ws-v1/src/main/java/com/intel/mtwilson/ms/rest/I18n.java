/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.ms.rest;

import com.intel.mtwilson.My;
import com.intel.mtwilson.datatypes.PortalUserLocale;
import com.intel.mtwilson.launcher.ws.ext.V1;
import com.intel.mtwilson.ms.controller.exceptions.MSDataException;
import com.intel.mtwilson.ms.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.shiro.ShiroUtil;
import com.intel.mtwilson.shiro.jdbi.LoginDAO;
import com.intel.mtwilson.shiro.jdbi.MyJdbi;
import com.intel.mtwilson.user.management.rest.v2.model.User;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * REST Web Service
 *
 * @author rksavinx
 */
@V1
@Path("/ManagementService/resources/i18n")
public class I18n {

    private Logger log = LoggerFactory.getLogger(getClass());

    public I18n() {
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
    public String getLocaleForUser(
            @QueryParam("username") String username) throws IOException, SQLException {
        
        if( username == null || ShiroUtil.subjectUsernameEquals(username)) {
            // allow any user to access own data 
            return getLocaleForCurrentUser(); // notice we don't pass in any argument, it will use the current logged in user
        }
        else {
            // permission is required to access any other user's data
            return getLocaleForAnyUser(username);
        }
    }
    
    // does not require any permission - anyone is allowed to get their own locale
    protected String getLocaleForCurrentUser() throws IOException, SQLException {
        String username = ShiroUtil.subjectUsername();
//        MwPortalUserJpaController mwPortalUserJpaController = My.jpa().mwPortalUser(); //new MwPortalUserJpaController(getMSEntityManagerFactory());
//        MwPortalUser portalUser = mwPortalUserJpaController.findMwPortalUserByUserName(username);
//        if( portalUser == null ) { throw new WebApplicationException(Response.Status.NOT_FOUND); }
        try(LoginDAO loginDAO = MyJdbi.authz()) {
        User user = loginDAO.findUserByName(username);
        if( user == null ) { throw new WebApplicationException(Response.Status.NOT_FOUND); }
        log.debug("Retrieved locale for current user {}: {}", user.getUsername(), user.getLocale());
        if(user.getLocale() != null) {
            return user.getLocale().toLanguageTag();
        } else {
            return My.configuration().getAvailableLocales()[0];// "en-US"; // getAvailableLocales() guarantees to return at least one element.
        }
        }
    }
    
    @RequiresPermissions("users:retrieve")
    protected String getLocaleForAnyUser(String username) throws IOException, SQLException {
        log.debug("Retrieving information from database for portal user: {}", username);
//        MwPortalUserJpaController mwPortalUserJpaController = My.jpa().mwPortalUser(); //new MwPortalUserJpaController(getMSEntityManagerFactory());
//        MwPortalUser portalUser = mwPortalUserJpaController.findMwPortalUserByUserName(username);
//        if( portalUser == null ) { throw new WebApplicationException(Response.Status.NOT_FOUND); }
        try(LoginDAO loginDAO = MyJdbi.authz()) {
        User user = loginDAO.findUserByName(username);
        if( user == null ) { throw new WebApplicationException(Response.Status.NOT_FOUND); }
        log.debug("Retrieved locale for current user {}: {}", user.getUsername(), user.getLocale());
        if(user.getLocale() != null) {
            return user.getLocale().toLanguageTag();
        } else {
            return My.configuration().getAvailableLocales()[0];// "en-US"; // getAvailableLocales() guarantees to return at least one element.
        }
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
    public String setLocaleForUser(PortalUserLocale pul) throws IOException, NonexistentEntityException, MSDataException, SQLException {
        if( pul.getUser() == null || ShiroUtil.subjectUsernameEquals(pul.getUser())) {
            setLocaleForCurrentUser(pul.getLocale());
        }
        else {
            setLocaleForAnyUser(pul.getUser(), pul.getLocale());
        }
        
        return "OK";
    }
    
    protected void setLocaleForCurrentUser(String locale) throws IOException, NonexistentEntityException, MSDataException, SQLException {
        String username = ShiroUtil.subjectUsername();
        log.debug("Retrieving current portal user [{}] from database.", username);
        //MwPortalUserJpaController mwPortalUserJpaController = My.jpa().mwPortalUser(); //new MwPortalUserJpaController(getMSEntityManagerFactory());
        //MwPortalUser portalUser = mwPortalUserJpaController.findMwPortalUserByUserName(username);
        //if( portalUser == null ) { throw new WebApplicationException(Response.Status.NOT_FOUND); }
        try(LoginDAO loginDAO = MyJdbi.authz()) {
        User user = loginDAO.findUserByName(username);
        if( user == null ) { throw new WebApplicationException(Response.Status.NOT_FOUND); }
        log.debug("Retrieved current user {}", user.getUsername());
        loginDAO.updateUser(user.getId(), locale, user.getComment());
        }

        //log.debug("Retrieved portal user [{}] from database.", portalUser.getUsername());
        //log.debug("Setting locale [{}] for portal user [{}] in database.", locale, portalUser.getUsername());
        //portalUser.setLocale(locale);
        //mwPortalUserJpaController.edit(portalUser);
    } 
    
    @RequiresPermissions("users:store")
    protected void setLocaleForAnyUser(String username, String locale) throws IOException, NonexistentEntityException, MSDataException, SQLException {
        log.debug("Retrieving portal user [{}] from database.", username);
        //MwPortalUserJpaController mwPortalUserJpaController = My.jpa().mwPortalUser(); //new MwPortalUserJpaController(getMSEntityManagerFactory());
        //MwPortalUser portalUser = mwPortalUserJpaController.findMwPortalUserByUserName(username);
        //if( portalUser == null ) { throw new WebApplicationException(Response.Status.NOT_FOUND); }
        //log.debug("Retrieved portal user [{}] from database.", portalUser.getUsername());
        //log.debug("Setting locale [{}] for portal user [{}] in database.", locale, portalUser.getUsername());
        //portalUser.setLocale(locale);
        //mwPortalUserJpaController.edit(portalUser);
        try(LoginDAO loginDAO = MyJdbi.authz()) {
        User user = loginDAO.findUserByName(username);
        if( user == null ) { throw new WebApplicationException(Response.Status.NOT_FOUND); }
        log.debug("Retrieved current user {}", user.getUsername());
        loginDAO.updateUser(user.getId(), locale, user.getComment());
        }
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
