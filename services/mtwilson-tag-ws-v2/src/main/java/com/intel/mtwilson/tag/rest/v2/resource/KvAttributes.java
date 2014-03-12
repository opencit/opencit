/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.rest.v2.resource;

import com.intel.mtwilson.tag.model.KvAttribute;
import com.intel.mtwilson.tag.model.KvAttributeCollection;
import com.intel.mtwilson.tag.model.KvAttributeFilterCriteria;
import com.intel.mtwilson.tag.model.KvAttributeLocator;
import com.intel.mtwilson.jersey.NoLinks;
import com.intel.mtwilson.jersey.resource.AbstractJsonapiResource;
import com.intel.mtwilson.launcher.ws.ext.V2;
import com.intel.mtwilson.tag.rest.v2.repository.KvAttributeRepository;
import javax.ws.rs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
@V2
@Path("/kv_attributes")
public class KvAttributes extends AbstractJsonapiResource<KvAttribute, KvAttributeCollection, KvAttributeFilterCriteria, NoLinks<KvAttribute>, KvAttributeLocator> {

    private Logger log = LoggerFactory.getLogger(getClass().getName());
    private KvAttributeRepository repository;
    
    public KvAttributes() {
        repository = new KvAttributeRepository();
    }
    
    @Override
    protected KvAttributeCollection createEmptyCollection() {
        return new KvAttributeCollection();
    }

    @Override
    protected KvAttributeRepository getRepository() {
        return repository;
    }
        
}
