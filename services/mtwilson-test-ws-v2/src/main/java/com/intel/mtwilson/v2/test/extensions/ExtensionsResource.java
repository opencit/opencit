/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.v2.test.extensions;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.dcsg.cpg.extensions.Extensions;
import com.intel.mtwilson.launcher.ws.ext.V2;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author jbuhacoff
 */
@V2
@Path("/test/extensions")
public class ExtensionsResource {

    @GET
    @Path("/whiteboard")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public WhiteboardMap getWhiteboard() {
        WhiteboardMap collection = new WhiteboardMap();
        Map<String, List<Class<?>>> whiteboard = Extensions.getWhiteboard();
        for (String key : whiteboard.keySet()) {
            ArrayList<String> extensionClassNames = new ArrayList<String>();
            List<Class<?>> extensions = whiteboard.get(key);
            for (Class extension : extensions) {
                extensionClassNames.add(extension.getName());
            }
            collection.whiteboard.add(new KeyValues(key, extensionClassNames));
        }
        return collection;
    }

    @GET
    @Path("/preferences")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PreferencesMap getPreferences() {
        PreferencesMap collection = new PreferencesMap();
        Map<String, List<String>> preferences = Extensions.getPreferences();
        for (String key : preferences.keySet()) {
            ArrayList<String> extensionClassNames = new ArrayList<String>();
            List<String> extensions = preferences.get(key);
            for (String extension : extensions) {
                extensionClassNames.add(extension);
            }
            collection.preferences.put(key, extensionClassNames);
        }
        return collection;
    }
    
    public static class KeyValues {
        public String key;
        @JsonInclude(JsonInclude.Include.ALWAYS)                // jackson 2.0
        @JacksonXmlElementWrapper(localName = "values")
        @JacksonXmlProperty(localName = "value")
        public List<String> values;
        
        public KeyValues(String key, List<String> values) {
            this.key = key;
            this.values = values;
        }
    }

    @JacksonXmlRootElement(localName = "whiteboard")
    public static class WhiteboardMap {
        public ArrayList<KeyValues> whiteboard = new ArrayList<KeyValues>();
    }

    @JacksonXmlRootElement(localName = "preferences")
    public static class PreferencesMap {

        @JsonInclude(JsonInclude.Include.ALWAYS)                // jackson 2.0
        @JacksonXmlElementWrapper(localName = "preferences")
        @JacksonXmlProperty(localName = "preference")
        public HashMap<String, ArrayList<String>> preferences = new HashMap<String, ArrayList<String>>();
    }
}
