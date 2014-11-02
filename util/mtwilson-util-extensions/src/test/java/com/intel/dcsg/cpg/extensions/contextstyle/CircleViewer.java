/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.extensions.contextstyle;

/**
 *
 * @author jbuhacoff
 */
public class CircleViewer extends ShapeViewer {
    private Circle circle;
    public CircleViewer(Circle circle) {
        this.circle = circle;
    }
    @Override
    public String getShapeDescription() {
        return String.format("%s circle with radius %d", circle.getColor(), circle.getRadius());
    }
    
}
