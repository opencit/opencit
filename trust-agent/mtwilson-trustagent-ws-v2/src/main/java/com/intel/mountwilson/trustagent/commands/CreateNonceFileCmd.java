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
			
            byte[] nonce = Base64.decodeBase64(context.getNonce());
			stream.write(nonce);
			
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
