/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tag.selection.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.intel.mtwilson.tag.selection.xml.AttributeType;
import com.intel.mtwilson.tag.selection.xml.SubjectType;
import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * The commented out block applies only to xml serialization; currently
 * only json serialization is used.
 *
 * @author jbuhacoff
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public abstract class SelectionTypeMixIn {
    /*
    // this section wouldn't be necessary if the JaxbAnnotationIntrospector worked properly and used the XmlAttribute annotations...
    @JacksonXmlProperty(isAttribute=true)
    protected String id;
    @JacksonXmlProperty(isAttribute=true)
    protected String name;
    @JacksonXmlProperty(isAttribute=true)
    protected XMLGregorianCalendar notBefore;
    @JacksonXmlProperty(isAttribute=true)
    protected XMLGregorianCalendar notAfter;
    */
    
    @JsonProperty("subjects")
    /*
    @JacksonXmlElementWrapper(localName="subjects")
    @JacksonXmlProperty(localName="subject")    
    */
    protected List<SubjectType> subject;
    
    @JsonProperty("attributes")
    /*
    @JacksonXmlElementWrapper(localName="attributes")
    @JacksonXmlProperty(localName="attribute")    
    */
    protected List<AttributeType> attribute;
    
    // if the mix-in includes both the protected attribute variable and this getter, the JsonProperty annotation must be applied to both or else we get com.fasterxml.jackson.databind.JsonMappingException: Conflicting property name definitions: 'attribute' (for [field com.intel.mtwilson.tag.selection.xml.SelectionType#attribute]) vs 'attributes' (for [method com.intel.mtwilson.tag.selection.xml.SelectionType#getAttribute(0 params)]) (through reference chain: com.intel.mtwilson.tag.selection.xml.SelectionsType["selections"]->java.util.ArrayList[0])
//    @JsonProperty("attributes")
//    abstract List<AttributeType> getAttribute();
}
