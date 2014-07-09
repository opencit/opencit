/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.as.rest.v2.repository;

import com.intel.mtwilson.as.rest.v2.model.Host;
import com.intel.mtwilson.as.rest.v2.model.HostCollection;
import com.intel.mtwilson.as.rest.v2.model.HostFilterCriteria;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.controller.TblHostsJpaController;
import com.intel.mtwilson.as.controller.TblMleJpaController;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.mtwilson.as.data.TblMle;
import com.intel.mtwilson.as.rest.v2.model.HostLocator;
import com.intel.mtwilson.i18n.ErrorCode;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import com.intel.mtwilson.as.business.HostBO;
import com.intel.mtwilson.datatypes.TxtHost;
import com.intel.mtwilson.datatypes.TxtHost;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import com.intel.mtwilson.jaxrs2.server.resource.SimpleRepository;

import java.util.List;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.apache.shiro.authz.annotation.RequiresPermissions;

/**
 *
 * @author jbuhacoff
 */
public class HostRepository implements SimpleRepository<Host,HostCollection,HostFilterCriteria,HostLocator> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HostRepository.class);
    
    @Override
    @RequiresPermissions("hosts:search")    
    public HostCollection search(HostFilterCriteria criteria) {
        log.debug("Host:Search - Got request to search for the Hosts.");        
        HostCollection objCollection = new HostCollection();
        try {
            TblHostsJpaController jpaController = My.jpa().mwHosts();
            if (criteria.filter == false) {
                List<TblHosts> objList = jpaController.findTblHostsEntities();
                if (objList != null && !objList.isEmpty()) {
                    for(TblHosts obj : objList) {
                        objCollection.getHosts().add(convert(obj));
                    }
                }                
            } else if (criteria.id != null) {
                TblHosts obj = jpaController.findHostByUuid(criteria.id.toString());
                if (obj != null) {
                    objCollection.getHosts().add(convert(obj));
                }
            } else if (criteria.nameEqualTo != null) {
                TblHosts obj = jpaController.findHostByUuid(criteria.nameEqualTo);
                if (obj != null) {
                    objCollection.getHosts().add(convert(obj));
                }
            } else if (criteria.nameContains != null && !criteria.nameContains.isEmpty()) {
                List<TblHosts> objList = jpaController.findHostsByNameSearchCriteria(criteria.nameContains);
                if (objList != null && !objList.isEmpty()) {
                    for(TblHosts obj : objList) {
                        objCollection.getHosts().add(convert(obj));
                    }
                }                
            }else if (criteria.descriptionContains != null && !criteria.descriptionContains.isEmpty()) {
                List<TblHosts> objList = jpaController.findHostsByDescriptionSearchCriteria(criteria.descriptionContains);
                if (objList != null && !objList.isEmpty()) {
                    for(TblHosts obj : objList) {
                        objCollection.getHosts().add(convert(obj));
                    }
                }                
            }
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during search for hosts.", ex);
            throw new ASException(ErrorCode.AS_QUERY_HOST_ERROR, ex.getClass().getSimpleName());
        }
        log.debug("Host:Search - Returning back {} of results.", objCollection.getHosts().size());                
        return objCollection;
    }

    @Override
    @RequiresPermissions("hosts:retrieve")    
    public Host retrieve(HostLocator locator) {
        if( locator == null || locator.id == null ) { return null; }
        log.debug("Host:Retrieve - Got request to retrieve Host with id {}.", locator.id);                
        String id = locator.id.toString();
        try {
            TblHostsJpaController jpaController = My.jpa().mwHosts();
            TblHosts obj = jpaController.findHostByUuid(id);
            if (obj != null) {
                Host host = convert(obj);
                return host;
            }
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during search for hosts.", ex);
            throw new ASException(ErrorCode.AS_QUERY_HOST_ERROR, ex.getClass().getSimpleName());
        }
        return null;
    }

    @Override
    @RequiresPermissions("hosts:store")    
    public void store(Host item) {
        log.debug("Host:Store - Got request to update Host with id {}.", item.getId().toString());        
        TxtHostRecord obj = new TxtHostRecord();
        try {
            
            obj.HostName = item.getName();
            obj.AddOn_Connection_String = item.getConnectionUrl();
            obj.Description = item.getDescription();
            obj.Email = item.getEmail();

            // Since the user would have passed in the UUID of the BIOS and VMM MLEs, they need to be verified and the
            // data has to be populated into the the TxtHostRecord object
            if (item.getBiosMleUuid() != null && !item.getBiosMleUuid().isEmpty()) {
                TblMleJpaController jpaController = My.jpa().mwMle();
                TblMle bios = jpaController.findTblMleByUUID(item.getBiosMleUuid());
                if (bios != null) {
                    obj.BIOS_Name = bios.getName();
                    obj.BIOS_Oem = bios.getOemId().getName();
                    obj.BIOS_Version = bios.getVersion();
                } else {
                    log.error("UUID specified {} for BIOS MLE is not valid.", item.getBiosMleUuid());
                    throw new ASException(ErrorCode.AS_INVALID_BIOS_MLE, item.getBiosMleUuid());
                }                
            } else {
                log.error("UUID specified for BIOS MLE is not valid.");
                throw new ASException(ErrorCode.AS_INVALID_INPUT);
            }
            
            if (item.getVmmMleUuid()!= null && !item.getVmmMleUuid().isEmpty()) {
                TblMleJpaController jpaController = My.jpa().mwMle();
                TblMle vmm = jpaController.findTblMleByUUID(item.getVmmMleUuid());
                if (vmm != null) {
                    obj.VMM_Name = vmm.getName();
                    obj.VMM_Version = vmm.getVersion();
                    obj.VMM_OSName = vmm.getOsId().getName();
                    obj.VMM_OSVersion = vmm.getOsId().getVersion();
                } else {
                    log.error("UUID specified {} for VMM MLE is not valid.", item.getVmmMleUuid());
                    throw new ASException(ErrorCode.AS_INVALID_VMM_MLE, item.getVmmMleUuid());                    
                }                
            } else {
                log.error("UUID specified for VMM MLE is not valid.");
                throw new ASException(ErrorCode.AS_INVALID_INPUT);
            }
            
            new HostBO().updateHost(new TxtHost(obj), null, null, item.getId().toString());
                        
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during host update.", ex);
            throw new ASException(ErrorCode.AS_UPDATE_HOST_ERROR, ex.getClass().getSimpleName());
        }        
    }

    @Override
    @RequiresPermissions("hosts:create")    
    public void create(Host item) {
        log.debug("Host:Create - Got request to create a new Host.");
        TxtHostRecord obj = new TxtHostRecord();
        try {
            
            obj.HostName = item.getName();
            obj.AddOn_Connection_String = item.getConnectionUrl();
            obj.Description = item.getDescription();
            obj.Email = item.getEmail();

            // Since the user would have passed in the UUID of the BIOS and VMM MLEs, they need to be verified and the
            // data has to be populated into the the TxtHostRecord object
            if (item.getBiosMleUuid() != null && !item.getBiosMleUuid().isEmpty()) {
                TblMleJpaController jpaController = My.jpa().mwMle();
                TblMle bios = jpaController.findTblMleByUUID(item.getBiosMleUuid());
                if (bios != null) {
                    obj.BIOS_Name = bios.getName();
                    obj.BIOS_Oem = bios.getOemId().getName();
                    obj.BIOS_Version = bios.getVersion();
                } else {
                    log.error("UUID specified {} for BIOS MLE is not valid.", item.getBiosMleUuid());
                    throw new ASException(ErrorCode.AS_INVALID_BIOS_MLE, item.getBiosMleUuid());                    
                }                
            } else {
                log.error("UUID specified {} for BIOS MLE is not valid.", item.getBiosMleUuid());
                throw new ASException(ErrorCode.AS_INVALID_BIOS_MLE, item.getBiosMleUuid());
            }
            
            if (item.getVmmMleUuid()!= null && !item.getVmmMleUuid().isEmpty()) {
                TblMleJpaController jpaController = My.jpa().mwMle();
                TblMle vmm = jpaController.findTblMleByUUID(item.getVmmMleUuid());
                if (vmm != null) {
                    obj.VMM_Name = vmm.getName();
                    obj.VMM_Version = vmm.getVersion();
                    obj.VMM_OSName = vmm.getOsId().getName();
                    obj.VMM_OSVersion = vmm.getOsId().getVersion();
                } else {
                    log.error("UUID specified {} for VMM MLE is not valid.", item.getVmmMleUuid());
                    throw new ASException(ErrorCode.AS_INVALID_VMM_MLE, item.getVmmMleUuid());                    
                }                
            } else {
                log.error("UUID specified {} for VMM MLE is not valid.", item.getVmmMleUuid());
                throw new ASException(ErrorCode.AS_INVALID_VMM_MLE, item.getVmmMleUuid());
            }
               
	    /*String tlsPolicyName = My.configuration().getDefaultTlsPolicyName();
	    String[] tlsCerts = null;
	    if(item.getTlsPolicy() != null)  {
		    if (item.getTlsPolicy().getInsecure()) {
                    	tlsPolicyName = TLSPolicy.INSECURE.toString();
		    }
		    if(item.getTlsPolicy().getCertificates() != null) {
                    	tlsPolicyName = TLSPolicy.TRUST_CA_VERIFY_HOSTNAME.toString();
			// Create the keystore here
			tlsCerts = item.getTlsPolicy().getCertificates();
		    }
	    }
	    new HostBO().addHost(new TxtHost(obj), null, null, item.getId().toString(), tlsPolicyName, tlsCerts);*/
	    new HostBO().addHost(new TxtHost(obj), null, null, item.getId().toString());
            log.debug("Host:Create - Created new host {} successfully.", item.getName());
            
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during host creation.", ex);
            throw new ASException(ErrorCode.AS_REGISTER_HOST_ERROR, ex.getClass().getSimpleName());
        }        
    }

    @Override
    @RequiresPermissions("hosts:delete")    
    public void delete(HostLocator locator) {
        if (locator == null || locator.id == null) { return; }
        log.debug("User:Delete - Got request to delete user with id {}.", locator.id.toString());        
        String id = locator.id.toString();
        try {
            new HostBO().deleteHost(null, id);
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during OEM deletion.", ex);
            throw new ASException(ErrorCode.AS_DELETE_HOST_ERROR, ex.getClass().getSimpleName());
        }
    }

    @Override
    @RequiresPermissions("hosts:delete,search")    
    public void delete(HostFilterCriteria criteria) {
        log.debug("Host:Delete - Got request to delete Host by search criteria.");        
        HostCollection objCollection = search(criteria);
        try { 
            for (Host obj : objCollection.getHosts()) {
                HostLocator locator = new HostLocator();
                locator.id = obj.getId();
                delete(locator);
            }
        } catch (Exception ex) {
            log.error("Error during Host deletion.", ex);
            throw new WebApplicationException("Please see the server log for more details.", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
    

    private Host convert(TblHosts obj) {
        Host convObj = new Host();
        convObj.setId(UUID.valueOf(obj.getUuid_hex()));
        convObj.setName(obj.getName());
        convObj.setConnectionUrl(obj.getAddOnConnectionInfo());
        convObj.setDescription(obj.getDescription());
        convObj.setEmail(obj.getEmail());
        convObj.setBiosMleUuid(obj.getBios_mle_uuid_hex());
        convObj.setVmmMleUuid(obj.getVmm_mle_uuid_hex());
        convObj.setAikCertificate(obj.getAIKCertificate());
        convObj.setAikSha1(obj.getAikSha1());
        convObj.setHardwareUuid(obj.getHardwareUuid());
        log.error("------------------------------------" + obj.getHardwareUuid());
        return convObj;
    }
    
}
