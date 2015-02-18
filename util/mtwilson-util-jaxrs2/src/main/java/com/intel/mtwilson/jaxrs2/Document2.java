/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jaxrs2;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.intel.dcsg.cpg.io.Attributes;
import java.util.ArrayList;
import java.util.List;
//import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * Revision of the Document class which places metadata such as href, 
 * etag, createdOn, and 
 * modifiedOn in the meta field; and uses a list of Link objects for links. 
 * 
 * @author jbuhacoff
 */
//@JsonSerialize(include=JsonSerialize.Inclusion.NON_EMPTY) // jackson 1.9
@JsonInclude(JsonInclude.Include.NON_EMPTY) // jackson 2.0
public abstract class Document2 extends AbstractDocument {
    private final DocumentAttributes meta = new DocumentAttributes();
    private final ArrayList<Link> links = new ArrayList<>();
    
    /**
     * 
     * @return the document attributes object; expected to be modifiable
     */
    public DocumentAttributes getMeta() {
        return meta;
    }

    /**
     * 
     * @return the list of links; expected to be modifiable
     */
    public List<Link> getLinks() {
        return links;
    }
}
