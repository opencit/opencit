/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.intel.mtwilson.atag.resource;

import com.intel.mtwilson.api.ApiException;
import com.intel.mtwilson.atag.Global;
import com.intel.mtwilson.atag.dao.Derby;
import com.intel.mtwilson.atag.dao.jdbi.TpmPasswordDAO;
import com.intel.mtwilson.atag.model.TpmPassword;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import java.io.IOException;
import java.security.SignatureException;
import java.sql.SQLException;
import java.util.List;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *
 * @author stdalex
 */
public class TpmPasswordResource extends ServerResource{
    
    private Logger log = LoggerFactory.getLogger(getClass());
    private TpmPasswordDAO dao = null; 
    
    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        
        try {
            log.debug("doInit opening tpm password dao"); //System.out.println("doInit opening tag dao");
            dao = Derby.tpmPasswordDao();
            log.debug("doInit success"); //System.out.println("doInit success");
        } catch (SQLException e) {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Cannot open database", e);
        }
    }


    @Override
    protected void doRelease() throws ResourceException {
        if (dao != null) {
            dao.close();
        }
        super.doRelease();
    }
    
    public class TpmPasswordRequest {
        public String uuid;
        public String password;
        
        public TpmPasswordRequest(){}
        
        public void setUuid(String uuid){
            this.uuid = uuid;
        }
        public String getUuid(){
            return this.uuid;
        }
        public void setPassword(String password) {
            this.password = password;
        }
        public String getPassword(){
            return this.password;
        }
    }

    @Post()
    public void setTpmPassword(/*TagSearchCriteria query*/TpmPasswordRequest request) {
        log.debug("Storing tpm password for " + request.uuid + " with password " + request.password);
        TpmPassword result = dao.findByUuid(request.uuid);
        if(result == null) {
            log.debug("no entry for " + request.uuid + " proceeding with add" );
            dao.insert(request.uuid,request.password);
        }else{
        }
    }
    
    public class TpmPasswordResponse{
        public String password;
        
        public TpmPasswordResponse(){}
        
        public void setPassword(String password) {
            this.password = password;
        }
        
        public String getPassword() {
            return this.password;
        }
    }
    
    @Get("json")
    public TpmPasswordResponse search(/*TagSearchCriteria query*/) throws IOException, ApiException, SignatureException, Exception {
        String uuid = getQuery().getFirstValue("uuid");
        log.debug("made it into tpmpassword got uuid of " + uuid);
        //String ip = getQuery().getFirstValue("ipaddress");
        TpmPasswordResponse response = new TpmPasswordResponse();
        
        TpmPassword result = dao.findByUuid(uuid);
        if(result == null) {
            log.debug("tpm-password did not get any results back for " + uuid);
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return null;
        }
        log.debug("tpm-passwords returned back result for " + uuid + " of " + result.getPassword());
        response.password = result.getPassword();
        return response;
    }
}
