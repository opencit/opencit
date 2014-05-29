/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.client.jaxrs;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.jaxrs2.client.MtWilsonClient;
import com.intel.mtwilson.tag.model.Certificate;
import com.intel.mtwilson.tag.model.CertificateCollection;
import com.intel.mtwilson.tag.model.CertificateFilterCriteria;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @since 2.0
 * @author ssbangal
 */
public class Certificates  extends MtWilsonClient {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Selections.class);

    public Certificates(URL url) throws Exception{
        super(url);
    }

    public Certificates(Properties properties) throws Exception {
        super(properties);
    }            
    
    /**
     * Creates a new certificate entry into the database that can be provisioned for the host. Note that the
     * certificate subject has to have the hardware uuid of the host to which the certificate has to be
     * provisioned. The UUID can be obtained using the search method in the HostUuid resource.
     * @param obj Certificate object that needs to be created. 
     * @return Created CertificateRequest object.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions tag_certificates:create
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType POST
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/tag-certificates
     * Input: {"certificate":"MIICMjCCARoCAQEwH6EdpBswGTEXMBUGAWkEEAZIZupiDRHgsakAHmcQQ8SgIDAepBwwGjEYMBYGA1UEAwwPYXNzZXRUY
     * WdTZXJ2aWNlMA0GCSqGSIb3DQEBCwUAAgYBRkaePNswIhgPMjAxNDA1MjkwNjE1MTNaGA8yMDE1MDUyOTA2MTUxM1owgZYwGAYFVQSGFQExDwwNY3Vzd
     * G9tZXI9Q29rZTATBgVVBIYVATEKDAhzdGF0ZT1DQTAVBgVVBIYVATEMDApjb3VudHJ5PVVTMBsGBVUEhhUBMRIMEGNpdHk9U2FudGEgQ2xhcmEwGQYFV
     * QSGFQExEAwOY3VzdG9tZXI9UGVwc2kwFgYFVQSGFQExDQwLY2l0eT1Gb2xzb20wDQYJKoZIhvcNAQELBQADggEBAH7+oMPKjZCVa3QuG/YgJrungrr32
     * xtbwb4d3tzln3KCtd/NjwWULRWPyNoXTeUh7lceNnAFZWBsm+iTke6hi1yjkou275MeXftIf8xVFJDie5BAq6aMENIalbEW7jYNUB5hDlebjOt4RgZ2n
     * efBB9M4/9BgInM6hcG3PXdmCeXLZBoKcu9Ae8I4C8WQB4JmgDco1u7pzamne2ZGQiwNuDIlkNqQqUwS7dul6KmzQHpv/7pPem7gGZFFmMAuqrC4ng4vJ
     * clNV1ojUXHl0M/BteTfKyaEolzD+muf8JXM0dzhjWVxu13wOBYrric22mo+HtbdqqrgVOH+oh59ESFVtUM=",
     * "sha1":"5b83754aedc10d1fe851b55e85baddf273af41c0","sha256":"b17585bf4f93e9c03ec09abb68a548a7871273f156aa41a8820b51d450e4d320",
     * "subject":"064866ea-620d-11e0-b1a9-001e671043c4","issuer":"CN=assetTagService","not_before":1401344113000,"not_after":1432880113000}
     * 
     * Output: {"id":"695e8d32-0dd8-46bb-90d6-d2520ff5e2f0","certificate":"MIICMjCCARoCAQEwH6EdpBswGTEXMBUGAWkE
     * EAZIZupiDRHgsakAHmcQQ8SgIDAepBwwGjEYMBYGA1UEAwwPYXNzZXRUYWdTZXJ2aWNlMA0GCSqGSIb3DQEBCwUAAgYBRkaePNswIhg
     * PMjAxNDA1MjkwNjE1MTNaGA8yMDE1MDUyOTA2MTUxM1owgZYwGAYFVQSGFQExDwwNY3VzdG9tZXI9Q29rZTATBgVVBIYVATEKDAhzdGF
     * 0ZT1DQTAVBgVVBIYVATEMDApjb3VudHJ5PVVTMBsGBVUEhhUBMRIMEGNpdHk9U2FudGEgQ2xhcmEwGQYFVQSGFQExEAwOY3VzdG9tZXI9
     * UGVwc2kwFgYFVQSGFQExDQwLY2l0eT1Gb2xzb20wDQYJKoZIhvcNAQELBQADggEBAH7+oMPKjZCVa3QuG/YgJrungrr32xtbwb4d3tzln
     * 3KCtd/NjwWULRWPyNoXTeUh7lceNnAFZWBsm+iTke6hi1yjkou275MeXftIf8xVFJDie5BAq6aMENIalbEW7jYNUB5hDlebjOt4RgZ2ne
     * fBB9M4/9BgInM6hcG3PXdmCeXLZBoKcu9Ae8I4C8WQB4JmgDco1u7pzamne2ZGQiwNuDIlkNqQqUwS7dul6KmzQHpv/7pPem7gGZFFmMA
     * uqrC4ng4vJclNV1ojUXHl0M/BteTfKyaEolzD+muf8JXM0dzhjWVxu13wOBYrric22mo+HtbdqqrgVOH+oh59ESFVtUM=",
     * "sha1":"5b83754aedc10d1fe851b55e85baddf273af41c0","sha256":"b17585bf4f93e9c03ec09abb68a548a7871273f156aa41a8820b51d450e4d320",
     * "subject":"064866ea-620d-11e0-b1a9-001e671043c4","issuer":"CN=assetTagService","not_before":1401344113000,
     * "not_after":1432880113000,"revoked":false}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  Certificates client = new Certificates(My.configuration().getClientProperties());
     *  Certificate obj = new Certificate();
     *  obj.setCertificate(certificate); // assuming the certificate has been created.
     *  obj.setSha1(Sha1Digest.digestOf(certificate).toByteArray());
     *  obj.setSha256(Sha256Digest.digestOf(certificate()).toByteArray());
     *  obj.setSubject("064866ea-620d-11e0-b1a9-001e671043c4");
     *  Date notBefore = new Date();
     *  Date notAfter = new Date(notBefore.getTime() + (1000 * 60 * 60 * 24 * 365)); // one year
     *  obj.setNotBefore(notBefore);
     *  obj.setNotAfter(notAfter);
     *  obj.setIssuer("CN=assetTagService");
     *  Certificate createdObj = client.createCertificate(obj);
     * </pre>
     */
    public Certificate createCertificate(Certificate obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        Certificate createdObj = getTarget().path("tag-certificates").request().accept(MediaType.APPLICATION_JSON).post(Entity.json(obj), Certificate.class);
        return createdObj;
    }

    /**
     * Deletes the specified certificate from the system.  
     * @param uuid - UUID of the certificate that has to be deleted.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions tag_certificate:delete
     * @mtwContentTypeReturned N/A
     * @mtwMethodType DELETE
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/tag-certificates/695e8d32-0dd8-46bb-90d6-d2520ff5e2f0
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  Certificates client = new Certificates(My.configuration().getClientProperties());
     *  client.deleteCertificate(UUID.valueOf("695e8d32-0dd8-46bb-90d6-d2520ff5e2f0"));
     * </pre>
     */
    public void deleteCertificate(UUID uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", uuid);
        Response obj = getTarget().path("tag-certificates/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).delete();
    }
    
    /**
     * Allows the user to edit the revoked status of the certificate. No other information can be edited.
     * @param obj - Certificate object having the status and the UUID of the certificate that needs to be updated. 
     * @return Updated Certificate object.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions tag_certificates:store
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType PUT
     * @mtwSampleRestCall
     * https://server.com:8181/mtwilson/v2/tag-certificates/695e8d32-0dd8-46bb-90d6-d2520ff5e2f0
     * Input: {"revoked":true}
     * Output: {"id":"695e8d32-0dd8-46bb-90d6-d2520ff5e2f0","revoked":true}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  Certificates client = new Certificates(My.configuration().getClientProperties());
     *  Certificate editObj = new Certificate();
     *  editObj.setId(UUID.valueOf("695e8d32-0dd8-46bb-90d6-d2520ff5e2f0"));
     *  editObj.setRevoked(false);
     *  editObj = client.editCertificate(editObj);
     * </pre>
     */    
    public Certificate editCertificate(Certificate obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", obj.getId().toString());
        Certificate updatedObj = getTarget().path("tag-certificates/{id}").resolveTemplates(map).request().accept(MediaType.APPLICATION_JSON).put(Entity.json(obj), Certificate.class);
        return updatedObj;
    }

    /**
     * Retrieves the details of the Certificate with the specified ID. 
     * @param uuid - UUID of the certificate that needs to be retrieved
     * @return Certificate object matching the specified UUID.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions tag_certificates:retrieve
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/tag-certificates/695e8d32-0dd8-46bb-90d6-d2520ff5e2f0
     * Output: {"id":"695e8d32-0dd8-46bb-90d6-d2520ff5e2f0","certificate":"MIICMjCCARoCAQEwH6EdpBswGTEXMBUGAWkE
     * EAZIZupiDRHgsakAHmcQQ8SgIDAepBwwGjEYMBYGA1UEAwwPYXNzZXRUYWdTZXJ2aWNlMA0GCSqGSIb3DQEBCwUAAgYBRkaePNswIhg
     * PMjAxNDA1MjkwNjE1MTNaGA8yMDE1MDUyOTA2MTUxM1owgZYwGAYFVQSGFQExDwwNY3VzdG9tZXI9Q29rZTATBgVVBIYVATEKDAhzdGF
     * 0ZT1DQTAVBgVVBIYVATEMDApjb3VudHJ5PVVTMBsGBVUEhhUBMRIMEGNpdHk9U2FudGEgQ2xhcmEwGQYFVQSGFQExEAwOY3VzdG9tZXI9
     * UGVwc2kwFgYFVQSGFQExDQwLY2l0eT1Gb2xzb20wDQYJKoZIhvcNAQELBQADggEBAH7+oMPKjZCVa3QuG/YgJrungrr32xtbwb4d3tzln
     * 3KCtd/NjwWULRWPyNoXTeUh7lceNnAFZWBsm+iTke6hi1yjkou275MeXftIf8xVFJDie5BAq6aMENIalbEW7jYNUB5hDlebjOt4RgZ2ne
     * fBB9M4/9BgInM6hcG3PXdmCeXLZBoKcu9Ae8I4C8WQB4JmgDco1u7pzamne2ZGQiwNuDIlkNqQqUwS7dul6KmzQHpv/7pPem7gGZFFmMA
     * uqrC4ng4vJclNV1ojUXHl0M/BteTfKyaEolzD+muf8JXM0dzhjWVxu13wOBYrric22mo+HtbdqqrgVOH+oh59ESFVtUM=",
     * "sha1":"5b83754aedc10d1fe851b55e85baddf273af41c0","sha256":"b17585bf4f93e9c03ec09abb68a548a7871273f156aa41a8820b51d450e4d320",
     * "subject":"064866ea-620d-11e0-b1a9-001e671043c4","issuer":"CN=assetTagService","not_before":1401344113000,
     * "not_after":1432880113000,"revoked":false}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  Certificates client = new Certificates(My.configuration().getClientProperties());
     *  Certificate obj = client.retrieveCertificate(UUID.valueOf("695e8d32-0dd8-46bb-90d6-d2520ff5e2f0");
     * </pre>
     */
    public Certificate retrieveCertificate(UUID uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", uuid);
        Certificate obj = getTarget().path("tag-certificates/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(Certificate.class);
        return obj;
    }    
        
    /**
     * Retrieves the details of the provisioned certificates based on the search criteria specified. If none
     * of the search criteria is specified, then search would return back and empty result set. The 
     * possible search options include subjectEqualTo, subjectContains, issuerEqualTo, issuerContains, 
     * sha1, sha256, notBefore, notAfter and revoked.  
     * @param criteria CertificateFilterCriteria object specifying the filter criteria. Search options include
     * subjectEqualTo, subjectContains, issuerEqualTo, issuerContains, sha1, sha256, notBefore, notAfter and revoked.
     * @return CertificateCollection object with the list of all the Certificate objects matching the specified filter criteria
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions tag_certificates:search
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/tag-certificates?subjectEqualTo=064866ea-620d-11e0-b1a9-001e671043c4
     * Output: {"certificates":[{"id":"695e8d32-0dd8-46bb-90d6-d2520ff5e2f0",
     * "certificate":"MIICMjCCARoCAQEwH6EdpBswGTEXMBUGAWkEEAZIZupiDRHgsakAHmcQQ8SgIDAepBwwGjEYMBYGA1UEAwwPYXNzZXRUYWdT
     * ZXJ2aWNlMA0GCSqGSIb3DQEBCwUAAgYBRkaePNswIhgPMjAxNDA1MjkwNjE1MTNaGA8yMDE1MDUyOTA2MTUxM1owgZYwGAYFVQSGFQExDwwNY3V
     * zdG9tZXI9Q29rZTATBgVVBIYVATEKDAhzdGF0ZT1DQTAVBgVVBIYVATEMDApjb3VudHJ5PVVTMBsGBVUEhhUBMRIMEGNpdHk9U2FudGEgQ2xhcmE
     * wGQYFVQSGFQExEAwOY3VzdG9tZXI9UGVwc2kwFgYFVQSGFQExDQwLY2l0eT1Gb2xzb20wDQYJKoZIhvcNAQELBQADggEBAH7+oMPKjZCVa3QuG/Yg
     * Jrungrr32xtbwb4d3tzln3KCtd/NjwWULRWPyNoXTeUh7lceNnAFZWBsm+iTke6hi1yjkou275MeXftIf8xVFJDie5BAq6aMENIalbEW7jYNUB5hDl
     * ebjOt4RgZ2nefBB9M4/9BgInM6hcG3PXdmCeXLZBoKcu9Ae8I4C8WQB4JmgDco1u7pzamne2ZGQiwNuDIlkNqQqUwS7dul6KmzQHpv/7pPem7gGZFF
     * mMAuqrC4ng4vJclNV1ojUXHl0M/BteTfKyaEolzD+muf8JXM0dzhjWVxu13wOBYrric22mo+HtbdqqrgVOH+oh59ESFVtUM=",
     * "sha1":"5b83754aedc10d1fe851b55e85baddf273af41c0","sha256":"b17585bf4f93e9c03ec09abb68a548a7871273f156aa41a8820b51d450e4d320",
     * "subject":"064866ea-620d-11e0-b1a9-001e671043c4","issuer":"CN=assetTagService","not_before":1401344113000,
     * "not_after":1432880113000,"revoked":false}]}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  Certificates client = new Certificates(My.configuration().getClientProperties());
     *  CertificateFilterCriteria criteria = new CertificateFilterCriteria();
     *  criteria.subjectEqualTo = "064866ea-620d-11e0-b1a9-001e671043c4";
     *  CertificateCollection objCollection = client.searchCertificates(criteria);
     * </pre>
     */
    public CertificateCollection searchCertificates(CertificateFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        CertificateCollection objCollection = getTargetPathWithQueryParams("tag-certificates", criteria)
                .request(MediaType.APPLICATION_JSON).get(CertificateCollection.class);
        return objCollection;
    }
    
}
