/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.jaxrs2.Document;

/**
 *
 * @author ssbangal
 */
@JacksonXmlRootElement(localName="selection_kv_attribute")
public class SelectionKvAttribute extends Document{
        
    private UUID selectionId;
    private UUID kvAttributeId;
    
    // These are used while returning back the results
    private String kvAttributeName;
    private String kvAttributeValue;
    private String selectionName;
    private String selectionDescription;
    
    public UUID getSelectionId() {
        return selectionId;
    }

    public void setSelectionId(UUID selectionId) {
        this.selectionId = selectionId;
    }

    public UUID getKvAttributeId() {
        return kvAttributeId;
    }

    public void setKvAttributeId(UUID kvAttributeId) {
        this.kvAttributeId = kvAttributeId;
    }

    public String getKvAttributeName() {
        return kvAttributeName;
    }

    public void setKvAttributeName(String kvAttributeName) {
        this.kvAttributeName = kvAttributeName;
    }

    public String getKvAttributeValue() {
        return kvAttributeValue;
    }

    public void setKvAttributeValue(String kvAttributeValue) {
        this.kvAttributeValue = kvAttributeValue;
    }

    public String getSelectionName() {
        return selectionName;
    }

    public void setSelectionName(String selectionName) {
        this.selectionName = selectionName;
    }    

    public String getSelectionDescription() {
        return selectionDescription;
    }

    public void setSelectionDescription(String selectionDescription) {
        this.selectionDescription = selectionDescription;
    }
 
    
}
