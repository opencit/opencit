/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.v2.rpc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.rpc.v2.model.RpcLocator;
import com.intel.mtwilson.rpc.v2.resource.RpcAdapter;
import com.intel.mtwilson.rpc.v2.resource.RpcRepository;
import java.util.concurrent.ConcurrentLinkedQueue;
import com.intel.dcsg.cpg.validation.Fault;
import com.intel.mtwilson.launcher.ext.annotations.Background;
import com.intel.mtwilson.rpc.v2.model.Rpc;
import com.intel.mtwilson.rpc.v2.model.RpcPriv;
import com.intel.mtwilson.rpc.v2.model.RpcCollection;
import com.intel.mtwilson.rpc.v2.model.RpcFilterCriteria;
import com.thoughtworks.xstream.XStream;
import java.nio.charset.Charset;
import java.util.List;
/**
 *
 * @author jbuhacoff
 */
@Background
public class RpcInvoker implements Runnable {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RpcInvoker.class);

//    private static final RpcInvoker instance = new RpcInvoker();
//    public static RpcInvoker getInstance() { return instance; }
    
    private RpcRepository repository = new RpcRepository();
    private ConcurrentLinkedQueue<UUID> queue = new ConcurrentLinkedQueue<>(); // of rpc request uuid's to run
    private ObjectMapper mapper = new ObjectMapper(); 
    
//    public void setRepository(RpcRepository repository) { this.repository = repository; }
    
    public void add(UUID id) { queue.offer(id); }
    public void remove(UUID id) { queue.remove(id); }
    
    // gets next uuid from queue and invokes it 
    // if you want continous processing of queue, use start() and stop() 
    // instead.
    @Override
    public void run() {
        UUID id = queue.poll();
        if( id == null ) {
            // the queue doesn't have any requests - check the database, add any QUEUE'd tasks to the queue 
            RpcFilterCriteria criteria = new RpcFilterCriteria();
            criteria.status = Rpc.Status.QUEUE;
            RpcCollection results = repository.search(criteria); // the result is "Rpc" class but it will not have inputs or outputs set because the "search" method does not retrieve those from database -- which is fine, we onlyw ant the id's anyway
            List<Rpc> items = results.getDocuments();
            log.debug("Found {} tasks marked QUEUE in database", items.size());
            for(Rpc item : items) {
                queue.offer(item.getId());
            }
            id = queue.poll();
            if( id == null ) {
                log.debug("No tasks in queue and no saved QUEUE tasks in database");
                return;
            }
        }
        
        
        RpcLocator locator = new RpcLocator();
        locator.id = id;
        RpcPriv rpc = repository.retrieveInput(locator); // retrieve(locator) would only return the status info ;  so we have an additional retrieveInput method to also return the input
        
        if (rpc == null) {
            log.error("Cannot retrieve rpc input.");
            return;
        }
        
        // make sure we have an extension to handle this rpc
        RpcAdapter adapter = RpcUtil.findRpcForName(rpc.getName());
        if( adapter == null ) {
            log.error("Cannot find RPC extension for {}", rpc.getName());
            rpc.setFaults(new Fault[] { new Fault("Unsupported operation") });
            rpc.setStatus(Rpc.Status.ERROR);
            repository.store(rpc);
            return;
        }
        
        XStream xs = new XStream();
       
        
        Object taskObject;
        // parse the request body and deserialize to automaticaly set the task inputs
        try {
            taskObject = xs.fromXML(new String(rpc.getInput(), Charset.forName("UTF-8")));
            log.debug("Input object: {}", mapper.writeValueAsString(taskObject));
        }
        catch(Exception e) {
            log.error("Cannot read input: {}", e.getMessage());
            rpc.setFaults(new Fault[] { new Fault("Cannot read input") });
            rpc.setStatus(Rpc.Status.ERROR);
            repository.store(rpc);
            return;
        }
        // run
        try {
        // assume that the rpc adapter is RunnableRpcAdapter   
//            Runnable runnable = (Runnable)taskObject;
//            runnable.run();
            adapter.setInput(taskObject);
            adapter.invoke();            
//            log.debug("After run: {}", mapper.writeValueAsString(taskObject));
            log.debug("After run: {}", mapper.writeValueAsString(adapter.getOutput()));
        }
        catch(Exception e) {
            log.error("Error while executing RPC {}", rpc.getName(), e);
            rpc.setFaults(new Fault[] { new Fault("Execution failed") });
            rpc.setStatus(Rpc.Status.ERROR);
            repository.store(rpc);
            return;
        }

        // format output
        try {
            /*
            javax.ws.rs.core.MultivaluedHashMap jaxrsHeaders = new javax.ws.rs.core.MultivaluedHashMap();
            jaxrsHeaders.putAll(headerMap.getMap());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            messageBodyWriter.writeTo(taskObject, adapter.getOutputClass(), adapter.getOutputClass(), new Annotation[]{}, outputMediaType, jaxrsHeaders, out);
            byte[] output = out.toByteArray(); // this will go in database
            log.debug("Intermediate output: {}", new String(output)); // we can only do this because we know the output is xml format for testing...
            rpc.setOutput(output);
            rpc.setOutputContentType(adapter.getContentType());
            rpc.setOutputContentClass(adapter.getOutputClass().getName());
            */
//            rpc.setOutput( xs.toXML(taskObject).getBytes("UTF-8"));
            rpc.setOutput(xs.toXML(adapter.getOutput()).getBytes("UTF-8"));
            // the OUTPUT status indicates the task has completed and output is avaialble
            rpc.setStatus(Rpc.Status.OUTPUT);
        }
        catch(Exception e) {
            log.error("Cannot write output: {}", e.getMessage());
            rpc.setFaults(new Fault[] { new Fault("Cannot write output") });
            rpc.setStatus(Rpc.Status.ERROR);
            repository.store(rpc);
            return;
        }

        // Task is done.  Now we check the progres -- if the task itself didn't report progress the current/max will be 0/0  , so we change it to 1/1  
        // but if the task did report progress, then it's max will be non-zero ,  and in that case we leave it alone.
        if( rpc.getMax() == null || rpc.getMax().longValue() == 0L ) {
            rpc.setMax(1L);
            rpc.setCurrent(1L);
        }
        
        repository.store(rpc);
        log.debug("RPC processing complete, output stored, status updated to OUTPUT");
                
    }
    
}
