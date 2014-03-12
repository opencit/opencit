/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.rest.v2.rpc;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.validation.Fault;
import com.intel.mtwilson.My;
import com.intel.mtwilson.datatypes.AssetTagCertCreateRequest;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import com.intel.mtwilson.launcher.ws.ext.RPC;
import com.intel.mtwilson.tag.common.Global;
import com.intel.mtwilson.tag.common.X509AttrBuilder;
import com.intel.mtwilson.tag.dao.TagJdbi;
import com.intel.mtwilson.tag.dao.jdbi.CertificateDAO;
import com.intel.mtwilson.tag.dao.jdbi.CertificateRequestDAO;
import com.intel.mtwilson.tag.dao.jdbi.SelectionDAO;
import com.intel.mtwilson.tag.dao.jdbi.SelectionKvAttributeDAO;
import com.intel.mtwilson.tag.rest.v2.model.Certificate;
import com.intel.mtwilson.tag.rest.v2.model.CertificateRequest;
import com.intel.mtwilson.tag.rest.v2.model.Selection;
import com.intel.mtwilson.tag.rest.v2.model.SelectionKvAttribute;
import com.intel.mtwilson.tag.rest.v2.repository.CertificateRepository;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.bouncycastle.cert.X509AttributeCertificateHolder;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
@RPC("certificate_auto_deploy")
@JacksonXmlRootElement(localName="certificate_auto_deploy")
public class CertificateAutoDeployRunnable extends ServerResource implements Runnable{
    
    private Logger log = LoggerFactory.getLogger(getClass().getName());
       
    private CertificateRequest item;

    public CertificateRequest getItem() {
        return item;
    }

    public void setItem(CertificateRequest item) {
        this.item = item;
    }

    
    @Override
    public void run() {
        log.debug("Got request to auto deploy certificate");        
        
        try (CertificateRequestDAO certRequestDao = TagJdbi.certificateRequestDao(); 
                SelectionDAO selectionDao = TagJdbi.selectionDao();
                SelectionKvAttributeDAO selectionKvAttributeDao = TagJdbi.selectionKvAttributeDao()) {
            log.debug("insertCertificateRequest for subject: {}", item.getSubject());
            if(! UUID.isValid(item.getSubject())) {
                List<TxtHostRecord> hostList = Global.mtwilson().queryForHosts(item.getSubject(),true);
                if(hostList == null || hostList.size() < 1) {
                    log.debug("host uuid didn't return back any results");
                    throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "No matching host found in the system.");
                }
                log.debug("get host uuid returned " + hostList.get(0).Hardware_Uuid);
                item.setSubject(hostList.get(0).Hardware_Uuid);
            }
            // IMPORTANT: provisioning policy choices:
            // Automatic Server-Based: always use the same pre-configured selection; find it in static config, ignore the requestor's selection
            // Manual and Automatic Host-Based: allow the requestor to specify a selection and look it up
            Selection selection = null;
            if( Global.configuration().isAllowTagsInCertificateRequests()) { 
                if (item.getSelectionName()!= null && !item.getSelectionName().isEmpty()) {
                    log.error("insertCertificateRequest processing request");
                    // Since name is unique, the below query is supposed to return back only one row if it exists.
                    Selection selectionObj = selectionDao.findByName(item.getSelectionName());
                    if( selectionObj == null) {
                        throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Specified selection does not exist in the system.");
                    }               
                    selection = selectionObj;
                } else {
                    // XXX TODO - If the selection is not provided, use the actual tags
                }
            }
            if( selection == null && Global.configuration().isAllowAutomaticTagSelection() ) {
                String selectionName = Global.configuration().getAutomaticTagSelectionName() ;
                log.debug("Automatic tag selection: {}", selectionName);
                if( selectionName == null || selectionName.isEmpty()  ) {
                    log.error("Server configured for automatic tag selection but no selection has been specified");
                    setStatus(Status.SERVER_ERROR_INTERNAL); 
                    throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Server configured for automatic tag selection but no selection has been specified.");
                }
                Selection selectionObj = selectionDao.findByName(selectionName);
                if( selectionObj == null) {
                    log.error("Server configured for automatic tag selection but '{}' is not available", selectionName);
                    setStatus(Status.SERVER_ERROR_INTERNAL);  // cannot make a certificate request without a valid selection
                    throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Specified selection does not exist in the system.");
                }
                selection = selectionObj;
            }
            // we have a selection object but we need to look up the associated tags before we continue
            log.debug("Get tags for selection with id of {}.", selection.getId().toString());
            // This is the location where we need to ensure that all the attributes that needs to be added to the certificated need to be
            // collected. Currently only the KV(Key-Value) attributes are supported. This KV attribute would always map to OID: "2.5.4.789.2"
            List<SelectionKvAttribute> selectionKvAttributes = selectionKvAttributeDao.findBySelectionIdWithValues(selection.getId());
            if( selectionKvAttributes == null || selectionKvAttributes.isEmpty()) {
                log.error("No tags in selection");
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);  // cannot make a certificate request without a valid selection;  we can't pick one automatically unless the administrator has configured a default selection and in that case we wouldn't even be searching here.
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Specified selection does not have any attibutes.");
            }

            // at this point we have a request for a subject (host uuid) and a specific selection of tags for that subject
            // Since the cert is not created, we will set the certificateID will be null.
            UUID newCertRequestID = new UUID();
            certRequestDao.insert(newCertRequestID, item.getSubject(), selection.getId(), null, item.getAuthorityName());

            // if sysadmin has configured automatic approvals, we need to check if we have a ca key to use
            if( Global.configuration().isApproveAllCertificateRequests() ) {
                // check if we have a private key to use for signing
                PrivateKey cakey = Global.cakey();
                X509Certificate cakeyCert = Global.cakeyCert();
                if( cakey != null && cakeyCert != null ) {
                    // we will automatically sign the request;  so mark it as pending
                    certRequestDao.updateStatus(newCertRequestID, "Pending");
                    item.setStatus("Pending");
                    // sign the cetificate; XXX TODO should be moved to another class and called from here
                    log.debug("Building certificate for request: {}", item.getSubject());
                    
                    try {
                        X509AttrBuilder builder = X509AttrBuilder.factory()
                                .issuerName(cakeyCert)
                                .issuerPrivateKey(cakey)
                                .dateSerial()
                                .subjectUuid(UUID.valueOf(item.getSubject()))
                                .expires(My.configuration().getAssetTagCertificateValidityPeriod(), TimeUnit.DAYS);
                        for (SelectionKvAttribute kvAttr : selectionKvAttributes) {
                            log.debug("Adding attribute : {}={}", kvAttr.getKvAttributeName()+"="+ kvAttr.getKvAttributeValue());
                                builder.attribute(kvAttr.getKvAttributeName(), kvAttr.getKvAttributeValue()); // will get encoded as 2.5.4.789.2 since we are migrating to that ;  after we update the UI to default to 2.5.4.789.2 this case will not get triggered
                                // The below line shows how we we would handle other types of attributes.
                                // XXX  this case should be changed so that the tag value is interpreted as byte array (asn1-encoded value) so we can generate the attribute properly in the certificate
                                // builder.attribute(tag.getTagOid(), tag.getTagValue());  // TODO -  binary/base64/ASN1Encodable                          
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
                        UUID  newCertId = new UUID();
                        certificate.setId(newCertId);
                        
                        // Call into the certificate repository to create the new certificate entry in the database.
                        CertificateRepository certRepo = new CertificateRepository();
                        certRepo.create(certificate);
                                                
                        // now the certificate has been created so update the certificate request record
                        certRequestDao.updateApproved(newCertRequestID, newCertId);
                        item.setCertificateId(newCertId); // XXX of no use to client, maybe remove this
                        item.setStatus("Done"); // done automatically in the database record by updateApproved() but we also need it here to send backto the client
                        
                        // import it to mtw
                        // first we need to check if auto import is already enabled
                        if( My.configuration().getAssetTagAutoImport() == true) {
                            String url = My.configuration().getAssetTagMtwilsonBaseUrl();
                            if(url != null && !url.isEmpty() ) {
                                AssetTagCertCreateRequest request = new AssetTagCertCreateRequest();
                                request.setCertificate(certificate.getCertificate());
                                log.debug("import cert to MTW ");
                                Global.mtwilson().importAssetTagCertificate(request);
                            }
                        }
                    } catch(Exception e) {
                        log.error("Cannot create attribute certificate", e);
                        setStatus(Status.SERVER_ERROR_INTERNAL);
                        // we will return the certificateRequest object with an updated status later normally
                        item.setStatus("Error"); // to indicate to client that we don't know when this miht be resolved... could be an input issue, or could be a server issue
                    }
                } else {
                    log.warn("Automatic approval of requests is enabled but no certificate authority is avaialable");
                    // no need to set any status here, we'll just return the request object later normally
                }
            } else {
                // automatic approval not enabled... so do nothing. certificate request is already marked as new , hopefully someone is polling the certificate requests and approvign them elsewhere
            }
        } catch (ResourceException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during certificate request creation.", ex);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Please see the server log for more details.");
        }       
        
    }
    
}
