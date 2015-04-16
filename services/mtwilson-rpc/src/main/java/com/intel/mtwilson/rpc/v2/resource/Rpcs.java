/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.rpc.v2.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.mtwilson.rpc.v2.model.Rpc;
import com.intel.mtwilson.rpc.v2.model.RpcFilterCriteria;
import com.intel.mtwilson.rpc.v2.model.RpcCollection;
import com.intel.mtwilson.jaxrs2.NoLinks;
import com.intel.mtwilson.jaxrs2.server.resource.AbstractJsonapiResource;
import com.intel.mtwilson.launcher.ws.ext.V2;
import com.intel.mtwilson.rpc.v2.model.RpcLocator;
import com.intel.mtwilson.rpc.v2.model.RpcPriv;
import com.thoughtworks.xstream.XStream;
import java.nio.charset.Charset;
//import javax.ejb.Stateless;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BeanParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.message.MessageBodyWorkers;

/**
 *
 * @author jbuhacoff
 */
@V2
//@Stateless
@Path("/rpcs")
public class Rpcs extends AbstractJsonapiResource<Rpc,RpcCollection,RpcFilterCriteria,NoLinks<Rpc>,RpcLocator> {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Rpcs.class);
  
    private RpcRepository repository;
    private ObjectMapper mapper = new ObjectMapper();
    
    public Rpcs() {
//        super();
        repository = new RpcRepository();
//        setRepository(repository);
    }
    
    @Override
    protected RpcRepository getRepository() { return repository; }
    
    @Context
    private MessageBodyWorkers workers;
    
/*
    @Override
    protected RpcCollection createEmptyCollection() {
        return new RpcCollection();
    }
    */
    
    @Override
    @Path("/{id}")
    @DELETE
    public void deleteOne(@BeanParam RpcLocator locator, @Context HttpServletRequest request, @Context HttpServletResponse response) {
        log.debug("Rpcs deleteOne");
        boolean isRunning = false;
        if (isRunning) {
            throw new WebApplicationException(Response.Status.CONFLICT);
        }
        // now remove from database
        super.deleteOne(locator, request, response);
    }
    
    @Path("/{id}/input")
    @GET
    public Response getRpcInput(@BeanParam RpcLocator locator) {
        log.debug("rpc get input, sending fake data");
        RpcPriv rpc = repository.retrieveInput(locator);
        if (rpc != null) {
            rpc.setInput("<input><sample/></input>".getBytes());
            Response response = Response.ok(rpc.getInput(), "application/octet-stream" /*rpc.getInputContentType()*/).build();
            return response;
        } else {
            return null;
        }
            
    }
/*
    @Path("/{id}/output")
    @GET
    public Response getRpcOutput(@BeanParam RpcLocator locator, @Context HttpServletRequest request) {
        Rpc rpc = repository.retrieveOutput(locator); 
        
        // convert the intermediate output to client's requested output type
        log.debug("Client requested output type: {}" ,request.getHeader(HttpHeaders.ACCEPT));
//        rpc.setOutputContentType(MediaType.WILDCARD); 

        String xml = new String(rpc.getOutput(), Charset.forName("UTF-8"));
        log.debug("output xml: {}", xml);
        // use jersey classes to find the appropriate message body reader based on request's content type 
        XStream xs = new XStream();
        Object pojo = xs.fromXML(xml);
        Class outputContentClass = pojo.getClass();
        log.debug("output pojo: {}", pojo.getClass().getName());
        
        com.intel.dcsg.cpg.util.MultivaluedHashMap<String,String> headerMap = RpcUtil.convertHeadersToMultivaluedMap(request);
        String responseAccept = RpcUtil.getPreferredTypeFromAccept(headerMap.get("Accept"));
        MediaType responseMediaType = MediaType.valueOf(responseAccept);
        final MessageBodyWriter messageBodyWriter2 =
            workers.getMessageBodyWriter(outputContentClass, outputContentClass,
                    new Annotation[]{}, responseMediaType); 
        if( messageBodyWriter2 == null ) {
            // we don't know how to write the final response!
            log.error("Cannot find MessageBodyWriter for response");
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        try {
            javax.ws.rs.core.MultivaluedHashMap jaxrsHeaders = new javax.ws.rs.core.MultivaluedHashMap();
            jaxrsHeaders.putAll(headerMap.getMap());
//            Object responseObject = messageBodyReader2.readFrom(outputContentClass, outputContentClass, new Annotation[]{}, outputMediaType, jaxrsHeaders, new ByteArrayInputStream(rpc.getOutput()));
            Object responseObject = pojo;
            log.debug("Read intermediate response object: {}", mapper.writeValueAsString(responseObject));
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            messageBodyWriter2.writeTo(responseObject, outputContentClass, outputContentClass, new Annotation[]{}, responseMediaType, jaxrsHeaders, out);
            byte[] responseContent = out.toByteArray(); // this will go in database
            log.debug("Response object: {}", new String(responseContent)); // we can only do this because we know the output is xml format for testing...
            
            Response response = Response.ok(responseContent,responseMediaType).build();
            return response;
            
        }
        catch(Exception e) {
            log.error("Cannot convert output to requested response type: {}", e.getMessage());
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }        
    }
    */
    @Path("/{id}/output")
    @GET
//    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, OtherMediaType.APPLICATION_YAML, OtherMediaType.TEXT_YAML})
    @Produces(MediaType.WILDCARD)
    public Object getRpcOutput(@BeanParam RpcLocator locator, @Context HttpServletRequest request) {
        RpcPriv rpc = repository.retrieveOutput(locator); 
        if (rpc == null) {
            return null;
        }
        // convert the intermediate output to client's requested output type
        log.debug("Client requested output type: {}" ,request.getHeader(HttpHeaders.ACCEPT));
//        rpc.setOutputContentType(MediaType.WILDCARD); 

        String xml = new String(rpc.getOutput(), Charset.forName("UTF-8"));
        log.debug("output xml: {}", xml);
        // use jersey classes to find the appropriate message body reader based on request's content type 
        XStream xs = new XStream();
        Object pojo = xs.fromXML(xml);
//        Class outputContentClass = pojo.getClass();
        log.debug("output pojo: {}", pojo.getClass().getName());
        return pojo;
    }

    @Override
    protected RpcCollection createEmptyCollection() {
        return new RpcCollection();
    }
    
        
}
