/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.model;

import com.intel.mtwilson.repository.FilterCriteria;
import com.intel.mtwilson.jaxrs2.DefaultFilterCriteria;
import javax.ws.rs.QueryParam;

/**
 *
 * @author ssbangal
 */
public class CaCertificateFilterCriteria extends DefaultFilterCriteria implements FilterCriteria<CaCertificate>{
    
    /**
     * Domain over which the issuer has authority.
     * Possible values are "tls", "aik", "ek", or "saml".
     * 
     */
    @QueryParam("domain")
    public String domain;
}
