/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcg.io;
import java.util.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.xml.*;
import com.fasterxml.jackson.dataformat.xml.annotation.*;
import java.io.IOException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class TestJacksonXml {
    private Logger log = LoggerFactory.getLogger(getClass());
    
public static class Event {
        private int pcr;
        private String initial;
        private String measurement;
        private byte[] data;

        public Event() {
        }

        public Event(int pcr, String initial, String measurement, byte[] data) {
            this.pcr = pcr;
            this.initial = initial;
            this.measurement = measurement;
            this.data = data;
        }

        public byte[] getData() {
            return data;
        }

        public String getInitial() {
            return initial;
        }

        public String getMeasurement() {
            return measurement;
        }

        public int getPcr() {
            return pcr;
        }

        public void setPcr(int pcr) {
            this.pcr = pcr;
        }

        public void setMeasurement(String measurement) {
            this.measurement = measurement;
        }

        public void setData(byte[] data) {
            this.data = data;
        }

        public void setInitial(String initial) {
            this.initial = initial;
        }
        
    }
    
    @JacksonXmlRootElement(localName="event-log")
    public static class EventLogWrapper {
        @JacksonXmlElementWrapper(localName="events")
        private ArrayList<Event> event = new ArrayList<Event>();

        public EventLogWrapper() {
        }

        public EventLogWrapper(ArrayList<Event> eventLog) {
            this.event = eventLog;
        }

        public ArrayList<Event> getEvent() {
            return event;
        }

        public void setEvent(ArrayList<Event> eventLog) {
            this.event = eventLog;
        }
        public void addEvent(Event event) {
            this.event.add(event);
        }
        
        
    }
    
    @Test
    public void testSerializeXml2() throws JsonProcessingException {
        EventLogWrapper eventLog = new EventLogWrapper();
        eventLog.addEvent(new Event(0,"0000000000000000000000000000000000000000","0000000000000000000000000000000000000000",new byte[] {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}));
        eventLog.addEvent(new Event(1,"1111111111111111111111111111111111111111","1111111111111111111111111111111111111111",new byte[] {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}));
        XmlMapper mapper = new XmlMapper();
        log.debug("Event log: {}", mapper.writeValueAsString(eventLog));
        //  <event-log xmlns=""><events><event><pcr>0</pcr><initial>0000000000000000000000000000000000000000</initial><measurement>0000000000000000000000000000000000000000</measurement><data>AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=</data></event><event><pcr>1</pcr><initial>1111111111111111111111111111111111111111</initial><measurement>1111111111111111111111111111111111111111</measurement><data>AQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQE=</data></event></events></event-log>
        
    }
    

    @Test
    public void testDeserializeXml() throws JsonProcessingException, IOException {
        String xml = "<event-log xmlns=\"\"><events><event><pcr>0</pcr><initial>0000000000000000000000000000000000000000</initial><measurement>0000000000000000000000000000000000000000</measurement><data>AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=</data></event><event><pcr>1</pcr><initial>1111111111111111111111111111111111111111</initial><measurement>1111111111111111111111111111111111111111</measurement><data>AQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQE=</data></event></events></event-log>";
        XmlMapper mapper = new XmlMapper();
        EventLogWrapper eventLog = mapper.readValue(xml, EventLogWrapper.class);
        for( Event event : eventLog.getEvent() ) {
            log.debug("Event: {}", String.format("pcr: %d initial: %s measurement: %s data length: %d", event.getPcr(), event.getInitial(), event.getMeasurement(), event.getData().length));
        }
        // output:
//Event: pcr: 0 initial: 0000000000000000000000000000000000000000 measurement: 0000000000000000000000000000000000000000 data length: 32
//Event: pcr: 1 initial: 1111111111111111111111111111111111111111 measurement: 1111111111111111111111111111111111111111 data length: 32
        
    }
}
