/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mountwilson.http.security.adapter;

import com.intel.mtwilson.security.http.HmacAuthorization;
import com.intel.dcsg.cpg.crypto.HmacCredential;
import com.jayway.restassured.filter.Filter;
import com.jayway.restassured.filter.FilterContext;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.FilterableRequestSpecification;
import com.jayway.restassured.specification.FilterableResponseSpecification;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Requires the restassured java library and the groovyx.net.http library (can
 * be obtained from the codehaus http-builder project)
 *
 * However, this isn't working right now because it seems that obtaining the
 * Http request method is a little sketchy... javadoc shows a
 * "getRequestMethod()" on FilterContext but it's not available here
 *
 * The restassured dependency is marked optional; you must add it specifically
 * to your project when using this class.
 * 
 * @author jbuhacoff
 */
public class MtWilsonRestAssuredFilter implements Filter {

    private String username;
    private String password;

    public MtWilsonRestAssuredFilter(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
        try {
            HmacAuthorization requestHelper = new HmacAuthorization(new HmacCredential(username, password));
            String authorizationHeader = requestHelper.getAuthorization(ctx.getRequestMethod().toString(), ctx.getRequestPath(), convert(requestSpec.getRequestParams()), requestSpec.<String>getBody()); // the null should be HTTP REQUEST METHOD ctx.getRequestMethod().toString()
            System.err.println("MtWilsonRestAssuredFilter created Authorization: " + authorizationHeader);
            if (authorizationHeader != null) {
                requestSpec.header("Authorization", authorizationHeader);
            }
        } catch (Exception e) {
            System.err.println("Cannot create request signature: " + e.toString());
            e.printStackTrace();
        }
        return ctx.next(requestSpec, responseSpec);
    }

    /**
     * Converts a Map<String,?> to a Map<String,Object> where Object is either
     * String or String[]
     * 
     * If the input map is null, then null will be returned.
     *
     * @param params
     * @return Map<String,List<String>> or null if the input was null
     */
    private Map<String, Object> convert(Map<String, ?> params) {
        if( params == null ) { return null; }
        Map<String, Object> map = new HashMap<String, Object>();
        Set<String> keys = params.keySet();
        for (String key : keys) {
            Object value = params.get(key);
            if( value == null ) {
                map.put(key, "");
            } else if (value instanceof String) {
                map.put(key, (String)value);
            } else if (value instanceof String[]) {
                map.put(key, Arrays.asList((String[])value));
            } else {
                System.err.println("MtWilsonRestAssuredFilter param: Unknown data type for " + key + ": " + value.getClass().getName());
                map.put(key, value.toString());
            }
        }
        return map;
    }
    /*
     * private void showParameters(FilterableRequestSpecification requestSpec) {
     * System.err.println("MtWilsonRestAssuredFilter query params:");
     * dumpMap(requestSpec.getQueryParams());
     * System.err.println("MtWilsonRestAssuredFilter request params:");
     * dumpMap(requestSpec.getRequestParams());
     * System.err.println("MtWilsonRestAssuredFilter form params:");
     * dumpMap(requestSpec.getFormParams());
     * System.err.println("MtWilsonRestAssuredFilter path params:");
     * dumpMap(requestSpec.getPathParams()); }
     *
     * private void dumpMap(Map<String,?> map) { Set<String> pkeys =
     * map.keySet(); for(String key : pkeys) { Object value = map.get(key); if(
     * value instanceof String ) { System.err.println("
     * MtWilsonRestAssuredFilter param: "+key+" = "+(String)value); } else if(
     * value instanceof String[] ) { System.err.println("
     * MtWilsonRestAssuredFilter param: "+key+" = "+StringUtils.join((String
     * [])value)); } else { System.err.println(" MtWilsonRestAssuredFilter
     * param: Unknown data type for "+key+": "+value.getClass().getName()); } }
     *
     * }
     *
     */
}
