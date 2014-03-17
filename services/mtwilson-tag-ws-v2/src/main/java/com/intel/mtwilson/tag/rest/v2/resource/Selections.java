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
import com.intel.mtwilson.tag.Util;
import com.intel.mtwilson.tag.dao.TagJdbi;
import com.intel.mtwilson.tag.dao.jdbi.SelectionKvAttributeDAO;
import com.intel.mtwilson.tag.model.SelectionKvAttribute;
import com.intel.mtwilson.tag.rest.v2.repository.SelectionRepository;
import com.intel.mtwilson.tag.selection.SelectionBuilder;
import com.intel.mtwilson.tag.selection.xml.SelectionType;
import com.intel.mtwilson.tag.selection.xml.SelectionsType;
import java.io.IOException;
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
@Path("/tag-selections")
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
    public String retrieveOneXml(@BeanParam SelectionLocator locator) throws SQLException, IOException {
        SelectionKvAttributeDAO attrDao = TagJdbi.selectionKvAttributeDao();
        Selection obj = super.retrieveOne(locator); //To change body of generated methods, choose Tools | Templates.
        if( obj == null ) {
            return null;
        }
        List<SelectionKvAttribute> selectionKvAttributes = attrDao.findBySelectionIdWithValues(obj.getId());
        if( selectionKvAttributes == null || selectionKvAttributes.isEmpty() ) {
            log.error("No tags in selection");
            return null;
        }
        SelectionBuilder builder = SelectionBuilder.factory().selection();
        for (SelectionKvAttribute kvAttribute : selectionKvAttributes) {
            builder.textAttributeKV(kvAttribute.getKvAttributeName(), kvAttribute.getKvAttributeValue());
        } 
        // TODO:  if there are any other attributes such as 2.5.4.789.2 or custom ones they should be added here too
        SelectionsType selectionsType = builder.build();
        String xml = Util.toXml(selectionsType);
        log.debug("Generated tag selection xml: {}", xml);
        return xml;
    }
            
}
