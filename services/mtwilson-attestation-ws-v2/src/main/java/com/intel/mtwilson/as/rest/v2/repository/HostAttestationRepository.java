/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.repository;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mountwilson.as.common.ASConfig;
import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.controller.TblTaLogJpaController;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.mtwilson.as.data.TblTaLog;
import com.intel.mtwilson.as.rest.v2.model.HostAttestation;
import com.intel.mtwilson.as.rest.v2.model.HostAttestationCollection;
import com.intel.mtwilson.as.rest.v2.model.HostAttestationFilterCriteria;
import com.intel.mtwilson.datatypes.ErrorCode;
import com.intel.mtwilson.datatypes.HostTrustResponse;
import com.intel.mtwilson.datatypes.HostTrustStatus;
import com.intel.mtwilson.model.Hostname;
import com.intel.mtwilson.as.business.trust.HostTrustBO;
import com.intel.mtwilson.as.rest.v2.model.HostAttestationLocator;
import com.intel.mtwilson.jersey.resource.SimpleRepository;
import com.intel.mtwilson.policy.TrustReport;

import java.util.Date;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author ssbangal
 */
public class HostAttestationRepository implements SimpleRepository<HostAttestation, HostAttestationCollection, HostAttestationFilterCriteria, HostAttestationLocator> {
    
    private Logger log = LoggerFactory.getLogger(getClass().getName());
    
    private static final int DEFAULT_CACHE_VALIDITY_SECS = 3600;
    private static final int CACHE_VALIDITY_SECS = ASConfig.getConfiguration().getInt("saml.validity.seconds", DEFAULT_CACHE_VALIDITY_SECS);
    
    public HostAttestationRepository() {
        super();
    }
    
    @Override
    public HostAttestationCollection search(HostAttestationFilterCriteria criteria) {
        HostAttestationCollection objCollection = new HostAttestationCollection();
        try {
            TblTaLogJpaController jpaController = My.jpa().mwTaLog();
            if (criteria.id != null) {
                TblTaLog obj = jpaController.findByUuid(criteria.id.toString());
                if (obj != null) {
                    objCollection.getHostAttestations().add(convert(obj, obj.getHost_uuid_hex()));
                }
            } else if (criteria.hostUuid != null) {
                TblTaLog obj = jpaController.findLatestTrustStatusByHostUuid(criteria.hostUuid.toString(), getCacheStaleAfter());
                if (obj != null) {
                    objCollection.getHostAttestations().add(convert(obj, obj.getHost_uuid_hex()));
                }
            } else if (criteria.aik != null && !criteria.aik.isEmpty()) {
                TblHosts hostObj = My.jpa().mwHosts().findByAikSha1(criteria.aik.toString());
                if (hostObj != null) {
                    TblTaLog obj = jpaController.findLatestTrustStatusByHostUuid(hostObj.getUuid_hex(), getCacheStaleAfter());
                    if (obj != null) {
                        objCollection.getHostAttestations().add(convert(obj, hostObj.getName()));
                    }
                }
            } else if (criteria.nameEqualTo != null && !criteria.nameEqualTo.isEmpty()) {
                TblHosts hostObj = My.jpa().mwHosts().findByName(criteria.nameEqualTo.toString());
                if (hostObj != null) {
                    TblTaLog obj = jpaController.findLatestTrustStatusByHostUuid(hostObj.getUuid_hex(), getCacheStaleAfter());
                    if (obj != null) {
                        objCollection.getHostAttestations().add(convert(obj, hostObj.getName()));
                    }
                }
            }
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during retrieval of host attestation status from cache.", ex);
            throw new ASException(ErrorCode.AS_HOST_ATTESTATION_REPORT_ERROR, ex.getClass().getSimpleName());
        }
        return objCollection;
    }

    @Override
    public HostAttestation retrieve(HostAttestationLocator locator) {
        if (locator == null || locator.aik == null) { return null;}
        String aik = locator.aik;
        try {
            TblHosts hostObj = My.jpa().mwHosts().findByAikSha1(aik);
            if (hostObj != null) {
                TblTaLog obj = My.jpa().mwTaLog().findLatestTrustStatusByHostUuid(hostObj.getUuid_hex(), getCacheStaleAfter());
                if (obj != null) {
                    HostAttestation haObj = convert(obj, hostObj.getName());
                    return haObj;
                }
            }           
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during retrieval of host attestation status from cache.", ex);
            throw new ASException(ErrorCode.AS_HOST_ATTESTATION_REPORT_ERROR, ex.getClass().getSimpleName());
        }
        return null;
    }
        
    @Override
    public void store(HostAttestation item) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void create(HostAttestation item) {
        try {
            TblHosts hostObj = My.jpa().mwHosts().findHostByUuid(item.getHostUuid());
            TrustReport htr = new HostTrustBO().getTrustReportForHost(hostObj, hostObj.getName());
            item.setHostName(hostObj.getName());
            item.setTrustReport(htr);
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during creating a new attestation report.", ex);
            throw new ASException(ErrorCode.AS_HOST_REPORT_ERROR, ex.getClass().getSimpleName());
        }
    }

    @Override
    public void delete(HostAttestationLocator locator) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void delete(HostAttestationFilterCriteria criteria) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    private Date getCacheStaleAfter(){
        return new DateTime().minusSeconds(CACHE_VALIDITY_SECS).toDate();
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
