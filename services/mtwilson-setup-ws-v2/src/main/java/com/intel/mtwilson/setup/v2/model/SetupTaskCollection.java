/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.v2.model;

import com.intel.mtwilson.jaxrs2.DocumentCollection;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author jbuhacoff
 */
@JacksonXmlRootElement(localName="setup_task_collection")
public class SetupTaskCollection extends DocumentCollection<SetupTask> {
    private final ArrayList<SetupTask> files = new ArrayList<SetupTask>();
    
    @JsonInclude(JsonInclude.Include.ALWAYS)                // jackson 2.0
    @JacksonXmlElementWrapper(localName="setup_tasks")
    @JacksonXmlProperty(localName="setup_task")    
    public List<SetupTask> getSetupTasks() { return files; }
    
    @Override
    public List<SetupTask> getDocuments() {
        return getSetupTasks();
    }
    
    
}
