/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.ms.business;

import com.intel.mtwilson.i18n.ErrorCode;
import com.intel.mtwilson.datatypes.*;
import com.intel.mtwilson.ms.common.MSConfig;
import com.intel.mtwilson.ms.common.MSException;
import com.intel.mtwilson.ms.MSComponentFactory;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author rksavinx
 */
public class BulkHostRegBO {
    private Logger log = LoggerFactory.getLogger(getClass());
    private HostBO hostBO = MSComponentFactory.getHostBO();
    private int timeout;
    private static ExecutorService scheduler = Executors.newFixedThreadPool(MSConfig.getConfiguration().getInt("mtwilson.ms.bulkmgmt.threads.max", 32)); //  bug #503 move thread pool to static so multiple requests do not overload it; 
    
    public BulkHostRegBO() {
        timeout = MSConfig.getConfiguration().getInt("mtwilson.ms.registration.hostTimeout", 600);
    }

    public BulkHostRegBO(int timeout) {
        this.timeout = timeout;
    }
    
    /**
     * This function handles multithread calls for the register hosts functionality.
     * @param hostRecords - List of the hosts that need to be added/updated.
     * @return 
     */
    public HostConfigResponseList registerHosts(TxtHostRecordList hostRecords) {
        HostConfigResponseList hostResponses = new HostConfigResponseList();
        ArrayList<Future<?>> taskStatus = new ArrayList<Future<?>>();
        Set<HostMgmt> tasks = new HashSet<HostMgmt>();

        try {
            
            for (TxtHostRecord hostRecord : hostRecords.getHostRecords()) {
                HostMgmt task = new HostMgmt(hostBO, hostRecord);
                tasks.add(task);
                Future<?> status = scheduler.submit(task);
                taskStatus.add(status);
            }

            // The purpose of this below loop is to ensure that all the threads have completed processing.
            // If in case we catch an exception from any one of them, we will log the same and continue. Since
            // we submitted the Runnable tasks we won't get back the status in this loop.
            for (Future<?> status : taskStatus) {
                try {
                    status.get(timeout, TimeUnit.SECONDS);
                } catch (Exception ex) {
                    log.error("Exception while retrieving the status of the task. {}.", ex.getMessage());
                }            
            }

            // Retrieve the status from all the threads and return back to the user.
            for (HostMgmt task : tasks) {
                if (task.getResult() == null) {
                    hostResponses.getHostRecords().add(task.getTimeoutResult());
                } else {
                    hostResponses.getHostRecords().add(task.getResult());
                }
            }

            return hostResponses;
        } catch (Exception ex) {
            // throw new MSException(ex);
            log.error("Error during bulk host registration. ", ex);
            throw new MSException(ErrorCode.MS_BULK_REGISTRATION_ERROR, ex.getClass().getSimpleName());
            
        }
    }

    private class HostMgmt implements Runnable {

        private HostBO dao;
        private TxtHostRecord hostObj;
        private HostResponse result;
        private String hostName;
        private boolean isError = false;

        public HostMgmt(HostBO dao, TxtHostRecord hostObj) {
            this.dao = dao;
            this.hostObj = hostObj;
            this.hostName = hostObj.HostName;
        }

        @Override
        public void run() {
            if (isError()) {
                return;
            }
            try {
                boolean success = dao.registerHost(hostObj);
                
                if (success)
                    result = new HostResponse(ErrorCode.OK);
                else
                    result = new HostResponse(ErrorCode.UNKNOWN_ERROR);
            } catch (MSException e) {
                isError = true;
                result = new HostResponse();
                result.setErrorCode(e.getErrorCode().toString());
                result.setErrorMessage(e.getErrorMessage());
            } catch (Exception e) {
                isError = true;
                log.error("Error during bulk host registration. ", e);
                result = new HostResponse(ErrorCode.MS_BULK_REGISTRATION_ERROR, e.getClass().getSimpleName());
            }
        }

        public boolean isError() {
            return isError;
        }

        public HostConfigResponse getResult() {
            HostConfigResponse hostResponse = new HostConfigResponse();
            hostResponse.setHostName(hostName);
            hostResponse.setErrorCode(result.getErrorCodeEnum());
            if (result.getErrorCodeEnum() == ErrorCode.OK) {
                hostResponse.setStatus(Boolean.toString(true));
                hostResponse.setErrorMessage("");
            } else {
                hostResponse.setStatus(Boolean.toString(false));
                hostResponse.setErrorMessage(result.getErrorMessage());
            }
            return hostResponse;
        }

        public HostConfigResponse getTimeoutResult() {
            HostConfigResponse hostResponse = new HostConfigResponse();
            hostResponse.setHostName(hostName);
            hostResponse.setErrorCode(ErrorCode.AS_ASYNC_TIMEOUT);
            hostResponse.setStatus(Boolean.toString(false));
            hostResponse.setErrorMessage("Exceeded timeout of " + timeout + " seconds");
            return hostResponse;
        }
    }
}
