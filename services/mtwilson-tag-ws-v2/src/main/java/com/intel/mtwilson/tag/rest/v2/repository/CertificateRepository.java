/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.rest.v2.repository;

import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.crypto.Sha256Digest;
import com.intel.dcsg.cpg.io.UUID;
import static com.intel.mtwilson.tag.dao.jooq.generated.Tables.MW_TAG_CERTIFICATE;
import com.intel.mtwilson.tag.dao.jdbi.CertificateDAO;
import com.intel.mtwilson.jaxrs2.server.resource.DocumentRepository;
import com.intel.mtwilson.jooq.util.JooqContainer;
import com.intel.mtwilson.repository.RepositoryCreateConflictException;
import com.intel.mtwilson.repository.RepositoryCreateException;
import com.intel.mtwilson.repository.RepositoryDeleteException;
import com.intel.mtwilson.repository.RepositoryException;
import com.intel.mtwilson.repository.RepositoryRetrieveException;
import com.intel.mtwilson.repository.RepositorySearchException;
import com.intel.mtwilson.repository.RepositoryStoreConflictException;
import com.intel.mtwilson.repository.RepositoryStoreException;
import com.intel.mtwilson.tag.dao.TagJdbi;
import com.intel.mtwilson.tag.model.Certificate;
import com.intel.mtwilson.tag.model.CertificateCollection;
import com.intel.mtwilson.tag.model.CertificateFilterCriteria;
import com.intel.mtwilson.tag.model.CertificateLocator;
import java.sql.Timestamp;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SelectQuery;

/**
 *
 * @author ssbangal
 */
public class CertificateRepository implements DocumentRepository<Certificate, CertificateCollection, CertificateFilterCriteria, CertificateLocator> {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CertificateRepository.class);
    
    @Override
    @RequiresPermissions("tag_certificates:search") 
    public CertificateCollection search(CertificateFilterCriteria criteria) {
        log.debug("Certificate:Search - Got request to search for the Certificates.");        
        CertificateCollection objCollection = new CertificateCollection();
        
        try(JooqContainer jc = TagJdbi.jooq()) {
            DSLContext jooq = jc.getDslContext();
            
            SelectQuery sql = jooq.select().from(MW_TAG_CERTIFICATE).getQuery();
            if (criteria.filter) {
                if( criteria.id != null ) {
                    sql.addConditions(MW_TAG_CERTIFICATE.ID.equalIgnoreCase(criteria.id.toString())); // when uuid is stored in database as the standard UUID string format (36 chars)
                }
                if( criteria.subjectEqualTo != null  && criteria.subjectEqualTo.length() > 0 ) {
                    sql.addConditions(MW_TAG_CERTIFICATE.SUBJECT.equalIgnoreCase(criteria.subjectEqualTo));
                }
                if( criteria.subjectContains != null  && criteria.subjectContains.length() > 0  ) {
                    sql.addConditions(MW_TAG_CERTIFICATE.SUBJECT.lower().contains(criteria.subjectContains.toLowerCase()));
                }
                if( criteria.issuerEqualTo != null  && criteria.issuerEqualTo.length() > 0 ) {
                    sql.addConditions(MW_TAG_CERTIFICATE.ISSUER.equalIgnoreCase(criteria.issuerEqualTo));
                }
                if( criteria.issuerContains != null  && criteria.issuerContains.length() > 0  ) {
                    sql.addConditions(MW_TAG_CERTIFICATE.ISSUER.lower().contains(criteria.issuerContains.toLowerCase()));
                }
                if( criteria.sha1 != null  ) {
                    sql.addConditions(MW_TAG_CERTIFICATE.SHA1.equalIgnoreCase(criteria.sha1.toHexString()));
                }
                if( criteria.sha256 != null  ) {
                    sql.addConditions(MW_TAG_CERTIFICATE.SHA256.equalIgnoreCase(criteria.sha256.toHexString()));
                }
                if( criteria.validOn != null ) {
                    sql.addConditions(MW_TAG_CERTIFICATE.NOTBEFORE.lessOrEqual(new Timestamp(criteria.validOn.getTime())));
                    sql.addConditions(MW_TAG_CERTIFICATE.NOTAFTER.greaterOrEqual(new Timestamp(criteria.validOn.getTime())));
                }
                if( criteria.validBefore != null ) {
                    sql.addConditions(MW_TAG_CERTIFICATE.NOTAFTER.greaterOrEqual(new Timestamp(criteria.validBefore.getTime())));
                }
                if( criteria.validAfter != null ) {
                    sql.addConditions(MW_TAG_CERTIFICATE.NOTBEFORE.lessOrEqual(new Timestamp(criteria.validAfter.getTime())));
                }
                if( criteria.revoked != null   ) {
                    sql.addConditions(MW_TAG_CERTIFICATE.REVOKED.equal(criteria.revoked));
                }
            }
            sql.addOrderBy(MW_TAG_CERTIFICATE.SUBJECT);
            Result<Record> result = sql.fetch();
            log.debug("Got {} records", result.size());
            for(Record r : result) {
                Certificate certObj = new Certificate();
                try {
                    certObj.setId(UUID.valueOf(r.getValue(MW_TAG_CERTIFICATE.ID)));
                    certObj.setCertificate((byte[])r.getValue(MW_TAG_CERTIFICATE.CERTIFICATE));  // unlike other table queries, here we can get all the info from the certificate itself... except for the revoked flag
                    certObj.setIssuer(r.getValue(MW_TAG_CERTIFICATE.ISSUER));
                    certObj.setSubject(r.getValue(MW_TAG_CERTIFICATE.SUBJECT));
                    certObj.setNotBefore(r.getValue(MW_TAG_CERTIFICATE.NOTBEFORE));
                    certObj.setNotAfter(r.getValue(MW_TAG_CERTIFICATE.NOTAFTER));
                    certObj.setSha1(Sha1Digest.valueOf(r.getValue(MW_TAG_CERTIFICATE.SHA1)));
                    certObj.setSha256(Sha256Digest.valueOf(r.getValue(MW_TAG_CERTIFICATE.SHA256)));
                    certObj.setRevoked(r.getValue(MW_TAG_CERTIFICATE.REVOKED));
                    log.debug("Certificate:Search - Created certificate record in search result {}", certObj.getId().toString());
                    objCollection.getCertificates().add(certObj);
                }
                catch(Exception e) {
                    log.error("Certificate:Search - Cannot load certificate #{}", r.getValue(MW_TAG_CERTIFICATE.ID), e);
                }
            }
            sql.close();            
        } catch (Exception ex) {
            log.error("Certificate:Search - Error during certificate search.", ex);
            throw new RepositorySearchException(ex, criteria);
        } 
        log.debug("Certificate:Search - Returning back {} of results.", objCollection.getCertificates().size());                                
        return objCollection;
    }

    @Override
    @RequiresPermissions("tag_certificates:retrieve") 
    public Certificate retrieve(CertificateLocator locator) {
        if (locator == null || locator.id == null) { return null;}
        log.debug("Certificate:Retrieve - Got request to retrieve user with id {}.", locator.id);                
        try (CertificateDAO dao = TagJdbi.certificateDao()) {
            Certificate obj = dao.findById(locator.id);
            if (obj != null) 
                return obj;
        } catch (Exception ex) {
            log.error("Certificate:Retrieve - Error during certificate retrieval.", ex);
            throw new RepositoryRetrieveException(ex, locator);
        } 
        return null;
    }

    @Override
    @RequiresPermissions("tag_certificates:store") 
    public void store(Certificate item) {
        log.debug("Certificate:Store - Got request to update Certificate with id {}.", item.getId().toString());        
        CertificateLocator locator = new CertificateLocator(); // will be used if we need to throw an exception
        locator.id = item.getId();
        try (CertificateDAO dao = TagJdbi.certificateDao()) {
            
            Certificate obj = dao.findById(item.getId());
            // Allowing the user to only edit the revoked field.
            if (obj != null) {
                dao.updateRevoked(item.getId(), item.isRevoked());
                log.debug("Certificate:Store - Updated the Certificate {} successfully.", item.getId().toString());                
            } else {
                log.error("Certificate:Store - Certificate will not be updated since it does not exist.");
                throw new RepositoryStoreConflictException(locator);
            }                                    
        } catch (RepositoryException re) {
            throw re;            
        } catch (Exception ex) {
            log.error("Certificate:Store - Error during Certificate update.", ex);
            throw new RepositoryStoreException(ex, locator);
        }        
    }

    @Override
    @RequiresPermissions("tag_certificates:create") 
    public void create(Certificate item) {
        log.debug("Certificate:Create - Got request to create a new Certificate {}.", item.getId().toString());
        CertificateLocator locator = new CertificateLocator();
        locator.id = item.getId();
        try (CertificateDAO dao = TagJdbi.certificateDao()) {
            Certificate newCert = dao.findById(item.getId());
            if (newCert == null) {
                newCert = Certificate.valueOf(item.getCertificate());
                dao.insert(item.getId(), newCert.getCertificate(), newCert.getSha1().toHexString(), 
                        newCert.getSha256().toHexString(), newCert.getSubject(), newCert.getIssuer(), newCert.getNotBefore(), newCert.getNotAfter());                
                log.debug("Certificate:Create - Created the Certificate {} successfully.", item.getId().toString());
            } else {
                log.error("Certificate:Create - Certificate {} will not be created since a duplicate Certificate already exists.", item.getId().toString());                
                throw new RepositoryCreateConflictException(locator);
            }
        } catch (RepositoryException re) {
            throw re;            
        } catch (Exception ex) {
            log.error("Certificate:Create - Error during certificate creation.", ex);
            throw new RepositoryCreateException(ex, locator);
        }        
    }

    @Override
    @RequiresPermissions("tag_certificates:delete") 
    public void delete(CertificateLocator locator) {
        if (locator == null || locator.id == null) { return;}
        log.debug("Certificate:Delete - Got request to delete Certificate with id {}.", locator.id.toString());                
        try (CertificateDAO dao = TagJdbi.certificateDao()) {
            Certificate obj = dao.findById(locator.id);
            if (obj != null) {
                dao.delete(locator.id);
                log.debug("Certificate:Delete - Deleted the Certificate {} successfully.", locator.id.toString());                
            }else {
                log.info("Certificate:Delete - Certificate does not exist in the system.");                
            }
        } catch (Exception ex) {
            log.error("Certificate:Delete - Error during certificate deletion.", ex);
            throw new RepositoryDeleteException(ex, locator);
        }        
    }
    
    @Override
    @RequiresPermissions("tag_certificates:delete,search") 
    public void delete(CertificateFilterCriteria criteria) {
        log.debug("Certificate:Delete - Got request to delete certificate by search criteria.");        
        CertificateCollection objCollection = search(criteria);
        try { 
            for (Certificate obj : objCollection.getCertificates()) {
                CertificateLocator locator = new CertificateLocator();
                locator.id = obj.getId();
                delete(locator);
            }
        } catch(RepositoryException re) {
            throw re;
        } catch (Exception ex) {
            log.error("Certificate:Delete - Error during Certificate deletion.", ex);
            throw new RepositoryDeleteException(ex);
        }
    }
        
}
