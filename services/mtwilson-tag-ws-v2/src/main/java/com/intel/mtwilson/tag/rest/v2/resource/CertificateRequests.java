/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.rest.v2.resource;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.tag.model.CertificateRequest;
import com.intel.mtwilson.tag.model.CertificateRequestCollection;
import com.intel.mtwilson.tag.model.CertificateRequestFilterCriteria;
import com.intel.mtwilson.tag.model.CertificateRequestLocator;
import com.intel.mtwilson.jersey.NoLinks;
import com.intel.mtwilson.jersey.http.OtherMediaType;
import com.intel.mtwilson.jersey.resource.AbstractJsonapiResource;
import com.intel.mtwilson.launcher.ws.ext.V2;
import com.intel.mtwilson.tag.rest.v2.repository.CertificateRequestRepository;
import java.nio.charset.Charset;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO: add a path /certificate_requests/{id}/content  to get the content as-is,
 * and remove the content from the regular response of /certificate_requests/{id}
 * from simple and jsonapi methods. 
 * 
 * @author ssbangal
 */
@V2
@Path("/certificate_requests")
public class CertificateRequests extends AbstractJsonapiResource<CertificateRequest, CertificateRequestCollection, CertificateRequestFilterCriteria, NoLinks<CertificateRequest>, CertificateRequestLocator> {

    private Logger log = LoggerFactory.getLogger(getClass().getName());
    private CertificateRequestRepository repository;
    
    public CertificateRequests() {
        repository = new CertificateRequestRepository();
    }
    
    @Override
    protected CertificateRequestCollection createEmptyCollection() {
        return new CertificateRequestCollection();
    }

    @Override
    protected CertificateRequestRepository getRepository() {
        return repository;
    }
    
    @Override
    @Consumes({MediaType.APPLICATION_JSON, OtherMediaType.APPLICATION_YAML, OtherMediaType.TEXT_YAML})
    @Produces({MediaType.APPLICATION_JSON, OtherMediaType.APPLICATION_YAML, OtherMediaType.TEXT_YAML})
    @POST
    public CertificateRequest createOne(@BeanParam CertificateRequestLocator locator, CertificateRequest item) {
        CertificateRequest response = super.createOne(locator, item);
        // TODO:  this doesn't conform to JSON API,  the non-jsonapi links should go in the meta.links section;   also TODO revise NoLinks in the class declaration
        response.getLinks().put("status", String.format("/certificate_requests/%s", response.getId().toString()));
        response.getLinks().put("certificate", String.format("/certificates?certificateRequestIdEqualTo=%s", response.getId().toString()));
        return response;
    }

    /**
     * Because the selection xml format (plaintext or encrypted) does not
     * include the target host's subject uuid, the client must specify
     * the target host subject uuid either with an HTTP header "Subject" whose
     * value is the uuid, or with a query parameter "subject" whose value is
     * the uuid.  If both are present only the HTTP header is used. If neither
     * is present the server will return a bad request error.
     * 
     * Unlike the JSON API, this method does not return the original request
     * as the response because the clients that send XML or encrypted XML
     * don't need it echoed back to them.
     * 
     * @param locator
     * @param message
     * @param request 
     */
    @Consumes({MediaType.APPLICATION_XML, OtherMediaType.MESSAGE_RFC822})
    @POST
    public void createOneXml(@BeanParam CertificateRequestLocator locator, byte[] message, @Context HttpServletRequest request, @Context HttpServletResponse response) {
        CertificateRequest certificateRequest = new CertificateRequest();
        certificateRequest.setId(new UUID());
        certificateRequest.setStatus("New");
        certificateRequest.setSubject(getSubject(request, locator));
        certificateRequest.setContent(message);
        certificateRequest.setContentType(request.getContentType()); // either application/xml or message/rfc822
        getRepository().create(certificateRequest);
        // TODO:  return certificate request id so client knows where to get updates (in case of external CA) /certificate_requests/{csrId}  and where to get the generated cert   /certificates?certificateRequestIdEqualTo={csrId}
        response.addHeader("Link", String.format("</certificate_requests/%s>; rel=status", certificateRequest.getId().toString()));
        response.addHeader("Link", String.format("</certificates?certificateRequestIdEqualTo=%s>; rel=certificate", certificateRequest.getId().toString()));
    }
    
    private String getSubject(HttpServletRequest request, CertificateRequestLocator locator) {
        String subject = request.getHeader("Subject");
        if( subject != null && !subject.isEmpty()) {
            return subject; 
        }
        else if( locator.subject != null && !locator.subject.isEmpty() ) {
            return locator.subject; // from query paramter  ?subject={subject}            
        }
        else {
            throw new WebApplicationException(Status.BAD_REQUEST); // TODO:  ErrorCode enum with internationalized message saying subject is required in header or query param
        }
    }
    
}
