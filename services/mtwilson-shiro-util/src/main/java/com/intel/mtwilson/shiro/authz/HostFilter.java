/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.authz;

import java.util.HashSet;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.apache.shiro.web.filter.authz.AuthorizationFilter;
import com.intel.dcsg.cpg.net.InternetAddress;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.util.StringUtils;
import org.apache.shiro.web.util.WebUtils;

/**
 *
 * @author jbuhacoff
 */
public class HostFilter extends AuthorizationFilter {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HostFilter.class);
    private HashSet<String> allowIp4 = new HashSet<>();
    private HashSet<String> allowHost = new HashSet<>();
    private String allowInput = null;

    /**
     *
     * @param allow comma-separated list of hostnames or ip addresses; subnets
     * and wildcards not yet supported
     */
    public void setAllow(String allow) {
        this.allowInput = allow;
        String[] list = allow.replace(" ", "").split(",");
        for (String item : list) {
            log.debug("Allow from {}", item);
            InternetAddress address = new InternetAddress(item);
            if (address.isHostname()) {
                allowHost.add(address.toString());
            } else if (address.isIPv4()) {
                allowIp4.add(address.toString());
            } else {
                log.error("Invalid address '{}'", item);
                throw new IllegalArgumentException("Address must be hostname or IPv4");
            }
        }
    }

    public String getAllow() {
        return allowInput;
    }

    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) throws Exception {
        log.debug("Remote addr = {}", request.getRemoteAddr());
        log.debug("Remote host = {}", request.getRemoteHost());

        InternetAddress remoteAddr = new InternetAddress(request.getRemoteAddr());
        if (remoteAddr.isIPv4() && allowIp4.contains(remoteAddr.toString())) {
            return true;
        }
        if (remoteAddr.isHostname() && allowHost.contains(remoteAddr.toString())) {
            return true;
        }
        InternetAddress remoteHost = new InternetAddress(request.getRemoteHost());
        if (remoteHost.isIPv4() && allowIp4.contains(remoteHost.toString())) {
            return true;
        }
        if (remoteHost.isHostname() && allowHost.contains(remoteHost.toString())) {
            return true;
        }
        return false;
    }

    /**
     * Because a client accessing the service from a denied address has no
     * possibility of logging in, the onAccessDenied behavior is modified to
     * either return an error or redirect to the unauthorizedUrl (which would
     * need to have the anon filter set to allow anyone to access it)
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws IOException {
        if (StringUtils.hasText(getUnauthorizedUrl())) {
            WebUtils.issueRedirect(request, response, getUnauthorizedUrl());
        } else {
            log.info("Access denied to unauthorized client {}", request.getRemoteAddr());
            WebUtils.toHttp(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);

        }
        return false;
    }
}
