/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.client.jaxrs;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.client.jaxrs.*;
import com.intel.mtwilson.tag.model.KvAttribute;
import com.intel.mtwilson.tag.model.KvAttributeCollection;
import com.intel.mtwilson.tag.model.KvAttributeFilterCriteria;
import java.net.URL;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
public class KvAttributes extends MtWilsonClient {
    
    Logger log = LoggerFactory.getLogger(getClass().getName());

    public KvAttributes(URL url) {
        //super(url);
    }

    public KvAttributes(Properties properties) throws Exception {
        super(properties);
    }
    
    /**
     * Retrieves the keys/attribtes and its corresponding values based on the search criteria specified. If none
     * of the search criteria is specified, then search would return back and empty result set. The 
     * possible search options include nameEqualTo, nameContains, valueEqualTo and valueContains.  
     * The search always returns back a collection.
     * <p>
     * <i><u>Roles Needed:</u></i> AssetManagement
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML<br>
     * User can either specify the content type "Accept: application/json" or provide the same on the url after the
     * resource name. Ex: /tag-kv-attributes.json?nameEqualTo=country or /tag-kv-attributes.xml?nameEqualTo=country.
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: GET</i><br>
     * https://192.168.1.101:8181/mtwilson/v2/tag-kv-attributes?nameEqualTo=country
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * {"kv_attributes":[{"id":"2676ee69-e42f-461b-824f-a6ec3d2c08f4","name":"country","value":"MX"},
     * {"id":"772c1358-feea-4827-bcf1-29cf3ca1a7d9","name":"country","value":"US"}]}
     * <p>
     * @since Mt.Wilson 2.0
     */
    public KvAttributeCollection searchKvAttributes(KvAttributeFilterCriteria criteria) {
        return null;
    }
    
    /**
     * Retrieves the key/attribute and its corresponding value for the specified ID. Note
     * that the ID should be a valid UUID.
     * <p>
     * <i><u>Roles Needed:</u></i> AssetManagement
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: GET</i><br>
     * https://192.168.1.101:8181/mtwilson/v2/tag-kv-attributes/008f2918-92d7-46c4-9b12-c3acbdd08b11
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * {"id":"008f2918-92d7-46c4-9b12-c3acbdd08b11","name":"customer","value":"Customer1"}
     * <p>
     * @since Mt.Wilson 2.0
     */
    public KvAttribute retrieveKvAttribute(UUID id) {
        return null;
    }

    /**
     * Creates a new key value pair. If the key-value pair already exists in the system, an appropriate
     * message would be returned back to the caller. The user can specify the ID, which should be a valid
     * UUID to be used as the primary key. If not specified, a new UUID would be automatically generated.
     * After successful execution the caller would be returned back the new object created.
     * <p>
     * <i><u>Roles Needed:</u></i> AssetManagement
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: POST</i><br>
     * https://192.168.1.101:8181/mtwilson/v2/tag-kv-attributes
     * <p>
     * <i>Sample Input</i><br>
     * {"name":"department","value":"finance"}
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * {"id":"a6544ff4-6dc7-4c74-82be-578592e7e3ba","name":"department","value":"finance"}
     * <p>
     * @since Mt.Wilson 2.0
     */
    public KvAttribute createKvAttribute(KvAttribute obj) {
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
     * https://192.168.1.101:8181/mtwilson/v2/tag-kv-attributes/a6544ff4-6dc7-4c74-82be-578592e7e3ba
     * <p>
     * <i>Sample Input</i><br>
     * {"value":"HR"}
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * {"id":"a6544ff4-6dc7-4c74-82be-578592e7e3ba","value":"HR"}
     * <p>
     * @since Mt.Wilson 2.0
     */
    public KvAttribute editKvAttribute(KvAttribute obj) {
        return null;
    }

    /**
     * Deletes the key value pair with the specified ID. 
     * <p>
     * <i><u>Roles Needed:</u></i> AssetManagement
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: DELETE</i><br>
     * https://192.168.1.101:8181/mtwilson/v2/tag-kv-attributes/a6544ff4-6dc7-4c74-82be-578592e7e3ba
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * NA
     * <p>
     * @since Mt.Wilson 2.0
     */
    public void deleteKvAttribute(UUID id) {
        return;
    }
    
}
