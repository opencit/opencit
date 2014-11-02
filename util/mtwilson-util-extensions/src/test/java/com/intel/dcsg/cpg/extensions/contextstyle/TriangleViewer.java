/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.extensions.contextstyle;

/**
 *
 * @author jbuhacoff
 */
public class TriangleViewer extends ShapeViewer {
    private Triangle triangle;
    public TriangleViewer(Triangle triangle) {
        this.triangle = triangle;
    }
    @Override
    public String getShapeDescription() {
        return String.format("%s triangle with sides %d, %d, and %d", triangle.getColor(), triangle.getSides()[0], triangle.getSides()[1], triangle.getSides()[2] );
    }
    
}
