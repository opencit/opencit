package com.intel.mtwilson.security.jpa;

import com.intel.mtwilson.ms.helper.MSPersistenceManager;
import com.intel.mtwilson.ms.controller.TblApiClientJpaController;
import com.intel.mtwilson.ms.data.TblApiClient;
import org.junit.Test;

//import com.intel.mountwilson.as.common.BaseBO;
//import com.intel.mtwilson.ms.data.TblApiClient;

public class TblApiClientJpaControllerTest {

        private MSPersistenceManager persistenceManager = new MSPersistenceManager();

    @Test
	public void testFindClientIdByPrimaryKey() {
		TblApiClient wlmportalByPrimaryKey = new TblApiClientJpaController(persistenceManager.getEntityManagerFactory("MSDataPU")).findTblApiClient("wlmportal@intel");
		System.out.println("Found Client id " + wlmportalByPrimaryKey.getClientId()+" with secret key +"+wlmportalByPrimaryKey.getSecretKey());
	}

	@Test
	public void testFindClientIdByClientId() {
		TblApiClient wlmportalByPrimaryKey = new TblApiClientJpaController(persistenceManager.getEntityManagerFactory("MSDataPU")).findTblApiClientByClientId("wlmportal@intel");
		if (wlmportalByPrimaryKey != null)
			System.out.println("Found Client id " + wlmportalByPrimaryKey.getClientId()+" with secret key +"+wlmportalByPrimaryKey.getSecretKey());
	}

}
