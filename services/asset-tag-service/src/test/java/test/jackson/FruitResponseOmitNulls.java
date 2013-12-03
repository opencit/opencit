/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 *
 * @author jbuhacoff
 */
@JsonInclude(Include.NON_NULL)
public class FruitResponseOmitNulls extends ResponseBase {
    private String color;
    private String name;

    public FruitResponseOmitNulls() {
        super();
    }

    public String getColor() {
        return color;
    }

    public String getName() {
        return name;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    
}
