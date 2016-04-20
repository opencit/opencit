/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.rest.v2.resource;

import com.intel.mtwilson.tag.model.SelectionKvAttributeCollection;
import com.intel.mtwilson.tag.model.SelectionKvAttributeFilterCriteria;
import com.intel.mtwilson.tag.model.SelectionKvAttributeLocator;
import com.intel.mtwilson.jaxrs2.NoLinks;
import com.intel.mtwilson.jaxrs2.server.resource.AbstractJsonapiResource;
import com.intel.mtwilson.launcher.ws.ext.V2;
import com.intel.mtwilson.tag.model.SelectionKvAttribute;
import com.intel.mtwilson.tag.rest.v2.repository.SelectionKvAttributeRepository;
import javax.ws.rs.Path;

/**
 *
 * @author ssbangal
 */
@V2
@Path("/tag-selection-kv-attributes")
public class SelectionKvAttributes extends AbstractJsonapiResource<SelectionKvAttribute, SelectionKvAttributeCollection, SelectionKvAttributeFilterCriteria, NoLinks<SelectionKvAttribute>, SelectionKvAttributeLocator> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SelectionKvAttributes.class);

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
