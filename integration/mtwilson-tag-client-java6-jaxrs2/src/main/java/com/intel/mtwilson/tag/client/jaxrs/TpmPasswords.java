/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.client.jaxrs;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.tag.model.TpmPassword;
import com.intel.mtwilson.tag.model.TpmPasswordCollection;
import com.intel.mtwilson.tag.model.TpmPasswordFilterCriteria;

/**
 *
 * @author ssbangal
 */
public class TpmPasswords {
    
    
    /**
     * Retrieves the TPM password based on the search criteria specified. If none
     * of the search criteria is specified, then search would return back and empty result set. The 
     * only search option currently supported is the ID.  
     * <p>
     * <i><u>Roles Needed:</u></i> AssetManagement
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML<br>
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: GET</i><br>
     * https://192.168.1.101:8181/mtwilson/v2/host-tpm-passwords?id=07217f9c-f625-4c5a-a538-73f1880abdda
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * {"id":"07217f9c-f625-4c5a-a538-73f1880abdda","password":"Password"}
     * <p>
     * @since Mt.Wilson 2.0
     */
    public TpmPasswordCollection searchTpmPasswords(TpmPasswordFilterCriteria criteria) {
        return null;
    }
    
    /**
     * Retrieves the TPM password value for the specified ID. Note
     * that the ID should be a valid UUID.
     * <p>
     * <i><u>Roles Needed:</u></i> AssetManagement
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: GET</i><br>
     * https://192.168.1.101:8181/mtwilson/v2/host-tpm-passwords/07217f9c-f625-4c5a-a538-73f1880abdda
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * {"id":"07217f9c-f625-4c5a-a538-73f1880abdda","password":"Password"}
     * <p>
     * @since Mt.Wilson 2.0
     */
    public TpmPassword retrieveTpmPassword(UUID id) {
        return null;
    }

    /**
     * Creates a new TPM password entry for the host. The ID that should be specified to create the
     * entry should be the host's hardware UUID. The hardware UUID can be obtained by running the
     * dmidecode command.
     * <p>
     * <i><u>Roles Needed:</u></i> AssetManagement
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: POST</i><br>
     * https://192.168.1.101:8181/mtwilson/v2/host-tpm-passwords
     * <p>
     * <i>Sample Input</i><br>
     * {"id":"07217f9c-f625-4c5a-a538-73f1880abdda","password":"Password"}
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * {"id":"07217f9c-f625-4c5a-a538-73f1880abdda","password":"Password"}
     * <p>
     * @since Mt.Wilson 2.0
     */
    public TpmPassword createTpmPassword(TpmPassword obj) {
        return null;
    }

    /**
     * Allows the user to update the TPM password for the specified ID, which is the
     * host's hardware UUID. 
     * <p>
     * <i><u>Roles Needed:</u></i> AssetManagement
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: PUT</i><br>
     * https://192.168.1.101:8181/mtwilson/v2/host-tpm-passwords/07217f9c-f625-4c5a-a538-73f1880abdda
     * <p>
     * <i>Sample Input</i><br>
     * "password":"Password123"
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * {"id":"07217f9c-f625-4c5a-a538-73f1880abdda","password":"Password123"}
     * <p>
     * @since Mt.Wilson 2.0
     */
    public TpmPassword editTpmPassword(TpmPassword obj) {
        return null;
    }

    /**
     * Deletes the TPM password entry for the specified ID. 
     * <p>
     * <i><u>Roles Needed:</u></i> AssetManagement
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: DELETE</i><br>
     * https://192.168.1.101:8181/mtwilson/v2/host-tpm-passwords/07217f9c-f625-4c5a-a538-73f1880abdda
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * NA
     * <p>
     * @since Mt.Wilson 2.0
     */
    public void deleteTpmPassword(UUID id) {
        return;
    }
    
}
