/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jaxrs2.provider;

import com.intel.mtwilson.jaxrs2.mediatype.CryptoMediaType;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.intel.dcsg.cpg.x509.X509Util;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import org.apache.commons.io.IOUtils;
//import org.codehaus.jackson.jaxrs.JacksonJsonProvider;

/**
 * Reference: https://jersey.java.net/documentation/latest/message-body-workers.html
 * @author jbuhacoff
 */
@Provider
@Consumes({MediaType.APPLICATION_OCTET_STREAM,CryptoMediaType.APPLICATION_PKIX_CERT})
@Produces({MediaType.APPLICATION_OCTET_STREAM,CryptoMediaType.APPLICATION_PKIX_CERT})
public class X509CertificateDerProvider implements
      MessageBodyWriter<X509Certificate>,
      MessageBodyReader<X509Certificate> {

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return X509Certificate.class.isAssignableFrom(type) && ( mediaType.toString().equals(MediaType.APPLICATION_OCTET_STREAM) || mediaType.toString().equals(CryptoMediaType.APPLICATION_PKIX_CERT) );
    }

    @Override
    public long getSize(X509Certificate t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        try {
            return t.getEncoded().length;
        }
        catch(CertificateEncodingException e) {
            return -1;
        }
    }

    @Override
    public void writeTo(X509Certificate t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        try {
            IOUtils.write(t.getEncoded(), entityStream);
        }
        catch(CertificateEncodingException e) {
            throw new IOException(e);
        }
    }

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return X509Certificate.class.isAssignableFrom(type) && ( mediaType.toString().equals(MediaType.APPLICATION_OCTET_STREAM) || mediaType.toString().equals(CryptoMediaType.APPLICATION_PKIX_CERT) );
    }

    @Override
    public X509Certificate readFrom(Class<X509Certificate> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
        try {
            // instead of using X509Util.decodeDerCertificate(byte[]) here we inline it because we have an inputstream instead
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) cf.generateCertificate(entityStream);
            return cert;
        }
        catch(CertificateException e) {
            throw new IOException(e);
        }
    }
    
}
