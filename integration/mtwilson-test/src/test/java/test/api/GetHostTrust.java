/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.api;

import com.intel.mtwilson.api.*;
import com.intel.mtwilson.My;
import com.intel.mtwilson.datatypes.ConnectionString;
import com.intel.mtwilson.i18n.ErrorCode;
import com.intel.mtwilson.datatypes.HostConfigData;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import com.intel.mtwilson.datatypes.Vendor;
import java.io.IOException;
import java.net.MalformedURLException;
import java.security.SignatureException;
import java.util.List;
import java.util.ArrayList;
import org.junit.Test;
import static org.junit.Assert.*;
/**
 * 
 * 
 * @author jbuhacoff
 */
public class GetHostTrust {
    

    @Test
    public void testGetHostTrustOk() {
        
    }
    
    @Test
    public void testGetHostTrustFailed() {
        /**
         * The idea here is to register a host in our environment, then randomly change one of the PCR's or modules that
         * were automatically added. Now the host that has the same values that were originally in the whitelist
         * will not match the modified whitelist values and we should get a trust failure report.  
         */
    }

}
