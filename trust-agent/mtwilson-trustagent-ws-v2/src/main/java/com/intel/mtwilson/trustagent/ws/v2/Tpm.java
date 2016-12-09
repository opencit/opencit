/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent.ws.v2;

import com.intel.dcsg.cpg.net.IPv4Address;
import com.intel.mountwilson.common.TAException;
import com.intel.mountwilson.trustagent.commands.BuildQuoteXMLCmd;
import com.intel.mountwilson.trustagent.commands.CreateNonceFileCmd;
import com.intel.mountwilson.trustagent.commands.GenerateModulesCmd;
import com.intel.mountwilson.trustagent.commands.GenerateQuoteCmd;
import com.intel.mountwilson.trustagent.commands.ReadIdentityCmd;
import com.intel.mountwilson.trustagent.data.TADataContext;
import com.intel.mtwilson.launcher.ws.ext.V2;
import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.mountwilson.common.ErrorCode;
import com.intel.mountwilson.trustagent.commands.ReadAssetTag;
import com.intel.mountwilson.trustagent.commands.RetrieveTcbMeasurement;
import com.intel.mtwilson.trustagent.TrustagentConfiguration;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import com.intel.mtwilson.trustagent.model.TpmQuoteRequest;
import com.intel.mtwilson.trustagent.model.TpmQuoteResponse;
import com.intel.mtwilson.util.exec.EscapeUtil;
import java.io.File;
import com.intel.mtwilson.util.exec.ExecUtil;
import com.intel.mtwilson.util.exec.Result;
import gov.niarl.his.privacyca.TpmModule;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author jbuhacoff
 */
@V2
@Path("/tpm")
public class Tpm {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Tpm.class);
    private long t0 = System.currentTimeMillis();
    

    /*
    @POST
    @Path("/quote")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public byte[] tpmQuoteBytes(TpmQuoteRequest tpmQuoteRequest, @Context HttpServletRequest request) throws IOException, TAException {
        return null;
    }
    */
    
    private void logPerformance(String message) {
        long t1 = System.currentTimeMillis();
        log.debug("performance: after {} ms: {}", t1-t0, message);
        t0 = t1;
    }
    
    @POST
    @Path("/quote")
    @Consumes({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
    public TpmQuoteResponse tpmQuote(TpmQuoteRequest tpmQuoteRequest, @Context HttpServletRequest request) throws IOException, TAException {
        logPerformance("inside tpmQuote");
        /**
         * issue #1038 we will hash this ip address together with the input
         * nonce to produce the quote nonce; mtwilson server will do the same
         * thing; this prevents a MITM "quote relay" attack where an attacker
         * can accept quote requests at host A, forward them to trusted host B,
         * and then reply with host B's quote to the challenger (same nonce etc)
         * because with this fix mtwilson is hashing host A's ip address into
         * the nonce on its end, and host B is hashing its ip address into the
         * nonce (here), so the quote will fail the challenger's verification
         * because of the different nonces; Attacker will also not be able to
         * cheat by hashing host B's ip address into the nonce because host B
         * will again has its ip address so it will be double-hashed and fail
         * verification
         */
        TrustagentConfiguration configuration = TrustagentConfiguration.loadConfiguration();
        logPerformance("TrustagentConfiguration.loadConfiguration()");
        if( configuration.isTpmQuoteWithIpAddress() ) {
            if( IPv4Address.isValid(request.getLocalAddr()) ) {
                IPv4Address ipv4 = new IPv4Address(request.getLocalAddr());
                byte[] extendedNonce = Sha1Digest.digestOf(tpmQuoteRequest.getNonce()).extend(ipv4.toByteArray()).toByteArray(); // again 20 bytes
                tpmQuoteRequest.setNonce(extendedNonce);
            }
            else {
                log.debug("Local address is {}", request.getLocalAddr());
                throw new WebApplicationException(Response.serverError().header("Error", "tpm.quote.ipv4 enabled but local address not IPv4").build());
            }
        }
          
        TADataContext context = new TADataContext(); // when we call getSessionId it will create a new random one
        String osName = System.getProperty("os.name");
        context.setOsName(osName);
        
        //set PCR banks only applies to TPM 2.0
        if (tpmQuoteRequest.getPcrbanks() == null)
            context.setSelectedPcrBanks("SHA1");
        else
            context.setSelectedPcrBanks(tpmQuoteRequest.getPcrbanks());

        /* If it is Windows host, Here we read Geotag from nvram index 0x40000010 and do sha1(nonce | geotag) and use the result as the nonce for TPM quote
           As of now, we still keep the same geotag provisioning mechanism by writing it to TPM. there are other approaches as well, but not in implementation.
        */  
        boolean isTagProvisioned = false;        
        byte[] ownerAuth = configuration.getTpmOwnerSecret();
        byte[] assetTagHash = null;
        try {
            assetTagHash = com.intel.mtwilson.trustagent.tpmmodules.Tpm.getModule().readAssetTag(ownerAuth);
            log.debug("Asset Tag is: {}", assetTagHash);
            byte[] extendedNoncewithAssetTag = Sha1Digest.digestOf(tpmQuoteRequest.getNonce()).extend(assetTagHash).toByteArray();
            tpmQuoteRequest.setNonce(extendedNoncewithAssetTag);
            isTagProvisioned = true;
        } catch (TpmModule.TpmModuleException ex) {
            log.debug("Could not read Asset Tag from TPM");
            log.debug("Asset Tag is not provisioned");
        }               

        context.setNonce(Base64.encodeBase64String(tpmQuoteRequest.getNonce()));
        context.setSelectedPCRs(joinIntegers(tpmQuoteRequest.getPcrs(), ' '));

        logPerformance("new TADataContext()");
        new CreateNonceFileCmd(context).execute(); // FileUtils.write to file nonce (binary)
        logPerformance("CreateNonceFileCmd");
        new ReadIdentityCmd(context).execute();  // trustagentrepository.getaikcertificate
        logPerformance("ReadIdentityCmd");

        // Get the module information
        if (!osName.toLowerCase().contains("windows")) {
            new GenerateModulesCmd(context).execute(); // String moduleXml = getXmlFromMeasureLog(configuration);
            logPerformance("GenerateModulesCmd");
            new RetrieveTcbMeasurement(context).execute(); //does nothing if measurement.xml does not exist
            logPerformance("RetrieveTcbMeasurement");
        }
        new GenerateQuoteCmd(context).execute();
        logPerformance("GenerateQuoteCmd");
        new BuildQuoteXMLCmd(context).execute();
        logPerformance("BuildQuoteXMLCmd");
            
        // return context.getResponseXML();
        TpmQuoteResponse response = context.getTpmQuoteResponse();
        logPerformance("context.getTpmQuoteResponse()");

        //assetTag 
        //#6560: Null pointer dereference of 'response' where null is returned from a method
        if (response != null){
            response.isTagProvisioned = isTagProvisioned;
            if (isTagProvisioned) 
                response.assetTag = assetTagHash;
        }

        // delete temporary session directory
        if (!osName.toLowerCase().contains("windows")) {
            CommandLine command = new CommandLine("rm");
            command.addArgument("-rf");
            command.addArgument(EscapeUtil.doubleQuoteEscapeShellArgument(context.getDataFolder()));
            Result result = ExecUtil.execute(command);
            logPerformance("ExecUtil.execute(command)");
            if (result.getExitCode() != 0) {
                log.error("Error running command [{}]: {}", command.getExecutable(), result.getStderr());
                throw new TAException(ErrorCode.ERROR, result.getStderr());
            }
            log.debug("command stdout: {}", result.getStdout());
        } else {
            // we should use more neutral ways to delete the folder
            FileUtils.forceDelete(new File(context.getDataFolder())); 
        }
        logPerformance("before return response");
        return response;
    }
    
    private String joinIntegers(int[] pcrs, char separator) {
        String[] array = new String[pcrs.length];
        for(int i=0; i<pcrs.length; i++) {
            array[i] = String.valueOf(pcrs[i]);
        }
        return StringUtils.join(array, separator);
    }
    
}
