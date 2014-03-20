/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.client.jaxrs;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.client.jaxrs.*;
import com.intel.mtwilson.tag.model.Selection;
import com.intel.mtwilson.tag.model.SelectionCollection;
import com.intel.mtwilson.tag.model.SelectionFilterCriteria;
import java.net.URL;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
public class Selections extends MtWilsonClient {
    
    Logger log = LoggerFactory.getLogger(getClass().getName());

    public Selections(URL url) {
        //super(url);
    }

    public Selections(Properties properties) throws Exception {
        super(properties);
    }
    
    /**
     * Retrieves the list of selections based on the search criteria specified. If none
     * of the search criteria is specified, then search would return back an empty result set. The 
     * possible search options include nameEqualTo, nameContains, descriptionEqualTo and descriptionContains.  
     * <p>
     * <i><u>Roles Needed:</u></i> AssetManagement
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML<br>
     * User can either specify the content type "Accept: application/json" or provide the same on the url after the
     * resource name. Ex: /tag-selections.json?nameEqualTo=Name1 or /tag-selections.xml?nameEqualTo=Name1.
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: GET</i><br>
     * https://192.168.1.101:8181/mtwilson/v2/tag-selections?nameContains=default
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * {"selections":[{"id":"f9dfff4f-ac19-4c71-9b95-116e2f0dabc2","name":"default","description":"default selections"}]}
     * <p>
     * @since Mt.Wilson 2.0
     */
    public SelectionCollection searchSelections(SelectionFilterCriteria criteria) {
        return null;
    }
    
    /**
     * Retrieves the details of the selection with the specified ID. Note
     * that the ID should be a valid UUID.
     * <p>
     * <i><u>Roles Needed:</u></i> AssetManagement
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: GET</i><br>
     * https://192.168.1.101:8181/mtwilson/v2/tag-selections/f9dfff4f-ac19-4c71-9b95-116e2f0dabc2
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * {"id":"f9dfff4f-ac19-4c71-9b95-116e2f0dabc2","name":"default","description":"default selections"}
     * <p>
     * @since Mt.Wilson 2.0
     */
    public Selection retrieveSelection(UUID id) {
        return null;
    }

    /**
     * Creates a new selection using the specified name and description. The user can specify the ID, 
     * which should be a valid UUID to be used as the primary key. If not specified, a new UUID would 
     * be automatically generated. After successful execution the caller would be returned back the new object created.
     * <p>
     * <i><u>Roles Needed:</u></i> AssetManagement
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: POST</i><br>
     * https://192.168.1.101:8181/mtwilson/v2/tag-selections
     * <p>
     * <i>Sample Input</i><br>
     * {"name":"Test","description":"Test selection"}
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * {"id":"e404ee8a-b114-40cc-b75f-a99d82fc11d7","name":"Test","description":"Test selection"}
     * <p>
     * @since Mt.Wilson 2.0
     */
    public Selection createSelection(Selection obj) {
        return null;
    }

    /**
     * Allows the user to edit an existing selection. Note that only the description of the selection
     * can be edited. The user has to specify the ID on the query string and the value to be updated
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
     * https://192.168.1.101:8181/mtwilson/v2/tag-selections/e404ee8a-b114-40cc-b75f-a99d82fc11d7
     * <p>
     * <i>Sample Input</i><br>
     * {"description":"Updated test description"}
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * {"id":"a6544ff4-6dc7-4c74-82be-578592e7e3ba","value":"HR"}
     * <p>
     * @since Mt.Wilson 2.0
     */
    public Selection editSelection(Selection obj) {
        return null;
    }

    /**
     * Deletes the Selection with the specified ID. Note that when the selection is deleted
     * all the associated key/attribute - values would also be deleted.
     * <p>
     * <i><u>Roles Needed:</u></i> AssetManagement
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: DELETE</i><br>
     * https://192.168.1.101:8181/mtwilson/v2/tag-selections/e404ee8a-b114-40cc-b75f-a99d82fc11d7
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * NA
     * <p>
     * @since Mt.Wilson 2.0
     */
    public void deleteSelection(UUID id) {
        return;
    }
    
}
