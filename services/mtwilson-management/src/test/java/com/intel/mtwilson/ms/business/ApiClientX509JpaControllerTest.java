package com.intel.mtwilson.ms.business;

import com.intel.mtwilson.ms.BaseBO;
import com.intel.mtwilson.ms.controller.ApiClientX509JpaController;
import com.intel.mtwilson.ms.data.ApiClientX509;
import org.junit.Test;

//import com.intel.mountwilson.as.common.BaseBO;
//import com.intel.mtwilson.ms.data.TblApiClient;

public class ApiClientX509JpaControllerTest {

//    @Test
	public void testFindClientByPrimaryKey() {
		//ManagementBaseBO config = new ManagementBaseBO();
                BaseBO config = new BaseBO();
		ApiClientX509 apiClient = new ApiClientX509JpaController(config.getMSEntityManagerFactory()).findApiClientX509(1);
		System.out.println("Found Client id " + apiClient.getName());
	}

//	@Test
	public void testFindClientByFignerprint() {
		//ManagementBaseBO config = new ManagementBaseBO();
                BaseBO config = new BaseBO();
		ApiClientX509 apiClient = new ApiClientX509JpaController(config.getMSEntityManagerFactory()).findApiClientX509ByFingerprint(new byte[] { 0, 0, 0, 0 });
		System.out.println("Found Client id " + apiClient.getName());
	}

}
