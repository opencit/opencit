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
@Table(name = "mw_processor_mapping")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "MwProcessorMapping.findAll", query = "SELECT m FROM MwProcessorMapping m"),
    @NamedQuery(name = "MwProcessorMapping.findById", query = "SELECT m FROM MwProcessorMapping m WHERE m.id = :id"),
    @NamedQuery(name = "MwProcessorMapping.findByPlatformName", query = "SELECT m FROM MwProcessorMapping m WHERE m.platformName = :platformName"),
    @NamedQuery(name = "MwProcessorMapping.findByProcessorType", query = "SELECT m FROM MwProcessorMapping m WHERE m.processorType = :processorType"),
    @NamedQuery(name = "MwProcessorMapping.findByProcessorCpuid", query = "SELECT m FROM MwProcessorMapping m WHERE m.processorCpuid LIKE :processorCpuid")})
public class MwProcessorMapping implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ID")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "platform_name")
    private String platformName;
    @Basic(optional = false)
    @Column(name = "processor_type")
    private String processorType;
    @Column(name = "processor_cpuid")
    private String processorCpuid;

    public MwProcessorMapping() {
    }

    public MwProcessorMapping(Integer id) {
        this.id = id;
    }

    public MwProcessorMapping(Integer id, String platformName, String processorType) {
        this.id = id;
        this.platformName = platformName;
        this.processorType = processorType;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPlatformName() {
        return platformName;
    }

    public void setPlatformName(String platformName) {
        this.platformName = platformName;
    }

    public String getProcessorType() {
        return processorType;
    }

    public void setProcessorType(String processorType) {
        this.processorType = processorType;
    }

    public String getProcessorCpuid() {
        return processorCpuid;
    }

    public void setProcessorCpuid(String processorCpuid) {
        this.processorCpuid = processorCpuid;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof MwProcessorMapping)) {
            return false;
        }
        MwProcessorMapping other = (MwProcessorMapping) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.intel.mtwilson.as.data.MwProcessorMapping[ id=" + id + " ]";
    }
    
}
