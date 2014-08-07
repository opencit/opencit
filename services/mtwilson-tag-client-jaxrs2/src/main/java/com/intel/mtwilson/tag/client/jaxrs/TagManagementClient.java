/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.client.jaxrs;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.tls.policy.TlsConnection;
import com.intel.mtwilson.jaxrs2.client.MtWilsonClient;
import com.intel.mtwilson.tag.model.*;
import java.util.Properties;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author ssbangal
 */
public class TagManagementClient extends MtWilsonClient {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TagManagementClient.class);
    
    public TagManagementClient(Properties properties) throws Exception {
        super(properties);
    }
    public TagManagementClient(Properties properties, TlsConnection tlsConnection) throws Exception {
        super(properties, tlsConnection);
    }
    
    /**
     * This function creates a new certificate with the provided key(attribute)-value pairs for the
     * specified host. If the property "tag.provision.autoimport" is set to true, then the created 
     * certificate is also imported into the Mt.Wilson system.
     * @param hostHardwareUuid - If the host is already registered with the system, then the search 
     * method can be used on the HostUuid resource to retrieve the hardware UUID of the host. It could 
     * also be obtained using the dmidecode command. This information should be specified on the query string.
     * @param selectionXml The key-value pairs that need to be added into the certificate should be provided as
     * XML in the body. The user can call the GET method on the Selections resource to retrieve
     * the already configured selection as XML
     * @return Created Certificate object.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions tag_certificates:create
     * @mtwContentTypeReturned JSON/XML
     * @mtwMethodType POST
     * @mtwSampleRestCall
     * https://server.com:8181/mtwilson/v2/tag-certificate-requests-rpc/provision?subject=064866ea-620d-11e0-b1a9-001e671043c4
     * Input: {"options":null,"default":{"selections":[{"attributes":[{"text":{"value":"city=Folsom"},"oid":"2.5.4.789.1"},
     * {"text":{"value":"customer=Pepsi"},"oid":"2.5.4.789.1"}]}]}}
     * 
     * Output: {"id":"b8249964-f188-4d31-8d67-509c58a48284","certificate":"MIIBzTCBtgIBATAfoR2kGzAZMRcwFQYBaQQQBkhm6mINEeCxqQAeZx
     * BDxKAgMB6kHDAaMRgwFgYDVQQDDA9hc3NldFRhZ1NlcnZpY2UwDQYJKoZIhvcNAQELBQACBgFGR62s4jAiGA8yMDE0MDUyOTExMTE0MloYDzIwMTUwNTI5MTEx
     * MTQyWjAzMBYGBVUEhhUBMQ0MC2NpdHk9Rm9sc29tMBkGBVUEhhUBMRAMDmN1c3RvbWVyPVBlcHNpMA0GCSqGSIb3DQEBCwUAA4IBAQAB1exoWvLgF5yQvZngbP
     * HOw9BjTaTkcXR5v6KohuTGlYB9rseCbJ9gvFef7XrNLz4hwjGdJ1W3vsVIsaemRUjcyfGmJ9uMsMAkSP08wmmLQKtcNwb111HAj6LTizyTpA0vO0OK6zk0di7F
     * jWdXeNJ9sOF/blzbtBF9GW/IKnk3I/C+cyEJS6hLIGsz8gSH6mKyQkqhZjMdBo7Rvgfa1Eq/Ok5kpRBr90oMNOIS0udM7hLNVrhrpp761kxivyIQUz6Po72HJ5
     * qHvYOpkxPbuYlt6VWzhJtE5lWiZ2zPsVcg6aoRUySjCBjmOPyMa+NtDjrMkb+wlmnkokpN1vi0ct/a","sha1":"772cc4dd103562e59a78768fb05a3cc0743c4395",
     * "sha256":"22b402f2693064ec61256523eda778365dd3473bd967bca9b063a1628193eed1","subject":"064866ea-620d-11e0-b1a9-001e671043c4",
     * "issuer":"CN=assetTagService","not_before":1401361902000,"not_after":1432897902000,"revoked":false}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  String xmlContent = "<selections xmlns=\"urn:mtwilson-tag-selection\"><default><selection><attribute oid=\"2.5.4.789.1\">
     *          <text>country=US</text></attribute><attribute oid=\"2.5.4.789.1\"><text>state=CA</text></attribute></selection></default></selections>";
     *  TagManagementClient client = new TagManagementClient(getClientProperties());
     *  Certificate cert = client.createOneXml(UUID.valueOf("064866ea-620d-11e0-b1a9-001e671043c4"), xmlContent);
     * </pre>
     */
    public Certificate createOneXml(UUID hostHardwareUuid, String selectionXml) {
        log.debug("target: {}", getTarget().getUri().toString());
        return createOneXml(hostHardwareUuid.toString(), selectionXml);
    }

    /**
     * This function creates a new certificate with the provided key(attribute)-value pairs for the
     * specified host. If the property "tag.provision.autoimport" is set to true, then the created 
     * certificate is also imported into the Mt.Wilson system.
     * @param hostUuidOrIp - Host's hardware UUID or the IP address/FQDN name of the host for which the
     * certificate has to be created. If the IP address/FQDN name is specified, then the host is 
     * expected to be already registered with the system.
     * @param selectionXml The key-value pairs that need to be added into the certificate should be provided as
     * XML in the body. The user can call the GET method on the Selections resource to retrieve
     * the already configured selection as XML
     * @return Created Certificate object.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions tag_certificates:create
     * @mtwContentTypeReturned JSON/XML
     * @mtwMethodType POST
     * @mtwSampleRestCall
     * https://server.com:8181/mtwilson/v2/tag-certificate-requests-rpc/provision?subject=192.168.0.1
     * Input: {"options":null,"default":{"selections":[{"attributes":[{"text":{"value":"city=Folsom"},"oid":"2.5.4.789.1"},
     * {"text":{"value":"customer=Pepsi"},"oid":"2.5.4.789.1"}]}]}}
     * 
     * Output: {"id":"b8249964-f188-4d31-8d67-509c58a48284","certificate":"MIIBzTCBtgIBATAfoR2kGzAZMRcwFQYBaQQQBkhm6mINEeCxqQAeZx
     * BDxKAgMB6kHDAaMRgwFgYDVQQDDA9hc3NldFRhZ1NlcnZpY2UwDQYJKoZIhvcNAQELBQACBgFGR62s4jAiGA8yMDE0MDUyOTExMTE0MloYDzIwMTUwNTI5MTEx
     * MTQyWjAzMBYGBVUEhhUBMQ0MC2NpdHk9Rm9sc29tMBkGBVUEhhUBMRAMDmN1c3RvbWVyPVBlcHNpMA0GCSqGSIb3DQEBCwUAA4IBAQAB1exoWvLgF5yQvZngbP
     * HOw9BjTaTkcXR5v6KohuTGlYB9rseCbJ9gvFef7XrNLz4hwjGdJ1W3vsVIsaemRUjcyfGmJ9uMsMAkSP08wmmLQKtcNwb111HAj6LTizyTpA0vO0OK6zk0di7F
     * jWdXeNJ9sOF/blzbtBF9GW/IKnk3I/C+cyEJS6hLIGsz8gSH6mKyQkqhZjMdBo7Rvgfa1Eq/Ok5kpRBr90oMNOIS0udM7hLNVrhrpp761kxivyIQUz6Po72HJ5
     * qHvYOpkxPbuYlt6VWzhJtE5lWiZ2zPsVcg6aoRUySjCBjmOPyMa+NtDjrMkb+wlmnkokpN1vi0ct/a","sha1":"772cc4dd103562e59a78768fb05a3cc0743c4395",
     * "sha256":"22b402f2693064ec61256523eda778365dd3473bd967bca9b063a1628193eed1","subject":"064866ea-620d-11e0-b1a9-001e671043c4",
     * "issuer":"CN=assetTagService","not_before":1401361902000,"not_after":1432897902000,"revoked":false}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  String xmlContent = "<selections xmlns=\"urn:mtwilson-tag-selection\"><default><selection><attribute oid=\"2.5.4.789.1\">
     *          <text>country=US</text></attribute><attribute oid=\"2.5.4.789.1\"><text>state=CA</text></attribute></selection></default></selections>";
     *  TagManagementClient client = new TagManagementClient(getClientProperties());
     *  Certificate cert = client.createOneXml("192.168.0.1", xmlContent);
     * </pre>
     */
    public Certificate createOneXml(String hostUuidOrIp, String selectionXml) {
        log.debug("target: {}", getTarget().getUri().toString());
        CertificateRequestLocator locator = new CertificateRequestLocator();
        locator.subject = hostUuidOrIp;
        Certificate certificate = 
                getTargetPathWithQueryParams("/tag-certificate-requests-rpc/provision", locator)
                .request()
                .accept(MediaType.APPLICATION_XML)
                .post(Entity.entity(selectionXml, MediaType.APPLICATION_XML), Certificate.class);
        return certificate;
    }

    /**
     * This function creates a new certificate with the provided key(attribute)-value pairs for the
     * specified host. If the property "tag.provision.autoimport" is set to true, then the created 
     * certificate is also imported into the Mt.Wilson system.
     * @param hostUuidOrIp - Host's hardware UUID or the IP address/FQDN name of the host for which the
     * certificate has to be created. If the IP address/FQDN name is specified, then the host is 
     * expected to be already registered with the system.
     * @param selectionXml The key-value pairs that need to be added into the certificate should be provided as
     * JSON/XML in the body. The user can call the GET method on the Selections resource to retrieve
     * the already configured selection as XML/JSON
     * @return Created Certificate object.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions tag_certificates:create
     * @mtwContentTypeReturned JSON/XML
     * @mtwMethodType POST
     * @mtwSampleRestCall
     * https://server.com:8181/mtwilson/v2/tag-certificate-requests-rpc/provision?subject=192.168.0.1
     * Input: {"options":null,"default":{"selections":[{"attributes":[{"text":{"value":"city=Folsom"},"oid":"2.5.4.789.1"},
     * {"text":{"value":"customer=Pepsi"},"oid":"2.5.4.789.1"}]}]}}
     * 
     * Output: {"id":"b8249964-f188-4d31-8d67-509c58a48284","certificate":"MIIBzTCBtgIBATAfoR2kGzAZMRcwFQYBaQQQBkhm6mINEeCxqQAeZx
     * BDxKAgMB6kHDAaMRgwFgYDVQQDDA9hc3NldFRhZ1NlcnZpY2UwDQYJKoZIhvcNAQELBQACBgFGR62s4jAiGA8yMDE0MDUyOTExMTE0MloYDzIwMTUwNTI5MTEx
     * MTQyWjAzMBYGBVUEhhUBMQ0MC2NpdHk9Rm9sc29tMBkGBVUEhhUBMRAMDmN1c3RvbWVyPVBlcHNpMA0GCSqGSIb3DQEBCwUAA4IBAQAB1exoWvLgF5yQvZngbP
     * HOw9BjTaTkcXR5v6KohuTGlYB9rseCbJ9gvFef7XrNLz4hwjGdJ1W3vsVIsaemRUjcyfGmJ9uMsMAkSP08wmmLQKtcNwb111HAj6LTizyTpA0vO0OK6zk0di7F
     * jWdXeNJ9sOF/blzbtBF9GW/IKnk3I/C+cyEJS6hLIGsz8gSH6mKyQkqhZjMdBo7Rvgfa1Eq/Ok5kpRBr90oMNOIS0udM7hLNVrhrpp761kxivyIQUz6Po72HJ5
     * qHvYOpkxPbuYlt6VWzhJtE5lWiZ2zPsVcg6aoRUySjCBjmOPyMa+NtDjrMkb+wlmnkokpN1vi0ct/a","sha1":"772cc4dd103562e59a78768fb05a3cc0743c4395",
     * "sha256":"22b402f2693064ec61256523eda778365dd3473bd967bca9b063a1628193eed1","subject":"064866ea-620d-11e0-b1a9-001e671043c4",
     * "issuer":"CN=assetTagService","not_before":1401361902000,"not_after":1432897902000,"revoked":false}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  String xmlContent = "<selections xmlns=\"urn:mtwilson-tag-selection\"><default><selection><attribute oid=\"2.5.4.789.1\">
     *          <text>country=US</text></attribute><attribute oid=\"2.5.4.789.1\"><text>state=CA</text></attribute></selection></default></selections>";
     *  TagManagementClient client = new TagManagementClient(getClientProperties());
     *  Certificate cert = client.createOneXml("192.168.0.1", xmlContent);
     * </pre>
     */
    public Certificate provisionTagCertificateForHost(String hostUuidOrIp, String selectionXml) {
        log.debug("target: {}", getTarget().getUri().toString());        
        return createOneXml(hostUuidOrIp, selectionXml);
    }

}
