/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.api;

import com.intel.mtwilson.datatypes.*;
import java.io.IOException;
import java.security.SignatureException;
import java.util.List;

/**
 *
 * @author jbuhacoff
 */
public interface WhitelistService {

    boolean addMLE(MleData mle) throws IOException, ApiException, SignatureException;

    boolean updateMLE(MleData mle) throws IOException, ApiException, SignatureException;

    List<MleData> searchMLE(String name) throws IOException, ApiException, SignatureException;

    MleData getMLEManifest(MLESearchCriteria criteria) throws IOException, ApiException, SignatureException;

    boolean deleteMLE(MLESearchCriteria criteria) throws IOException, ApiException, SignatureException;

    List<OemData> listAllOEM() throws IOException, ApiException, SignatureException;

    boolean addOEM(OemData oem) throws IOException, ApiException, SignatureException;

    boolean updateOEM(OemData oem) throws IOException, ApiException, SignatureException;

    boolean deleteOEM(String name) throws IOException, ApiException, SignatureException;

    List<OsData> listAllOS() throws IOException, ApiException, SignatureException;

    boolean updateOS(OsData os) throws IOException, ApiException, SignatureException;

    boolean addOS(OsData os) throws IOException, ApiException, SignatureException;

    boolean deleteOS(OsData os) throws IOException, ApiException, SignatureException;

    boolean addPCRWhiteList(PCRWhiteList pcrObj) throws IOException, ApiException, SignatureException;
    
    boolean updatePCRWhiteList(PCRWhiteList pcrObj) throws IOException, ApiException, SignatureException;
    
    boolean deletePCRWhiteList(PCRWhiteList pcrObj) throws IOException, ApiException, SignatureException;
    
    boolean addModuleWhiteList(ModuleWhiteList moduleObj) throws IOException, ApiException, SignatureException;
    
    boolean updateModuleWhiteList(ModuleWhiteList moduleObj) throws IOException, ApiException, SignatureException;
    
    boolean deleteModuleWhiteList(ModuleWhiteList moduleObj) throws IOException, ApiException, SignatureException;    
    
    List<ModuleWhiteList> listModuleWhiteListForMLE(String mleName, String mleVersion, 
            String osName, String osVersion, String oemName) throws IOException, ApiException, SignatureException;
    
    boolean addMleSource(MleSource mleSourceObj) throws IOException, ApiException, SignatureException;
    
    boolean updateMleSource(MleSource mleSourceObj) throws IOException, ApiException, SignatureException;
    
    boolean deleteMleSource(MleData mleDataObj) throws IOException, ApiException, SignatureException;
    
    String getMleSource(MleData mleDataObj) throws IOException, ApiException, SignatureException;
}
