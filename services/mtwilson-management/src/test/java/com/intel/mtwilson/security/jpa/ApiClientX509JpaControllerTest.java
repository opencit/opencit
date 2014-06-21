package com.intel.mtwilson.security.jpa;

//import com.intel.mountwilson.ms.common.BaseBO;
import com.intel.mtwilson.My;
import com.intel.mtwilson.ms.controller.ApiClientX509JpaController;
import com.intel.mtwilson.ms.data.ApiClientX509;
import java.io.IOException;
import java.util.List;
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
	public void testFindClientByFingerprint() throws IOException {
		ApiClientX509 apiClient = My.jpa().mwApiClientX509().findApiClientX509ByFingerprint(new byte[] { 0, 0, 0, 0 });
		System.out.println("Found Client id " + apiClient.getName());
	}

	@Test
	public void testFindClientByEnabledStatus() throws IOException {
            ApiClientX509JpaController jpa = My.jpa().mwApiClientX509();
            List<ApiClientX509> apiClients = jpa.findApiClientX509ByEnabledStatus(false, "PENDING");
            System.out.println("Found "+apiClients.size()+" clients");
            for( ApiClientX509 apiClient : apiClients ) {
    		System.out.println("Found Client id " + apiClient.getName());                    
            }
	}

}
