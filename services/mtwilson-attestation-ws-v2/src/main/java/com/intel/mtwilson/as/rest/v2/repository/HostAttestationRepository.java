/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.repository;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mountwilson.as.common.ASConfig;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.controller.TblTaLogJpaController;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.mtwilson.as.data.TblTaLog;
import com.intel.mtwilson.as.rest.v2.model.HostAttestation;
import com.intel.mtwilson.as.rest.v2.model.HostAttestationCollection;
import com.intel.mtwilson.as.rest.v2.model.HostAttestationFilterCriteria;
import com.intel.mtwilson.datatypes.HostTrustResponse;
import com.intel.mtwilson.datatypes.HostTrustStatus;
import com.intel.mtwilson.model.Hostname;
import com.intel.mtwilson.as.business.trust.HostTrustBO;
import com.intel.mtwilson.as.rest.v2.model.HostAttestationLocator;
import com.intel.mtwilson.jaxrs2.server.resource.DocumentRepository;
import com.intel.mtwilson.policy.TrustReport;
import com.intel.mtwilson.repository.RepositoryCreateException;
import com.intel.mtwilson.repository.RepositoryException;
import com.intel.mtwilson.repository.RepositoryInvalidInputException;
import com.intel.mtwilson.repository.RepositoryRetrieveException;
import com.intel.mtwilson.repository.RepositorySearchException;

import java.util.Date;
import java.util.List;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.joda.time.DateTime;


/**
 *
 * @author ssbangal
 */
public class HostAttestationRepository implements DocumentRepository<HostAttestation, HostAttestationCollection, HostAttestationFilterCriteria, HostAttestationLocator> {
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HostAttestationRepository.class);
    
    private static final int DEFAULT_CACHE_VALIDITY_SECS = 3600;
    private static final int CACHE_VALIDITY_SECS = ASConfig.getConfiguration().getInt("saml.validity.seconds", DEFAULT_CACHE_VALIDITY_SECS);
        
    @Override
    @RequiresPermissions("host_attestations:search")    
    public HostAttestationCollection search(HostAttestationFilterCriteria criteria) {
        log.debug("HostAttestation:Search - Got request to search for host attestations.");        
        HostAttestationCollection objCollection = new HostAttestationCollection();
        try {
            TblTaLogJpaController jpaController = My.jpa().mwTaLog();
            if (criteria.id != null) {
                TblTaLog obj = jpaController.findByUuid(criteria.id.toString());
                if (obj != null) {
                    TblHosts hostObj = My.jpa().mwHosts().findHostByUuid(obj.getHost_uuid_hex());
                    objCollection.getHostAttestations().add(convert(obj, hostObj.getName()));
                }
            } else {
                TblHosts hostObj;
                if (criteria.hostUuid != null) {
                    hostObj = My.jpa().mwHosts().findHostByUuid(criteria.hostUuid.toString());
                } else if (criteria.aikSha1 != null && !criteria.aikSha1.isEmpty()) {
                    hostObj = My.jpa().mwHosts().findByAikSha1(criteria.aikSha1);
                } else if (criteria.nameEqualTo != null && !criteria.nameEqualTo.isEmpty()) {
                    hostObj = My.jpa().mwHosts().findByName(criteria.nameEqualTo);
                } else {
                    // no condition specified
                    hostObj = null;
                }
                if (hostObj != null) {
                    List<TblTaLog> taLogList = jpaController.findTrustStatusByHostId(hostObj.getId(), criteria.limit);
                    if (taLogList != null && !taLogList.isEmpty()) {
                        for (TblTaLog obj : taLogList) {
                            objCollection.getHostAttestations().add(convert(obj, hostObj.getName()));
                        }
                    }
                }
            } 
        } catch (Exception ex) {
            log.error("HostAttestation:Search - Error during retrieval of host attestation status from cache.", ex);
            throw new RepositorySearchException(ex, criteria);
        }
        log.debug("HostAttestation:Search - Returning back {} of results.", objCollection.getHostAttestations().size());                
        return objCollection;
    }

    @Override
    @RequiresPermissions("host_attestations:retrieve")    
    public HostAttestation retrieve(HostAttestationLocator locator) {
        if (locator == null || locator.id == null) { return null;}
        log.debug("HostAttestation:Store - Got request to retrieve the host attestation role with id {}.", locator.id.toString());        
        String id = locator.id.toString();
        try {
            TblTaLog obj = My.jpa().mwTaLog().findByUuid(id);
            if (obj != null) {
                HostAttestation haObj = convert(obj, obj.getHost_uuid_hex());
                return haObj;
            }
        } catch (Exception ex) {
            log.error("HostAttestation:Store - Error during retrieval of host attestation status from cache.", ex);
            throw new RepositoryRetrieveException(ex, locator);
        }
        return null;
    }
        
    @Override
    @RequiresPermissions("host_attestations:store")    
    public void store(HostAttestation item) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    @RequiresPermissions("host_attestations:create")    
    public void create(HostAttestation item) {
        log.debug("HostAttestation:Create - Got request to create host attestation.");  
        HostAttestationLocator locator = new HostAttestationLocator();
        locator.id = item.getId();
        try {
            HostTrustBO asBO = new HostTrustBO();
            TblHosts hostObj = My.jpa().mwHosts().findHostByUuid(item.getHostUuid());
            if (hostObj != null) {
                TrustReport htr = new HostTrustBO().getTrustReportForHost(hostObj, hostObj.getName());            
                item.setHostName(hostObj.getName());
                item.setTrustReport(htr);
                // Need to cache the attestation report
                asBO.logTrustReport(hostObj, htr);
            } else {
                log.error("HostAttestation:Create - Specified host with UUID {} does not exist in the system.", item.getHostUuid());
                throw new RepositoryInvalidInputException(locator);
            }
        } catch (RepositoryException re) {
            throw re;            
        } catch (Exception ex) {
            log.error("HostAttestation:Create - Error during creating a new attestation report.", ex);
            throw new RepositoryCreateException(ex, locator);
        }
    }

    @Override
    @RequiresPermissions("host_attestations:delete")    
    public void delete(HostAttestationLocator locator) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    @RequiresPermissions("host_attestations:delete")    
    public void delete(HostAttestationFilterCriteria criteria) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private HostAttestation convert(TblTaLog obj, String hostName) {
        HostAttestation convObj = new HostAttestation();
        convObj.setId(UUID.valueOf(obj.getUuid_hex()));
        convObj.setHostUuid(obj.getHost_uuid_hex());
        convObj.setHostName(hostName);
        convObj.setHostTrustResponse(new HostTrustResponse(new Hostname(hostName), getHostTrustStatusObj(obj)));
        return convObj;
    }

    private HostTrustStatus getHostTrustStatusObj(TblTaLog tblTaLog) {
        HostTrustStatus hostTrustStatus = new HostTrustStatus();
        
        String[] parts = tblTaLog.getError().split(",");
        
        for(String part : parts){
            String[] subparts = part.split(":");
            if(subparts[0].equalsIgnoreCase("BIOS")){
                hostTrustStatus.bios = (Integer.valueOf(subparts[1]) != 0);
            }else{
                hostTrustStatus.vmm = (Integer.valueOf(subparts[1]) != 0);
            }
        }
        return hostTrustStatus;
    }    

}
