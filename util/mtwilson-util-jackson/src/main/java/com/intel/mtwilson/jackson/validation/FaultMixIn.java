/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jackson.validation;

import com.intel.dcsg.cpg.validation.Fault;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.List;

/**
 *
 * @author rksavino
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, property="type")
public abstract class FaultMixIn {

//    @JsonInclude(JsonInclude.Include.NON_EMPTY)
//    public abstract List<Fault> getFaults();
}
