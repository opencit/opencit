/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.rest.v2.resource;

import com.intel.mtwilson.tag.model.File;
import com.intel.mtwilson.tag.model.FileCollection;
import com.intel.mtwilson.tag.model.FileFilterCriteria;
import com.intel.mtwilson.tag.model.FileLocator;
import com.intel.mtwilson.jaxrs2.NoLinks;
import com.intel.mtwilson.jaxrs2.server.resource.AbstractJsonapiResource;
import com.intel.mtwilson.launcher.ws.ext.V2;
import com.intel.mtwilson.tag.rest.v2.repository.FileRepository;
import javax.ws.rs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ssbangal
 */
@V2
@Path("/files")
public class Files extends AbstractJsonapiResource<File, FileCollection, FileFilterCriteria, NoLinks<File>, FileLocator> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Files.class);

    private FileRepository repository;
    
    public Files() {
        repository = new FileRepository();
    }
    
    @Override
    protected FileCollection createEmptyCollection() {
        return new FileCollection();
    }

    @Override
    protected FileRepository getRepository() {
        return repository;
    }
        
}
