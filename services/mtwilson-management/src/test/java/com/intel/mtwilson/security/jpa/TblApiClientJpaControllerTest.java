package com.intel.mtwilson.security.jpa;

import com.intel.mtwilson.My;
import com.intel.mtwilson.ms.data.TblApiClient;
import java.io.IOException;
import org.junit.Test;

public class TblApiClientJpaControllerTest {

    @Test
	public void testFindClientIdByPrimaryKey() throws IOException {
		TblApiClient wlmportalByPrimaryKey = My.jpa().mwApiClientHmac().findTblApiClient("wlmportal@intel");
		System.out.println("Found Client id " + wlmportalByPrimaryKey.getClientId()+" with secret key +"+wlmportalByPrimaryKey.getSecretKey());
	}

	@Test
	public void testFindClientIdByClientId() throws IOException {
		TblApiClient wlmportalByPrimaryKey = My.jpa().mwApiClientHmac().findTblApiClientByClientId("wlmportal@intel");
		if (wlmportalByPrimaryKey != null)
			System.out.println("Found Client id " + wlmportalByPrimaryKey.getClientId()+" with secret key +"+wlmportalByPrimaryKey.getSecretKey());
	}

}
