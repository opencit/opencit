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
import com.intel.mtwilson.tag.model.CertificateRequest;
import com.intel.mtwilson.tag.model.CertificateRequestCollection;
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
     * provisioned. The UUID can be obtained using the dmidecode command.
     * @param Certificate object that needs to be created. 
     * @return Created CertificateRequest object.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions tag_certificates:create
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType POST
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/tag-certificates
     * Input: {"certificate":"MIIO....ic=","sha1":"7704753ac4a8771499610352f28967e39c75d88b",
     * "sha256":"09740b068caba9e8647488d3e5a1a546e136c47cffcc30198a4446c765e344e0","subject":"2676ee69-e42f-461b-824f-a6ec3d2c08f4",
     * "issuer":"CN=ExternalCA","not_before":"2014-03-21","not_after":"2015-03-21","revoked":false}
     * Output: {"id":"187ec902-c6c6-4dfb-adb4-f240099aa4b0","certificate":"MIIO....ic=","sha1":"7704753ac4a8771499610352f28967e39c75d88b",
     * "sha256":"09740b068caba9e8647488d3e5a1a546e136c47cffcc30198a4446c765e344e0","subject":"2676ee69-e42f-461b-824f-a6ec3d2c08f4",
     * "issuer":"CN=Intel","not_before":"2014-03-21","not_after":"2015-03-21","revoked":true}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  Certificates client = new Certificates(My.configuration().getClientProperties());
     *  Certificate obj = new Certificate();
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
     * @return N/A
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions tag_certificate:delete
     * @mtwContentTypeReturned N/A
     * @mtwMethodType DELETE
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/tag-certificates/187ec902-c6c6-4dfb-adb4-f240099aa4b0
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  Certificates client = new Certificates(My.configuration().getClientProperties());
     *  client.deleteCertificateRequest("732566c3-10fc-41bf-8c29-72026a355c58");
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
     * @param role - Certificate object having the status and the Id of the certificate that needs to be updated. 
     * @return Updated Certificate object.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions tag_certificates:store
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType PUT
     * @mtwSampleRestCall
     * https://server.com:8181/mtwilson/v2/tag-certificates/187ec902-c6c6-4dfb-adb4-f240099aa4b0
     * Input: {"revoked":true}
     * Output: {"id":"187ec902-c6c6-4dfb-adb4-f240099aa4b0","revoked":true}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  Certificates client = new Certificates(My.configuration().getClientProperties());
     *  Certificate obj = new Certificate();
     *  Certificate updatedObj = client.editCertificate(obj);
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
     * Retrieves the details of the Certificate for the specified ID. 
     * @param uuid - UUID of the certificate that needs to be retrieved
     * @return Certificate object matching the specified UUID.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions tag_certificates:retrieve
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/tag-certificates/187ec902-c6c6-4dfb-adb4-f240099aa4b0
     * Output: {"id":"187ec902-c6c6-4dfb-adb4-f240099aa4b0","certificate":"MIIO....ic=","sha1":"7704753ac4a8771499610352f28967e39c75d88b",
     * "sha256":"09740b068caba9e8647488d3e5a1a546e136c47cffcc30198a4446c765e344e0","subject":"2676ee69-e42f-461b-824f-a6ec3d2c08f4",
     * "issuer":"CN=Intel","not_before":"2014-03-21","not_after":"2015-03-21","revoked":true}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  Certificates client = new Certificates(My.configuration().getClientProperties());
     *  Certificate obj = client.retrieveCertificate(UUID.valueOf("187ec902-c6c6-4dfb-adb4-f240099aa4b0");
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
     * @param CertificateFilterCriteria object specifying the filter criteria. Search options include
     * subjectEqualTo, subjectContains, issuerEqualTo, issuerContains, sha1, sha256, notBefore, notAfter and revoked.
     * @return CertificateCollection object with the list of all the Certificate objects matching the specified filter criteria
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions tag_certificates:search
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/tag-certificates?issuer=Intel
     * Output: {"certificates":[{"id":"187ec902-c6c6-4dfb-adb4-f240099aa4b0","certificate":"MIICMj...BYG=","sha1":"7704753ac4a8771499610352f28967e39c75d88b",
     * "sha256":"09740b068caba9e8647488d3e5a1a546e136c47cffcc30198a4446c765e344e0","subject":"2676ee69-e42f-461b-824f-a6ec3d2c08f4",
     * "issuer":"CN=Intel","not_before":1395407513000,"not_after":1426943513000,"revoked":true}]}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  Certificates client = new Certificates(My.configuration().getClientProperties());
     *  CertificateFilterCriteria criteria = new CertificateFilterCriteria();
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
