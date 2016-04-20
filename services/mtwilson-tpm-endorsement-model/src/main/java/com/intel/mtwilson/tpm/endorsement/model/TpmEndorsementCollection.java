/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tpm.endorsement.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.mtwilson.jaxrs2.DocumentCollection;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jbuhacoff
 */
@JacksonXmlRootElement(localName="tpm_endorsement_collection")
public class TpmEndorsementCollection extends DocumentCollection<TpmEndorsement> {
    private final ArrayList<TpmEndorsement> tpmEndorsements = new ArrayList<>();
    
    @JsonInclude(JsonInclude.Include.ALWAYS)                // jackson 2.0
    @JacksonXmlElementWrapper(localName="tpm_endorsements")
    @JacksonXmlProperty(localName="tpm_endorsement")    
    public List<TpmEndorsement> getTpmEndorsements() { return tpmEndorsements; }

    @Override
    public List<TpmEndorsement> getDocuments() {
        return getTpmEndorsements();
    }
    
}
