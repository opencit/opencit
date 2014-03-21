/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.client.jaxrs;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.tag.model.SelectionKvAttribute;
import com.intel.mtwilson.tag.model.SelectionKvAttributeCollection;
import com.intel.mtwilson.tag.model.SelectionKvAttributeFilterCriteria;
import java.net.URL;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
public class SelectionKvAttributes extends MtWilsonClient {
    
    Logger log = LoggerFactory.getLogger(getClass().getName());

    public SelectionKvAttributes(URL url) {
        //super(url);
    }

    public SelectionKvAttributes(Properties properties) throws Exception {
        super(properties);
    }
    
    /**
     * Retrieves the list of mappings between the selection and the associated key-value pairs based on the 
     * search criteria specified. If none of the search criteria is specified, then search would return back 
     * an empty result set. The possible search options include nameEqualTo, nameContains, attrNameEqualTo,
     * attrNameContains, attrValueContains, and attrValueEqualTo.  
     * <p>
     * <i><u>Roles Needed:</u></i> AssetManagement
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML<br>
     * User can either specify the content type "Accept: application/json" or provide the same on the url after the
     * resource name. Ex: /tag-selection-kv-attributes.json?nameEqualTo=Name1 or /tag-selection-kv-attributes.xml?nameEqualTo=Name1.
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: GET</i><br>
     * https://192.168.1.101:8181/mtwilson/v2/tag-selection-kv-attributes?attrValueContains=Folsom
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * {"selection_kv_attribute_values":
     * [{"selection_id":"61116006-1cb8-40df-bb8f-f89e609e678b","kv_attribute_id":"061fbaf6-c5a6-4fce-9f69-1a68e65c1281",
     * "kv_attribute_name":"city","kv_attribute_value":"Hillsboro","selection_name":"other"}]}
     * <p>
     * @since Mt.Wilson 2.0
     */
    public SelectionKvAttributeCollection searchSelectionKvAttributes(SelectionKvAttributeFilterCriteria criteria) {
        return null;
    }
    
    /**
     * Retrieves the details of the mapping between selection and the key value pair with the specified ID. Note
     * that the ID should be a valid UUID.
     * <p>
     * <i><u>Roles Needed:</u></i> AssetManagement
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: GET</i><br>
     * https://192.168.1.101:8181/mtwilson/v2/tag-selection-kv-attributes/129ceab1-7c63-4eeb-b1b8-ccc7b5039836
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * {"id":"129ceab1-7c63-4eeb-b1b8-ccc7b5039836","selection_id":"a92c6e0c-1bf8-4646-9eb4-9fbd582d7eae","kv_attribute_id":"a847262e-8afe-4020-b40c-ce89dacb2b60"}
     * <p>
     * @since Mt.Wilson 2.0
     */
    public SelectionKvAttribute retrieveSelectionKvAttribute(UUID id) {
        return null;
    }

    /**
     * Creates a new mapping between the selection and the key-value pair specified. The user can specify the ID, 
     * which should be a valid UUID to be used as the primary key. If not specified, a new UUID would 
     * be automatically generated. After successful execution the caller would be returned back the new object created.
     * <p>
     * <i><u>Roles Needed:</u></i> AssetManagement
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: POST</i><br>
     * https://192.168.1.101:8181/mtwilson/v2/tag-selection-kv-attributes
     * <p>
     * <i>Sample Input</i><br>
     * {"selection_name":"Test Mapping","kv_attribute_id":"a847262e-8afe-4020-b40c-ce89dacb2b60"}
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * {"id":"e404ee8a-b114-40cc-b75f-a99d82fc11d7","name":"Test","description":"Test selection"}
     * <p>
     * @since Mt.Wilson 2.0
     */
    public SelectionKvAttribute createSelectionKvAttribute(SelectionKvAttribute obj) {
        return null;
    }

    public SelectionKvAttribute editSelectionKvAttribute(SelectionKvAttribute obj) {
        return null;
    }

    /**
     * Deletes an existing mapping between the Selection and the key-value pair.
     * <p>
     * <i><u>Roles Needed:</u></i> AssetManagement
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: DELETE</i><br>
     * https://192.168.1.101:8181/mtwilson/v2/tag-selection-kv-attributes/e404ee8a-b114-40cc-b75f-a99d82fc11d7
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * NA
     * <p>
     * @since Mt.Wilson 2.0
     */
    public void deleteSelectionKvAttribute(UUID id) {
        return;
    }
    
}
