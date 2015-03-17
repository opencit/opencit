/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mountwilson.trustagent.commands;

import com.intel.dcsg.cpg.xml.JAXB;
import com.intel.mountwilson.common.ErrorCode;
import com.intel.mountwilson.common.ICommand;
import com.intel.mountwilson.common.TAException;
import com.intel.mountwilson.trustagent.data.TADataContext;
import com.intel.mtwilson.trustagent.measurement.TcbMeasurement;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
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
    public void execute() throws TAException {
        if (!context.getTcbMeasurementXmlFile().exists()) {
            log.warn("TCB measurement XML file not present.");
        } else {
            try {
                InputStream in = new FileInputStream(context.getTcbMeasurementXmlFile());
                String tcbMeasurementString = IOUtils.toString(in, Charset.forName("UTF-8"));
                log.info("TCB measurement XML string: {}", tcbMeasurementString);

//                JAXB jaxb = new JAXB();
//                TcbMeasurement tcbMeasurement = jaxb.read(xml, TcbMeasurement.class);
//                log.info("TcbMeasurement unmarshalled successfully.");
//                String tcbMeasurementString = jaxb.write(tcbMeasurement);
                log.info("Marshalled TcbMeasurement: {}", tcbMeasurementString);

                context.setTcbMeasurement(tcbMeasurementString);
            } catch (IOException e) {
                log.warn("IOException, invalid measurement.xml: {}", e.getMessage());
                throw new TAException(ErrorCode.BAD_REQUEST, "Invalid measurement.xml file. Cannot unmarshal/marshal object using jaxb.");
            } catch (Exception e) {
                log.warn("Exception, invalid measurement.xml: {}", e.getMessage());
                throw new TAException(ErrorCode.BAD_REQUEST, "Invalid measurement.xml file. Cannot unmarshal/marshal object using jaxb.");
            }
        }
    }
}
