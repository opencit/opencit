/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.rest.v2.resource;

import com.intel.mtwilson.tag.model.SelectionKvAttributeCollection;
import com.intel.mtwilson.tag.model.SelectionKvAttributeFilterCriteria;
import com.intel.mtwilson.tag.model.SelectionKvAttributeLocator;
import com.intel.mtwilson.jersey.NoLinks;
import com.intel.mtwilson.jersey.resource.AbstractJsonapiResource;
import com.intel.mtwilson.launcher.ws.ext.V2;
import com.intel.mtwilson.tag.model.SelectionKvAttribute;
import com.intel.mtwilson.tag.rest.v2.repository.SelectionKvAttributeRepository;
import javax.ws.rs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
@V2
@Path("/selection_kv_attributes")
public class SelectionKvAttributes extends AbstractJsonapiResource<SelectionKvAttribute, SelectionKvAttributeCollection, SelectionKvAttributeFilterCriteria, NoLinks<SelectionKvAttribute>, SelectionKvAttributeLocator> {

    private Logger log = LoggerFactory.getLogger(getClass().getName());
    private SelectionKvAttributeRepository repository;
    
    public SelectionKvAttributes() {
        repository = new SelectionKvAttributeRepository();
    }
    
    @Override
    protected SelectionKvAttributeCollection createEmptyCollection() {
        return new SelectionKvAttributeCollection();
    }

    @Override
    protected SelectionKvAttributeRepository getRepository() {
        return repository;
    }
            
}
