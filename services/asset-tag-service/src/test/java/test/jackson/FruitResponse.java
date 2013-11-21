/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.jackson;

/**
 *
 * @author jbuhacoff
 */
public class FruitResponse extends ResponseBase {
    private String color;
    private String name;

    public FruitResponse() {
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
