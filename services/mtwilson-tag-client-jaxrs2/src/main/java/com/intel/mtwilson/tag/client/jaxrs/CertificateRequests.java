/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.client.jaxrs;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.jaxrs2.client.MtWilsonClient;
import com.intel.mtwilson.tag.model.CertificateRequest;
import com.intel.mtwilson.tag.model.CertificateRequestCollection;
import com.intel.mtwilson.tag.model.CertificateRequestFilterCriteria;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author ssbangal
 */
public class CertificateRequests extends MtWilsonClient {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Selections.class);

    public CertificateRequests(URL url) throws Exception{
        super(url);
    }

    public CertificateRequests(Properties properties) throws Exception {
        super(properties);
    }    
    /**
     * Creates a new certificate request in the system. The certificate request would be created only if 
     * the certificates would be created by an external CA. The attributes (key-value pairs) that needs to
     * be added to the certificate would also be stored in the Certificate request.
     * @param CertificateRequest object that needs to be created. The subject indicates the host for which
     * the certificate should be created. The certificate that would be created would have this same value
     * in the subject field.
     * @return Created CertificateRequest object.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions tag_certificate_requests:create
     * @mtwContentTypeReturned message/rfc822
     * @mtwMethodType POST
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/tag-certificate-requests
     * Input: 
     * Output: {@code <certificate_request><id>732566c3-10fc-41bf-8c29-72026a355c58</id>
     * <links><content>/tag-certificate-requests/732566c3-10fc-41bf-8c29-72026a355c58/content</content>
     * <certificate>/tag-certificates?certificateRequestIdEqualTo=732566c3-10fc-41bf-8c29-72026a355c58</certificate>
     * <status>/tag-certificate-requests/732566c3-10fc-41bf-8c29-72026a355c58</status></links>
     * <subject>192.168.0.156</subject>
     * <status>New</status>
     * <content>LS0tLS1CRUdJTiBFTkNSWVBURUQgREFUQS0tLS0tDQpDb250ZW50LUVuY29kaW5nOiBiYXNlNjQN
     * CkVuY3J5cHRpb24tQWxnb3JpdGhtOiBBRVMvQ0JDL1BLQ1M1UGFkZGluZw0KRW5jcnlwdGlvbi1L
     * ZXktSWQ6IHVoVU44czJLMCtnPTp5cXY2WTE1Zmd2Z0lOalNkRHI5MHE4eXFESTB0ZWVLdzFIVHc2
     * dkV2dC9FPQ0KSW50ZWdyaXR5LUFsZ29yaXRobTogU0hBMjU2DQpLZXktQWxnb3JpdGhtOiBQQktE
     * RjJXaXRoSG1hY1NIQTE7IGl0ZXJhdGlvbnM9MTAwMDsga2V5LWxlbmd0aD0xMjg7IHNhbHQtYnl0
     * ZXM9OA0KDQpiWUF6ZVVOQUs2a0dlamhEckY0bkllb2JZdkN5NHp1VFVxb2h1VVRycTFtTjZoSzN1
     * SlVSK2xQQUpsOTlkM3U0UWVjQzNlL0RmS2VTDQo2a2F5S3dBZlhWTHdxQWN4YktCRg0KLS0tLS1F
     * TkQgRU5DUllQVEVEIERBVEEtLS0tLQ0K</content><content_type>message/rfc822</content_type></certificate_request>}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  CertificateRequests client = new CertificateRequests(My.configuration().getClientProperties());
     *  CertificateRequest obj = new CertificateRequest();
     *  CertificateRequest createdObj = client.createCertificateRequest(obj);
     * </pre>
     */
    public CertificateRequest createCertificateRequest(CertificateRequest obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        CertificateRequest createdObj = getTarget().path("tag-certificate-requests").request().accept(MediaType.APPLICATION_JSON).post(Entity.json(obj), CertificateRequest.class);
        return createdObj;
    }

    /**
     * Deletes the Certificate request for the specified ID. 
     * @param uuid - UUID of the certificate request that has to be deleted.
     * @return N/A
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions tag_certificate_requests:delete
     * @mtwContentTypeReturned N/A
     * @mtwMethodType DELETE
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/tag-certificate-requests/732566c3-10fc-41bf-8c29-72026a355c58
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  CertificateRequests client = new CertificateRequests(My.configuration().getClientProperties());
     *  client.deleteCertificateRequest("732566c3-10fc-41bf-8c29-72026a355c58");
     * </pre>
     */
    public void deleteCertificateRequest(UUID uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", uuid);
        Response obj = getTarget().path("tag-certificate-requests/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).delete();
    }
    
    /**
     * Allows the user to update the status of the certificate request after the external CA has completed
     * processing the request. 
     * @param role - CertificateRequest object having the status and the Id of the request that needs to be updated. 
     * @return Updated CertificateRequest object.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions tag_certificate_requests:store
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType PUT
     * @mtwSampleRestCall
     * https://server.com:8181/mtwilson/v2/tag-certificate-requests/07217f9c-f625-4c5a-a538-73f1880abdda
     * Input: {"status":"approved"}
     * Output:{"id":"732566c3-10fc-41bf-8c29-72026a355c58","status":"approved"}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  CertificateRequests client = new CertificateRequests(My.configuration().getClientProperties());
     *  CertificateRequest obj = new CertificateRequest();
     *  CertificateRequest updatedObj = client.editCertificateRequest(obj);
     * </pre>
     */
    public CertificateRequest editCertificateRequest(CertificateRequest obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", obj.getId().toString());
        CertificateRequest updatedObj = getTarget().path("tag-certificate-requests/{id}").resolveTemplates(map).request().accept(MediaType.APPLICATION_JSON).put(Entity.json(obj), CertificateRequest.class);
        return updatedObj;
    }

    /**
     * Retrieves the Certificate request details for the specified ID. 
     * @param uuid - UUID of the certificate request that needs to be retrieved
     * @return CertificateRequest object matching the specified UUID.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions tag_certificate_requests:retrieve
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/tag-certificate-requests/07217f9c-f625-4c5a-a538-73f1880abdda
     * Output: {"id":"732566c3-10fc-41bf-8c29-72026a355c58","subject":"192.168.0.156","status":"New",
     * "content":"LS0tLS1CRUdJTiBFTkNSWVBUR........tLS0tLQ0K","content_type":"message/rfc822"}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  CertificateRequests client = new CertificateRequests(My.configuration().getClientProperties());
     *  CertificateRequest obj = client.retrieveCertificateRequest(UUID.valueOf("07217f9c-f625-4c5a-a538-73f1880abdda");
     * </pre>
     */
    public CertificateRequest retrieveCertificateRequest(UUID uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", uuid);
        CertificateRequest obj = getTarget().path("tag-certificate-requests/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(CertificateRequest.class);
        return obj;
    }

    /**
     * Retrieves the Certificate requests based on the search criteria specified. 
     * @param CertificateRequestFilterCriteria object specifying the filter criteria. Search options include
     * id, subjectEqualTo, subjectContains, statusEqualTo & contentTypeEqualTo.
     * @return CertificateRequestCollection object with the list of all the CertificateRequest objects matching the specified filter criteria
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions tag_certificate_requests:search
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/tag-certificate-requests?contentTypeEqualTo=rfc822
     * Output: {"certificate_requests":[{"id":"1865f370-bd58-4a38-996a-ff2149dfb689","subject":"192.168.0.155","status":"New","content":"....","content_type":"message/rfc822"},
     * {"id":"732566c3-10fc-41bf-8c29-72026a355c58","subject":"192.168.0.156","status":"approved","content":"....","content_type":"message/rfc822"}]}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  CertificateRequests client = new CertificateRequests(My.configuration().getClientProperties());
     *  CertificateRequestFilterCriteria criteria = new CertificateRequestFilterCriteria();
     *  criteria.contentTypeEqualTo = "rfc822";
     *  CertificateRequestCollection objCollection = client.searchCertificateRequests(criteria);
     * </pre>
     */
    public CertificateRequestCollection searchCertificateRequests(CertificateRequestFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        CertificateRequestCollection objCollection = getTargetPathWithQueryParams("tag-certificate-requests", criteria).request(MediaType.APPLICATION_JSON).get(CertificateRequestCollection.class);
        return objCollection;
    }
    
    
}
