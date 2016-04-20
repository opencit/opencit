/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tag.selection.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.mtwilson.tag.selection.SelectionBuilder;
import com.intel.mtwilson.tag.selection.xml.AttributeType;
import com.intel.mtwilson.tag.selection.xml.SelectionType;
import com.intel.mtwilson.tag.selection.xml.SelectionsType;
import com.intel.mtwilson.tag.selection.xml.SubjectType;
import java.io.IOException;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class TagSelectionJsonTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TagSelectionJsonTest.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    
    @BeforeClass
    public static void configureMapper() {
        mapper.registerModule(new TagSelectionModule());
    }

    private void printSelection(SelectionType selection) {
        log.debug("selection id {} name {} notBefore {} notAfter {}", selection.getId(), selection.getName(), selection.getNotBefore(), selection.getNotAfter());
        List<SubjectType> subjectList = selection.getSubject();
        for(SubjectType subject : subjectList) {
            log.debug("subject uuid {} name {} ip {}", (subject.getUuid()==null?"null":subject.getUuid().getValue()), (subject.getName()==null?"null":subject.getName().getValue()), (subject.getIp()==null?"null":subject.getIp().getValue())); // only one will appear 
        }
        List<AttributeType> attributeList = selection.getAttribute();
        for(AttributeType attribute : attributeList) {
            log.debug("attribute oid {} text {}", attribute.getOid(), attribute.getText().getValue());
        }
    }
    private void printSelections(SelectionsType selections) {
        if( selections.getSelection() != null ) {
            log.debug("SELECTIONS");
            List<SelectionType> selectionList = selections.getSelection();
            for(SelectionType selection : selectionList) {
                printSelection(selection);
            }
        }
        if( selections.getDefault() != null ) {
            log.debug("DEFAULT");
            List<SelectionType> defaultSelectionList = selections.getDefault().getSelection();
            for(SelectionType selection : defaultSelectionList) {
                printSelection(selection);
            }
        }
        if( selections.getOptions() != null ) {
            log.debug("OPTIONS");
            if( selections.getOptions().getCache() != null ) {
                log.debug("cache mode {}", (selections.getOptions().getCache().getMode()==null?"null":selections.getOptions().getCache().getMode().value()));
            }
        }
    }
    
    @Test
    public void createTagSelectionJson() throws JsonProcessingException {
        SelectionsType selections = SelectionBuilder.factory().selection().textAttributeKV("Country", "US").build();
        log.debug("selections: {}", mapper.writeValueAsString(selections)); // {"selections":[{"attributes":[{"text":{"value":"Country=US"},"oid":"2.5.4.789.1"}]}]}
    }

    @Test
    public void createTagSelectionJsonById() throws JsonProcessingException {
        SelectionsType selections = SelectionBuilder.factory().selection().name("California Finance").build();
        log.debug("selections: {}", mapper.writeValueAsString(selections)); // {"selections":[{"attributes":[{"text":{"value":"Country=US"},"oid":"2.5.4.789.1"}]}]}
    }
    
    @Test
    public void parseTagSelectionJson() throws IOException {
        String json = "{\"selections\":[{\"attributes\":[{\"text\":{\"value\":\"Country=US\"},\"oid\":\"2.5.4.789.1\"}]}]}";
        SelectionsType selections = mapper.readValue(json, SelectionsType.class);
        printSelections(selections);        
    }

    /**
     * Sample output:
     * <pre>
     * selections: {"options":{"cache":{"mode":"on"}},"default":{"selections":[{"attributes":[{"text":{"value":"Country=US"},"oid":"2.5.4.789.1"}]}]},"selections":[{"subjects":[{"uuid":null,"name":null,"ip":{"value":"192.168.1.100"}}],"attributes":[{"text":{"value":"Country=US"},"oid":"2.5.4.789.1"}]}]}
     * </pre>
     * @throws JsonProcessingException 
     */
    @Test
    public void createTagSelectionWithDefaultJson() throws JsonProcessingException {
        SelectionsType selections = SelectionBuilder.factory()
                .options().cacheMode("on")
                .defaultSelection().textAttributeKV("Country", "US")
                .selection().subjectIp("192.168.1.100").textAttributeKV("Country","US").build();
        log.debug("selections: {}", mapper.writeValueAsString(selections)); // {"selections":[{"attributes":[{"text":{"value":"Country=US"},"oid":"2.5.4.789.1"}]}]}
    }

    @Test
    public void parseTagSelectionWithDefaultJson() throws Exception {
        String json = "{\"options\":{\"cache\":{\"mode\":\"on\"}},\"default\":{\"selections\":[{\"attributes\":[{\"text\":{\"value\":\"Country=US\"},\"oid\":\"2.5.4.789.1\"}]}]},\"selections\":[{\"subjects\":[{\"uuid\":null,\"name\":null,\"ip\":{\"value\":\"192.168.1.100\"}}],\"attributes\":[{\"text\":{\"value\":\"Country=US\"},\"oid\":\"2.5.4.789.1\"}]}]}";
        SelectionsType selections = mapper.readValue(json, SelectionsType.class);
        printSelections(selections);        
    }
}
