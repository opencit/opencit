package com.intel.mtwilson.ms;

import com.intel.mtwilson.ms.controller.TblApiClientJpaController;
import com.intel.mtwilson.ms.data.TblApiClient;
import org.junit.Test;

//import com.intel.mountwilson.as.common.BaseBO;
//import com.intel.mtwilson.ms.data.TblApiClient;

public class TblApiClientJpaControllerTest {

//    @Test
	public void testFindClientIdByPrimaryKey() {
		ManagementBaseBO config = new ManagementBaseBO();
		TblApiClient wlmportalByPrimaryKey = new TblApiClientJpaController(config.getEntityManagerFactory()).findTblApiClient("wlmportal@intel");
		System.out.println("Found Client id " + wlmportalByPrimaryKey.getClientId()+" with secret key +"+wlmportalByPrimaryKey.getSecretKey());
	}

//	@Test
	public void testFindClientIdByClientId() {
		ManagementBaseBO config = new ManagementBaseBO();
		TblApiClient wlmportalByPrimaryKey = new TblApiClientJpaController(config.getEntityManagerFactory()).findTblApiClientByClientId("wlmportal@intel");
		if (wlmportalByPrimaryKey != null)
			System.out.println("Found Client id " + wlmportalByPrimaryKey.getClientId()+" with secret key +"+wlmportalByPrimaryKey.getSecretKey());
	}

}
