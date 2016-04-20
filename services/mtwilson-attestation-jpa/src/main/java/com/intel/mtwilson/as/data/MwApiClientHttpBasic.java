/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.data;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author ssbangal
 */
@Entity
@Table(name = "mw_api_client_http_basic")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "MwApiClientHttpBasic.findAll", query = "SELECT m FROM MwApiClientHttpBasic m"),
    @NamedQuery(name = "MwApiClientHttpBasic.findById", query = "SELECT m FROM MwApiClientHttpBasic m WHERE m.id = :id"),
    @NamedQuery(name = "MwApiClientHttpBasic.findByUserName", query = "SELECT m FROM MwApiClientHttpBasic m WHERE m.userName = :userName"),
    @NamedQuery(name = "MwApiClientHttpBasic.findByPassword", query = "SELECT m FROM MwApiClientHttpBasic m WHERE m.password = :password")})
public class MwApiClientHttpBasic implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ID")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "user_name")
    private String userName;
    @Basic(optional = false)
    @Column(name = "password")
    private String password;

    public MwApiClientHttpBasic() {
    }

    public MwApiClientHttpBasic(Integer id) {
        this.id = id;
    }

    public MwApiClientHttpBasic(Integer id, String userName, String password) {
        this.id = id;
        this.userName = userName;
        this.password = password;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof MwApiClientHttpBasic)) {
            return false;
        }
        MwApiClientHttpBasic other = (MwApiClientHttpBasic) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.intel.mtwilson.as.data.MwApiClientHttpBasic[ id=" + id + " ]";
    }
    
}
