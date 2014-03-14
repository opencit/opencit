/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.rest.v2.resource;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.atag.model.SelectionTagValue;
import com.intel.mtwilson.atag.model.x509.UTF8NameValueSequence;
import com.intel.mtwilson.tag.model.Selection;
import com.intel.mtwilson.tag.model.SelectionCollection;
import com.intel.mtwilson.tag.model.SelectionFilterCriteria;
import com.intel.mtwilson.tag.model.SelectionLocator;
import com.intel.mtwilson.jersey.NoLinks;
import com.intel.mtwilson.jersey.http.OtherMediaType;
import com.intel.mtwilson.jersey.resource.AbstractJsonapiResource;
import com.intel.mtwilson.launcher.ws.ext.V2;
import com.intel.mtwilson.tag.dao.TagJdbi;
import com.intel.mtwilson.tag.dao.jdbi.SelectionKvAttributeDAO;
import com.intel.mtwilson.tag.model.SelectionKvAttribute;
import com.intel.mtwilson.tag.rest.v2.repository.SelectionRepository;
import java.sql.SQLException;
import java.util.List;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import org.apache.commons.codec.binary.Base64;
import javax.ws.rs.core.MediaType;
import org.restlet.data.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
@V2
@Path("/selections")
public class Selections extends AbstractJsonapiResource<Selection, SelectionCollection, SelectionFilterCriteria, NoLinks<Selection>, SelectionLocator> {

    private Logger log = LoggerFactory.getLogger(getClass().getName()); 
    private SelectionRepository repository;
    
    public Selections() {
        repository = new SelectionRepository();
    }
    
    @Override
    protected SelectionCollection createEmptyCollection() {
        return new SelectionCollection();
    }

    @Override
    protected SelectionRepository getRepository() {
        return repository;
    }

    @Override
    @Path("/{id}")
    @GET
    @Produces({MediaType.APPLICATION_JSON, OtherMediaType.APPLICATION_YAML, OtherMediaType.TEXT_YAML})   
    public Selection retrieveOne(@BeanParam SelectionLocator locator) {
        return super.retrieveOne(locator); 
    }
        
    
            
    @Path("/{id}")
    @GET
    @Produces({MediaType.APPLICATION_XML})   
    public String retrieveOneXml(@BeanParam SelectionLocator locator) throws SQLException {
        SelectionKvAttributeDAO attrDao = TagJdbi.selectionKvAttributeDao();
        Selection obj = super.retrieveOne(locator); //To change body of generated methods, choose Tools | Templates.
        if( obj == null ) {
            return null;
        }
        List<SelectionKvAttribute> selectionKvAttributes = attrDao.findBySelectionIdWithValues(obj.getId().toString());
        if( selectionKvAttributes == null || selectionKvAttributes.isEmpty() ) {
            log.error("No tags in selection");
            return null;
        }
        StringBuilder str = new StringBuilder();
        str.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
                     "<selections xmlns=\"urn:mtwilson-tag-selection\">\n"+
                     "<selection id=\"" + obj.getId() + "\" name=\"" + obj.getName() + "\" >");
        if(selectionKvAttributes != null && !selectionKvAttributes.isEmpty()) {
            for(SelectionKvAttribute kvAttr : selectionKvAttributes) {
                UTF8NameValueSequence sequence = new UTF8NameValueSequence(kvAttr.getKvAttributeName(),kvAttr.getKvAttributeValue());
                str.append("<attribute oid=\""+ "2.5.4.789.1" +"\"><der>" + Base64.encodeBase64String(sequence.getDEREncoded()) + "</der></attribute>\n");                    
            }
        }
        str.append("</selection>\n</selections>");
        return str.toString();
    }
            
}
