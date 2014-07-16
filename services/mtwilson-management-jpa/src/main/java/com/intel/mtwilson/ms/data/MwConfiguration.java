/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.ms.data;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author jbuhacoff
 */
@Entity
@Table(name = "mw_configuration")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "MwConfiguration.findAll", query = "SELECT a FROM MwConfiguration a"),
    @NamedQuery(name = "MwConfiguration.findByKey", query = "SELECT a FROM MwConfiguration a WHERE a.key = :key"),
    @NamedQuery(name = "MwConfiguration.findByCommentLike", query = "SELECT a FROM MwConfiguration a WHERE a.comment LIKE :comment") // it's the caller's responsibility to add "%" before and/or after the name value
})
public class MwConfiguration implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "key")
    private String key;
    @Basic(optional = false)
    @Column(name = "value")
    private String value;
    @Basic(optional = true)
    @Column(name = "comment")
    private String comment;

    public MwConfiguration() {
    }

    public MwConfiguration(String key) {
        this.key = key;
    }

    public MwConfiguration(String key, String value, String comment) {
        this.key = key;
        this.value = value;
        this.comment = comment;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (key != null ? key.hashCode() : 0);
        hash += (value != null ? value.hashCode() : 0);
        hash += (comment != null ? comment.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof MwConfiguration)) {
            return false;
        }
        MwConfiguration other = (MwConfiguration) object;
        if ((this.key == null && other.key != null) || (this.key != null && !this.key.equals(other.key))) {
            return false;
        }
        if ((this.value == null && other.value != null) || (this.value != null && !this.value.equals(other.value))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.intel.mountwilson.ms.data.MwConfiguration[ key=" + key + " ]";
    }
        
}
