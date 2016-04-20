/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.attestation.client.jaxrs;

import com.intel.mtwilson.jaxrs2.client.MtWilsonClient;
import com.intel.mtwilson.jaxrs2.mediatype.CryptoMediaType;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.dcsg.cpg.tls.policy.TlsConnection;
import com.intel.mtwilson.as.rest.v2.model.CaCertificateFilterCriteria;

/**
 *
 * @author ssbangal
 */
public class CaCertificates extends MtWilsonClient {
    
    Logger log = LoggerFactory.getLogger(getClass().getName());

    public CaCertificates(URL url) throws Exception{
        super(url);
    }

    public CaCertificates(Properties properties) throws Exception {
        super(properties);
    }
    
    public CaCertificates(Configuration configuration) throws Exception {
        super(configuration);
    }
    public CaCertificates(Properties properties, TlsConnection tlsConnection) throws Exception {
        super(properties, tlsConnection);
    }
        
    /**
     * Retrieves the details of the specified certificate from the system.
     * @param certificateId - Id of the certificate being requested. Possible options include "root", "saml", "tls", and "privacy".
     * @return X509Certificate of the requested type.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions None
     * @mtwContentTypeReturned application/x-pem-file
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/ca-certificates/root
     * Output: 
     * -----BEGIN CERTIFICATE-----
     * MIIC0zCCAbugAwIBAgIJAP8y0d2XNaa0MA0GCSqGSIb3DQEBCwUAMCkxETAPBgNVBAsTCG10d2ls
     * c29uMRQwEgYDVQQDEwttdHdpbHNvbi1jYTAeFw0xNDA0MjMwNDI0NTdaFw0xNTA0MjMwNDI0NTda
     * MCkxETAPBgNVBAsTCG10d2lsc29uMRQwEgYDVQQDEwttdHdpbHNvbi1jYTCCASIwDQYJKoZIhvcN
     * AQEBBQADggEPADCCAQoCggEBAL6r6DnRdQiuH8uHP/BboABxfwquWwzyX5OY5cjMxfR8RR4XhOi/
     * govUzcFzOotwv6YUM49QVK0c3C4Q5dVuE3EX8PaU7KzCik6DcuMzFdHe4hQzoINIvjDKmW1A3lwp
     * HKEnMTuYkbAnJToEg0G2ZhBX6Ye/kZvLaDpvBF84EJBDjxXKFksLWONyakRXOSLkfIshEvQF6kfz
     * JxCPwxDHAU94svm2Wcl7GLKScr/MUiZxJSIX7GWZSt2LLLq6hQvXXw3XeQCdExmwOipYtAj7JI4u
     * 7lO+bmpQX/UtIGePJCYAtogQ6KbZ+0EnJursdZH2sfJNPuPQ37JOsGf8G6Z+nyUCAwEAATANBgkq
     * hkiG9w0BAQsFAAOCAQEAZbzmOBilsCwCRMakJT//U6kAZLo0DFhBU5ITPz+wGXcO5FcAOMZL3qou
     * YbXL9H7KRMXHa6VcNOOkgoUjrjbOiZtzSWmyVZdjpyeT/9Lct7lLYY+MXMei9SMaiywtLCzAkHf4
     * Ewpl8zaMSjs9baE/18/1SAneyXz6jwrZBua5GJWTDwiZidk3l9MfgRpStYaKXpiian0MTrvp0Lcc
     * 2wzn8esuaBfEx0GGeJQyPDRV3fbpDON9sZRMLjS6pX99XeAdh+qJdjaW9CYsfi40k1vlZRK/Pt2H
     * gkVhnRnidYrMN5Qu4VqEQkd4Gz0jPJW+EfnbM+W/PvlWgDIZvhq7UfpjMA==
     * -----END CERTIFICATE-----
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  CaCertificates client = new CaCertificates(My.configuration().getClientProperties());
     *  X509Certificate rootCertificate = client.retrieveCaCertificate("root");
     *  X509Certificate tlsCertificate = client.retrieveCaCertificate("tls");
     *  X509Certificate samlCertificate = client.retrieveCaCertificate("saml");
     *  X509Certificate privacyCertificate = client.retrieveCaCertificate("privacy");
     * </pre>
    */    
    public X509Certificate retrieveCaCertificate(String certificateId) {
        //  {id} can be:  "root", "saml", "tls", "privacy"
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", certificateId);
        X509Certificate certificate = getTargetPath("ca-certificates/{id}").resolveTemplates(map).request(CryptoMediaType.APPLICATION_PKIX_CERT).get(X509Certificate.class);
        return certificate;
    }
       
    public String searchCaCertificatesPem(CaCertificateFilterCriteria criteria) {
        criteria.domain = "ek";
        String certificatesPem = getTargetPathWithQueryParams("ca-certificates", criteria).request(CryptoMediaType.APPLICATION_X_PEM_FILE).get(String.class);
        return certificatesPem;
    }
    
}
