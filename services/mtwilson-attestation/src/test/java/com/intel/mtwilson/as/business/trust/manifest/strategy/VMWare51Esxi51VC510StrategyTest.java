package com.intel.mtwilson.as.business.trust.manifest.strategy;

import java.util.ArrayList;

import org.junit.Test;

//import com.intel.mountwilson.manifest.strategy.VMWare51Esxi51Strategy;

public class VMWare51Esxi51VC510StrategyTest {

	@Test
	public void test() {
		try {
			ArrayList pcrs = new ArrayList<String>();
			
			pcrs.add("0");
			pcrs.add("17");
			pcrs.add("18");
			pcrs.add("19");
			pcrs.add("20");

			//new VMWare51Esxi51Strategy().getQuoteInformationForHost("10.1.71.151", pcrs, "https://10.1.71.142:444/sdk;Administrator;P@ssw0rd");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
