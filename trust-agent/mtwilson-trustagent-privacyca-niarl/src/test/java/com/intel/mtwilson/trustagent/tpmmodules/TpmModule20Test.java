package com.intel.mtwilson.trustagent.tpmmodules;

import java.io.IOException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.mtwilson.trustagent.TrustagentConfiguration;

import gov.niarl.his.privacyca.TpmModule.TpmModuleException;
import gov.niarl.his.privacyca.TpmUtils;

public class TpmModule20Test {	

	/*
	 * 
	 * @author zjj 
	 */
	@BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {

		System.setProperty("mtwilson.application.id", "trustagent");
    }
    
    @After
    public void tearDown() {
    }
    
    /**
     * Test of  method, of class TpmModule20.
     */
	@Test
    public void TestTpmModule20() throws IOException {      
		
		TpmModule20 tpm20=new TpmModule20();
		String tagIndex;
		
		TrustagentConfiguration configuration = TrustagentConfiguration.loadConfiguration();
		
		
		
		
		
		try {
			
			
			tagIndex = tpm20.getAssetTagIndex();
			
			System.out.println("tpm20 tag index :" + tagIndex);
			
			
			
			//System.out.println(TpmUtils.byteArrayToHexString( configuration.getTpmOwnerSecret()));
			
			//tpm20.readAssetTag();
			
		} catch (TpmModuleException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	//	tpm20.readAssetTag(ownerAuth);
    }
}
