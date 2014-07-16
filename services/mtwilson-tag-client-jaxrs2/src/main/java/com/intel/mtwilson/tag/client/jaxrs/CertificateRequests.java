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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

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
     * be added to the certificate would also be stored in the Certificate request in the encrypted format.
     * @param obj CertificateRequest object that needs to be created. The subject indicates the host for which
     * the certificate should be created. The certificate that would be created would have this same value
     * in the subject field. The data for the content field can be obtained by calling into the GET method on the 
     * Selections resource in an encrypted format.
     * @return Created CertificateRequest object.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions tag_certificate_requests:create
     * @mtwContentTypeReturned message/rfc822
     * @mtwMethodType POST
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/tag-certificate-requests
     * Input: {"subject":"064866ea-620d-11e0-b1a9-001e671043c4","content":"U2FsdGVkX19foheWuO2gmlwyOdHwwnGZydv8BYR9adE+Q
     * ujdXx/+w2Lm8wa6bZgp+srGGrTC08Zp8cLHaCs4Bep/ARaCfW86PwH0v0obpgm03P0pKkRZcT7NWKDXz105zPONQJ1HyX6PAv1SuplXAD6rgv2lSTG
     * 1Q8jc0fdmMphR1mjv2j3nxfVcy4b195jJGXu63upueaY3bRr12YdWgcxMUFY9kwTgCQgXS2V4KqbQ6degKGrTi1ghoDF5r+R35LbKz1sQEiJ6KI+x3
     * 1/yr4h5MCPbuh58VwxkDC0XHdKNezm2WAGTanYZoUWQDW69cO7oYbI3TFG07299dIlBPY0dgRxPhGgIxKncuMmI28NjKnJrc3klMED7R0AZkS11Fzfc
     * BikSbchxGf4C3iGvm/dBG+8sOBAaA8Gkbc5zfSiibTl9maT+WN974P0JoM6aAl8K/CSAni8Q5wl06rg9RrFeVpYCmjshF7KeOqlipK3Ps1CQ8CoZUA9
     * PyWmu2y0mrCmzkkwi+KN0CVbCWOOntmLfrlNXcP3Nh/KbldwTB7VRM+qIoaxgYIUxq6RVT7tRBmXulZlU5fZLTvETnydu1qoFFukhbYo7x3PHm+K4ne
     * ZukrvytF09QyZJjZCqedP44r0yw7/vWSCR1m4T8uB+PqaqGSriENvVa1uu3o4dQzw5U2abZ767TIcI6h02P63wzCkYbeW+Kell13gPsEeQISRUvIYDD
     * +eVXKmEGHesbbBO9G0pD2SO5bIVyHNqTKZNHQwAzn8M9id5ippNBclJ+J2aWdI8AOxPZDNwT4KoibUh0z3jHf0rXgMmRPyFhW8iLaLKofUaiRm86nQ3
     * NTLBWWCl6Ga7pWsBVshcM2Fh+PIwaDNGQLmbZPKE3s8S/zBBAfTM5TcZTHFsqi18lOi1A+GlgyBUza0ssQF4rqahAhL3gMRc2Gk9NQlQSwZ8p+v1UefT
     * AUxvkBpq4MLLAfVwePomHE1L9LZVjFK+dRm3M6TCis1Qg7Ve2ThBYtgVmer++yFymvXn4QAe1k3ihOjsfTtr106xEL8qDK6/81mRs9fSs6r4wvt90x3u
     * CwWbL6+mSKt0fxy5cgnUJ/jJ7Eoql7uotQsAUUdTLR1AVkvKop31581FtryXCGoTYP00tCMuD+uZH5ZzF6qBsOOk8ukJko3a9Fo9yKLALw=="}
     * 
     * Output: {"id":"68e91a2c-a74f-4969-b1d8-a07e91afa0d9",
     * "links":{"content":"/tag-certificate-requests/68e91a2c-a74f-4969-b1d8-a07e91afa0d9/content",
     * "certificate":"/tag-certificates?certificateRequestIdEqualTo=68e91a2c-a74f-4969-b1d8-a07e91afa0d9",
     * "status":"/tag-certificate-requests/68e91a2c-a74f-4969-b1d8-a07e91afa0d9"},
     * "subject":"064866ea-620d-11e0-b1a9-001e671043c4","status":"New",
     * "content":"LS0tLS1CRUdJTiBFTkNSWVBURUQgREFUQS0tLS0tDQpDb250ZW50LUVuY29kaW5nOiBiYXNlNjQNCkVuY3J5cHRpb24tQWxnb3JpdGhtO
     * iBBRVMvQ0JDL1BLQ1M1UGFkZGluZw0KRW5jcnlwdGlvbi1LZXktSWQ6IGtXanFsL1dNYkhRPTpJcnVMT0tSSVpPTCtTRFZtY0UxdVdkR3ZwdXY0K0NGS
     * kcxTFc1MHIrbzVrPQ0KSW50ZWdyaXR5LUFsZ29yaXRobTogU0hBMjU2DQpLZXktQWxnb3JpdGhtOiBQQktERjJXaXRoSG1hY1NIQTE7IGl0ZXJhdGlvb
     * nM9MTAwMDsga2V5LWxlbmd0aD0xMjg7IHNhbHQtYnl0ZXM9OA0KDQpFSzQ1cXZiYXBXNS84QUlYdldtMUJnZktwS0xLNWNIOXBUUnNydVJYWXlIUXFma
     * U5jY3hVNXVzbUdnelBYVEhmWmJlbHp6aDh2a25tDQoxcDlNeUVwb3VpeFNHTGdNN3RiTjRiR2VNSFdFTlpQVGVHKzJvakZ4K1pFTGx2SVlsS2d3M01FR
     * mxDWFlXVFhNRWlqbEIzMW1LdzU0DQpMRnlua0RWNGhaYmhqVWFHcFl2aGluU2lIMzYrdElON3d0Ymw3QmpuNGk0Nkg2L1c0RGlLeTJ1NGpTWTFxWW05Y
     * lR6UHdIZkxVcmRuDQpEWVNsbmZxRWRJOFpoekluU2o0dXdUOU9TY2xOZndqSENPT1BUNThaSHh3YWgwdWo3ZUdIUi9aakVCd2FicjRENVkwT240UkZEe
     * WZMDQpSemNtaEFLcnJlZUowbkJKYmRhS3VaVklDSDJsOXlvWUJ5eTkzZU91aDRuSGRSRk9GUmh3Ni94WmlFeE9SOXk3WmFmNlhiVTh2N0l2DQo1TVFtV
     * VZrKzFkTEEyY2liV3ZaZTBJV3JPbmlkR01wSEY1d1J6OHVDQitiRzg2SGJRSkNHNWR3bklzYkozOTFHZUltMERWWmtiS0xQDQo1OVN3czJVUmhsRFg5M
     * TVSL29abmhkVVRzVzFML3JTUm1HZEFMRVIxU2E1OUYrTGg4dXlZelhKbFU3dUVHQ2dXSlZ2RzMxRHBLRDhsDQpLZ3JEekpGNmxuZFloV3oycE44dVM0N
     * 0M2Q1p5VnRpQXNpUGc3Wi83eXBhOGE5cW5BelZ1WG54cWg3SDBhQnNsUkZkaHhVM0hkSU5WDQphT1NvWHpMdTVFUEVXV05iVEo4V3d2SUcyWmh6MXhna
     * zFKNGhMbkxBWEh3NzdMQTREblNJU1pMY3hiZDFiM3g3TCtvVFUzb21WUVV6DQpidkdyeVJ6NEVhZlVZR0ZTQmxaZ1labXhjQnhXdDJOUUVvdVlDQmQzd
     * zIzenN1eG9WdmZXWlJ6T1F1VlY2aWZ4ejhuTE9JQk4yenVkDQpvM0s2NS9BT0VueEdVcndWbzdwTmV6bVFJT2RMd2RxTjVrV1BGWlFrQ2tNSHQ1b0J2R
     * URGVko2eU1PYWRpVXFhdEgwUVErNkZvK1grDQpsTG93cXlWM1cwMGQzdnM4Q05pRElOdkZoV09RRW5SN01QMEsxTkViRGR6blprSG9Ib3Y4dWhQMEpSa
     * HpvOUxKTFk3OU9MK2lqYmcxDQphbkhvVVlLdk1BeVdkTmIvS3BaWVNreTUxVk95OWNZSFhxSUwySmk5UGNNbnRPem5zREF3Tng0QmJvb0NtU05ya1phR
     * EU2WjE3Wm1nDQpOR3FLTGkrT1J3Znp4bm1qY2VyRzB0dWh1UllLVnFZRFhQZStLN2tnaVZpYTdXeXRFWnNuTzBaOHZhbkxYRWtOZzI3alFqY1dXZkduD
     * QpJL3lweE5TcDhNQUVKN2NXbGRjTjZYdmF4UVJlWmdiYmY2azJpVHV1K3ZwT3dBNklSdnNPNkZPbXFKeEpnczNkWWJiUCtKbHFGR3BNDQpLb09wUW1UT
     * FowRjhwUmJzUk9PKzNTaVFoUWM3SDRBM1d5SG1ubE5IK3RlOUVIeVhJM0YyRUpCa2o4VEdlNTBva1d4VVN4UHhmQlE2DQpOV2ZBZzZoNk9oVVM0TGxpR
     * VNzSldURDRyeithVElwdnlzcStNN2J2SDBaTFlWaDAvR0VyeXc9PQ0KLS0tLS1FTkQgRU5DUllQVEVEIERBVEEtLS0tLQ0K",
     * "content_type":"message/rfc822"}
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
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions tag_certificate_requests:delete
     * @mtwContentTypeReturned N/A
     * @mtwMethodType DELETE
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/tag-certificate-requests/68e91a2c-a74f-4969-b1d8-a07e91afa0d9
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  CertificateRequests client = new CertificateRequests(My.configuration().getClientProperties());
     *  client.deleteCertificateRequest(UUID.valueOf("68e91a2c-a74f-4969-b1d8-a07e91afa0d9"));
     * </pre>
     */
    public void deleteCertificateRequest(UUID uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", uuid);
        Response obj = getTarget().path("tag-certificate-requests/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).delete();
        if( !obj.getStatusInfo().getFamily().equals(Family.SUCCESSFUL)) {
            throw new WebApplicationException("Delete certificate request failed");
        }
    }

    /**
     * Deletes the Certificate requests matching the specified search criteria. 
     * @param criteria CertificateRequestFilterCriteria object specifying the search criteria. The search options include
     * id, subjectEqualTo, subjectContains, statusEqualTo and contentTypeEqualTo.
     * @return N/A
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions tag_certificate_requests:delete,search
     * @mtwContentTypeReturned N/A
     * @mtwMethodType DELETE
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/tag-certificates?subjectEqualTo=064866ea-620d-11e0-b1a9-001e671043c4
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  Certificates client = new Certificates(My.configuration().getClientProperties());
     *  CertificateFilterCriteria criteria = new CertificateFilterCriteria();
     *  criteria.subjectEqualTo = "064866ea-620d-11e0-b1a9-001e671043c4";
     *  client.deleteCertificate(criteria);
     * </pre>
     */
    public void deleteCertificate(CertificateRequestFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        Response obj = getTargetPathWithQueryParams("tag-certificates", criteria).request(MediaType.APPLICATION_JSON).delete();
        
        if( !obj.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
            throw new WebApplicationException("Delete Certificate Request failed");
        }
    }
    
    /**
     * Allows the user to update the status of the certificate request after the external CA has completed
     * processing the request. After the certificate has been created and stored in the system, the status
     * of the corresponding certificate request has to be updated.
     * @param obj CertificateRequest object having the status and the Id of the request that needs to be updated. 
     * @return Updated CertificateRequest object.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions tag_certificate_requests:store
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType PUT
     * @mtwSampleRestCall
     * https://server.com:8181/mtwilson/v2/tag-certificate-requests/68e91a2c-a74f-4969-b1d8-a07e91afa0d9
     * Input: {"status":"APPROVED"}
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
     * Retrieves the Certificate request details for the specified request ID. 
     * @param uuid - UUID of the certificate request that needs to be retrieved
     * @return CertificateRequest object matching the specified UUID.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions tag_certificate_requests:retrieve
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/tag-certificate-requests/07217f9c-f625-4c5a-a538-73f1880abdda
     * Output: {"id":"68e91a2c-a74f-4969-b1d8-a07e91afa0d9","subject":"064866ea-620d-11e0-b1a9-001e671043c4","status":"New",
     * "content":"LS0tLS1CRUdJTiURUQgREFUQS0tLS0tD...EIERBVEEtLS0tLQ0K","content_type":"message/rfc822"}
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
     * @param criteria CertificateRequestFilterCriteria object specifying the filter criteria. Search options include
     * id, subjectEqualTo, subjectContains, statusEqualTo & contentTypeEqualTo.
     * If the user wants to retrieve all the records, filter=false criteria can be specified. This would override any
     * other filter criteria that the user would have specified.
     * @return CertificateRequestCollection object with the list of all the CertificateRequest objects matching the specified filter criteria
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions tag_certificate_requests:search
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/tag-certificate-requests?statusEqualTo=approved
     * Output: {"certificate_requests":[{"id":"68e91a2c-a74f-4969-b1d8-a07e91afa0d9","subject":"064866ea-620d-11e0-b1a9-001e671043c4",
     * "status":"APPROVED","content":"LS0tLS1CRUdJTiBFTkNSWVBURUQgREFUQS0tLS0tDQpDb250ZW50LUVuY29kaW5nOiBiYXNlNjQNCkVuY3J5cHRpb24tQWxnb
     * 3JpdGhtOiBBRVMvQ0JDL1BLQ1M1UGFkZGluZw0KRW5jcnlwdGlvbi1LZXktSWQ6IGtXanFsL1dNYkhRPTpJcnVMT0tSSVpPTCtTRFZtY0UxdVdkR3ZwdXY0K0NGSkcxT
     * Fc1MHIrbzVrPQ0KSW50ZWdyaXR5LUFsZ29yaXRobTogU0hBMjU2DQpLZXktQWxnb3JpdGhtOiBQQktERjJXaXRoSG1hY1NIQTE7IGl0ZXJhdGlvbnM9MTAwMDsga2V5
     * LWxlbmd0aD0xMjg7IHNhbHQtYnl0ZXM9OA0KDQpFSzQ1cXZiYXBXNS84QUlYdldtMUJnZktwS0xLNWNIOXBUUnNydVJYWXlIUXFmaU5jY3hVNXVzbUdnelBYVEhmWmJ
     * lbHp6aDh2a25tDQoxcDlNeUVwb3VpeFNHTGdNN3RiTjRiR2VNSFdFTlpQVGVHKzJvakZ4K1pFTGx2SVlsS2d3M01FRmxDWFlXVFhNRWlqbEIzMW1LdzU0DQpMRnlua0
     * RWNGhaYmhqVWFHcFl2aGluU2lIMzYrdElON3d0Ymw3QmpuNGk0Nkg2L1c0RGlLeTJ1NGpTWTFxWW05YlR6UHdIZkxVcmRuDQpEWVNsbmZxRWRJOFpoekluU2o0dXdUOU9
     * TY2xOZndqSENPT1BUNThaSHh3YWgwdWo3ZUdIUi9aakVCd2FicjRENVkwT240UkZEeWZMDQpSemNtaEFLcnJlZUowbkJKYmRhS3VaVklDSDJsOXlvWUJ5eTkzZU91aDRuS
     * GRSRk9GUmh3Ni94WmlFeE9SOXk3WmFmNlhiVTh2N0l2DQo1TVFtVVZrKzFkTEEyY2liV3ZaZTBJV3JPbmlkR01wSEY1d1J6OHVDQitiRzg2SGJRSkNHNWR3bklzYkozOTF
     * HZUltMERWWmtiS0xQDQo1OVN3czJVUmhsRFg5MTVSL29abmhkVVRzVzFML3JTUm1HZEFMRVIxU2E1OUYrTGg4dXlZelhKbFU3dUVHQ2dXSlZ2RzMxRHBLRDhsDQpLZ3JEe
     * kpGNmxuZFloV3oycE44dVM0N0M2Q1p5VnRpQXNpUGc3Wi83eXBhOGE5cW5BelZ1WG54cWg3SDBhQnNsUkZkaHhVM0hkSU5WDQphT1NvWHpMdTVFUEVXV05iVEo4V3d2SUc
     * yWmh6MXhnazFKNGhMbkxBWEh3NzdMQTREblNJU1pMY3hiZDFiM3g3TCtvVFUzb21WUVV6DQpidkdyeVJ6NEVhZlVZR0ZTQmxaZ1labXhjQnhXdDJOUUVvdVlDQmQzdzIze
     * nN1eG9WdmZXWlJ6T1F1VlY2aWZ4ejhuTE9JQk4yenVkDQpvM0s2NS9BT0VueEdVcndWbzdwTmV6bVFJT2RMd2RxTjVrV1BGWlFrQ2tNSHQ1b0J2RURGVko2eU1PYWRpVXF
     * hdEgwUVErNkZvK1grDQpsTG93cXlWM1cwMGQzdnM4Q05pRElOdkZoV09RRW5SN01QMEsxTkViRGR6blprSG9Ib3Y4dWhQMEpSaHpvOUxKTFk3OU9MK2lqYmcxDQphbkhvVV
     * lLdk1BeVdkTmIvS3BaWVNreTUxVk95OWNZSFhxSUwySmk5UGNNbnRPem5zREF3Tng0QmJvb0NtU05ya1phREU2WjE3Wm1nDQpOR3FLTGkrT1J3Znp4bm1qY2VyRzB0dWh1U
     * llLVnFZRFhQZStLN2tnaVZpYTdXeXRFWnNuTzBaOHZhbkxYRWtOZzI3alFqY1dXZkduDQpJL3lweE5TcDhNQUVKN2NXbGRjTjZYdmF4UVJlWmdiYmY2azJpVHV1K3ZwT3dBN
     * klSdnNPNkZPbXFKeEpnczNkWWJiUCtKbHFGR3BNDQpLb09wUW1UTFowRjhwUmJzUk9PKzNTaVFoUWM3SDRBM1d5SG1ubE5IK3RlOUVIeVhJM0YyRUpCa2o4VEdlNTBva1d4V
     * VN4UHhmQlE2DQpOV2ZBZzZoNk9oVVM0TGxpRVNzSldURDRyeithVElwdnlzcStNN2J2SDBaTFlWaDAvR0VyeXc9PQ0KLS0tLS1FTkQgRU5DUllQVEVEIERBVEEtLS0tLQ0K",
     * "content_type":"message/rfc822"}]}
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
