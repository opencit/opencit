package com.intel.mtwilson.trustagent.ws.v2;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.intel.dcsg.cpg.crypto.RandomUtil;
import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.net.IPv4Address;
import com.intel.mountwilson.common.ErrorCode;
import com.intel.mountwilson.common.TAException;
import com.intel.mountwilson.trustagent.commands.BuildQuoteXMLCmd;
import com.intel.mountwilson.trustagent.commands.CreateNonceFileCmd;
import com.intel.mountwilson.trustagent.commands.GenerateModulesCmd;
import com.intel.mountwilson.trustagent.commands.GenerateQuoteCmd;
import com.intel.mountwilson.trustagent.commands.ReadIdentityCmd;
import com.intel.mountwilson.trustagent.commands.RetrieveTcbMeasurement;
import com.intel.mountwilson.trustagent.data.TADataContext;
import com.intel.mtwilson.trustagent.TrustagentConfiguration;
import com.intel.mtwilson.trustagent.model.TpmQuoteRequest;
import com.intel.mtwilson.trustagent.model.TpmQuoteResponse;
import com.intel.mtwilson.util.exec.EscapeUtil;
import com.intel.mtwilson.util.exec.ExecUtil;
import com.intel.mtwilson.util.exec.Result;

import gov.niarl.his.privacyca.TpmModule.TpmModuleException;

/*
 * before you run this test you need export TRUSTAGENT_PASSWORD=<your password> in linux cmd line
 */

/**
 * 
 * @author zjj
 *
 */
public class TpmTest {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Tpm.class);
	private long t0 = System.currentTimeMillis();
	 
	@BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    	
    	System.setProperty("mtwilson.application.id", "trustagent"); //set your install directory
    	System.setProperty("mtwilson.environment.prefix", "TRUSTAGENT_");
    	
    }
    
    @After
    public void tearDown() {
    }
    
    
    /*
     * 
     * Test methods of tpmQuote use, of class Tpm.
     */	  
    
    @Test
    public void testTpmQuote(){
    	
    	int[] pcrs={0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23};
    	
    	TpmQuoteRequest tpmQuoteRequest = new TpmQuoteRequest();
    	
    	tpmQuoteRequest.setNonce(RandomUtil.randomByteArray(20));    	
    	tpmQuoteRequest.setPcrs(pcrs);
    	
    	boolean isTagProvisioned = false; 
    	 
    	try {
    		
			TrustagentConfiguration configuration = TrustagentConfiguration.loadConfiguration();
			logPerformance("TrustagentConfiguration.loadConfiguration()");
			
			if( configuration.isTpmQuoteWithIpAddress() ) {
	            if( IPv4Address.isValid("172.16.6.130") ) {  //just a test ip address
	                IPv4Address ipv4 = new IPv4Address("172.16.6.130");
	                byte[] extendedNonce = Sha1Digest.digestOf(tpmQuoteRequest.getNonce()).extend(ipv4.toByteArray()).toByteArray(); 
	                tpmQuoteRequest.setNonce(extendedNonce);
	            }
	            else {
	                log.debug("Local address is {}", "172.16.6.130");
	                throw new WebApplicationException(Response.serverError().header("Error", "tpm.quote.ipv4 enabled but local address not IPv4").build());
	            }
	        }
			
			TADataContext context = new TADataContext(); 
	        String osName = System.getProperty("os.name");
	       	log.debug("os name is {}",osName);
	        context.setOsName(osName);
			
	        //get pcrbank 
	        if (tpmQuoteRequest.getPcrbanks() == null)
	            context.setSelectedPcrBanks("SHA1");
	        else
	            context.setSelectedPcrBanks(tpmQuoteRequest.getPcrbanks());
	        
	        //get asset tag hash
			byte[] ownerAuth = configuration.getTpmOwnerSecret();
			byte[] assetTagHash = com.intel.mtwilson.trustagent.tpmmodules.Tpm.getModule().readAssetTag(ownerAuth);
			
			log.debug("Asset Tag is: {}", assetTagHash);
            byte[] extendedNoncewithAssetTag = Sha1Digest.digestOf(tpmQuoteRequest.getNonce()).extend(assetTagHash).toByteArray();
            tpmQuoteRequest.setNonce(extendedNoncewithAssetTag);
            context.setAssetTagHash(Base64.encodeBase64String(extendedNoncewithAssetTag));
            isTagProvisioned = true;

            context.setNonce(Base64.encodeBase64String(tpmQuoteRequest.getNonce()));
            context.setSelectedPCRs(joinIntegers(tpmQuoteRequest.getPcrs(), ' '));
            
            logPerformance("new TADataContext()");
            new CreateNonceFileCmd(context).execute(); 
            logPerformance("CreateNonceFileCmd");
            
            new ReadIdentityCmd(context).execute();  
            logPerformance("ReadIdentityCmd");

            // Get the module information
            if (!osName.toLowerCase().contains("windows")) {
                new GenerateModulesCmd(context).execute(); 
                logPerformance("GenerateModulesCmd");
                
                new RetrieveTcbMeasurement(context).execute(); 
                logPerformance("RetrieveTcbMeasurement");
            }
            
            new GenerateQuoteCmd(context).execute();
            logPerformance("GenerateQuoteCmd");
            
            new BuildQuoteXMLCmd(context).execute();
            logPerformance("BuildQuoteXMLCmd");                
            
            TpmQuoteResponse response = context.getTpmQuoteResponse();
            logPerformance("context.getTpmQuoteResponse()");

           
            if (response != null){
                response.isTagProvisioned = isTagProvisioned;
                if (isTagProvisioned) 
                    response.assetTag = assetTagHash;
            }
          
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
               
                FileUtils.forceDelete(new File(context.getDataFolder())); 
            }
            logPerformance("before return response");
            
            String responseXML =
                    "<client_request> \n"
                    + "<timestamp>" + new Date(System.currentTimeMillis()).toString() + "</timestamp>\n"
                    + "<clientIp>" + "172.16.6.130" + "</clientIp>\n"
                    + "<error_code>" + context.getErrorCode().getErrorCode() + "</error_code>\n"
                    + "<error_message>" + context.getErrorCode().getMessage() + "</error_message>\n"
                    + "<aikcert>" + context.getAIKCertificate() + "</aikcert>\n"
                    + "<quote>" + new String(Base64.encodeBase64(context.getTpmQuote())) + "</quote>\n"
                    + "<eventLog>" + context.getModules() + "</eventLog>\n" //To add the module information into the response.
                    + "<assetTag>" + context.getAssetTagHash() + "<assetTag/>\n"
                    + "<tcbMeasurement>" + context.getAssetTagHash() + "<tcbMeasurement/>\n"
                    + "<selectedPcrBanks>" + context.getSelectedPcrBanks() + "<selectedPcrBanks/>\n"
                    + "<isTagProvisioned>" + context.isQuoteWithIPAddress() + "<isTagProvisioned/>\n"                    
                    + "</client_request>\n";
         System.out.println(responseXML);
            
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TpmModuleException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
    }
    
    private String joinIntegers(int[] pcrs, char separator) {
        String[] array = new String[pcrs.length];
        for(int i=0; i<pcrs.length; i++) {
            array[i] = String.valueOf(pcrs[i]);
        }
        return StringUtils.join(array, separator);
    }
    
    private void logPerformance(String message) {
        long t1 = System.currentTimeMillis();
        log.debug("performance: after {} ms: {}", t1-t0, message);
        t0 = t1;
    }
}
