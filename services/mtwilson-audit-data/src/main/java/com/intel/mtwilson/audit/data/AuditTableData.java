/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.audit.data;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
//import org.codehaus.jackson.annotate.JsonProperty;

/**
 *
 * @author dsmagadx
 */
public class AuditTableData {
    
    private List<AuditColumnData> columns = new ArrayList<AuditColumnData>();
    
    @JsonProperty("columns")
    public List<AuditColumnData> getColumns() {
        return columns;
    }
    
    @JsonProperty("columns")
    public void setColumns(List<AuditColumnData> columns) {
        this.columns = columns;
    }
    
}
