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
import com.intel.mtwilson.jaxrs2.server.resource.SimpleRepository;
import com.intel.mtwilson.jooq.util.JooqContainer;
import com.intel.mtwilson.tag.dao.TagJdbi;
import com.intel.mtwilson.tag.model.Certificate;
import com.intel.mtwilson.tag.model.CertificateCollection;
import com.intel.mtwilson.tag.model.CertificateFilterCriteria;
import com.intel.mtwilson.tag.model.CertificateLocator;
import java.sql.Timestamp;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SelectQuery;
//import org.restlet.data.Status;
//import org.restlet.resource.ResourceException;
//import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
public class CertificateRepository implements SimpleRepository<Certificate, CertificateCollection, CertificateFilterCriteria, CertificateLocator> {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CertificateRepository.class);
    
    @Override
    @RequiresPermissions("tag_certificates:search") 
    public CertificateCollection search(CertificateFilterCriteria criteria) {
        CertificateCollection objCollection = new CertificateCollection();
        // TODO: Evaluate the use of byte search in MySQL and PostgreSQL against using this option.
        
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
                    log.debug("Created certificate record in search result {}", certObj.getId().toString());
                    objCollection.getCertificates().add(certObj);
                }
                catch(Exception e) {
                    log.error("Cannot load certificate #{}", r.getValue(MW_TAG_CERTIFICATE.ID), e);
                }
            }
            log.debug("Closing sql");
            sql.close();
            log.debug("Returning {} certificates", objCollection.getCertificates().size());
            
        } catch (WebApplicationException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during certificate search.", ex);
            throw new WebApplicationException("Please see the server log for more details.", Response.Status.INTERNAL_SERVER_ERROR);
        } 
        return objCollection;
    }

    @Override
    @RequiresPermissions("tag_certificates:retrieve") 
    public Certificate retrieve(CertificateLocator locator) {
        if (locator == null || locator.id == null) { return null;}
        try (CertificateDAO dao = TagJdbi.certificateDao()) {
        
            Certificate obj = dao.findById(locator.id);
            if (obj != null) 
                return obj;

        } catch (WebApplicationException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during certificate search.", ex);
            throw new WebApplicationException("Please see the server log for more details.", Response.Status.INTERNAL_SERVER_ERROR);
        } 
        return null;
    }

    @Override
    @RequiresPermissions("tag_certificates:store") 
    public void store(Certificate item) {

        try (CertificateDAO dao = TagJdbi.certificateDao()) {
            
            Certificate obj = dao.findById(item.getId());
            // Allowing the user to only edit the revoked field.
            if (obj != null)
                dao.updateRevoked(item.getId(), item.isRevoked());
            else {
                throw new WebApplicationException("Object not found.", Response.Status.NOT_FOUND);
            }
                                    
        } catch (WebApplicationException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during attribute update.", ex);
            throw new WebApplicationException("Please see the server log for more details.", Response.Status.INTERNAL_SERVER_ERROR);
        }        
    }

    @Override
    @RequiresPermissions("tag_certificates:create") 
    public void create(Certificate item) {

        try (CertificateDAO dao = TagJdbi.certificateDao()) {
            Certificate newCert = dao.findById(item.getId());
            if (newCert == null) {
                newCert = Certificate.valueOf(item.getCertificate());
                dao.insert(item.getId(), newCert.getCertificate(), newCert.getSha1().toHexString(), 
                        newCert.getSha256().toHexString(), newCert.getSubject(), newCert.getIssuer(), newCert.getNotBefore(), newCert.getNotAfter());                
            } else {
                throw new WebApplicationException(Response.Status.CONFLICT);
            }
        } catch (WebApplicationException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during certificate creation.", ex);
            throw new WebApplicationException("Please see the server log for more details.", Response.Status.INTERNAL_SERVER_ERROR);
        }        
    }

    @Override
    @RequiresPermissions("tag_certificates:delete") 
    public void delete(CertificateLocator locator) {
        if (locator == null || locator.id == null) { return;}
        try (CertificateDAO dao = TagJdbi.certificateDao()) {
            Certificate obj = dao.findById(locator.id);
            if (obj != null) {
                dao.delete(locator.id);
            }else {
                throw new WebApplicationException("Certificate not found.", Response.Status.NOT_FOUND);
            }
        } catch (WebApplicationException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during certificate deletion.", ex);
            throw new WebApplicationException("Please see the server log for more details.", Response.Status.INTERNAL_SERVER_ERROR);
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
        } catch (Exception ex) {
            log.error("Error during Certificate deletion.", ex);
            throw new WebApplicationException("Please see the server log for more details.", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
        
}
