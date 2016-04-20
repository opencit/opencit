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
import com.intel.mtwilson.jaxrs2.mediatype.CryptoMediaType;
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
@Path("/rpc")
public class BlockingRpc extends AbstractRpc {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BlockingRpc.class);
    private ObjectMapper mapper; // for debug only

    public BlockingRpc() {
        mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
    }

    @Path("/{name}")
    @POST
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.WILDCARD)
    public Object invokeRemoteProcedureCall(@PathParam("name") String name, @Context HttpServletRequest request, byte[] input) {
        // make sure we have an extension to handle this rpc
        RpcAdapter adapter = getAdapter(name);
        
        // convert the client's input into our internal format
        Object inputObject = getInput(input, adapter.getInputClass(), request);
        // now serialize the input object with xstream;  even though we're going to process immediately, we are still going to record the call in the RPC table so we need the xml
        byte[] inputXml = toXml(inputObject);

        // prepare the rpc task with the input
        RpcPriv rpc = new RpcPriv();
        rpc.setId(new UUID());
        rpc.setName(name);
        rpc.setInput(inputXml);
//        com.intel.dcsg.cpg.util.MultivaluedHashMap<String,String> headers = RpcUtil.convertHeadersToMultivaluedMap(request);
//        rpc.setInputHeaders(toRfc822(headers));
        rpc.setStatus(Rpc.Status.PROGRESS); // unlike async where we store it with QUEUE and the RpcInvoker picks it up,  in blocking mode we store it with PROGRESS because we will handle it here and don't want RpcInvoker to get it also

        // store it
        repository.create(rpc);

        // from this point forward the implmentation is a duplicate of what is in RpcInvoker after it deserializes the task object
        Object outputObject;
        try {
            adapter.setInput(inputObject);
            adapter.invoke();
            outputObject = adapter.getOutput();
            List<Fault> faults = adapter.getFaults();
            if (faults != null && faults.size() > 0) {
                // Since there are errors, we will capture the error details and throw an exception.
                for (Fault fault : faults) {
                    log.error("Error during RPC execution: {}", fault.toString());
                }
                throw new Exception("Error during RPC execution"); // this will get converted to the web application exception in the catch block.
            }
//            ((Runnable)inputObject).run();
//            outputObject = inputObject;
        }
        catch(Exception e) {
            log.error("Error while executing RPC {}", rpc.getName(), e);
            rpc.setFaults(new Fault[] { new Fault("Execution failed") });
            rpc.setStatus(Rpc.Status.ERROR);
            repository.store(rpc);
            throw new WebApplicationException("RPC execution failed; see server log for details");
        }
        
        // format output
        try {
            /*
            javax.ws.rs.core.MultivaluedHashMap jaxrsHeaders = new javax.ws.rs.core.MultivaluedHashMap();
            jaxrsHeaders.putAll(headerMap.getMap());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            messageBodyWriter.writeTo(taskObject, adapter.getOutputClass(), adapter.getOutputClass(), new Annotation[]{}, outputMediaType, jaxrsHeaders, out);
            byte[] output = out.toByteArray(); // this will go in database
            log.debug("Intermediate output: {}", new String(output)); // we can only do this because we know the output is xml format for testing...
            rpc.setOutput(output);
            rpc.setOutputContentType(adapter.getContentType());
            rpc.setOutputContentClass(adapter.getOutputClass().getName());
            */
            rpc.setOutput( xstream.toXML(outputObject).getBytes("UTF-8"));
            // the OUTPUT status indicates the task has completed and output is avaialble
            rpc.setStatus(Rpc.Status.OUTPUT);
            // update the status
            // Task is done.  Now we check the progres -- if the task itself didn't report progress the current/max will be 0/0  , so we change it to 1/1  
            // but if the task did report progress, then it's max will be non-zero ,  and in that case we leave it alone.
            if( rpc.getMax() == null || rpc.getMax().longValue() == 0L ) {
                rpc.setMax(1L);
                rpc.setCurrent(1L);
            }
            repository.store(rpc);
        }
        catch(Exception e) {
            log.error("Cannot write output: {}", e.getMessage());
            rpc.setFaults(new Fault[] { new Fault("Cannot write output") });
            rpc.setStatus(Rpc.Status.ERROR);
            repository.store(rpc);
            throw new WebApplicationException("Cannot write output");
        }
        

        // from this point on the code would be duplicated from Rpcs /{id}/output  but that method simply returns the object and lets jersey serialize it. 
        
        return outputObject;
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
