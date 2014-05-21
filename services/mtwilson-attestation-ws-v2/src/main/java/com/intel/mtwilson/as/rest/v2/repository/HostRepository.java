/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.as.rest.v2.repository;

import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.io.Resource;
import com.intel.mtwilson.as.rest.v2.model.Host;
import com.intel.mtwilson.as.rest.v2.model.HostCollection;
import com.intel.mtwilson.as.rest.v2.model.HostFilterCriteria;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.tls.policy.TlsUtil;
import com.intel.dcsg.cpg.util.ByteArray;
import com.intel.dcsg.cpg.x509.X509Builder;
import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.controller.TblHostsJpaController;
import com.intel.mtwilson.as.controller.TblMleJpaController;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.mtwilson.as.data.TblMle;
import com.intel.mtwilson.as.rest.v2.model.HostLocator;
import com.intel.mtwilson.datatypes.ErrorCode;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import com.intel.mtwilson.as.business.HostBO;
import com.intel.mtwilson.as.rest.v2.model.HostTlsPolicy;
import com.intel.mtwilson.datatypes.HostResponse;
import com.intel.mtwilson.datatypes.TLSPolicy;
import com.intel.mtwilson.datatypes.TxtHost;
import com.intel.mtwilson.jersey.resource.SimpleRepository;
import java.io.ByteArrayInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateFactorySpi;
import java.security.cert.X509Certificate;

import java.util.List;
import org.apache.commons.codec.binary.Base64;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.bouncycastle.jce.provider.JDKKeyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class HostRepository implements SimpleRepository<Host,HostCollection,HostFilterCriteria,HostLocator> {

    private Logger log = LoggerFactory.getLogger(getClass().getName());
    
    @Override
    @RequiresPermissions("hosts:search")    
    public HostCollection search(HostFilterCriteria criteria) {
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
    @RequiresPermissions("hosts:retrieve")    
    public Host retrieve(HostLocator locator) {
        if( locator == null || locator.id == null ) { return null; }
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
                    log.error("UUID specified {} for VMM MLE is not valid.", item.getVmmMleUuid().toString());
                    throw new ASException(ErrorCode.AS_INVALID_VMM_MLE, item.getVmmMleUuid().toString());                    
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
               
	    new HostBO().addHost(new TxtHost(obj), null, null, item.getId().toString(), tlsPolicyName, tlsCerts);
           
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
        // TODO: Call into the search function and delete all the items. Low priority for WW 14
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
