/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.audit.helper;

import com.intel.mtwilson.audit.data.AuditContext;
//import com.sun.jersey.spi.container.ContainerRequest;
//import com.sun.jersey.spi.container.ContainerResponse;
//import com.sun.jersey.spi.container.ContainerResponseFilter;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dsmagadx
 */
public class AuditJerseyResponseFilter implements ContainerResponseFilter {
    private static Logger log = LoggerFactory.getLogger(AuditJerseyResponseFilter.class);
    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) {

        long endTime = System.currentTimeMillis();
        AuditContext auditContext = MtWilsonThreadLocal.get();
        if( auditContext != null ) {
        log.debug("AuditJerseyResponseFilter request for {} {}  Transaction Id {} End {} Time {} ", new String[] 
                    { request.getMethod(), request.getUriInfo().getPath(), auditContext.getTransactionUuid(), String.valueOf(endTime),String.valueOf(endTime -auditContext.getStartMilliseconds()) });
        }        
        // Remove the context from thread local
        
        MtWilsonThreadLocal.unset();
        log.info("Removed the Audit context from thread local.");
//        return response;
    }
    
    
}
