/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.as.data;

import java.io.Serializable;
import java.util.Date;
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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Difference between this class and TblRequestQueue is that an MwRequestLog can record any request at all,
 * and is used to prevent replay attacks, and can also be used for auditing,
 * whereas TblRequestQueue was created specifically to log host trust requests and their results. 
 * 
 * @author jbuhacoff
 */
@Entity
@Table(name = "mw_request_log")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "MwRequestLog.findAll", query = "SELECT a FROM MwRequestLog a"),
    @NamedQuery(name = "MwRequestLog.findById", query = "SELECT a FROM MwRequestLog a WHERE a.id = :id"),
    @NamedQuery(name = "MwRequestLog.findByInstance", query = "SELECT a FROM MwRequestLog a WHERE a.instance = :instance"),
    @NamedQuery(name = "MwRequestLog.findByMd5Hash", query = "SELECT a FROM MwRequestLog a WHERE a.md5_hash = :md5_hash"),
    @NamedQuery(name = "MwRequestLog.findBySource", query = "SELECT a FROM MwRequestLog a WHERE a.source = :source"),
    @NamedQuery(name = "MwRequestLog.findBySourceMd5HashReceivedAfter", query = "SELECT a FROM MwRequestLog a WHERE  a.source = :source AND a.md5_hash = :md5_hash AND a.received > :received"),
    @NamedQuery(name = "MwRequestLog.findByReceived", query = "SELECT a FROM MwRequestLog a WHERE a.received = :received"),
    @NamedQuery(name = "MwRequestLog.findByReceivedAfter", query = "SELECT a FROM MwRequestLog a WHERE a.received > :received"),
    @NamedQuery(name = "MwRequestLog.findByReceivedBefore", query = "SELECT a FROM MwRequestLog a WHERE a.received < :received"),
    @NamedQuery(name = "MwRequestLog.findByContentLike", query = "SELECT a FROM MwRequestLog a WHERE a.content LIKE :content")})
public class MwRequestLog implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ID")
    private Integer id;
    
    @Basic(optional = false)
    @Column(name = "instance")
    private String instance;
    
    @Basic(optional = false)
    @Column(name = "received")
    @Temporal(TemporalType.TIMESTAMP)
    private Date received;

    @Basic(optional = false)
    @Column(name = "source")
    private String source;

    @Lob
    @Basic(optional = false)
    @Column(name = "content")
    private String content;
    
//    @Lob
    @Basic(optional = false)
    @Column(name = "md5_hash")
    private byte[] md5_hash;
    

    public MwRequestLog() {
    }

    public MwRequestLog(Integer id) {
        this.id = id;
    }
    public MwRequestLog(Integer id, String instance, Date received, String source, String content, byte[] md5_hash) {
        this.id = id;
        this.instance = instance;
        this.received = received;
        this.source = source;
        this.content = content;
        this.md5_hash = md5_hash;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    
    public Date getReceived() {
        return received;
    }

    public void setReceived(Date received) {
        this.received = received;
    }



    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    
    public byte[] getMd5Hash() {
        return md5_hash;
    }

    public void setMd5Hash(byte[] md5_hash) {
        this.md5_hash = md5_hash;
    }

    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof MwRequestLog)) {
            return false;
        }
        MwRequestLog other = (MwRequestLog) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.intel.mountwilson.ms.data.MwRequestLog[ id=" + id + " ]";
    }
        
}
