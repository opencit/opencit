/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.ms.rest;

import com.intel.mtwilson.crypto.Password;
import java.io.File;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.mtwilson.datatypes.ErrorCode;
import com.intel.mtwilson.ms.business.CertificateAuthorityBO;
import com.intel.mtwilson.ms.common.MSConfig;
import com.intel.mtwilson.ms.common.MSException;
import com.intel.mtwilson.security.annotations.PermitAll;
import com.intel.mtwilson.util.CertUtils;
import com.intel.mtwilson.util.ResourceFinder;
import javax.ws.rs.POST;

/**
 * REST Web Service
 *
 * @author dsmagadX
 */
@Path("/ca")
public class CA {
	
    private Logger log = LoggerFactory.getLogger(getClass());
    private CertificateAuthorityBO dao = new CertificateAuthorityBO();


    public CA() {
    }

    @POST @Path("/enable")
    @Consumes("application/json")
    @Produces({MediaType.TEXT_PLAIN})
    public String enableCa(String newSaltedPasswordString) {
        try {
            Password newPassword = Password.valueOf(newSaltedPasswordString);
            dao.enableCaWithPassword(newPassword);
            return Boolean.TRUE.toString();
        } catch (Exception e) {
            throw new MSException(ErrorCode.SYSTEM_ERROR, ErrorCode.SYSTEM_ERROR.getMessage(), e);
        }
        
    }

    @POST @Path("/disable")
    @Produces({MediaType.TEXT_PLAIN})
    public String disableCa() {
        try {
            dao.disableCa();
            return Boolean.TRUE.toString();
        } catch (Exception e) {
            throw new MSException(ErrorCode.SYSTEM_ERROR, ErrorCode.SYSTEM_ERROR.getMessage(), e);
        }
        
    }
    
}
