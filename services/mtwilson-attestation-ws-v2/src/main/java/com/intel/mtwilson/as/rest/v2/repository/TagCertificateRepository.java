/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.repository;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.controller.MwAssetTagCertificateJpaController;
import com.intel.mtwilson.as.data.MwAssetTagCertificate;
import com.intel.mtwilson.as.rest.v2.model.TagCertificate;
import com.intel.mtwilson.as.rest.v2.model.TagCertificateCollection;
import com.intel.mtwilson.as.rest.v2.model.TagCertificateFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.TagCertificateLocator;
import com.intel.mtwilson.datatypes.ErrorCode;
import com.intel.mtwilson.as.business.AssetTagCertBO;
import com.intel.mtwilson.datatypes.AssetTagCertCreateRequest;
import com.intel.mtwilson.datatypes.AssetTagCertRevokeRequest;
import com.intel.mtwilson.jersey.resource.SimpleRepository;
import java.util.List;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
public class TagCertificateRepository implements SimpleRepository<TagCertificate, TagCertificateCollection, TagCertificateFilterCriteria, TagCertificateLocator> {

    private Logger log = LoggerFactory.getLogger(getClass().getName());
        
    @Override
    @RequiresPermissions("tag_certificates:search")
    public TagCertificateCollection search(TagCertificateFilterCriteria criteria) {
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
                List<MwAssetTagCertificate> objList = jpaController.findAssetTagCertificatesByHostUUID(criteria.id.toString());
                if (objList != null && !objList.isEmpty()) {
                    for(MwAssetTagCertificate obj : objList) {
                        objCollection.getTagCertificates().add(convert(obj));
                    }
                }                
            }

        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during retrieval of asset tag certificate.", ex);
            throw new ASException(ErrorCode.AS_ASSET_TAG_CERT_RETRIEVE_ERROR, ex.getClass().getSimpleName());
        }
        return objCollection;
    }

    @Override
    @RequiresPermissions("tag_certificates:retrieve")
    public TagCertificate retrieve(TagCertificateLocator locator) {
        if( locator == null || locator.id == null ) { return null; }
        String id = locator.id.toString();
        try {
            MwAssetTagCertificateJpaController jpaController = My.jpa().mwAssetTagCertificate();
            List<MwAssetTagCertificate> objList = jpaController.findAssetTagCertificatesByUuid(id);
            if (objList != null & !objList.isEmpty()) {
                TagCertificate convObj = convert(objList.get(0)); // there should always be only one row matching.
                return convObj;
            }
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during retrieval of asset tag certificate.", ex);
            throw new ASException(ErrorCode.AS_ASSET_TAG_CERT_RETRIEVE_ERROR, ex.getClass().getSimpleName());
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
        AssetTagCertCreateRequest obj = new AssetTagCertCreateRequest();
        try {
            obj.setCertificate(item.getCertificate());
            new AssetTagCertBO().importAssetTagCertificate(obj, item.getId().toString());
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during creation of asset tag certificate.", ex);
            throw new ASException(ErrorCode.AS_ASSET_TAG_CERT_CREATE_ERROR, ex.getClass().getSimpleName());
        }        
    }

    @Override
    @RequiresPermissions("tag_certificates:revoke")
    public void delete(TagCertificateLocator locator) {
        if (locator == null || locator.id == null) { return; }
        AssetTagCertRevokeRequest obj = new AssetTagCertRevokeRequest();
        try {
            obj.setSha1OfAssetCert(null);
            new AssetTagCertBO().revokeAssetTagCertificate(obj, locator.id.toString());
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during update of asset tag certificate.", ex);
            throw new ASException(ErrorCode.AS_ASSET_TAG_CERT_UPDATE_ERROR, ex.getClass().getSimpleName());
        }                
    }
    
    @Override
    @RequiresPermissions("tag_certificates:delete,search")
    public void delete(TagCertificateFilterCriteria criteria) {
        TagCertificateCollection objCollection = search(criteria);
        if (objCollection != null && !objCollection.getTagCertificates().isEmpty()) {
            for (TagCertificate obj : objCollection.getTagCertificates()) {
                TagCertificateLocator locator = new TagCertificateLocator();
                locator.id = obj.getId();
                delete(locator);
            }
        }
    }

    private TagCertificate convert(MwAssetTagCertificate obj) {
        TagCertificate convObj = new TagCertificate();
        convObj.setId(UUID.valueOf(obj.getUuid_hex()));
        convObj.setCertificate(obj.getCertificate());
        return convObj;
    }
    
}
