/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.repository;

import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.as.rest.v2.model.PollHost;
import com.intel.mtwilson.as.rest.v2.model.PollHostCollection;
import com.intel.mtwilson.as.rest.v2.model.PollHostFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.PollHostLocator;
import com.intel.mtwilson.datatypes.ErrorCode;
import com.intel.mtwilson.datatypes.OpenStackHostTrustLevelQuery;
import com.intel.mtwilson.jersey.resource.SimpleRepository;
import com.intel.mtwilson.as.business.trust.HostTrustBO;
import com.intel.mtwilson.datatypes.OpenStackHostTrustLevelReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
public class PollHostRepository implements SimpleRepository<PollHost, PollHostCollection, PollHostFilterCriteria, PollHostLocator> {

    Logger log = LoggerFactory.getLogger(getClass().getName());
    
    @Override
    public PollHostCollection search(PollHostFilterCriteria criteria) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public PollHost retrieve(PollHostLocator locator) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void store(PollHost item) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void create(PollHost item) {
        OpenStackHostTrustLevelQuery obj = new OpenStackHostTrustLevelQuery();
        try {
            obj.setHosts(item.getHosts());
            OpenStackHostTrustLevelReport pollHosts = new HostTrustBO().getPollHosts(obj);
            item.setResult(pollHosts);
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during retrieval of host attestation status from cache.", ex);
            throw new ASException(ErrorCode.AS_HOST_ATTESTATION_REPORT_ERROR, ex.getClass().getSimpleName());
        }        
    }

    @Override
    public void delete(PollHostLocator locator) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void delete(PollHostFilterCriteria criteria) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
