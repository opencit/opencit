/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.rest.v2.repository;

import com.intel.dcsg.cpg.crypto.file.PasswordEncryptedFile;
import com.intel.dcsg.cpg.crypto.key.password.PasswordProtection;
import com.intel.dcsg.cpg.crypto.key.password.PasswordProtectionBuilder;
import com.intel.dcsg.cpg.io.ByteArrayResource;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import static com.intel.mtwilson.tag.dao.jooq.generated.Tables.MW_TAG_CERTIFICATE_REQUEST;
import com.intel.mtwilson.jaxrs2.server.resource.DocumentRepository;
import com.intel.mtwilson.jooq.util.JooqContainer;
import com.intel.mtwilson.repository.RepositoryCreateException;
import com.intel.mtwilson.repository.RepositoryDeleteException;
import com.intel.mtwilson.repository.RepositoryException;
import com.intel.mtwilson.repository.RepositoryRetrieveException;
import com.intel.mtwilson.repository.RepositorySearchException;
import com.intel.mtwilson.repository.RepositoryStoreConflictException;
import com.intel.mtwilson.repository.RepositoryStoreException;
import com.intel.mtwilson.tag.dao.TagJdbi;
import com.intel.mtwilson.tag.dao.jdbi.CertificateRequestDAO;
import com.intel.mtwilson.tag.model.CertificateRequest;
import com.intel.mtwilson.tag.model.CertificateRequestCollection;
import com.intel.mtwilson.tag.model.CertificateRequestFilterCriteria;
import com.intel.mtwilson.tag.model.CertificateRequestLocator;
import java.io.IOException;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SelectQuery;

/**
 *
 * @author ssbangal
 */
public class CertificateRequestRepository implements DocumentRepository<CertificateRequest, CertificateRequestCollection, CertificateRequestFilterCriteria, CertificateRequestLocator> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CertificateRequestRepository.class);
    

    @Override
    @RequiresPermissions("tag_certificate_requests:search") 
    public CertificateRequestCollection search(CertificateRequestFilterCriteria criteria) {
        log.debug("CertificateRequest:Search - Got request to search for the CertificateRequests.");        
        CertificateRequestCollection objCollection = new CertificateRequestCollection();
        try(JooqContainer jc = TagJdbi.jooq()) {
            DSLContext jooq = jc.getDslContext();
            
            SelectQuery sql = jooq.select().from(MW_TAG_CERTIFICATE_REQUEST).getQuery();
            if (criteria.filter) {
                if( criteria.id != null ) {
                    sql.addConditions(MW_TAG_CERTIFICATE_REQUEST.ID.equalIgnoreCase(criteria.id.toString())); // when uuid is stored in database as the standard UUID string format (36 chars)
                }
                if( criteria.subjectEqualTo != null  && criteria.subjectEqualTo.length() > 0 ) {
                    sql.addConditions(MW_TAG_CERTIFICATE_REQUEST.SUBJECT.equalIgnoreCase(criteria.subjectEqualTo));
                }
                if( criteria.subjectContains != null  && criteria.subjectContains.length() > 0  ) {
                    sql.addConditions(MW_TAG_CERTIFICATE_REQUEST.SUBJECT.lower().contains(criteria.subjectContains.toLowerCase()));
                }
                if( criteria.statusEqualTo != null  && criteria.statusEqualTo.length() > 0 ) {
                    sql.addConditions(MW_TAG_CERTIFICATE_REQUEST.STATUS.equalIgnoreCase(criteria.statusEqualTo));
                }
            }
            sql.addOrderBy(MW_TAG_CERTIFICATE_REQUEST.ID);
            Result<Record> result = sql.fetch();
            
            log.debug("Got {} records", result.size());
            UUID c = new UUID(); // id of the current certificate request object built, used to detect when it's time to build the next one
            for(Record r : result) {
                if( !UUID.valueOf(r.getValue(MW_TAG_CERTIFICATE_REQUEST.ID)).equals(c) ) {
                    c = UUID.valueOf(r.getValue(MW_TAG_CERTIFICATE_REQUEST.ID));
                    CertificateRequest obj = new CertificateRequest();
                    obj.setId(UUID.valueOf(r.getValue(MW_TAG_CERTIFICATE_REQUEST.ID)));
                    obj.setSubject(r.getValue(MW_TAG_CERTIFICATE_REQUEST.SUBJECT));
                    obj.setStatus(r.getValue(MW_TAG_CERTIFICATE_REQUEST.STATUS));
                    obj.setContent(r.getValue(MW_TAG_CERTIFICATE_REQUEST.CONTENT));
                    obj.setContentType(r.getValue(MW_TAG_CERTIFICATE_REQUEST.CONTENTTYPE));
                    objCollection.getCertificateRequests().add(obj);
                }
            }
            sql.close();
        } catch (Exception ex) {
            log.error("CertificateRequest:Search - Error during CertificateRequest search.", ex);
            throw new RepositorySearchException(ex, criteria);
        }        
        log.debug("CertificateRequest:Search - Returning back {} of results.", objCollection.getCertificateRequests().size());                        
        return objCollection;
    }

    @Override
    @RequiresPermissions("tag_certificate_requests:retrieve") 
    public CertificateRequest retrieve(CertificateRequestLocator locator) {
        if (locator == null || locator.id == null) { return null;}
        log.debug("CertificateRequest:Retrieve - Got request to retrieve CertificateRequest with id {}.", locator.id);                
        try (CertificateRequestDAO certRequestDao = TagJdbi.certificateRequestDao()) {            
            CertificateRequest obj = certRequestDao.findById(locator.id);
            if (obj != null) {
                return obj;
            }
        } catch (Exception ex) {
            log.error("CertificateRequest:Retrieve - Error during CertificateRequest deletion.", ex);
            throw new RepositoryRetrieveException(ex, locator);
        }
        return null;
    }

    @Override
    @RequiresPermissions("tag_certificate_requests:store") 
    public void store(CertificateRequest item) {
        log.debug("CertificateRequest:Create - Got request to update CertificateRequest with id {}.", item.getId().toString());        
        CertificateRequestLocator locator = new CertificateRequestLocator();
        locator.id = item.getId();
        try (CertificateRequestDAO certRequestDao = TagJdbi.certificateRequestDao()) {            
            CertificateRequest obj = certRequestDao.findById(item.getId());
            if (obj == null) {
                log.error("CertificateRequest:Store - CertificateRequest will not be updated since it does not exist.");
                throw new RepositoryStoreConflictException(locator);
            }
                        
            if (item.getStatus() != null && !item.getStatus().isEmpty())
                certRequestDao.updateStatus(item.getId(), item.getStatus());
            log.debug("CertificateRequest:Store - Updated the CertificateRequest with id {} successfully.", item.getId().toString());            
         } catch(RepositoryException re) { 
             throw re; 
        } catch (Exception ex) {
            log.error("CertificateRequest:Create - Error during CertificateRequest update.", ex);
            throw new RepositoryStoreException(ex, locator);
        }
    }

    
    // similar to ImportConfig command in mtwilson-configuration
    public PasswordProtection getPasswordProtection() {
            PasswordProtection protection = PasswordProtectionBuilder.factory().aes(256).block().sha256().pbkdf2WithHmacSha1().saltBytes(8).iterations(1000).build();
            if( !protection.isAvailable() ) {
                protection = PasswordProtectionBuilder.factory().aes(128).block().sha256().pbkdf2WithHmacSha1().saltBytes(8).iterations(1000).build();
            }
        return protection;
    }
    
    protected void encrypt(CertificateRequest certificateRequest) throws IOException {
        byte[] plaintext = certificateRequest.getContent();
        
            // NOTE: the base64-encoded value of mtwilson.as.dek is used as the encryption password;  this is different than using the decoded value as the aes-128 key which is currently done for attestation service host connection info
            ByteArrayResource resource = new ByteArrayResource();
            PasswordEncryptedFile passwordEncryptedFile = new PasswordEncryptedFile(resource, My.configuration().getDataEncryptionKeyBase64(), getPasswordProtection());
            passwordEncryptedFile.encrypt(plaintext); // saves it to resource
            
            certificateRequest.setContent(resource.toByteArray()); // encrypted xml file wrapped in rfc822-style message format which indicates the encryption settings
            certificateRequest.setContentType("message/rfc822"); 
        
    }
    
    protected void decrypt(CertificateRequest certificateRequest) throws IOException {
            ByteArrayResource resource = new ByteArrayResource(certificateRequest.getContent());
            PasswordEncryptedFile passwordEncryptedFile = new PasswordEncryptedFile(resource, My.configuration().getDataEncryptionKeyBase64());
            byte[] plaintext = passwordEncryptedFile.decrypt(); 
            
            certificateRequest.setContent(plaintext); 
            certificateRequest.setContentType("application/xml"); 
    }
    
    @Override
    @RequiresPermissions("tag_certificate_requests:create") 
    public void create(CertificateRequest item) {
        log.debug("CertificateRequest:Create - Got request to create a new user {}.", item.getId().toString());        
        CertificateRequestLocator locator = new CertificateRequestLocator(); // will be used if we need to throw an exception
        locator.id = item.getId();
        try (CertificateRequestDAO certRequestDao = TagJdbi.certificateRequestDao()) {
            if( item.getStatus() == null || item.getStatus().isEmpty() ) { 
                item.setStatus("New");
            }
            encrypt(item);
//            certRequestDao.insert(item.getId().toString(), item.getSubject(), selectionObj.getId().toString(), null, null);
            certRequestDao.insert(item);
            log.debug("CertificateRequest:Create - Created the CertificateRequest {} successfully.", item.getId().toString());
            
        } catch (RepositoryException re) {
            throw re;            
        } catch (Exception ex) {
            log.error("CertificateRequest:Create - Error during CertificateRequest creation.", ex);
            throw new RepositoryCreateException(ex, locator);
        }        
    }

    @Override
    @RequiresPermissions("tag_certificate_requests:delete") 
    public void delete(CertificateRequestLocator locator) {
        if (locator == null || locator.id == null) { return;}
        log.debug("CertificateRequest:Delete - Got request to delete CertificateRequest with id {}.", locator.id.toString());                
        try (CertificateRequestDAO certRequestDao = TagJdbi.certificateRequestDao()) {            
            CertificateRequest obj = certRequestDao.findById(locator.id);
            if (obj != null) {
                certRequestDao.deleteById(locator.id);
                log.debug("CertificateRequest:Delete - Deleted the CertificateRequest {} successfully.", locator.id.toString());                                
            }else {
                log.info("CertificateRequest:Delete - CertificateRequest does not exist in the system.");                
            }
        } catch (Exception ex) {
            log.error("CertificateRequest:Delete - Error during CertificateRequest deletion.", ex);
            throw new RepositoryDeleteException(ex, locator);
        }        
    }
    
    @Override
    @RequiresPermissions("tag_certificate_requests:delete,search") 
    public void delete(CertificateRequestFilterCriteria criteria) {
        log.debug("CertificateRequest:Delete - Got request to delete certificate requests by search criteria.");        
        CertificateRequestCollection objCollection = search(criteria);
        try { 
            for (CertificateRequest obj : objCollection.getCertificateRequests()) {
                CertificateRequestLocator locator = new CertificateRequestLocator();
                locator.id = obj.getId();
                delete(locator);
            }
        } catch(RepositoryException re) {
            throw re;
        } catch (Exception ex) {
            log.error("CertificateRequest:Delete - Error during CertificateRequest deletion.", ex);
            throw new RepositoryDeleteException(ex);
        }
    }
        
}
