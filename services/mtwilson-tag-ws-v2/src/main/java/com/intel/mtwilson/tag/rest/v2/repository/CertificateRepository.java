/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.rest.v2.repository;

import com.intel.dcsg.cpg.io.UUID;
import static com.intel.mtwilson.tag.dao.jooq.generated.Tables.MW_TAG_CERTIFICATE;
import com.intel.mtwilson.tag.dao.jdbi.CertificateDAO;
import com.intel.mtwilson.jersey.resource.SimpleRepository;
import com.intel.mtwilson.tag.dao.TagJdbi;
import com.intel.mtwilson.tag.model.Certificate;
import com.intel.mtwilson.tag.model.CertificateCollection;
import com.intel.mtwilson.tag.model.CertificateFilterCriteria;
import com.intel.mtwilson.tag.model.CertificateLocator;
import java.sql.Timestamp;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SelectQuery;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
public class CertificateRepository extends ServerResource implements SimpleRepository<Certificate, CertificateCollection, CertificateFilterCriteria, CertificateLocator> {

    private Logger log = LoggerFactory.getLogger(getClass().getName());

    @Override
    public CertificateCollection search(CertificateFilterCriteria criteria) {
        CertificateCollection objCollection = new CertificateCollection();
        DSLContext jooq = null;
        
        try {
            jooq = TagJdbi.jooq();
            
            SelectQuery sql = jooq.select()
                    .from(MW_TAG_CERTIFICATE) // .join(CERTIFICATE_TAG_VALUE)
                    //.on(CERTIFICATE_TAG_VALUE.CERTIFICATEREQUESTID.equal(CERTIFICATE.ID)))
                    .getQuery();
            if( criteria.id != null ) {
                sql.addConditions(MW_TAG_CERTIFICATE.ID.equalIgnoreCase(criteria.id.toString())); // when uuid is stored in database as the standard UUID string format (36 chars)
            }
            if( criteria.subjectEqualTo != null  && criteria.subjectEqualTo.length() > 0 ) {
                sql.addConditions(MW_TAG_CERTIFICATE.SUBJECT.equal(criteria.subjectEqualTo));
            }
            if( criteria.subjectContains != null  && criteria.subjectContains.length() > 0  ) {
                sql.addConditions(MW_TAG_CERTIFICATE.SUBJECT.equal(criteria.subjectContains));
            }
            if( criteria.issuerEqualTo != null  && criteria.issuerEqualTo.length() > 0 ) {
                sql.addConditions(MW_TAG_CERTIFICATE.ISSUER.equal(criteria.issuerEqualTo));
            }
            if( criteria.issuerContains != null  && criteria.issuerContains.length() > 0  ) {
                sql.addConditions(MW_TAG_CERTIFICATE.ISSUER.equal(criteria.issuerContains));
            }
            if( criteria.sha1 != null  ) {
                sql.addConditions(MW_TAG_CERTIFICATE.SHA1.equal(criteria.sha1.toHexString()));
            }
            if( criteria.sha256 != null  ) {
                sql.addConditions(MW_TAG_CERTIFICATE.SHA256.equal(criteria.sha256.toHexString()));
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
            sql.addOrderBy(MW_TAG_CERTIFICATE.ID);
            Result<Record> result = sql.fetch();
            log.debug("Got {} records", result.size());
            UUID c = new UUID(); // id of the current certificate request object built, used to detect when it's time to build the next one
            for(Record r : result) {
                Certificate certObj = new Certificate();
                if( UUID.valueOf(r.getValue(MW_TAG_CERTIFICATE.ID)) != c ) {
                    c = UUID.valueOf(r.getValue(MW_TAG_CERTIFICATE.ID));
                    try {
                        log.debug("Creating certificate record at c={}", c);
                        certObj.setCertificate((byte[])r.getValue(MW_TAG_CERTIFICATE.CERTIFICATE));  // unlike other table queries, here we can get all the info from the certificate itself... except for the revoked flag
                        certObj.setId(UUID.valueOf(r.getValue(MW_TAG_CERTIFICATE.ID)));
                        if( r.getValue(MW_TAG_CERTIFICATE.REVOKED) != null ) {
                            certObj.setRevoked(r.getValue(MW_TAG_CERTIFICATE.REVOKED));
                        }
                        log.debug("Created certificate record {}", certObj.getId().toString());
                        objCollection.getCertificates().add(certObj);
                    }
                    catch(Exception e) {
                        log.error("Cannot load certificate #{}", r.getValue(MW_TAG_CERTIFICATE.ID), e);
                    }
                }
            }
            log.debug("Closing sql");
            sql.close();
            log.debug("Returning {} certificates", objCollection.getCertificates().size());
            
        } catch (ResourceException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during certificate search.", ex);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Please see the server log for more details.");
        }        
        return objCollection;
    }

    @Override
    public Certificate retrieve(CertificateLocator locator) {
        if (locator == null || locator.id == null) { return null;}
        try (CertificateDAO dao = TagJdbi.certificateDao()) {
        
            Certificate obj = dao.findById(locator.id);
            if (obj != null) 
                return obj;

        } catch (ResourceException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during certificate search.", ex);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Please see the server log for more details.");
        } 
        return null;
    }

    @Override
    public void store(Certificate item) {

        try (CertificateDAO dao = TagJdbi.certificateDao()) {
            
            Certificate obj = dao.findById(item.getId());
            // Allowing the user to only edit the revoked field.
            if (obj != null)
                dao.updateRevoked(item.getId(), item.isRevoked());
            else {
                throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Object not found.");
            }
                                    
        } catch (ResourceException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during attribute update.", ex);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Please see the server log for more details.");
        }        
    }

    @Override
    public void create(Certificate item) {

        try (CertificateDAO dao = TagJdbi.certificateDao()) {
            
            dao.insert(item.getId(), item.getCertificate(), item.getSha1().toHexString(), 
                    item.getSha256().toHexString(), item.getSubject(), item.getIssuer(), item.getNotBefore(), item.getNotAfter());

        } catch (ResourceException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during attribute creation.", ex);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Please see the server log for more details.");
        }        
    }

    @Override
    public void delete(CertificateLocator locator) {
        if (locator == null || locator.id == null) { return;}
        CertificateDAO dao = null;
        try {            
            dao = TagJdbi.certificateDao();
            Certificate obj = dao.findById(locator.id);
            if (obj != null) {
                dao.delete(locator.id);
            }else {
                throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Certificate not found.");
            }
        } catch (ResourceException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during certificate deletion.", ex);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Please see the server log for more details.");
        } finally {
            if (dao != null)
                dao.close();
        }        
    }
    
    @Override
    public void delete(CertificateFilterCriteria criteria) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
        
}
