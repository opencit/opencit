package com.intel.mtwilson.as.business.trust;

import com.intel.mtwilson.as.business.trust.worker.AsyncTrustWorkerLocal;
import com.intel.mountwilson.as.common.ASConfig;
import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.datatypes.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.ejb.EJB;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import org.codehaus.jackson.map.ObjectMapper;

@Stateless(name = "BulkHostsTrustBean")

public class BulkHostsTrustBean {
    /*
     * Instance of async worker for getting trust
     */
    public static int TIME_OUT = 90;
    
    static{
        TIME_OUT = ASConfig.getConfiguration().getInt("com.intel.as.async.timeout.secs", TIME_OUT);
        LoggerFactory.getLogger(BulkHostsTrustBean.class).info("Config com.intel.as.async.timeout.secs = " + TIME_OUT);
    }
    
    @EJB
    AsyncTrustWorkerLocal asyncTrustWorkerLocal;
    Logger log = LoggerFactory.getLogger(getClass().getName());

    public String getTrustSaml(String hosts, boolean forceVerify) {
        long start = System.currentTimeMillis();
        try {
            Map<String, Future<String>> futureResponse = new HashMap<String, Future<String>>();

            Set<String> hostSet = getHostSet(hosts.split(","));
            //log.info("asyncTrustWorkerLocal " + asyncTrustWorkerLocal);
            for (String host : hostSet) {
                log.info("Host - {} Start Processing Parent Thread {} TIME - {} ", new Object[]{host , Thread.currentThread().getName(), System.currentTimeMillis()});
                futureResponse.put(host, asyncTrustWorkerLocal.getTrustSaml(host, forceVerify));
            }
            return buildResponse(futureResponse);
        } finally {
            log.info("Time taken for all hosts - " + (System.currentTimeMillis() - start) + " milliseconds");

        }

    }

    private Set<String> getHostSet(String[] hostList) {

        log.info("Requesting trust for " + hostList.length + " hosts");

        Set<String> hostSet = new HashSet<String>();

        for (String hostString : hostList) {
            if (hostString != null && !hostString.isEmpty()) {
                hostSet.add(hostString);
            }
        }

        return hostSet;
    }

    private String buildResponse(Map<String, Future<String>> futureResponse) {

        StringBuilder response = new StringBuilder();

        response.append("<Hosts>");
        for (String host : futureResponse.keySet()) {

            try {
                log.info("Future object" + futureResponse.get(host));
                response.append((futureResponse.get(host).get(TIME_OUT, TimeUnit.SECONDS)));

            } catch (TimeoutException ex) {
                log.error("Operation timed out", ex);
                response.append(buildErrorResponse(host, ErrorCode.AS_ASYNC_TIMEOUT,TIME_OUT));
            } catch (Exception ex) {
                log.error("Exception in BulkHostsTrustBean", ex);
                response.append(buildErrorResponse(host, ErrorCode.SYSTEM_ERROR, ex.getMessage()));
            }
        }
        response.append("</Hosts>");
        return response.toString();
    }

    private String buildErrorResponse(
            String host,
            ErrorCode errorCode,
            Object... params ) {

        AuthResponse authResponse = new AuthResponse(errorCode, params);    
        return String.format("<Host><Name>%s</Name><ErrorCode>%s</ErrorCode><ErrorMessage>%s</ErrorMessage><Host>", host, errorCode.toString(), authResponse.getErrorMessage());
    }

    public BulkHostTrustResponse getTrustJson(String hosts, Boolean forceVerify) {


        long start = System.currentTimeMillis();
        try {
            Map<String, Future<HostTrust>> futureResponse = new HashMap<String, Future<HostTrust>>();

            Set<String> hostSet = getHostSet(hosts.split(","));
            for (String host : hostSet) {
                log.info("Processing trust for " + host);
                futureResponse.put(host, asyncTrustWorkerLocal.getTrustJson(host, forceVerify));
            }
            return buildJsonResponse(futureResponse);
        } catch (Exception e) {
            throw new ASException(e);
        } finally {
            log.info("Time taken for all hosts - " + (System.currentTimeMillis() - start) / 1000 + " seconds");
        }
    }

    private BulkHostTrustResponse buildJsonResponse(Map<String, Future<HostTrust>> futureResponse) {
        BulkHostTrustResponse response = new BulkHostTrustResponse();
        for (String host : futureResponse.keySet()) {

            try {
                log.info("Future object" + futureResponse.get(host));
                response.getHosts().add(futureResponse.get(host).get(TIME_OUT, TimeUnit.SECONDS));

            }catch (Exception ex) {
                log.error("Ssytem error while processing trust", ex);
                response.getHosts().add(new HostTrust(ErrorCode.SYSTEM_ERROR, ex.getMessage(), host, null, null));
            }
        }
        return response;
    }


}
