/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.business;

import com.intel.mountwilson.as.common.ASConfig;
import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.as.helper.ASComponentFactory;
import com.intel.mtwilson.datatypes.ErrorCode;
import com.intel.mtwilson.datatypes.HostConfigResponse;
import com.intel.mtwilson.datatypes.HostConfigResponseList;
import com.intel.mtwilson.datatypes.HostResponse;
import com.intel.mtwilson.datatypes.TxtHost;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import com.intel.mtwilson.datatypes.TxtHostRecordList;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
public class BulkHostMgmtBO {

    private Logger log = LoggerFactory.getLogger(getClass());
    private HostBO hostBO = new ASComponentFactory().getHostBO();
    //TODO: Do we need to add this time out setting also in the configuration file?
    private int timeout;
    //TODO: Add a new setting for host mgmt threads name:bulkmgmt
    private static ExecutorService scheduler = Executors.newFixedThreadPool(ASConfig.getConfiguration().getInt("mtwilson.bulkmgmt.threads.max", 32)); //  bug #503 move thread pool to static so multiple requests do not overload it; TODO do we need to provide a web application listener that calls shutdown() on this pool?

    public BulkHostMgmtBO() {
        //TODO: Do we need to add this time out setting also in the configuration file?
        timeout = 600;
    }

    public BulkHostMgmtBO(int timeout) {
        this.timeout = timeout;
    }

    public HostConfigResponseList addHosts(TxtHostRecordList hostRecords) {
        // Set the updateHost flag to false since we are adding the hosts;
        return addUpdateHosts(hostRecords, false);
    }
    
    public HostConfigResponseList updateHosts(TxtHostRecordList hostRecords) {
        // Set the updateHost flag to true since we are adding the hosts;
        return addUpdateHosts(hostRecords, true);
    }
    
    /**
     * This function handles multithread calls for both add and update host functionalities.
     * @param hostRecords - List of the hosts that need to be added/updated
     * @param updateHost - flag indicating whether the hosts should be added or updated.
     * @return 
     */
    private HostConfigResponseList addUpdateHosts(TxtHostRecordList hostRecords, boolean updateHost) {
        HostConfigResponseList hostResponses = new HostConfigResponseList();
        ArrayList<Future<?>> taskStatus = new ArrayList<Future<?>>();
        Set<HostMgmt> tasks = new HashSet<HostMgmt>();

        try {
            
            for (TxtHostRecord hostRecord : hostRecords.getHostRecords()) {
                HostMgmt task = new HostMgmt(hostBO, hostRecord, updateHost);
                tasks.add(task);
                Future<?> status = scheduler.submit(task);
                taskStatus.add(status);
            }

            // The purpose of this below loop is to ensure that all the threads have completed processing.
            // If in case we can an exception from any one of them, we will log the same and continue. Since
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
            throw new ASException(ex);
        }
    }

    private class HostMgmt implements Runnable {

        private HostBO dao;
        private TxtHostRecord hostObj;
        private HostResponse result;
        private String hostName;
        private boolean isError = false;
        private boolean updateHost = false;

        public HostMgmt(HostBO dao, TxtHostRecord hostObj, boolean updateHost) {
            this.dao = dao;
            this.hostObj = hostObj;
            this.hostName = hostObj.HostName;
            this.updateHost = updateHost;
        }

        @Override
        public void run() {
            if (isError()) {
                return;
            }
            try {
                if (!updateHost)
                    result = dao.addHost(new TxtHost(hostObj));
                else
                    result = dao.updateHost(new TxtHost(hostObj));
            } catch (ASException e) {
                isError = true;
                result = new HostResponse(e.getErrorCode(), e.getErrorMessage());
            } catch (Exception e) {
                isError = true;
                result = new HostResponse(ErrorCode.UNKNOWN_ERROR, e.getLocalizedMessage());
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
