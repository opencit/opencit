/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.rest.v2.resource;

import com.intel.mtwilson.tag.model.KvAttribute;
import com.intel.mtwilson.tag.model.KvAttributeCollection;
import com.intel.mtwilson.tag.model.KvAttributeFilterCriteria;
import com.intel.mtwilson.tag.model.KvAttributeLocator;
import com.intel.mtwilson.jaxrs2.NoLinks;
import com.intel.mtwilson.jaxrs2.server.resource.AbstractJsonapiResource;
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
@Path("/tag-kv-attributes")
public class KvAttributes extends AbstractJsonapiResource<KvAttribute, KvAttributeCollection, KvAttributeFilterCriteria, NoLinks<KvAttribute>, KvAttributeLocator> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(KvAttributes.class);

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
