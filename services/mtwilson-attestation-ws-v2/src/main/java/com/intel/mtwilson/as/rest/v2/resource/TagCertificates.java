/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.resource;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.controller.MwAssetTagCertificateJpaController;
import com.intel.mtwilson.as.data.MwAssetTagCertificate;
import com.intel.mtwilson.as.rest.v2.model.TagCertificate;
import com.intel.mtwilson.as.rest.v2.model.TagCertificateCollection;
import com.intel.mtwilson.as.rest.v2.model.TagCertificateFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.TagCertificateLinks;
import com.intel.mtwilson.datatypes.ErrorCode;
import com.intel.mtwilson.jersey.resource.AbstractResource;
import com.intel.mtwilson.launcher.ws.ext.V2;
import com.intel.mtwilson.as.business.AssetTagCertBO;
import com.intel.mtwilson.datatypes.AssetTagCertCreateRequest;
import com.intel.mtwilson.datatypes.AssetTagCertRevokeRequest;

import java.util.List;
//import javax.ejb.Stateless;
import javax.ws.rs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
@V2
//@Stateless
@Path("/tag-certificates")
public class TagCertificates extends AbstractResource<TagCertificate, TagCertificateCollection, TagCertificateFilterCriteria, TagCertificateLinks> {

    private Logger log = LoggerFactory.getLogger(getClass().getName());
    
    public TagCertificates() {
        super();
    }
    
    @Override
    protected TagCertificateCollection search(TagCertificateFilterCriteria criteria) {
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
    protected TagCertificate retrieve(String id) {
        if( id == null ) { return null; }
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
    protected void store(TagCertificate item) {
        AssetTagCertRevokeRequest obj = new AssetTagCertRevokeRequest();
        try {
            obj.setSha256OfAssetCert(null);
            new AssetTagCertBO().revokeAssetTagCertificate(obj, item.getId().toString());
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during update of asset tag certificate.", ex);
            throw new ASException(ErrorCode.AS_ASSET_TAG_CERT_UPDATE_ERROR, ex.getClass().getSimpleName());
        }        
    }

    @Override
    protected void create(TagCertificate item) {
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
    protected void delete(String id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected TagCertificateCollection createEmptyCollection() {
        return new TagCertificateCollection();
    }
    
    private TagCertificate convert(MwAssetTagCertificate obj) {
        TagCertificate convObj = new TagCertificate();
        convObj.setId(UUID.valueOf(obj.getUuid_hex()));
        convObj.setCertificate(obj.getCertificate());
        return convObj;
    }
    
}
