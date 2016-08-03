/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.v2.resource;

import com.intel.dcsg.cpg.crypto.Sha256Digest;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.io.FileResource;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.My;
import com.intel.mtwilson.jaxrs2.mediatype.CryptoMediaType;
import com.intel.mtwilson.jaxrs2.mediatype.DataMediaType;
import com.intel.mtwilson.launcher.ws.ext.V2;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;

/**
 * Utility for the UI to provide users with their "trustagent.env" file contents
 * that can be used when installing and setting up the trust agent to
 * automatically point to this mt wilson server
 *
 * Note that the client provides the username and password as input to this
 * utility for now , but later we might allow the client to automatically
 * generate the username and password (would require the security privileges)
 *
 * @author jbuhacoff
 */
@V2
@Path("/util/trustagent-env-file")
public class TrustAgentEnvironmentUtil {

    public static class BasicAuthorizationInput {

        @FormParam("username")
        public String username;
        @FormParam("password")
        public String password;
    }

    public static class TrustagentEnvFileOutput {

        public String content;
    }

    //         SimpleKeystore keystore = new SimpleKeystore(new FileResource(My.configuration().getTlsKeystoreFile()), My.configuration().getTlsKeystorePassword());
    protected X509Certificate getTlsCertificate() throws FileNotFoundException, IOException, CertificateException {
        // code duplicated from CaCertificatesRepository in mtwilson-attestation-ws-v2
        String certFile = My.configuration().getConfiguration().getString("mtwilson.tls.certificate.file"); // throws IOException
        if (certFile != null && !certFile.startsWith(File.separator)) {
            certFile = "/etc/intel/cloudsecurity/" + certFile;
        }
        if (certFile != null) {
            if (certFile.endsWith(".pem")) {
                File tlsPemFile = new File(certFile);
                try (FileInputStream in = new FileInputStream(tlsPemFile)) { // throws FileNotFoundException
                    String pem = IOUtils.toString(in); // throws IOException
                    X509Certificate cert = X509Util.decodePemCertificate(pem); // throws CertificateException
                    return cert;
                }
            }
            if (certFile.endsWith(".crt")) {
                File tlsPemFile = new File(certFile);
                try (FileInputStream in = new FileInputStream(tlsPemFile)) { // throws FileNotFoundException
                    byte[] der = IOUtils.toByteArray(in); // throws IOException
                    X509Certificate cert = X509Util.decodeDerCertificate(der);
                    return cert;
                }
            }
            throw new FileNotFoundException("Certificate file is not in .pem or .crt format");
        } else {
            throw new FileNotFoundException("Could not obtain TLS cert chain location from config");
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    @RequiresPermissions("files:create")
    public String generateTrustagentEnvFileText(@BeanParam BasicAuthorizationInput input, @Context HttpServletRequest request) throws Exception {
        if (input.username.contains(":")) {
            throw new IllegalArgumentException("The colon ':' is not allowed in usernames");
        }
        String urltext = String.format("%s://%s:%d/mtwilson/v2", request.getScheme(), request.getLocalName(), request.getLocalPort());
        X509Certificate cert = getTlsCertificate();
        String tlsCertFingerprint = Sha256Digest.digestOf(cert.getEncoded()).toHexString();
        String content = String.format("MTWILSON_API_URL=%s\nMTWILSON_API_USERNAME=%s\nMTWILSON_API_PASSWORD=%s\nMTWILSON_TLS_CERT_SHA256=%s\n",
                urltext, input.username, input.password, tlsCertFingerprint);
        return content;
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, DataMediaType.APPLICATION_YAML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, DataMediaType.APPLICATION_YAML})
    @RequiresPermissions("files:create")
    public TrustagentEnvFileOutput generateTrustagentEnvFile(BasicAuthorizationInput input, @Context HttpServletRequest request) throws Exception {
        TrustagentEnvFileOutput output = new TrustagentEnvFileOutput();
        output.content = generateTrustagentEnvFileText(input, request);
        return output;
    }
    
    @GET
    @Produces(MediaType.TEXT_HTML)
    @RequiresPermissions("files:retrieve")
    public String getForm() {
        return "<html><body><form method=\"post\" action=\"trustagent-env-file.txt\"><table><tr><td><label>mtwilson API username: </label></td><td><input type=\"text\" name=\"username\"/></td></tr><tr><td><label>mtwilson API password: </label></td><td><input type=\"password\" name=\"password\"/></td></tr><tr><td>&nbsp;</td></tr><tr><td><input type=\"submit\"/></td></tr></table></form></body></html>";
    }
}
