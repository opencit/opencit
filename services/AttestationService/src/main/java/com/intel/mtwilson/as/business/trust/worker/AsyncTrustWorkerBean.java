/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.business.trust.worker;

import java.util.concurrent.Future;

import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.mtwilson.as.business.trust.HostTrustBO;
import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.as.helper.ASComponentFactory;
import com.intel.mtwilson.datatypes.AuthResponse;
import com.intel.mtwilson.datatypes.ErrorCode;
import com.intel.mtwilson.datatypes.HostTrust;
import org.codehaus.jackson.map.ObjectMapper;

/**
 *
 * @author dsmagadx
 */
@Stateless(name = "AsyncTrustWorkerBean")
public class AsyncTrustWorkerBean implements AsyncTrustWorkerLocal {

    Logger log = LoggerFactory.getLogger(getClass().getName());


    @Override
    @Asynchronous
    public Future<String> getTrustSaml(String host, boolean forceVerify) {
        log.info("Host - {}  Thread id {} TIME - {} ", new Object[]{host , Thread.currentThread().getName(), System.currentTimeMillis()});
        long start = System.currentTimeMillis();
        try {
            return buildSuccessResponse(host, new ASComponentFactory().getHostTrustBO().getTrustWithSaml(host, forceVerify));
        } catch (ASException ae) {
            log.error("Error while getting trust saml assertion for " + host, ae);
            return buildErrorResponse(host, ae.getErrorCode(), ae.getErrorMessage());
        } catch (Exception e) {
            log.error("Error while getting trust saml assertion for " + host, e);
            return buildErrorResponse(host, ErrorCode.SYSTEM_ERROR, e.getMessage());
        } finally {
            log.info("Done processing - " + host + " in " + (System.currentTimeMillis() - start ) + " millisecondss");
        }
    }

    private Future<String> buildSuccessResponse(String host, String trustWithSaml) {
        return new AsyncResult<String>(String.format("<Host><Name>%s</Name><ErrorCode>%s</ErrorCode><Assertion>%s</Assertion></Host>", host, ErrorCode.OK.toString(), addCDATA(trustWithSaml)));
    }

    private Future<String> buildErrorResponse(
            String host,
            ErrorCode errorCode,
            String errorMessage) {

        AuthResponse authResponse = new AuthResponse(errorCode, errorMessage);
        // Bug: 439 - Modified to use the errorMessage directly instead of AuthResponse's error message 
        return new AsyncResult<String>(String.format("<Host><Name>%s</Name><ErrorCode>%s</ErrorCode><ErrorMessage>%s</ErrorMessage></Host>", host, errorCode.toString(), errorMessage));

    }
    
    private String addCDATA(String xml){
        return String.format("<![CDATA[%s]]>", xml);
    }

    @Override
    @Asynchronous
    public Future<HostTrust> getTrustJson(String host, boolean forceVerify) {
        log.info("Container thread id " + Thread.currentThread().getName());
        long start = System.currentTimeMillis();
        try{
            return new AsyncResult<HostTrust>(new ASComponentFactory().getHostTrustBO().getTrustWithCache(host, forceVerify));
        }catch(ASException ae){
            log.error("Error while getting trust",ae);
            return new AsyncResult<HostTrust>(new HostTrust(ae.getErrorCode(),ae.getMessage(),host,null,null));
        }catch(Exception e){
            log.error("Error while getting trust",e);
            return new AsyncResult<HostTrust>(new HostTrust(ErrorCode.SYSTEM_ERROR,e.getMessage(),host,null,null));
        }finally{
            log.info("Done processing - " + host + " in " + (System.currentTimeMillis() - start )/1000 + " secs");
        }
        
    }


    
}
