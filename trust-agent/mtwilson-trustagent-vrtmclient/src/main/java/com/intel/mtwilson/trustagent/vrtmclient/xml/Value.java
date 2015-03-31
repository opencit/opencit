/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.trustagent.vrtmclient.xml;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 *
 * @author hxia5
 */

@JacksonXmlRootElement(localName = "value")
public class Value {
    
    @JacksonXmlProperty(localName = "string")
    String string;

    @Override
    public String toString() {
        return "<string>" + string + "</string>";
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }
}