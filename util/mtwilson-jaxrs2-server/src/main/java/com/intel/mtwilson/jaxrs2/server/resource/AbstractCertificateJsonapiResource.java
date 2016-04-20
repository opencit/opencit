/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.jaxrs2.server.resource;

import com.intel.dcsg.cpg.validation.ValidationUtil;
import com.intel.mtwilson.jaxrs2.CertificateDocument;
import com.intel.mtwilson.jaxrs2.DocumentCollection;
import com.intel.mtwilson.repository.FilterCriteria;
import com.intel.mtwilson.repository.Locator;
import com.intel.mtwilson.jaxrs2.PatchLink;
import com.intel.mtwilson.jaxrs2.mediatype.CryptoMediaType;
import java.security.cert.X509Certificate;
import java.util.List;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author ssbangal
 */
public abstract class AbstractCertificateJsonapiResource<T extends CertificateDocument, C extends DocumentCollection<T>, F extends FilterCriteria<T>, P extends PatchLink<T>, L extends Locator<T>> extends AbstractJsonapiResource<T,C,F,P,L> {
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractCertificateJsonapiResource.class);
    /*
    public AbstractCertificateJsonapiResource() {
        super();
    }
    */
    @GET
    @Produces({ CryptoMediaType.APPLICATION_X_PEM_FILE, MediaType.TEXT_PLAIN})
    public X509Certificate[] searchX509CertificateCollection(@BeanParam F criteria) {
        log.debug("searchX509CertificateCollection");
        ValidationUtil.validate(criteria); // throw new MWException(e, ErrorCode.AS_INPUT_VALIDATION_ERROR, input, method.getName());
        C collection = getRepository().search(criteria);
        List<T> list = collection.getDocuments();
        X509Certificate[] certificates = new X509Certificate[list.size()];
        for(int i=0; i<certificates.length; i++) {
            certificates[i] = list.get(i).getX509Certificate();
        }
        return certificates;
    }

    @Path("/{id}")
    @GET
    @Produces({MediaType.APPLICATION_OCTET_STREAM, CryptoMediaType.APPLICATION_PKIX_CERT, CryptoMediaType.APPLICATION_X_PEM_FILE, MediaType.TEXT_PLAIN})
    public X509Certificate retrieveOneX509Certificate(@BeanParam L locator) {
        log.debug("retrieveOneX509Certificate");
        T item = getRepository().retrieve(locator);
        if (item == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND); 
        }        
        X509Certificate certificate = item.getX509Certificate();
        return certificate;
    }
    
}
