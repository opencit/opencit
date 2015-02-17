/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mountwilson.trustagent.commands;

import com.intel.mountwilson.common.ErrorCode;
import com.intel.mountwilson.common.ICommand;
import com.intel.mountwilson.common.TAException;
import com.intel.mountwilson.trustagent.data.TADataContext;
import com.intel.mtwilson.trustagent.model.TcbMeasurement;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author rksavino
 */
public class RetrieveTcbMeasurement implements ICommand {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RetrieveTcbMeasurement.class);
    private TADataContext context;

    public RetrieveTcbMeasurement(TADataContext context) {
        this.context = context;
    }

    @Override
    public void execute() {
        try {
            InputStream in = new FileInputStream(context.getTcbMeasurementXmlFile());
            String xml = IOUtils.toString(in, Charset.forName("UTF-8"));
            log.info("TCB measurement XML string: {}", xml);

            JAXBContext tcbMeasurementJaxbContext = JAXBContext.newInstance(TcbMeasurement.class);
            Unmarshaller um = tcbMeasurementJaxbContext.createUnmarshaller();
            TcbMeasurement tcbMeasurement = (TcbMeasurement) um.unmarshal(new StringReader(xml));
            log.info("TcbMeasurement unmarshalled successfully.");

            javax.xml.bind.Marshaller m = tcbMeasurementJaxbContext.createMarshaller();
            StringWriter sw = new StringWriter();
            m.marshal(tcbMeasurement, sw);
            log.info("Marshalled TcbMeasurement: {}", sw.toString());
            
            context.setTcbMeasurement(sw.toString());
        } catch (IOException e) {
            log.warn("IOException, invalid measurement.xml: {}", e.getMessage());
        } catch (Exception e) {
            log.warn("Exception, invalid measurement.xml: {}", e.getMessage());
        }
    }
}
