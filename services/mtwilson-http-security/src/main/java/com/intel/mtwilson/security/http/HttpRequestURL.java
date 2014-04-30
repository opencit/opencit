/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.security.http;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

/**
 * This component encodes values in www-urlencoded format, using UTF-8 encoding
 * to allow the use of any Unicode character. If UTF-8 encoding is not available
 * on your platform, the class will throw UnsupportedEncodingException.
 * 
 * Parameters are appended to the string in alphabetical order by key and
 * then by value.
 * 
 * @since 0.5.1
 * @author jbuhacoff
 */
public class HttpRequestURL {
    
    private String path = null;
    private String query = null;
    
    public HttpRequestURL() {
        path = "";
        query = "";
    }
    
    public HttpRequestURL(String path, String query) {
        this.path = path;
        this.query = query;
    }
    
    /**
     * using Map<String,Object> because due to generics type erasure it's not possible to have 
     * separate constructors for Map<String,String> and Map<String,String[]> and Map<String,List<String>>
     **/
    public HttpRequestURL(String path, Map<String,Object> query) throws UnsupportedEncodingException {
        this.path = path;
        this.query = queryString(query);
    }
    
    @Override
    public String toString() {
        return path + ( query.isEmpty() ? "" : "?"+query );
    }
    
    /**
     * The query parameters can be String or String[] for multi-valued parameters.
     * They will be sorted by key in alphabetical order, then by value in alphabetical order.
     * 
     * If the query object is null then an empty query string will be returned.
     * 
     * @param query
     * @return query string like name=John&age=30
     */
    private String queryString(Map<String,Object> query) throws UnsupportedEncodingException {
        if( query == null ) { return ""; }
        List<String> keyList = new ArrayList<>(query.keySet());
        Collections.sort(keyList);
        List<String> queryParam = new ArrayList<>();
        for(String key : keyList) {
            Object value = query.get(key);
            if( value == null ) {
                queryParam.add(singleValue(key, ""));
            }
            else if( value instanceof String ) {
                queryParam.add(singleValue(key, (String)value)); // String.format("%s=%s", urlencode(key), urlencode((String)value)));
            }
            else if( value instanceof String[] ) {
                queryParam.addAll(multiValue(key, (String[])value));
            }
            else if( value instanceof List ) {
                queryParam.addAll(multiValue(key, (List<String>)value));
            }
            else {
                queryParam.add(singleValue(key, value.toString()));
            }
        }
        return join(queryParam, "&");
    }
    
    private String singleValue(String key, String value) throws UnsupportedEncodingException {
        return String.format("%s=%s", urlencode(key), urlencode(value));
    }

    private List<String> multiValue(String key, String[] value) throws UnsupportedEncodingException {
        List<String> paramList = new ArrayList<>();
        List<String> list = Arrays.asList((String[])value);
        Collections.sort(list);
        for(String v : list) {
            paramList.add(String.format("%s=%s", urlencode(key), urlencode((String)v)));
        }
        return paramList;
    }

    private List<String> multiValue(String key, List<String> valueList) throws UnsupportedEncodingException {
        List<String> paramList = new ArrayList<>();
        Collections.sort(valueList);
        for(String v : valueList) {
            paramList.add(String.format("%s=%s", urlencode(key), urlencode((String)v)));
        }
        return paramList;
    }
    
    /**
     * Encodes a URL parameter name or value with the following rules:
     * 
     * The alphanumeric characters a-z, A-Z, and 0-9 remain the same
     * The special characters dot, hyphen, star, and underscore remain the same
     * The space character is converted to %20  (not a plus sign)
     * All other characters are unsafe and are converted to a sequence of
     * bytes using UTF-8 and then each byte is encoded using the %xy hexadecimal
     * representation.
     * 
     * The output of this function is SIMILAR to URLEncoder but differs in
     * the handling of spaces. This function emits %20 for a space whereas
     * URLEncoder emits a plus sign for a space.
     * 
     * If the value is null, an empty string will be returned.
     * 
     * @param value
     * @return
     * @throws UnsupportedEncodingException 
     */
    private String urlencode(String value) throws UnsupportedEncodingException {
        if( value == null ) { return ""; }
        return URLEncoder.encode(value, "UTF-8").replace("+", "%20"); // replace + with %20 to be compatible with PHP's rawurlencode
    }


    /**
        * Joins the elements returned by any iterator using the given separator. If the iterator
        * is of String then the toString method is called on the elements.
        * If the iterator has no elements, an empty string is returned.
        * @param <T>
        * @param it
        * @param separator
        * @return a String that contains the iterator elements joined with the separator
        */
    private static String join(Collection<String> collection, final String separator) {
            Iterator<String> it = collection.iterator();
            if( it.hasNext() ) {
                    StringBuilder buffer = new StringBuilder();
                    buffer.append(it.next());			
                    while(it.hasNext()) {
                            buffer.append(separator).append(it.next());
                    }
                    return buffer.toString();		
            }
            else {
                    return "";
            }
    }
    
}
