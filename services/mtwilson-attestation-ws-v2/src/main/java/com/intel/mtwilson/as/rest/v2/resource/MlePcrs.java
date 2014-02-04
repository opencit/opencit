/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.resource;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.controller.TblPcrManifestJpaController;
import com.intel.mtwilson.as.data.TblPcrManifest;
import com.intel.mtwilson.as.rest.v2.model.MlePcr;
import com.intel.mtwilson.as.rest.v2.model.MlePcrCollection;
import com.intel.mtwilson.as.rest.v2.model.MlePcrFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.MlePcrLocator;
import com.intel.mtwilson.as.rest.v2.repository.MlePcrRepository;
import com.intel.mtwilson.datatypes.ErrorCode;
import com.intel.mtwilson.datatypes.PCRWhiteList;
import com.intel.mtwilson.jersey.NoLinks;
import com.intel.mtwilson.jersey.resource.AbstractJsonapiResource;
import com.intel.mtwilson.jersey.resource.AbstractResource;
import com.intel.mtwilson.launcher.ws.ext.V2;
import com.intel.mtwilson.wlm.business.MleBO;
import java.util.List;
import javax.ejb.Stateless;
import javax.ws.rs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
@V2
@Stateless
@Path("/mle/{id}/pcrs")
public class MlePcrs extends AbstractJsonapiResource<MlePcr, MlePcrCollection, MlePcrFilterCriteria, NoLinks<MlePcr>, MlePcrLocator>{

    private Logger log = LoggerFactory.getLogger(getClass().getName());

    public MlePcrs() {
        super();
        setRepository(new MlePcrRepository());
    }
    
    @Override
    protected MlePcrCollection createEmptyCollection() {
        return new MlePcrCollection();
    }
        
}
