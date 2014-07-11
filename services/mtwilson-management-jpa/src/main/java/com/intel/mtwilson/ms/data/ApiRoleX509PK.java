/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.ms.data;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 *
 * @author dsmagadx
 */
@Embeddable
public class ApiRoleX509PK implements Serializable {
    @Basic(optional = false)
    @Column(name = "api_client_x509_ID")
    private int apiclientx509ID;
    @Basic(optional = false)
    @Column(name = "role")
    private String role;

    public ApiRoleX509PK() {
    }

    public ApiRoleX509PK(int apiclientx509ID, String role) {
        this.apiclientx509ID = apiclientx509ID;
        this.role = role;
    }

    public int getApiclientx509ID() {
        return apiclientx509ID;
    }

    public void setApiclientx509ID(int apiclientx509ID) {
        this.apiclientx509ID = apiclientx509ID;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) apiclientx509ID;
        hash += (role != null ? role.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ApiRoleX509PK)) {
            return false;
        }
        ApiRoleX509PK other = (ApiRoleX509PK) object;
        if (this.apiclientx509ID != other.apiclientx509ID) {
            return false;
        }
        if ((this.role == null && other.role != null) || (this.role != null && !this.role.equals(other.role))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.intel.mountwilson.ms.data.ApiRoleX509PK[ apiclientx509ID=" + apiclientx509ID + ", role=" + role + " ]";
    }
    
}
