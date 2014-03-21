/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.client.jaxrs;

import java.net.URL;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
public class MtWilsonImportTagCertificate extends MtWilsonClient {
    
    Logger log = LoggerFactory.getLogger(getClass().getName());

    public MtWilsonImportTagCertificate(URL url) {
        //super(url);
    }

    public MtWilsonImportTagCertificate(Properties properties) throws Exception {
        super(properties);
    }
    
    /**
     * This function imports the specified certificate into Mt.Wilson. If the host is 
     * already registered with Mt.Wilson, the certificate would be automatically mapped to the host.
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
    public void mtwilsonImportTagCertificate(String host, String selections) {
        return;
    }
        
}
