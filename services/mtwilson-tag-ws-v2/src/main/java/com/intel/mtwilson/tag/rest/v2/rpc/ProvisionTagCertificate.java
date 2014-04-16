/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.rest.v2.rpc;

import com.intel.dcsg.cpg.crypto.file.PasswordEncryptedFile;
import com.intel.dcsg.cpg.crypto.key.password.PasswordProtection;
import com.intel.dcsg.cpg.crypto.key.password.PasswordProtectionBuilder;
import com.intel.dcsg.cpg.io.ByteArrayResource;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import com.intel.mtwilson.MyFilesystem;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import com.intel.mtwilson.jersey.http.OtherMediaType;
import com.intel.mtwilson.launcher.ws.ext.V2;
import com.intel.mtwilson.tag.PlaintextFilenameFilter;
import com.intel.mtwilson.tag.TagCertificateAuthority;
import com.intel.mtwilson.tag.TagConfiguration;
import com.intel.mtwilson.tag.Util;
import com.intel.mtwilson.tag.common.Global;
import com.intel.mtwilson.tag.model.Certificate;
import com.intel.mtwilson.tag.model.CertificateCollection;
import com.intel.mtwilson.tag.model.CertificateFilterCriteria;
import com.intel.mtwilson.tag.model.CertificateRequest;
import com.intel.mtwilson.tag.model.CertificateRequestLocator;
import com.intel.mtwilson.tag.rest.v2.repository.CertificateRepository;
import com.intel.mtwilson.tag.rest.v2.repository.CertificateRequestRepository;
import com.intel.mtwilson.tag.selection.SelectionBuilder;
import com.intel.mtwilson.tag.selection.xml.SelectionsType;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.io.IOUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.bouncycastle.cert.X509AttributeCertificateHolder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * See also AbstractRpcResource which does something similar with HTTP 202 Accepted
 * 
 * @author ssbangal and jbuhacoff
 */
@V2
@Path("/tag-certificate-requests-rpc/provision")
//@RPC("provision_tag_certificate")
//@JacksonXmlRootElement(localName="provision_tag_certificate")
public class ProvisionTagCertificate  {    
    private Logger log = LoggerFactory.getLogger(getClass().getName());

   private CertificateRepository certificateRepository;
   private CertificateRequestRepository repository;
    
    public ProvisionTagCertificate() {
        repository = new CertificateRequestRepository();
        certificateRepository = new CertificateRepository();
    }
    
    protected CertificateRequestRepository getRepository() {
        return repository;
    }
    
    protected boolean isAsync(HttpServletRequest request) {
        String async = request.getHeader("Asynchronous");
        return async != null && async.equalsIgnoreCase("true");
    }

    
    protected void storeAsyncRequest(String subject, SelectionsType selections, HttpServletResponse response) throws IOException {
            String xml = Util.toXml(selections);
            byte[] plaintext = xml.getBytes(Charset.forName("UTF-8"));
            CertificateRequest certificateRequest = new CertificateRequest();
            certificateRequest.setId(new UUID());
            certificateRequest.setStatus("New");
            certificateRequest.setSubject(subject);
            certificateRequest.setContent(plaintext); // will be automatically encrypted by the CertificateRequestRepository before storing
            certificateRequest.setContentType("application/xml"); 
            getRepository().create(certificateRequest);
            response.addHeader("Asynchronous", "true");
            response.addHeader("Link", String.format("</tag-certificate-requests/%s>; rel=status", certificateRequest.getId().toString()));
//            response.addHeader("Link", String.format("</tag-certificates?certificateRequestIdEqualTo=%s>; rel=certificate", certificateRequest.getId().toString()));
            response.setStatus(Response.Status.ACCEPTED.getStatusCode());        
    }
    
    protected Certificate storeTagCertificate(String subject, byte[] attributeCertificateBytes) throws IOException {
        X509AttributeCertificateHolder certificateHolder = new X509AttributeCertificateHolder(attributeCertificateBytes);
        Certificate certificate = Certificate.valueOf(certificateHolder.getEncoded());
        certificate.setId(new UUID());

        // Call into the certificate repository to create the new certificate entry in the database.
        certificateRepository.create(certificate);
        return certificate;
    }
    
    /**
     * Returns the tag certificate bytes or null if one was not generated
     * 
     * @param subject
     * @param selection may be null; the default selection will be used, if configured
     * @param request
     * @param response
     * @return
     * @throws IOException
     */
    public Certificate createOne(String subject, SelectionsType selections, HttpServletRequest request, HttpServletResponse response) throws Exception {        
        TagConfiguration configuration = new TagConfiguration(My.configuration().getConfiguration());
        TagCertificateAuthority ca = new TagCertificateAuthority(configuration);
        // if the subject is an ip address or hostname, resolve it to a hardware uuid with mtwilson - if the host isn't registered in mtwilson we can't get the hardware uuid so we have to reject the request
        if( !UUID.isValid(subject)) {
            subject = ca.findSubjectHardwareUuid(subject);
            if (subject == null) {
                throw new Exception("Invalid subject specified in the call");
            }
        }
        if( selections == null ) {
            selections = SelectionBuilder.factory().selection().build(); // default empty selection
        }
        // if external ca is configured then we only save the request to the database and indicate async processing in our response
        if( configuration.isTagProvisionExternal() || isAsync(request) ) {
            // requires async processing - we store the request, and an external ca will poll for requests, generate certs, and post the certs back to us; the client can periodically check the status and then download the cert when it's available
            storeAsyncRequest(subject, selections, response);
            return null;
        }
        // if always generate ca is enabled then generate it right now and return it - no need to check database for existing certs etc. 
        if( configuration.isTagProvisionNoCache() ) {
            byte[] certificateBytes = ca.createTagCertificate(UUID.valueOf(subject), selections);
            Certificate certificate = storeTagCertificate(subject, certificateBytes);
            return certificate;
        }
        // if there is an existing currently valid certificate we return it
        CertificateFilterCriteria criteria = new CertificateFilterCriteria();
        criteria.subjectEqualTo = subject;
        CertificateCollection results = certificateRepository.search(criteria); // DONE: TODO:  order by creation date so we get most recent first, and we pick the most recently created cert that is currently valid. 
        Date today = new Date();
        Certificate latestCert = null;
        BigInteger latestCreateTime = BigInteger.ZERO;
        if( !results.getCertificates().isEmpty() ) {
            for (Certificate certificate : results.getCertificates()) {
                if (today.before(certificate.getNotBefore())) {
                    continue;
                }
                if (today.after(certificate.getNotAfter())) {
                    continue;
                }
                // While creating the certificates we are storing the create time in the serial number field
                if (latestCreateTime.compareTo(certificate.getX509Certificate().getSerialNumber()) <= 0) {
                    latestCreateTime = certificate.getX509Certificate().getSerialNumber();
                    latestCert = certificate;                    
                } else {
                    continue;
                }
                // We won't return the first certificate found. Instead we return back the latest certificate.
                //return certificate;  // we return the first certificate (most recent, see TODO above) that is currently valid
            }
        }
        // Check if a valid certificate was found during the search.
        if (latestCert != null)
            return latestCert;
        
        // no cached certificate so generate a new certificate
            byte[] certificateBytes = ca.createTagCertificate(UUID.valueOf(subject), selections);
            Certificate certificate = storeTagCertificate(subject, certificateBytes);
            return certificate;
        
    }
    
    /**
     * Because the selection xml format (plaintext or encrypted) does not
     * include the target host's subject uuid, the client must specify
     * the target host subject uuid either with an HTTP header "Subject" whose
     * value is the uuid, or with a query parameter "subject" whose value is
     * the uuid.  If both are present only the HTTP header is used. If neither
     * is present the server will return a bad request error.
     * 
     * Unlike the JSON API, this method does not return the original request
     * as the response because the clients that send XML or encrypted XML
     * don't need it echoed back to them. If there is a response then it is
     * the generated tag certificate. If there is no response and the 
     * Asynchronous header is set to "true" in the response it means the
     * certificate will be generated later; Link headers in the same response
     * will indicate where the generated certificate can be obtained once
     * it is available.
     * 
     * @param locator
     * @param message
     * @param request 
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(OtherMediaType.APPLICATION_PKIX_CERT)
    @RequiresPermissions("tag_certificates:create")         
    public byte[] createOneFromJsonToBytes(@BeanParam CertificateRequestLocator locator, String json, @Context HttpServletRequest request, @Context HttpServletResponse response) throws Exception {        
        Certificate certificate = createOneJson(locator, json, request, response);
        return certificate.getCertificate();
    }
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresPermissions("tag_certificates:create")         
    public Certificate createOneJson(@BeanParam CertificateRequestLocator locator, String json, @Context HttpServletRequest request, @Context HttpServletResponse response) throws Exception {        
         TagConfiguration configuration = new TagConfiguration(My.configuration().getConfiguration());
         if( configuration.isTagProvisionXmlEncryptionRequired() ) {
             throw new WebApplicationException("Encryption is required", Response.Status.BAD_REQUEST);// TODO: i18n
         }
        SelectionsType selections = null;
        if( json != null ) {
            selections = Util.fromJson(json);
        }
        return createOne(getSubject(request, locator), selections, request, response);
    }
    
    /**
     * Because the selection xml format (plaintext or encrypted) does not
     * include the target host's subject uuid, the client must specify
     * the target host subject uuid either with an HTTP header "Subject" whose
     * value is the uuid, or with a query parameter "subject" whose value is
     * the uuid.  If both are present only the HTTP header is used. If neither
     * is present the server will return a bad request error.
     * 
     * Unlike the JSON API, this method does not return the original request
     * as the response because the clients that send XML or encrypted XML
     * don't need it echoed back to them.
     * 
     * @param locator
     * @param message
     * @param request 
     */
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(OtherMediaType.APPLICATION_PKIX_CERT)
    @RequiresPermissions("tag_certificates:create")         
    public byte[] createOneFromXmlToBytes(@BeanParam CertificateRequestLocator locator, String xml, @Context HttpServletRequest request, @Context HttpServletResponse response) throws Exception {
        Certificate certificate = createOneXml(locator, xml, request, response);
        return certificate.getCertificate();
    }
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @RequiresPermissions("tag_certificates:create")         
    public Certificate createOneXml(@BeanParam CertificateRequestLocator locator, String xml, @Context HttpServletRequest request, @Context HttpServletResponse response) throws Exception {
         TagConfiguration configuration = new TagConfiguration(My.configuration().getConfiguration());
         if( configuration.isTagProvisionXmlEncryptionRequired() ) {
             throw new WebApplicationException("Encryption is required", Response.Status.BAD_REQUEST);// TODO: i18n
         }
        SelectionsType selections = null;
        if( xml != null ) {
            selections = Util.fromXml(xml);
        }
        return createOne(getSubject(request, locator), selections, request, response);
    }
    
    /**
     * Because the selection xml format (plaintext or encrypted) does not
     * include the target host's subject uuid, the client must specify
     * the target host subject uuid either with an HTTP header "Subject" whose
     * value is the uuid, or with a query parameter "subject" whose value is
     * the uuid.  If both are present only the HTTP header is used. If neither
     * is present the server will return a bad request error.
     * 
     * Unlike the JSON API, this method does not return the original request
     * as the response because the clients that send XML or encrypted XML
     * don't need it echoed back to them.
     * 
     * @param locator
     * @param message
     * @param request 
     */
    @POST
    @Consumes(OtherMediaType.MESSAGE_RFC822)
    @Produces(OtherMediaType.APPLICATION_PKIX_CERT)
    @RequiresPermissions("tag_certificates:create")         
    public byte[] createOneEncryptedXml(@BeanParam CertificateRequestLocator locator, byte[] message, @Context HttpServletRequest request, @Context HttpServletResponse response) throws Exception {
         TagConfiguration configuration = new TagConfiguration(My.configuration().getConfiguration());
        
        /*
         * format mismatch - input file is encrypted with openssl, so we need to decrypt it using openssl or a compatible algorithm (which strips off the initial magic bytes and then decrypts it using specified algorithm and key length)
        ByteArrayResource resource = new ByteArrayResource(message);
        PasswordEncryptedFile passwordEncryptedFile = new PasswordEncryptedFile(resource, configuration.getTagProvisionXmlEncryptionPassword());
        byte[] content = passwordEncryptedFile.decrypt();
        */
        UUID uuid = new UUID();
        // TODO:  setup should create this path
        String encryptedFilePath = MyFilesystem.getApplicationFilesystem().getFeatureFilesystem("tag").getVarPath() + File.separator + uuid.toString() + ".enc";
        File encryptedFile = new File(encryptedFilePath);
        try(FileOutputStream out = new FileOutputStream(encryptedFile)) {
            IOUtils.write(message, out);
        }
        String tagCmdPath = MyFilesystem.getApplicationFilesystem().getFeatureFilesystem("tag").getBinPath();
        log.debug("Tag command path: {}", tagCmdPath);
        Process process = Runtime.getRuntime().exec(tagCmdPath+File.separator+"decrypt.sh -p PASSWORD "+ encryptedFilePath, new String[] { "PASSWORD="+configuration.getTagProvisionXmlEncryptionPassword() });
        try { 
            int exitValue = process.waitFor();
            if( exitValue != 0 ) { // same as exitValue but waits for process to end first; prevents java.lang.IllegalThreadStateException: process hasn't exited        at java.lang.UNIXProcess.exitValue(UNIXProcess.java:217)
                throw new IOException("Failed to decrypt file or integrity check failed (error "+exitValue+")");
            }
        }
        catch(InterruptedException e) {
                throw new IOException("Failed to decrypt file (interrupted)", e);
        }
        // now search for the original file inside the archive, ignoring the .sig file (which was already used for integrity check)
        File encryptedFileContentFolder = new File(encryptedFilePath+".d");
        File[] selectionFiles = encryptedFileContentFolder.listFiles(new PlaintextFilenameFilter());
        if( selectionFiles != null && selectionFiles.length > 0 ) {
            // process only the first file we find.  TODO:  what if there are multiple selection files in the zip ?
            try(FileInputStream in = new FileInputStream(selectionFiles[0])) {
                String xml = IOUtils.toString(in);
                //return createOneFromXmlToBytes(locator, xml, request, response); // don't call this because it checks if encryption is required and doesn't "know" that we just decrypted the file
                SelectionsType selections = Util.fromXml(xml);
                Certificate certificate = createOne(getSubject(request, locator), selections, request, response);
                return certificate.getCertificate();
            }
            finally {
                // TODO: delete all the temporary files - won't be necessary when the decryption moves to java and it's all in memory
            }
        }
        throw new IOException("Failed to read tag selection xml");
    }    
    
    private String getSubject(HttpServletRequest request, CertificateRequestLocator locator) {
        String subject = request.getHeader("Subject");
        if( subject != null && !subject.isEmpty()) {
            return subject; 
        }
        else if( locator.subject != null && !locator.subject.isEmpty() ) {
            return locator.subject; // from query paramter  ?subject={subject}            
        }
        else {
            throw new WebApplicationException(Response.Status.BAD_REQUEST); // TODO:  ErrorCode enum with internationalized message saying subject is required in header or query param
        }
    }
        
}
