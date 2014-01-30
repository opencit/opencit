/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jersey.resource;

import com.intel.mtwilson.jersey.http.OtherMediaType;
import com.intel.mtwilson.jersey.http.PATCH;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.validation.ValidationUtil;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import com.intel.mtwilson.jersey.Document;
import com.intel.mtwilson.jersey.DocumentCollection;
import com.intel.mtwilson.jersey.FilterCriteria;
import com.intel.mtwilson.jersey.Patch;
import com.intel.mtwilson.jersey.PatchLink;
import java.util.List;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.BeanParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * TENTATIVE -  there are some problems with this draft implementation, because
 * it is not able to fulfill all the documented requirements for the RPC feature.
 * 
 * 
 * Characteristics of a Remote Procedure Call is that the input and output
 * are likely different types, that the client and server may negotiate for
 * the processing to occur immediately (synchronous) or to be queued
 * (asynchronous), that an RPC consists of a single method that is a 
 * verb phrase, and that the server is not expected to keep track of
 * past inputs (but it is allowed to store them and index them as well as
 * the outputs, of course) - if any storing or indexing of RPCs takes places,
 * the interface to that stored data would be a resource.
 * 
 * In contrast, a resource has one type and typically has several standard
 * operations are defined
 * on that type: create, store (update), retrieve, delete, and search
 * which is defined on the collection of that type; a resource operation
 * typically happens immediately (but the server may still choose to 
 * delay it and return an appropriate "accepted" http status code). 
 * 
 * Reference: https://jersey.java.net/documentation/latest/user-guide.html
 * https://jersey.java.net/documentation/latest/media.html
 * https://jersey.java.net/documentation/latest/deployment.html
 * https://wikis.oracle.com/display/Jersey/Overview+of+JAX-RS+1.0+Features
 * http://docs.oracle.com/cd/E19776-01/820-4867/6nga7f5o5/index.html
 *
 * When listing the media types in
 * @Produces annotations, it's important to put application/json before
 * application/vnd.api+json so it will be chosen as the default if the browser
 * doesn't specify an Accept header. If application/json is first, accessing
 * /hosts will display the JSON output in the browser. If
 * application/vnd.api+json is first, the browser will not recognize that
 * content type (even though it has +json) and will download it as a file. The
 * .json file extension maps to application/json specifically. JSON API clients
 * must set the Accept header if they want to receive the output with
 * Content-Type: application/vnd.api+json
 *
 *
 * Example simple JSON output:
 *
 *
 * {"hosts":[{"id":"06285da4-e170-4322-a843-480f3a55feec","name":"hostabc","connection_url":"http://1.2.3.4","description":"test
 * host","bios_mle":"bios-4.3.2"}]}
 *
 * Example XML output:
 * * 
<host_collection><hosts><host><id>bd7094d2-2ed3-468e-9c16-40999f9e4b8c</id><name>hostabc</name><connectionUrl>http://1.2.3.4</connectionUrl><description>test
 * host</description><biosMLE>bios-4.3.2</biosMLE></host></hosts></host_collection>
 *
 * This abstract class defines the following HTTP interface:
 *
 * GET /collection -> application/vnd.api+json, application/json,
 * application/xml, application/yaml, text/yaml
 *
 * POST /collection Content-Type: application/json, application/xml,
 * application/yaml, text/yaml -> application/json, application/xml,
 * application/yaml, text/yaml
 *
 *
 *
 * @author jbuhacoff
 */
//@Stateless
//@Path("/hosts")
public abstract class AbstractRpc<T,U> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractRpc.class);

    /**
     * Subclasses must override execute as the implementation of the 
     * remote procedure call.
     *
     * @param criteria
     * @return
     */
    protected abstract U execute(T input);


    /**
     * This wrapper method validates the input before calling the 
     * subclassed execute method.
     *
     * @param item
     * @return
     */
    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, OtherMediaType.APPLICATION_YAML, OtherMediaType.TEXT_YAML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, OtherMediaType.APPLICATION_YAML, OtherMediaType.TEXT_YAML})
    public U remoteProcedureCall(T input) {
        log.debug("remoteProcedureCall");
        ValidationUtil.validate(input); // throw new MWException(e, ErrorCode.AS_INPUT_VALIDATION_ERROR, input, method.getName());
        U output = execute(input);
        return output;
    }

}
