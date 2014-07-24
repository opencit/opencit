/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tpm.endorsement.repository;

import com.intel.mtwilson.My;
import com.intel.mtwilson.jaxrs2.server.resource.DocumentRepository;
import com.intel.mtwilson.repository.RepositoryCreateConflictException;
import com.intel.mtwilson.repository.RepositoryCreateException;
import com.intel.mtwilson.repository.RepositoryDeleteException;
import com.intel.mtwilson.repository.RepositoryException;
import com.intel.mtwilson.repository.RepositoryRetrieveException;
import com.intel.mtwilson.repository.RepositorySearchException;
import com.intel.mtwilson.repository.RepositoryStoreException;
import com.intel.mtwilson.tpm.endorsement.model.TpmEndorsement;
import com.intel.mtwilson.tpm.endorsement.model.TpmEndorsementCollection;
import com.intel.mtwilson.tpm.endorsement.model.TpmEndorsementFilterCriteria;
import com.intel.mtwilson.tpm.endorsement.model.TpmEndorsementLocator;
import com.intel.mtwilson.tpm.endorsement.jdbi.TpmEndorsementDAO;
import com.intel.mtwilson.tpm.endorsement.jdbi.TpmEndorsementJdbiFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import org.apache.shiro.authz.annotation.RequiresPermissions;

/**
 *
 * @author jbuhacoff
 */
public class TpmEndorsementRepository implements DocumentRepository<TpmEndorsement, TpmEndorsementCollection, TpmEndorsementFilterCriteria, TpmEndorsementLocator> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TpmEndorsementRepository.class);

    @Override
    @RequiresPermissions("tpm_endorsements:search")
    public TpmEndorsementCollection search(TpmEndorsementFilterCriteria criteria) {
        TpmEndorsementCollection objCollection = new TpmEndorsementCollection();
        log.debug("TpmEndorsementRepository search");

        try (TpmEndorsementDAO dao = TpmEndorsementJdbiFactory.tpmEndorsementDAO()) {
            List<TpmEndorsement> result = new ArrayList<>();
            if (criteria.filter) {
                // id search
                if (criteria.id != null) {
                    String[] ids = criteria.id.split("\\s*,\\s*");
                    log.debug("TpmEndorsementRepository search ids: {}", (Object[]) ids);
                    HashSet<String> set = new HashSet<>(Arrays.asList(ids));
                    List<TpmEndorsement> tpmEndorsementRecords = dao.findTpmEndorsementByIds(set);
                    if (tpmEndorsementRecords != null) {
                        result.addAll(tpmEndorsementRecords);
                    }
                } else if (criteria.hardwareUuidEqualTo != null) {
                    TpmEndorsement tpmEndorsementRecord = dao.findTpmEndorsementByHardwareUuidEqualTo(criteria.hardwareUuidEqualTo);
                    if (tpmEndorsementRecord != null) {
                        result.add(tpmEndorsementRecord);
                    }
                } else if (criteria.revokedEqualTo != null) {
                    List<TpmEndorsement> tpmEndorsementRecords = dao.findTpmEndorsementByRevokedEqualTo(criteria.revokedEqualTo.booleanValue());
                    if (tpmEndorsementRecords != null) {
                        result.addAll(tpmEndorsementRecords);
                    }
                } else if (criteria.issuerEqualTo != null) {
                    TpmEndorsement tpmEndorsementRecord = dao.findTpmEndorsementByIssuerEqualTo(criteria.issuerEqualTo);
                    if (tpmEndorsementRecord != null) {
                        result.add(tpmEndorsementRecord);
                    }
                } else if (criteria.issuerContains != null) {
                    List<TpmEndorsement> tpmEndorsementRecords = dao.findTpmEndorsementByIssuerContains(criteria.issuerContains);
                    if (tpmEndorsementRecords != null) {
                        result.addAll(tpmEndorsementRecords);
                    }
                } else if (criteria.commentEqualTo != null) {
                    TpmEndorsement tpmEndorsementRecord = dao.findTpmEndorsementByCommentEqualTo(criteria.commentEqualTo);
                    if (tpmEndorsementRecord != null) {
                        result.add(tpmEndorsementRecord);
                    }
                } else if (criteria.commentContains != null) {
                    List<TpmEndorsement> tpmEndorsementRecords = dao.findTpmEndorsementByCommentContains(criteria.commentContains);
                    if (tpmEndorsementRecords != null) {
                        result.addAll(tpmEndorsementRecords);
                    }
                }
            } else {
                // no filter, get all records
                List<TpmEndorsement> tpmEndorsementRecords = dao.findAllTpmEndorsement();
                if (tpmEndorsementRecords != null) {
                    result.addAll(tpmEndorsementRecords);
                }
            }
            objCollection.getTpmEndorsements().addAll(result);

        } catch (IOException ex) {
            log.error("TpmEndorsement:Search - Error during TpmEndorsement search.", ex);
            throw new RepositorySearchException(ex, criteria);
        }
        log.debug("TpmEndorsement:Search - Returning back {} of results.", objCollection.getTpmEndorsements().size());                
        return objCollection;
    }
    /*

     TblHostsJpaController jpaController = My.jpa().mwHosts();
     // when searching for a private tpmEndorsement, the tpmEndorsement name is the host uuid
     if (criteria.privateEqualTo != null && criteria.privateEqualTo.booleanValue() && criteria.nameEqualTo != null) {
     TblHosts obj = jpaController.findHostByUuid(criteria.nameEqualTo.toString());
     if (obj != null) {
     objCollection.getTpmEndorsements().add(convert(obj));
     }
     }
     * 
     */

    @Override
    @RequiresPermissions("tpm_endorsements:retrieve")
    public TpmEndorsement retrieve(TpmEndorsementLocator locator) {
        if (locator.id == null) {
            return null;
        }
        log.debug("TpmEndorsementRepository retrieve id {}", locator.id);                        
        try (TpmEndorsementDAO dao = TpmEndorsementJdbiFactory.tpmEndorsementDAO()) {
            TpmEndorsement tpmEndorsementRecord = dao.findTpmEndorsementById(locator.id);
            return tpmEndorsementRecord;
        } catch (IOException ex) {
            log.error("TpmEndorsement:Retrieve - Error during TpmEndorsement retrieval.", ex);
            throw new RepositoryRetrieveException(ex, locator);
        }
    }

    @Override
    @RequiresPermissions("tpm_endorsements:store")
    public void store(TpmEndorsement item) {
        log.debug("TpmEndorsementRepository store id {}", item.getId().toString());        
        if (item.getId() == null) {
            return;
        }
        TpmEndorsementLocator locator = new TpmEndorsementLocator();
        locator.id = item.getId();
        try (TpmEndorsementDAO dao = TpmEndorsementJdbiFactory.tpmEndorsementDAO()) {
            dao.updateTpmEndorsement(item);
        } catch (IOException ex) {
            log.error("TpmEndorsementRepository store error: {}", ex.getMessage());
            throw new RepositoryStoreException(ex, locator);
        }

    }
    /*

     try {
     TblHostsJpaController jpaController = My.jpa().mwHosts();
     TblHosts obj = jpaController.findHostByUuid(item.getHostUuid()); 
     if (obj != null) {
     obj.setTpmEndorsementName(item.getName());
     //                obj.setTlsKeystore(item.getKeyStore());
     jpaController.edit(obj);
     }
     } catch (ASException aex) {
     throw aex;            
     } catch (Exception ex) {
     log.error("Error during update of host Tls Policy.", ex);
     throw new ASException(ErrorCode.AS_UPDATE_HOST_ERROR, ex.getClass().getSimpleName());
     */

    @Override
    @RequiresPermissions("tpm_endorsements:create")
    public void create(TpmEndorsement item) {
        log.debug("TpmEndorsementRepository create");
        
        if (item.getId() == null) {
            return;
        }
        TpmEndorsementLocator locator = new TpmEndorsementLocator();
        locator.id = item.getId();

        try (TpmEndorsementDAO dao = TpmEndorsementJdbiFactory.tpmEndorsementDAO()) {
            if ((dao.findTpmEndorsementById(item.getId()) != null) || (dao.findTpmEndorsementByHardwareUuidEqualTo(item.getHardwareUuid()) != null)) {
                log.error("TpmEndorsementRepository create - TpmEndorsement {} will not be created since a duplicate TpmEndorsement already exists.", item.getId().toString());                
                throw new RepositoryCreateConflictException(locator);
            }
            
            dao.insertTpmEndorsement(item);
        } catch (IOException ex) {
            log.error("TpmEndorsementRepository create error during TpmEndorsement creation.", ex);
            throw new RepositoryCreateException(ex, locator);
        }
    }

    @Override
    @RequiresPermissions("tpm_endorsements:delete")
    public void delete(TpmEndorsementLocator locator) {
        if (locator.id == null) {
            return;
        }
        log.debug("TpmEndorsementRepository delete id {}.", locator.id.toString());        
        
        try (TpmEndorsementDAO dao = TpmEndorsementJdbiFactory.tpmEndorsementDAO()) {
            dao.deleteTpmEndorsementById(locator.id);
        } catch (IOException ex) {
            log.error("TpmEndorsement:Delete - Error during TpmEndorsement deletion.", ex);
            throw new RepositoryDeleteException(ex, locator);
        }
    }

    @Override
    @RequiresPermissions("tpm_endorsements:delete,search")
    public void delete(TpmEndorsementFilterCriteria criteria) {
        log.debug("TpmEndorsementRepository delete by search criteria");        
        try {
            TpmEndorsementCollection result = search(criteria);
            for (TpmEndorsement hostTpmEndorsement : result.getTpmEndorsements()) {
                TpmEndorsementLocator locator = new TpmEndorsementLocator();
                locator.id = hostTpmEndorsement.getId();
                delete(locator);
            }
        } catch (RepositoryException re) {
            throw re;
        } catch (Exception ex) {
            log.error("TpmEndorsement:Delete - Error during TpmEndorsement deletion.", ex);
            throw new RepositoryDeleteException(ex);
        }
    }


}
