/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
//import org.codehaus.jackson.map.annotate.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.mtwilson.jaxrs2.DocumentCollection;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ssbangal
 */
@JacksonXmlRootElement(localName="selection_kv_attribute_collection")
public class SelectionKvAttributeCollection extends DocumentCollection<SelectionKvAttribute> {
    private final ArrayList<SelectionKvAttribute> selectionKvAttributes = new ArrayList<SelectionKvAttribute>();
    
    @JsonSerialize(include=JsonSerialize.Inclusion.ALWAYS) // jackson 1.9
    @JsonInclude(JsonInclude.Include.ALWAYS)                // jackson 2.0
    @JacksonXmlElementWrapper(localName="selection_kv_attributes")
    @JacksonXmlProperty(localName="selection_kv_attribute")    
    public List<SelectionKvAttribute> getSelectionKvAttributeValues() { return selectionKvAttributes; }

    @Override
    public List<SelectionKvAttribute> getDocuments() {
        return getSelectionKvAttributeValues();
    }
    
}
