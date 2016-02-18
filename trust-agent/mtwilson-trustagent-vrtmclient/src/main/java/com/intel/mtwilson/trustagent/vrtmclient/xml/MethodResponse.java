/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.trustagent.vrtmclient.xml;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;


@JacksonXmlRootElement(localName = "methodCall")
public class MethodResponse {
	
    @JacksonXmlProperty(localName = "methodName")
    private String methodName;
    
    @JacksonXmlProperty(localName = "params")
    //@JacksonXmlElementWrapper(useWrapping = false)
    private Param[] params;

    @Override
    public String toString() {
        String str ="<params>";
        for (Param param : params) {
            str = str.concat(param.toString());
        }
        str = str + "</params>";
        return str;
    }

    //getters, setters, toString
    public Param[] getParams() {
        return params;
    }

    public void setParam(Param[] params) {
        this.params = params;
    }
}

