/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.client.jaxrs;


/**
 *
 * @author ssbangal
 */
public class RevokeTagCertificate {
        
    /**
     * This function revokes the specified certificate and sends the revocation information to
     * Mt.Wilson. 
     * <p>
     * <i><u>Roles Needed:</u></i> AssetManagement
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML<br>
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: POST</i><br>
     * https://192.168.1.101:8181/mtwilson/v2/rpc/revoke_tag_certificate
     * <p>
     * <i>Sample Input</i><br>
     * {"certificate_id":"a6544ff4-6dc7-4c74-82be-578592e7e3ba"}
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * None
     * <p>
     * @since Mt.Wilson 2.0
     */
    public void revokeTagCertificate(String host, String selections) {
        return;
    }
        
}
