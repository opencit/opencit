/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.repository;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.controller.TblMleJpaController;
import com.intel.mtwilson.as.controller.TblOemJpaController;
import com.intel.mtwilson.as.controller.TblOsJpaController;
import com.intel.mtwilson.as.data.MwMleSource;
import com.intel.mtwilson.as.data.TblMle;
import com.intel.mtwilson.as.data.TblOem;
import com.intel.mtwilson.as.data.TblOs;
import com.intel.mtwilson.as.rest.v2.model.Mle;
import com.intel.mtwilson.as.rest.v2.model.MleCollection;
import com.intel.mtwilson.as.rest.v2.model.MleFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.MleLocator;
import com.intel.mtwilson.as.rest.v2.model.MleSource;
import com.intel.mtwilson.datatypes.MleData;
import com.intel.mtwilson.jaxrs2.server.resource.DocumentRepository;
import com.intel.mtwilson.repository.RepositoryCreateException;
import com.intel.mtwilson.repository.RepositoryDeleteException;
import com.intel.mtwilson.repository.RepositoryException;
import com.intel.mtwilson.repository.RepositoryInvalidInputException;
import com.intel.mtwilson.repository.RepositoryRetrieveException;
import com.intel.mtwilson.repository.RepositorySearchException;
import com.intel.mtwilson.repository.RepositoryStoreException;
import com.intel.mtwilson.wlm.business.MleBO;
import java.util.Collection;
import java.util.List;
import org.apache.shiro.authz.annotation.RequiresPermissions;

/**
 *
 * @author ssbangal
 */
public class MleRepository implements DocumentRepository<Mle, MleCollection, MleFilterCriteria, MleLocator> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MleRepository.class);

    @Override
    @RequiresPermissions("mles:search")    
    public MleCollection search(MleFilterCriteria criteria) {
        log.debug("Mle:Search - Got request to search for the Mles.");        
        MleCollection mleCollection = new MleCollection();
        try {
            TblMleJpaController jpaController = My.jpa().mwMle();
            if (criteria.filter == false) {
                List<TblMle> mleList = jpaController.findTblMleEntities();
                if (mleList != null && !mleList.isEmpty()) {
                    for(TblMle mleObj : mleList) {
                        mleCollection.getMles().add(convert(mleObj));
                    }
                }                                
            } else if (criteria.id != null) {
                TblMle tblMle = jpaController.findTblMleByUUID(criteria.id.toString());
                if (tblMle != null) {
                    mleCollection.getMles().add(convert(tblMle));
                }
            } else if (criteria.nameContains != null && !criteria.nameContains.isEmpty()) {
                List<TblMle> mleList = jpaController.findByNameLike(criteria.nameContains);
                if (mleList != null && !mleList.isEmpty()) {
                    for(TblMle mleObj : mleList) {
                        mleCollection.getMles().add(convert(mleObj));
                    }
                }                
            }  else if (criteria.nameEqualTo != null && !criteria.nameEqualTo.isEmpty()) {
                List<TblMle> mleList = jpaController.findByName(criteria.nameEqualTo);
                if (mleList != null && !mleList.isEmpty()) {
                    for(TblMle mleObj : mleList) {
                        mleCollection.getMles().add(convert(mleObj));
                    }
                }                
            } else if (criteria.osUuid != null && !criteria.osUuid.isEmpty()) {
                List<TblMle> mleList = jpaController.findByOsUuid(criteria.osUuid);
                if (mleList != null && !mleList.isEmpty()) {
                    for(TblMle mleObj : mleList) {
                        mleCollection.getMles().add(convert(mleObj));
                    }
                }                
            } else if (criteria.oemUuid != null && !criteria.oemUuid.isEmpty()) {
                List<TblMle> mleList = jpaController.findByOemUuid(criteria.oemUuid);
                if (mleList != null && !mleList.isEmpty()) {
                    for(TblMle mleObj : mleList) {
                        mleCollection.getMles().add(convert(mleObj));
                    }
                }                
            } else if (criteria.mleType != null) {
                List<TblMle> mleList = jpaController.findByMleType(criteria.mleType.name());
                if (mleList != null && !mleList.isEmpty()) {
                    for(TblMle mleObj : mleList) {
                        mleCollection.getMles().add(convert(mleObj));
                    }
                }                
            }
        } catch (Exception ex) {
            log.error("Mle:Search - Error during MLE search.", ex);
            throw new RepositorySearchException(ex, criteria);
        }
        log.debug("Mle:Search - Returning back {} of results.", mleCollection.getMles().size());                
        return mleCollection;
    }

    @Override
    @RequiresPermissions("mles:retrieve")    
    public Mle retrieve(MleLocator locator) {
        if( locator == null || locator.id == null ) { return null; }
        log.debug("Mle:Retrieve - Got request to retrieve Mle with id {}.", locator.id);                
        String id = locator.id.toString();
        try {
            TblMleJpaController jpaController = My.jpa().mwMle();
            TblMle tblMle = jpaController.findTblMleByUUID(id); 
            if (tblMle != null) {
                Mle mle = convert(tblMle);
                return mle;
            } 
        } catch (Exception ex) {
            log.error("Mle:Retrieve - Error during MLE search.", ex);
            throw new RepositoryRetrieveException(ex, locator);
        }
        return null;
    }

    @Override
    @RequiresPermissions("mles:store")    
    public void store(Mle item) {
        log.debug("Mle:Store - Got request to update Mle with id {}.", item.getId().toString());        
        MleLocator locator = new MleLocator();
        locator.id = item.getId();

        try {
            // Only the description and the PCR white lists are editable.
            MleData obj = new MleData();

            obj.setDescription(item.getDescription());
            obj.setManifestList(item.getMleManifests());
                
            new MleBO().updateMle(obj, item.getId().toString());
            log.debug("Mle:Store - Successfully updated Mle with id {}.", item.getId().toString());            
        } catch (Exception ex) {
            log.error("Mle:Store - Error during Mle update.", ex);
            throw new RepositoryStoreException(ex, locator);
        }
    }

    @Override
    @RequiresPermissions("mles:create")    
    public void create(Mle item) {
        log.debug("Mle:Create - Got request to create a new Mle.");
        MleLocator locator = new MleLocator();
        locator.id = item.getId();

        try {
            // Since the new APIs accept the UUID of the OEM and OS associated with the MLE, we need to verify the UUIDs
            // then form the MleData object before calling into the business layer.
            MleData obj = new MleData();
            obj.setName(item.getName());
            obj.setVersion(item.getVersion());
            obj.setDescription(item.getDescription());
            obj.setAttestationType(item.getAttestationType().toString());
            obj.setMleType(item.getMleType().toString());
            obj.setManifestList(item.getMleManifests());
                        
            // If the MLE type is BIOS, then the user has to have specified the OEM UUID
            if (item.getMleType() == Mle.MleType.BIOS) {
                TblOemJpaController oemJpaController = My.jpa().mwOem();
                TblOem tblOem = oemJpaController.findTblOemByUUID(item.getOemUuid());
                if (tblOem != null) {
                    obj.setOemName(tblOem.getName());
                } else {
                    log.error("Mle:Create - The OEM specified with UUID {} does not exist.", item.getOemUuid());
                    throw new RepositoryInvalidInputException(locator);
                }                
            } else {
                TblOsJpaController osJpaController = My.jpa().mwOs();            
                TblOs tblOs = osJpaController.findTblOsByUUID(item.getOsUuid());
                if (tblOs != null) {
                    obj.setOsName(tblOs.getName());
                    obj.setOsVersion(tblOs.getVersion());
                } else {
                    log.error("Mle:Create - The OS specified with UUID {} does not exist.", item.getOsUuid());
                    throw new RepositoryInvalidInputException(locator);
                }                                
            }
            
            // Call into the business layer to create the MLE
            new MleBO().addMLe(obj, item.getId().toString());
            
            // Check if the user has provided the mle source (the host from which the white list is being added)
            if (item.getSource() != null && !item.getSource().isEmpty())
            {
                log.debug("Configuring Mle source host {} for mle {}", item.getSource(), item.getId().toString());
                MleSourceRepository sourceRepo = new MleSourceRepository();
                MleSource mleSource = new MleSource();
                mleSource.setId(new UUID());
                mleSource.setName(item.getSource());
                mleSource.setMleUuid(item.getId().toString());
                sourceRepo.create(mleSource);
            }
            log.debug("Mle:Create - Successfully created the new Mle with name {}.", item.getName());
            
        } catch (RepositoryException re) {
            throw re;
        } catch (Exception ex) {
            log.error("Mle:Create - Error during MLE creation.", ex);
            throw new RepositoryCreateException(ex, locator);
        }
    }

    @Override
    @RequiresPermissions("mles:delete")    
    public void delete(MleLocator locator) {
        if( locator == null || locator.id == null ) { return; }
        log.debug("Mle:Delete - Got request to delete Mle with id {}.", locator.id.toString());        
        String id = locator.id.toString();
        try {
            new MleBO().deleteMle(null, null, null, null, null, id);
        } catch (Exception ex) {
            log.error("Mle:Delete - Error during MLE delete.", ex);
            throw new RepositoryDeleteException(ex, locator);
        }
    }
    
    @Override
    @RequiresPermissions("mles:delete,search")    
    public void delete(MleFilterCriteria criteria) {
        log.debug("Mle:Delete - Got request to delete Mle by search criteria.");        
        MleCollection objCollection = search(criteria);
        try { 
            for (Mle obj : objCollection.getMles()) {
                MleLocator locator = new MleLocator();
                locator.id = obj.getId();
                delete(locator);
            }
        } catch (RepositoryException re) {
            throw re;
        } catch (Exception ex) {
            log.error("Mle:Delete - Error during MLE delete.", ex);
            throw new RepositoryDeleteException(ex);
        }
    }
    
    private Mle convert(TblMle tblMleObj) {
        Mle mle = new Mle();
        mle.setId(UUID.valueOf(tblMleObj.getUuid_hex()));
        mle.setName(tblMleObj.getName());
        mle.setVersion(tblMleObj.getVersion());
        mle.setAttestationType(Mle.AttestationType.valueOf(tblMleObj.getAttestationType()));
        mle.setMleType(Mle.MleType.valueOf(tblMleObj.getMLEType()));
        if (tblMleObj.getMLEType().equalsIgnoreCase(Mle.MleType.BIOS.name())) {
            mle.setOemUuid(tblMleObj.getOemId().getUuid_hex());
            mle.setOsUuid(null);
        } else {
            mle.setOemUuid(null);
            mle.setOsUuid(tblMleObj.getOsId().getUuid_hex());
        }
        mle.setDescription(tblMleObj.getDescription());   
        // Since there will be only one entry per MLE in the MleSource table, we will try to get it and return it back to the caller
        Collection<MwMleSource> mwMleSourceCollection = tblMleObj.getMwMleSourceCollection();
        if (mwMleSourceCollection != null && !mwMleSourceCollection.isEmpty()) {
            MwMleSource mleSource = (MwMleSource) mwMleSourceCollection.toArray()[0];
            mle.setSource(mleSource.getHostName());
        }
        if (tblMleObj.getTarget_type() != null && !tblMleObj.getTarget_type().isEmpty()) {
            mle.setTargetType(tblMleObj.getTarget_type());
            mle.setTargetValue(tblMleObj.getTarget_value());
        }
        return mle;
    }
    
}
