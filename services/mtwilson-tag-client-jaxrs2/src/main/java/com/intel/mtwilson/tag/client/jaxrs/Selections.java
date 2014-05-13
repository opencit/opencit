/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.client.jaxrs;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.client.jaxrs.common.MtWilsonClient;
import com.intel.mtwilson.jersey.http.OtherMediaType;
import com.intel.mtwilson.tag.model.Selection;
import com.intel.mtwilson.tag.model.SelectionCollection;
import com.intel.mtwilson.tag.model.SelectionFilterCriteria;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class Selections extends MtWilsonClient {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Selections.class);

    public Selections(URL url) throws Exception{
        super(url);
    }

    public Selections(Properties properties) throws Exception {
        super(properties);
    }
    
    /**
     * Creates a new selection using the specified name and description. The user can specify the ID, 
     * which should be a valid UUID to be used as the primary key. If not specified, a new UUID would 
     * be automatically generated. After successful execution the caller would be returned back the new object created.
     * @param Selection object that needs to be created. 
     * @return Created Selection object.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions tag_selections:create
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType POST
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/tag-selections
     * Input: {"name":"Test","description":"Test selection"}
     * Output: {"id":"e404ee8a-b114-40cc-b75f-a99d82fc11d7","name":"Test","description":"Test selection"}
     * @mtwSampleApiCall
     * <pre>
     *  Selections client = new Selections(My.configuration().getClientProperties());
     *  Selection selObj = new Selection();
     *  selObj.setName("Intel");
     *  selObj.setDescription("Intel OEM");
     *  Selection createdSelObj = client.createSelection(selObj);
     * </pre>
     */
    public Selection createSelection(Selection obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        Selection createdObj = getTarget().path("tag-selections").request().accept(MediaType.APPLICATION_JSON).post(Entity.json(obj), Selection.class);
        return createdObj;
    }


    /**
     * Deletes the Selection with the specified ID. Note that when the selection is deleted
     * all the associated key/attribute - values would also be deleted.
     * @param uuid - UUID of the selection that has to be deleted.
     * @return N/A
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions tag_selections:delete
     * @mtwContentTypeReturned N/A
     * @mtwMethodType DELETE
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/tag-selections/e404ee8a-b114-40cc-b75f-a99d82fc11d7
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  Selections client = new Selections(My.configuration().getClientProperties());
     *  client.deleteSelection("e404ee8a-b114-40cc-b75f-a99d82fc11d7");
     * </pre>
     */
    public void deleteSelection(UUID uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", uuid);
        Response selectionObj = getTarget().path("tag-selections/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).delete();
    }
    
    /**
     * Allows the user to edit an existing selection. Note that only the description of the selection
     * can be edited.  
     * @param Selection object having the value that needs to be updated. 
     * @return Updated Selection object.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions tag_selections:store
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType PUT
     * @mtwSampleRestCall
     * https://server.com:8181/mtwilson/v2/tag-selections/e404ee8a-b114-40cc-b75f-a99d82fc11d7
     * Input: {"description":"Updated test description"}
     * Output: {"id":"e404ee8a-b114-40cc-b75f-a99d82fc11d7","value":"Updated test description"}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  Selections client = new Selections(My.configuration().getClientProperties());
     *  Selection selObj = new Selection();
     *  selObj.setId("e404ee8a-b114-40cc-b75f-a99d82fc11d7");
     *  selObj.setDescription("Updated test description");
     *  Selection editedSelObj = client.editSelection(selObj);
     * </pre>
     */
    public Selection editSelection(Selection obj) {         log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", obj.getId().toString());
        Selection updatedSelObj = getTarget().path("tag-selections/{id}").resolveTemplates(map)
                .request().accept(MediaType.APPLICATION_JSON).put(Entity.json(obj), Selection.class);
        return updatedSelObj;
    }
    
    /**
     * Retrieves the details of the selection with the specified ID. This function retrieves the details
     * of all the associated mappings with the key-value pairs as well. 
     * @param uuid - UUID of the selection to be retrieved
     * @return Selection object matching the specified UUID.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions tag_selections:retrieve
     * @mtwContentTypeReturned JSON/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/tag-selections/f9dfff4f-ac19-4c71-9b95-116e2f0dabc2
     * Output: {"id":"f9dfff4f-ac19-4c71-9b95-116e2f0dabc2","name":"default","description":"default selections"}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  Selections client = new Selections(My.configuration().getClientProperties());
     *  Selection retrieveSelection = client.retrieveSelection("f9dfff4f-ac19-4c71-9b95-116e2f0dabc2");
     * </pre>
     */
    public Selection retrieveSelection(UUID uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", uuid);
        Selection role = getTarget().path("tag-selections/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(Selection.class);
        return role;
    }
    
    /**
     * Retrieves the details of the selection with the specified ID. This function retrieves the details
     * of all the associated mappings with the key-value pairs in an XML format. 
     * @param uuid - UUID of the selection to be retrieved
     * @return Selection object matching the specified UUID.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions tag_selections:retrieve
     * @mtwContentTypeReturned XML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/tag-selections/f9dfff4f-ac19-4c71-9b95-116e2f0dabc2
     * Output: {"id":"f9dfff4f-ac19-4c71-9b95-116e2f0dabc2","name":"default","description":"default selections"}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  Selections client = new Selections(My.configuration().getClientProperties());
     *  Selection retrieveSelection = client.retrieveSelection("f9dfff4f-ac19-4c71-9b95-116e2f0dabc2");
     * </pre>
     */
    public String retrieveSelectionAsXml(UUID uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("id", uuid);
        String xml = getTarget().path("tag-selections/{id}").resolveTemplates(map).request(MediaType.APPLICATION_XML).get(String.class);
        return xml;
    }

    /**
     * Retrieves the details of the selection with the specified ID. This function retrieves the details
     * of all the associated mappings with the key-value pairs as well in an encrypted XML format. Users
     * calling into the REST API directly should set the accept header to "message/rfc822". 
     * @param uuid - UUID of the selection to be retrieved
     * @return Selection object matching the specified UUID.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions tag_selections:retrieve
     * @mtwContentTypeReturned message/rfc822
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/tag-selections/f9dfff4f-ac19-4c71-9b95-116e2f0dabc2
     * Output: {"id":"f9dfff4f-ac19-4c71-9b95-116e2f0dabc2","name":"default","description":"default selections"}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  Selections client = new Selections(My.configuration().getClientProperties());
     *  Selection retrieveSelection = client.retrieveSelection("f9dfff4f-ac19-4c71-9b95-116e2f0dabc2");
     * </pre>
     */
    public String retrieveSelectionAsEncryptedXml(UUID uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", uuid);
        String encryptedXml = getTarget().path("tag-selections/{id}").resolveTemplates(map).request(OtherMediaType.MESSAGE_RFC822).get(String.class);
        return encryptedXml;
    }

    /**
     * Retrieves the list of selections based on the search criteria specified. 
     * @param SelectionFilterCriteria object specifying the filter criteria. The 
     * possible search options include nameEqualTo, nameContains, descriptionEqualTo and descriptionContains.  
     * User can retrieve all the selections by setting the filter criteria to false. By default this filter
     * criteria would be set to true. [Ex: /v2/tag-selections?filter=false]
     * @return SelectionCollection object with the list of all the Selection objects matching the specified filter criteria
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions tag_selections:search
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://192.168.1.101:8181/mtwilson/v2/tag-selections?nameContains=default
     * Output: {"selections":[{"id":"f9dfff4f-ac19-4c71-9b95-116e2f0dabc2","name":"default","description":"default selections"}]}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  Selections client = new Selections(My.configuration().getClientProperties());
     *  SelectionFilterCriteria criteria = new SelectionFilterCriteria();
     *  criteria.nameContains = "default";
     *  SelectionCollection selections = client.searchSelections(criteria);
     * </pre>
     */
    public SelectionCollection searchSelections(SelectionFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        SelectionCollection objCollection = getTargetPathWithQueryParams("tag-selections", criteria).request(MediaType.APPLICATION_JSON).get(SelectionCollection.class);
        return objCollection;
    }
    
}
