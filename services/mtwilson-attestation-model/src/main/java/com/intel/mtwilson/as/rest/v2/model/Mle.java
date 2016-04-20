/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.mtwilson.datatypes.ManifestData;
import com.intel.mtwilson.jaxrs2.Document;
import java.util.List;

/**
 *
 * @author ssbangal
 */
@JacksonXmlRootElement(localName="mle")
public class Mle extends Document{
    
    public enum MleType {
        BIOS,
        VMM;
    }

    public enum AttestationType {
        PCR,
        MODULE;
    }
    
    private String name;
    private String version;
    private AttestationType attestationType;
    private MleType mleType;
    private String description;
    private String osUuid;
    private String oemUuid;
    private String source; // source host used for whitelisting
    private List<ManifestData> mleManifests;
    private String targetType;
    private String targetValue;
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public AttestationType getAttestationType() {
        return attestationType;
    }

    public void setAttestationType(AttestationType attestationType) {
        this.attestationType = attestationType;
    }

    public MleType getMleType() {
        return mleType;
    }

    public void setMleType(MleType mleType) {
        this.mleType = mleType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOsUuid() {
        return osUuid;
    }

    public void setOsUuid(String osUuid) {
        this.osUuid = osUuid;
    }

    public String getOemUuid() {
        return oemUuid;
    }

    public void setOemUuid(String oemUuid) {
        this.oemUuid = oemUuid;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public List<ManifestData> getMleManifests() {
        return mleManifests;
    }

    public void setMleManifests(List<ManifestData> mleManifests) {
        this.mleManifests = mleManifests;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public String getTargetValue() {
        return targetValue;
    }

    public void setTargetValue(String targetValue) {
        this.targetValue = targetValue;
    }
    
    
    
}
