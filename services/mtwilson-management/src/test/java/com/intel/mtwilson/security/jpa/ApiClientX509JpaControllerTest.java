package com.intel.mtwilson.security.jpa;

//import com.intel.mountwilson.ms.common.BaseBO;
import com.intel.mtwilson.ms.MSPersistenceManager;
import com.intel.mtwilson.ms.controller.ApiClientX509JpaController;
import com.intel.mtwilson.ms.data.ApiClientX509;
import java.util.List;
import org.junit.Test;

//import com.intel.mountwilson.as.common.BaseBO;
//import com.intel.mtwilson.ms.data.TblApiClient;

public class ApiClientX509JpaControllerTest {

    private MSPersistenceManager persistenceManager = new MSPersistenceManager();
                
    @Test
	public void testFindClientByPrimaryKey() {
		ApiClientX509 apiClient = new ApiClientX509JpaController(persistenceManager.getEntityManagerFactory("MSDataPU")).findApiClientX509(1);
		System.out.println("Found Client id " + apiClient.getName());
	}

//	@Test
	public void testFindClientByFingerprint() {
		ApiClientX509 apiClient = new ApiClientX509JpaController(persistenceManager.getEntityManagerFactory("MSDataPU")).findApiClientX509ByFingerprint(new byte[] { 0, 0, 0, 0 });
		System.out.println("Found Client id " + apiClient.getName());
	}

	@Test
	public void testFindClientByEnabledStatus() {
            ApiClientX509JpaController jpa = new ApiClientX509JpaController(persistenceManager.getEntityManagerFactory("MSDataPU"));
            List<ApiClientX509> apiClients = jpa.findApiClientX509ByEnabledStatus(false, "PENDING");
            System.out.println("Found "+apiClients.size()+" clients");
            for( ApiClientX509 apiClient : apiClients ) {
    		System.out.println("Found Client id " + apiClient.getName());                    
            }
	}

}
