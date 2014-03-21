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
public class ApproveTagCertificateRequest extends MtWilsonClient {
    
    Logger log = LoggerFactory.getLogger(getClass().getName());

    public ApproveTagCertificateRequest(URL url) {
        //super(url);
    }

    public ApproveTagCertificateRequest(Properties properties) throws Exception {
        super(properties);
    }
    
    /**
     * This function provided the certificate request ID, and the certificate associated to it (provided
     * by an external CA), stores the certificate details in the database and updates the request status to
     * completed. Here it is assumed that the external CA would have added the required attributes in the
     * certificate it generated.
     * <p>
     * <i><u>Roles Needed:</u></i> AssetManagement
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML<br>
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: POST</i><br>
     * https://192.168.1.101:8181/mtwilson/v2/approve_tag_certificate_request
     * <p>
     * <i>Sample Input</i><br>
     * 
     * </selection></selections>
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * 
     * <p>
     * @since Mt.Wilson 2.0
     */
    public byte[] provisionTagCertificate(String host, String selections) {
        return null;
    }
        
}
