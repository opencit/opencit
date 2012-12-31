/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.ms.rest;

import com.intel.mtwilson.as.data.MwCertificateX509;
import com.intel.mtwilson.crypto.Password;
import com.intel.mtwilson.datatypes.ErrorCode;
import com.intel.mtwilson.ms.business.CertificateAuthorityBO;
import com.intel.mtwilson.ms.common.MSException;
import com.intel.mtwilson.security.annotations.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    @RolesAllowed({"Security"}) // XXX TODO maybe need a separate "CA" role
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
    @RolesAllowed({"Security"}) // XXX TODO maybe need a separate "CA" role
    @Produces({MediaType.TEXT_PLAIN})
    public String disableCa() {
        try {
            dao.disableCa();
            return Boolean.TRUE.toString();
        } catch (Exception e) {
            throw new MSException(ErrorCode.SYSTEM_ERROR, ErrorCode.SYSTEM_ERROR.getMessage(), e);
        }
        
    }

    @GET @Path("/certificate")
    @RolesAllowed({"Security"}) // XXX TODO maybe need a separate "CA" role
    @Produces({MediaType.APPLICATION_OCTET_STREAM})
    public byte[] getCaCertificate() {
        try {
            MwCertificateX509 cacert = dao.getCaCertificate();
            if( cacert == null ) {
                throw new MSException(ErrorCode.MS_MISSING_CERTIFICATE_FILE, ErrorCode.MS_MISSING_CERTIFICATE_FILE.getMessage());
            }
            return cacert.getCertificate();
        } catch (Exception e) {
            throw new MSException(ErrorCode.SYSTEM_ERROR, ErrorCode.SYSTEM_ERROR.getMessage(), e);
        }
        
    }
    
}
