/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson;

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

    /**
     * This API creates new OS information in the system. 
     * 
     * Method type: POST
     * 
     * Sample Call: https://192.168.1.100:8181/WLMService/resources/os
     * 
     * Sample Input: {"Name":"OS Name 1","Version":"v1234","Description":"Test OS"}
     * 
     * Sample Output: True (HTTP Status code: 200)
     * 
     * If the OS Name/Version combination already exists in the database an appropriate error would be returned back.
     * 
     * Sample Input: {"Name":"OS Name 1","Version":"v1234","Description":"New description"}
     * 
     * Sample Output: (Http Status Code: 400)
     * 
     * {
     * "error_code": 1006,
     * "error_message": "Data Error - OS OS Name 1 Version v1234 already exists in the database"
     * }
     * 
     * @param os information comprised of name, version, and optional description
     * @return
     * @throws IOException
     * @throws ApiException
     * @throws SignatureException 
     */
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
