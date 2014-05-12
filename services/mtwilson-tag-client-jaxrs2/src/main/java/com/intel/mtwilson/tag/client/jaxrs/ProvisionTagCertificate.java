/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.client.jaxrs;

/**
 *
 * @author ssbangal
 */
public class ProvisionTagCertificate {
        
    /**
     * This function creates a new certificate with the provided key(attribute)-value pairs for the
     * specified host. Host can be provided either as a IP address or as the hardware UUID, which can
     * be obtained using dmidecode function. If in case the property "tag.provision.autoimport" is set
     * to true, then the created certificate is also imported into the Mt.Wilson system.
     * If the user provides an IP address or the FQDN name for the host, then it is expected to have
     * the host already registered with Mt.Wilson.
     * <br>
     * The host information (UUID/IP address) should be specified on the query string.
     * <br>
     * The key-value pairs that need to be added into the certificate should be provided either as
     * JSON or XML in the body. Accordingly the content type has to be provided. The user can use
     * the GET method on a specific Selection to get this information.
     * <p>
     * <i><u>Roles Needed:</u></i> AssetManagement
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML<br>
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: POST</i><br>
     * https://192.168.1.101:8181/mtwilson/v2/tag-certificate-requests-rpc/provision?subject=192.168.0.1
     * or
     * https://192.168.1.101:8181/mtwilson/v2/tag-certificate-requests-rpc/provision?subject=2676ee69-e42f-461b-824f-a6ec3d2c08f4
     * <p>
     * <i>Sample XML Input</i><br>
     * {@code <selections xmlns="urn:mtwilson-tag-selection"><selection>
     * <attribute oid="2.5.4.789.1"><text>country=US</text></attribute>
     * <attribute oid="2.5.4.789.1"><text>state=CA</text></attribute>
     * <attribute oid="2.5.4.789.1"><text>city=Folsom</text></attribute>
     * <attribute oid="2.5.4.789.1"><text>city=Santa Clara</text></attribute>
     * <attribute oid="2.5.4.789.1"><text>customer=Coke</text></attribute>
     * <attribute oid="2.5.4.789.1"><text>customer=Pepsi</text></attribute>
     * </selection></selections> }
     * <p>
     * <i>Sample JSON Input</i><br>
     * {"selections":[{"attributes":[{"text":{"value":"state=CA"},"oid":"2.5.4.789.1"},{"text":{"value":"customer=Coke"},"oid":"2.5.4.789.1"},
     * {"text":{"value":"country=US"},"oid":"2.5.4.789.1"},{"text":{"value":"city=Folsom"},"oid":"2.5.4.789.1"},{"text":{"value":"customer=Pepsi"},
     * "oid":"2.5.4.789.1"},{"text":{"value":"city=Santa Clara"},"oid":"2.5.4.789.1"}]}]}
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * The byte array of the certificate created.
     * <p>
     * @since Mt.Wilson 2.0
     */
    public byte[] provisionTagCertificate(String host, String selections) {
        return null;
    }
        
}
