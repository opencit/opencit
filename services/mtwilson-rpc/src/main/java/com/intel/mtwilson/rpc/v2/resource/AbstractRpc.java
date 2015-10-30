/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.rpc.v2.resource;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.collection.MultivaluedHashMap;
import com.intel.dcsg.cpg.validation.Fault;
import com.intel.mtwilson.rpc.v2.model.Rpc;
import com.intel.mtwilson.rpc.v2.model.RpcPriv;
import com.intel.mtwilson.v2.rpc.RpcUtil;
import com.thoughtworks.xstream.XStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.charset.Charset;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyReader;
import org.glassfish.jersey.message.MessageBodyWorkers;

/**
 * Code shared by AsyncRpc and BlockingRpc, mostly converting the POST body
 * from its original format to xstream for storing in database.
 * 
 * @author jbuhacoff
 */
public class AbstractRpc {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractRpc.class);

    protected RpcRepository repository = new RpcRepository();
    
    protected RpcRepository getRepository() { return repository; }
    
    @Context
    protected MessageBodyWorkers workers;
    
    protected MessageBodyWorkers getMessageBodyWorkers() { return workers; }
    
    protected XStream xstream = new XStream();

    protected RpcAdapter getAdapter(String name) {
        RpcAdapter adapter = RpcUtil.findRpcForName(name);
        if (adapter == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return adapter;
    }

    protected Object getInput(byte[] input, Class<?> inputClass, HttpServletRequest request) {
        Object inputObject;
        // convert the client's input into our internal format
        try {
            String inputAccept = RpcUtil.getPreferredTypeFromAccept(request.getHeader(HttpHeaders.CONTENT_TYPE));
            log.debug("Client prefers content type: {}", inputAccept);
            MediaType inputMediaType = MediaType.valueOf(inputAccept);

            // use jersey classes to find the appropriate message body reader based on request's content type 
            final MessageBodyReader messageBodyReader =
                    workers.getMessageBodyReader(inputClass, inputClass,
                    inputClass.getAnnotations(), inputMediaType);
            if (messageBodyReader == null) {
                throw new WebApplicationException(Response.Status.NOT_ACCEPTABLE);
            }
            javax.ws.rs.core.MultivaluedHashMap jaxrsHeaders = new javax.ws.rs.core.MultivaluedHashMap();
            MultivaluedHashMap<String, String> headerMap = RpcUtil.convertHeadersToMultivaluedMap(request);
            jaxrsHeaders.putAll(headerMap.getMap());

            inputObject = messageBodyReader.readFrom(inputClass, inputClass, inputClass.getAnnotations(), inputMediaType, jaxrsHeaders, new ByteArrayInputStream(input));

        } catch (IOException e) {
            log.error ("IOException.", e);
            throw new WebApplicationException("Invalid input to RPC", e); 
        }
        return inputObject;
    }
    
    
    protected byte[] toXml(Object inputObject) {
//        log.info("BlockRPC: input xml is {}.", xstream.toXML(inputObject));
        return xstream.toXML(inputObject).getBytes(Charset.forName("UTF-8"));
    }
    
}
