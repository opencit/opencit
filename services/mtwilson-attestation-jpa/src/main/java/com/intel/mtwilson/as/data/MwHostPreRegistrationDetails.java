/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.as.data;

import com.intel.mtwilson.util.ASDataCipher;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
@Entity
@Table(name = "mw_host_pre_registration_details")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "MwHostPreRegistrationDetails.findAll", query = "SELECT m FROM MwHostPreRegistrationDetails m"),
    @NamedQuery(name = "MwHostPreRegistrationDetails.findById", query = "SELECT m FROM MwHostPreRegistrationDetails m WHERE m.id = :id"),
    @NamedQuery(name = "MwHostPreRegistrationDetails.findByName", query = "SELECT m FROM MwHostPreRegistrationDetails m WHERE m.name = :name ORDER BY m.createdTs DESC"),
    @NamedQuery(name = "MwHostPreRegistrationDetails.findByLogin", query = "SELECT m FROM MwHostPreRegistrationDetails m WHERE m.login = :login"),
    @NamedQuery(name = "MwHostPreRegistrationDetails.findByPassword", query = "SELECT m FROM MwHostPreRegistrationDetails m WHERE m.password = :password"),
    @NamedQuery(name = "MwHostPreRegistrationDetails.findByCreatedTs", query = "SELECT m FROM MwHostPreRegistrationDetails m WHERE m.createdTs = :createdTs")})
public class MwHostPreRegistrationDetails implements Serializable {
    @Transient
    private transient Logger log = LoggerFactory.getLogger(getClass());

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "id")
    private String id;
    @Basic(optional = false)
    @Column(name = "name")
    private String name;
    @Basic(optional = false)
    @Column(name = "login")
    private String login;
    @Column(name = "password")
    private String password;
    @Column(name = "created_ts")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdTs;

    @Transient
    private transient String passwordInPlain; // the decrypted version
    
    public MwHostPreRegistrationDetails() {
    }

    public MwHostPreRegistrationDetails(String id) {
        this.id = id;
    }

    public MwHostPreRegistrationDetails(String id, String name, String login) {
        this.id = id;
        this.name = name;
        this.login = login;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        if (passwordInPlain == null && password != null) {
            try {
                passwordInPlain = ASDataCipher.cipher.decryptString(password);
                //log.debug("MwHostPreRegistrationDetails ASDataCipher plainText = {}", passwordInPlain);
                log.debug("MwHostPreRegistrationDetails ASDataCipher cipherText = {}", password);
            } catch (Exception e) {
                log.error("Cannot decrypt host pre-registration credentials", e);
                throw new IllegalArgumentException("Cannot decrypt host pre-registration credentials.");
            }
        }        
        return passwordInPlain;
    }

    public void setPassword(String password) {
        this.passwordInPlain = password;
        if (passwordInPlain == null) {
            this.password = null;
        } else {
            this.password = ASDataCipher.cipher.encryptString(passwordInPlain);
        }        
    }

    public Date getCreatedTs() {
        return createdTs;
    }

    public void setCreatedTs(Date createdTs) {
        this.createdTs = createdTs;
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
        if (!(object instanceof MwHostPreRegistrationDetails)) {
            return false;
        }
        MwHostPreRegistrationDetails other = (MwHostPreRegistrationDetails) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.intel.mtwilson.as.data.MwHostPreRegistrationDetails[ id=" + id + " ]";
    }
    
}
