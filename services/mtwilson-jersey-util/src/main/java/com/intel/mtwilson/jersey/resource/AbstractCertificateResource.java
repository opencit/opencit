/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jersey.resource;

import com.intel.dcsg.cpg.validation.ValidationUtil;
import com.intel.mtwilson.jersey.CertificateDocument;
import com.intel.mtwilson.jersey.DocumentCollection;
import com.intel.mtwilson.jersey.FilterCriteria;
import com.intel.mtwilson.jersey.PatchLink;
import com.intel.mtwilson.jersey.http.OtherMediaType;
import java.security.cert.X509Certificate;
import java.util.List;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author jbuhacoff
 */
public abstract class AbstractCertificateResource<T extends CertificateDocument, C extends DocumentCollection<T>, F extends FilterCriteria<T>, L extends PatchLink<T>> extends AbstractResource<T,C,F,L> {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractCertificateResource.class);
    
    // XXX TODO find a mediatype/structure for list of DER-encoded certs...   could use APPLICATION_ZIP  but then we'd need to name the files inside! could use cert hashes...
    @GET
    @Produces({ OtherMediaType.APPLICATION_X_PEM_FILE, MediaType.TEXT_PLAIN})
    public X509Certificate[] searchX509CertificateCollection(@BeanParam F criteria) {
        log.debug("searchX509CertificateCollection");
        ValidationUtil.validate(criteria); // throw new MWException(e, ErrorCode.AS_INPUT_VALIDATION_ERROR, input, method.getName());
        C collection = search(criteria);
        List<T> list = collection.getDocuments();
        X509Certificate[] certificates = new X509Certificate[list.size()];
        for(int i=0; i<certificates.length; i++) {
            certificates[i] = list.get(i).getX509Certificate();
        }
        return certificates;
    }

    @Path("/{id}")
    @GET
    @Produces({MediaType.APPLICATION_OCTET_STREAM, OtherMediaType.APPLICATION_PKIX_CERT, OtherMediaType.APPLICATION_X_PEM_FILE, MediaType.TEXT_PLAIN})
    public X509Certificate retrieveOneX509Certificate(@PathParam("id") String id) {
        log.debug("retrieveOneX509Certificate");
        T item = retrieve(id);
        X509Certificate certificate = item.getX509Certificate();
        return certificate;
    }
    
}
