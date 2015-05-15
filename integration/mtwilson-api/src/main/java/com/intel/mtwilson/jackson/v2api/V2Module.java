/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jackson.v2api;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;
import com.intel.mtwilson.datatypes.ApiClientCreateRequest;
import com.intel.mtwilson.datatypes.HostConfigData;
import com.intel.mtwilson.datatypes.HostConfigResponse;
import com.intel.mtwilson.datatypes.HostConfigResponseList;
import com.intel.mtwilson.datatypes.ManifestData;
import com.intel.mtwilson.datatypes.MleData;

/**
 *
 * @author rksavino
 */
public class V2Module extends Module {

    @Override
    public String getModuleName() {
        return "V2Module";
    }

    @Override
    public Version version() {
        return new Version(1, 0, 0, "com.intel.mtwilson.util", "mtwilson-util-jackson-v2api", null);
    }

    @Override
    public void setupModule(SetupContext sc) {
        sc.setMixInAnnotations(MleData.class, MleDataMixIn.class);
        sc.setMixInAnnotations(ManifestData.class, ManifestDataMixIn.class);
        sc.setMixInAnnotations(HostConfigData.class, HostConfigDataMixIn.class);
        sc.setMixInAnnotations(ApiClientCreateRequest.class, ApiClientCreateRequestMixIn.class);
        sc.setMixInAnnotations(HostConfigResponse.class, HostConfigResponseMixIn.class);
        sc.setMixInAnnotations(HostConfigResponseList.class, HostConfigResponseListMixIn.class);
    }
}
