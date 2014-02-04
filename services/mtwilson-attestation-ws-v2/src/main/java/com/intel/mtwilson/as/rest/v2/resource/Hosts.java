/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.as.rest.v2.resource;

import com.intel.mtwilson.as.rest.v2.model.Host;
import com.intel.mtwilson.as.rest.v2.model.HostCollection;
import com.intel.mtwilson.as.rest.v2.model.HostFilterCriteria;
import com.intel.mtwilson.jersey.resource.AbstractResource;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.controller.TblHostsJpaController;
import com.intel.mtwilson.as.controller.TblMleJpaController;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.mtwilson.as.data.TblMle;
import com.intel.mtwilson.as.rest.v2.model.HostLinks;
import com.intel.mtwilson.datatypes.ErrorCode;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import com.intel.mtwilson.launcher.ws.ext.V2;
import com.intel.mtwilson.as.business.HostBO;
import com.intel.mtwilson.datatypes.TxtHost;
import com.intel.mtwilson.wlm.business.OemBO;

import java.util.List;
import javax.ejb.Stateless;
import javax.ws.rs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
@V2
@Stateless
@Path("/hosts")
public class Hosts extends AbstractResource<Host,HostCollection,HostFilterCriteria,HostLinks> {

    private Logger log = LoggerFactory.getLogger(getClass().getName());
    
    public Hosts() {
        super();
    }
    
    @Override
    protected HostCollection search(HostFilterCriteria criteria) {
        HostCollection objCollection = new HostCollection();
        try {
            TblHostsJpaController jpaController = My.jpa().mwHosts();
            if (criteria.id != null) {
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
        return objCollection;
    }

    @Override
    protected Host retrieve(String id) {
        if( id == null ) { return null; }
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
            log.error("Error during OEM retrieval.", ex);
            throw new ASException(ErrorCode.WS_OEM_RETRIEVAL_ERROR, ex.getClass().getSimpleName());
        }
        return null;
    }

    @Override
    protected void store(Host item) {
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
                    log.error("UUID specified {} for BIOS MLE is not valid.", item.getBiosMleUuid().toString());
                    throw new ASException(ErrorCode.AS_INVALID_BIOS_MLE, item.getBiosMleUuid().toString());                    
                }                
            } else {
                log.error("UUID specified {} for BIOS MLE is not valid.", item.getBiosMleUuid().toString());
                throw new ASException(ErrorCode.AS_INVALID_BIOS_MLE, item.getBiosMleUuid().toString());
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
                    log.error("UUID specified {} for VMM MLE is not valid.", item.getVmmMleUuid().toString());
                    throw new ASException(ErrorCode.AS_INVALID_VMM_MLE, item.getVmmMleUuid().toString());                    
                }                
            } else {
                log.error("UUID specified {} for VMM MLE is not valid.", item.getVmmMleUuid().toString());
                throw new ASException(ErrorCode.AS_INVALID_VMM_MLE, item.getVmmMleUuid().toString());
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
    protected void create(Host item) {
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
                    log.error("UUID specified {} for BIOS MLE is not valid.", item.getBiosMleUuid().toString());
                    throw new ASException(ErrorCode.AS_INVALID_BIOS_MLE, item.getBiosMleUuid().toString());                    
                }                
            } else {
                log.error("UUID specified {} for BIOS MLE is not valid.", item.getBiosMleUuid().toString());
                throw new ASException(ErrorCode.AS_INVALID_BIOS_MLE, item.getBiosMleUuid().toString());
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
                    log.error("UUID specified {} for VMM MLE is not valid.", item.getVmmMleUuid().toString());
                    throw new ASException(ErrorCode.AS_INVALID_VMM_MLE, item.getVmmMleUuid().toString());                    
                }                
            } else {
                log.error("UUID specified {} for VMM MLE is not valid.", item.getVmmMleUuid().toString());
                throw new ASException(ErrorCode.AS_INVALID_VMM_MLE, item.getVmmMleUuid().toString());
            }
            
            new HostBO().addHost(new TxtHost(obj), null, null, item.getId().toString());
                        
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during host creation.", ex);
            throw new ASException(ErrorCode.AS_REGISTER_HOST_ERROR, ex.getClass().getSimpleName());
        }        
    }

    @Override
    protected void delete(String id) {
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
    protected HostCollection createEmptyCollection() {
        return new HostCollection();
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
        return convObj;
    }
    
}
