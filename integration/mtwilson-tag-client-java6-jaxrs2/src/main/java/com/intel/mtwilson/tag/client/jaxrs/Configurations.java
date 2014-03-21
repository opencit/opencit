/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.client.jaxrs;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.tag.model.Configuration;
import com.intel.mtwilson.tag.model.ConfigurationCollection;
import com.intel.mtwilson.tag.model.ConfigurationFilterCriteria;
import java.net.URL;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
public class Configurations extends MtWilsonClient {
    
    Logger log = LoggerFactory.getLogger(getClass().getName());

    public Configurations(URL url) {
        //super(url);
    }

    public Configurations(Properties properties) throws Exception {
        super(properties);
    }
    
    /**
     * Retrieves the details of the configuration based on the search criteria specified. If none
     * of the search criteria is specified, then search would return back and empty result set. The 
     * possible search options include nameEqualTo and nameContains.  
     * The search always returns back a collection.
     * <p>
     * <i><u>Roles Needed:</u></i> AssetManagement
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML<br>
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: GET</i><br>
     * https://192.168.1.101:8181/mtwilson/v2/configurations?nameEqualTo=main
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * {"configurations":[{"id":"5330ead0-7ce4-4eac-a68f-e249052ea9aa","name":"main",
     * "content":{"allowTagsInCertificateRequests":"true","approveAllCertificateRequests":"true","allowAutomaticTagSelection":"true",
     * "automaticTagSelectionName":"d16034fe-1f3f-4648-a305-4b7f90aa213f"}}]}
     * <p>
     * @since Mt.Wilson 2.0
     */
    public ConfigurationCollection searchConfigurations(ConfigurationFilterCriteria criteria) {
        return null;
    }
    
    /**
     * Retrieves the details of the configuration with for the specified ID. Note
     * that the ID should be a valid UUID.
     * <p>
     * <i><u>Roles Needed:</u></i> AssetManagement
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: GET</i><br>
     * https://192.168.1.101:8181/mtwilson/v2/configurations/5330ead0-7ce4-4eac-a68f-e249052ea9aa
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * {"id":"5330ead0-7ce4-4eac-a68f-e249052ea9aa","name":"main",
     * "content":{"allowTagsInCertificateRequests":"true","approveAllCertificateRequests":"true",
     * "allowAutomaticTagSelection":"true","automaticTagSelectionName":"d16034fe-1f3f-4648-a305-4b7f90aa213f"}}
     * <p>
     * @since Mt.Wilson 2.0
     */
    public Configuration retrieveConfiguration(UUID id) {
        return null;
    }

    /**
     * Creates a new configuration item using the list of properties specified.
     * <p>
     * <i><u>Roles Needed:</u></i> AssetManagement
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: POST</i><br>
     * https://192.168.1.101:8181/mtwilson/v2/configurations
     * <p>
     * <i>Sample Input</i><br>
     * {"name":"backup","content":{"allowTagsInCertificateRequests":"true","approveAllCertificateRequests":"true",
     * "allowAutomaticTagSelection":"true","automaticTagSelectionName":"d16034fe-1f3f-4648-a305-4b7f90aa213f"}}
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * {"id":"1230ead0-7ce4-4eac-a68f-e249052ea9aa","name":"backup",
     * "content":{"allowTagsInCertificateRequests":"true","approveAllCertificateRequests":"true",
     * "allowAutomaticTagSelection":"true","automaticTagSelectionName":"d16034fe-1f3f-4648-a305-4b7f90aa213f"}}
     * <p>
     * @since Mt.Wilson 2.0
     */
    public Configuration createConfiguration(Configuration obj) {
        return null;
    }

    /**
     * Allows the user to edit an existing key value pair. Note that only the value of the key-value
     * pair can be edited. The user has to specify the ID on the query string and the value to be updated
     * in the body. If the specified ID does not exist in the system, appropriate error would be returned
     * back to the caller. 
     * After successful execution the caller would be returned back the object updated.
     * <p>
     * <i><u>Roles Needed:</u></i> AssetManagement
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: PUT</i><br>
     * https://192.168.1.101:8181/mtwilson/v2/configurations/1230ead0-7ce4-4eac-a68f-e249052ea9aa
     * <p>
     * <i>Sample Input</i><br>
     * {"name":"backup","content":{"allowTagsInCertificateRequests":"false","approveAllCertificateRequests":"true",
     * "allowAutomaticTagSelection":"false","automaticTagSelectionName":"d16034fe-1f3f-4648-a305-4b7f90aa213f"}}
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * {"id":"1230ead0-7ce4-4eac-a68f-e249052ea9aa","name":"backup",
     * "content":{"allowTagsInCertificateRequests":"false","approveAllCertificateRequests":"true",
     * "allowAutomaticTagSelection":"false","automaticTagSelectionName":"d16034fe-1f3f-4648-a305-4b7f90aa213f"}}
     * <p>
     * @since Mt.Wilson 2.0
     */
    public Configuration editConfiguration(Configuration obj) {
        return null;
    }

    /**
     * Deletes the configuration details with the specified ID. 
     * <p>
     * <i><u>Roles Needed:</u></i> AssetManagement
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: DELETE</i><br>
     * https://192.168.1.101:8181/mtwilson/v2/configurations/1230ead0-7ce4-4eac-a68f-e249052ea9aa
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * NA
     * <p>
     * @since Mt.Wilson 2.0
     */
    public void deleteConfiguration(UUID id) {
        return;
    }
    
}
