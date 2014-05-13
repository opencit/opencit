/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.client.jaxrs;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.tag.model.Certificate;
import com.intel.mtwilson.tag.model.CertificateCollection;
import com.intel.mtwilson.tag.model.CertificateFilterCriteria;

/**
 *
 * @author ssbangal
 */
public class Certificates {
        
    
    /**
     * Creates a new certificate entry into the database that can be provisioned for the host. Note that the
     * certificate subject has to have the hardware uuid of the host to which the certificate has to be
     * provisioned. The UUID can be obtained using the dmidecode command.
     * <p>
     * <i><u>Roles Needed:</u></i> AssetManagement
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: POST</i><br>
     * https://192.168.1.101:8181/mtwilson/v2/tag-certificates
     * <p>
     * <i>Sample Input</i><br>
     * {"certificate":"MIIO....ic=","sha1":"7704753ac4a8771499610352f28967e39c75d88b",
     * "sha256":"09740b068caba9e8647488d3e5a1a546e136c47cffcc30198a4446c765e344e0","subject":"2676ee69-e42f-461b-824f-a6ec3d2c08f4",
     * "issuer":"CN=ExternalCA","not_before":"2014-03-21","not_after":"2015-03-21","revoked":false}
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * {"id":"187ec902-c6c6-4dfb-adb4-f240099aa4b0","certificate":"MIIO....ic=","sha1":"7704753ac4a8771499610352f28967e39c75d88b",
     * "sha256":"09740b068caba9e8647488d3e5a1a546e136c47cffcc30198a4446c765e344e0","subject":"2676ee69-e42f-461b-824f-a6ec3d2c08f4",
     * "issuer":"CN=Intel","not_before":"2014-03-21","not_after":"2015-03-21","revoked":true}
     * <p>
     * @since Mt.Wilson 2.0
     */
    public Certificate createCertificate(Certificate obj) {
        return null;
    }

    /**
     * Deletes the specified certificate from the system.  
     * <p>
     * <i><u>Roles Needed:</u></i> AssetManagement
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: DELETE</i><br>
     * https://192.168.1.101:8181/mtwilson/v2/tag-certificates/187ec902-c6c6-4dfb-adb4-f240099aa4b0
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * NA
     * <p>
     * @since Mt.Wilson 2.0
     */
    public void deleteCertificate(UUID id) {
        return;
    }
    
    /**
     * Allows the user to edit the revoked status of the certificate. No other information can be edited.
     * that the ID should be a valid UUID.
     * <p>
     * <i><u>Roles Needed:</u></i> AssetManagement
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: GET</i><br>
     * https://192.168.1.101:8181/mtwilson/v2/tag-certificates/187ec902-c6c6-4dfb-adb4-f240099aa4b0
     * <p>
     * <i>Sample Input</i><br>
     * {"revoked":true}
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * {"id":"187ec902-c6c6-4dfb-adb4-f240099aa4b0","revoked":true}
     * <p>
     * @since Mt.Wilson 2.0
     */    
    public Certificate editCertificate(Certificate obj) {
        return null;
    }

    /**
     * Retrieves the details of the Certificate for the specified ID. Note
     * that the ID should be a valid UUID.
     * <p>
     * <i><u>Roles Needed:</u></i> AssetManagement
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: GET</i><br>
     * https://192.168.1.101:8181/mtwilson/v2/tag-certificates/187ec902-c6c6-4dfb-adb4-f240099aa4b0
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * {"id":"187ec902-c6c6-4dfb-adb4-f240099aa4b0","certificate":"MIIO....ic=","sha1":"7704753ac4a8771499610352f28967e39c75d88b",
     * "sha256":"09740b068caba9e8647488d3e5a1a546e136c47cffcc30198a4446c765e344e0","subject":"2676ee69-e42f-461b-824f-a6ec3d2c08f4",
     * "issuer":"CN=Intel","not_before":"2014-03-21","not_after":"2015-03-21","revoked":true}
     * <p>
     * @since Mt.Wilson 2.0
     */
    public Certificate retrieveCertificate(UUID id) {
        return null;
    }    
        
    /**
     * Retrieves the details of the provisioned certificates based on the search criteria specified. If none
     * of the search criteria is specified, then search would return back and empty result set. The 
     * possible search options include subjectEqualTo, subjectContains, issuerEqualTo, issuerContains, 
     * sha1, sha256, notBefore, notAfter and revoked.  
     * The search always returns back a collection.
     * <p>
     * <i><u>Roles Needed:</u></i> AssetManagement
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML<br>
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: GET</i><br>
     * https://192.168.1.101:8181/mtwilson/v2/tag-certificates?issuer=Intel
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * {"certificates":[{"id":"187ec902-c6c6-4dfb-adb4-f240099aa4b0","certificate":"MIICMj...BYG=","sha1":"7704753ac4a8771499610352f28967e39c75d88b",
     * "sha256":"09740b068caba9e8647488d3e5a1a546e136c47cffcc30198a4446c765e344e0","subject":"2676ee69-e42f-461b-824f-a6ec3d2c08f4",
     * "issuer":"CN=Intel","not_before":1395407513000,"not_after":1426943513000,"revoked":true}]}
     * <p>
     * @since Mt.Wilson 2.0
     */
    public CertificateCollection searchCertificates(CertificateFilterCriteria criteria) {
        return null;
    }
    
}
