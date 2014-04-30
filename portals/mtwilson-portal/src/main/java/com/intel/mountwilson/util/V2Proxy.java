/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mountwilson.util;

import java.io.IOException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;

/**
 * @author jbuhacoff
 */
public class V2Proxy extends HttpServlet {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(V2Proxy.class);

    protected void proxy(HttpServletRequest request, HttpServletResponse response) {
        
        try {
            HttpSession session = request.getSession();
			if( session.getAttribute("logged-in") == null || !(boolean)session.getAttribute("logged-in") ) {
                response.setStatus(401);
                return;
            }
            ProxyApiClient client = (ProxyApiClient) session.getAttribute("api-object");
            client.proxy(request, response);
        } catch (Exception e) {
            log.error("V2Proxy error", e);
            response.setStatus(500);
        }
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        proxy(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        proxy(request, response);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        proxy(request, response);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        proxy(request, response);
    }

    /*
    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        proxy(request, response);
    }

    @Override
    protected void doHead(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        proxy(request, response);
    }

    @Override
    protected void doTrace(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        proxy(request, response);
    }
    */
}
