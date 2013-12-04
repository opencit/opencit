/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mountwilson.trustagent.commands;

import com.intel.dcsg.cpg.io.ByteArray;
import java.io.FileOutputStream;
import java.io.IOException;


import org.apache.commons.codec.binary.Base64;

import com.intel.mountwilson.common.ErrorCode;
import com.intel.mountwilson.common.ICommand;
import com.intel.mountwilson.common.TAException;
import com.intel.mountwilson.trustagent.data.TADataContext;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dsmagadX
 */
public class CreateNonceFileCmd implements ICommand {
    
    private  TADataContext context = null;
    
    private Logger log = LoggerFactory.getLogger(getClass().getName());
    
    
    public CreateNonceFileCmd(TADataContext context) {
        this.context = context;
        
    }

    @Override
    public void execute() throws TAException  {
        
        FileOutputStream stream = null;
        try {
        	
        	
			stream = new FileOutputStream(
			        context.getNonceFileName());
			
            // to fix issue #1038 trust agent relay we include the local host's ip address in the nonce so the tpm will sign it 
            // the server sends us a nonce that is 20 random bytes, but the server also replaces the last 4 bytes of ITS OWN COPY of the nonce with our host address
            // so we do the same thing here.   it's important that when this feature is enabled we deliberately replace the last 4 bytes
            // of the nonce with our ip address,  and DO NOT ACCEPT IT from the server.  in other words, we don't let the caller 
            // tell us what to use as the ip address -- we always use the ip address that we configured locally. 
            // See also corresponding code in TAHelper in trust-utils
            byte[] nonce = Base64.decodeBase64(context.getNonce());
            byte[] verifyNonce = nonce;
            if( context.isQuoteWithIPAddress() ) {
                byte[] ipaddress = null;
                InetAddress inetAddress = InetAddress.getByName(context.getIPAddress()); 
                if( inetAddress instanceof Inet4Address ) {
                    ipaddress = inetAddress.getAddress();
                    assert ipaddress.length == 4;                    
                }
                else if( inetAddress instanceof Inet6Address ) {
                    if( ((Inet6Address)inetAddress).isIPv4CompatibleAddress() ) {
                        ipaddress = ByteArray.subarray(inetAddress.getAddress(), 12, 4); // the last 4 bytes of of an ipv4-compatible ipv6 address are the ipv4 address (first 12 bytes are zero)
                    }
                    else {
                        log.error("mtwilson.tpm.quote.ipv4 is enabled and requires an IPv4-compatible address but host address is IPv6: "+context.getIPAddress());                        
                    }
                }
                else {
                    log.error("mtwilson.tpm.quote.ipv4 is enabled but localhost address is not IPv4 compatible: {}", context.getIPAddress());
                }
                if( ipaddress == null ) {
                    throw new IllegalArgumentException("mtwilson.tpm.quote.ipv4 is enabled but localhost address cannot be resolved: "+context.getIPAddress());
                }
                verifyNonce = ByteArray.concat(ByteArray.subarray(nonce,0,16),ipaddress);
            }
            
			stream.write(verifyNonce);
			
			stream.flush();
			
			
		} catch (Exception e) {
			throw new TAException(ErrorCode.ERROR, "Error while creating the nonce file" ,e);
		} finally{
			
				try {
                    if(stream != null){
                        stream.close();
                    }
				} catch (IOException e) {
					log.warn("Error while closing the file stream");
					throw new TAException(ErrorCode.ERROR, "Error while closing file",e );
					
				}
		}
    }
    
}
