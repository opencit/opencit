/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.servlet;

import com.intel.mtwilson.My;
import java.io.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletException;
import org.apache.commons.io.IOUtils;

/**
 * @author jbuhacoff
 */
public class FileServlet extends HttpServlet {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FileServlet.class);
    
    private String directory = null;
    private String prefixTarget = null;
    
    /**
     * Only files inside this directory and sub-directories will be served.
     * The servlet will not access files outside the directory.
     * @param directory 
     */
    public void setDirectory(String directory) {
        this.directory = directory;
    }
    
@Override
public void doGet(HttpServletRequest request, HttpServletResponse response) 
                               throws ServletException, IOException {

    if( directory == null ) {
        setDirectory(My.configuration().getPortalHtml5Dir());
        log.info("Static content directory: {}", directory);
    }
    if( prefixTarget == null ) {
        File prefixFile = new File(directory);
        prefixTarget = prefixFile.getCanonicalPath();
    }
    
    // Get the file to view
    String path = request.getPathInfo();
    if (path == null) { path = ""; }
    
    File file = new File(directory, path);
    
    // prevent client from using .. to get out of our content folder and to arbitrary files
    String target = file.getCanonicalPath();
    if( !target.startsWith(prefixTarget) ) {
        response.setStatus(404);
        return;
    }
    
    // redirect to use trailing slash on directories in order for relative filenames in links to work
    file = new File(target);
    if( file.isDirectory() && !path.endsWith("/") ) {
        String queryString = request.getQueryString() == null ? "" : "?" + request.getQueryString();
        if (queryString.contains("\r\n")) {
            response.setStatus(400);
            return;
        }
        response.sendRedirect(request.getRequestURI() + "/" + queryString);
        return;
    }
    
    // automatic index file for directories; we don't support listing contents so if the index file is not there the client will get a 404 for the directory
    if( file.isDirectory() ) {
        file = new File(target, "index.html"); 
    }
    
    if (!file.exists()) {
        file = new File(target, "index.html5");
    }

    // Get and set the type of the file; relies on the mime types defined in web.xml
    String contentType = getServletContext().getMimeType(file.getName());
    response.setContentType(contentType);
    
    // we set the content length only if it's less than max int, to avoid
    // sending a bogus content length for a huge file (when it matters most!)
    if( file.length() < Integer.MAX_VALUE ) {
        response.setContentLength((int)file.length());
    }

    // read the file and send to the client
    try(FileInputStream in = new FileInputStream(file);
        ServletOutputStream out = response.getOutputStream()) {
        IOUtils.copy(in, out);
    }
    catch (FileNotFoundException e) {
        log.info("File not found: {}", file.getAbsolutePath());
        response.setStatus(404);
    }
    catch (IOException e) { 
        log.error("Cannot retrieve file", e);
        response.setStatus(500);
    }
    catch (IllegalArgumentException iae) {
        log.error("Illegal arguments specified.", iae);
        response.setStatus(500);
    }
  }    
}
