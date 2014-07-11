package com.intel.mtwilson.security.jpa;

import com.intel.mtwilson.as.controller.MwRequestLogJpaController;
import com.intel.mtwilson.as.data.MwRequestLog;
import com.intel.mtwilson.security.core.RequestInfo;
import com.intel.mtwilson.security.core.RequestLog;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManagerFactory;

/**
 * @since 1.2
 * @author jbuhacoff
 */
public class RequestLogBO implements RequestLog {
    private MwRequestLogJpaController controller;
    
    public RequestLogBO(EntityManagerFactory factory) {
        controller = new MwRequestLogJpaController(factory);
    }

    @Override
    public List<RequestInfo> findRequestFromSourceWithMd5HashAfter(String source, byte[] md5_hash, Date after) {
        List<MwRequestLog> requestLog = controller.findBySourceMd5HashReceivedAfter(source, md5_hash, after);
        ArrayList<RequestInfo> list = new ArrayList<RequestInfo>(requestLog.size());
        for(MwRequestLog entry : requestLog) {
            RequestInfo item = new RequestInfo();
            item.instance = entry.getInstance();
            item.received = entry.getReceived();
            item.source = entry.getSource();
            item.md5Hash = entry.getMd5Hash();
            item.content = entry.getContent();
            list.add(item);
        }
        return list;
    }

    @Override
    public void logRequestInfo(RequestInfo request) {
        MwRequestLog entry = new MwRequestLog();
        entry.setInstance(request.instance);
        entry.setReceived(request.received);
        entry.setSource(request.source);
        entry.setMd5Hash(request.md5Hash);
        entry.setContent(request.content);
        controller.create(entry);
    }

}
