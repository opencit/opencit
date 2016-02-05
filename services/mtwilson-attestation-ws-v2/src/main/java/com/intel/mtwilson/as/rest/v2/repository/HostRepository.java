/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.as.rest.v2.repository;

import com.intel.mtwilson.as.rest.v2.model.Host;
import com.intel.mtwilson.as.rest.v2.model.HostCollection;
import com.intel.mtwilson.as.rest.v2.model.HostFilterCriteria;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.controller.TblHostsJpaController;
import com.intel.mtwilson.as.controller.TblMleJpaController;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.mtwilson.as.data.TblMle;
import com.intel.mtwilson.as.rest.v2.model.HostLocator;
import com.intel.mtwilson.as.business.HostBO;
import com.intel.mtwilson.datatypes.TxtHost;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import com.intel.mtwilson.jaxrs2.server.resource.DocumentRepository;
import com.intel.mtwilson.repository.RepositoryCreateException;
import com.intel.mtwilson.repository.RepositoryDeleteException;
import com.intel.mtwilson.repository.RepositoryException;
import com.intel.mtwilson.repository.RepositoryInvalidInputException;
import com.intel.mtwilson.repository.RepositoryRetrieveException;
import com.intel.mtwilson.repository.RepositorySearchException;
import com.intel.mtwilson.repository.RepositoryStoreException;
import com.intel.mtwilson.tls.policy.TlsPolicyChoice;
import com.intel.mtwilson.tls.policy.jdbi.TlsPolicyDAO;
import com.intel.mtwilson.tls.policy.jdbi.TlsPolicyJdbiFactory;
import java.util.List;
import org.apache.shiro.authz.annotation.RequiresPermissions;

/**
 *
 * @author jbuhacoff
 */
public class HostRepository implements DocumentRepository<Host,HostCollection,HostFilterCriteria,HostLocator> {

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
                TblHosts obj = jpaController.findByName(criteria.nameEqualTo);
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
        } catch (Exception ex) {
            log.error("Host:Search - Error during search for hosts.", ex);
            throw new RepositorySearchException(ex, criteria);
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
        } catch (Exception ex) {
            log.error("Host:Retrieve - Error during search for hosts.", ex);
            throw new RepositoryRetrieveException(ex, locator);
        }
        return null;
    }

    @Override
    @RequiresPermissions("hosts:store")    
    public void store(Host item) {
        log.debug("Host:Store - Got request to update Host with id {}.", item.getId().toString());
        HostLocator locator = new HostLocator();
        locator.id = item.getId();
        
        TxtHostRecord obj = new TxtHostRecord();
        try {
            TblHostsJpaController hostJpaController = My.jpa().mwHosts();
            TblHosts tblHost = hostJpaController.findHostByUuid(item.getId().toString());
            if (tblHost == null) {
                log.error("Host:Store - Host specified with UUID {} is not valid.", item.getId().toString());
                throw new RepositoryInvalidInputException(locator);                                        
            }

            // Bug: 4391 - We should not allow the update to host name
            if (!tblHost.getName().equals(item.getName())) {
                log.error("Host:Store - Host name specified {} does not exist. Host name cannot be updated.", item.getName());
                throw new RepositoryInvalidInputException(locator);                                                        
            }
            
            obj.HostName = tblHost.getName();
            if (item.getConnectionUrl() != null && !item.getConnectionUrl().isEmpty())
                obj.AddOn_Connection_String = item.getConnectionUrl();
            if (item.getDescription() != null && !item.getDescription().isEmpty())
                obj.Description = item.getDescription();
            if (item.getEmail() != null && !item.getEmail().isEmpty())
                obj.Email = item.getEmail();

            // Bug: 4390 - Validate the TLS Policy before updating.
            TlsPolicyChoice tlsPolicyChoice = new TlsPolicyChoice();
            if (("TRUST_FIRST_CERTIFICATE".equals(item.getTlsPolicyId())) || ("INSECURE".equals(item.getTlsPolicyId()))) {
                tlsPolicyChoice.setTlsPolicyId(item.getTlsPolicyId());
            } else {
                // Validate the tls policy against the DB
                try (TlsPolicyDAO dao = TlsPolicyJdbiFactory.tlsPolicyDAO()) {
                    if ((dao.findTlsPolicyById(item.getId()) != null) || (dao.findTlsPolicyByNameEqualTo(item.getName()) != null)) {
                        tlsPolicyChoice.setTlsPolicyId(item.getTlsPolicyId());
                    } else {
                        log.error("Host:Store - TLSPolicy specified {} is not valid.", item.getTlsPolicyId());
                        throw new RepositoryInvalidInputException(locator);                        
                    }

                }                
            }
            obj.tlsPolicyChoice = tlsPolicyChoice;            
            
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
                    log.error("Host:Store - UUID specified {} for BIOS MLE is not valid.", item.getBiosMleUuid());
                    throw new RepositoryInvalidInputException(locator);
                }                
            } else {
                log.error("Host:Store - UUID specified for BIOS MLE is not valid.");
                throw new RepositoryInvalidInputException(locator);
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
                    log.error("Host:Store - UUID specified {} for VMM MLE is not valid.", item.getVmmMleUuid());
                    throw new RepositoryInvalidInputException(locator);                    
                }                
            } else {
                log.error("Host:Store - UUID specified for VMM MLE is not valid.");
                throw new RepositoryInvalidInputException(locator);
            }
            
            new HostBO().updateHost(new TxtHost(obj), null, null, item.getId().toString());
                        
        } catch (RepositoryException re) {
            throw re;            
        } catch (Exception ex) {
            log.error("Host:Store - Error during host update.", ex);
            throw new RepositoryStoreException(ex, locator);
        }        
    }

    @Override
    @RequiresPermissions("hosts:create")    
    public void create(Host item) {
        log.debug("Host:Create - Got request to create a new Host.");
        HostLocator locator = new HostLocator();
        locator.id = item.getId();

        TxtHostRecord obj = new TxtHostRecord();
        try {

            obj.HostName = item.getName();
            obj.AddOn_Connection_String = item.getConnectionUrl();
            obj.Description = item.getDescription();
            obj.Email = item.getEmail();

            // Validate the TLS Policy before updating.
            TlsPolicyChoice tlsPolicyChoice = new TlsPolicyChoice();
            if (("TRUST_FIRST_CERTIFICATE".equals(item.getTlsPolicyId())) || ("INSECURE".equals(item.getTlsPolicyId()))) {
                tlsPolicyChoice.setTlsPolicyId(item.getTlsPolicyId());
            } else {
                // Validate the tls policy against the DB
                try (TlsPolicyDAO dao = TlsPolicyJdbiFactory.tlsPolicyDAO()) {
                    if ((dao.findTlsPolicyById(item.getId()) != null) || (dao.findTlsPolicyByNameEqualTo(item.getName()) != null)) {
                        tlsPolicyChoice.setTlsPolicyId(item.getTlsPolicyId());
                    } else {
                        log.error("Host:Store - TLSPolicy specified {} is not valid.", item.getTlsPolicyId());
                        throw new RepositoryInvalidInputException(locator);                        
                    }

                }                
            }
            obj.tlsPolicyChoice = tlsPolicyChoice;            

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
                    log.error("Host:Create - UUID specified {} for BIOS MLE is not valid.", item.getBiosMleUuid());
                    throw new RepositoryInvalidInputException(locator);                    
                }                
            } else {
                log.error("Host:Create - UUID specified {} for BIOS MLE is not valid.", item.getBiosMleUuid());
                throw new RepositoryInvalidInputException(locator);
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
                    log.error("Host:Create - UUID specified {} for VMM MLE is not valid.", item.getVmmMleUuid());
                    throw new RepositoryInvalidInputException(locator);                    
                }                
            } else {
                log.error("Host:Create - UUID specified {} for VMM MLE is not valid.", item.getVmmMleUuid());
                throw new RepositoryInvalidInputException(locator);
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
            
        } catch (RepositoryException re) {
            throw re;            
        } catch (Exception ex) {
            log.error("Host:Create - Error during host creation.", ex);
            throw new RepositoryCreateException(ex, locator);
        }        
    }

    @Override
    @RequiresPermissions("hosts:delete")    
    public void delete(HostLocator locator) {
        if (locator == null || locator.id == null) { return; }
        log.debug("Host:Delete - Got request to delete host with id {}.", locator.id.toString());        
        String id = locator.id.toString();
        try {
            new HostBO().deleteHost(null, id);
        } catch (Exception ex) {
            log.error("Host:Delete - Error during Host deletion.", ex);
            throw new RepositoryDeleteException(ex, locator);
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
        } catch (RepositoryException re) {
            throw re;            
        } catch (Exception ex) {
            log.error("Host:Delete - Error during Host deletion.", ex);
            throw new RepositoryDeleteException(ex);
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
        convObj.setTlsPolicyId(obj.getTlsPolicyId());
        log.debug("------------------------------------" + obj.getHardwareUuid());
        return convObj;
    }
    
}
