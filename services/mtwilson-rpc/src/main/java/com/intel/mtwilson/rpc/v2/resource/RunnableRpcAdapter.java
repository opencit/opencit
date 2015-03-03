/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.rpc.v2.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.intel.dcsg.cpg.validation.Fault;
import com.intel.mtwilson.patch.PatchException;
import com.intel.mtwilson.patch.PatchUtil;
import com.intel.mtwilson.rpc.faults.InvalidRpcInput;
import com.intel.mtwilson.rpc.faults.RpcFailed;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Given an @RPC instance that implements Runnable, this class organizes
 * how that instance will be handled for input and output, 
 * how the intermediate output will
 * be stored, etc. 
 * 
 * A runnable instance itself is the input and output type - its setters and
 * getters are used as the available attributes. This means a Runnable cannot
 * limit which attributes are "in" and which are "out" , but of course whatever
 * attributes are "out" attributes can be set by the runnable so if the 
 * client tried to set them they would be ignored.  IT also means there cannot
 * be private inputs because inputs will be displayed to the user together
 * with the outputs. For example:
 * 
 * Input:   <add_integers><x>1</x><y>1</y></add_integers>
 * 
 * Output: <add_integers><x>1</x><y>1</y><result>2</result></add_integers>
 * 
 * @author jbuhacoff
 */
public class RunnableRpcAdapter implements RpcAdapter<Object,Object> {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RunnableRpcAdapter.class);
    private ObjectMapper mapper;
    private Runnable rpcInstance;
    private ArrayList<Fault> faults = new ArrayList<Fault>();
    public RunnableRpcAdapter(Runnable rpcInstance) {
        this.rpcInstance = rpcInstance;
        mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
    }
    // these declare how we want to interpret the client's request body
    // and how we want to store the intermediate output
    @Override
    public Class getInputClass() {
        return rpcInstance.getClass();
    }
    @Override
    public Class/*<T>*/ getOutputClass() {
        return rpcInstance.getClass();
    }
    // this method will receive the input object which was deserialized from
    // the client's request body - and is the same type we returned in 
    // getInputClass
    @Override
    public void setInput(Object/*T*/ input) {
        // use beanutils to copy the properties we need
        try {
//            Map<String,Object> diff = PatchUtil.diff(input, rpcInstance);
//            log.debug("Going to copy input to rpc instance: {}", mapper.writeValueAsString(input)); // throws 
//            PatchUtil.apply(diff, rpcInstance); // throws JsonProcessingException
            PatchUtil.copy(input, rpcInstance);
//            log.debug("RPC instance is now: {}", mapper.writeValueAsString(rpcInstance));
        }
        catch(Exception e) {
            log.error("Error while setting task input: {}", e.getMessage());
            faults.add(new InvalidRpcInput(e));  // "Error while preparing task"
        }
    }
    // this method is called to invoke the rpc
    @Override
    public void invoke() {
        try {
            rpcInstance.run();
        }
        catch(Exception e) {
            log.error("Error while executing task: {}", e.getMessage());
            faults.add(new RpcFailed(e));  // "Error while executing task"
        }
    }
    // this method is called to get the output from the rpc,
    // which is also called the intermediate output because
    // it will later be serialized according to the client's
    // preference at delivery time
    @Override
    public Object getOutput() {
        return rpcInstance;
    }
    
    @Override
    public List<Fault> getFaults() { return faults; }
}
