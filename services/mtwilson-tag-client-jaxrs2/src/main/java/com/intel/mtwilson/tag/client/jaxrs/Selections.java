/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.client.jaxrs;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.jaxrs2.client.MtWilsonClient;
import com.intel.mtwilson.jaxrs2.mediatype.CryptoMediaType;
import com.intel.mtwilson.jaxrs2.mediatype.DataMediaType;
import com.intel.mtwilson.tag.model.Selection;
import com.intel.mtwilson.tag.model.SelectionCollection;
import com.intel.mtwilson.tag.model.SelectionFilterCriteria;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

public class Selections extends MtWilsonClient {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Selections.class);

    public Selections(URL url) throws Exception{
        super(url);
    }

    public Selections(Properties properties) throws Exception {
        super(properties);
    }
    
    /**
     * Creates a new selection using the specified name and description. 
     * @param obj Selection object that needs to be created. 
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
     *  Selection obj = new Selection();
     *  obj.setName("Test");
     *  obj.setDescription("Test Selection");                
     *  obj = client.createSelection(obj);
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
     *  client.deleteSelection(UUID.valueOf("e404ee8a-b114-40cc-b75f-a99d82fc11d7"));
     * </pre>
     */
    public void deleteSelection(UUID uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", uuid);
        Response selectionObj = getTarget().path("tag-selections/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).delete();
        if( !selectionObj.getStatusInfo().getFamily().equals(Family.SUCCESSFUL)) {
            throw new WebApplicationException("Delete selection failed");
        }
    }
    
    /**
     * Deletes the list of selections based on the search criteria specified. 
     * @param criteria - SelectionFilterCriteria object specifying the filter criteria. The 
     * possible search options include nameEqualTo, nameContains, descriptionEqualTo and descriptionContains.
     * @return N/A
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions tag_selections:delete,search
     * @mtwContentTypeReturned N/A
     * @mtwMethodType DELETE
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/tag-selections?nameEqualTo=default
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  Selections client = new Selections(My.configuration().getClientProperties());
     *  SelectionFilterCriteria criteria = new SelectionFilterCriteria();
     *  criteria.nameEqualTo = "default";
     *  client.deleteSelection(criteria);
     * </pre>
     */
    public void deleteSelection(SelectionFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        Response obj = getTargetPathWithQueryParams("tag-selections", criteria).request(MediaType.APPLICATION_JSON).delete();
        
        if( !obj.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
            throw new WebApplicationException("Delete Selection failed");
        }
    }
    

    
    /**
     * Allows the user to edit an existing selection. Note that only the description of the selection
     * can be edited.  
     * @param obj Selection object having the value that needs to be updated. 
     * @return Updated Selection object.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions tag_selections:store
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType PUT
     * @mtwSampleRestCall
     * https://server.com:8181/mtwilson/v2/tag-selections/e404ee8a-b114-40cc-b75f-a99d82fc11d7
     * Input: {"description":"Updated test description"}
     * Output: {"id":"e404ee8a-b114-40cc-b75f-a99d82fc11d7","description":"Updated test description"}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  Selections client = new Selections(My.configuration().getClientProperties());
     *  Selection selObj = new Selection();
     *  selObj.setId("e404ee8a-b114-40cc-b75f-a99d82fc11d7");
     *  selObj.setDescription("Updated test description");
     *  selObj = client.editSelection(selObj);
     * </pre>
     */
    public Selection editSelection(Selection obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", obj.getId().toString());
        Selection updatedSelObj = getTarget().path("tag-selections/{id}").resolveTemplates(map)
                .request().accept(MediaType.APPLICATION_JSON).put(Entity.json(obj), Selection.class);
        return updatedSelObj;
    }
        
    /**
     * Retrieves the details of the selection with the specified ID. This function retrieves the details
     * of all the associated mappings with the key-value pairs in the XML format. If the selection is 
     * not associated with any key-value pairs, this function would return a null value.
     * @param uuid - UUID of the selection to be retrieved
     * @return Selection object matching the specified UUID.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions tag_selections:retrieve
     * @mtwContentTypeReturned XML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/tag-selections/f9dfff4f-ac19-4c71-9b95-116e2f0dabc2
     * Output: 
     * <?xml version="1.0" encoding="UTF-8" standalone="yes"?><selections xmlns="urn:mtwilson-tag-selection">
     * <default><selection><attribute oid="2.5.4.789.1"><text>city=Folsom</text></attribute>
     *                     <attribute oid="2.5.4.789.1"><text>state=CA</text></attribute>
     *                     <attribute oid="2.5.4.789.1"><text>country=US</text></attribute>
     *                     <attribute oid="2.5.4.789.1"><text>customer=Coke</text></attribute>
     *                     <attribute oid="2.5.4.789.1"><text>city=Santa Clara</text></attribute>
     *                     <attribute oid="2.5.4.789.1"><text>customer=Pepsi</text></attribute>
     * </selection></default></selections>
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  Selections client = new Selections(My.configuration().getClientProperties());
     *  String retrieveSelection = client.retrieveSelectionAsXml(UUID.valueOf("f9dfff4f-ac19-4c71-9b95-116e2f0dabc2"));
     * </pre>
     */
    public String retrieveSelectionAsXml(UUID uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", uuid);
        String xml = getTarget().path("tag-selections/{id}").resolveTemplates(map).request(MediaType.APPLICATION_XML).get(String.class);
        return xml;
    }

    /**
     * Retrieves the details of the selection with the specified ID. This function retrieves the details
     * of all the associated mappings with the key-value pairs in the JSON format. If the selection is 
     * not associated with any key-value pairs, this function would return a null value.
     * @param uuid - UUID of the selection to be retrieved
     * @return Selection object matching the specified UUID.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions tag_selections:retrieve
     * @mtwContentTypeReturned JSON
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/tag-selections/f9dfff4f-ac19-4c71-9b95-116e2f0dabc2
     * Output: {"options":null,"default":{"selections":[{"attributes":[{"text":{"value":"city=Folsom"},"oid":"2.5.4.789.1"},
     * {"text":{"value":"state=CA"},"oid":"2.5.4.789.1"},{"text":{"value":"country=US"},"oid":"2.5.4.789.1"},
     * {"text":{"value":"customer=Coke"},"oid":"2.5.4.789.1"},{"text":{"value":"city=Santa Clara"},"oid":"2.5.4.789.1"},
     * {"text":{"value":"customer=Pepsi"},"oid":"2.5.4.789.1"}]}]}}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  Selections client = new Selections(My.configuration().getClientProperties());
     *  String retrieveSelection = client.retrieveSelectionAsJson(UUID.valueOf("f9dfff4f-ac19-4c71-9b95-116e2f0dabc2"));
     * </pre>
     */
    public String retrieveSelectionAsJson(UUID uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", uuid);
        String xml = getTarget().path("tag-selections/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(String.class);
        return xml;
    }
    
    /**
     * Retrieves the details of the selection with the specified ID. This function retrieves the details
     * of all the associated mappings with the key-value pairs as well in an encrypted XML format. If the 
     * selection is not associated with any key-value pairs, this function would return a null value.
     * Users calling into the REST API directly should set the accept header to "message/rfc822". 
     * @param uuid - UUID of the selection to be retrieved
     * @return Selection object matching the specified UUID.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions tag_selections:retrieve
     * @mtwContentTypeReturned message/rfc822
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/tag-selections/f9dfff4f-ac19-4c71-9b95-116e2f0dabc2
     * Output: Content-Type: encrypted/openssl; alg="aes-256-ofb"; digest-alg="sha256"; enclosed="application/zip"
     * Date: Wed May 28 01:44:52 PDT 2014
     * 
     * U2FsdGVkX19foheWuO2gmlwyOdHwwnGZydv8BYR9adE+QujdXx/+w2Lm8wa6bZgp
     * +srGGrTC08Zp8cLHaCs4Bep/ARaCfW86PwH0v0obpgm03P0pKkRZcT7NWKDXz105
     * zPONQJ1HyX6PAv1SuplXAD6rgv2lSTG1Q8jc0fdmMphR1mjv2j3nxfVcy4b195jJ
     * GXu63upueaY3bRr12YdWgcxMUFY9kwTgCQgXS2V4KqbQ6degKGrTi1ghoDF5r+R3
     * 5LbKz1sQEiJ6KI+x31/yr4h5MCPbuh58VwxkDC0XHdKNezm2WAGTanYZoUWQDW69
     * cO7oYbI3TFG07299dIlBPY0dgRxPhGgIxKncuMmI28NjKnJrc3klMED7R0AZkS11
     * FzfcBikSbchxGf4C3iGvm/dBG+8sOBAaA8Gkbc5zfSiibTl9maT+WN974P0JoM6a
     * Al8K/CSAni8Q5wl06rg9RrFeVpYCmjshF7KeOqlipK3Ps1CQ8CoZUA9PyWmu2y0m
     * rCmzkkwi+KN0CVbCWOOntmLfrlNXcP3Nh/KbldwTB7VRM+qIoaxgYIUxq6RVT7tR
     * BmXulZlU5fZLTvETnydu1qoFFukhbYo7x3PHm+K4neZukrvytF09QyZJjZCqedP4
     * 4r0yw7/vWSCR1m4T8uB+PqaqGSriENvVa1uu3o4dQzw5U2abZ767TIcI6h02P63w
     * zCkYbeW+Kell13gPsEeQISRUvIYDD+eVXKmEGHesbbBO9G0pD2SO5bIVyHNqTKZN
     * HQwAzn8M9id5ippNBclJ+J2aWdI8AOxPZDNwT4KoibUh0z3jHf0rXgMmRPyFhW8i
     * LaLKofUaiRm86nQ3NTLBWWCl6Ga7pWsBVshcM2Fh+PIwaDNGQLmbZPKE3s8S/zBB
     * AfTM5TcZTHFsqi18lOi1A+GlgyBUza0ssQF4rqahAhL3gMRc2Gk9NQlQSwZ8p+v1
     * UefTAUxvkBpq4MLLAfVwePomHE1L9LZVjFK+dRm3M6TCis1Qg7Ve2ThBYtgVmer+
     * +yFymvXn4QAe1k3ihOjsfTtr106xEL8qDK6/81mRs9fSs6r4wvt90x3uCwWbL6+m
     * SKt0fxy5cgnUJ/jJ7Eoql7uotQsAUUdTLR1AVkvKop31581FtryXCGoTYP00tCMu
     * D+uZH5ZzF6qBsOOk8ukJko3a9Fo9yKLALw==
     * 
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
        String encryptedXml = getTarget().path("tag-selections/{id}").resolveTemplates(map).request(CryptoMediaType.MESSAGE_RFC822).get(String.class);
        return encryptedXml;
    }

    /**
     * Retrieves the list of selections based on the search criteria specified. 
     * @param criteria - SelectionFilterCriteria object specifying the filter criteria. The 
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
     * https://192.168.1.101:8181/mtwilson/v2/tag-selections?nameEqualTo=default
     * Output: {"selections":[{"id":"f9dfff4f-ac19-4c71-9b95-116e2f0dabc2","name":"default","description":"default selections"}]}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  Selections client = new Selections(My.configuration().getClientProperties());
     *  SelectionFilterCriteria criteria = new SelectionFilterCriteria();
     *  criteria.nameEqualTo = "default";
     *  SelectionCollection selections = client.searchSelections(criteria);
     * </pre>
     */
    public SelectionCollection searchSelections(SelectionFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        SelectionCollection objCollection = getTargetPathWithQueryParams("tag-selections", criteria).request(MediaType.APPLICATION_JSON).get(SelectionCollection.class);
        return objCollection;
    }
    
}
