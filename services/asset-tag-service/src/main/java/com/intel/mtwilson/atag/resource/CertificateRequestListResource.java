/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag.resource;

import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.crypto.Sha256Digest;
import com.intel.mtwilson.atag.model.CertificateRequestSearchCriteria;
import com.intel.mtwilson.atag.model.CertificateRequest;
import com.intel.mtwilson.atag.dao.jdbi.*;
import com.intel.mtwilson.atag.dao.Derby;
import static com.intel.mtwilson.atag.dao.jooq.generated.Tables.*;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.validation.Fault;
import com.intel.mtwilson.atag.Global;
import com.intel.mtwilson.atag.X509AttrBuilder;
import com.intel.mtwilson.atag.dao.jdbi.*;
import com.intel.mtwilson.atag.model.Certificate;
import com.intel.mtwilson.atag.model.Selection;
import com.intel.mtwilson.atag.model.SelectionTagValue;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.bouncycastle.cert.X509AttributeCertificateHolder;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SelectQuery;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * References: http://restlet.org/learn/guide/2.2/core/resource/
 *
 * @author jbuhacoff
 */
public class CertificateRequestListResource extends ServerResource {

    private Logger log = LoggerFactory.getLogger(getClass());
    private CertificateRequestDAO certificateRequestDao = null;
//    private CertificateRequestTagValueDAO certificateRequestTagValueDao = null;
//    private TagDAO tagDao = null;
//    private TagValueDAO tagValueDao = null;
    private SelectionDAO selectionDao = null;
    private SelectionTagValueDAO selectionTagValueDao = null;
    private CertificateDAO certificateDao = null;
    private DSLContext jooq = null;
    
    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        try {
            certificateRequestDao = Derby.certificateRequestDao();
//            certificateRequestTagValueDao = Derby.certificateRequestTagValueDao();
//            tagDao = Derby.tagDao();
//            tagValueDao = Derby.tagValueDao();
            selectionDao = Derby.selectionDao();
            selectionTagValueDao = Derby.selectionTagValueDao();
            certificateDao = Derby.certificateDao();
            jooq = Derby.jooq();
        } catch (SQLException e) {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Cannot open database", e);
        }
    }

    @Override
    protected void doRelease() throws ResourceException {
        if (certificateRequestDao != null) {
            certificateRequestDao.close();
        }
        if( selectionDao != null ) {
            selectionDao.close();
        }
        if( selectionTagValueDao != null ) {
            selectionTagValueDao.close();
        }
        if( certificateDao != null ) {
            certificateDao.close();
        }
//        if( certificateRequestTagValueDao != null ) { certificateRequestTagValueDao.close(); }
        super.doRelease();
    }

    /**
     * Input certificate requests provide a certificate subject and one or more tags where each tag
     * is a pair of (name,value) or (oid,value).
     * We look up the tagId and tagValueId for each tag and populate them in the CertificateRequestTagValue
     * object. Then we can insert it to the database.
     */
//    @Post("json:json")
    public CertificateRequest insertCertificateRequest(CertificateRequest certificateRequest) throws SQLException, IOException {
        log.debug("insertCertificateRequest for subject: {}", certificateRequest.getSubject());
        certificateRequest.setUuid(new UUID());
        // IMPORTANT: provisioning policy choices:
        // Automatic Server-Based: always use the same pre-configured selection; find it in static config, ignore the requestor's selection
        // Manual and Automatic Host-Based: allow the requestor to specify a selection and look it up
        Selection selection = null;
        if( Global.configuration().isAllowTagsInCertificateRequests() && certificateRequest.getSelection() != null && !certificateRequest.getSelection().isEmpty() ) {
            if( UUID.isValid(certificateRequest.getSelection() )) {
                selection = selectionDao.findByUuid(UUID.valueOf(certificateRequest.getSelection()));
                if( selection == null ) {
                    setStatus(Status.CLIENT_ERROR_BAD_REQUEST);  // cannot make a certificate request without a valid selection
                    return null;
                }
            }
            else {
                if( UUID.isValid(certificateRequest.getSelection()) ) {
                    selection = selectionDao.findByUuid(UUID.valueOf(certificateRequest.getSelection()));
                }
                else {
                    List<Selection> selections = selectionDao.findByName(certificateRequest.getSelection());
                    if( selections.isEmpty() ) {
                        setStatus(Status.CLIENT_ERROR_BAD_REQUEST);  // cannot make a certificate request without a valid selection
                        return null;
                    }
                    if( selections.size() > 1 ) {
        //                setStatus(Status.REDIRECTION_MULTIPLE_CHOICES); // we could do a multiple choices redirection if we generate the urls ... but it would have to be url to the selection search (with nameEqualTo the provided name) since we can't provide urls' to various POST /certificate-requests... the otherw ay would be to do URLs to filled-out forms...  (put the parameters in the URL and have the javascript populate it) but we don't have forms resource collection yet and anyway that would require support on the client to be useful (today only a browser would MAYBE handle that well)
                       setStatus(Status.CLIENT_ERROR_BAD_REQUEST);  // cannot make a certificate request without a valid selection;  we can't pick one automatically unless the administrator has configured a default selection and in that case we wouldn't even be searching here.
                       return null;
                     }
                    selection = selections.get(0); // size is exactly one, we get the first and only selection
                }
            }
        }
        if( selection == null && Global.configuration().isAllowAutomaticTagSelection() ) {
            String selectionName = Global.configuration().getAutomaticTagSelectionName() ;
            log.debug("Automatic tag selection: {}", selectionName);
            if( selectionName == null || selectionName.isEmpty()  ) {
                log.error("Server configured for automatic tag selection but no selection has been specified");
                setStatus(Status.SERVER_ERROR_INTERNAL); 
                return null;
            }
            if( UUID.isValid(selectionName ) ) {
                selection = selectionDao.findByUuid(UUID.valueOf(selectionName));
            }
            else {
                List<Selection> selections = selectionDao.findByName(selectionName);
                if( selections.isEmpty() ) {
                    log.error("Server configured for automatic tag selection but '{}' is not available", selectionName);
                    setStatus(Status.SERVER_ERROR_INTERNAL);  // cannot make a certificate request without a valid selection
                    return null;
                }
                if( selections.size() > 1 ) {
                    log.error("Server configured for automatic tag selection but multiple '{}' are available", selectionName);
    //                setStatus(Status.REDIRECTION_MULTIPLE_CHOICES); // we could do a multiple choices redirection if we generate the urls ... but it would have to be url to the selection search (with nameEqualTo the provided name) since we can't provide urls' to various POST /certificate-requests... the otherw ay would be to do URLs to filled-out forms...  (put the parameters in the URL and have the javascript populate it) but we don't have forms resource collection yet and anyway that would require support on the client to be useful (today only a browser would MAYBE handle that well)
                   setStatus(Status.SERVER_ERROR_INTERNAL);  // cannot make a certificate request without a valid selection;  we can't pick one automatically unless the administrator has configured a default selection and in that case we wouldn't even be searching here.
                   return null;
                 }
                selection = selections.get(0); // size is exactly one, we get the first and only selection
            }
        }
        if( selection == null ) {
            log.error("Bad tag selection configuration");
               setStatus(Status.SERVER_ERROR_INTERNAL);  // cannot make a certificate request without a valid selection;  we can't pick one automatically unless the administrator has configured a default selection and in that case we wouldn't even be searching here.
               return null;
        }
        // we have a selection object but we need to look up the associated tags before we continue
        certificateRequest.setSelectionId(selection.getId()); // XXX of no use to the client, maybe remove this
        certificateRequest.setSelection(selection.getUuid().toString());
        List<SelectionTagValue> selectionTagValues = selectionTagValueDao.findBySelectionIdWithValues(selection.getId());
        if( selectionTagValues == null || selectionTagValues.isEmpty() ) {
            log.error("No tags in selection");
               setStatus(Status.CLIENT_ERROR_BAD_REQUEST);  // cannot make a certificate request without a valid selection;  we can't pick one automatically unless the administrator has configured a default selection and in that case we wouldn't even be searching here.
               return null;            
        }
        selection.setTags(selectionTagValues);
        
        // at this point we have a request for a subject (host uuid) and a specific selection of tags for that subject
        long certificateRequestId = certificateRequestDao.insert(certificateRequest.getUuid(), certificateRequest.getSubject(), selection.getId());
        certificateRequest.setId(certificateRequestId); // XXX of no use to the client, maybe remove this
        certificateRequest.setStatus("New");
        
        // if sysadmin has configured automatic approvals, we need to check if we have a ca key to use
        if( Global.configuration().isApproveAllCertificateRequests() ) {
            // check if we have a private key to use for signing
            PrivateKey cakey = Global.cakey();
            X509Certificate cakeyCert = Global.cakeyCert();
            if( cakey != null && cakeyCert != null ) {
                // we will automatically sign the request;  so mark it as pending
                certificateRequestDao.updateStatus(certificateRequestId, "Pending");
                certificateRequest.setStatus("Pending");
                // sign the cetificate; XXX TODO should be moved to another class and called from here
                log.debug("Building certificate for request: {}", certificateRequest.getSubject());
                try {
                    X509AttrBuilder builder = X509AttrBuilder.factory()
                            .issuerName(cakeyCert)
                            .issuerPrivateKey(cakey)
                            .randomSerial()
                            .subjectUuid(UUID.valueOf(certificateRequest.getSubject()))
                            .expires(7, TimeUnit.DAYS);
                    for (SelectionTagValue tag : selection.getTags()) {
                        log.debug("Adding attribute: {} = {}", tag.getTagOid(), tag.getTagValue());
                        builder.attribute(tag.getTagOid(), tag.getTagValue());
                    }
                    byte[] attributeCertificateBytes = builder.build();
                    if( attributeCertificateBytes == null ) {
                        log.error("Cannot build attribute certificate");
                        for(Fault fault : builder.getFaults()) {
                            log.error(String.format("%s%s", fault.toString(), fault.getCause() == null ? "" : ": "+fault.getCause().getMessage()));
                        }
                        throw new IllegalArgumentException("Cannot build attribute certificate");
                    }
                    X509AttributeCertificateHolder certificateHolder = new X509AttributeCertificateHolder(attributeCertificateBytes);
                    Certificate certificate = Certificate.valueOf(certificateHolder.getEncoded());
                    certificate.setUuid(new UUID());
                    long certificateId = certificateDao.insert(certificate.getUuid(), certificate.getCertificate(), certificate.getSha256().toHexString(), certificate.getPcrEvent().toHexString(), certificate.getSubject(), certificate.getIssuer(), certificate.getNotBefore(), certificate.getNotAfter());
                    // now the certificate has been created so update the certificate request record
                    certificateRequestDao.updateApproved(certificateRequestId, certificateId);
                    certificateRequest.setCertificateId(certificateId); // XXX of no use to client, maybe remove this
                    certificateRequest.setCertificate(certificate.getUuid());
                    certificateRequest.setStatus("Done"); // done automatically in the database record by updateApproved() but we also need it here to send backto the client
                }
                catch(Exception e) {
                    log.debug("Cannot create attribute certificate", e);
                    setStatus(Status.SERVER_ERROR_INTERNAL);
                    // we will return the certificateRequest object with an updated status later normally
                    certificateRequest.setStatus("Error"); // to indicate to client that we don't know when this miht be resolved... could be an input issue, or could be a server issue
                }
            }
            else {
                log.warn("Automatic approval of requests is enabled but no certificate authority is avaialable");
                // no need to set any status here, we'll just return the request object later normally
            }
        }
        else {
            // automatic approval not enabled... so do nothing. certificate request is already marked as new , hopefully someone is polling the certificate requests and approvign them elsewhere
        }

        return certificateRequest;
    }

    /**
     * Note: must use the list wrapper class. If you try to accept CertificateRequest[] certificateRequests as a parameter, you will get this
     * exception: com.fasterxml.jackson.databind.JsonMappingException: Can not deserialize instance of
     * com.intel.dcsg.cpg.acertificateRequest.model.CertificateRequest out of START_ARRAY token
     *
     * java.lang.ClassCastException: java.util.LinkedHashMap cannot be cast to com.intel.dcsg.cpg.acertificateRequest.model.CertificateRequest
     *
     * @param certificateRequests
     * @return
     * @throws SQLException
     */
    @Post("json:json")
    public CertificateRequest[] insertCertificateRequests(CertificateRequest[] certificateRequests) throws SQLException, IOException {
        CertificateRequest[] results = new CertificateRequest[certificateRequests.length];
        for (int i = 0; i < certificateRequests.length; i++) {
            results[i] = insertCertificateRequest(certificateRequests[i]);
//            insertCertificateRequest(certificateRequests[i]);
        }
        return results;
//        return certificateRequests;
    }

    /**
     * References:
     * http://www.jooq.org/doc/2.6/manual/sql-building/sql-statements/dsl-and-non-dsl/
     * http://comments.gmane.org/gmane.comp.java.restlet.devel/1115
     * http://blog.restlet.com/2006/11/15/reconsidering-content-negotiation/
     * http://www.jooq.org/doc/3.1/manual/sql-building/table-expressions/nested-selects/
     * 
     * Because certificateRequest values are in a separate table, if the client wants to find certificateRequests that
     * have specific values, we need to search for those values first. 
     * 
     * find certificateRequest where certificateRequest.id = certificateRequest_value.certificateRequestId and certificateRequest_value
     * 
     * @param query
     * @return
     * @throws SQLException 
     */
    
    @Get("json")
    public CertificateRequest[] search() throws SQLException {
        CertificateRequestSearchCriteria query = new CertificateRequestSearchCriteria();
        query.id = getQuery().getFirstValue("id") == null || getQuery().getFirstValue("id").isEmpty() ? null : UUID.valueOf(getQuery().getFirstValue("id"));
        query.subjectEqualTo = getQuery().getFirstValue("subjectEqualTo");
        query.subjectContains = getQuery().getFirstValue("subjectContains");
        query.selectionEqualTo = getQuery().getFirstValue("selectionEqualTo");
        query.selectionContains = getQuery().getFirstValue("selectionContains");
        query.statusEqualTo = getQuery().getFirstValue("statusEqualTo");
        log.debug("Search: {}", getQuery().getQueryString());
        SelectQuery sql = jooq.select()
                .from(CERTIFICATE_REQUEST) // .join(CERTIFICATE_REQUEST_TAG_VALUE)
                //.on(CERTIFICATE_REQUEST_TAG_VALUE.CERTIFICATEREQUESTID.equal(CERTIFICATE_REQUEST.ID)))
                .getQuery();
        if( query.id != null ) {
//            sql.addConditions(TAG.UUID.equal(query.id.toByteArray().getBytes())); // when uuid is stored in database as binary
            sql.addConditions(CERTIFICATE_REQUEST.UUID.equal(query.id.toString())); // when uuid is stored in database as the standard UUID string format (36 chars)
        }
        if( query.subjectEqualTo != null  && query.subjectEqualTo.length() > 0 ) {
            sql.addConditions(CERTIFICATE_REQUEST.SUBJECT.equal(query.subjectEqualTo));
        }
        if( query.subjectContains != null  && query.subjectContains.length() > 0  ) {
            sql.addConditions(CERTIFICATE_REQUEST.SUBJECT.equal(query.subjectContains));
        }
        if( query.selectionEqualTo != null  && query.selectionEqualTo.length() > 0 ) {
            sql.addConditions(CERTIFICATE_REQUEST.SUBJECT.equal(query.selectionEqualTo));
        }
        if( query.selectionContains != null  && query.selectionContains.length() > 0  ) {
            sql.addConditions(CERTIFICATE_REQUEST.SUBJECT.equal(query.selectionContains));
        }
        if( query.statusEqualTo != null  && query.statusEqualTo.length() > 0 ) {
            sql.addConditions(CERTIFICATE_REQUEST.STATUS.equal(query.statusEqualTo));
        }
        sql.addOrderBy(CERTIFICATE_REQUEST.ID);
        Result<Record> result = sql.fetch();
        CertificateRequest[] certificateRequests = new CertificateRequest[result.size()];
        log.debug("Got {} records", certificateRequests.length);
        int i = -1; //  index into the target array certificateRequests
        long c = -1; // id of the current certificate request object built, used to detect when it's time to build the next one
        for(Record r : result) {
            if( r.getValue(CERTIFICATE_REQUEST.ID) != c ) {
                i++;
                c = r.getValue(CERTIFICATE_REQUEST.ID);
                certificateRequests[i] = new CertificateRequest();
                certificateRequests[i].setId(r.getValue(CERTIFICATE_REQUEST.ID));
                certificateRequests[i].setUuid(UUID.valueOf(r.getValue(CERTIFICATE_REQUEST.UUID)));
                certificateRequests[i].setSubject(r.getValue(CERTIFICATE_REQUEST.SUBJECT));
//                certificateRequests[i].setTags(new ArrayList<CertificateRequestTagValue>());
                certificateRequests[i].setSelectionId(r.getValue(CERTIFICATE_REQUEST.SELECTIONID));
                certificateRequests[i].setStatus(r.getValue(CERTIFICATE_REQUEST.STATUS));
                if( r.getValue(CERTIFICATE_REQUEST.CERTIFICATEID) != null ) { // a Long object, can be null
                    certificateRequests[i].setCertificateId(r.getValue(CERTIFICATE_REQUEST.CERTIFICATEID)); // a long primitive, cannot set to null
                }
                // XXX TODO instead of doing a separate query here probably faster to do a join above since it's a 1:1 
                Selection selection = selectionDao.findById(certificateRequests[i].getSelectionId());
                certificateRequests[i].setSelection(selection.getUuid().toString());
                if( certificateRequests[i].getCertificateId() > 0 ) {
                    Certificate certificate = certificateDao.findById(certificateRequests[i].getCertificateId());
                    if( certificate != null ) {
                        certificateRequests[i].setCertificate(certificate.getUuid());
                    }
                }
            }
        }
        sql.close();
        return certificateRequests;
    }
}
