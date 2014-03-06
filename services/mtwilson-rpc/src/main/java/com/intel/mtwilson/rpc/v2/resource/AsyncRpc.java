/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.rpc.v2.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.intel.dcsg.cpg.extensions.Extensions;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.rfc822.Message;
import com.intel.dcsg.cpg.util.MultivaluedHashMap;
import com.intel.dcsg.cpg.validation.Fault;
import com.intel.mtwilson.jersey.http.OtherMediaType;
import com.intel.mtwilson.launcher.ws.ext.RPC;
import com.intel.mtwilson.launcher.ws.ext.V2;
import com.intel.mtwilson.rpc.v2.model.Rpc;
import com.intel.mtwilson.rpc.v2.model.RpcPriv;
import com.intel.mtwilson.v2.rpc.RpcInvoker;
import com.intel.mtwilson.v2.rpc.RpcUtil;
import com.thoughtworks.xstream.XStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
//import javax.ejb.Stateless;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
//import javax.ws.rs.core.MultivaluedHashMap;
//import com.intel.dcsg.cpg.util.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.message.MessageBodyWorkers;

/**
 * Characteristics of a Remote Procedure Call is that the input and output are
 * likely different types, that the client and server may negotiate for the
 * processing to occur immediately (synchronous) or to be queued (asynchronous),
 * that an RPC consists of a single method that is a verb phrase, and that the
 * server is not expected to keep track of past inputs (but it is allowed to
 * store them and index them as well as the outputs, of course) - if any storing
 * or indexing of RPCs takes places, the interface to that stored data would be
 * a resource.
 *
 * In contrast, a resource has one type and typically has several standard
 * operations are defined on that type: create, store (update), retrieve,
 * delete, and search which is defined on the collection of that type; a
 * resource operation typically happens immediately (but the server may still
 * choose to delay it and return an appropriate "accepted" http status code).
 *
 * @author jbuhacoff
 */
@V2
//@Stateless
@Path("/rpc-async")
public class AsyncRpc {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AsyncRpc.class);
    private ObjectMapper mapper; // for debug only
    private RpcRepository repository = new RpcRepository();

    public AsyncRpc() {
        mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
    }
    @Context
    private MessageBodyWorkers workers;

    @Path("/{name}")
    @POST
    @Consumes(MediaType.WILDCARD)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, OtherMediaType.APPLICATION_YAML, OtherMediaType.TEXT_YAML})
    public Rpc invokeAsyncRemoteProcedureCall(@PathParam("name") String name, @Context HttpServletRequest request, byte[] input) {
        // make sure we have an extension to handle this rpc
        RpcAdapter adapter = RpcUtil.findRpcForName(name);// always creates a new instance of the adapter for the rpc
        if (adapter == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        byte[] inputXml;
        // convert the client's input into our internal format
        try {
            String inputAccept = RpcUtil.getPreferredTypeFromAccept(request.getHeader(HttpHeaders.ACCEPT));
            log.debug("Client prefers content type: {}", inputAccept);
            // XXX TODO  call ValidationUtil with inputAccept and a good regex for that header... should allow letters, digits, hyphens, underscores, commas, semicolons, periods - no quotes or other puncutation
            MediaType inputMediaType = MediaType.valueOf(inputAccept);

            // use jersey classes to find the appropriate message body reader based on request's content type 
            final MessageBodyReader messageBodyReader =
                    workers.getMessageBodyReader(adapter.getInputClass(), adapter.getInputClass(),
                    new Annotation[]{}, inputMediaType);
            if (messageBodyReader == null) {
                throw new WebApplicationException(Status.NOT_ACCEPTABLE); // TODO   make a more user friendly message and i18n
            }
            javax.ws.rs.core.MultivaluedHashMap jaxrsHeaders = new javax.ws.rs.core.MultivaluedHashMap();
            MultivaluedHashMap<String, String> headerMap = RpcUtil.convertHeadersToMultivaluedMap(request);
            jaxrsHeaders.putAll(headerMap.getMap());

            Object inputObject = messageBodyReader.readFrom(adapter.getInputClass(), adapter.getInputClass(), new Annotation[]{}, inputMediaType, jaxrsHeaders, new ByteArrayInputStream(input));

            // now serialize the input object with xstream
            XStream xs = new XStream();
            String xml = xs.toXML(inputObject);
            log.debug("input xml: {}", xml);

            inputXml = xml.getBytes(Charset.forName("UTF-8"));

        } catch (IOException e) {
            throw new WebApplicationException("Invalid input to RPC"); // TODO  i18n mesasge
        }


        // prepare the rpc task with the input
        RpcPriv rpc = new RpcPriv();
        rpc.setId(new UUID());
        rpc.setName(name);
        rpc.setInput(inputXml);
//        com.intel.dcsg.cpg.util.MultivaluedHashMap<String,String> headers = RpcUtil.convertHeadersToMultivaluedMap(request);
//        rpc.setInputHeaders(toRfc822(headers));
        rpc.setStatus(Rpc.Status.QUEUE);

        // store it
        repository.create(rpc);

        // queue it (must follow storage to prevent situation where an executing task needs to store an update to the table and it hasn't been stored yet)
//        RpcInvoker.getInstance().add(rpc.getId());

        Rpc status = new Rpc();
        status.copyFrom(rpc);

        return status;
    }

    /*
     private String toRfc822(com.intel.dcsg.cpg.util.MultivaluedHashMap<String,String> headers) {
     ArrayList<String> lines = new ArrayList<String>();
     for(String name : headers.keySet()) {
     for(String value : headers.getAll(name)) {
     lines.add(String.format("%s: %s", name, value));
     }
     }
     return StringUtils.join(lines, "\n");
     }*/
}
