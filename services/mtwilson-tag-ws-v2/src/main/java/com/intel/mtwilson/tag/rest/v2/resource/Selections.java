/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.rest.v2.resource;

import com.intel.mtwilson.tag.rest.v2.model.Selection;
import com.intel.mtwilson.tag.rest.v2.model.SelectionCollection;
import com.intel.mtwilson.tag.rest.v2.model.SelectionFilterCriteria;
import com.intel.mtwilson.tag.rest.v2.model.SelectionLocator;
import com.intel.mtwilson.jersey.NoLinks;
import com.intel.mtwilson.jersey.resource.AbstractJsonapiResource;
import com.intel.mtwilson.launcher.ws.ext.V2;
import com.intel.mtwilson.tag.rest.v2.repository.SelectionRepository;
import javax.ws.rs.Path;
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

//    @Override
//    @Path("/{id}")
//    @GET
//    @Produces({MediaType.APPLICATION_JSON, OtherMediaType.APPLICATION_YAML, OtherMediaType.TEXT_YAML})   
//    public Selection retrieveOne(SelectionLocator locator) {
//        return super.retrieveOne(locator); 
//    }
//        
//    
//            
//    @Override
//    @Path("/{id}")
//    @GET
//    @Produces({MediaType.APPLICATION_XML})   
//    public String retrieveOneXml(SelectionLocator locator) {
//        Selection obj = super.retrieveOne(locator); //To change body of generated methods, choose Tools | Templates.
//    }
            
}
