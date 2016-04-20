/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package test.jdbi;

import com.intel.dcsg.cpg.io.UUID;
import java.util.Date;

/**
 *
 * @author jbuhacoff
 */
public class TestClass {
    private UUID id;
    private String name;
    private Long length;
    private Date created;
    private byte[] content;
    private Boolean flag;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getLength() {
        return length;
    }

    public void setLength(Long length) {
        this.length = length;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public Boolean getFlag() {
        return flag;
    }

    public void setFlag(Boolean flag) {
        this.flag = flag;
    }
    
    
}
