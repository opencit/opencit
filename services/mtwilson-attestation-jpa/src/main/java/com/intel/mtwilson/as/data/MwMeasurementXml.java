/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.as.data;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author ssbangal
 */
@Entity
@Table(name = "mw_measurement_xml")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "MwMeasurementXml.findAll", query = "SELECT m FROM MwMeasurementXml m"),
    @NamedQuery(name = "MwMeasurementXml.findById", query = "SELECT m FROM MwMeasurementXml m WHERE m.id = :id"),
    @NamedQuery(name = "MwMeasurementXml.findByMleID", query = "SELECT m FROM MwMeasurementXml m WHERE m.mleId.id =:mleId"),
    @NamedQuery(name = "MwMeasurementXml.findByContent", query = "SELECT m FROM MwMeasurementXml m WHERE m.content = :content")})
public class MwMeasurementXml implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "id")
    private String id;
    @JoinColumn(name = "mleId", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private TblMle mleId;    
    @Column(name = "content")
    private String content;

    public MwMeasurementXml() {
    }

    public MwMeasurementXml(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TblMle getMleId() {
        return mleId;
    }

    public void setMleId(TblMle mleId) {
        this.mleId = mleId;
    }
    
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof MwMeasurementXml)) {
            return false;
        }
        MwMeasurementXml other = (MwMeasurementXml) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.intel.mtwilson.as.data.MwMeasurementXml[ id=" + id + " ]";
    }
    
}
