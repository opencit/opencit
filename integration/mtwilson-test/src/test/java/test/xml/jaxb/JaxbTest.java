/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package test.xml.jaxb;

import com.intel.dcsg.cpg.xml.JAXB;
import com.intel.mtwilson.measurement.xml.FileMeasurementType;
import com.intel.mtwilson.measurement.xml.MeasurementType;
import com.intel.mtwilson.measurement.xml.Measurements;
import java.io.IOException;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class JaxbTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JaxbTest.class);

//    @Test
    public void testWriteXml() throws JAXBException {
        Measurements measurements = new Measurements();
        measurements.setDigestAlg("SHA-1");
        MeasurementType measurement = new FileMeasurementType();
        measurement.setPath("/etc/hosts");
        measurement.setValue("b8b740c0499f2eadb6defcd4ae6700e1bd90dc36");
        measurements.getMeasurements().add(measurement);
        
        JAXB jaxb = new JAXB();
        String xml = jaxb.write(measurements);
        log.debug("xml: {}", xml);
    }
    
//    @Test
    public void testWriteManyXml() throws JAXBException {
        for(int i=0; i<100; i++) {
            testWriteXml();
        }
    }

//    @Test
    public void testReadXml() throws JAXBException, IOException, XMLStreamException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><Measurements xmlns=\"mtwilson:trustdirector:measurements:1.1\" DigestAlg=\"SHA-1\"><File Path=\"/etc/hosts\">b8b740c0499f2eadb6defcd4ae6700e1bd90dc36</File></Measurements>";
        JAXB jaxb = new JAXB();
        Measurements measurements = jaxb.read(xml, Measurements.class);
        assertEquals("b8b740c0499f2eadb6defcd4ae6700e1bd90dc36", measurements.getMeasurements().get(0).getValue());
    }

    // this shows the leak:  # of generated classes increases with every iteration and survives GC
    @Test
    public void testReadManyXml() throws JAXBException, IOException, XMLStreamException {
        for(int i=0; i<1000; i++) {
            long t0 = System.currentTimeMillis();
            testReadXml();
            long t1 = System.currentTimeMillis();
            log.debug("ELAPSED: {}", t1-t0);
            System.gc();            
        }
    }


        
}
