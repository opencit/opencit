/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.ms.data;

import java.io.Serializable;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @since 0.5.1
 * @author jbuhacoff
 */
@Entity
@Table(name = "mw_api_client_hmac")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "TblApiClient.findAll", query = "SELECT t FROM TblApiClient t"),
    @NamedQuery(name = "TblApiClient.findByClientId", query = "SELECT t FROM TblApiClient t WHERE t.clientId = :clientId")})
public class TblApiClient implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    @Basic(optional = false)
    @Column(name = "Client_ID")
    private String clientId; // the distinguished name of the api client; should be unique
    
    @Basic(optional = true)
    @Column(name = "Secret_Key")
    private String secretKey; // only for HMAC-SHA256 authentication in 0.5.1;  not used for X509Certificate or RSAPublicKey in 0.5.2
    
    public TblApiClient() {
    }

    public TblApiClient(String clientId) {
        this.clientId = clientId;
    }

    public TblApiClient(String clientId, String secretKey) {
        this.clientId = clientId;
        this.secretKey = secretKey;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String id) {
        this.clientId = id;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (clientId != null ? clientId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof TblApiClient)) {
            return false;
        }
        TblApiClient other = (TblApiClient) object;
        if ((this.clientId == null && other.clientId != null) || (this.clientId != null && !this.clientId.equals(other.clientId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.intel.mountwilson.as.data.TblApiClient[ clientId=" + clientId + " ]";
    }
    
}
