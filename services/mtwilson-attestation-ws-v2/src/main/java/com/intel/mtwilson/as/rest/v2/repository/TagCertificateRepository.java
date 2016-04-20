/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.repository;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.controller.MwAssetTagCertificateJpaController;
import com.intel.mtwilson.as.data.MwAssetTagCertificate;
import com.intel.mtwilson.as.rest.v2.model.TagCertificate;
import com.intel.mtwilson.as.rest.v2.model.TagCertificateCollection;
import com.intel.mtwilson.as.rest.v2.model.TagCertificateFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.TagCertificateLocator;
import com.intel.mtwilson.as.business.AssetTagCertBO;
import com.intel.mtwilson.datatypes.AssetTagCertCreateRequest;
import com.intel.mtwilson.datatypes.AssetTagCertRevokeRequest;
import com.intel.mtwilson.jaxrs2.server.resource.DocumentRepository;
import com.intel.mtwilson.repository.RepositoryCreateException;
import com.intel.mtwilson.repository.RepositoryDeleteException;
import com.intel.mtwilson.repository.RepositoryException;
import com.intel.mtwilson.repository.RepositoryRetrieveException;
import com.intel.mtwilson.repository.RepositorySearchException;
import java.util.List;
import org.apache.shiro.authz.annotation.RequiresPermissions;

/**
 *
 * @author ssbangal
 */
public class TagCertificateRepository implements DocumentRepository<TagCertificate, TagCertificateCollection, TagCertificateFilterCriteria, TagCertificateLocator> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TagCertificateRepository.class);
        
    @Override
    @RequiresPermissions("tag_certificates:search")
    public TagCertificateCollection search(TagCertificateFilterCriteria criteria) {
        log.debug("TagCertificate:Search - Got request to search for the TagCertificates.");        
        TagCertificateCollection objCollection = new TagCertificateCollection();
        try {
            MwAssetTagCertificateJpaController jpaController = My.jpa().mwAssetTagCertificate();
            if (criteria.id != null) {
                List<MwAssetTagCertificate> objList = jpaController.findAssetTagCertificatesByUuid(criteria.id.toString());
                if (objList != null && !objList.isEmpty()) {
                    for(MwAssetTagCertificate obj : objList) {
                        objCollection.getTagCertificates().add(convert(obj));
                    }
                }                
            } else if (criteria.hostUuid != null) {
                List<MwAssetTagCertificate> objList = jpaController.findAssetTagCertificatesByHostUUID(criteria.hostUuid.toString());
                if (objList != null && !objList.isEmpty()) {
                    for(MwAssetTagCertificate obj : objList) {
                        objCollection.getTagCertificates().add(convert(obj));
                    }
                }                
            }

        } catch (Exception ex) {
            log.error("TagCertificate:Search - Error during retrieval of asset tag certificate.", ex);
            throw new RepositorySearchException(ex, criteria);
        }
        log.debug("TagCertificate:Search - Returning back {} of results.", objCollection.getTagCertificates().size());                
        return objCollection;
    }

    @Override
    @RequiresPermissions("tag_certificates:retrieve")
    public TagCertificate retrieve(TagCertificateLocator locator) {
        if( locator == null || locator.id == null ) { return null; }
        log.debug("TagCertificate:Retrieve - Got request to retrieve TagCertificate with id {}.", locator.id);                
        String id = locator.id.toString();
        try {
            MwAssetTagCertificateJpaController jpaController = My.jpa().mwAssetTagCertificate();
            List<MwAssetTagCertificate> objList = jpaController.findAssetTagCertificatesByUuid(id);
            if (!objList.isEmpty()) { // since the jpa controller either returns valida data or an empty list
                TagCertificate convObj = convert(objList.get(0)); // there should always be only one row matching.
                return convObj;
            }
        } catch (Exception ex) {
            log.error("TagCertificate:Retrieve - Error during retrieval of asset tag certificate.", ex);
            throw new RepositoryRetrieveException(ex, locator);
        }
        return null;
    }

    @Override
    public void store(TagCertificate item) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.    
    }

    @Override
    @RequiresPermissions("tag_certificates:import")
    public void create(TagCertificate item) {
        log.debug("TagCertificate:Create - Got request to import a new TagCertificate.");
        TagCertificateLocator locator = new TagCertificateLocator();
        locator.id = item.getId();
        
        AssetTagCertCreateRequest obj = new AssetTagCertCreateRequest();
        try {
            obj.setCertificate(item.getCertificate());
            new AssetTagCertBO().importAssetTagCertificate(obj, item.getId().toString());
            log.debug("TagCertificate:Create - Imported the TagCertificate successfully.");
        } catch (Exception ex) {
            log.error("TagCertificate:Create - Error during import of asset tag certificate.", ex);
            throw new RepositoryCreateException(ex, locator);
        }        
    }

    @Override
    @RequiresPermissions("tag_certificates:revoke")
    public void delete(TagCertificateLocator locator) {
        if (locator == null || locator.id == null) { return; }
        log.debug("TagCertificate:Delete - Got request to revoke TagCertificate with id {}.", locator.id.toString());        
        AssetTagCertRevokeRequest obj = new AssetTagCertRevokeRequest();
        try {
            obj.setSha1OfAssetCert(null);
            new AssetTagCertBO().revokeAssetTagCertificate(obj, locator.id.toString());
            log.debug("TagCertificate:Delete - Revoked the TagCertificate with id {} successfully.", locator.id.toString());        
        } catch (Exception ex) {
            log.error("TagCertificate:Delete - Error during revocation of asset tag certificate.", ex);
            throw new RepositoryDeleteException(ex, locator);
        }                
    }
    
    @Override
    @RequiresPermissions("tag_certificates:delete,search")
    public void delete(TagCertificateFilterCriteria criteria) {
        log.debug("TagCertificate:Delete - Got request to revoke asset tag certificate by search criteria.");        
        TagCertificateCollection objCollection = search(criteria);
        try {
            if (objCollection != null && !objCollection.getTagCertificates().isEmpty()) {
                for (TagCertificate obj : objCollection.getTagCertificates()) {
                    TagCertificateLocator locator = new TagCertificateLocator();
                    locator.id = obj.getId();
                    delete(locator);
                }
            }
        } catch (RepositoryException re) {
            throw re;
        } catch (Exception ex) {
            log.error("TagCertificate:Delete - Error during revocation of asset tag certificate.", ex);
            throw new RepositoryDeleteException(ex);
        }                
    }

    private TagCertificate convert(MwAssetTagCertificate obj) {
        TagCertificate convObj = new TagCertificate();
        convObj.setId(UUID.valueOf(obj.getUuid_hex()));
        convObj.setCertificate(obj.getCertificate());
        return convObj;
    }
    
}
