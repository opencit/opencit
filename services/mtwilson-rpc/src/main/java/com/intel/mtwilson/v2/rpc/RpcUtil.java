/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.v2.rpc;

import com.intel.dcsg.cpg.extensions.Extensions;
import com.intel.mtwilson.launcher.ws.ext.RPC;
import com.intel.mtwilson.rpc.v2.resource.CallableRpcAdapter;
import com.intel.mtwilson.rpc.v2.resource.RpcAdapter;
import com.intel.mtwilson.rpc.v2.resource.RunnableRpcAdapter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.Callable;
import javax.servlet.http.HttpServletRequest;
import org.apache.http.HeaderElement;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicHeaderElement;
import org.apache.http.message.BasicHeaderValueParser;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.message.ParserCursor;
import org.apache.http.util.CharArrayBuffer;
import org.glassfish.jersey.message.MessageBodyWorkers;

/**
 *
 * @author jbuhacoff
 */
public class RpcUtil {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RpcUtil.class);

    /**
     * The Jersey framework injects MessageBodyWorkers into AsyncRpc via 
     * @Context, and AsyncRpc stores it here so that RpcInvoker running
     * in the background can still use it to parse and generate messages
     * from/to the database.
     * One possible issue is that if the web service is restarted, something
     * needs to start RpcInvoker in the background to continue processing
     * RPCs stored in the database... but messageBodyWorkers will be 
     * null until the first RPC request processed by RpcUtil.
     * If this happens, one approach is to send an RPC request to a 
     * non-existing "init_message_body_workers" call, which the AsyncRpc
     * will handle and even though the RPC itself will fail due to not
     * existing, by that time the messagebodyworkers will already be
     * set here and a background RpcInvoker will be able to use it.
     */
//    private static MessageBodyWorkers messageBodyWorkers = null;
    
    /**
     * Will set the static variable messageBodyWorkers only if it has
     * not yet been set. 
     * 
     * @param mbw injected by Jersey via @Context
     */
//    public static void offerMessageBodyWorkers(MessageBodyWorkers mbw) {
//        if( messageBodyWorkers == null ) {
//            messageBodyWorkers = mbw;
//        }
//    }
    /**
     * 
     * @return static variable messageBodyWorkers, or null if it has not been set
     */
//    public static MessageBodyWorkers getMessageBodyWorkers() {
//        return messageBodyWorkers;
//    }
    
    
    
    private static RpcAdapter createAdapter(Object rpcObject) throws InstantiationException, IllegalAccessException {
        Object rpcInstance = rpcObject.getClass().newInstance(); // create a new instance of the RPC object to prevent multi-threaded access to the same instance where client A invokes RPC and sets inputs and then client B invokes RPC and sets inputs and then client A gets the result of client B's inputs
        if( rpcInstance instanceof Callable ) {
            return new CallableRpcAdapter((Callable)rpcInstance);
        }
        if( rpcInstance instanceof Runnable ) {
            return new RunnableRpcAdapter((Runnable)rpcInstance);
        }
        return null;
    }
    
    
    
    public static RpcAdapter findRpcForName(String name) {
        ArrayList<Object> found = new ArrayList<>();
        List<Object> rpcs = Extensions.findAllAnnotated(RPC.class);
        for(Object rpc : rpcs) {
            if( rpc.getClass().isAnnotationPresent(RPC.class) ) {
                RPC rpcAnnotation = rpc.getClass().getAnnotation(RPC.class);
                if( rpcAnnotation.value() == null ) { continue; }
                if( rpcAnnotation.value().equals(name) ) {
                    found.add(rpc);
                }
            }
        }
        if( found.isEmpty() ) {
            return null;
        }
        if( found.size() > 1 ) {
            log.error("Application configuration error: multiple RPC extensions found for {}: {}", name, found);
            return null;
        }
        try {
            RpcAdapter adapter = createAdapter(found.get(0));
            if( adapter == null ) {
                log.error("Cannot find RpcAdapter for {}", name);
                return null;
            }
            return adapter;
        }
        catch(InstantiationException | IllegalAccessException e ) {
            log.error("Cannot instantiate RPC {} class {}: {}", name, found.get(0).getClass().getName(), e.getMessage());
            return null;
        }
    }
    
    public static com.intel.dcsg.cpg.util.MultivaluedHashMap<String,String> convertHeadersToMultivaluedMap(HttpServletRequest request) {
        com.intel.dcsg.cpg.util.MultivaluedHashMap<String,String> map = new com.intel.dcsg.cpg.util.MultivaluedHashMap<String,String>();
        Enumeration<String> names = request.getHeaderNames();
        while(names.hasMoreElements()) {
            String name = names.nextElement();
            Enumeration<String> values = request.getHeaders(name);
            while(values.hasMoreElements()) {
                String value = values.nextElement();
                map.add(name, value);
            }
        }
        return map;
    }
    
    
    
    /**
     * accept can look like this: "application/json;0.9, application/xml;0.8, text/plain";
     */
    public static String getPreferredTypeFromAccept(String accept) {
        if( accept == null || accept.isEmpty() ) { return "*/*"; } // or should we default to json or xml? 
        // this way doesn't work:
        // java.lang.IllegalArgumentException: Error parsing media type 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8'
        // Caused by: java.text.ParseException: Expected separator ';' instead of ','
        /*
        CharArrayBuffer buffer = new CharArrayBuffer(accept.length());
        buffer.append(accept);
        BasicHeaderValueParser parser = new BasicHeaderValueParser();
        HeaderElement[] headerElements = parser.parseElements(buffer, new ParserCursor(0,accept.length()));
        // sample headerElements structure:
        //     [{"name":"application/json","value":null,"parameters":[{"name":"0.9","value":null}],"parameterCount":1},{"name":"application/xml","value":null,"parameters":[{"name":"0.8","value":null}],"parameterCount":1},{"name":"text/plain","value":null,"parameters":[],"parameterCount":0},{"name":" * / * ","value":null,"parameters":[],"parameterCount":0}]
        // sort according to ascending priority  ... so most preferred wil be at the end of the array
        Arrays.sort(headerElements, new AcceptComparator());
        // now grab the last element
        return headerElements[headerElements.length-1].getName();
        */
        String[] contentTypeArray = accept.replace(" ","").split(",");
        BasicHeaderElement[] headerElements = new BasicHeaderElement[contentTypeArray.length];
        for(int i=0; i<contentTypeArray.length; i++) {
            String[] typePreference = contentTypeArray[i].split(";");
            String name = typePreference[0];
            if( typePreference.length > 1 ) {
                BasicNameValuePair[] parameters = new BasicNameValuePair[typePreference.length-1]; // because index 0 is the cnotent type name itself
                for(int j=1; j<typePreference.length; j++) {
                    String[] parameterValue = typePreference[j].split("=");
                    BasicNameValuePair nameValuePair = new BasicNameValuePair(parameterValue[0], parameterValue[1]);  // like q = 0.9
                    parameters[j-1] = nameValuePair;
                }
                headerElements[i] = new BasicHeaderElement(name, null, parameters);
            }
            else {
                headerElements[i] = new BasicHeaderElement(name, null);
            }
        }
        Arrays.sort(headerElements, new AcceptComparator());
        return headerElements[headerElements.length-1].getName();        
    }
    
    public static class AcceptComparator implements Comparator<HeaderElement> {

        @Override
        public int compare(HeaderElement o1, HeaderElement o2) {
            if( o1.getParameterCount() == 0 && o2.getParameterCount() > 0 ) { return -1; }
            if( o1.getParameterCount() > 0 && o2.getParameterCount() == 0 ) { return 1; }
            if( o1.getParameterCount() > 0 && o2.getParameterCount() > 0 ) { 
                NameValuePair nvpair1 = o1.getParameter(0);
                NameValuePair nvpair2 = o2.getParameter(0);
                try {
//                    Float p1 = Float.parseFloat(nvpair1.getName().replace("q=", "")); // the name is the preference like 0.9, 0.8, etc.
//                    Float p2 = Float.parseFloat(nvpair2.getName().replace("q=", ""));
                    Float p1 = Float.parseFloat(nvpair1.getValue()); // the name is the parameter name "q"  as in q=0.9  ; the value is the float like 0.9 or 0.8
                    Float p2 = Float.parseFloat(nvpair2.getValue()); 
                    if( p1 <= p2 ) { return -1; } else { return 1; }
                    //if( p1 > p2 ) { return 1; }
                }
                catch(NumberFormatException e) {
                    log.debug("Failed to parse preference in accept header: {}", e.getMessage());
                    return 0;
                }
                //return 1; 
            }
            return 0;
        }

        
    }
    
}
