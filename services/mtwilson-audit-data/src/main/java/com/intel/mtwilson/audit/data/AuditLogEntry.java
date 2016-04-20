/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.audit.data;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author dsmagadx
 */
@Entity
@Table(name = "mw_audit_log_entry")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "AuditLogEntry.findAll", query = "SELECT a FROM AuditLogEntry a"),
    @NamedQuery(name = "AuditLogEntry.findById", query = "SELECT a FROM AuditLogEntry a WHERE a.id = :id"),
    @NamedQuery(name = "AuditLogEntry.findByEntityId", query = "SELECT a FROM AuditLogEntry a WHERE a.entityId = :entityId"),
    @NamedQuery(name = "AuditLogEntry.findByEntityType", query = "SELECT a FROM AuditLogEntry a WHERE a.entityType = :entityType"),
    @NamedQuery(name = "AuditLogEntry.findByFingerPrint", query = "SELECT a FROM AuditLogEntry a WHERE a.fingerPrint = :fingerPrint"),
    @NamedQuery(name = "AuditLogEntry.findByCreateDt", query = "SELECT a FROM AuditLogEntry a WHERE a.createDt = :createDt"),
    @NamedQuery(name = "AuditLogEntry.findByAction", query = "SELECT a FROM AuditLogEntry a WHERE a.action = :action")})
public class AuditLogEntry implements Serializable {
    @Column(name = "transaction_id")
    private String transactionId;
    @Column(name = "entity_id")
    private Integer entityId;
    @Basic(optional = false)
    @Column(name = "create_dt")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createDt;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ID")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "entity_type")
    private String entityType;
    @Basic(optional = false)
    @Column(name = "finger_print")
    private String fingerPrint;
    @Basic(optional = false)
    @Column(name = "action")
    private String action;
    @Lob
    @Column(name = "data")
    private String data;

    public AuditLogEntry() {
    }

    public AuditLogEntry(Integer id) {
        this.id = id;
    }

    public AuditLogEntry(Integer id, int entityId, String entityType, String fingerPrint, Date createDt, String action) {
        this.id = id;
        this.entityId = entityId;
        this.entityType = entityType;
        this.fingerPrint = fingerPrint;
        this.createDt = createDt;
        this.action = action;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getEntityId() {
        return entityId;
    }

    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getFingerPrint() {
        return fingerPrint;
    }

    public void setFingerPrint(String fingerPrint) {
        this.fingerPrint = fingerPrint;
    }

    public Date getCreateDt() {
        return createDt;
    }

    public void setCreateDt(Date createDt) {
        this.createDt = createDt;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof AuditLogEntry)) {
            return false;
        }
        AuditLogEntry other = (AuditLogEntry) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.intel.mtwilson.audit.data.AuditLogEntry[ id=" + id + " ]";
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    
}
