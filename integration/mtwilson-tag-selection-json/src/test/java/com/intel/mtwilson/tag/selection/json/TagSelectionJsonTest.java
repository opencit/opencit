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
    
    private void printSelections(SelectionsType selections) {
        List<SelectionType> selectionList = selections.getSelection();
        for(SelectionType selection : selectionList) {
            log.debug("selection id {} name {} notBefore {} notAfter {}", selection.getId(), selection.getName(), selection.getNotBefore(), selection.getNotAfter());
            List<SubjectType> subjectList = selection.getSubject();
            for(SubjectType subject : subjectList) {
                log.debug("subject uuid {} name {} ip {}", subject.getUuid(), subject.getName(), subject.getIp()); // only one will appear 
            }
            List<AttributeType> attributeList = selection.getAttribute();
            for(AttributeType attribute : attributeList) {
                log.debug("attribute oid {} text {}", attribute.getOid(), attribute.getText().getValue());
            }
        }
    }
    
    @Test
    public void createTagSelectionJson() throws JsonProcessingException {
        SelectionsType selections = SelectionBuilder.factory().selection().textAttributeKV("Country", "US").build();
        log.debug("selections1: {}", mapper.writeValueAsString(selections)); // {"selections":[{"attributes":[{"text":{"value":"Country=US"},"oid":"2.5.4.789.1"}]}]}
    }
    
    @Test
    public void parseTagSelectionJson() throws IOException {
        String json = "{\"selections\":[{\"attributes\":[{\"text\":{\"value\":\"Country=US\"},\"oid\":\"2.5.4.789.1\"}]}]}";
        SelectionsType selections = mapper.readValue(json, SelectionsType.class);
        printSelections(selections);        
    }
}
