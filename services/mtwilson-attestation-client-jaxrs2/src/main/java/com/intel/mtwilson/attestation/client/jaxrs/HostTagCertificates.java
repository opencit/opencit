/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.attestation.client.jaxrs;

import com.intel.mtwilson.jaxrs2.client.MtWilsonClient;
import com.intel.dcsg.cpg.tls.policy.TlsConnection;
import com.intel.mtwilson.as.rest.v2.model.TagCertificate;
import com.intel.mtwilson.as.rest.v2.model.TagCertificateCollection;
import com.intel.mtwilson.as.rest.v2.model.TagCertificateFilterCriteria;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
public class HostTagCertificates extends MtWilsonClient {
    
    Logger log = LoggerFactory.getLogger(getClass().getName());

    public HostTagCertificates(URL url) throws Exception{
        super(url);
    }

    public HostTagCertificates(Properties properties) throws Exception {
        super(properties);
    }
    
    public HostTagCertificates(Properties properties, TlsConnection tlsConnection) throws Exception {
        super(properties, tlsConnection);
    }    
           
    /**
     * Imports the tag certificate into the system and associates the same to the host for which the tag certificate was created if the has already
     * been registered in the system. If the host is not already registered, certificate would be imported and will not be associated with any hosts.
     * @param obj TagCertificate object with the details of the certificate to be imported. The subject of the certificate would have the hardware UUID of
     * the host for which the certificate was generated. 
     * @return TagCertificate created in the system.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions tag_certificates:import
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType POST
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/host-tag-certificates
     * Input: {"certificate":"MIICMjCCARoCAQEwH6EdpBswGTEXMBUGAWkEEAZIZupiDRHgsakAHmcQQ8SgIDAepBwwGjEYMBYG....ic="} 
     * Output: {"id":"e43424ca-9e00-4cb9-b038-9259d0307888","certificate":"MIICMjCCARoCAQEwH6EdpBswGTEXMBUGAWkEEAZIZupiDRHgsakAHmcQQ8SgIDAepBwwGjEYMBYG....ic="}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     * HostTagCertificates client = new HostTagCertificates(My.configuration().getClientProperties());
     * TagCertificate obj = new TagCertificate();
     * String attrCert = "MIICMjCCARoCAQEwH6EdpBswGTEXMBUGAWkEEAZIZupiDRHgsakAHmcQQ8SgIDAepBwwGjEYMBYG....ic=";
     * obj.setCertificate((Base64.decodeBase64(attrCert.getBytes())));
     * TagCertificate createTagCertificate = client.createHostTagCertificate(obj);
     * </pre>
     */    
    public TagCertificate createHostTagCertificate(TagCertificate obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        TagCertificate newObj = getTarget().path("host-tag-certificates").request().accept(MediaType.APPLICATION_JSON).post(Entity.json(obj), TagCertificate.class);
        return newObj;
    }

    /**
     * Deletes the mapping between the host and the tag certificate with which the host has been associated with.
     * @param uuid - UUID of the mapping created. 
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions tag_certificates:revoke
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType DELETE
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/host-tag-certificates/e43424ca-9e00-4cb9-b038-9259d0307888
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     * HostTagCertificates client = new HostTagCertificates(My.configuration().getClientProperties());
     * client.deleteHostTagCertificate("e43424ca-9e00-4cb9-b038-9259d0307888");
     * </pre>
     */    
    public void deleteHostTagCertificate(String uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", uuid);
        Response obj = getTarget().path("host-tag-certificates/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).delete();
        log.debug(obj.toString());
    }

    /**
     * Retrieves the details of the tag certificate associated with host for the specified UUID.
     * @param uuid - UUID of the mapping created. 
     * @return TagCertificate matching the specified UUID.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions tag_certificates:retrieve
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/host-tag-certificates/e43424ca-9e00-4cb9-b038-9259d0307888
     * Output: {"id":"e43424ca-9e00-4cb9-b038-9259d0307888","certificate":"MIICMjCCARoCAQEwH6EdpBswGTEXMBUGAWkEEAZIZupiDRHgsakAHmcQQ8SgIDAepBwwGjEYMBYG....ic="}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     * HostTagCertificates client = new HostTagCertificates(My.configuration().getClientProperties());
     * TagCertificate retrieveTagCertificate = client.retrieveHostTagCertificate("6d0bbcf9-b662-4d59-bc71-7b360afeb94a");
     * </pre>
    */    
    public TagCertificate retrieveHostTagCertificate(String uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", uuid);
        TagCertificate obj = getTarget().path("host-tag-certificates/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(TagCertificate.class);
        return obj;
    }

    /**
     * Searches for the tag certificates in the system based on the specified criteria.
     * @param criteria TagCertificateFilterCriteria object that specifies the search criteria.
     * The possible search options include id, & hostUuid.
     * @return TagCertificateCollection object with a list of tag certificates that match the search criteria.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions tag_certificates:search
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/host-tag-certificates?hostUuid=064866ea-620d-11e0-b1a9-001e671043c4
     * Output: {"tag_certificates":[{"id":"921d9ff7-33d8-425c-8164-c1fd8fa33f19",
     * "certificate":"MIICMjCCARoCAQEwH6EdpBswGTEXMBUGAWkEEAZIZupiDRHgsakAHmcQQ8...n3GG0BVGDoIg="}]}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     * HostTagCertificates client = new HostTagCertificates(My.configuration().getClientProperties());
     * TagCertificateFilterCriteria criteria = new TagCertificateFilterCriteria();
     * criteria.hostUuid = UUID.valueOf("6d0bbcf9-b662-4d59-bc71-7b360afeb94a");
     * TagCertificateCollection objCollection = client.searchHostTagCertificates(criteria);
     * </pre>
     */    
    public TagCertificateCollection searchHostTagCertificates(TagCertificateFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        TagCertificateCollection objCollection = getTargetPathWithQueryParams("host-tag-certificates", criteria).request(MediaType.APPLICATION_JSON).get(TagCertificateCollection.class);
        return objCollection;
    }
    
}
