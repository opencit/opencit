/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jackson.v2api;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;
import com.intel.mtwilson.datatypes.ApiClientCreateRequest;
import com.intel.mtwilson.datatypes.AssetTagCertAssociateRequest;
import com.intel.mtwilson.datatypes.AssetTagCertCreateRequest;
import com.intel.mtwilson.datatypes.AssetTagCertRevokeRequest;
import com.intel.mtwilson.datatypes.AttestationReport;
import com.intel.mtwilson.datatypes.AuditLogEntry;
import com.intel.mtwilson.datatypes.AuthResponse;
import com.intel.mtwilson.datatypes.BulkHostTrustResponse;
import com.intel.mtwilson.datatypes.HostConfigData;
import com.intel.mtwilson.datatypes.HostConfigResponse;
import com.intel.mtwilson.datatypes.HostConfigResponseList;
import com.intel.mtwilson.datatypes.MLEVerifyResponse;
import com.intel.mtwilson.datatypes.ManifestData;
import com.intel.mtwilson.datatypes.MleData;
import com.intel.mtwilson.datatypes.ModuleLogReport;
import com.intel.mtwilson.datatypes.ModuleWhiteList;
import com.intel.mtwilson.datatypes.PCRWhiteList;
import com.intel.mtwilson.datatypes.PcrLogReport;
import com.intel.mtwilson.datatypes.PollHostsOutput;
import com.intel.mtwilson.datatypes.TxtHostRecordList;

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
        sc.setMixInAnnotations(AssetTagCertAssociateRequest.class, AssetTagCertAssociateRequestMixIn.class);
        sc.setMixInAnnotations(AssetTagCertCreateRequest.class, AssetTagCertCreateRequestMixIn.class);
        sc.setMixInAnnotations(AssetTagCertRevokeRequest.class, AssetTagCertRevokeRequestMixIn.class);
        sc.setMixInAnnotations(AttestationReport.class, AttestationReportMixIn.class);
        sc.setMixInAnnotations(AuditLogEntry.class, AuditLogEntryMixIn.class);
        sc.setMixInAnnotations(BulkHostTrustResponse.class, BulkHostTrustResponseMixIn.class);
        sc.setMixInAnnotations(ModuleLogReport.class, ModuleLogReportMixIn.class);
        sc.setMixInAnnotations(ModuleWhiteList.class, ModuleWhiteListMixIn.class);
        sc.setMixInAnnotations(PCRWhiteList.class, PCRWhiteListMixIn.class);
        sc.setMixInAnnotations(PcrLogReport.class, PcrLogReportMixIn.class);
        sc.setMixInAnnotations(PollHostsOutput.class, PollHostsOutputMixIn.class);
        sc.setMixInAnnotations(TxtHostRecordList.class, TxtHostRecordListMixIn.class);
    }
}
