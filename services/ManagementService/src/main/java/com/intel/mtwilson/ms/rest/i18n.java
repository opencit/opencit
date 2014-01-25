/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.ms.rest;

import com.intel.mtwilson.My;
import com.intel.mtwilson.security.annotations.RolesAllowed;
import java.io.IOException;
import javax.ejb.Stateless;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST Web Service
 *
 * @author rksavinx
 */
@Stateless
@Path("/i18n")
public class i18n {

    private Logger log = LoggerFactory.getLogger(getClass());

    public i18n() {
    }

    /**
     * Retrieves list of available locales
     *
     * @return
     * @throws IOException
     */
    @GET
    @Produces("application/json")
    @Path("/locales")
    @RolesAllowed({"Security"})
    public String[] getLocales() throws IOException {
        return My.configuration().getAvailableLocales();
    }
//    @POST
//    @Produces(MediaType.TEXT_PLAIN)
//    @Consumes("application/json")
//    @Path("/locales")
//    @RolesAllowed({"Security"})
//    public String setLocaleForUser(ApiClientCreateRequest apiClientRequest) {
//        ValidationUtil.validate(apiClientRequest);
//        new ApiClientBO().create(apiClientRequest);
//        return "OK";
//    }
}
