/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent.ws.v2;

import com.intel.dcsg.cpg.net.IPv4Address;
import com.intel.dcsg.cpg.util.ByteArray;
import com.intel.mountwilson.common.TAException;
import com.intel.mountwilson.trustagent.commands.BuildQuoteXMLCmd;
import com.intel.mountwilson.trustagent.commands.CreateNonceFileCmd;
import com.intel.mountwilson.trustagent.commands.GenerateModulesCmd;
import com.intel.mountwilson.trustagent.commands.GenerateQuoteCmd;
import com.intel.mountwilson.trustagent.commands.ReadIdentityCmd;
import com.intel.mountwilson.trustagent.data.TADataContext;
import com.intel.mtwilson.My;
import com.intel.mtwilson.launcher.ws.ext.V2;
import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.mtwilson.trustagent.TrustagentConfiguration;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import com.intel.mtwilson.trustagent.model.TpmQuoteRequest;
import com.intel.mtwilson.trustagent.model.TpmQuoteResponse;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author jbuhacoff
 */
@V2
@Path("/tpm")
public class Tpm {

    /*
    @POST
    @Path("/quote")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public byte[] tpmQuoteBytes(TpmQuoteRequest tpmQuoteRequest, @Context HttpServletRequest request) throws IOException, TAException {
        // TODO:   return just the tpm quote structure from the tpm which should be verifiable with aikqverify
        return null;
    }
    */
    
    @POST
    @Path("/quote")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_XML)
    public TpmQuoteResponse tpmQuote(TpmQuoteRequest tpmQuoteRequest, @Context HttpServletRequest request) throws IOException, TAException {
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
        if( configuration.isTpmQuoteWithIpAddress() ) {
            IPv4Address ipv4 = new IPv4Address(request.getLocalAddr());
            byte[] extendedNonce = Sha1Digest.digestOf(tpmQuoteRequest.getNonce()).extend(ipv4.toByteArray()).toByteArray(); // again 20 bytes
            tpmQuoteRequest.setNonce(extendedNonce);
        }
        
        
        
            TADataContext context = new TADataContext();

            context.setNonce(Base64.encodeBase64String(tpmQuoteRequest.getNonce()));
            
            context.setSelectedPCRs(StringUtils.join(tpmQuoteRequest.getPcrs(), ','));
            
            new CreateNonceFileCmd(context).execute();
            new ReadIdentityCmd(context).execute();
            // Get the module information
            new GenerateModulesCmd(context).execute();
            new GenerateQuoteCmd(context).execute();
            new BuildQuoteXMLCmd(context).execute();
            
//            return context.getResponseXML();
            return context.getTpmQuoteResponse();
        
    }
}
