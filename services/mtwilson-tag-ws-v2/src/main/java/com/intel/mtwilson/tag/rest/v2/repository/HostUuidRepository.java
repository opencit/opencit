/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.rest.v2.repository;

import com.intel.mtwilson.datatypes.TxtHostRecord;
import com.intel.mtwilson.jersey.resource.SimpleRepository;
import com.intel.mtwilson.tag.common.Global;
import com.intel.mtwilson.tag.rest.v2.model.HostUuid;
import com.intel.mtwilson.tag.rest.v2.model.HostUuidCollection;
import com.intel.mtwilson.tag.rest.v2.model.HostUuidFilterCriteria;
import com.intel.mtwilson.tag.rest.v2.model.HostUuidLocator;
import java.util.List;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
public class HostUuidRepository implements SimpleRepository<HostUuid, HostUuidCollection, HostUuidFilterCriteria, HostUuidLocator> {

    private Logger log = LoggerFactory.getLogger(getClass().getName());

    @Override
    public HostUuidCollection search(HostUuidFilterCriteria criteria) {
        HostUuidCollection objCollection = new HostUuidCollection();
        
        try {
            String ip = criteria.id.toString();
            log.debug("made it into actionAutomation! got ip of " + ip);
            //String ip = getQuery().getFirstValue("ipaddress");
            List<TxtHostRecord> hostList = Global.mtwilson().queryForHosts(ip,true);
            if(hostList == null || hostList.size() < 1) {
                log.debug("host uuid didn't return back any results");
                throw new Exception("No host records found");
            }
            log.debug("get host uuid returned " + hostList.get(0).Hardware_Uuid);
            HostUuid obj = new HostUuid();
            obj.setHardwareUuid(hostList.get(0).Hardware_Uuid);
            objCollection.getHostUuids().add(obj);
        } catch (ResourceException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during search for host hardware uuid.", ex);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Please see the server log for more details.");
        }        
        return objCollection;
    }

    @Override
    public HostUuid retrieve(HostUuidLocator locator) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void store(HostUuid item) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void create(HostUuid item) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void delete(HostUuidLocator locator) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public void delete(HostUuidFilterCriteria criteria) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
        
}