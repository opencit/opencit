/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.ms.data;

import java.io.Serializable;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author dsmagadx
 */
@Entity
@Table(name = "mw_api_role_x509")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ApiRoleX509.findAll", query = "SELECT a FROM ApiRoleX509 a"),
    @NamedQuery(name = "ApiRoleX509.findByApiclientx509ID", query = "SELECT a FROM ApiRoleX509 a WHERE a.apiRoleX509PK.apiclientx509ID = :apiclientx509ID"),
    @NamedQuery(name = "ApiRoleX509.findByRole", query = "SELECT a FROM ApiRoleX509 a WHERE a.apiRoleX509PK.role = :role")})
public class ApiRoleX509 implements Serializable {
    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected ApiRoleX509PK apiRoleX509PK;
    @JoinColumn(name = "api_client_x509_ID", referencedColumnName = "ID", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private ApiClientX509 apiClientX509;

    public ApiRoleX509() {
    }

    public ApiRoleX509(ApiRoleX509PK apiRoleX509PK) {
        this.apiRoleX509PK = apiRoleX509PK;
    }

    public ApiRoleX509(int apiclientx509ID, String role) {
        this.apiRoleX509PK = new ApiRoleX509PK(apiclientx509ID, role);
    }

    public ApiRoleX509PK getApiRoleX509PK() {
        return apiRoleX509PK;
    }

    public void setApiRoleX509PK(ApiRoleX509PK apiRoleX509PK) {
        this.apiRoleX509PK = apiRoleX509PK;
    }

    public ApiClientX509 getApiClientX509() {
        return apiClientX509;
    }

    public void setApiClientX509(ApiClientX509 apiClientX509) {
        this.apiClientX509 = apiClientX509;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (apiRoleX509PK != null ? apiRoleX509PK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ApiRoleX509)) {
            return false;
        }
        ApiRoleX509 other = (ApiRoleX509) object;
        if ((this.apiRoleX509PK == null && other.apiRoleX509PK != null) || (this.apiRoleX509PK != null && !this.apiRoleX509PK.equals(other.apiRoleX509PK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.intel.mountwilson.ms.data.ApiRoleX509[ apiRoleX509PK=" + apiRoleX509PK + " ]";
    }
    
}
