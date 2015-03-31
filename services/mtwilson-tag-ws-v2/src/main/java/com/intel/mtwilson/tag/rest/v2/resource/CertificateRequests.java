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
import com.intel.mtwilson.jaxrs2.NoLinks;
import com.intel.mtwilson.jaxrs2.mediatype.CryptoMediaType;
import com.intel.mtwilson.jaxrs2.mediatype.DataMediaType;
import com.intel.mtwilson.jaxrs2.server.resource.AbstractJsonapiResource;
import com.intel.mtwilson.launcher.ws.ext.V2;
import com.intel.mtwilson.tag.Util;
import com.intel.mtwilson.tag.rest.v2.repository.CertificateRequestRepository;
import com.intel.mtwilson.tag.selection.xml.SelectionsType;
import java.io.IOException;
import java.nio.charset.Charset;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ssbangal
 */
@V2
@Path("/tag-certificate-requests")
public class CertificateRequests extends AbstractJsonapiResource<CertificateRequest, CertificateRequestCollection, CertificateRequestFilterCriteria, NoLinks<CertificateRequest>, CertificateRequestLocator> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CertificateRequests.class);

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
    
    /**
     * Same as superclass but adds two links to the response headers (tentative)
     * 
     * @param locator
     * @param certificateRequest
     * @return 
     */
    @Override
    @POST
    @RequiresPermissions("tag_certificate_requests:create")         
    public CertificateRequest createOne(@BeanParam CertificateRequestLocator locator, CertificateRequest certificateRequest,  @Context HttpServletRequest httpServletRequest, @Context HttpServletResponse httpServletResponse) {
        certificateRequest = super.createOne(locator, certificateRequest, httpServletRequest, httpServletResponse);
        certificateRequest.getLinks().put("status", String.format("/tag-certificate-requests/%s", certificateRequest.getId().toString()));
        certificateRequest.getLinks().put("certificate", String.format("/tag-certificates?certificateRequestIdEqualTo=%s", certificateRequest.getId().toString()));
        certificateRequest.getLinks().put("content", String.format("/tag-certificate-requests/%s/content", certificateRequest.getId().toString()));
        return certificateRequest;
    }

    protected SelectionsType getSelection(CertificateRequest certificateRequest) throws IOException {
        SelectionsType selection;
        switch(certificateRequest.getContentType()) {
            case MediaType.APPLICATION_JSON:
                selection = Util.fromJson(new String(certificateRequest.getContent(), Charset.forName("UTF-8")));
                break;
            case MediaType.APPLICATION_XML:
                selection = Util.fromXml(new String(certificateRequest.getContent(), Charset.forName("UTF-8")));
                break;
            default:
                throw new IOException("Unsupported content type of selection data");
        }
        return selection;
    }
    
    @Path("/{id}/content")
    @Produces({MediaType.APPLICATION_JSON, DataMediaType.APPLICATION_YAML, DataMediaType.TEXT_YAML})
    @GET
    @RequiresPermissions("tag_certificate_requests:retrieve")         
    public String retrieveOneSelectionJson(@BeanParam CertificateRequestLocator locator, @Context HttpServletRequest request, @Context HttpServletResponse response) throws IOException {
        CertificateRequest certificateRequest = super.retrieveOne(locator, request, response);
        SelectionsType selection = getSelection(certificateRequest);
        response.addHeader("Link", String.format("</tag-certificate-requests/%s>; rel=status", certificateRequest.getId().toString()));
        return Util.toJson(selection);
    }
    
    @Path("/{id}/content")
    @Produces({MediaType.APPLICATION_XML})
    @GET
    @RequiresPermissions("tag_certificate_requests:retrieve")         
    public String retrieveOneSelectionXml(@BeanParam CertificateRequestLocator locator,@Context HttpServletRequest request, @Context HttpServletResponse response) throws IOException {
        CertificateRequest certificateRequest = super.retrieveOne(locator, request, response);
        SelectionsType selection = getSelection(certificateRequest);
        response.addHeader("Link", String.format("</tag-certificate-requests/%s>; rel=status", certificateRequest.getId().toString()));
        return Util.toXml(selection);
    }
    
}
