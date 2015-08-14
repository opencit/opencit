/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package test.fault;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.mtwilson.policy.fault.XmlMeasurementLogValueMismatchEntries;
import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class ReadWriteFaultTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ReadWriteFaultTest.class);

    @Test
    public void testXmlMeasurementLogValueMismatchEntriesEmpty() throws IOException {
        XmlMeasurementLogValueMismatchEntries fault = new XmlMeasurementLogValueMismatchEntries();
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(fault);
        log.debug("fault: {}", json); // {"fault_name":"com.intel.mtwilson.policy.fault.XmlMeasurementLogValueMismatchEntries","faultName":"com.intel.mtwilson.policy.fault.XmlMeasurementLogValueMismatchEntries"}
        XmlMeasurementLogValueMismatchEntries faultFromJson = mapper.readValue(json, XmlMeasurementLogValueMismatchEntries.class);
        assertNotNull(faultFromJson);
        String jsonWithMeasurements = "{\"fault_name\":\"com.intel.mtwilson.policy.fault.XmlMeasurementLogValueMismatchEntries\",\"faultName\":\"com.intel.mtwilson.policy.fault.XmlMeasurementLogValueMismatchEntries\", \"pcrIndex\": \"0\", \"missingEntries\": [ { \"label\":\"wrong_entry\", \"value\":\"95ac39878cdbd4fb1c74cf24e086dd1817982857\" } ]}";
        XmlMeasurementLogValueMismatchEntries faultFromJsonWithMeasurements = mapper.readValue(jsonWithMeasurements, XmlMeasurementLogValueMismatchEntries.class);
        assertNotNull(faultFromJsonWithMeasurements);
    }
    
    @Test
    public void testXmlMeasurementLogValueMismatchEntriesWithMeasurements() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String jsonWithMeasurements = "{\"fault_name\":\"com.intel.mtwilson.policy.fault.XmlMeasurementLogValueMismatchEntries\",\"faultName\":\"com.intel.mtwilson.policy.fault.XmlMeasurementLogValueMismatchEntries\", \"pcrIndex\": \"0\", \"missingEntries\": [ { \"label\":\"wrong_entry\", \"value\":\"95ac39878cdbd4fb1c74cf24e086dd1817982857\" } ]}";
        XmlMeasurementLogValueMismatchEntries faultFromJsonWithMeasurements = mapper.readValue(jsonWithMeasurements, XmlMeasurementLogValueMismatchEntries.class);
        assertNotNull(faultFromJsonWithMeasurements);
    }
    
}
