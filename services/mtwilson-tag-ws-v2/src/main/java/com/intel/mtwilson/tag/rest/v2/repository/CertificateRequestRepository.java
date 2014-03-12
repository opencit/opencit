/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.rest.v2.repository;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.validation.Fault;
import com.intel.mtwilson.My;
import static com.intel.mtwilson.atag.dao.jooq.generated.Tables.CERTIFICATE_REQUEST;
import com.intel.mtwilson.datatypes.AssetTagCertCreateRequest;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import com.intel.mtwilson.jersey.resource.SimpleRepository;
import com.intel.mtwilson.tag.common.Global;
import com.intel.mtwilson.tag.common.X509AttrBuilder;
import com.intel.mtwilson.tag.dao.TagJdbi;
import com.intel.mtwilson.tag.dao.jdbi.CertificateRequestDAO;
import com.intel.mtwilson.tag.dao.jdbi.SelectionDAO;
import com.intel.mtwilson.tag.dao.jdbi.SelectionKvAttributeDAO;
import com.intel.mtwilson.tag.rest.v2.model.Certificate;
import com.intel.mtwilson.tag.rest.v2.model.CertificateRequest;
import com.intel.mtwilson.tag.rest.v2.model.CertificateRequestCollection;
import com.intel.mtwilson.tag.rest.v2.model.CertificateRequestFilterCriteria;
import com.intel.mtwilson.tag.rest.v2.model.CertificateRequestLocator;
import com.intel.mtwilson.tag.rest.v2.model.Selection;
import com.intel.mtwilson.tag.rest.v2.model.SelectionKvAttribute;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.bouncycastle.cert.X509AttributeCertificateHolder;
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
public class CertificateRequestRepository extends ServerResource implements SimpleRepository<CertificateRequest, CertificateRequestCollection, CertificateRequestFilterCriteria, CertificateRequestLocator> {

    private Logger log = LoggerFactory.getLogger(getClass().getName());

    @Override
    public CertificateRequestCollection search(CertificateRequestFilterCriteria criteria) {
       /* CertificateRequestCollection objCollection = new CertificateRequestCollection();
        DSLContext jooq = null;
        SelectionDAO selectionDao = null;
        
        try {
            jooq = TagJdbi.jooq();
            selectionDao = TagJdbi.selectionDao();
            
            SelectQuery sql = jooq.select()
                    .from(CERTIFICATE_REQUEST) // .join(CERTIFICATE_REQUEST_TAG_VALUE)
                    //.on(CERTIFICATE_REQUEST_TAG_VALUE.CERTIFICATEREQUESTID.equal(CERTIFICATE_REQUEST.ID)))
                    .getQuery();
            if( criteria.id != null ) {
    //            sql.addConditions(TAG.UUID.equal(query.id.toByteArray().getBytes())); // when uuid is stored in database as binary
                sql.addConditions(CERTIFICATE_REQUEST.UUID.equal(criteria.id.toString())); // when uuid is stored in database as the standard UUID string format (36 chars)
            }
            if( criteria.subjectEqualTo != null  && criteria.subjectEqualTo.length() > 0 ) {
                sql.addConditions(CERTIFICATE_REQUEST.SUBJECT.equal(criteria.subjectEqualTo));
            }
            if( criteria.subjectContains != null  && criteria.subjectContains.length() > 0  ) {
                sql.addConditions(CERTIFICATE_REQUEST.SUBJECT.equal(criteria.subjectContains));
            }
            if( criteria.selectionEqualTo != null  && criteria.selectionEqualTo.length() > 0 ) {
                sql.addConditions(CERTIFICATE_REQUEST.SUBJECT.equal(criteria.selectionEqualTo));
            }
            if( criteria.selectionContains != null  && criteria.selectionContains.length() > 0  ) {
                sql.addConditions(CERTIFICATE_REQUEST.SUBJECT.equal(criteria.selectionContains));
            }
            if( criteria.statusEqualTo != null  && criteria.statusEqualTo.length() > 0 ) {
                sql.addConditions(CERTIFICATE_REQUEST.STATUS.equal(criteria.statusEqualTo));
            }
            sql.addOrderBy(CERTIFICATE_REQUEST.ID);
            Result<Record> result = sql.fetch();
            
            log.debug("Got {} records", result.size());
            long c = -1; // id of the current certificate request object built, used to detect when it's time to build the next one
            for(Record r : result) {
                if( r.getValue(CERTIFICATE_REQUEST.ID) != c ) {
                    c = r.getValue(CERTIFICATE_REQUEST.ID);
                    CertificateRequest obj = new CertificateRequest();
                    obj.setId(r.getValue(CERTIFICATE_REQUEST.ID));
                    obj.setSubject(r.getValue(CERTIFICATE_REQUEST.SUBJECT));
                    obj.setSelectionId(r.getValue(CERTIFICATE_REQUEST.SELECTIONID));
                    obj.setStatus(r.getValue(CERTIFICATE_REQUEST.STATUS));
                    if( r.getValue(CERTIFICATE_REQUEST.CERTIFICATEID) != null ) { // a Long object, can be null
                        obj.setCertificateId(r.getValue(CERTIFICATE_REQUEST.CERTIFICATEID)); // a long primitive, cannot set to null
                    }
                    objCollection.getCertificates().add(obj);
                }
            }
            sql.close();
        } catch (ResourceException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during certificate search.", ex);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Please see the server log for more details.");
        }        
        return objCollection;*/
        return null;
    }

    @Override
    public CertificateRequest retrieve(CertificateRequestLocator locator) {
        if (locator == null || locator.id == null) { return null;}
        try (CertificateRequestDAO certRequestDao = TagJdbi.certificateRequestDao()) {            
            CertificateRequest obj = certRequestDao.findById(locator.id);
            if (obj != null) {
                return obj;
            }
        } catch (ResourceException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during certificate deletion.", ex);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Please see the server log for more details.");
        }
        return null;
    }

    @Override
    public void store(CertificateRequest item) {
        try (CertificateRequestDAO certRequestDao = TagJdbi.certificateRequestDao()) {            
            CertificateRequest obj = certRequestDao.findById(item.getId());
            if (obj == null) {
                log.error("Object with specified id does not exist in the system.");
                throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "No matching certificate request found in the system.");
            }
            
            // Let us check what parameter the user wants to update
            if (item.getAuthorityName() != null && !item.getAuthorityName().isEmpty())
                certRequestDao.updateAuthority(item.getId(), item.getAuthorityName());
            
            if (item.getCertificateId() != null)
                certRequestDao.updateApproved(item.getId(), item.getCertificateId());
            
            if (item.getStatus() != null && !item.getStatus().isEmpty())
                certRequestDao.updateStatus(item.getId(), item.getStatus());
            
        } catch (ResourceException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during certificate deletion.", ex);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Please see the server log for more details.");
        }
    }

    @Override
    public void create(CertificateRequest item) {
        
        try (CertificateRequestDAO certRequestDao = TagJdbi.certificateRequestDao(); 
                SelectionDAO selectionDao = TagJdbi.selectionDao()) {
            
            // Since the user would have specified the selectin name, we need to get the selection id first
            Selection selectionObj = selectionDao.findByName(item.getSelectionName());
            if( selectionObj == null) {
                log.error("Selection {} is not available.", item.getSelectionName());
                setStatus(Status.SERVER_ERROR_INTERNAL);  // cannot make a certificate request without a valid selection
                throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Specified selection does not exist in the system.");
            }
            
            // Since this is the new certificate request, the certificate id would be null.
            certRequestDao.insert(item.getId(), item.getSubject(), selectionObj.getId(), null, null);
            
        } catch (ResourceException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during certificate request creation.", ex);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Please see the server log for more details.");
        }       
    }

    @Override
    public void delete(CertificateRequestLocator locator) {
        if (locator == null || locator.id == null) { return;}
        try (CertificateRequestDAO certRequestDao = TagJdbi.certificateRequestDao()) {            
            CertificateRequest obj = certRequestDao.findById(locator.id);
            if (obj != null) {
                certRequestDao.delete(locator.id);
            }
        } catch (ResourceException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during certificate deletion.", ex);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Please see the server log for more details.");
        }       
    }
    
    @Override
    public void delete(CertificateRequestFilterCriteria criteria) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
        
}
