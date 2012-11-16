package com.intel.mtwilson.ms;

import com.intel.mtwilson.ms.controller.ApiClientX509JpaController;
import com.intel.mtwilson.ms.data.ApiClientX509;
import java.util.Calendar;
import java.util.List;
import org.junit.Test;

//import com.intel.mountwilson.as.common.BaseBO;
//import com.intel.mtwilson.ms.data.TblApiClient;

public class ApiClientX509JpaControllerTest {

    //@Test
	public void testFindClientByPrimaryKey() {
		ManagementBaseBO config = new ManagementBaseBO();
		ApiClientX509 apiClient = new ApiClientX509JpaController(config.getEntityManagerFactory()).findApiClientX509(1);
		System.out.println("Found Client id " + apiClient.getName());
	}

//	@Test
	public void testFindClientByFignerprint() {
		ManagementBaseBO config = new ManagementBaseBO();
		ApiClientX509 apiClient = new ApiClientX509JpaController(config.getEntityManagerFactory()).findApiClientX509ByFingerprint(new byte[] { 0, 0, 0, 0 });
		System.out.println("Found Client id " + apiClient.getName());
	}

    @Test
	public void testFindClientByIssuer() {
		ManagementBaseBO config = new ManagementBaseBO();
		List<ApiClientX509> list = new ApiClientX509JpaController(config.getEntityManagerFactory()).findApiClientX509ByIssuer("jonathan");
                if( list == null ) {
                    System.out.println("No records found");
                    return;
                }
                System.out.println("Found "+list.size()+" records");                    
                for(ApiClientX509 apiClient : list) {
                    System.out.println("Found Client id " + apiClient.getName());                    
                }
	}

        @Test
	public void testFindClientByExpiresBefore() {
		ManagementBaseBO config = new ManagementBaseBO();
                Calendar expiresBefore = Calendar.getInstance();
                expiresBefore.set(Calendar.YEAR, 2021);
                expiresBefore.set(Calendar.MONTH, Calendar.JANUARY);
                expiresBefore.set(Calendar.DATE, 1);
		List<ApiClientX509> list = new ApiClientX509JpaController(config.getEntityManagerFactory()).findApiClientX509ByExpiresBefore(expiresBefore.getTime());
                if( list == null ) {
                    System.out.println("No records found");
                    return;
                }
                System.out.println("Found "+list.size()+" records");                    
                for(ApiClientX509 apiClient : list) {
                    System.out.println("Found Client id " + apiClient.getName());                    
                }
	}

    @Test
	public void testFindClientByComment() {
		ManagementBaseBO config = new ManagementBaseBO();
		List<ApiClientX509> list = new ApiClientX509JpaController(config.getEntityManagerFactory()).findApiClientX509ByCommentLike("bootstrap");
                if( list == null ) {
                    System.out.println("No records found");
                    return;
                }
                System.out.println("Found "+list.size()+" records");                    
                for(ApiClientX509 apiClient : list) {
                    System.out.println("Found Client id " + apiClient.getName());                    
                }
	}
        
}
