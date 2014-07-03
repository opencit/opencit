package com.intel.mtwilson.ms.business;

import com.intel.mtwilson.My;
import com.intel.mtwilson.ms.controller.ApiClientX509JpaController;
import com.intel.mtwilson.ms.data.ApiClientX509;
import java.io.IOException;
import org.junit.Test;

//import com.intel.mountwilson.as.common.BaseBO;
//import com.intel.mtwilson.ms.data.TblApiClient;

public class ApiClientX509JpaControllerTest {

    @Test
	public void testFindClientByPrimaryKey() throws IOException {
		ApiClientX509 apiClient = My.jpa().mwApiClientX509().findApiClientX509(1);
		System.out.println("Found Client id " + apiClient.getName());
	}

	@Test
	public void testFindClientByFignerprint() throws IOException {
		ApiClientX509 apiClient = My.jpa().mwApiClientX509().findApiClientX509ByFingerprint(new byte[] { 0, 0, 0, 0 });
		System.out.println("Found Client id " + apiClient.getName());
	}

}
